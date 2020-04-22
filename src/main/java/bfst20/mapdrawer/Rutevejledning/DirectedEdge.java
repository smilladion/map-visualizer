package bfst20.mapdrawer.Rutevejledning;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.pow;

public class DirectedEdge {
    int from;
    int to;
    double weight;
    boolean bike;
    boolean walk;
    boolean car;

    public OSMWay getWay() {
        return way;
    }

    OSMWay way;

    public DirectedEdge(int from, int to, double weight, boolean bike, boolean walk, boolean car, OSMWay way) {
        this.from = from;
        this.to = to;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
        this.weight = weight;
        this.way = way;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public double getWeight() { return weight; }

    public String toString() {
        return String.format("from: " + from + ", to: " + to);
    }


    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
