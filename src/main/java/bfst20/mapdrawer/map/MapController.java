package bfst20.mapdrawer.map;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.kdtree.NodeProvider;
import bfst20.mapdrawer.osm.OSMMap;
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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MapController {

    private final OSMMap model;
    private MapView view;
    private final Stage stage;

    // HashSet provides O(1) time for lookups, but not as fast iteration
    // Street names are part of the model, but will only be set and accessed via controller
    private final Set<String> streetNames = new HashSet<>();

    private final EventHandler<ActionEvent> searchAction;
    private final EventHandler<ActionEvent> clearAction;

    private final EventHandler<ActionEvent> saveAddressAction;
    private final EventHandler<MouseEvent> toggleAction;

    private final EventHandler<ActionEvent> loadFileAction;
    private final EventHandler<ActionEvent> saveFileAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> clickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private final EventHandler<MouseEvent> roadFinderAction;

    private Point2D lastMouse;

    MapController(OSMMap model, MapView view, Stage stage) {
        this.model = model;
        this.view = view;
        this.stage = stage;

        try {
            populateStreets(streetNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearAction = e -> {
            view.getToSearchField().clear();
            view.getFromSearchField().clear();
            view.getToSearchField().setPromptText("Til...");
            view.getFromSearchField().setPromptText("Fra...");
            view.getSearchedDrawables().clear();
            view.setPointOfInterest(new Point());
            view.paintPoints(null, null);
        };

        // Saves the current address to my list.
        saveAddressAction = e -> {
            String to = view.getToSearchField().getText().toLowerCase();
            String from = view.getFromSearchField().getText().toLowerCase();

            if (to.isEmpty() && from.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setTitle("Besked");
                alert.setContentText("Du skal have valgt mindst én adresse for at gemme den!");
                alert.showAndWait();
            }
            if (!to.isEmpty()) {
                view.savePoint(to);
            }
            if (!from.isEmpty()) {
                view.savePoint(from);
            }
        };

        // the toggle button.
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

        searchAction = e -> {
            view.getSearchedDrawables().clear();
            view.paintPoints(null, null);

            String addressTo = view.getToSearchField().getText().toLowerCase();
            String addressFrom = view.getFromSearchField().getText().toLowerCase();

            if (addressFrom.trim().equals("")) {
                addressFrom = null;
            }

            view.paintPoints(addressTo, addressFrom);
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
                    new Alert(AlertType.ERROR, "Filen skal gemmes i '.bin' format.");
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
                OSMWay result = model.getKdTree().nearest(mousePoint.getX(), mousePoint.getY());
                if (result.getRoad() != null) {
                    view.setClosestRoad(result.getRoad());
                }
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }
        };
    }

    // Can be moved to a separate model if needed (right now, it's only used in the controller)
    private static void populateStreets(Set<String> streetNames) throws IOException {
        InputStream in = MapController.class.getClassLoader().getResourceAsStream("streetnames.txt");

        // If the file does not exist, do nothing
        if (in == null) {
            return;
        }

        // UTF-8 makes sure you can read special characters, ex. ä
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String streetName;
        while ((streetName = br.readLine()) != null) {
            streetNames.add(streetName);
        }
    }

    public EventHandler<ActionEvent> getSearchAction() {
        return searchAction;
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

    public EventHandler<MouseEvent> getRoadFinderAction() {
        return roadFinderAction;
    }
}
