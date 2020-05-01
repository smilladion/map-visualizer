package bfst20.mapdrawer.map;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.dijkstra.*;
import bfst20.mapdrawer.drawing.Line;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.exceptions.NoAddressMatchException;
import bfst20.mapdrawer.exceptions.NoPointChosenException;
import bfst20.mapdrawer.exceptions.NoRouteException;
import bfst20.mapdrawer.exceptions.NoSavedPointsException;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
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
import java.util.LinkedList;

public class MapController {

    private final OSMMap model;
    private MapView view;
    private final Stage stage;
    
    private final EventHandler<ActionEvent> clearAction;

    private final EventHandler<ActionEvent> saveAddressAction;
    private final EventHandler<MouseEvent> toggleAction;
    private final EventHandler<MouseEvent> colorToggleAction;
    private final EventHandler<MouseEvent> nearestToggleAction;

    private final EventHandler<ActionEvent> loadFileAction;
    private final EventHandler<ActionEvent> saveFileAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> clickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private final EventHandler<ActionEvent> searchDijkstraAction;
    private final EventHandler<MouseEvent> roadFinderAction;
    private final EventHandler<ActionEvent> closeRouteMenuAction;
    private final EventHandler<ActionEvent> swapAddressAction;

    private String lastSearchFrom = "";
    private Point2D lastMouse;
    private Dijkstra dijkstra;
    private Vehicle lastVehicle = null;

