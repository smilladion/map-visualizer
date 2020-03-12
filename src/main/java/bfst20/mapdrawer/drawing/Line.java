package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.scene.canvas.GraphicsContext;

import java.util.Scanner;

public class Line implements Drawable {

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(String line) {
        try (var scanner = new Scanner(line)) {
            // Skip LINE prefix, uninteresting
            scanner.next();

            x1 = scanner.nextDouble();
            y1 = scanner.nextDouble();
            x2 = scanner.nextDouble();
            y2 = scanner.nextDouble();
        }
    }

    public Line(OSMNode from, OSMNode to) {
        this(from.getLon(), from.getLat(), to.getLon(), to.getLat());
    }

    public String toString() {
        return "LINE " + x1 + " " + y1 + " " + x2 + " " + y2;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }
}
