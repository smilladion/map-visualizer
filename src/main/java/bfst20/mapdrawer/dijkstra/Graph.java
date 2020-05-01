package bfst20.mapdrawer.dijkstra;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Bag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Graph implements Serializable {

    private static final long serialVersionUID = 1L;

    int vertices;
    ArrayList<DirectedEdge>[] adj;

    public Graph(int vertices, List<OSMWay> highways) {
        this.vertices = vertices;
        adj = new ArrayList[vertices];

        for (OSMWay way : highways) {

            String road = way.getRoad();

            boolean bike = way.isBike();
            boolean walk = way.isWalk();
            boolean car = way.isCar();
            double speed = way.getSpeed();
            boolean onewayCar = way.isOnewayCar();
            boolean onewayBike = way.isOnewayBike();
            boolean onewayWalk = way.isOnewayWalk();
            boolean roundabout = way.isRoundabout();

            for (int i = 0; i < way.getNodes().size() - 1; i++) {
                OSMNode node = way.getNodes().get(i);
                OSMNode node1 = way.getNodes().get(i + 1);
                int from = node.getNumberForGraph();
                int to = node1.getNumberForGraph();

                double x1 = node.getLon();
                double y1 = node.getLat();
                double x2 = node1.getLon();
                double y2 = node1.getLat();
                
                double distance = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));

                if (!onewayCar && !onewayBike && !onewayWalk) {
                    addEdge(from, to, speed, distance, bike, walk, car, roundabout, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat());
                    addEdge(to, from, speed, distance, bike, walk, car, roundabout, road, node1.getLon(), node1.getLat(), node.getLon(), node.getLat());
                } else if (onewayCar && !onewayBike && !onewayWalk) {
                    addEdge(from, to, speed, distance, bike, walk, car, roundabout, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat());
                    addEdge(to, from, speed, distance, bike, walk, false, roundabout, road, node1.getLon(), node1.getLat(), node.getLon(), node.getLat());
                } else if (onewayCar && onewayBike && !onewayWalk) {
                    addEdge(from, to, speed, distance, bike, walk, car, roundabout, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat());
                    addEdge(from, to, speed, distance,false, true, false, roundabout, road, node1.getLon(), node1.getLat(), node.getLon(), node.getLat());
                } else if (onewayCar && onewayBike && onewayWalk) {
                    addEdge(from, to, speed, distance, bike, walk, car, roundabout, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat());
                }

            }
        }
    }
    
    public void addEdge(int from, int to, double speed, double distance, boolean bike, boolean walk, boolean car, boolean roundabout, String road, float x1, float y1, float x2, float y2) {
        DirectedEdge edge = new DirectedEdge(from, to, speed, distance, bike, walk, car, roundabout, road, x1, y1, x2, y2);
        if (adj[from] == null) {
            adj[from] = new ArrayList<>();
            adj[from].add(edge);
        } else {
            adj[from].add(edge);
        }
    }

    public Iterable<DirectedEdge> adja(int v) {
        return adj[v];
    }

    public Iterable<DirectedEdge> edges() {
        Bag<DirectedEdge> bag = new Bag<>();
        for (int v = 0; v < vertices; v++) {
            for (DirectedEdge edge : adj[v]) {
                bag.add(edge);
            }
        }
        return bag;
    }

    public int numberOfOutgoingEdges(int v) {
        int y = 0;
        for (DirectedEdge edge : adj[v]) {
            y++;
        }
        return y;
    }

    public int getVertices() {
        return vertices;
    }
}
