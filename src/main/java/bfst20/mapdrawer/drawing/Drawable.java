package bfst20.mapdrawer.drawing;

import javafx.scene.canvas.GraphicsContext;

/** Classes implementing this interface can be drawn on the map. */
public interface Drawable {
    
    /** Draws the object on the map. */
    void draw(GraphicsContext gc);
}
