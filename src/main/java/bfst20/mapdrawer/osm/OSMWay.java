package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.map.PathColor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

public class OSMWay implements LongSupplier {

    // A dummy way is used to avoid error when a relation references an unknown way
    // This allows files to be loaded which would normally fail under stricter parsing
    public static final OSMWay DUMMY_WAY = new OSMWay();
    private static final long NO_ID = Long.MIN_VALUE;

    private final long id;
    private final List<OSMNode> nodes;
    private final Paint color;

    OSMWay(long id, List<OSMNode> nodes, Paint color) {
        this.id = id;
        this.nodes = nodes;
        this.color = color;
    }

    private OSMWay() {
        this.id = NO_ID;
        this.nodes = new ArrayList<>();
        color = PathColor.UNKNOWN.getColor();
    }

    public static OSMWay fromWays(OSMWay input, OSMWay output) {
        if (input == null) {
            return output;
        } else if (output == null) {
            return input;
        }

        // Create a "no-id" OSM way
        OSMWay way = new OSMWay();

        // Where input is a path (collection of points)
        // And output is another path (similarly)

        // If input and output start on same point
        if (input.first() == output.first()) {
            // Input way goes from S->T, output way goes from S->U, mutual node is first (S)

            way.nodes.addAll(input.nodes); // Add all input nodes to list (S->T)
            Collections.reverse(way.nodes); // Reverses the input nodes (T->S)

            way.nodes.remove(way.nodes.size() - 1); // Remove duplicate node (S)
            way.nodes.addAll(output.nodes); // Adds all of output to end (T->S->U aka T->U), now connected
        } else if (input.first() == output.last()) {
            // Input way goes from S->T, output way goes from U->S, mutual node is middle (S)

            way.nodes.addAll(output.nodes); // Add all output nodes to list
            way.nodes.remove(way.nodes.size() - 1); // Remove duplicate node
            way.nodes.addAll(input.nodes); // Add all input nodes, now connected
        } else if (input.last() == output.first()) {
            // Input way goes from T->S, output way goes from S->U, mutual node is middle (S)

            way.nodes.addAll(input.nodes); // Add all input nodes to list
            way.nodes.remove(way.nodes.size() - 1); // Remove duplicate node
            way.nodes.addAll(output.nodes); // Add all output nodes, now connected
        } else if (input.last() == output.last()) {
            // Input way goes from S->T, output way goes from U->T, mutual node is last (T)

            way.nodes.addAll(input.nodes); // Take the nodes in input (S->T)
            way.nodes.remove(way.nodes.size() - 1); // Remove node T to avoid duplicate

            var outputNodes = new ArrayList<>(output.nodes); // Take a copy of output nodes
            Collections.reverse(outputNodes); // Reverses output way (U->T becomes T->U)
            way.nodes.addAll(outputNodes); // Add reversed nodes (S->T->U aka S->U), now connected
        } else {
            // No mutual node shared between the ways (can't connect them!)
            throw new IllegalArgumentException("Cannot connect way without similar node");
        }

        return way;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public OSMNode first() {
        return nodes.get(0);
    }

    public OSMNode last() {
        return nodes.get(nodes.size() - 1);
    }

    public List<OSMNode> getNodes() {
        return nodes;
    }

    public Paint getColor() {
        return color;
    }

    public static boolean isColorable(OSMWay way) {

        if (way.getColor() == PathColor.BUILDING.getColor()) {
            return true;
        }
        if (way.getColor() == PathColor.FOREST.getColor()) {
            return true;
        }
        if (way.getColor() == PathColor.WATER.getColor()) {
            return true;
        }
        if (way.getColor() == PathColor.BEACH.getColor())
            return true;
        
        if (way.getColor() == PathColor.COMMERCIAL.getColor())
            return true;

        if (way.getColor() == PathColor.CONSTRUCTION.getColor())
            return true;
        
        if (way.getColor() == PathColor.ALLOTMENTS.getColor())
            return true;

        if (way.getColor() == PathColor.FARMLAND.getColor())
            return true;

        if (way.getColor() == PathColor.MEADOW.getColor())
            return true;

        if (way.getColor() == PathColor.ORCHARD.getColor())
            return true;
    
        if (way.getColor() == PathColor.BASIN.getColor())
            return true;
        
        if (way.getColor() == PathColor.BROWNFIELD.getColor())
            return true; 
        
        if (way.getColor() == PathColor.CEMETERY.getColor())
            return true;
        
        if (way.getColor() == PathColor.GRASS.getColor())
            return true;

        if (way.getColor() == PathColor.RESERVOIR.getColor())
            return true;
        
        if (way.getColor() == PathColor.VILLAGE_GREEN.getColor())
            return true;

        if (way.getColor() == PathColor.PARK.getColor())
            return true;

        if (way.getColor() == PathColor.DANGER_AREA.getColor())
            return true;

        if (way.getColor() == PathColor.QUARRY.getColor())
            return true;

        if (way.getColor() == PathColor.WOOD.getColor())
            return true;

        if (way.getColor() == PathColor.HEATH.getColor())
            return true;

        if (way.getColor() == PathColor.GRASSLAND.getColor())
            return true;

        if (way.getColor() == PathColor.SCRUB.getColor())
            return true;

        return false;
    }
}
