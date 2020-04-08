package routefinding;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Bag;
import java.util.ArrayList;
import java.util.List;

public class EdgeWeightedDiGraph {
    private int vertexAmount;             // number of vertices in this digraph
    private int edgeAmount;               // number of edges in this digraph

    private int[] indegree;              // indegree[v] = indegree of vertex v
    private Bag<DirectedEdge>[] adj;         // adj[v] = adjacency list for vertex v
    private List<DirectedEdge> graph = new ArrayList<>();

    public EdgeWeightedDiGraph(List<OSMWay> highways) {
        for (OSMWay way : highways) {

            for (int i = 0; i < way.getNodes().size(); i++) {
                DirectedEdge edge = new DirectedEdge(i, i+1);
                addEdge(edge);
            }
        }
    }

    public void addEdge(DirectedEdge directedEdge) {
        int v = directedEdge.from();
        int w = directedEdge.to();
        //validateVertex(v);
        //validateVertex(w);

        adj[v].add(directedEdge);
        indegree[w]++;
        edgeAmount++;

        graph.add(directedEdge);
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= graph.size())
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (graph.size()-1));
    }


    public int indegree(int v) {
        //validateVertex(v);
        return indegree[v];
    }

    public int outdegree(int v) {
        //validateVertex(v);
        return adj[v].size();
    }

    public Iterable<DirectedEdge> edges() {
        Bag<DirectedEdge> list = new Bag<DirectedEdge>();
        for (int v = 0; v < graph.size(); v++) {
            for (DirectedEdge directedEdge : adj(v)) {
                list.add(directedEdge);
            }
        }
        return list;
    }

    public Iterable<DirectedEdge> adj(int v) {
        //validateVertex(v);
        return adj[v];
    }
}