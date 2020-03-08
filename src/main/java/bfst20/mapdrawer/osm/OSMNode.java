package bfst20.mapdrawer.osm;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {

    private final long id;
    private final float lat;
    private final float lon;

    OSMNode(long id, float lon, float lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
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
