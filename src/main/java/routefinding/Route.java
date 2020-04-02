package routefinding;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.*;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;
/*
public class Route implements Drawable {

    Point startNode;
    Point endNode;
    GraphicsContext gc;
    List<OSMWay> nodes;

    public Route(Point startNode, Point endNode){
        //startnode = input-i-frabox
        //endNode = input-1-tilbox

        //OSMWay routes = new OSMWay(startNode, endNode);



        //color = PathColor.UNKNOWN.getColor();
        gc.beginPath();
        gc.moveTo(startNode.getX1(), startNode.getY1());
        gc.lineTo(endNode.getX1(), endNode.getY1());
        gc.stroke();
    }

    public void shortestPath() {
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        int s = Integer.parseInt(args[1]);

        // compute shortest paths
        DijkstraSP sp = new DijkstraSP(G, s);


        // print shortest path
        for (int t = 0; t < G.V(); t++) {
            if (sp.hasPathTo(t)) {
                StdOut.printf("%d to %d (%.2f)  ", s, t, sp.distTo(t));
                for (DirectedEdge e : sp.pathTo(t)) {
                    StdOut.print(e + "   ");
                }
                StdOut.println();
            }
            else {
                StdOut.printf("%d to %d         no path\n", s, t);
            }
        }
    }


    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        for (OSMWay route : routes) {
            line.trace(gc);
        }
        gc.stroke();
    }


}
 */