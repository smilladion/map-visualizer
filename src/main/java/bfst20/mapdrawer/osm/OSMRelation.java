package bfst20.mapdrawer.osm;

import java.util.List;
import java.util.function.LongSupplier;

public class OSMRelation implements LongSupplier {

    private final long id;
    private final List<OSMWay> ways;

    public OSMRelation(long id, List<OSMWay> ways) {
        this.id = id;
        this.ways = ways;
    }

    @Override
    public long getAsLong() {
        return id;
    }
}
