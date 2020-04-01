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

    public Point(OSMNode node) {
        this.node = node;
        x1 = node.getLon();
        y1 = node.getLat();
    }

    @Override
    public void draw(GraphicsContext gc) {

        Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("REDlogotrans.png"));
        gc.drawImage(pointImage, x1, y1, -0.01, -0.01);
    }
}
