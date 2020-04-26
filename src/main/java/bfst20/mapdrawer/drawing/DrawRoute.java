package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.Rutevejledning.Dijkstra;
import bfst20.mapdrawer.Rutevejledning.DirectedEdge;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.paint.Color.RED;

public class DrawRoute implements Drawable {
    private final static List<Drawable> routeDrawables = new ArrayList<>();

    public DrawRoute(Dijkstra dijkstra) {
        for(DirectedEdge edge : dijkstra.getEdgeTo()){
            OSMWay way = new OSMWay();
            LinePath linePath = new LinePath(way);
            routeDrawables.add(linePath);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(RED);
        gc.beginPath();
        for (Drawable drawable : routeDrawables) {
            drawable.draw(gc);
        }
        gc.stroke();
    }
}