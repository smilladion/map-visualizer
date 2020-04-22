package bfst20.mapdrawer.drawing;

import bfst20.mapdrawer.osm.OSMNode;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.Serializable;
import java.util.Scanner;

public class Line implements Drawable, Serializable {

    private static final long serialVersionUID = 1L;

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

    public Line(OSMNode from, OSMNode to) {
        this(from.getLon(), from.getLat(), to.getLon(), to.getLat());
    }

    public Line(Point2D p1, Point2D p2) {
        this.x1 = p1.getX();
        this.y1 = p1.getY();
        this.x2 = p2.getX();
        this.y2 = p2.getY();
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }

    public void drawAndSetWidth(GraphicsContext gc, double width) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(width);
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }
}
