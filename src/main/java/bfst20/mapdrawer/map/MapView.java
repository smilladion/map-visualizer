package bfst20.mapdrawer.map;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Line;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.kdtree.NodeProvider;
import bfst20.mapdrawer.kdtree.Rectangle;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapView {

    private final Affine transform = new Affine();

    private final CheckMenuItem showKdTree = new CheckMenuItem("Vis KD-Træ");

    private final MapController controller;
    private OSMMap model;

    private final Canvas canvas;
    private final StackPane rootPane;
    private final GraphicsContext context;

    private final List<NodeProvider> drawables = new ArrayList<>(); // All map elements
    private final List<Drawable> drawableExtras = new ArrayList<>(); // Extra UI elements
    private final List<Drawable> searchedDrawables = new ArrayList<>(); // User search results currently visible

    private final List<Drawable> savedPoints = new ArrayList<>(); // Search results that have been saved

    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("Fil");
    private final Menu optionsMenu = new Menu("Indstillinger");

    private final TextField toSearchField = new TextField();
    private final TextField fromSearchField = new TextField();
    private final ToggleSwitch myPointsToggle; //from the ControlsFX library

    private final Label zoomDisplay = new Label();

    public MapView(OSMMap model, Stage window) {

        window.setTitle("Google Map'nt");

        this.model = model;

        canvas = new Canvas(1280, 720);
        context = canvas.getGraphicsContext2D();

        rootPane = new StackPane(canvas); // Makes sure UI elements can go on top of the map itself

        controller = new MapController(model, this, window);

        VBox menuBox = new VBox(menuBar);
        menuBox.setPickOnBounds(false);
        menuBar.getMenus().add(fileMenu);
        MenuItem loadFile = new MenuItem("Åbn...      (.zip, .osm, .bin)");
        loadFile.setOnAction(controller.getLoadFileAction());
        fileMenu.getItems().add(loadFile);

        optionsMenu.getItems().add(showKdTree);
        menuBar.getMenus().add(optionsMenu);

        showKdTree.addEventHandler(EventType.ROOT, event -> paintMap());

        rootPane.getChildren().add(menuBox);

        toSearchField.setPromptText("Til...");
        fromSearchField.setPromptText("Fra...");

        AutoCompletionBinding<String> autoTo = TextFields.bindAutoCompletion(toSearchField, model.getAddressList());
        AutoCompletionBinding<String> autoFrom = TextFields.bindAutoCompletion(fromSearchField, model.getAddressList());

        autoTo.setVisibleRowCount(5);
        autoTo.setMinWidth(300);

        autoFrom.setVisibleRowCount(5);
        autoFrom.setMinWidth(300);

        Button clearButton = new Button("Nulstil");
        clearButton.setOnAction(controller.getClearAction());

        Button saveToSearch = new Button("Gem adresse");

        myPointsToggle = new ToggleSwitch(); // From the ControlsFX library
        myPointsToggle.setText("Vis gemte adresser");

        HBox zoomLevel = new HBox(zoomDisplay);
        zoomLevel.setAlignment(Pos.BOTTOM_RIGHT);
        zoomLevel.setPickOnBounds(false);
        rootPane.getChildren().add(zoomLevel);

        myPointsToggle.setOnMouseClicked(controller.getToggleAction());
        toSearchField.setOnAction(controller.getSearchAction());
        fromSearchField.setOnAction(controller.getSearchAction());
        saveToSearch.setOnAction(controller.getSaveAddressAction());

        canvas.setOnMouseClicked(controller.getPanClickAction());
        canvas.setOnMousePressed(controller.clickOnMapAction());
        canvas.setOnMouseDragged(controller.getPanAction());
        canvas.setOnScroll(controller.getScrollAction());

        HBox searchRow = new HBox(fromSearchField, toSearchField, saveToSearch, clearButton, myPointsToggle);
        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.TOP_CENTER);
        searchRow.setPadding(new Insets(35.0));
        searchRow.setPickOnBounds(false); // Transparent areas of the HBox are ignored - zoom/pan now works in those areas

        rootPane.getChildren().add(searchRow);

        Scene scene = new Scene(rootPane);

        window.setScene(scene);
        window.show();

        // Code below makes the canvas resizable when the window changes (responsive design)
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        canvas.widthProperty().addListener((a, b, c) -> {
            paintMap();
        });
        canvas.heightProperty().addListener((a, b, c) -> {
            paintMap();
        });

        resetSearchField();

        resetPanZoom();

        paintMap();

        // Remove focus from search field on startup
        canvas.requestFocus();
    }

    public void populateDrawables(OSMMap model) {
        drawables.clear();
        drawableExtras.clear();

        Point2D topLeft = null;
        Point2D bottomRight = null;

        try {
            if (showKdTree.isSelected()) { // If the "Show KD Tree" button has been pressed
                float size = 325.0f; // Use this offset to create a smaller range for searching (making the culling visible on screen)

                // Gives coords for the current zoom/pan level
                topLeft = transform.inverseTransform(canvas.getWidth() / 2 - size / 2, canvas.getHeight() / 2 - size / 2);
                bottomRight = transform.inverseTransform(canvas.getWidth() / 2 + size / 2, canvas.getHeight() / 2 + size / 2);
            } else {
                topLeft = transform.inverseTransform(0.0f, 0.0f);
                bottomRight = transform.inverseTransform(canvas.getWidth(), canvas.getHeight());
            }
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        model.getKdTree().search(
                drawables,
                model.getKdTree().getRoot(),
                new Rectangle(
                        topLeft.getX(),
                        topLeft.getY(),
                        bottomRight.getX(),
                        bottomRight.getY()
                )
        );

        // Sort the NodeProviders in drawables list based on types
        // to make sure we draw the elements in the right order
        Collections.sort(drawables);

        // Draws borders for where the culling happens
        drawableExtras.add(new Line(topLeft.getX(), topLeft.getY(), topLeft.getX(), bottomRight.getY()));
        drawableExtras.add(new Line(bottomRight.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY()));
        drawableExtras.add(new Line(topLeft.getX(), topLeft.getY(), bottomRight.getX(), topLeft.getY()));
        drawableExtras.add(new Line(topLeft.getX(), bottomRight.getY(), bottomRight.getX(), bottomRight.getY()));
    }

    void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        paintMap();
    }

    void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        zoomDisplay.setText("Zoom niveau: " + transform.getMxx());
        paintMap();
    }

    private void resetPanZoom() {
        pan(-model.getMinLon(), -model.getMinLat());
        zoom(canvas.getWidth() / (model.getMaxLat() - model.getMinLat()), 0, 0);
    }

    // Updates and repaints the whole map
    public void paintMap() {
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

        populateDrawables(model);

        // Draw islands
        for (Drawable island : model.getIslands()) {
            island.draw(context);
            context.fill();
        }

        // Draw OSMWays and relations
        for (NodeProvider provider : drawables) {
            if (provider.getDrawable() == null) continue;

            if(provider.getType().shouldPaint(transform.getMxx())){
                int lineWidth = provider.getType().getLineWidth();
                // Change linewidth for drawable objects where this is specified
                if (lineWidth > 0) context.setLineWidth(lineWidth / Math.sqrt(Math.abs(transform.determinant())));
                provider.getDrawable().draw(context);
                // Change linewidth back to normal to ensure next element is drawn properly
                context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
            }
        }

        // Draw search results
        for (Drawable drawable : searchedDrawables) {
            drawable.draw(context);
        }

        // Draws saved searches so they are updated on pan/zoom
        if (myPointsToggle.isSelected()) {
            for (Drawable drawable : savedPoints) {
                drawable.draw(context);
            }
        }

        // Draw extra UI elements
        for (Drawable drawable : drawableExtras) {
            drawable.draw(context);
        }
    }

    public void paintPoints(String addressTo, String addressFrom) {
        context.setTransform(transform);
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));

        if (addressTo == null && addressFrom == null) {
            paintMap();
        } else if ((addressFrom == null)) {
            for (Map.Entry<String, Long> entry : model.getAddressToId().entrySet()) {
                if (entry.getKey().contains(addressTo)) {
                    searchedDrawables.add(new Point(model.getIdToNodeMap().get(entry.getValue()), transform));
                }
            }

            for (Drawable drawable : searchedDrawables) {
                drawable.draw(context);
            }
        } else if (addressTo != null) {
            for (Map.Entry<String, Long> entry : model.getAddressToId().entrySet()) {
                if (entry.getKey().equals(addressTo) || entry.getKey().equals(addressFrom)) {
                    searchedDrawables.add(new Point(model.getIdToNodeMap().get(entry.getValue()), transform));
                }
            }

            for (Drawable drawable : searchedDrawables) {
                drawable.draw(context);
            }
        }
    }

    public void paintSavedAddresses() {
        for (Drawable drawable : savedPoints) {
            drawable.draw(context);
        }
    }

    public void savePoint(String s) {
        long id = model.getAddressToId().get(s);
        getSavedPoints().add(new Point(model.getIdToNodeMap().get(id), transform));
    }

    public void resetSearchField() {
        toSearchField.clear();
        fromSearchField.clear();
        rootPane.requestFocus();
    }

    public TextField getToSearchField() {
        return toSearchField;
    }

    public TextField getFromSearchField() {
        return fromSearchField;
    }

    public List<Drawable> getSearchedDrawables() {
        return searchedDrawables;
    }

    public List<Drawable> getSavedPoints() {
        return savedPoints;
    }

    public ToggleSwitch getMyPointsToggle() {
        return myPointsToggle;
    }
}
