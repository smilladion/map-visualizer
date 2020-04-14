package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

import java.util.Objects;

public class Point implements Drawable {

    private static final float SIZE = 0.85f;

    private final double x;
    private final double y;
    private final OSMNode node;
    private final Affine transform;
    private final Image image;

    // To be used when you want to mark a single point. Image that is being painted is a pin point, like we know from other maps.
    public Point(OSMNode node, Affine transform) {
        this.node = node;
        this.transform = transform;

        x = node.getLon();
        y = node.getLat();

        image = new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("point_a.png"),
                "Point image not found!"
        ));
    }

    // Scales the image relative to the zoom level. Variable SIZE can be changed to whatever size we want it to be.
    @Override
    public void draw(GraphicsContext gc) {
        double initialZoom = 5000.0; // Found to be the appropriate value
        double scale = transform.getMxx() / initialZoom;

        gc.drawImage(
                image,
                x + SIZE * 0.01 / (2 * scale), y,
                SIZE * -0.01 / scale, SIZE * -0.01 / scale
        );
    }
}
