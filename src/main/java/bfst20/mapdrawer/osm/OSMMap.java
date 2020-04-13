package bfst20.mapdrawer.osm;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OSMMap {

    private final Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();

    private final Map<String, Long> addressToId = new HashMap<>();
    private final List<String> addressList = new ArrayList<>();

    // Empty lookup maps (quickly find an OSM Node/Way/Relation from an ID)
    private final Map<Long, OSMNode> idToNode = new HashMap<>();
    private final Map<Long, OSMWay> idToWay = new HashMap<>();
    private final Map<Long, OSMRelation> idToRelation = new HashMap<>();

    private final float minLat;
    private final float minLon;
    private final float maxLat;
    private final float maxLon;

    private final List<OSMNode> nodes = new ArrayList<>();
    private final List<OSMWay> ways = new ArrayList<>();
    private final List<OSMRelation> relations = new ArrayList<>();

    private final List<Drawable> islands = new ArrayList<>();

    private KdTree kdtree;

    private OSMMap(float minLat, float minLon, float maxLat, float maxLon) {
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
                        map = new OSMMap(-Float.parseFloat(xmlReader.getAttributeValue(null, "maxlat")),
                                0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "minlon")),
                                -Float.parseFloat(xmlReader.getAttributeValue(null, "minlat")),
                                0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "maxlon")));

                        break;
                    case "node": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));
                        float lat = Float.parseFloat(xmlReader.getAttributeValue(null, "lat"));
                        float lon = Float.parseFloat(xmlReader.getAttributeValue(null, "lon"));

                        // Read id, lat, and lon and add a new OSM node (0.56 fixes curvature)
                        // Store this OSM node into a map for fast lookups (used in readWay method)
                        map.idToNode.put(id, new OSMNode(id, 0.56f * lon, -lat));
                        map.addressToId.put(readAddress(map, xmlReader), id);

                        break;
                    }
                    case "way": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readWay method to read all nodes inside of way
                        // Store this OSM way into a map for fast lookups (used in readRelation)
                        map.idToWay.put(id, readWay(map, xmlReader, id));

                        break;
                    }
                    case "relation": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readRelation method to read all ways inside of relation
                        map.idToRelation.put(id, readRelation(xmlReader, map.idToWay, id));

                        break;
                    }
                }
            }
        }

        // Map can be null if osm file is invalid (or no bounds found)
        if (map != null) {
            map.nodes.addAll(map.idToNode.values());
            map.ways.addAll(map.idToWay.values());
            map.relations.addAll(map.idToRelation.values());

            for (var entry : map.nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    map.islands.add(new LinePath(entry.getValue()));
                }
            }

            List<NodeProvider> providers = new ArrayList<>();

            providers.addAll(map.ways);
            providers.addAll(map.relations);

            map.kdtree = new KdTree(providers);
        }

        return map;
    }

    /**
     * readWay will continuously read XML tags until the end of the way is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMWay readWay(OSMMap map, XMLStreamReader xmlReader, long id)
            throws XMLStreamException {
        List<OSMNode> nodes = new ArrayList<>();

        Type type = Type.UNKNOWN;
        OSMWay currentWay = new OSMWay(id, nodes, type);

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "nd":
                        // Found a nd tag within this way, fetch the OSMNode object and add it to the
                        // way
                        nodes.add(map.idToNode.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref"))));
                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            type = Type.BUILDING;

                        } else if (key.equals("highway")) {
                            type = Type.HIGHWAY;

                        } else if (Type.containsType(value)) {
                            type = Type.getType(value);

                            if (type == Type.COASTLINE) {
                                currentWay = new OSMWay(id, nodes, type);

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

        currentWay = new OSMWay(id, nodes, type);
        return currentWay;
    }

    /**
     * readRelation will continuously read XML tags until the end of the relation is
     * found This is a better, and less error-prone, design than reading in the main
     * loop
     */
    private static OSMRelation readRelation(XMLStreamReader xmlReader, Map<Long, OSMWay> idToWay, long id) throws XMLStreamException {
        List<OSMWay> ways = new ArrayList<>();

        Type type = Type.UNKNOWN;
        OSMRelation currentRelation = new OSMRelation(id, ways, type);

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {

                switch (xmlReader.getLocalName()) {
                    case "member":
                        if (xmlReader.getAttributeValue(null, "type").equals("way")) {
                            // If we have found a way member type, fetch the OSMWay object from the fast
                            // lookup and add to ways list
                            ways.add(idToWay.getOrDefault(Long.parseLong(xmlReader.getAttributeValue(null, "ref")),
                                    OSMWay.DUMMY_WAY));
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
                // Once we have reached the end of the relation, break and return the OSM
                // relation
                break;
            }
        }

        currentRelation = new OSMRelation(id, ways, type);
        return currentRelation;
    }

    private static String readAddress(OSMMap map, XMLStreamReader xmlReader) throws XMLStreamException {
        String street = null;
        String houseNumber = null;
        String city = null;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("addr:street")) {
                            street = value;
                        }
                        if (key.equals("addr:housenumber")) {
                            houseNumber = value;
                        }
                        if (key.equals("addr:city")) {
                            city = value;
                        }
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("node")) {
                // Reached the end of the current way, break and return a new OSMWay object
                break;
            }
        }

        String address = street + " " + houseNumber + " " + city;

        if (street != null && houseNumber != null && city != null) {
            map.addressList.add(address);
        }

        return address.toLowerCase();
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

    public List<OSMNode> getNodes() {
        return nodes;
    }

    public List<OSMWay> getWays() {
        return ways;
    }

    public List<OSMRelation> getRelations() {
        return relations;
    }

    public List<Drawable> getIslands() {
        return islands;
    }

    public Map<Long, OSMNode> getIdToNodeMap() {
        return idToNode;
    }

    public KdTree getKdTree() {
        return kdtree;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public Map<String, Long> getAddressToId() {
        return addressToId;
    }

    // Can move this to its own file if needed
    public static final class InvalidMapException extends Exception {
    }
}
