package bfst20.mapdrawer.dijkstra;

import javafx.geometry.Point2D;

import java.util.LinkedList;
import java.util.List;

public class RouteDescription {

    private LinkedList<DirectedEdge> edgeList;

    public RouteDescription(LinkedList<DirectedEdge> edgeList) {
        this.edgeList = edgeList;
    }

    public void createRouteDescription() {

        for (int i = 0; i < edgeList.size()-1; i++) {

            DirectedEdge current = edgeList.get(i);
            DirectedEdge next = edgeList.get(i+1);

            String currentRoad = current.getRoad();
            String nextRoad = next.getRoad();

            if (currentRoad == null) {
                currentRoad = "ukendt vej";
            }
            if (nextRoad == null) {
                nextRoad = "ukendt vej";
            }

            if (i == 0) {
                System.out.println("Fortsæt ligeud ad " + currentRoad);
            }

            if (!currentRoad.equals(nextRoad)) {

                //the 3 points for ccw
                Point2D a = new Point2D(current.getX1(), current.getY1());
                Point2D b = new Point2D(current.getX2(), current.getY2());
                Point2D c = new Point2D(next.getX2(), next.getY2());

                int ccw = ccw(a, b, c);

                //making the two edges into direction vectors.
                Point2D vectorFrom = new Point2D(current.getX2() - current.getX1(), - (current.getY2() - current.getY1()));
                Point2D vectorTo = new Point2D(next.getX2() - current.getX2(), - (next.getY2() - current.getY2()));

                double angle = calculateAngle1(vectorFrom, vectorTo);

                if (angle > 20 && angle < 150) {
                    if (ccw > 0) {
                        System.out.println("Drej til højre ad " + nextRoad);
                    } else if (ccw < 0) {
                        System.out.println("Drej til venstre ad "+ nextRoad);
                    }
                } else if (angle > 150) {
                    if (ccw > 0) {
                        System.out.println("Fortsæt ligeud ad " + nextRoad);
                    } else if (ccw < 0) {
                        System.out.println("Fortsæt ligeud ad " + nextRoad);

                    }
                } else if (angle < 20) {
                    if (ccw < 0) {
                        System.out.println("Drej skarpt til højre ad " + nextRoad);
                    } else if (ccw > 0) {
                        System.out.println("Drej skarpt til venstre ad " + nextRoad);
                    }
                } else if (ccw == 0) {
                    System.out.println("Fortsæt ligeud ad " + nextRoad);
                }
            }
        }
    }

    private double calculateAngle1(Point2D vectorFrom, Point2D vectorTo) {

        double dot = vectorFrom.dotProduct(vectorTo);
        double lengthFrom = (Math.sqrt(((vectorFrom.getX())*(vectorFrom.getX()))+((vectorFrom.getY())*(vectorFrom.getY()))));
        double lengthTo = (Math.sqrt(((vectorTo.getX())*(vectorTo.getX()))+((vectorTo.getY())*(vectorTo.getY()))));

        double cosv = (dot / (lengthFrom * lengthTo));

        double angle = Math.acos(cosv);
        double angle1 = Math.toDegrees(angle);

        double realAngle = 180 - angle1;

        return realAngle;
    }

    //From algs4 library
    public static int ccw(Point2D a, Point2D b, Point2D c) {
        double area2 = (b.getX()-a.getX())*(c.getY()-a.getY()) - (b.getY()-a.getY())*(c.getX()-a.getX());
        if      (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else                return  0;
    }
}
