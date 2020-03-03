package bfst20.mapdrawer;
import java.io.File;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model {
    float minlat;
    float maxlat;
    float minlon;
    float maxlon;

    public Model() throws Exception {
        loadOSM(new File("resources/samsoe.osm"));
    }


    private void loadOSM(File file) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        var reader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file));
        Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();

        SortedArrayList<OSMNode> SortedOSMNode = new SortedArrayList<>();
        SortedArrayList<OSMWay> SortedOSMWay = new SortedArrayList<>();
    }
}

        //Skriv alt hernede til enkelte metoder




/*
            OSMRelation currentRelation = null;
            OSMWay currentWay = null;
            Type type = Type.UNKNOWN;
            while (reader.hasNext()) {
                reader.next();
                switch (reader.getEventType()) {
                    case START_ELEMENT:
                        String tagname = reader.getLocalName();
                        switch (tagname) {
                            case "bounds":
                                minlat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                                maxlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                                maxlat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                                minlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                                break;
                            case "node":
                                long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                                float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                                float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                                var node = new OSMNode(id, 0.56f*lon, -lat);
                                idToNode.add(node);
                                break;
                            case "way":
                                id = Long.parseLong(reader.getAttributeValue(null, "id"));
                                currentWay = new OSMWay(id);
                                idToWay.add(currentWay);
                                type = Type.UNKNOWN;
                                break;
                            case "nd":
                                var ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                if (currentWay != null) {
                                    if (idToNode.get(ref) != null) currentWay.add(idToNode.get(ref));
                                }
                                break;
                            case "tag":
                                var k = reader.getAttributeValue(null, "k");
                                var v = reader.getAttributeValue(null, "v");
                                switch (k) {
                                    case "building":
                                        type = Type.BUILDING;
                                        break;
                                    case "natural":
                                        switch (v) {
                                            case "coastline":
                                                type = Type.COASTLINE;
                                                break;
                                            case "water":
                                            case "wetland":
                                                type = Type.WATER;
                                                break;
                                            case "grassland":
                                                type = Type.GREEN;
                                                break;
                                        }
                                        break;
                                    case "highway":
                                        type = Type.HIGHWAY;
                                        break;
                                    case "landuse":
                                        switch (v) {
                                            case "forest":
                                            case "meadow":
                                                type = Type.GREEN;
                                                break;
                                        }
                                }
                                break;
                            case "relation":
                                currentRelation = new OSMRelation();
                                type = Type.UNKNOWN;
                                break;
                            case "member":
                                var t = reader.getAttributeValue(null, "type");
                                ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                                if (t.equals("way")) {
                                    if (idToWay.get(ref) != null) currentRelation.add(idToWay.get(ref));
                                }
                        }
                        break;
                    case END_ELEMENT:
                        tagname = reader.getLocalName();
                        switch (tagname) {
                            case "way":
                                if (type != Type.COASTLINE) {
                                    if (!enumMap.containsKey(type)) enumMap.put(type, new ArrayList<>());
                                    enumMap.get(type).add(new LinePath(currentWay, type));
                                } else {
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
                                    currentWay = OSMWay.merge(OSMWay.merge(before, currentWay), after);
                                    nodeToCoastline.put(currentWay.first(), currentWay);
                                    nodeToCoastline.put(currentWay.last(), currentWay);
                                }
                                type = Type.UNKNOWN;
                                break;
                            case "relation":
                                if (type != Type.UNKNOWN && type != Type.COASTLINE) {
                                    if (!enumMap.containsKey(type)) enumMap.put(type, new ArrayList<>());
                                    enumMap.get(type).add(new PolyLinePath(currentRelation, type));
                                }
                                type = Type.UNKNOWN;
                        }
                }
            }
            for (var entry : nodeToCoastline.entrySet()) {
                if (entry.getKey() == entry.getValue().last()) {
                    islands.add(new LinePath(entry.getValue(), type));
                }
            }
        }

        public List<Drawable> getDrawablesOfType(Type type) {
            if (enumMap.containsKey(type)) return enumMap.get(type);
            return new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        private void loadBinary(File file) throws IOException, FileNotFoundException {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                try {
                    enumMap = (HashMap<Type, List<Drawable>>) in.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void add(Line line) {
        /*drawables.add(line);
        notifyObservers();
        }

        public void save(File file) throws IOException {
            long time = -System.nanoTime();
            if (file.getName().endsWith(".bin")) {
                try (var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                    out.writeObject(enumMap);
                }
            } else {
            /*try (PrintStream out = new PrintStream(file)) {
                for (Drawable line : drawables) {
                    out.println(line);
                }
            }
            }
            time += System.nanoTime();
            System.out.printf("Save time: %.3fms\n", time / 1e6);
        }
    }


    */
