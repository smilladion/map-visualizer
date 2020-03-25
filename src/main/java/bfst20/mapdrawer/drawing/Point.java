package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Point implements Drawable {

    private OSMNode node;
    private final double x1;
    private final double x2;

    public Point(OSMNode node) {
        this.node = node;
        x1 = node.getLon();
        x2 = node.getLat();
    }

    @Override
    public void draw(GraphicsContext gc) {

        Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("main/resources/mapslogoRed.png"));
        Image pointImage1 = new Image(this.getClass().getClassLoader().getResourceAsStream("main/resources/mapslogo.png"));

        //gc.drawImage(pointImage, node.getLon(), node.getLat(), 20, 20);
        //gc.drawImage(pointImage1, node.getLon(), node.getLat(), 20, 20 );


    }
}
