package bfst20.mapdrawer.osm;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {

    private final long id;
    private final float lon; // x
    private final float lat; // y

    public OSMNode(long id, float lon, float lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public double getLonAsDouble() { return (double) lon; }

    public double getLatAsDouble() { return (double) lat; }

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }
}
