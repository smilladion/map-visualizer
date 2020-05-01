package bfst20.mapdrawer.map;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.dijkstra.*;
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
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.LinkedList;

/**
 * This class controls the user interface - it runs code when the user
 * interacts with the program. Specifically, it contains all of the
 * different action events.
 */
public class MapController {

    private final EventHandler<ActionEvent> clearAction;

    private final EventHandler<ActionEvent> saveAddressAction;
    
    private final EventHandler<MouseEvent> savedToggleAction;
    private final EventHandler<MouseEvent> colorToggleAction;

    private final EventHandler<ActionEvent> loadFileAction;
    private final EventHandler<ActionEvent> saveFileAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> clickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private final EventHandler<ActionEvent> searchDijkstraAction;
    private final EventHandler<MouseEvent> roadFinderAction;
    private final EventHandler<ActionEvent> closeRouteMenuAction;
    
    LinkedList<DirectedEdge> routeEdges = new LinkedList<>();
    
    private String lastSearchFrom = "";
    private Dijkstra dijkstra;
    private Vehicle lastVehicle = null;
    
    private MapView view;
    
    private Point2D lastMouse;

    MapController(OSMMap model, MapView view, Stage stage) {
        this.view = view;

        // Clears the canvas of routes and points.
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

        // Saves the current point to my list.
        saveAddressAction = e -> {
            try {
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
                Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                alert.showAndWait();
            }
        };

        // Displays saved points.
        savedToggleAction = e -> {
            if (view.getMyPointsToggle().isSelected()) {
                try {
                    view.paintSavedPoints();
                } catch (NoSavedPointsException e1) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Besked");
                    alert.setHeaderText(null);
                    alert.setContentText(e1.getMessage());
                    Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                    s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                    alert.showAndWait();
                    view.getMyPointsToggle().setSelected(false);
                }
            } else {
                view.paintMap();
            }
        };

        // Changes color scheme to black and white.
        colorToggleAction = e -> {
            view.paintMap();
        };

        // Displays the shortest/quickest route between two addresses.
        searchDijkstraAction = e -> {
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
                    Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                    s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                    alert.showAndWait();
                    return;
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

                OSMWay nearestTo = model.getHighwayTree().nearest(nodeTo.getLon(), nodeTo.getLat(), true, view.getTransform());
                OSMWay nearestFrom = model.getHighwayTree().nearest(nodeFrom.getLon(), nodeFrom.getLat(), true, view.getTransform());

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
                    view.getRouteMenu().setVisible(false);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Ingen rute fundet");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                    s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                    alert.showAndWait();
                    return;
                }

                view.paintRoute(routeEdges);
                view.openRouteDescription();
                lastSearchFrom = addressFrom;
                lastVehicle = vehicle;
            }
        };
        
        // Controls clicks on the map.
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

        // Pans the map in the dragged direction.
        panAction = e -> {
            if (lastMouse != null) {
                view.pan(e.getX() - lastMouse.getX(), e.getY() - lastMouse.getY());
            }

            lastMouse = new Point2D(e.getX(), e.getY());
        };

        // Zooms in and out on the map.
        scrollAction = e -> {
            double factor = Math.pow(1.001, e.getDeltaY());
            view.zoom(factor, e.getX(), e.getY());
        };

        // Loads whichever file the user chooses.
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
                    Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                    s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                    alert.showAndWait();
                }
            }
        };

        // Saves a binary file of the currently opened map.
        saveFileAction = e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BIN files (*.bin)", "*.bin");
            chooser.getExtensionFilters().add(extFilter);
            File file = chooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    if (file.getName().endsWith(".bin")) {
                        try (var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                            out.writeObject(model);
                        }
                    } else {
                        Alert alert = new Alert(AlertType.ERROR, "Filen skal gemmes i '.bin' format.");
                        alert.setTitle("Fejlmeddelelse");
                        alert.setHeaderText(null);
                        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                        s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                        alert.showAndWait();
                        return;
                    }
                    
                    System.out.println("Succes - binær fil er blevet gemt.");

                } catch (IOException exception) {
                    Alert alert = new Alert(AlertType.ERROR, "Filen kan ikke gemmes.");
                    alert.setTitle("Fejlmeddelelse");
                    alert.setHeaderText(null);
                    Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
                    s.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));
                    alert.showAndWait();
                }
            }
        };

        // Displays the nearest visible road to the mouse.
        roadFinderAction = e -> {
            try {
                Point2D mousePoint = view.getTransform().inverseTransform(e.getX(), e.getY());
                OSMWay result = model.getHighwayTree().nearest(mousePoint.getX(), mousePoint.getY(), false, view.getTransform());
                if (result.getRoad() != null) {
                    view.getClosestRoad().setVisible(true);
                    view.setClosestRoad(result.getRoad());
                } else {
                    view.getClosestRoad().setVisible(false);
                }
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
        };

        // Closes the route menu.
        closeRouteMenuAction = e -> {
            view.getRouteMenu().setVisible(false);
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

    public EventHandler<MouseEvent> getSavedToggleAction() {
        return savedToggleAction;
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
}
