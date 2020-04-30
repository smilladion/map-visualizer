package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;
import java.io.Serializable;
import java.util.Objects;

/** This class is used to draw pin points on the map. */
public class Point implements Drawable, Serializable {

    private static final long serialVersionUID = 1L;

    // Variable used to quickly adjust the size of the point
    private static final double SIZE = 0.65;

    private final double x;
    private final double y;
    private final OSMNode node;
    private final Affine transform;
    private final Image image;
    private boolean isEmpty = true;

    /** To be used when you want to mark a single point with an address. */
    public Point(OSMNode node, Affine transform) {
        this.node = node;
        this.transform = transform;

        x = node.getLon();
        y = node.getLat();

        image = new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("point_a.png"),
                "Point image not found!"
        ));

        isEmpty = false;
    }

    /** To be used for user-made point of interest. */
    public Point(double x, double y, Affine transform) {
        this.node = null;
        this.transform = transform;
        this.x = x;
        this.y = y;

        image = new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("point_b.png"),
                "Point image not found!"
        ));

        isEmpty = false;
    }

    /** Empty point for initialization. */
    public Point() {
        this.node = null;
        transform = null;
        x = 0;
        y = 0;
        image = null;
    }
    
    @Override
    // Scales the image relative to the zoom level.
    public void draw(GraphicsContext gc) {
        if (image == null) {
            return;
        }

        double initialZoom = 5000.0f; // All maps start off with roughly this zoom level
        double scale = transform.getMxx() / initialZoom;

        gc.drawImage(
                image,
                x + SIZE * 0.01 / (2 * scale), y,
                SIZE * -0.01 / scale, SIZE * -0.01 / scale
        );
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
