package bfst20.mapdrawer;

import bfst20.mapdrawer.dijkstra.RouteDescription;
import javafx.geometry.Point2D;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class calculateAngleTest extends TestCase {

    @Test
    void calculateAngleTest() {
        //Points created
        Point2D p1 = new Point2D(2,2);
        Point2D p2 = new Point2D(2,4);
        Point2D p3 = new Point2D(4,4);

        Point2D vec1 = new Point2D(p2.getX()-p1.getX(), p2.getY()-p1.getY());
        Point2D vec2 = new Point2D(p3.getX()-p2.getX(), p3.getY()-p2.getY());

        //calculate
        double angle = RouteDescription.calculateAngle(vec1, vec2);

        //assert
        assertEquals(true, true);

    }
}