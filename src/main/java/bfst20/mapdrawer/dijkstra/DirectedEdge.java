package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;
import bfst20.mapdrawer.osm.OSMNode;

public class DirectedEdge implements Serializable {

    private static final long serialVersionUID = 1L;
    
    int from;
    int to;
    double weight;
    boolean bike;
    boolean walk;
    boolean car;
    String road;
    double x1;
    double y1;
    double x2;
    double y2;
    OSMNode nodeFrom;
    OSMNode nodeTo;


    public DirectedEdge(int from, int to, double weight, boolean bike, boolean walk, boolean car, String road, double x1, double y1, double x2, double y2, OSMNode nodeFrom, OSMNode nodeTo) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
        this.road = road;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public String getRoad() {
        return road;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public OSMNode getNodeFrom() {
        return nodeFrom;
    }

    public OSMNode getNodeTo() {
        return nodeTo;
    }

    public boolean isCar() {
        return car;
    }

    public boolean isBike() {
        return bike;
    }

    public boolean isWalk() {
        return walk;
    }

    public String toString() {
        return String.format("from: " + from + ", to: " + to);
   }
}
