package bfst20.mapdrawer.map;

import bfst20.mapdrawer.Launcher;

import bfst20.mapdrawer.dijkstra.*;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import edu.princeton.cs.algs4.Stack;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapController {

    private final OSMMap model;
    private MapView view;
    private final Stage stage;
    
    private final EventHandler<ActionEvent> clearAction;

    private final EventHandler<ActionEvent> saveAddressAction;
    private final EventHandler<MouseEvent> toggleAction;
    private final EventHandler<MouseEvent> colorToggleAction;

    private final EventHandler<ActionEvent> loadFileAction;
    private final EventHandler<ActionEvent> saveFileAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> clickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private final EventHandler<ActionEvent> searchActionDijkstra;

    private final EventHandler<MouseEvent> roadFinderAction;

    private Point2D lastMouse;

    private Dijkstra dijkstra;

    List<OSMNode> listForDijkstraOSMWay = new ArrayList<>();

    MapController(OSMMap model, MapView view, Stage stage) {
        this.model = model;
        this.view = view;
        this.stage = stage;

        clearAction = e -> {
            view.getToSearchField().clear();
            view.getFromSearchField().clear();
            view.getToSearchField().setPromptText("Til...");
            view.getFromSearchField().setPromptText("Fra...");
            view.getSearchedDrawables().clear();
            listForDijkstraOSMWay.clear();
            view.setPointOfInterest(new Point());
            view.paintPoints(null, null);
        };

        // Saves the current address to my list.
        saveAddressAction = e -> {
            if (!view.getPointOfInterest().isEmpty()) {
                view.getSavedPoints().add(view.getPointOfInterest());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Besked");
                alert.setHeaderText(null);
                alert.setContentText("Du skal først sætte et punkt på kortet (via. højreklik) for at kunne gemme det!");
                alert.showAndWait();
            }
        };

        toggleAction = e -> {
            if (view.getMyPointsToggle().isSelected()) {
                if (view.getSavedPoints().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Besked");
                    alert.setHeaderText(null);
                    alert.setContentText("Du har ingen gemte adresser!");
                    alert.showAndWait();
                } else {
                    view.paintSavedAddresses();
                }
            } else {
                view.paintPoints(null, null);
            }
        };
        
        colorToggleAction = e -> {
            view.paintMap();
        };
        
        searchActionDijkstra = e -> {
            view.getSearchedDrawables().clear();
            listForDijkstraOSMWay.clear();

            String addressTo = view.getToSearchField().getText().toLowerCase();
            String addressFrom = view.getFromSearchField().getText().toLowerCase();
            
            view.paintPoints(addressTo, addressFrom);
            
            if (!addressFrom.isEmpty() && !addressTo.isEmpty()) {
                
                OSMNode nodeTo = new OSMNode();
                OSMNode nodeFrom = new OSMNode();

                for (OSMNode node : model.getAddressNodes()) {
                    if (node.getAddress().equals(addressTo)) {
                        nodeTo = node;
                    }
                    if (node.getAddress().equals(addressFrom)) {
                        nodeFrom = node;
                    }
                }

                OSMWay nearestTo = model.getHighwayTree().nearest(nodeTo.getLon(), nodeTo.getLat());
                OSMWay nearestFrom = model.getHighwayTree().nearest(nodeFrom.getLon(), nodeFrom.getLat());

                Point2D pointTo = new Point2D(nodeTo.getLon(), nodeTo.getLat());
                OSMNode nearestToNode = model.getHighwayTree().nodeDistance(pointTo, nearestTo);

                Point2D pointFrom = new Point2D(nodeFrom.getLon(), nodeFrom.getLat());
                OSMNode nearestFromNode = model.getHighwayTree().nodeDistance(pointFrom, nearestFrom);

                Vehicle v;

                if (view.getCar().isSelected()) {
                    v = new Car();
                } else if (view.getBike().isSelected()) {
                    v = new Bike();
                } else {
                    v = new Walk();
                }

                dijkstra = new Dijkstra(model.getRouteGraph(), nearestFromNode.getNumberForGraph(), v);

                Stack<DirectedEdge> route = dijkstra.pathTo(nearestToNode.getNumberForGraph());

                List<DirectedEdge> edgeList = new ArrayList<>();

                //adds all the nodes from the route to a list. it only adds the "from" nodes, to avoid duplicates.
                //it check if its the last edge of the stack, and if it is it also adds the "to" node.
                while (!route.isEmpty()) {
                    OSMNode u = route.peek().getNodeFrom();
                    //OSMNode y = model.getIntToNode().get(route.peek().from());
                    listForDijkstraOSMWay.add(u);
                    if (route.size() == 1) {
                        OSMNode x = route.peek().getNodeTo();
                        listForDijkstraOSMWay.add(x);
                    }
                    edgeList.add(route.pop());
                }
                Type type = Type.SEARCHRESULT;

                double distance = 0;

                for (DirectedEdge edge : edgeList) {
                    distance = distance + edge.getWeight();
                }
                distance = distance * 10000;
                distance = Math.ceil(distance);

                System.out.println("Tid: " + distance + " min");

                OSMWay searchedWay = new OSMWay(1, listForDijkstraOSMWay, type, null);
                view.paintRoute(searchedWay);
                view.createRouteDescription(edgeList);
            }
        };

        clickAction = e -> {
            // Resets the value of lastMouse before the next pan/drag occurs
            if (!e.isPrimaryButtonDown()) {
                lastMouse = null;
            }

            try {
                Point2D mousePoint = view.getTransform().inverseTransform(e.getX(), e.getY());
                
                // Sets point of interest on right click
                if (e.getButton() == MouseButton.SECONDARY) {
                    Point p = new Point(mousePoint.getX(), mousePoint.getY(), view.getTransform());
                    view.setPointOfInterest(p);
                    view.paintMap();
                }
                
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
        };

        panAction = e -> {
            if (lastMouse != null) {
                view.pan(e.getX() - lastMouse.getX(), e.getY() - lastMouse.getY());
            }

            lastMouse = new Point2D(e.getX(), e.getY());
        };

        scrollAction = e -> {
            double factor = Math.pow(1.001, e.getDeltaY());
            view.zoom(factor, e.getX(), e.getY());
        };

        loadFileAction = e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Launcher.getPrimaryStage());

            if (file != null) {
                String fileName = file.getName();
                String fileExt = fileName.substring(fileName.lastIndexOf("."));

                switch (fileExt) {
                    case ".osm":
                        try {
                            this.view = new MapView(OSMMap.fromFile(file), stage);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                        break;
                    case ".zip":
                        try {
                            this.view = new MapView(OSMMap.fromFile(OSMMap.unZip(file.getAbsolutePath(), "src/main/resources/")), stage);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                        break;
                    case ".bin":
                        try {
                            this.view = new MapView(OSMMap.loadBinary(file), stage);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                        break;
                    default:
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Fejlmeddelelse");
                        alert.setHeaderText(null);
                        alert.setContentText("Forkert filtype! \n\n Programmet understøtter OSM, ZIP og BIN.");
                        alert.showAndWait();
                }
            }
        };

        saveFileAction = e -> {
            File file = new FileChooser().showSaveDialog(stage);
            if(file != null) try{
                long time = -System.nanoTime();

                if(file.getName().endsWith(".bin")) {
                    try(var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
                        out.writeObject(model);
                    }

                }else{
                    Alert alert = new Alert(AlertType.ERROR, "Filen skal gemmes i '.bin' format.");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }

                time += System.nanoTime();
                System.out.println(time);

            }catch(IOException exception){
                new Alert(AlertType.ERROR, "Filen kan ikke gemmes.");
            }
        };
        
        roadFinderAction = e -> {
            try {
                Point2D mousePoint = view.getTransform().inverseTransform(e.getX(), e.getY());
                OSMWay result = model.getHighwayTree().nearest(mousePoint.getX(), mousePoint.getY());
                if (result.getRoad() != null) {
                    view.setClosestRoad(result.getRoad());
                }
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
        };
    }
    
    public EventHandler<MouseEvent> getPanAction() {
        return panAction;
    }

    public EventHandler<MouseEvent> getClickAction() {
        return clickAction;
    }

    public EventHandler<ScrollEvent> getScrollAction() {
        return scrollAction;
    }

    public EventHandler<ActionEvent> getLoadFileAction() {
        return loadFileAction;
    }

    public EventHandler<ActionEvent> getSaveFileAction() {
        return saveFileAction;
    }

    public EventHandler<ActionEvent> getClearAction() {
        return clearAction;
    }

    public EventHandler<ActionEvent> getSaveAddressAction() {
        return saveAddressAction;
    }

    public EventHandler<MouseEvent> getToggleAction() {
        return toggleAction;
    }
    
    public EventHandler<MouseEvent> getColorToggleAction() {
        return colorToggleAction;
    }
    
    public EventHandler<ActionEvent> getSearchActionDijkstra() {
        return searchActionDijkstra;
    }

    public EventHandler<MouseEvent> getRoadFinderAction() {
        return roadFinderAction;
    }
}
