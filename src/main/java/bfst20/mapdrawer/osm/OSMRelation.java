package bfst20.mapdrawer.osm;

import javafx.scene.paint.Paint;

import java.util.List;
import java.util.function.LongSupplier;

public class OSMRelation implements LongSupplier {

    private final long id;
    private final List<OSMWay> ways;
    private final Paint color;

    OSMRelation(long id, List<OSMWay> ways, Paint color) {
        this.id = id;
        this.ways = ways;
        this.color = color;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public List<OSMWay> getWays() {
        return ways;
    }

    public Paint getColor() {
        return color;
    }
}
