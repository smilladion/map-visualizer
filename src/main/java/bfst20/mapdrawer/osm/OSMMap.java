package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.dijkstra.Graph;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.KdTree;
import bfst20.mapdrawer.util.SortedList;
import javafx.scene.control.Alert;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * This class contains all data necessary for the map to function, and it is used and
 * displayed by the MapView. It has a static method that parses a given OSM file and
 * creates an instance of itself from it. This instance can then be turned into a binary
 * file for faster loading at startup.
 */
public class OSMMap implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final List<OSMNode> addressNodes = new ArrayList<>();
    
    private final float minLat;
    private final float minLon;
    private final float maxLat;
    private final float maxLon;
    
    private final Map<Type, KdTree> typeToTree = new EnumMap<>(Type.class);
    private List<String> addressList = new ArrayList<>();
    private KdTree highwayTree;

    private int nodeNumber = 1;
    private Graph routeGraph;

    private OSMMap(float minLat, float minLon, float maxLat, float maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    /**
     * Parses the given OSM file and returns an OSMMap object containing the necessary data from the file.
     */
    public static OSMMap fromFile(File file) throws XMLStreamException, IOException, InvalidMapException {
        // Use charset encoding of UTF-8 (originally Windows-1252) to display ÆØÅ characters properly
        XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file, StandardCharsets.UTF_8));

        String fileName = file.getName();
        String fileExt = fileName.substring(file.getName().lastIndexOf("."));

        // If zip file, create an XML parser directly from the zip input stream
        if (fileExt.equals(".zip")) {
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            ZipInputStream zipIn = new ZipInputStream(in);
            String zipFileName = zipIn.getNextEntry().getName(); // Points at the first file in the zip, gets its name

            String zipFileExt = zipFileName.substring(zipFileName.lastIndexOf("."));

            if (!zipFileExt.equals(".osm")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fejlmeddelelse");
                alert.setHeaderText(null);
                alert.setContentText("ZIP-filen skal indeholde én OSM-fil, ingen andre filer eller filtyper tilladt.");
                alert.showAndWait();
                return null;
            }

            xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new InputStreamReader(zipIn, StandardCharsets.UTF_8));
        }

        OSMMap map = null;

        SortedList<OSMNode> nodes = new SortedList<>();
        SortedList<OSMWay> ways = new SortedList<>();

        List<NodeProvider> islands = new ArrayList<>();
        Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();

        List<OSMWay> highways = new ArrayList<>();

        EnumMap<Type, List<NodeProvider>> typeToProviders = new EnumMap<>(Type.class);

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
                        map = new OSMMap(-Float.parseFloat(xmlReader.getAttributeValue(null, "maxlat")),
                                0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "minlon")),
                                -Float.parseFloat(xmlReader.getAttributeValue(null, "minlat")),
                                0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "maxlon")));

                        break;
                    case "node": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));
                        float lat = Float.parseFloat(xmlReader.getAttributeValue(null, "lat"));
                        float lon = Float.parseFloat(xmlReader.getAttributeValue(null, "lon"));
                        String address = readAddress(map, xmlReader);

                        OSMNode node = new OSMNode(id, 0.56f * lon, -lat, -1, address);

                        nodes.add(node);

                        // Add to list if the node has an address
                        if (address != null && !address.isBlank()) {
                            map.addressNodes.add(node);
                        }

                        break;
                    }
                    case "way": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readWay method to read all nodes inside of the way
                        OSMWay currentWay = readWay(map, nodes, highways, nodeToCoastline, typeToProviders, xmlReader, id);

                        if (currentWay != null) {
                            ways.add(currentWay);
                        }

                        break;
                    }
                    case "relation": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readRelation method to read all ways inside of the relation
                        readRelation(ways, typeToProviders, xmlReader, id);

                        break;
                    }
                }
            }
        }

        // Map can be null if OSM file is invalid (no bounds found)
        if (map != null) {

            // Adds all the coastlines to a list
            for (var entry : nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    islands.add(entry.getValue());
                }
            }

            List<NodeProvider> providers = new ArrayList<>();

            // If the type has the highway key, add its list to the highways list (and make a kd-tree from it below)
            for (Type type : Type.values()) {
                if (type.getKey() != null && type.getKey().equals("highway") && typeToProviders.containsKey(type)) {
                    providers.addAll(typeToProviders.get(type));
                }
            }

            map.highwayTree = new KdTree(providers);

            // Create a kd-tree for each list mapped to types
            for (Map.Entry<Type, List<NodeProvider>> entry : typeToProviders.entrySet()) {
                map.typeToTree.put(entry.getKey(), new KdTree(entry.getValue()));
            }
            map.typeToTree.put(Type.COASTLINE, new KdTree(islands));

            // Create a graph for this map
            map.routeGraph = new Graph(map.nodeNumber + 1, highways);

            // Remove duplicate strings from the list of addresses
            map.addressList = map.addressList.stream().distinct().collect(Collectors.toList());
        }

        return map;
    }

    /** Reads all necessary data about this way, creates a way from this data, and adds it to an EnumMap for creation of kd-trees. */
    private static OSMWay readWay(OSMMap map, SortedList<OSMNode> nodes, List<OSMWay> highways, Map<OSMNode, OSMWay> nodeToCoastline,
                                  Map<Type, List<NodeProvider>> typeToProviders, XMLStreamReader xmlReader, long id) throws XMLStreamException {
        OSMWay currentWay;
        List<OSMNode> localNodes = new ArrayList<>();

        Type type = Type.UNKNOWN;
        String road = null;

        int speed = 40;
        boolean roundabout = false;

        boolean car = true;
        boolean bike = true;
        boolean walk = true;
        
        boolean onewayCar = false;
        boolean onewayBike = false;
        boolean onewayWalk = false;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "nd":
                        // Found an <nd> tag within this way, fetch the OSMNode object and add it to the way
                        localNodes.add(nodes.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref"))));
                        break;
                    case "tag":
                        // Found a property tag, read and set the correct key and value for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        // Set the correct types depending on what the key/value of this tag is
                        if (key.equals("building")) {
                            type = Type.BUILDING;

                        } else if (key.equals("access")) {
                            if (value.equals("no")) {
                                car = false;
                                bike = false;
                                walk = false;
                                break;
                            }
                        } else if (key.equals("bicycle")) {
                            if (value.equals("yes")) {
                                bike = true;
                            } else if (value.equals("no")) {
                                bike = false;
                            }
                        } else if (key.equals("foot")) {
                            if (value.equals("yes")) {
                                walk = true;
                            } else if (value.equals("no")) {
                                walk = false;
                            }
                        } else if (key.equals("motor-vehicle")) {
                            if (value.equals("yes")) {
                                car = true;
                            } else if (value.equals("no")) {
                                car = false;
                            }
                        } else if (key.equals("highway")) {
                            type = Type.HIGHWAY;
                            
                            if (value.equals("path")) {
                                car = false;
                            }
                            if (value.equals("steps")) {
                                bike = false;
                                car = false;
                            }
                            if (value.equals("residential")) {
                                speed = 30;
                            }
                            if (value.equals("footway")) {
                                car = false;
                                bike = false;
                            }
                            if (value.equals("motorway")) {
                                bike = false;
                                walk = false;
                            }

                            if (Type.containsType(value)) {
                                type = Type.getType(value);
                            }
                        } else if (key.equals("maxspeed")) {
                            try {
                                speed = Integer.parseInt(value);
                            } catch (Exception e) {
                                continue;
                            }
                        } else if (key.equals("name") && "highway".equals(type.getKey())) {
                            road = value;
                            
                        } else if (key.equals("junction")) {
                            if (value.equals("roundabout")) {
                                onewayBike = true;
                                onewayCar = true;
                                roundabout = true;
                            }
                        } else if (key.equals("surface")) {
                            if (value.equals("gravel") || value.equals("unpaved")) {
                                speed = 20;
                            }
                        } else if (key.equals("oneway") && "highway".equals(type.getKey())) {
                            if (value.equals("yes")) {
                                onewayCar = true;
                            }
                        } else if (key.equals("oneway:bicycle")) {
                            if (value.equals("yes")) {
                                onewayBike = true;
                            } else if (value.equals("no")) {
                                onewayBike = false;
                            }
                        } else if (Type.containsType(value) && !key.equals("ferry") && !key.equals("disused:ferry")) {
                            type = Type.getType(value);

                            if (type == Type.COASTLINE) {
                                currentWay = new OSMWay(id, localNodes, type, road);

                                var before = nodeToCoastline.remove(currentWay.first());
                                if (before != null) {
                                    nodeToCoastline.remove(before.first());
                                    nodeToCoastline.remove(before.last());
                                }
                                var after = nodeToCoastline.remove(currentWay.last());
                                if (after != null) {
                                    nodeToCoastline.remove(after.first());
                                    nodeToCoastline.remove(after.last());
                                }
                                currentWay = OSMWay.fromWays(OSMWay.fromWays(before, currentWay), after);
                                nodeToCoastline.put(currentWay.first(), currentWay);
                                nodeToCoastline.put(currentWay.last(), currentWay);
                            }
                        }

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("way")) {
                // Reached the end of the current way, break (and return a new OSMWay object below)
                break;
            }
        }

        if (type == Type.UNKNOWN || type == null) {
            return null;
        }

        if (road != null) {
            currentWay = new OSMWay(id, localNodes, type, road.intern());
        } else {
            currentWay = new OSMWay(id, localNodes, type, road);
        }

        if (!typeToProviders.containsKey(type)) {
            typeToProviders.put(type, new ArrayList<>());
        }

        typeToProviders.get(type).add(currentWay);
        
        // Create a way with route finding info, if it is a highway
        if (road != null || "highway".equals(type.getKey())) {

            for (OSMNode node : localNodes) {
                node.setNumberForGraph(map.nodeNumber);
                map.nodeNumber++;
            }

            highways.add(new OSMWay(id, localNodes, Type.SEARCHRESULT, road, speed, roundabout, car, bike, walk, onewayCar, onewayBike, onewayWalk));
        }

        return currentWay;
    }

    /** Reads all necessary data about this relation and adds it to an EnumMap for creation of kd-trees. */
    private static void readRelation(SortedList<OSMWay> ways, Map<Type, List<NodeProvider>> typeToProviders,
                                     XMLStreamReader xmlReader, long id) throws XMLStreamException {
        List<OSMWay> localWays = new ArrayList<>();

        Type type = Type.UNKNOWN;
        OSMRelation currentRelation;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {

                switch (xmlReader.getLocalName()) {
                    case "member":
                        if (xmlReader.getAttributeValue(null, "type").equals("way")) {
                            // If we have found a way member type, fetch the OSMWay object and add to localWays list
                            OSMWay way = ways.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref")));
                            localWays.add(Objects.requireNonNullElse(way, new OSMWay()));
                        }

                        break;
                    case "tag":
                        // Found a property tag, read and set the correct key and value for this tag
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
            return;
        }

        currentRelation = new OSMRelation(id, localWays, type);

        if (!typeToProviders.containsKey(type)) {
            typeToProviders.put(type, new ArrayList<>());
        }
        typeToProviders.get(type).add(currentRelation);
    }

    /** Reads the attached address of a node, formats plus returns it and adds it to the address list. */
    private static String readAddress(OSMMap map, XMLStreamReader xmlReader) throws XMLStreamException {
        String street = "", houseNumber = "", postcode = "", place = "", city = "";
        boolean addressCheck = false;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                if ("tag".equals(xmlReader.getLocalName())) {

                    String key = xmlReader.getAttributeValue(null, "k");
                    String value = xmlReader.getAttributeValue(null, "v");

                    switch (key) {
                        case "addr:street":
                            street = value + " ";
                            addressCheck = true;
                            break;
                        case "addr:housenumber":
                            houseNumber = value + ", ";
                            addressCheck = true;
                            break;
                        case "addr:postcode":
                            postcode = value + " ";
                            addressCheck = true;
                            break;
                        case "addr:place":
                            place = value + ", ";
                            addressCheck = true;
                            break;
                        case "addr:city":
                            city = value;
                            addressCheck = true;
                            break;
                    }
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("node")) {
                break;
            }
        }

        String address = street + houseNumber + postcode + place + city;

        if (addressCheck) {
            String uniqueString = address.intern();
            map.addressList.add(uniqueString);
            return uniqueString;
        } else {
            return null;
        }
    }

    /** Loads the given binary file, returning the OSMMap within. */
    public static OSMMap loadBinary(File file) throws IOException {
        OSMMap map;
        try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            map = (OSMMap) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /** Loads the given binary file, returning the OSMMap within (used to embed bin file into .jar). */
    public static OSMMap loadBinary(InputStream in) {
        if (in == null) {
            System.err.println("Unable to find resource!");
        } else {
            try (var ois = new ObjectInputStream(new BufferedInputStream(in))) {
                return (OSMMap) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        System.exit(0);
        
        return null;
    }

    public float getMinLat() {
        return minLat;
    }

    public float getMinLon() {
        return minLon;
    }

    public float getMaxLat() {
        return maxLat;
    }

    public float getMaxLon() {
        return maxLon;
    }

    public Map<Type, KdTree> getTypeToTree() {
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

    public Graph getRouteGraph() {
        return routeGraph;
    }

    public static final class InvalidMapException extends Exception {
    }
}
