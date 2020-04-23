package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.dijkstra.Dijkstra;
import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.LinePath;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.KdTree;
import bfst20.mapdrawer.kdtree.NodeProvider;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import bfst20.mapdrawer.dijkstra.Graph;
import bfst20.mapdrawer.util.SortedList;

public class OSMMap implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();
    
    private final List<String> addressList = new ArrayList<>();
    private final List<OSMNode> addressNodes = new ArrayList<>();
    
    private SortedList<OSMNode> nodes = new SortedList<>();
    private SortedList<OSMWay> ways = new SortedList<>();
    private SortedList<OSMRelation> relations = new SortedList<>();

    private final double minLat;
    private final double minLon;
    private final double maxLat;
    private final double maxLon;
    
    private final HashMap<Type, KdTree> typeToTree = new HashMap<>();
    private HashMap<Type, List<NodeProvider>> typeToProviders = new HashMap<>();
    private KdTree highwayTree;

    private final List<Drawable> islands = new ArrayList<>();

    private List<OSMWay> highways = new ArrayList<>();

    private final Map<OSMNode, Integer> nodeToInt = new HashMap<>();
    private final Map<Integer, OSMNode> intToNode = new HashMap<>();
    private int nodeNumber = 1;

    private Graph routeGraph;
    private Dijkstra dijkstra;

    private OSMMap(double minLat, double minLon, double maxLat, double maxLon) {

        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public static OSMMap fromFile(File file) throws XMLStreamException, IOException, InvalidMapException {
        // Use charset encoding of UTF-8 (originally Windows-1252) to display ÆØÅ characters properly
        XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file, StandardCharsets.UTF_8));
        
        OSMMap map = null;

        // While there are more tags in the XML file to be read
        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            // If the next element is the start of a tag (e.g. a <node> tag)
            if (nextType == XMLStreamConstants.START_ELEMENT) {
                // Read the local name (ignore any prefix i.e. hello:world -> world)
                String tagName = xmlReader.getLocalName();

                // Handle different opening tags (bounds, node, etc.)
                switch (tagName) {
                    case "bounds":
                        // If map is not null then bounds was declared twice and map is invalid
                        if (map != null) {
                            throw new InvalidMapException();
                        }

                        // Create a new map and flips and fixes the spherical orientation
                        map = new OSMMap(-Double.parseDouble(xmlReader.getAttributeValue(null, "maxlat")),
                                0.56f * Double.parseDouble(xmlReader.getAttributeValue(null, "minlon")),
                                -Double.parseDouble(xmlReader.getAttributeValue(null, "minlat")),
                                0.56f * Double.parseDouble(xmlReader.getAttributeValue(null, "maxlon")));

                        break;
                    case "node": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));
                        double lat = Double.parseDouble(xmlReader.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(xmlReader.getAttributeValue(null, "lon"));
                        String address = readAddress(map, xmlReader);
                        OSMNode node = new OSMNode(id, 0.56f * lon, -lat, -1, address);

                        // Read id, lat, and lon and add a new OSM node (0.56 fixes curvature)
                        // Store this OSM node into a map for fast lookups (used in readWay method)
                        
                        map.nodes.add(node);
                        
                        if (!address.contains("null") && !address.isEmpty()) {
                            map.addressNodes.add(node);
                        }

                        break;
                    }
                    case "way": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readWay method to read all nodes inside of way
                        OSMWay currentWay = readWay(map, xmlReader, id);
                        if (currentWay != null) {
                            map.ways.add(currentWay);
                        }

                        break;
                    }
                    case "relation": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readRelation method to read all ways inside of relation
                        OSMRelation currentRelation = readRelation(map, xmlReader, id);
                        if (currentRelation != null) {
                            map.relations.add(currentRelation);
                        }

                        break;
                    }
                }
            }
        }

        // Map can be null if osm file is invalid (or no bounds found)
        if (map != null) {

            for (var entry : map.nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    map.islands.add(new LinePath(entry.getValue()));
                }
            }
            
            List<NodeProvider> highways = new ArrayList<>();
            
            // If the type has the highway key, add its list to the highways list (and make a kdtree from it below)
            for (Type type : Type.values()) {
                if (type.getKey() != null && type.getKey().equals("highway") && map.typeToProviders.containsKey(type)) {
                    highways.addAll(map.typeToProviders.get(type));
                }
            }
            
            map.highwayTree = new KdTree(highways);
            
            // Create kdtree for each list mapped to types
            for (Map.Entry<Type, List<NodeProvider>> entry : map.typeToProviders.entrySet()) {
                map.typeToTree.put(entry.getKey(), new KdTree(entry.getValue()));
            }

            map.routeGraph = new Graph(20000, map.highways);
            
            map.nodes = null;
            map.ways = null;
            map.relations = null;
            map.nodeToCoastline = null;
            map.typeToProviders = null;
            map.highways = null;
        }

        return map;
    }

    /**
     * readWay will continuously read XML tags until the end of the way is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMWay readWay(OSMMap map, XMLStreamReader xmlReader, long id) throws XMLStreamException {
        List<OSMNode> nodes = new ArrayList<>();

        Type type = Type.UNKNOWN;
        String road = null;
        OSMWay currentWay;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "nd":
                        // Found a nd tag within this way, fetch the OSMNode object and add it to the way
                        nodes.add(map.nodes.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref"))));
                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            type = Type.BUILDING;

                        } else if (key.equals("highway")) {
                            type = Type.HIGHWAY;

                            readTags(key, value, nodes, id, map);

                            if (Type.containsType(value)) type = Type.getType(value);
                            
                        } else if (key.equals("name") && "highway".equals(type.getKey())) {
                            road = value;
                            
                        } else if (Type.containsType(value)) {
                            type = Type.getType(value);

                            if (type == Type.COASTLINE) {
                                currentWay = new OSMWay(id, nodes, type, road);

                                var before = map.nodeToCoastline.remove(currentWay.first());
                                if (before != null) {
                                    map.nodeToCoastline.remove(before.first());
                                    map.nodeToCoastline.remove(before.last());
                                }
                                var after = map.nodeToCoastline.remove(currentWay.last());
                                if (after != null) {
                                    map.nodeToCoastline.remove(after.first());
                                    map.nodeToCoastline.remove(after.last());
                                }
                                currentWay = OSMWay.fromWays(OSMWay.fromWays(before, currentWay), after);
                                map.nodeToCoastline.put(currentWay.first(), currentWay);
                                map.nodeToCoastline.put(currentWay.last(), currentWay);
                            }
                        }

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("way")) {
                // Reached the end of the current way, break and return a new OSMWay object
                break;
            }
        }
        
        if (type == Type.UNKNOWN || type == null) {
            return null;
        }

        currentWay = new OSMWay(id, nodes, type, road);
        
        if (!map.typeToProviders.containsKey(type)) {
            map.typeToProviders.put(type, new ArrayList<>());
        }
        map.typeToProviders.get(type).add(currentWay);
        
        return currentWay;
    }

    private static void readTags(String key, String value, List<OSMNode> list, long id, OSMMap map) {

            for (OSMNode node : list) {
                node.setNumberForGraph(map.nodeNumber);
                map.intToNode.put(map.nodeNumber, node);
                map.nodeToInt.put(node, map.nodeNumber);
                map.nodeNumber++;
            }
            //TODO put road name in way constructor
            map.highways.add(new OSMWay(id, list, Type.SEARCHRESULT, 1, true, true, true, null));
    }
    
    /**
     * readRelation will continuously read XML tags until the end of the relation is
     * found This is a better, and less error-prone, design than reading in the main
     * loop
     */
    private static OSMRelation readRelation(OSMMap map, XMLStreamReader xmlReader, long id) throws XMLStreamException {
        List<OSMWay> ways = new ArrayList<>();

        Type type = Type.UNKNOWN;
        OSMRelation currentRelation = new OSMRelation(id, ways, type);

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {

                switch (xmlReader.getLocalName()) {
                    case "member":
                        if (xmlReader.getAttributeValue(null, "type").equals("way")) {
                            // If we have found a way member type, fetch the OSMWay object and add to ways list
                            OSMWay way = map.ways.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref")));
                            ways.add(Objects.requireNonNullElse(way, OSMWay.DUMMY_WAY));
                        }

                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            type = Type.BUILDING;

                        } else if (Type.containsType(value)) {
                            type = Type.getType(value);
                        }

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("relation")) {
                // Once we have reached the end of the relation, break and return the OSM relation
                break;
            }
        }

        if (type == Type.UNKNOWN || type == null) {
            return null;
        }

        currentRelation = new OSMRelation(id, ways, type);

        if (!map.typeToProviders.containsKey(type)) {
            map.typeToProviders.put(type, new ArrayList<>());
        }
        map.typeToProviders.get(type).add(currentRelation);
        
        return currentRelation;
    }

    private static String readAddress(OSMMap map, XMLStreamReader xmlReader) throws XMLStreamException {
        String street = null, houseNumber = null, postcode = null, place = null, city = null;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                if ("tag".equals(xmlReader.getLocalName())) {

                    String key = xmlReader.getAttributeValue(null, "k");
                    String value = xmlReader.getAttributeValue(null, "v");

                    switch (key) {
                        case "addr:street":
                            street = value;
                            break;
                        case "addr:housenumber":
                            houseNumber = value;
                            break;
                        case "addr:postcode":
                            postcode = value;
                            break;
                        case "addr:place":
                            place = value;
                            break;
                        case "addr:city":
                            city = value;
                            break;
                    }
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("node")) {
                // Reached the end of the current way, break and return a new OSMWay object
                break;
            }
        }

        String address = street + " " + houseNumber + ", " + postcode + " " + place + ", " + city;

        if (!address.contains("null")) {
            map.addressList.add(address);
        }

        return address.toLowerCase();
    }

    public static OSMMap loadBinary(File file) throws IOException {
        OSMMap map = null;
        try(var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))){
            map = (OSMMap) in.readObject();
        } catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
        return map;
    }
    
    public static File unZip(String zipFilePath, String destDir) throws FileNotFoundException {
        File newFile = null;
        // Buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            FileInputStream in = new FileInputStream(zipFilePath);
            ZipInputStream zipIn = new ZipInputStream(in);
            ZipEntry zippedFile = zipIn.getNextEntry(); // Removing this variable crashes the program
            newFile = new File(destDir + File.separator + "unzippedMap.osm");
            FileOutputStream out = new FileOutputStream(newFile);
            int herp;
            while ((herp = zipIn.read(buffer)) > 0)
                out.write(buffer, 0, herp);
            out.close();
            // Close this ZipEntry
            zipIn.closeEntry();
            zippedFile = zipIn.getNextEntry();
            // Close last ZipEntry
            zipIn.closeEntry();
            zipIn.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (newFile == null)
            throw new FileNotFoundException();
        return newFile;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public List<Drawable> getIslands() {
        return islands;
    }

    public HashMap<Type, KdTree> getTypeToTree() {
        return typeToTree;
    }
    
    public KdTree getHighwayTree() {
        return highwayTree;
    }

    public List<String> getAddressList() {
        return addressList;
    }
    
    public List<OSMNode> getAddressNodes() {
        return addressNodes;
    }

    public Map<Integer, OSMNode> getIntToNode() {
        return intToNode;
    }

    public Graph getRouteGraph() {
        return routeGraph;
    }

    // Can move this to its own file if needed
    public static final class InvalidMapException extends Exception {
    }
}
