package bfst20.mapdrawer.osm;

import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier, Serializable {
    
    private static final long serialVersionUID = 1L;

    private final long id;
    private final double lon; // x
    private final double lat; // y
    
    private String address;

    private String road;

    private int numberForGraph;

    public OSMNode(long id, double lon, double lat, int numberForGraph, String address) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.numberForGraph = numberForGraph;
        this.address = address;
    }
    
    public OSMNode() {
        id = Long.MIN_VALUE;
        lon = 0;
        lat = 0;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }
    
    public String getAddress() {
        return address;
    }

    public int getNumberForGraph() {
        return numberForGraph;
    }

    public void setNumberForGraph(int number) {
        if (numberForGraph < 0) {
            numberForGraph = number;
        }
    }
    
    /**
     * Calculates the distance between this node and another point.
     * NOTE: Square root has been removed because it is very slow to run, meaning it gives the wrong result,
     * so it can only be used for condition checks (because the relation holds, so ex. distance1 < distance2 still works
     * - but the numbers themselves are wrong). Normal calculation is sqrt(a^2 + b^2), this one is only a^2 + b^2.
     */
    public double distanceSq(Point2D point) {
        double dX = point.getX() - lon;
        double dY = point.getY() - lat;
        return dX * dX + dY * dY;
    }
    
    public void setRoad(String road) {
        this.road = road;
    }

    public String getRoad() {
        return road;
    }
}
