package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class PolyLinePath implements Drawable {

    private final ArrayList<LinePath> paths = new ArrayList<>();

    // Effectively an abstraction of list of LinePaths
    PolyLinePath(OSMRelation relation) {
        for (OSMWay way : relation.getWays()) {
            paths.add(new LinePath(way));
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        for (LinePath line : paths) {
            line.trace(gc);
        }
        gc.stroke();
    }
}
