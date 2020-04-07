package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Type;

/*
This interface makes sure the classes implementing it contain a drawable.
This drawable must have a corresponding bounding box and center point.
 */
public interface NodeProvider {

    Drawable getDrawable();

    Rectangle getBoundingBox();

    float getAvgX();

    float getAvgY();

    Type getType();
}
