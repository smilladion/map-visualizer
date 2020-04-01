package routefinding;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.map.PathColor;
import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;

public class Route implements Drawable {
    Point startNode;
    Point endNode;
    GraphicsContext gc;

    public Route(Point startNode, Point endNode){
        //color = PathColor.UNKNOWN.getColor();
        gc.beginPath();
        gc.moveTo(startNode.getX1(), startNode.getY1());
        gc.lineTo(endNode.getX1(), endNode.getY1());
        gc.stroke();
    }


    @Override
    public void draw(GraphicsContext gc) {
        //for()
    }
}