    LinkedList<DirectedEdge> routeEdges = new LinkedList<>();

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
            view.getRouteDrawables().clear();
            view.setPointOfInterest(new Point());
            view.getRouteMenu().setVisible(false);
            view.paintMap();
        };

        // Saves the current address to my list.
        saveAddressAction = e -> {
            try{
            if (!view.getPointOfInterest().isEmpty()) {
                view.getSavedPoints().add(view.getPointOfInterest());
            } else {
                throw new NoPointChosenException();
            }
        } catch (NoPointChosenException e1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Besked");
                alert.setHeaderText(null);
                alert.setContentText(e1.getMessage());
                alert.showAndWait();
            }
        };

        toggleAction = e -> {
            if (view.getMyPointsToggle().isSelected()) {
                try {
                    view.paintSavedAddresses();
                } catch (NoSavedPointsException e1) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Besked");
                        alert.setHeaderText(null);
                        alert.setContentText(e1.getMessage());
                        alert.showAndWait();
                        view.getMyPointsToggle().setSelected(false);
                }
            } else {
                view.paintMap();
            }
        };
        
        colorToggleAction = e -> {
            view.paintMap();
        };

        searchDijkstraAction = e -> {
            searchAction();
        };


        swapAddressAction = e -> {
            String temp = view.getFromSearchField().getText();
            view.getFromSearchField().setText(view.getToSearchField().getText());
            view.getToSearchField().setText(temp);
            searchAction();
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
                
                if (fileExt.equals(".osm") || fileExt.equals(".zip")) {
                    try {
                        if (OSMMap.fromFile(file) != null) {
                            this.view = new MapView(OSMMap.fromFile(file), stage);
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                } else if (fileExt.equals(".bin")) {
                    try {
                        this.view = new MapView(OSMMap.loadBinary(file), stage);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fejlmeddelelse");
                    alert.setHeaderText(null);
                    alert.setContentText("Forkert filtype! \n\n Programmet understøtter OSM, ZIP og BIN.");
                    alert.showAndWait();
                }
            }
        };

        saveFileAction = e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BIN files (*.bin)", "*.bin");
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showSaveDialog(stage);
            
            if (file != null) try {
                long time = -System.nanoTime();

                if (file.getName().endsWith(".bin")) {
                    try (var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                        out.writeObject(model);
                    }

                } else {
                    Alert alert = new Alert(AlertType.ERROR, "Filen skal gemmes i '.bin' format.");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }

                time += System.nanoTime();
                System.out.println(time);

            } catch (IOException exception) {
                new Alert(AlertType.ERROR, "Filen kan ikke gemmes.");
            }
        };
        
        roadFinderAction = e -> {
            if (view.getNearestToggle().isSelected()) {
                try {
                    Point2D mousePoint = view.getTransform().inverseTransform(e.getX(), e.getY());
                    OSMWay result = model.getHighwayTree().nearest(mousePoint.getX(), mousePoint.getY());
                    if (result.getRoad() != null) {
                        view.setClosestRoad(result.getRoad());
                    }
                } catch (NonInvertibleTransformException ex) {
                    ex.printStackTrace();
                }
            }
        };

        closeRouteMenuAction = e -> {
            view.getRouteMenu().setVisible(false);
        };

        nearestToggleAction = e -> {
            if (view.getNearestToggle().isSelected()) {
                view.getClosestRoad().setVisible(true);
            } else {
                view.getClosestRoad().setVisible(false);
            }
        };
    }
    public void searchAction() {
        view.getSearchedDrawables().clear();
        view.getRouteDrawables().clear();

        String addressTo = view.getToSearchField().getText();
        String addressFrom = view.getFromSearchField().getText();

        OSMNode nodeTo = null;
        OSMNode nodeFrom = null;

        if (!addressTo.isEmpty() || !addressFrom.isEmpty()) {
            for (OSMNode node : model.getAddressNodes()) {
                if (node.getAddress().equals(addressTo)) {
                    nodeTo = node;
                }
                if (node.getAddress().equals(addressFrom)) {
                    nodeFrom = node;
                }
            }
        }

        try {
            view.paintPoints(nodeTo, nodeFrom);
        } catch (NoAddressMatchException ex) {
            if ((!addressTo.isEmpty() && !addressFrom.isEmpty()) ||
                    ((nodeFrom == null && nodeTo == null) &&
                            (!addressTo.isEmpty() || !addressFrom.isEmpty()))) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Ingen adresse fundet");
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }

        if (nodeFrom != null && nodeTo != null) {
            if (!routeEdges.isEmpty()) {
                routeEdges.clear();
            }

            if (view.getHelicopter().isSelected()) {
                DirectedEdge route = new DirectedEdge(nodeFrom.distance(nodeTo), nodeFrom.getLon(), nodeFrom.getLat(), nodeTo.getLon(), nodeTo.getLat());
                routeEdges.add(route);
                view.paintRoute(routeEdges);
                view.openRouteDescription();
                return;
            }

            Vehicle vehicle;

            if (view.getCar().isSelected()) {
                vehicle = new Car();
            } else if (view.getBike().isSelected()) {
                vehicle = new Bike();
            } else {
                vehicle = new Walk();
            }

            OSMWay nearestTo = model.getHighwayTree().nearest(nodeTo.getLon(), nodeTo.getLat());
            OSMWay nearestFrom = model.getHighwayTree().nearest(nodeFrom.getLon(), nodeFrom.getLat());

            Point2D pointTo = new Point2D(nodeTo.getLon(), nodeTo.getLat());
            OSMNode nearestToNode = model.getHighwayTree().nodeDistance(pointTo, nearestTo);

            Point2D pointFrom = new Point2D(nodeFrom.getLon(), nodeFrom.getLat());
            OSMNode nearestFromNode = model.getHighwayTree().nodeDistance(pointFrom, nearestFrom);

            try {
                if (!lastSearchFrom.equals(view.getFromSearchField().getText())) {
                    dijkstra = new Dijkstra(model.getRouteGraph(), nearestFromNode.getNumberForGraph(), vehicle);

                } else {
                    if (lastVehicle == null || (!lastVehicle.isSameVehicleAs(vehicle))) {
                        dijkstra = new Dijkstra(model.getRouteGraph(), nearestFromNode.getNumberForGraph(), vehicle);
                    }
                }

                routeEdges = dijkstra.pathTo(nearestToNode.getNumberForGraph(), vehicle);

            } catch (NoRouteException ex) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Ingen rute fundet");
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }

            view.paintRoute(routeEdges);
            view.openRouteDescription();
            lastSearchFrom = addressFrom;
            lastVehicle = vehicle;
        }
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
    
    public EventHandler<ActionEvent> getSearchDijkstraAction() {
        return searchDijkstraAction;
    }

    public EventHandler<MouseEvent> getRoadFinderAction() {
        return roadFinderAction;
    }

    public LinkedList<DirectedEdge> getRouteEdges() {
        return routeEdges;
    }

    public EventHandler<ActionEvent> getCloseRouteMenuAction() {
        return closeRouteMenuAction;
    }

    public EventHandler<MouseEvent> getNearestToggleAction() {
        return nearestToggleAction;
    }

    public EventHandler<ActionEvent> getSwapAddressAction() { return swapAddressAction; }

}
