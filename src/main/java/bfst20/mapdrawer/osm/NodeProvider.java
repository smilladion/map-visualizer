package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.canvas.GraphicsContext;
import java.util.function.LongSupplier;

/** This interface is used for ways and relations, so they both can be contained in a kd-tree. */
public interface NodeProvider extends LongSupplier, Drawable {

    Rectangle getBoundingBox();

    float getAvgX();

    float getAvgY();

    Type getType();

    long getAsLong();

    void draw(GraphicsContext gc);
}
