package bfst20.mapdrawer.dijkstra;

import bfst20.mapdrawer.map.MapController;
import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to create a route description from the chosen route 
 * - this is accomplished by using createRouteDescription(). It also has other
 * methods used for calculating time and distance of a route.
 */
public class RouteDescription {
    private final OSMMap model;
    private final MapView view;
    private final MapController controller;

    private final LinkedList<DirectedEdge> edgeList;
    List<String> routeDescriptionList;

    public RouteDescription(LinkedList<DirectedEdge> edgeList, OSMMap model, MapView view, MapController controller) {
        this.edgeList = edgeList;
        this.model = model;
        this.view = view;
        this.controller = controller;
    }

    public List<String> createRouteDescription() {
        routeDescriptionList = new ArrayList<>();
        String startAt = ("Start ved: " + System.lineSeparator() + view.getFromSearchField().getCharacters());
        routeDescriptionList.add(startAt);

        int roundaboutExit = 0;

        for (int i = 0; i < edgeList.size() - 1; i++) {
            DirectedEdge current = edgeList.get(i);
            DirectedEdge next = edgeList.get(i + 1);

            String currentRoad = current.getRoad();
            String nextRoad = next.getRoad();

            if (currentRoad == null) {
                currentRoad = "ukendt vej";
            }
            if (nextRoad == null) {
                nextRoad = "ukendt vej";
            }

            if (i == 0) {
                String straightAhead = ("Fortsæt ligeud ad " + currentRoad);
                routeDescriptionList.add(straightAhead);
            }

            if (next.isRoundabout()) {
                int numberForGraph = next.to();
                int outgoing = model.getRouteGraph().numberOfOutgoingEdges(numberForGraph);
                if (outgoing > 2) {
                    roundaboutExit++;
                }
            } else if (!currentRoad.equals(nextRoad)) {
                if (roundaboutExit > 0) {
                    String roundAbout = "Ved rundkørslen, tag den " + roundaboutExit + ". afkørsel";
                    routeDescriptionList.add(roundAbout);
                    roundaboutExit = 0;
                } else {
                    // The 3 points for ccw.
                    Point2D a = new Point2D(current.getX1(), current.getY1());
                    Point2D b = new Point2D(current.getX2(), current.getY2());
                    Point2D c = new Point2D(next.getX2(), next.getY2());

                    int ccw = ccw(a, b, c);

                    // Making the two edges into direction vectors.
                    Point2D vectorFrom = new Point2D(current.getX2() - current.getX1(), -(current.getY2() - current.getY1()));
                    Point2D vectorTo = new Point2D(next.getX2() - current.getX2(), -(next.getY2() - current.getY2()));

                    double angle = calculateAngle(vectorFrom, vectorTo);

                    if (angle > 20 && angle < 150) {
                        if (ccw > 0) {
                            String turnRight = ("Drej til højre ad " + nextRoad);
                            routeDescriptionList.add(turnRight);
                        } else if (ccw < 0) {
                            String turnLeft = ("Drej til venstre ad " + nextRoad);
                            routeDescriptionList.add(turnLeft);
                        }
                    } else if (angle > 150) {
                        if (ccw > 0) {
                            String continueForward = ("Fortsæt ligeud ad " + nextRoad);
                            routeDescriptionList.add(continueForward);
                        } else if (ccw < 0) {
                            String continueForwardTwo = ("Fortsæt ligeud ad " + nextRoad);
                            routeDescriptionList.add(continueForwardTwo);
                        }
                    } else if (angle < 20) {
                        if (ccw > 0) {
                            String turnHardRight = ("Drej skarpt til højre ad " + nextRoad);
                            routeDescriptionList.add(turnHardRight);
                        } else if (ccw < 0) {
                            String turnHardLeft = ("Drej skarpt til venstre ad " + nextRoad);
                            routeDescriptionList.add(turnHardLeft);
                        }
                    } else if (ccw == 0) {
                        String continueForwardThree = ("Fortsæt ligeud ad " + nextRoad);
                        routeDescriptionList.add(continueForwardThree);
                    }
                }
            }
        }

        if (view.getHelicopter().isSelected()) {
            String s = "Flyv ligeud";
            routeDescriptionList.add(s);
        }
        
        String destination = ("Ankommet til destination: " + System.lineSeparator() + view.getToSearchField().getCharacters());
        routeDescriptionList.add(destination);

        return routeDescriptionList;
    }
    
    // Calculates the angle between two vectors.
    public static double calculateAngle(Point2D vectorFrom, Point2D vectorTo) {
        double dot = vectorFrom.dotProduct(vectorTo);
        double lengthFrom = (Math.sqrt(((vectorFrom.getX()) * (vectorFrom.getX())) + ((vectorFrom.getY()) * (vectorFrom.getY()))));
        double lengthTo = (Math.sqrt(((vectorTo.getX()) * (vectorTo.getX())) + ((vectorTo.getY()) * (vectorTo.getY()))));

        double cosv = (dot / (lengthFrom * lengthTo));

        double angle = Math.acos(cosv);
        double angle1 = Math.toDegrees(angle);

        double realAngle = 180 - angle1;

        return realAngle;
    }

    // From algs4 library.
    public static int ccw(Point2D a, Point2D b, Point2D c) {
        double area2 = (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
        if (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else return 0;
    }

    /** Returns the time it takes to drive/cycle/walk the currently selected route. */
    public String getRouteTime() {
        double timeInHours = 0;

        for (DirectedEdge edge : controller.getRouteEdges()) {
            double distance = (111111 * edge.getDistance()) / 1000; // 111111 is roughly meters per 1 degree lat

            if (view.getCar().isSelected()) {
                timeInHours += distance / edge.getSpeed();
            } else if (view.getBike().isSelected()) {
                timeInHours += distance / 16;
            } else if (view.getWalk().isSelected()) {
                timeInHours += distance / 5;
            } else if (view.getHelicopter().isSelected()) {
                timeInHours += distance / 200;
            }
        }

        if (timeInHours < 1) {
            double minutes = timeInHours * 60;
            
            if (minutes < 1) {
                return "Tid: " + (int) (minutes * 60) + " sek";
            }
            
            return "Tid: " + (int) minutes + " min";
        } else {
            int hours = (int) timeInHours;
            int minutes = (int) ((timeInHours - hours) * 60);
            return "Tid: " + hours + " t. " + minutes + " min";
        }
    }

    /** Returns the given distance (in meters) as a string. */
    public static String routeDistanceToString(double distance) {
        if (distance >= 1000) {
            return "Distance: " + String.format("%.1f", distance / 1000) + " km";
        } else {
            return "Distance: " + (int) distance + " m";
        }
    }

    /** Returns the distance of the currently selected route in meters. */
    public double getRouteDistance(LinkedList<DirectedEdge> edgeList) {
        double totalDistanceMeters = 0;

        for (DirectedEdge edge : edgeList) {
            totalDistanceMeters += 111111 * edge.getDistance(); // 111111 is roughly meters per 1 degree lat
        }

        return totalDistanceMeters;
    }
}
