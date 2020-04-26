package bfst20.mapdrawer.dijkstra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Stack;

public class Dijkstra implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private DirectedEdge[] edgeTo;
    private double[] distTo;
    private IndexMinPQ<Double> pq;

    public Dijkstra(Graph g, int s) {

        edgeTo = new DirectedEdge[g.getVertices()];
        distTo = new double[g.getVertices()];

        pq = new IndexMinPQ<Double>(g.getVertices());

        for (int v = 0; v < g.getVertices(); v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[s] = 0.0;

        pq.insert(s, 0.0);


        while (!pq.isEmpty()) {

            relax(g, pq.delMin());
        }
    }

    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    public List<DirectedEdge> pathTo(int v) {
        if(!hasPathTo(v)) {
            return null;
        }
        List<DirectedEdge> path = new ArrayList<>();

        for (DirectedEdge edge = edgeTo[v]; edge != null; edge = edgeTo[edge.from()]) {
            path.add(edge);
        }
        return path;
    }

    private void relax(Graph g, int v) {
        for (DirectedEdge edge : g.adja(v)) {

            int w = edge.to();
            if (distTo[w] > distTo[v] + edge.getWeight()) {
                distTo[w] = distTo[v] + edge.getWeight();
                edgeTo[w] = edge;
                if (pq.contains(w)) {
                    pq.changeKey(w, distTo[w]);
                } else {
                    pq.insert(w, distTo[w]);
                }
            }
        }
    }
}

