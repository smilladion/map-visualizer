package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Point implements Drawable {

    private OSMNode node;

    public Point(OSMNode node) {
        this.node = node;
    }

    @Override
    public void draw(GraphicsContext gc) {

        Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("main/resources/mapslogoRed.png"));
        Image pointImage1 = new Image(this.getClass().getClassLoader().getResourceAsStream("main/resources/mapslogo.png"));
        gc.drawImage(pointImage, node.getLon(), node.getLat());

        //gc.drawImage(pointImage, node.getLon(), node.getLat(), 20, 20);
        //gc.drawImage(pointImage1, node.getLon(), node.getLat(), 20, 20 );


    }
}
