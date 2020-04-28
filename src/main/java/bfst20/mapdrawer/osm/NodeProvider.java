package bfst20.mapdrawer.osm;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.kdtree.Rectangle;
import javafx.scene.canvas.GraphicsContext;

import java.util.function.LongSupplier;

/*
This interface is used for ways and relations, making sure they have the below methods.
 */
public interface NodeProvider extends Comparable<NodeProvider>, LongSupplier, Drawable {

    Rectangle getBoundingBox();

    float getAvgX();

    float getAvgY();

    Type getType();

    int compareTo(NodeProvider that);

    long getAsLong();
    
    void draw(GraphicsContext gc);
}
