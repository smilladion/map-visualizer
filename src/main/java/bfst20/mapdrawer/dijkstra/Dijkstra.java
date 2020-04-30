package bfst20.mapdrawer.dijkstra;

import bfst20.mapdrawer.exceptions.NoRouteException;
import edu.princeton.cs.algs4.IndexMinPQ;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * This class uses Dijkstra's algorithm to find the shortest path between two points.
 * It takes in a graph containing all roads, a source point, and the type of route.
 * When you use pathTo(), it then calculates the quickest route for cars (varying speed limits),
 * and the shortest route for bike/walk. Class inspired by algs4 library.
 */
public class Dijkstra implements Serializable {

    private static final long serialVersionUID = 1L;

    private final DirectedEdge[] edgeTo;
    private final double[] distTo;
    private final IndexMinPQ<Double> pq;

    public Dijkstra(Graph g, int s, Vehicle vehicle) {
        edgeTo = new DirectedEdge[g.getVertices()];
        distTo = new double[g.getVertices()];

        pq = new IndexMinPQ<>(g.getVertices());

        for (int v = 0; v < g.getVertices(); v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[s] = 0.0;

        pq.insert(s, 0.0);

        while (!pq.isEmpty()) {
            relax(g, pq.delMin(), vehicle);
        }
    }

    // Determines whether the chosen path from the source exists.
    private boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    /**
     * Calculates and returns the shortest route from the source to the chosen point.
     */
    public LinkedList<DirectedEdge> pathTo(int v, Vehicle vehicle) throws NoRouteException {
        if (!hasPathTo(v)) {
            String vehicleAlternative1;
            String vehicleAlternative2;
            if (vehicle.isCar()) {
                vehicleAlternative1 = "Cykel";
                vehicleAlternative2 = "Gå";
            } else if (vehicle.isBike()) {
                vehicleAlternative1 = "Bil";
                vehicleAlternative2 = "Gå";
            } else {
                vehicleAlternative1 = "Bil";
                vehicleAlternative2 = "Cykel";
            }
            throw new NoRouteException(vehicleAlternative1, vehicleAlternative2);
        }
        LinkedList<DirectedEdge> path = new LinkedList<>();

        for (DirectedEdge edge = edgeTo[v]; edge != null; edge = edgeTo[edge.from()]) {
            path.addFirst(edge);
        }
        return path;
    }

    // Taking an int v, the method checks all vertices that you can go to from v.
    private void relax(Graph g, int v, Vehicle vehicle) {
        if (g.adja(v) != null) {
            for (DirectedEdge edge : g.adja(v)) {

                if (edge.isCar() && vehicle.isCar()) {
                    relaxMethod(v, edge, vehicle);
                } else if (edge.isBike() && vehicle.isBike()) {
                    relaxMethod(v, edge, vehicle);
                } else if (edge.isWalk() && vehicle.isWalk()) {
                    relaxMethod(v, edge, vehicle);
                }
            }
        }
    }

    // Attempts to relax the inputted edge, with changes to weight depending on vehicle type.
    private void relaxMethod(int v, DirectedEdge edge, Vehicle vehicle) {
        int w = edge.to();

        double weight = 0;
        if (vehicle.isCar()) {
            weight = edge.getDistance() / edge.getSpeed();
        } else {
            weight = edge.getDistance();
        }
        
        // Checks if the distance to w is bigger than the distance to v + the weight to w.
        // If it is, w's distance is updated, and its edgeTo is set to be v.
        if (distTo[w] > distTo[v] + weight) {
            distTo[w] = distTo[v] + weight;
            edgeTo[w] = edge;
            // As it always relaxes the edge that has the shortest distance to s (and has not been relaxed yet) we need to update w's position in the PQ.
            if (pq.contains(w)) {
                pq.changeKey(w, distTo[w]);
            } else {
                pq.insert(w, distTo[w]);
            }
        }
    }
}
