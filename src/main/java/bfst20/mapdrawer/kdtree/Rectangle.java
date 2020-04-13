package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class Rectangle implements Drawable {

    private final double xmin;
    private final double ymin;
    private final double xmax;
    private final double ymax;

    public Rectangle(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    // Creates a bounding box for a way
    public Rectangle(OSMWay way) {
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;

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

    public Point2D getCenterPoint() {
        return new Point2D((xmin + xmax) / 2.0, (ymin + ymax) / 2.0);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeRect(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    public double getXmin() {
        return xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmax() {
        return ymax;
    }
}
