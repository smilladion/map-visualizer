package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class Polygon implements Drawable {

    private final Drawable shape;
    private final Paint paint;

    // Draw filled-in Way
    public Polygon(OSMWay way, Paint paint) {
        shape = new LinePath(way);
        this.paint = paint;
    }

    // Draw filled-in relation
    public Polygon(OSMRelation relation, Paint paint) {
        shape = new PolyLinePath(relation);
        this.paint = paint;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(paint);
        // Shape is either a LinePath or PolyLinePath
        shape.draw(gc);
        gc.fill();
    }
}
