package bfst20.mapdrawer.map;

import bfst20.mapdrawer.osm.OSMMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class View {

    private final OSMMap model;
    private final StackPane root;
    private final Controller controller;

    private final TextField searchField = new TextField();
    private final Label userSearchLabel = new Label();
    private final Button streetButton = new Button();

    public View(OSMMap model, Stage window) {
        window.setTitle("Google Map'nt");

        this.model = model;
        root = new StackPane(new Canvas(1280, 720));
        controller = new Controller(model, this);

        searchField.setPromptText("Street name");

        Button editButton = new Button("Edit");
        editButton.setOnAction(controller.getEditAction());

        searchField.setOnAction(controller.getSearchAction());

        HBox searchLabels = new HBox(new Label("Last search: "), userSearchLabel);

        searchLabels.setAlignment(Pos.BASELINE_CENTER);

        HBox searchRow = new HBox(searchField, searchLabels, editButton, streetButton);

        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.BASELINE_CENTER);
        searchRow.setPadding(new Insets(15.0));

        root.getChildren().add(searchRow);

        window.setScene(new Scene(root));
        window.show();

        // Remove focus from search field on startup
        resetSearchField();

        streetButton.setVisible(false);
    }

    String getSearchText() {
        return searchField.getText();
    }

    void setSearchText(String text) {
        searchField.setText(text);
    }

    String getLastSearch() {
        return userSearchLabel.getText();
    }

    void setLastSearch(String text) {
        userSearchLabel.setText(text);
    }

    void showStreetButton(String text) {
        streetButton.setVisible(true);
        streetButton.setText(text);
    }

    void resetSearchField() {
        searchField.clear();
        root.requestFocus();
    }
}
