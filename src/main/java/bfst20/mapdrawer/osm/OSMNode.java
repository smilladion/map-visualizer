package bfst20.mapdrawer.osm;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {

    private final long id;
    private final float lon; // x
    private final float lat; // y

    private int numberForGraph;

    OSMNode(long id, float lon, float lat, int numberForGraph) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.numberForGraph = numberForGraph;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }

    public int getNumberForGraph() {
        return numberForGraph;
    }

    public void setNumberForGraph(int number) {
        if (numberForGraph < 0) {
            numberForGraph = number;
        }
    }
}
