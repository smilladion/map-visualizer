package routefinding;

import bfst20.mapdrawer.osm.OSMNode;

public class DirectedEdge {
    private int v;
    private int w;
    private double weight;

    public double length;
    public double speedLimit;


    public boolean isForwardAllowed;
    public boolean isBackwardAllowed;

    public DirectedEdge(int v, int w) {
        this.v = v;
        this.w = w;
        this.weight = 0;
    }

    public int from() {
        return v;
    }

    public int to() {
        return w;
    }

    public double weight() {
        return weight;
    }

    public String toString() {
        return v + "->" + w + " " + String.format("%5.2f", weight);
    }
}