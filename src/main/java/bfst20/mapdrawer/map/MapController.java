package bfst20.mapdrawer.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;

public class MapController {

    private final OSMMap model;
    private final MapView view;

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

    private final EventHandler<ActionEvent> loadZipAction;
    private final EventHandler<ActionEvent> loadOSMAction;

    private final EventHandler<MouseEvent> panAction;
    private final EventHandler<MouseEvent> panClickAction;
    private final EventHandler<ScrollEvent> scrollAction;

    private Point2D lastMouse;

    MapController(OSMMap model, MapView view) {
        this.model = model;
        this.view = view;

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
            view.paintOnMap(null, null);

        };

        // Saves the address from the "til..." search field to my list.
        savePointOfInterestTo = e-> {
            String s = view.getToSearchField().getText().toLowerCase();
            savePoint(s);
        };

        // saves address from the "fra..." searchfield.
        savePointOfInterestFrom = e-> {
            String s = view.getFromSearchField().getText().toLowerCase();
            savePoint(s);
        };

        // the toggle button.
        toggleAction = e-> {
            if (view.getMyPointsToggle().isSelected()) {
                if (view.getMyPoints().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("You have no saved addresses");
                    alert.showAndWait();
                } else {
                    for (Drawable drawable : view.getMyPoints()) {
                        view.getMyPointsTemp().add(drawable);
                    }
                    view.paintSavedAddresses();
                }
            } else {
                view.getMyPointsTemp().clear();
                view.paintOnMap(null, null);
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
                view.paintOnMap(address, address1);
            } else if (address1 == null) {
                view.paintOnMap(address, null);

            }
            view.setLastSearch(view.getToSearchText());
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

        loadZipAction = e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Launcher.getPrimaryStage());
            try {
                MapView.updateMap(OSMMap.fromFile(OSMMap.unZip(file.getAbsolutePath(), "src/main/resources/")));
            } catch (Exception exc){
                exc.printStackTrace();
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

        loadOSMAction = e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Launcher.getPrimaryStage());
            try{
                MapView.updateMap(OSMMap.fromFile(file));
            } catch (Exception exc){
                exc.printStackTrace();
            }
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
            view.getMyPoints().add(new Point(model.getIdToNodeMap().get(id)));
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

    public EventHandler<ActionEvent> getLoadZipAction() {
        return loadZipAction;
    }

    public EventHandler<ActionEvent> getLoadOSMAction() {
        return loadOSMAction;
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
}
