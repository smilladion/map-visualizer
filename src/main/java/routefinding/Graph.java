package routefinding;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Bag;
import java.util.NoSuchElementException;
/*
public class Graph {
    private int vertex;             // number of vertices in this digraph
    private int edge;               // number of edges in this digraph
    private int[] indegree;         // indegree[v] = indegree of vertex v
    private Bag<Integer>[] adj;    // adj[v] = adjacency list for vertex v

    //Initialize new empty graph with vertices
    public Graph(int vertex) {
        if (vertex < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be nonnegative");
        this.vertex = vertex;     //Should be OSMWays.length
        this.edge = 0;              //This constructor is not useable, but for sanity - edge is the number of edges therefore OSMNodes.length
        indegree = new int[vertex];

        adj = (Bag<Integer>[]) new Bag[vertex];
        for (int i = 0; i < vertex; i++) {
            adj[i] = new Bag<Integer>();
        }
    }

    //Initialize new graph from input
    public Graph(OSMWay edge, OSMNode vertex) {
        if (edge == null) throw new IllegalArgumentException("edge is null");
        if (vertex == null) throw new IllegalArgumentException("vertice is null");
        try {
            this.vertex = in.readInt();
            if (vertex < 0) throw new IllegalArgumentException("number of vertices in a Digraph must be nonnegative");
            indegree = new int[vertex];
            adj = (Bag<Integer>[]) new Bag[vertex];
            for (int i = 0; i < vertex; i++) {
                adj[vertex] = new Bag<Integer>();
            }
            int E = in.readInt();
            if (E < 0) throw new IllegalArgumentException("number of edges in a Digraph must be nonnegative");
            for (int i = 0; i < E; i++) {
                int v = in.readInt();
                int w = in.readInt();
                addEdge(v, w);
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in Digraph constructor", e);
        }
    }

    public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        adj[v].add(w);
        indegree[w]++;
        edge++;
    }

    //Validate that vertex is within
    private void validateVertex(int v) {
        if (v < 0 || v >= vertex)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (vertex -1));
    }
}

 */
