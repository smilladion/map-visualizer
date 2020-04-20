package bfst20.mapdrawer.Rutevejledning;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Bag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Graph {

        int vertices;
        ArrayList<DirectedEdge>[] adj;

    public Graph(int vertices, List<OSMWay> highways) {
        this.vertices = vertices;
        adj = new ArrayList[vertices];

        for (OSMWay way : highways) {

            boolean bike = way.isBike();
            boolean walk = way.isWalk();
            boolean car = way.isCar();
            int weight = way.getWeight();

            for (int i = 0; i < way.getNodes().size() - 1; i++) {
                int from = way.getNodes().get(i).getNumberForGraph();
                int to = way.getNodes().get(i + 1).getNumberForGraph();

                addEdge(from, to, weight, bike, walk, car);
                addEdge(to, from, weight, bike, walk, car);
            }
        }
    }

    public void addEdge(int from, int to, int weight, boolean bike, boolean walk, boolean car) {
        DirectedEdge edge = new DirectedEdge(from, to, weight, bike, walk, car);
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

