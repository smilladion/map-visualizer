package bfst20.mapdrawer.kdtree;

import java.io.Serializable;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class Rectangle implements Drawable, Serializable {

    private static final long serialVersionUID = 1L;

    private final float xmin;
    private final float ymin;
    private final float xmax;
    private final float ymax;

    public Rectangle(float xmin, float ymin, float xmax, float ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    // Creates a bounding box for a way
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

    // Returns true for normal intersections (including bounds) and if one is fully contained within the other
    public boolean intersects(Rectangle rect) {
        return xmax >= rect.xmin && ymax >= rect.ymin && rect.xmax >= xmin && rect.ymax >= ymin;
    }

    // Returns true if this point is within the rectangle bounds
    public boolean containsPoint(Point2D p) {
        return p.getX() >= xmin && p.getX() <= xmax && p.getY() >= ymin && p.getY() <= ymax;
    }

    public Point2D getCenterPoint() {
        return new Point2D((xmin + xmax) / 2.0, (ymin + ymax) / 2.0);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeRect(xmin, ymin, xmax - xmin, ymax - ymin);
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