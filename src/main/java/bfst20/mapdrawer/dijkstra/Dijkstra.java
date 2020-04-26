package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Stack;

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

    public Stack<DirectedEdge> pathTo(int v) {
        if(!hasPathTo(v)) {
            return null;
        }
        Stack<DirectedEdge> path = new Stack<DirectedEdge>();

        for (DirectedEdge edge = edgeTo[v]; edge != null; edge = edgeTo[edge.from()]) {
            path.push(edge);
        }
        return path;
    }

    private void relax(Graph g, int v, Vehicle vehicle) {
        //Taking an int v, it checks all vertices that you can go to from v.
        for (DirectedEdge edge : g.adja(v)) {

            if (edge.isOnlyCar() && vehicle.isCar()) {
                relaxMethod(v, edge);
            } else if(edge.isOnlyBike() && vehicle.isBike()) {
                relaxMethod(v, edge);
            } else if (edge.isOnlyWalk() && vehicle.isWalk()) {
                relaxMethod(v, edge);
            } else {
                relaxMethod(v, edge);
            }
        }
    }

    private void relaxMethod(int v, DirectedEdge edge) {
        int w = edge.to();
        //checks if the distance to w is bigger than the distance to v + the weight to w.
        //if it is, w's distance is updated, and it's edgeTo is set to be v.
        if (distTo[w] > distTo[v] + edge.getWeight()) {
            distTo[w] = distTo[v] + edge.getWeight();
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

