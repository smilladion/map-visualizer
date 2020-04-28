package bfst20.mapdrawer.dijkstra;

import bfst20.mapdrawer.Exceptions.noAddressMatchException;
import bfst20.mapdrawer.Exceptions.noRouteException;
import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RouteDescription {

    private LinkedList<DirectedEdge> edgeList;

    public List<String> getRouteDescriptionList() {
        return routeDescriptionList;
    }

    List<String> routeDescriptionList;
    private final OSMMap model;
    private MapView view;

    public RouteDescription(LinkedList<DirectedEdge> edgeList, OSMMap model, MapView view) {
        this.edgeList = edgeList;
        this.model = model;
        this.view = view;
    }

    public List<String> createRouteDescription() {
        routeDescriptionList = new ArrayList();
        String startAt = ("Start ved " + System.lineSeparator() + view.getFromSearchField().getCharacters());
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
                System.out.println("Fortsæt ligeud ad " + currentRoad);
            }

            if (next.isRoundabout()) {
                int numberForGraph = next.to();
                int outgoing = model.getRouteGraph().numberOfOutgoingEdges(numberForGraph);
                if (outgoing > 2) {
                    roundaboutExit++;
                }
            } else if (!currentRoad.equals(nextRoad)) {

                    if (roundaboutExit > 0) {
                        String roundAbout = ("Ved rundkørslen, tag den " + roundaboutExit + ". afkørsel");
                        System.out.println("Ved rundkørslen, tag den " + roundaboutExit + ". afkørsel");
                        roundaboutExit = 0;
                    } else {

                        //the 3 points for ccw
                        Point2D a = new Point2D(current.getX1(), current.getY1());
                        Point2D b = new Point2D(current.getX2(), current.getY2());
                        Point2D c = new Point2D(next.getX2(), next.getY2());

                        int ccw = ccw(a, b, c);

                        //making the two edges into direction vectors.
                        Point2D vectorFrom = new Point2D(current.getX2() - current.getX1(), -(current.getY2() - current.getY1()));
                        Point2D vectorTo = new Point2D(next.getX2() - current.getX2(), -(next.getY2() - current.getY2()));

                        double angle = calculateAngle1(vectorFrom, vectorTo);

                        if (angle > 20 && angle < 150) {
                            if (ccw > 0) {
                                String turnRight = ("Drej til højre ad " + nextRoad);
                                routeDescriptionList.add(turnRight);
                                System.out.println("Drej til højre ad " + nextRoad);
                            } else if (ccw < 0) {
                                String turnLeft = ("Drej til venstre ad "+ nextRoad);
                                routeDescriptionList.add(turnLeft);
                                System.out.println("Drej til venstre ad " + nextRoad);
                            }
                        } else if (angle > 150) {
                            if (ccw > 0) {
                                String continueForward = ("Fortsæt ligeud ad " + nextRoad);
                                routeDescriptionList.add(continueForward);
                                System.out.println("Fortsæt ligeud ad " + nextRoad);
                            } else if (ccw < 0) {
                                String continueForwardTwo = ("Fortsæt ligeud ad " + nextRoad);
                                routeDescriptionList.add(continueForwardTwo);
                                System.out.println("Fortsæt ligeud ad " + nextRoad);

                            }
                        } else if (angle < 20) {
                            if (ccw < 0) {
                                String turnHardRight = ("Drej skarpt til højre ad " + nextRoad);
                                routeDescriptionList.add(turnHardRight);
                                System.out.println("Drej skarpt til højre ad " + nextRoad);
                            } else if (ccw > 0) {
                                String turnHardLeft = ("Drej skarpt til venstre ad " + nextRoad);
                                routeDescriptionList.add(turnHardLeft);
                                System.out.println("Drej skarpt til venstre ad " + nextRoad);
                            }
                        } else if (ccw == 0) {
                            String continueForwardThree = ("Fortsæt ligeud ad " + nextRoad);
                            routeDescriptionList.add(continueForwardThree);
                            System.out.println("Fortsæt ligeud ad " + nextRoad);
                        }
                    }
                }
            }
        String destination = ("Ankommet til destination: " + System.lineSeparator() + view.getToSearchField().getCharacters());
        routeDescriptionList.add(destination);

        return routeDescriptionList;
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
/*
    public searchActionDijsktra() {
        view.getSearchedDrawables().clear();

        String addressTo = view.getToSearchField().getText();
        String addressFrom = view.getFromSearchField().getText();

        OSMNode nodeTo = null;
        OSMNode nodeFrom = null;

        for (OSMNode node : model.getAddressNodes()) {

            if (node.getAddress().equals(addressTo)) {
                nodeTo = node;
            }
            if (node.getAddress().equals(addressFrom)) {
                nodeFrom = node;
            }
        }

        if (addressTo.equals("")) {
            addressTo = null;
        }
        if (addressFrom.equals("")) {
            addressFrom = null;
        }
        if (nodeTo == null) {
            addressTo = null;
        }
        if (nodeFrom == null) {
            addressFrom = null;
        }

        try {
            view.paintPoints(addressTo, addressFrom, false);
        } catch (noAddressMatchException ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ingen adresse fundet");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }

        if (addressFrom != null && addressTo != null) {
            if (!routeEdges.isEmpty()) {
                routeEdges.clear();
            }
            Vehicle vehicle;

            if (view.getCar().isSelected()) {
                vehicle = new Car();
            } else if (view.getBike().isSelected()) {
                vehicle = new Bike();
            } else {
                vehicle = new Walk();
            }

            if (nodeTo != null && nodeFrom != null) {
                OSMWay nearestTo = model.getHighwayTree().nearest(nodeTo.getLon(), nodeTo.getLat());
                OSMWay nearestFrom = model.getHighwayTree().nearest(nodeFrom.getLon(), nodeFrom.getLat());

                Point2D pointTo = new Point2D(nodeTo.getLon(), nodeTo.getLat());
                OSMNode nearestToNode = model.getHighwayTree().nodeDistance(pointTo, nearestTo);

                Point2D pointFrom = new Point2D(nodeFrom.getLon(), nodeFrom.getLat());
                OSMNode nearestFromNode = model.getHighwayTree().nodeDistance(pointFrom, nearestFrom);

                try {
                    if (!lastSearchFrom.equals(view.getFromSearchField().getText())) {

                        dijkstra = new Dijkstra(model.getRouteGraph(), nearestFromNode.getNumberForGraph(), vehicle);

                        routeEdges = dijkstra.pathTo(nearestToNode.getNumberForGraph(), vehicle);
                    } else {
                        routeEdges = dijkstra.pathTo(nearestToNode.getNumberForGraph(), vehicle);
                    }
                } catch (noRouteException ex) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ingen rute fundet");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }

                double distance = 0;

                for (DirectedEdge edge : routeEdges) {
                    distance = distance + edge.getDistance();
                }

                distance = distance * 10000;
                distance = Math.ceil(distance);

                System.out.println("Tid: " + distance + " min");

            }
    }

 */
}
