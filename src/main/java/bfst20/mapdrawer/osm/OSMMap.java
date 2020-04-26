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
import java.util.zip.ZipInputStream;

public class OSMMap implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> addressList = new ArrayList<>();
    private final List<OSMNode> addressNodes = new ArrayList<>();

    private final float minLat;
    private final float minLon;
    private final float maxLat;
    private final float maxLon;

    private final Map<Type, KdTree> typeToTree = new EnumMap<>(Type.class);
    private final List<OSMWay> islands = new ArrayList<>();
    private KdTree highwayTree;
    private int nodeNumber = 1;

    private Graph routeGraph;

    private OSMMap(float minLat, float minLon, float maxLat, float maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public static OSMMap fromFile(File file) throws XMLStreamException, IOException, InvalidMapException {
        // Use charset encoding of UTF-8 (originally Windows-1252) to display ÆØÅ characters properly
        XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file, StandardCharsets.UTF_8));

        String fileName = file.getName();
        String fileExt = fileName.substring(file.getName().lastIndexOf("."));

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

        Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();
        EnumMap<Type, List<NodeProvider>> typeToProviders = new EnumMap<>(Type.class);
        List<OSMWay> highways = new ArrayList<>();

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

                        if (address != null && !address.isBlank()) {
                            map.addressNodes.add(node);
                        }

                        break;
                    }
                    case "way": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readWay method to read all nodes inside of way
                        OSMWay currentWay = readWay(map, nodes, highways, nodeToCoastline, typeToProviders, xmlReader, id);
                        if (currentWay != null) {
                            ways.add(currentWay);
                        }

                        break;
                    }
                    case "relation": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readRelation method to read all ways inside of relation
                        readRelation(ways, typeToProviders, xmlReader, id);

                        break;
                    }
                }
            }
        }

        // Map can be null if osm file is invalid (or no bounds found)
        if (map != null) {

            for (var entry : nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    map.islands.add(entry.getValue());
                }
            }

            List<NodeProvider> providers = new ArrayList<>();

            // If the type has the highway key, add its list to the highways list (and make a kdtree from it below)
            for (Type type : Type.values()) {
                if (type.getKey() != null && type.getKey().equals("highway") && typeToProviders.containsKey(type)) {
                    providers.addAll(typeToProviders.get(type));
                }
            }

            map.highwayTree = new KdTree(providers);

            // Create kdtree for each list mapped to types
            for (Map.Entry<Type, List<NodeProvider>> entry : typeToProviders.entrySet()) {
                map.typeToTree.put(entry.getKey(), new KdTree(entry.getValue()));
            }

            map.routeGraph = new Graph(map.nodeNumber + 1, highways);
        }

        return map;
    }

    /**
     * readWay will continuously read XML tags until the end of the way is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMWay readWay(OSMMap map, SortedList<OSMNode> nodes, List<OSMWay> highways, Map<OSMNode, OSMWay> nodeToCoastline,
                                  Map<Type, List<NodeProvider>> typeToProviders, XMLStreamReader xmlReader, long id) throws XMLStreamException {
        List<OSMNode> localNodes = new ArrayList<>();

        Type type = Type.UNKNOWN;
        String road = null;
        OSMWay currentWay;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "nd":
                        // Found a nd tag within this way, fetch the OSMNode object and add it to the way
                        localNodes.add(nodes.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref"))));
                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            type = Type.BUILDING;

                        } else if (key.equals("highway")) {
                            type = Type.HIGHWAY;

                            if (Type.containsType(value)) type = Type.getType(value);

                        } else if (key.equals("name") && "highway".equals(type.getKey())) {
                            road = value;

                            for (OSMNode node : localNodes) {
                                node.setNumberForGraph(map.nodeNumber);
                                map.nodeNumber++;
                                node.setRoad(road);
                            }

                            highways.add(new OSMWay(id, localNodes, Type.SEARCHRESULT, 1, true, true, true, road));

                        } else if (Type.containsType(value)) {
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
                // Reached the end of the current way, break and return a new OSMWay object
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

        return currentWay;
    }

    /**
     * readRelation will continuously read XML tags until the end of the relation is found.
     * This is a better, and less error-prone, design than reading in the main loop.
     */
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
                            // If we have found a way member type, fetch the OSMWay object and add to ways list
                            OSMWay way = ways.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref")));
                            localWays.add(Objects.requireNonNullElse(way, OSMWay.DUMMY_WAY));
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
            return;
        }

        currentRelation = new OSMRelation(id, localWays, type);

        if (!typeToProviders.containsKey(type)) {
            typeToProviders.put(type, new ArrayList<>());
        }
        typeToProviders.get(type).add(currentRelation);
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
            String uniqueString = address.intern();
            map.addressList.add(uniqueString);
            return uniqueString;
        } else {
            return null;
        }
    }

    public static OSMMap loadBinary(File file) throws IOException {
        OSMMap map = null;
        try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            map = (OSMMap) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return map;
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

    public List<OSMWay> getIslands() {
        return islands;
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

    // Can move this to its own file if needed
    public static final class InvalidMapException extends Exception {
    }
}
