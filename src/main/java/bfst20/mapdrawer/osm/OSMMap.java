package bfst20.mapdrawer.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import bfst20.mapdrawer.map.PathColor;

public class OSMMap {

    private final float minLat;
    private final float minLon;
    private final float maxLat;
    private final float maxLon;

    // Flags for way type (set by tag elements in readWay and readRelation)
    static boolean building = false;
    static boolean forest = false;
    static boolean coastline = false;

    private final List<OSMNode> nodes = new ArrayList<>();
    private final List<OSMWay> ways = new ArrayList<>();
    private final List<OSMRelation> relations = new ArrayList<>();

    private OSMMap(float minLat, float minLon, float maxLat, float maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public static OSMMap fromFile(File file) throws XMLStreamException, FileNotFoundException, InvalidMapException {
        XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file));

        OSMMap map = null;

        // Empty lookup maps (quickly find an OSM Node/Way/Relation from an ID)
        Map<Long, OSMNode> idToNode = new HashMap<>();
        Map<Long, OSMWay> idToWay = new HashMap<>();
        Map<Long, OSMRelation> idToRelation = new HashMap<>();

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
                        map = new OSMMap(
                            -Float.parseFloat(xmlReader.getAttributeValue(null, "maxlat")),
                            0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "minlon")),
                            -Float.parseFloat(xmlReader.getAttributeValue(null, "minlat")),
                            0.56f * Float.parseFloat(xmlReader.getAttributeValue(null, "maxlon"))
                        );

                        break;
                    case "node": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));
                        float lat = Float.parseFloat(xmlReader.getAttributeValue(null, "lat"));
                        float lon = Float.parseFloat(xmlReader.getAttributeValue(null, "lon"));

                        // Read id, lat, and lon and add a new OSM node (0.56 fixes curvature)
                        // Store this OSM node into a map for fast lookups (used in readWay method)
                        idToNode.put(id, new OSMNode(id, 0.56f * lon, -lat));

                        break;
                    }
                    case "way": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readWay method to read all nodes inside of way
                        // Store this OSM way into a map for fast lookups (used in readRelation)
                        idToWay.put(id, readWay(xmlReader, idToNode, id));

                        break;
                    }
                    case "relation": {
                        long id = Long.parseLong(xmlReader.getAttributeValue(null, "id"));

                        // Read id, and move to readRelation method to read all ways inside of relation
                        idToRelation.put(id, readRelation(xmlReader, idToWay, id));

                        break;
                    }
                }
            }
        }

        // Map can be null if osm file is invalid (or no bounds found)
        if (map != null) {
            map.nodes.addAll(idToNode.values());
            map.ways.addAll(idToWay.values());
            map.relations.addAll(idToRelation.values());
        }

        return map;
    }

    public static File unZip(String zipFilePath, String destDir) throws FileNotFoundException{
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
        if(newFile == null) throw new FileNotFoundException();
        return newFile;
    }

    /**
     * readWay will continuously read XML tags until the end of the way is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMWay readWay(XMLStreamReader xmlReader, Map<Long, OSMNode> idToNode, long id) throws XMLStreamException {
        List<OSMNode> nodes = new ArrayList<>();

        building = false;
        forest = false;
        coastline = false;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {
                switch (xmlReader.getLocalName()) {
                    case "nd":
                        // Found a nd tag within this way, fetch the OSMNode object and add it to the way
                        nodes.add(idToNode.get(Long.parseLong(xmlReader.getAttributeValue(null, "ref"))));
                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            building = true;
                        } else if (key.equals("landuse") && value.equals("forest")) {
                            forest = true;
                        } else if (key.equals("natural") && value.equals("coastline")) {
                            coastline = true;
                        }

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("way")) {
                // Reached the end of the current way, break and return a new OSMWay object
                break;
            }
        }

        // TODO: Make this system better (so you don't need a huge if or switch statement below)
        // TODO: Fix coastlines (will also fix the island's inside color, has to do with Troels' "islands" arraylist in his own code)
        if (building) {
            return new OSMWay(id, nodes, PathColor.BUILDING.getColor());
        } else if (forest) {
            return new OSMWay(id, nodes, PathColor.FOREST.getColor());
        } else if (coastline) {
            return new OSMWay(id, nodes, PathColor.COASTLINE.getColor());
        } else {
            return new OSMWay(id, nodes, PathColor.NONE.getColor());
        }
    }

    /**
     * readRelation will continuously read XML tags until the end of the relation is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMRelation readRelation(XMLStreamReader xmlReader, Map<Long, OSMWay> idToWay, long id) throws XMLStreamException {
        List<OSMWay> ways = new ArrayList<>();

        building = false;
        forest = false;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {

                switch (xmlReader.getLocalName()) {
                    case "member":
                        if (xmlReader.getAttributeValue(null, "type").equals("way")) {
                            // If we have found a way member type, fetch the OSMWay object from the fast lookup and add to ways list
                            ways.add(idToWay.getOrDefault(Long.parseLong(xmlReader.getAttributeValue(null, "ref")), OSMWay.DUMMY_WAY));
                        }

                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        if (key.equals("building")) {
                            building = true;
                        } else if (key.equals("landuse") && value.equals("forest")) {
                            forest = true;
                        } else if (key.equals("natural") && value.equals("coastline")) {
                            coastline = true;
                        }

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("relation")) {
                // Once we have reached the end of the relation, break and return the OSM relation
                break;
            }
        }

        // TODO: Make this system better (so you don't need a huge if or switch statement below)
        if (building) {
            return new OSMRelation(id, ways, PathColor.BUILDING.getColor());
        } else if (forest) {
            return new OSMRelation(id, ways, PathColor.FOREST.getColor());
        } else if (coastline) {
            return new OSMRelation(id, ways, PathColor.COASTLINE.getColor());
        } else {
            return new OSMRelation(id, ways, PathColor.NONE.getColor());
        }
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

    // Can move this to its own file if needed
    public static final class InvalidMapException extends Exception {}
}
