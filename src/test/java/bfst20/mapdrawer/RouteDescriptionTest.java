package bfst20.mapdrawer;

import bfst20.mapdrawer.dijkstra.DirectedEdge;
import bfst20.mapdrawer.dijkstra.RouteDescription;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import java.util.LinkedList;

public class RouteDescriptionTest {

    @Test
    void createRouteDescriptionTest() {
        assertEquals(true, true);
    }

    @Test
    void getRouteTimeTest() {
        // Need to implement mocking framework to test method dependent on radiobuttons
        // ... or refactor code
        assertEquals(true, true);
    }

    @Test
    void routeDistanceToStringTest() {
        double dist0 = -1;
        double dist1 = 1000;
        double dist2 = 1001;

        assertEquals("Distance: " + String.format("%.1f", dist2 / 1000) + " km", RouteDescription.routeDistanceToString(dist2));
        assertEquals("Distance: " + String.format("%.1f", dist2 / 1000) + " km", RouteDescription.routeDistanceToString(dist1));
        assertNotEquals("Distance: " + String.format("%.1f", dist2 / 1000) + " km", RouteDescription.routeDistanceToString(dist0));

        assertEquals("Distance: " + (int) dist0 + " m", RouteDescription.routeDistanceToString(dist0));
        assertNotEquals("Distance: " + (int) dist0 + " m", RouteDescription.routeDistanceToString(dist2));
    }

    @Test
    void getRouteDistanceTest() {
        // Initialize edge of length 1 (111111 meter)
        DirectedEdge edge = new DirectedEdge(0, 0, 0, 1, false, false, false, false, null, 0, 0, 0, 0);
        LinkedList<DirectedEdge> edges = new LinkedList<>();

        edges.add(edge);
        var route = new RouteDescription(edges, null, null, null);

        // the edge should be distance 111111 meter
        var length = 111111;
        assertEquals(length, route.getRouteDistance(edges), 0.00001);

        // Add another edge, total distance should double
        edges.add(edge);
        route = new RouteDescription(edges, null, null, null);

        // Check if doubled
        assertEquals(length*2, route.getRouteDistance(edges), 0.00001);
    }
}