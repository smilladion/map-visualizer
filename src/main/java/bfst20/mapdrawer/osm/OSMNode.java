package bfst20.mapdrawer.osm;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {

    private final float lat;
    private final float lon;
    private final long id;

    public OSMNode(float lon, float lat, long id) {
        this.lon = lon;
        this.lat = lat;
        this.id = id;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    @Override
    public long getAsLong() {
        return id;
    }
}
