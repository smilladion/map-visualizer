package bfst20.mapdrawer.map;

import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

public class MapView {

    private static OSMMap model;

    private static Canvas canvas;
    private static GraphicsContext context;
    private final static Affine transform = new Affine();

    private final StackPane rootPane;
    private final MapController controller;

    private final MenuBar menuBar = new MenuBar();
    private final Menu loadMenu = new Menu("Load");
    private final TextField searchField = new TextField();
    private final Label userSearchLabel = new Label();
    private final Button streetButton = new Button();

    private static List<Drawable> drawables = new ArrayList<>();

    public MapView(OSMMap model, Stage window) {
        window.setTitle("Google Map'nt");

        this.model = model;

        canvas = new Canvas(1280, 720);
        context = canvas.getGraphicsContext2D();

        rootPane = new StackPane(canvas); // Makes sure UI elements can go on top of the map itself

        controller = new MapController(model, this);

        VBox menuBox = new VBox(menuBar);
        menuBox.setPickOnBounds(false);
        menuBar.getMenus().add(loadMenu);
        MenuItem loadZip = new MenuItem("Load .zip-file");
        loadZip.setOnAction(controller.getLoadZipAction());
        MenuItem somethingElse = new MenuItem("Something else");
        loadMenu.getItems().addAll(loadZip, somethingElse);

        rootPane.getChildren().add(menuBox);

        searchField.setPromptText("Street name");

        Button editButton = new Button("Edit");
        editButton.setOnAction(controller.getEditAction());

        searchField.setOnAction(controller.getSearchAction());

        canvas.setOnMouseClicked(controller.getPanClickAction());
        canvas.setOnMouseDragged(controller.getPanAction());
        canvas.setOnScroll(controller.getScrollAction());

        HBox searchLabels = new HBox(new Label("Last search: "), userSearchLabel);
        searchLabels.setAlignment(Pos.BASELINE_CENTER);
        searchLabels.setPickOnBounds(false);

        HBox searchRow = new HBox(searchField, searchLabels, editButton, streetButton);
        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.TOP_CENTER);
        searchRow.setPadding(new Insets(35.0));
        searchRow.setPickOnBounds(false); // Transparent areas of the HBox are ignored - zoom/pan now works in those
                                          // areas

        rootPane.getChildren().add(searchRow);

        Scene scene = new Scene(rootPane);

        window.setScene(scene);
        window.show();

        // Code below makes the canvas resizable when the window changes (responsive
        // design)
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        canvas.widthProperty().addListener((a, b, c) -> {
            paintMap();
        });
        canvas.heightProperty().addListener((a, b, c) -> {
            paintMap();
        });

        // Remove focus from search field on startup
        resetSearchField();
        streetButton.setVisible(false);

        populateDrawables(model);
        resetPanZoom();

        paintMap();

        canvas.requestFocus();
    }

    public static void updateMap(OSMMap map) {
        MapView.model = map;
        MapView.populateDrawables(model);
        MapView.paintMap();
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
        rootPane.requestFocus();
    }

    public static void populateDrawables(OSMMap model) {
        drawables.clear();

        // TODO: Need to find a system for coloring objects, or we are gonna end up with huge else-if statements here (and also several places in OSMMap)
        for (OSMWay way : model.getWays()) {
            if (way.getNodes().isEmpty()) {
                // If a way has no nodes, do not draw
                continue;
            }  else if (way.getColor() == PathColor.BUILDING.getColor() || way.getColor() == PathColor.FOREST.getColor()) {
                // If a way has the color specified, make a polygon
                drawables.add(new Polygon(way, way.getColor()));
            } else {
                // If it has no color or otherwise shouldn't be filled with color, draw a line
                drawables.add(new LinePath(way));
            }
        }

        for (OSMRelation relation : model.getRelations()) {
            if (relation.getWays().isEmpty()) {
                // If a relation has no ways, do not draw
                continue;
            } else if (relation.getColor() == PathColor.NONE.getColor()) {
                // If a relation has no color, do not draw
                continue;
            } else {
                drawables.add(new Polygon(relation, relation.getColor()));
            }
        }
    }

    static void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        paintMap();
    }

    static void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        paintMap();
    }

    private static void resetPanZoom() {
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

    public static void paintMap() {
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

        // Draw islands
        for (Drawable island : model.getIslands()) {
            island.draw(context);
            context.fill();
        }

        // Draw the map's drawables
        for (Drawable drawable : drawables) {
            drawable.draw(context);
        }
    }
}
