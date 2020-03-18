package bfst20.mapdrawer.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import bfst20.mapdrawer.Launcher;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMMap.InvalidMapException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;

public class MapController {

    private final OSMMap model;
    private final MapView view;

    // HashSet provides O(1) time for lookups, but not as fast iteration
    // Street names are part of the model, but will only be set and accessed via
    // controller
    private final Set<String> streetNames = new HashSet<>();

    private final EventHandler<ActionEvent> editAction;
    private final EventHandler<ActionEvent> searchAction;

    private final EventHandler<ActionEvent> loadZipAction;

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

        searchAction = e -> {
            if (streetNames.contains(view.getSearchText())) {
                view.showStreetButton(view.getSearchText());
            }

            view.setLastSearch(view.getSearchText());
            view.resetSearchField();
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

        //TODO: make workings
        loadZipAction = e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Launcher.getPrimaryStage());
            try{
                MapView.updateMap(OSMMap.fromFile(OSMMap.unZip(file.getAbsolutePath(), "src/main/resources/")));
            } catch (Exception exc){
                System.out.println(" ");
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
}
