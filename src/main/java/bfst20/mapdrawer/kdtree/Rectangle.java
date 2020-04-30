package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import java.io.Serializable;

/** This class stores coordinates for a simple rectangle, and is used for bounding boxes (kd-tree). */
public class Rectangle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final float xmin;
    private final float ymin;
    private final float xmax;
    private final float ymax;

    /** Creates a bounding box from four coordinates. */
    public Rectangle(float xmin, float ymin, float xmax, float ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    /** Creates a bounding box from a way. */
    public Rectangle(OSMWay way) {
        float xMin = Float.MAX_VALUE;
        float xMax = -Float.MAX_VALUE;
        float yMin = Float.MAX_VALUE;
        float yMax = -Float.MAX_VALUE;

        for (OSMNode node : way.getNodes()) {
            xMin = Math.min(xMin, node.getLon());
            xMax = Math.max(xMax, node.getLon());
            yMin = Math.min(yMin, node.getLat());
            yMax = Math.max(yMax, node.getLat());
        }

        this.xmin = xMin;
        this.xmax = xMax;
        this.ymin = yMin;
        this.ymax = yMax;
    }

    /** Returns true for normal intersections (including bounds) and if one is fully contained within the other. */
    public boolean intersects(Rectangle rect) {
        return xmax >= rect.xmin && ymax >= rect.ymin && rect.xmax >= xmin && rect.ymax >= ymin;
    }

    public float getXmin() {
        return xmin;
    }

    public float getYmin() {
        return ymin;
    }

    public float getXmax() {
        return xmax;
    }

    public float getYmax() {
        return ymax;
    }
}
