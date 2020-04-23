package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;

import bfst20.mapdrawer.drawing.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DirectedEdge implements Serializable, Drawable {

    private static final long serialVersionUID = 1L;
    
    private final int from;
    private final int to;
    private final int weight;
    private final boolean bike;
    private final boolean walk;
    private final boolean car;
    private final String road;
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;


    public DirectedEdge(int from, int to, int weight, boolean bike, boolean walk, boolean car, String road, double x1, double y1, double x2, double y2) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
        this.road = road;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int getWeight() {
        return weight;
    }

    public String getRoad() {
        return road;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y1;
    }

    public String toString() {
        return String.format("from: " + from + ", to: " + to);
   }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }
}
