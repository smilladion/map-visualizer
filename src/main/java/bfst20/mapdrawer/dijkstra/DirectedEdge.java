package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;

import bfst20.mapdrawer.drawing.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DirectedEdge implements Serializable, Drawable {

    private static final long serialVersionUID = 1L;
    
    private final int from;
    private final int to;
    private final double speed;
    private final double distance;
    private final boolean bike;
    private final boolean walk;
    private final boolean car;
    private final boolean roundabout;
    private final String road;
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;


    public DirectedEdge(int from, int to, double speed, double distance, boolean bike, boolean walk, boolean car, boolean roundabout, String road, float x1, float y1, float x2, float y2) {
        this.from = from;
        this.to = to;
        this.speed = speed;
        this.distance = distance;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
        this.roundabout = roundabout;
        this.road = road;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    // Only used for helicopter route
    public DirectedEdge(double distance, float x1, float y1, float x2, float y2) {
        this.from = -1;
        this.to = -1;
        this.speed = -1;
        this.distance = distance;
        this.bike = false;
        this.walk = false;
        this.car = false;
        this.roundabout = false;
        this.road = null;
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

    public double getSpeed() {
        return speed;
    }

    public double getDistance() {
        return distance;
    }

    public String getRoad() {
        return road;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }
    
    public double getY2() {
        return y2;
    }

    public boolean isCar() {
        return car;
    }

    public boolean isBike() {
        return bike;
    }

    public boolean isWalk() {
        return walk;
    }

    public boolean isRoundabout() {
        return roundabout;
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
