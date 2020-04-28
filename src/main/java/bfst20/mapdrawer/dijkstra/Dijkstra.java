package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;
import java.util.LinkedList;

import bfst20.mapdrawer.exceptions.*;

import edu.princeton.cs.algs4.IndexMinPQ;

public class Dijkstra implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private DirectedEdge[] edgeTo;
    private double[] distTo;
    private IndexMinPQ<Double> pq;

    public Dijkstra(Graph g, int s, Vehicle vehicle) {

        edgeTo = new DirectedEdge[g.getVertices()];
        distTo = new double[g.getVertices()];

        pq = new IndexMinPQ<Double>(g.getVertices());

        for (int v = 0; v < g.getVertices(); v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[s] = 0.0;

        pq.insert(s, 0.0);

        while (!pq.isEmpty()) {

            relax(g, pq.delMin(), vehicle);
        }
    }

    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    public LinkedList<DirectedEdge> pathTo(int v, Vehicle vehicle) throws NoRouteException {
        if(!hasPathTo(v)) {
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

    private void relax(Graph g, int v, Vehicle vehicle) {
        //Taking an int v, it checks all vertices that you can go to from v.
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

    private void relaxMethod(int v, DirectedEdge edge, Vehicle vehicle) {
        int w = edge.to();

        double weight = 0;
        if (vehicle.isCar()) {
            weight = edge.getDistance() / edge.getSpeed();
        } else {
            weight = edge.getDistance();
        }
        //checks if the distance to w is bigger than the distance to v + the weight to w.
        //if it is, w's distance is updated, and it's edgeTo is set to be v.
        if (distTo[w] > distTo[v] + weight) {
            distTo[w] = distTo[v] + weight;
            edgeTo[w] = edge;
            //as it always relaxes the edge that has the shortest distance to s (and has not been relaxed yet) we need to update w's position in the pq.
            if (pq.contains(w)) {
                pq.changeKey(w, distTo[w]);
            } else {
                pq.insert(w, distTo[w]);
            }
        }
    }
}
