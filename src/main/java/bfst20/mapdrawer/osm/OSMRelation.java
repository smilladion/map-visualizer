package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Polygon;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.NodeProvider;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.paint.Paint;

import java.io.Serializable;
import java.util.List;
import java.util.function.LongSupplier;

public class OSMRelation implements LongSupplier, NodeProvider, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final long id;
    private final List<OSMWay> ways;
    private final Drawable drawable;

    private final Type type;

    OSMRelation(long id, List<OSMWay> ways, Type type) {
        this.id = id;
        this.ways = ways;
        this.type = type;

        if (type.getColor() == Type.NONE.getColor()) {
            drawable = null;
        } else {
            drawable = new Polygon(this);
        }
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public List<OSMWay> getWays() {
        return ways;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public Rectangle getBoundingBox() {
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;

        for (OSMWay way : ways) {
            xMin = Math.min(xMin, way.getBoundingBox().getXmin());
            xMax = Math.max(xMax, way.getBoundingBox().getXmax());
            yMin = Math.min(yMin, way.getBoundingBox().getYmin());
            yMax = Math.max(yMax, way.getBoundingBox().getYmax());
        }

        return new Rectangle(xMin, yMin, xMax, yMax);
    }

    @Override
    public float getAvgX() {
        return (float) getBoundingBox().getCenterPoint().getX();
    }

    @Override
    public float getAvgY() {
        return (float) getBoundingBox().getCenterPoint().getY();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(NodeProvider that) {
        return type.ordinal() - that.getType().ordinal();
    }
}