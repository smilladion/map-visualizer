package bfst20.mapdrawer;

import javafx.scene.canvas.GraphicsContext;
import java.util.Scanner;

public class Line implements Drawable {

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(String line) {
        try (var scanner = new Scanner(line)) {
            scanner.next();
            x1 = scanner.nextDouble();
            y1 = scanner.nextDouble();
            x2 = scanner.nextDouble();
            y2 = scanner.nextDouble();
        }
    }

    public String toString() {
        return "LINE " + x1 + " " + y1 + " " + x2 + " " + y2;
    }

    public Line(OSMNode from, OSMNode to) {
        this(from.getLon(), from.getLat(), to.getLon(), to.getLat());
    }

    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }
}
