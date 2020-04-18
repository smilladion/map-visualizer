package bfst20.mapdrawer.map;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.Rutevejledning.Dijkstra;
import bfst20.mapdrawer.Rutevejledning.DirectedEdge;
import bfst20.mapdrawer.drawing.Drawable;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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

    // HashSet provides O(1) time for lookups, but not as fast iteration
    // Street names are part of the model, but will only be set and accessed via controller
    private final Set<String> streetNames = new HashSet<>();

    private final EventHandler<ActionEvent> editAction;
    private final EventHandler<ActionEvent> searchAction;
    private final EventHandler<ActionEvent> clearAction;
    private final EventHandler<MouseEvent> clickOnMapAction;

    private final EventHandler<ActionEvent> savePointOfInterestTo;
    private final EventHandler<ActionEvent> savePointOfInterestFrom;
    private final EventHandler<MouseEvent> toggleAction;

    private final EventHandler<ActionEvent> loadFileAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> panClickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private final EventHandler<ActionEvent> searchActionDijkstraTest;

    private Point2D lastMouse;

    private Dijkstra dijkstra;

    MapController(OSMMap model, MapView view, Stage stage) {
        this.model = model;
        this.view = view;
        this.stage = stage;

        try {
            populateStreets(streetNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        editAction = e -> view.setSearchText(view.getLastSearch());

        clearAction = e -> {
            view.getToSearchField().clear();
            view.getFromSearchField().clear();
            view.getToSearchField().setPromptText("Til...");
            view.getFromSearchField().setPromptText("Fra...");
            view.getSearchedDrawables().clear();
            view.paintPoints(null, null);

        };

        // Saves the address from the "til..." search field to my list.
        savePointOfInterestTo = e -> {
            String s = view.getToSearchField().getText().toLowerCase();
            savePoint(s);
        };

        // saves address from the "fra..." searchfield.
        savePointOfInterestFrom = e -> {
            String s = view.getFromSearchField().getText().toLowerCase();
            savePoint(s);
        };

        toggleAction = e -> {
            if (view.getMyPointsToggle().isSelected()) {
                if (view.getMyPoints().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Du har ingen gemte adresser");
                    alert.showAndWait();
                } else {
                    for (Drawable drawable : view.getMyPoints()) {
                        view.getMyPointsTemp().add(drawable);
                    }
                    view.paintSavedAddresses();
                }
            } else {
                view.getMyPointsTemp().clear();
                view.paintPoints(null, null);
            }
        };

        searchAction = e -> {

            String address = view.getToSearchText().toLowerCase();
            String address1 = view.getFromSearchField().getText().toLowerCase();

            if (address1.trim().equals("")) {
                address1 = null;
            }

            view.getFromSearchField().setVisible(true);
            view.getSaveFromSearch().setVisible(true);

            if (address1 != null) {
                view.paintPoints(address, address1);
            } else if (address1 == null) {
                view.paintPoints(address, null);

            }
            view.setLastSearch(view.getToSearchText());
        };

        searchActionDijkstraTest = e -> {

            String from = view.getFromSearchField().getText();
            String to = view.getToSearchField().getText();

            int from1 = Integer.parseInt(from);
            int to2 = Integer.parseInt(to);

            dijkstra = new Dijkstra(model.getRouteGraph(), from1);

            List<OSMNode> list = new ArrayList<>();

            Stack<DirectedEdge> route = dijkstra.pathTo(to2);

            while (!route.isEmpty()) {
                OSMNode x = model.getIntToNode().get(route.peek().to());
                OSMNode y = model.getIntToNode().get(route.peek().from());
                list.add(x);
                list.add(y);
                System.out.println(route.pop());
            }
            Type type = Type.SEARCHRESULT;

            OSMWay searchedWay = new OSMWay(1, list, type.getColor(), type);
            view.paintRoute(searchedWay);
        };

        // Resets the value of lastMouse before the next pan/drag occurs
        panClickAction = e -> {
            if (!e.isPrimaryButtonDown()) {
                lastMouse = null;
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

        //TODO - doesn't work. Should probably be something else other than just clicking - maybe a double click?
        clickOnMapAction = e -> {
            /*double x1 = e.getX();
            double y1 = e.getY();

            try {
            Image pointImage = new Image(this.getClass().getClassLoader().getResourceAsStream("REDlogotrans.png"));
            view.getContext().drawImage(pointImage, x1+(0.01 / 2), y1, -0.01, -0.01);
            } catch (NullPointerException ex) {
                System.err.println("Pin point image not found!");
            }*/
        };
    }

    public void savePoint(String s) {
        if (s.trim().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("No address was found - please write an address to add it to your saved points");
            alert.showAndWait();
        } else {
            long id = model.getAddressToId().get(s);
            view.getMyPoints().add(new Point(model.getIdToNodeMap().get(id), view.getTransform(), view.getInitialZoom()));
        }
    }

    // Can be moved to a separate model if needed (right now, it's only used in the controller)
    private static void populateStreets(Set<String> streetNames) throws IOException {
        InputStream in = MapController.class.getClassLoader().getResourceAsStream("streetnames.txt");

        // If the file does not exist, do nothing
        if (in == null) {
            return;
        }

        //ISO-8859-1 makes sure you can read special characters, ex. ä
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));

        String streetName;
        while ((streetName = br.readLine()) != null) {
            streetNames.add(streetName);
        }
    }

    public EventHandler<ActionEvent> getEditAction() {
        return editAction;
    }

    public EventHandler<ActionEvent> getSearchAction() {
        return searchAction;
    }

    public EventHandler<MouseEvent> getPanAction() {
        return panAction;
    }

    public EventHandler<MouseEvent> getPanClickAction() {
        return panClickAction;
    }

    public EventHandler<ScrollEvent> getScrollAction() {
        return scrollAction;
    }

    public EventHandler<ActionEvent> getLoadFileAction() {
        return loadFileAction;
    }

    public EventHandler<MouseEvent> clickOnMapAction() {
        return clickOnMapAction;
    }

    public EventHandler<ActionEvent> getClearAction() {
        return clearAction;
    }

    public EventHandler<ActionEvent> getSavePointOfInterestTo() {
        return savePointOfInterestTo;
    }

    public EventHandler<ActionEvent> getSavePointOfInterestFrom() {
        return savePointOfInterestFrom;
    }

    public EventHandler<MouseEvent> getToggleAction() {
        return toggleAction;
    }

    public EventHandler<ActionEvent> getSearchActionDijkstraTest() {
        return searchActionDijkstraTest;
    }
}
