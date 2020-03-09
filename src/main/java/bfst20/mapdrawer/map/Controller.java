package bfst20.mapdrawer.map;

import bfst20.mapdrawer.osm.OSMMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Controller {

    private final OSMMap model;
    private final View view;

    // HashSet provides O(1) time for lookups, but not as fast iteration
    // Street names are part of the model, but will only be set and accessed via controller
    private final Set<String> streetNames = new HashSet<>();

    private final EventHandler<ActionEvent> editAction;
    private final EventHandler<ActionEvent> searchAction;

    public Controller(OSMMap model, View view) {
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
    }

    // Can be moved to a separate model if needed
    private static void populateStreets(Set<String> streetNames) throws IOException {
        InputStream in = Controller.class.getClassLoader().getResourceAsStream("streetnames.txt");

        // If the file does not exist, do nothing
        if (in == null) {
            return;
        }

        //ISO-8859-1 gør at man kan læse specielle tegn, f.eks. ä.
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
}
