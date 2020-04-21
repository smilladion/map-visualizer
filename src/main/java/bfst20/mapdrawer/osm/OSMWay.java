package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.rutevejledning.DirectedEdge;
import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.LinePath;
import bfst20.mapdrawer.drawing.Polygon;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.NodeProvider;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.paint.Paint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

public class OSMWay implements LongSupplier, NodeProvider, Serializable {

    private static final long serialVersionUID = 1L;
    
    // A dummy way is used to avoid error when a relation references an unknown way
    // This allows files to be loaded which would normally fail under stricter parsing
    public static final OSMWay DUMMY_WAY = new OSMWay();
    private static final long NO_ID = Long.MIN_VALUE;

    private final long id;
    private final List<OSMNode> nodes;
    private final Drawable drawable;
    private final Type type;
    private final String road; // null if way is not a highway or there is no <name> tag

    private int weight;
    private boolean bike;
    private boolean walk;
    private boolean car;


    public OSMWay(long id, List<OSMNode> nodes, Type type, String road) {

        this.id = id;
        this.nodes = nodes;
        this.type = type;
        this.road = road;

        if (nodes.isEmpty()) {
            // If a way has no nodes, do not draw
            drawable = null;
        } else if (type.shouldBeFilled()) {
            // If a way should be filled with colour, make a polygon
            drawable = new Polygon(this);
        } else {
            // If it should not, draw a line
            drawable = new LinePath(this);
        }
    }

    // OSMWay to make into a directed edge - it will have a weight and info about vehicles.
    public OSMWay(long id, List<OSMNode> nodes, Type type, int weight, boolean bike, boolean walk, boolean car, String road) {
        this.id = id;
        this.nodes = nodes;
        this.type = type;

        this.road = road;

        this.weight = weight;
        this.bike = bike;
        this.walk = walk;
        this.car = car;

        if (nodes.isEmpty()) {
            // If a way has no nodes, do not draw
            drawable = null;
        } else {
            drawable = new LinePath(this);
        }
    }

    private OSMWay() {
        this.id = NO_ID;
        this.nodes = new ArrayList<>();
        drawable = null;
        type = Type.UNKNOWN;
        road = null;
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
    
    public String getRoad() {
        return road;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isBike() {
        return bike;
    }

    public boolean isWalk() {
        return walk;
    }

    public boolean isCar() {
        return car;
    }

    @Override
    public float getAvgX() {
        return (float) getBoundingBox().getCenterPoint().getX();
    }

    @Override
    public float getAvgY() {
        return (float) getBoundingBox().getCenterPoint().getY();
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(this);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(NodeProvider that) {
        // Ordinal returns a number representing the type's order/position in the enum class, from 0 and up
        return type.ordinal() - that.getType().ordinal();
    }
}
