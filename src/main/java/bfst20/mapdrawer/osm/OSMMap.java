package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.LinePath;
import bfst20.mapdrawer.map.PathColor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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

public class OSMMap {

    private final static List<OSMWay> ways = new ArrayList<>();
    // Flags for way type (set by tag elements in readWay and readRelation)
    static boolean building = false;
    static boolean forest = false;
    static boolean coastline = false;
    static boolean water = false;
    static boolean beach = false;
    static boolean commercial = false;
    static boolean construction = false;
    static boolean allotments = false;
    static boolean farmland = false;
    static boolean meadow = false;
    static boolean orchard = false;
    static boolean basin = false;
    static boolean brownfield = false;
    static boolean cemetery = false;
    static boolean grass = false;
    static boolean reservoir = false;
    static boolean villageGreen = false;
    static boolean park = false;
    static boolean dangerArea = false;
    static boolean quarry = false;
    static boolean wood = false;
    static boolean heath = false;
    static boolean grassland = false;
    static boolean scrub = false;
    private static Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();
    private final float minLat;
    private final float minLon;
    private final float maxLat;
    private final float maxLon;
    private final List<OSMNode> nodes = new ArrayList<>();
    private final List<OSMRelation> relations = new ArrayList<>();
    private final List<Drawable> islands = new ArrayList<>();

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

