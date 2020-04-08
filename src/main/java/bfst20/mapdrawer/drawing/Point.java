package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Point implements Drawable {

    private OSMNode node;

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    private final double x1;
    private final double y1;

    // to be used when you want to mark a single point. Image that is being painted is a pin point, like we know from other maps.
    public Point(OSMNode node) {
        this.node = node;
        x1 = node.getLon();
        y1 = node.getLat();
    }

    //x1 gets plussed with (0.01 / 2) to center the point of the pin at the searched spot.
    @Override
    public void draw(GraphicsContext gc) {
        Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("REDlogotrans.png"));
        gc.drawImage(pointImage, x1, y1, -0.01, -0.01);
        try {
            Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("REDlogotrans.png"));
            gc.drawImage(pointImage, x1+ (0.01 / 2), y1, -0.01, -0.01);
        } catch (NullPointerException e) {
            System.err.println("Pin point image not found!");
        }
    }
}
