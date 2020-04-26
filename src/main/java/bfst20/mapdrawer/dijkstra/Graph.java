package bfst20.mapdrawer.dijkstra;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Bag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Graph implements Serializable{

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
            boolean onewayAll = way.isOnewayAll();

            for (int i = 0; i < way.getNodes().size() - 1; i++) {
                OSMNode node = way.getNodes().get(i);
                OSMNode node1 = way.getNodes().get(i+1);
                int from = node.getNumberForGraph();
                int to = node1.getNumberForGraph();

                double x1 = node.getLon();
                double y1 = node.getLat();
                double x2 = node1.getLon();
                double y2 = node1.getLat();

                double tempWeight = (Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1))));
                double weight = tempWeight / speed;

                if (!onewayAll) {
                    addEdge(from, to, weight, bike, walk, car, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat(), node, node1);
                    addEdge(to, from, weight, bike, walk, car, road, node1.getLon(), node1.getLat(), node.getLon(), node.getLat(), node1, node);
                } else if (onewayCar){
                    addEdge(from, to, weight, bike, walk, car, road, node.getLon(), node.getLat(), node1.getLon(), node1.getLat(), node, node1);
                    addEdge(from, to, weight, bike, walk, false, road, node1.getLon(), node1.getLat(), node.getLon(), node.getLat(), node1, node);
                }
            }
        }
    }

    public void addEdge(int from, int to, double weight, boolean bike, boolean walk, boolean car, String road, double x1, double y1, double x2, double y2, OSMNode node, OSMNode node1) {
        DirectedEdge edge = new DirectedEdge(from, to, weight, bike, walk, car, road, x1, y1, x2, y2, node, node1);
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

    public int getVertices() {
        return vertices;
    }
}

