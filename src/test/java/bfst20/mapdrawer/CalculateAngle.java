package bfst20.mapdrawer;

import bfst20.mapdrawer.dijkstra.RouteDescription;
import javafx.geometry.Point2D;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class calculateAngleTest extends TestCase {

    @Test
    void calculateAngle90leftTest() {
        //Points created
        Point2D p1 = new Point2D((0.56f*2),-(2));
        Point2D p2 = new Point2D((0.56f*2),-(4));
        Point2D p3 = new Point2D((0.56f*1),-(4));

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), -(p2.getY()-p1.getY()));
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), -(p3.getY()-p2.getY()));

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);
        int leftOrRight = RouteDescription.ccw(p1, p2, p3);

        //assert
        assertEquals(90.0, angle);
        assertEquals(-1, leftOrRight);
    }

    @Test
    void calculateAngle90RightTest() {
        Point2D p1 = new Point2D((0.56f*4),-(4));
        Point2D p2 = new Point2D((0.56f*4),-(10));
        Point2D p3 = new Point2D((0.56f*10),-(10));

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), -(p2.getY()-p1.getY()));
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), -(p3.getY()-p2.getY()));

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);
        int leftOrRight = RouteDescription.ccw(p1, p2, p3);

        //assert
        assertEquals(90.0, angle);
        assertEquals(1, leftOrRight);

    }

    @Test
    void calculateAngle180straightTest() {
        //Points created
        Point2D p1 = new Point2D((0.56f*2),-(2));
        Point2D p2 = new Point2D((0.56f*2),-(4));
        Point2D p3 = new Point2D((0.56f*2),-(8));

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), -(p2.getY()-p1.getY()));
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), -(p3.getY()-p2.getY()));

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);
        int leftOrRight = RouteDescription.ccw(p1, p2, p3);

        //assert
        assertEquals(180.0, angle);
        assertEquals(0, leftOrRight);
    }
    @Test
    void calculateAngleSharpRightTest() {
        //Points created
        Point2D p1 = new Point2D((0.56f*2),-(2));
        Point2D p2 = new Point2D((0.56f*2),-(10));
        Point2D p3 = new Point2D((0.56f*3),-(3));

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), -(p2.getY()-p1.getY()));
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), -(p3.getY()-p2.getY()));

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);
        int leftOrRight = RouteDescription.ccw(p1, p2, p3);

        //assert
        assertEquals(8, angle, 10);
        assertEquals(1, leftOrRight);
    }

    @Test
    void calculateAngleSharpLeftTest() {
        //Points created
        Point2D p1 = new Point2D((0.56f*2),-(2));
        Point2D p2 = new Point2D((0.56f*2),-(10));
        Point2D p3 = new Point2D((0.56f*1),-(1));

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), -(p2.getY()-p1.getY()));
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), -(p3.getY()-p2.getY()));

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);
        int leftOrRight = RouteDescription.ccw(p1, p2, p3);

        //assert
        assertEquals(8, angle, 10);
        assertEquals(-1, leftOrRight);
    }
}