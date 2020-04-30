package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.canvas.GraphicsContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * This class defines a series of lines between several nodes - you could
 * also call it a path. It contains several attributes that defines what the
 * path is supposed to represent.
 */
public class OSMWay implements LongSupplier, NodeProvider, Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final long NO_ID = Long.MIN_VALUE;

    private final long id;
    private final List<OSMNode> nodes;
    private final Type type;
    private final String road; // Null if way is not a highway or there is no <name> tag

    private double speed;

    private boolean car;
    private boolean bike;
    private boolean walk;
    
    private boolean onewayCar;
    private boolean onewayBike;
    private boolean onewayWalk;
    
    private boolean roundabout;

    /** A normal way created from a list of nodes, with an id, type and road name. */
    public OSMWay(long id, List<OSMNode> nodes, Type type, String road) {
        this.id = id;
        this.nodes = nodes;
        this.type = type;
        this.road = road;
    }

    /** A way to be made into a directed edge - it will have information about traffic rules used for route finding. */
    public OSMWay(long id, List<OSMNode> nodes, Type type, String road, 
                  double speed, boolean roundabout,
                  boolean car, boolean bike, boolean walk, 
                  boolean onewayCar, boolean onewayBike, boolean onewayWalk) {
        this.id = id;
        this.nodes = nodes;
        this.type = type;
        this.road = road;
        this.speed = speed;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
        this.onewayCar = onewayCar;
        this.onewayBike = onewayBike;
        this.onewayWalk = onewayWalk;
        this.roundabout = roundabout;
    }
    
    /** Empty way used for initialization. */
    public OSMWay() {
        this.id = NO_ID;
        this.nodes = new ArrayList<>();
        type = Type.UNKNOWN;
        road = null;
    }

    /** 
     * Used for coastlines: stitches two ways together (with one end-node in common), 
     * returning a new way with them both a part of it. 
     */
    public static OSMWay fromWays(OSMWay input, OSMWay output) {
        if (input == null) {
            return output;
        } else if (output == null) {
            return input;
        }

        // Create a "no-id" OSM way
        OSMWay way = new OSMWay(NO_ID, new ArrayList<>(), Type.COASTLINE, null);

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

    public double getSpeed() {
        return speed;
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
    public void draw(GraphicsContext gc) {
        if (nodes.size() > 1) {
            trace(gc);

            if (type.shouldBeFilled()) {
                gc.fill();
            }
        }
    }
    
    public void trace(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(nodes.get(0).getLon(), nodes.get(0).getLat());

        for (OSMNode node : nodes.subList(1, nodes.size())) {
            gc.lineTo(node.getLon(), node.getLat());
        }

        gc.stroke();
    }

    /**
     * Gets the average x-coordinate of this way, based on the average 
     * x-coordinates of its nodes.
     */
    @Override
    public float getAvgX() {
        float sumX = 0.0f;

        for (OSMNode node : nodes) {
            sumX += node.getLon();
        }

        return sumX / nodes.size();
    }

    /**
     * Gets the average y-coordinate of this way, based on the average 
     * y-coordinates of its nodes.
     */
    @Override
    public float getAvgY() {
        float sumY = 0.0f;

        for (OSMNode node : nodes) {
            sumY += node.getLat();
        }
        
        return sumY / nodes.size();
    }

    public boolean isOnewayCar() {
        return onewayCar;
    }

    public boolean isOnewayBike() {
        return onewayBike;
    }

    public boolean isOnewayWalk() {
        return onewayWalk;
    }

    public boolean isRoundabout() {
        return roundabout;
    }

    /** Gets the bounding box (rectangle) encompassing this way. */
    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(this);
    }

    @Override
    public Type getType() {
        return type;
    }
}
