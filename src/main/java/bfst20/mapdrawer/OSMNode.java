package bfst20.mapdrawer;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {
    float lat;
    float lon;
    long id;

    public OSMNode(float lon, float lat, long id) {
        this.lon = lon;
        this.lat = lat;
        this.id = id;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}