            for (var entry : nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    map.islands.add(new LinePath(entry.getValue()));
                }
            }
        }

        return map;
    }

    /**
     * readWay will continuously read XML tags until the end of the way is found
     * This is a better, and less error-prone, design than reading in the main loop
     */
    private static OSMWay readWay(XMLStreamReader xmlReader, Map<Long, OSMNode> idToNode, long id)
        throws XMLStreamException {
        List<OSMNode> nodes = new ArrayList<>();

        building = false;
        forest = false;
        coastline = false;
        water = false;
        beach = false;
        commercial = false;
        construction = false;
        allotments = false;
        farmland = false;
        meadow = false;
        orchard = false;
        basin = false;
        brownfield = false;
        cemetery = false;
        grass = false;
        reservoir = false;
        villageGreen = false;
        park = false;
        dangerArea = false;
        quarry = false;
        wood = false;
        heath = false;
        grassland = false;
        scrub = false;

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

                        setTag(key, value);

                        break;
                }
            } else if (nextType == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals("way")) {
                // Reached the end of the current way, break and return a new OSMWay object
                break;
            }
        }

        // TODO: Make this system better (so you don't need a huge if or switch statement below)
        if (building) {
            return new OSMWay(id, nodes, PathColor.BUILDING.getColor());
        } else if (forest) {
            return new OSMWay(id, nodes, PathColor.FOREST.getColor());
        } else if (coastline) {
            OSMWay currentWay = new OSMWay(id, nodes, PathColor.COASTLINE.getColor());

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

            return currentWay;
        } else if (water) {
            return new OSMWay(id, nodes, PathColor.WATER.getColor());
        } else if (beach) {
            return new OSMWay(id, nodes, PathColor.BEACH.getColor());
        } else if (commercial) {
            return new OSMWay(id, nodes, PathColor.COMMERCIAL.getColor());
        } else if (construction) {
            return new OSMWay(id, nodes, PathColor.CONSTRUCTION.getColor());
        } else if (allotments) {
            return new OSMWay(id, nodes, PathColor.ALLOTMENTS.getColor());
        } else if (farmland) {
            return new OSMWay(id, nodes, PathColor.FARMLAND.getColor());
        } else if (meadow) {
            return new OSMWay(id, nodes, PathColor.MEADOW.getColor());
        } else if (orchard) {
            return new OSMWay(id, nodes, PathColor.ORCHARD.getColor());
        } else if (basin) {
            return new OSMWay(id, nodes, PathColor.BASIN.getColor());
        } else if (brownfield) {
            return new OSMWay(id, nodes, PathColor.BROWNFIELD.getColor());
        } else if (cemetery) {
            return new OSMWay(id, nodes, PathColor.CEMETERY.getColor());
        } else if (grass) {
            return new OSMWay(id, nodes, PathColor.GRASS.getColor());
        } else if (reservoir) {
            return new OSMWay(id, nodes, PathColor.RESERVOIR.getColor());
        } else if (villageGreen) {
            return new OSMWay(id, nodes, PathColor.VILLAGE_GREEN.getColor());
        } else if (park) {
            return new OSMWay(id, nodes, PathColor.PARK.getColor());
        } else if (dangerArea) {
            return new OSMWay(id, nodes, PathColor.DANGER_AREA.getColor());
        } else if (quarry) {
            return new OSMWay(id, nodes, PathColor.QUARRY.getColor());
        } else if (wood) {
            return new OSMWay(id, nodes, PathColor.WOOD.getColor());
        } else if (heath) {
            return new OSMWay(id, nodes, PathColor.HEATH.getColor());
        } else if (grassland) {
            return new OSMWay(id, nodes, PathColor.GRASSLAND.getColor());
        } else if (scrub) {
            return new OSMWay(id, nodes, PathColor.SCRUB.getColor());
        } else {
            return new OSMWay(id, nodes, PathColor.NONE.getColor());
        }
    }

    /**
     * readRelation will continuously read XML tags until the end of the relation is
     * found This is a better, and less error-prone, design than reading in the main
     * loop
     */
    private static OSMRelation readRelation(XMLStreamReader xmlReader, Map<Long, OSMWay> idToWay, long id)
        throws XMLStreamException {
        List<OSMWay> ways = new ArrayList<>();

        building = false;
        forest = false;
        water = false;
        beach = false;
        commercial = false;
        construction = false;
        allotments = false;
        farmland = false;
        meadow = false;
        orchard = false;
        basin = false;
        brownfield = false;
        cemetery = false;
        grass = false;
        reservoir = false;
        villageGreen = false;
        park = false;
        dangerArea = false;
        quarry = false;
        wood = false;
        heath = false;
        grassland = false;
        scrub = false;

        while (xmlReader.hasNext()) {
            int nextType = xmlReader.next();

            if (nextType == XMLStreamReader.START_ELEMENT) {

                switch (xmlReader.getLocalName()) {
                    case "member":
                        if (xmlReader.getAttributeValue(null, "type").equals("way")) {
                            // If we have found a way member type, fetch the OSMWay object from the fast lookup and add to ways list
                            ways.add(idToWay.getOrDefault(Long.parseLong(xmlReader.getAttributeValue(null, "ref")),
                                OSMWay.DUMMY_WAY));
                        }

                        break;
                    case "tag":
                        // Found a property tag, read and set the correct boolean for this tag
                        String key = xmlReader.getAttributeValue(null, "k");
                        String value = xmlReader.getAttributeValue(null, "v");

                        setTag(key, value);

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
        } else if (water) {
            return new OSMRelation(id, ways, PathColor.WATER.getColor());
        } else if (beach) {
            return new OSMRelation(id, ways, PathColor.BEACH.getColor());
        } else if (commercial) {
            return new OSMRelation(id, ways, PathColor.COMMERCIAL.getColor());
        } else if (construction) {
            return new OSMRelation(id, ways, PathColor.CONSTRUCTION.getColor());
        } else if (allotments) {
            return new OSMRelation(id, ways, PathColor.ALLOTMENTS.getColor());
        } else if (farmland) {
            return new OSMRelation(id, ways, PathColor.FARMLAND.getColor());
        } else if (meadow) {
            return new OSMRelation(id, ways, PathColor.MEADOW.getColor());
        } else if (orchard) {
            return new OSMRelation(id, ways, PathColor.ORCHARD.getColor());
        } else if (basin) {
            return new OSMRelation(id, ways, PathColor.BASIN.getColor());
        } else if (brownfield) {
            return new OSMRelation(id, ways, PathColor.BROWNFIELD.getColor());
        } else if (cemetery) {
            return new OSMRelation(id, ways, PathColor.CEMETERY.getColor());
        } else if (grass) {
            return new OSMRelation(id, ways, PathColor.GRASS.getColor());
        } else if (reservoir) {
            return new OSMRelation(id, ways, PathColor.RESERVOIR.getColor());
        } else if (villageGreen) {
            return new OSMRelation(id, ways, PathColor.VILLAGE_GREEN.getColor());
        } else if (park) {
            return new OSMRelation(id, ways, PathColor.PARK.getColor());
        } else if (dangerArea) {
            return new OSMRelation(id, ways, PathColor.DANGER_AREA.getColor());
        } else if (quarry) {
            return new OSMRelation(id, ways, PathColor.QUARRY.getColor());
        } else if (wood) {
            return new OSMRelation(id, ways, PathColor.WOOD.getColor());
        } else if (heath) {
            return new OSMRelation(id, ways, PathColor.HEATH.getColor());
        } else if (grassland) {
            return new OSMRelation(id, ways, PathColor.GRASSLAND.getColor());
        } else if (scrub) {
            return new OSMRelation(id, ways, PathColor.SCRUB.getColor());
        } else {
            return new OSMRelation(id, ways, PathColor.NONE.getColor());
        }
    }

    public static void setTag(String key, String value) {
        if (key.equals("building")) {
            building = true;
        } else if (key.equals("landuse")) {
            // &&
            if (value.equals("forest")) forest = true;
            else if (value.equals("commercial")) commercial = true;
            else if (value.equals("construction")) construction = true;
            else if (value.equals("allotments")) allotments = true;
            else if (value.equals("farmland")) farmland = true;
            else if (value.equals("meadow")) meadow = true;
            else if (value.equals("orchard")) orchard = true;
            else if (value.equals("basin")) basin = true;
            else if (value.equals("brownfield")) brownfield = true;
            else if (value.equals("cemetery")) cemetery = true;
            else if (value.equals("grass")) grass = true;
            else if (value.equals("reservoir")) reservoir = true;
            else if (value.equals("villageGreen")) villageGreen = true;
            else if (value.equals("quarry")) quarry = true;
        } else if (key.equals("natural")) {
            // &&
            if (value.equals("coastline")) coastline = true;
            else if (value.equals("water")) water = true;
            else if (value.equals("beach")) beach = true;
            else if (value.equals("wood")) wood = true;
            else if (value.equals("heath")) heath = true;
            else if (value.equals("grassland")) grassland = true;
            else if (value.equals("scrub")) scrub = true;
        } else if (key.equals("leisure")) {
            // &&
            if (value.equals("park")) park = true;
        } else if (key.equals("military")) {
            // &&
            if (value.equals("danger_area")) dangerArea = true;
        }
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

    // Can move this to its own file if needed
    public static final class InvalidMapException extends Exception {}
}
