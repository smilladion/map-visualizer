package bfst20.mapdrawer.Rutevejledning;

import edu.princeton.cs.algs4.IndexMinPQ;

public class Dijkstra {

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
        }
    }

