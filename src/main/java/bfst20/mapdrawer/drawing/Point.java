package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Point implements Drawable {

    private OSMNode node;
    private final double x1;
    private final double y1;

    public Point(OSMNode node) {
        this.node = node;
        x1 = node.getLon();
        y1 = node.getLat();
    }

    @Override
    public void draw(GraphicsContext gc) {

        Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("main/resources/REDlogotrans.png"));
        gc.drawImage(pointImage, x1+ (0.01 / 2), y1, -0.01, -0.01);
    }
}
