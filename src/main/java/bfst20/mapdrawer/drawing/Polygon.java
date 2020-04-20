package bfst20.mapdrawer.drawing;

import java.io.Serializable;

import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class Polygon implements Drawable, Serializable {

    private static final long serialVersionUID = 1L;

    private final Drawable shape; // Shape is either a LinePath or PolyLinePath

    // Draw filled-in way
    public Polygon(OSMWay way) {
        shape = new LinePath(way);
    }

    // Draw filled-in relation
    public Polygon(OSMRelation relation) {
        shape = new PolyLinePath(relation);
    }

    @Override
    public void draw(GraphicsContext gc) {
        shape.draw(gc);
        gc.fill();
    }
}
