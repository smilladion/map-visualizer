package bfst20.tegneprogram;

import java.util.function.LongSupplier;

public class OSMNode implements LongSupplier {
    long id;
    float lat, lon;

    public OSMNode(long id, float lon, float lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public long getAsLong() {
        return id;
    }
}