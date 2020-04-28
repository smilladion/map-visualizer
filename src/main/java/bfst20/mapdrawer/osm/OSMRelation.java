package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.List;
import java.util.function.LongSupplier;

public class OSMRelation implements LongSupplier, NodeProvider, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final long id;
    private final List<OSMWay> ways;

    private final Type type;

    OSMRelation(long id, List<OSMWay> ways, Type type) {
        this.id = id;
        this.ways = ways;
        this.type = type;
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public List<OSMWay> getWays() {
        return ways;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        for (OSMWay way : ways) {
            if (way.getNodes().isEmpty()) {
                continue;
            }
            
            way.trace(gc);
        }

        if (type.shouldBeFilled()) {
            gc.fill();
        }
    }

    @Override
    public Rectangle getBoundingBox() {
        float xMin = Float.MAX_VALUE;
        float xMax = -Float.MAX_VALUE;
        float yMin = Float.MAX_VALUE;
        float yMax = -Float.MAX_VALUE;

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
        float sumX = 0.0f;

        for (OSMWay way : ways) {
            sumX += way.getAvgX();
        }

        return sumX / ways.size();
    }

    @Override
    public float getAvgY() {
        float sumY = 0.0f;

        for (OSMWay way : ways) {
            sumY += way.getAvgY();
        }

        return sumY / ways.size();
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