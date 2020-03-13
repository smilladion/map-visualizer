package bfst20.mapdrawer.map;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.LinePath;
import bfst20.mapdrawer.drawing.Polygon;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MapView {

    private final OSMMap model;

    private final Canvas canvas;
    private final GraphicsContext context;
    private final Affine transform = new Affine();

    private final StackPane root;
    private final MapController controller;

    private final TextField searchField = new TextField();
    private final Label userSearchLabel = new Label();
    private final Button streetButton = new Button();

    private final List<Drawable> drawables = new ArrayList<>();

    public MapView(OSMMap model, Stage window) {
        window.setTitle("Google Map'nt");

        this.model = model;

        canvas = new Canvas(1280, 720);
        context = canvas.getGraphicsContext2D();

        root = new StackPane(canvas);
        controller = new MapController(model, this);

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

        populateDrawables(model);
        resetPanZoom();

        // Test zoom and pan, remove later
        zoom(0.6, 0.0, 0.0);
        pan(400.0, 0.0);

        paintMap();
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

    private void populateDrawables(OSMMap model) {
        drawables.clear();

        for (OSMWay way : model.getWays()) {
            // If a way has no nodes, do not draw
            if (way.getNodes().isEmpty()) {
                continue;
            }

            drawables.add(new LinePath(way));
        }

        for (OSMRelation relation : model.getRelations()) {
            // If a relation has no ways, do not draw
            if (relation.getWays().isEmpty()) {
                continue;
            }

            drawables.add(new Polygon(relation, PathColor.BUILDING.getColor()));
        }
    }

    private void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        paintMap();
    }

    private void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        paintMap();
    }

    private void resetPanZoom() {
        pan(-model.getMinLon(), -model.getMinLat());
        zoom(canvas.getWidth() / (model.getMaxLat() - model.getMinLat()), 0, 0);
        paintMap();
    }

    private Point2D convertMouseToMap(double x, double y) {
        try {
            return transform.inverseTransform(x, y);
        } catch (Exception ignored) {
            return Point2D.ZERO;
        }
    }

    private void paintMap() {
        // Using identity matrix (no transform)
        context.setTransform(new Affine());

        // Paint background light blue
        context.setFill(Color.LIGHTBLUE);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Pan and scale all below
        context.setTransform(transform);

        // Paint using light yellow
        context.setFill(Color.LIGHTYELLOW);

        // Line width proportionate to pan/zoom
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
        context.setFillRule(FillRule.EVEN_ODD);

        // Draw the map's drawables
        for (Drawable drawable : drawables) {
            drawable.draw(context);
        }
    }
}
