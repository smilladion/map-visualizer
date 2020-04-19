package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class LinePath implements Drawable {

    // Coords holds the X and Y coordinates (X1, Y1, X2, Y2, etc)
    private final double[] coords;
    private final Paint color;

    public LinePath(OSMWay way) {
        // Each point has an X and a Y (number of slots we need)
        coords = new double[2 * way.getNodes().size()];

        // For each point in this path, add the coordinate to our list
        for (int i = 0; i < way.getNodes().size(); i++) {
            coords[i * 2] = way.getNodes().get(i).getLon();
            coords[i * 2 + 1] = way.getNodes().get(i).getLat();
        }

        this.color = way.getType().getColor();
    }

    @Override
    public void draw(GraphicsContext gc) {

        gc.setStroke(color);
        gc.beginPath();
        trace(gc);
        gc.stroke();
    }

    // Draws the actual lines (must be within a beginPath block)
    void trace(GraphicsContext gc) {
        // Move to the first coordinate
        gc.moveTo(coords[0], coords[1]);

        // Draw lines to the next coordinate(s)
        for (int i = 2; i < coords.length; i += 2) {
            gc.lineTo(coords[i], coords[i + 1]);
        }
    }
}
