package bfst20.mapdrawer.map;

import bfst20.mapdrawer.dijkstra.DirectedEdge;
import bfst20.mapdrawer.dijkstra.RouteDescription;
import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Line;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.exceptions.NoAddressMatchException;
import bfst20.mapdrawer.exceptions.NoSavedPointsException;
import bfst20.mapdrawer.kdtree.Rectangle;
import bfst20.mapdrawer.osm.NodeProvider;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.List;

/**
 * This creates the program window itself - and draws everything,
 * the whole map plus UI elements, within. Interactions with these elements
 * are handled by the MapController, and the data drawn comes from OSMMap.
 */
public class MapView {

    private final Affine transform = new Affine();
    private final MapController controller;
    private final OSMMap model;

    private final Canvas canvas;
    private final GraphicsContext context;
    private final StackPane rootPane;

    private final CheckMenuItem showKdTree = new CheckMenuItem("Vis KD-Træ");

    private final TextField toSearchField = new TextField();
    private final TextField fromSearchField = new TextField();

    private final Label zoomDisplay = new Label();

    private final Label closestRoad = new Label();

    private final RadioButton car = new RadioButton();
    private final RadioButton bike = new RadioButton();
    private final RadioButton walk = new RadioButton();
    private final RadioButton helicopter = new RadioButton();

    private final ToggleSwitch myPointsToggle = new ToggleSwitch(); // From the ControlsFX library
    private final ToggleSwitch colorToggle = new ToggleSwitch();

    private final List<Line> lineExtras = new ArrayList<>(); // Extra UI elements
    private final List<Drawable> searchedDrawables = new ArrayList<>(); // User search results currently visible
    private final List<Drawable> routeDrawables = new ArrayList<>(); // Calculated route between two points
    private final List<Drawable> savedPoints = new ArrayList<>(); // Search results that have been saved

    private Point pointOfInterest = new Point();

    private final VBox routeDescription = new VBox(); // Empty VBox gets filled with labels from RouteDescription object
    private final Button closeRouteButton = new Button("✕");
    private final ScrollPane scrollPane = new ScrollPane(routeDescription);
    private final VBox routeMenu = new VBox(closeRouteButton, scrollPane);

    public MapView(OSMMap model, Stage window) {
        this.model = model;
        controller = new MapController(model, this, window);

        // Application options.
        window.setTitle("Google Map'nt");
        window.getIcons().add(new Image("point_a_window.png"));

        // Creating the JavaFX basics.
        canvas = new Canvas(1280, 720);
        context = canvas.getGraphicsContext2D();
        rootPane = new StackPane(canvas); // StackPane makes sure UI elements can go on top of the map itself

        // The top menu and its items.
        MenuBar menuBar = new MenuBar();
        VBox menuBox = new VBox(menuBar);
        menuBox.setPickOnBounds(false); // Transparent areas of the box are ignored - zoom/pan now works in those areas

        MenuItem loadFile = new MenuItem("Åbn...      (.zip, .osm, .bin)");
        MenuItem saveFile = new MenuItem("Gem...                      (.bin)");

        Menu fileMenu = new Menu("Fil");
        Menu optionsMenu = new Menu("Indstillinger");
        fileMenu.getItems().add(loadFile);
        fileMenu.getItems().add(saveFile);
        optionsMenu.getItems().add(showKdTree);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(optionsMenu);

        rootPane.getChildren().add(menuBox);

        // The text for the search fields.
        toSearchField.setPromptText("Til...");
        fromSearchField.setPromptText("Fra...");

        // Setting autocompletion on the search fields.
        AutoCompletionBinding<String> autoTo = TextFields.bindAutoCompletion(toSearchField, model.getAddressList());
        AutoCompletionBinding<String> autoFrom = TextFields.bindAutoCompletion(fromSearchField, model.getAddressList());

        autoTo.setVisibleRowCount(5);
        autoTo.setMinWidth(300);

        autoFrom.setVisibleRowCount(5);
        autoFrom.setMinWidth(300);

        // Our buttons.
        Button clearButton = new Button("Nulstil");
        Button savePointButton = new Button("Gem punkt");
        Button swapAddressButton = new Button("<->");
        swapAddressButton.setId("swapAddress");

        // The zoom scale in the bottom right corner.
        zoomDisplay.setId("zoomDisplay");
        HBox zoomLevel = new HBox(zoomDisplay);
        zoomLevel.setAlignment(Pos.BOTTOM_RIGHT);
        zoomLevel.setPickOnBounds(false);
        rootPane.getChildren().add(zoomLevel);

        // The label in the bottom left corner for nearest road.
        closestRoad.setId("closestRoad");
        HBox roadBox = new HBox(closestRoad);
        roadBox.setPadding(new Insets(0, 0, 13, 15));
        roadBox.setAlignment(Pos.BOTTOM_LEFT);
        roadBox.setPickOnBounds(false);
        rootPane.getChildren().add(roadBox);

        // The different types of routes.
        // ToggleGroup ensures you can only choose one button at a time.
        ToggleGroup radioGroup = new ToggleGroup();
        car.setToggleGroup(radioGroup);
        bike.setToggleGroup(radioGroup);
        walk.setToggleGroup(radioGroup);
        helicopter.setToggleGroup(radioGroup);
        car.setSelected(true);
        
        bike.setGraphic(new ImageView(new Image("bike.png")));
        car.setGraphic(new ImageView(new Image("car.png")));
        walk.setGraphic(new ImageView(new Image("walk.png")));
        helicopter.setGraphic(new ImageView(new Image("helicopter.png")));

        HBox routeType = new HBox(car, bike, walk, helicopter);
        routeType.setAlignment(Pos.TOP_CENTER);
        routeType.setSpacing(20.0);
        routeType.setPickOnBounds(false);

        // The upper row for address searching.
        HBox searchRow = new HBox(clearButton, fromSearchField, swapAddressButton, toSearchField, savePointButton);
        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.TOP_CENTER);
        searchRow.setPadding(new Insets(10.0));
        searchRow.setPickOnBounds(false);

        // The search row and types of route put together vertically.
        VBox searchUI = new VBox(searchRow, routeType);
        searchUI.setPadding(new Insets(25.0));
        searchUI.setPickOnBounds(false);

        rootPane.getChildren().add(searchUI);

        // The toggles on the right side of the screen.
        myPointsToggle.setText("Vis gemte punkter");
        colorToggle.setText("Sort/hvid tema");

        VBox toggles = new VBox(myPointsToggle, colorToggle);
        toggles.setId("toggleBox");
        toggles.setAlignment(Pos.TOP_RIGHT);
        toggles.setPickOnBounds(false);
        rootPane.getChildren().add(toggles);

        // The box for the route description.
        routeMenu.setMaxSize(300, 550);
        routeMenu.setPickOnBounds(false);
        routeMenu.setVisible(false);
        routeMenu.setSpacing(3);
        
        routeDescription.setId("routeDescription");
        closeRouteButton.setId("closeRoute");
        routeMenu.setId("routeMenu");

        rootPane.setAlignment(Pos.CENTER_LEFT);
        rootPane.getChildren().add(routeMenu);

        // All of the events and listeners from the controller, attached to UI elements.
        myPointsToggle.setOnMouseClicked(controller.getSavedToggleAction());
        colorToggle.setOnMouseClicked(controller.getColorToggleAction());

        toSearchField.setOnAction(controller.getSearchDijkstraAction());
        fromSearchField.setOnAction(controller.getSearchDijkstraAction());
        swapAddressButton.setOnAction(controller.getSwapAddressAction());
        
        savePointButton.setOnAction(controller.getSaveAddressAction());
        clearButton.setOnAction(controller.getClearAction());

        loadFile.setOnAction(controller.getLoadFileAction());
        saveFile.setOnAction(controller.getSaveFileAction());

        car.setOnAction(controller.getSearchDijkstraAction());
        bike.setOnAction(controller.getSearchDijkstraAction());
        walk.setOnAction(controller.getSearchDijkstraAction());
        helicopter.setOnAction(controller.getSearchDijkstraAction());
        
        canvas.setOnMouseClicked(controller.getClickAction());
        canvas.setOnMouseDragged(controller.getPanAction());
        canvas.setOnScroll(controller.getScrollAction());
        canvas.setOnMouseMoved(controller.getRoadFinderAction());

        closeRouteButton.setOnAction(controller.getCloseRouteMenuAction());
        
        showKdTree.addEventHandler(EventType.ROOT, event -> paintMap());

        Scene scene = new Scene(rootPane);

        // Adds CSS styling to the program
        scene.getStylesheets().add("mapStyle.css");

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
        
        // Set the zoom scale to the correct value
        if (getMetersPerPixels(80) >= 1000) {
            zoomDisplay.setText(String.format("%.2f", (getMetersPerPixels(80)) / 1000) + " km");
        } else {
            zoomDisplay.setText(String.format("%.0f", getMetersPerPixels(80)) + " m");
        }
        
        // Remove focus from all UI on startup
        canvas.requestFocus();
    }

    /** Pans the map to the correct values. */
    public void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        paintMap();
    }

    /** Scales the map to the correct values. */
    public void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        
        if (getMetersPerPixels(80) >= 1000) {
            zoomDisplay.setText(String.format("%.2f", (getMetersPerPixels(80)) / 1000) + " km");
        } else {
            zoomDisplay.setText(String.format("%.0f", getMetersPerPixels(80)) + " m");
        }
        
        paintMap();
    }

    /** Sets the camera to original pan/zoom values. */
    private void resetPanZoom() {
        pan(-model.getMinLon(), -model.getMinLat());
        zoom(canvas.getWidth() / (model.getMaxLat() - model.getMinLat()), 0, 0);
    }

    /** Updates and repaints the whole map and all its elements. */
    public void paintMap() {
        clearCanvas();
        lineExtras.clear();
        
        context.setTransform(new Affine());

        // Paint background
        if (colorToggle.isSelected()) {
            context.setFill(Color.LIGHTGREY);
        } else {
            context.setFill(Color.LIGHTBLUE);
        }

        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Pan and scale all below
        context.setTransform(transform);

        // Line width proportionate to pan/zoom
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
        context.setFillRule(FillRule.EVEN_ODD);

        Point2D topLeft = null;
        Point2D bottomRight = null;

        try {
            if (showKdTree.isSelected()) { // If the "Show KD Tree" button has been pressed
                double size = 325.0; // Use this offset to create a smaller range for searching (making the culling visible on screen)

                // Gives coords for the current zoom/pan level
                topLeft = transform.inverseTransform(canvas.getWidth() / 2 - size / 2, canvas.getHeight() / 2 - size / 2);
                bottomRight = transform.inverseTransform(canvas.getWidth() / 2 + size / 2, canvas.getHeight() / 2 + size / 2);

                // Draws borders for where the culling happens
                lineExtras.add(new Line(topLeft.getX(), topLeft.getY(), topLeft.getX(), bottomRight.getY()));
                lineExtras.add(new Line(bottomRight.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY()));
                lineExtras.add(new Line(topLeft.getX(), topLeft.getY(), bottomRight.getX(), topLeft.getY()));
                lineExtras.add(new Line(topLeft.getX(), bottomRight.getY(), bottomRight.getX(), bottomRight.getY()));
            } else {
                // Set the coords to the corners of the screen
                topLeft = transform.inverseTransform(0.0f, 0.0f);
                bottomRight = transform.inverseTransform(canvas.getWidth(), canvas.getHeight());
            }
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        // Paint all map elements
        for (Type type : Type.values()) {
            if (type.shouldPaint(transform.getMxx())) {
                // Change the linewidth
                context.setLineWidth(type.getLineWidth() / Math.sqrt(Math.abs(transform.determinant())));
                
                // Change the color
                if (colorToggle.isSelected()) {
                    context.setStroke(type.getAlternateColor());
                    context.setFill(type.getAlternateColor());
                } else {
                    context.setStroke(type.getColor());
                    context.setFill(type.getColor());
                }
                
                if (model.getTypeToTree().containsKey(type)) {
                    for (NodeProvider p : model.getTypeToTree().get(type).search(
                            new Rectangle((float) topLeft.getX(), (float) topLeft.getY(), (float) bottomRight.getX(), (float) bottomRight.getY()))) {
                        if (p instanceof OSMWay) {
                            skipInvisibleWays((OSMWay) p);
                        } else {
                            p.draw(context);
                        }
                    }
                }
            }
        }

        context.setLineWidth(3.0 / Math.sqrt(Math.abs(transform.determinant())));
        
        // Draw route
        for (Drawable drawable : routeDrawables) {
            drawable.draw(context);
        }

        // Draw search results
        for (Drawable drawable : searchedDrawables) {
            drawable.draw(context);
        }

        // Set line width back to normal
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));

        // Draw saved points
        if (myPointsToggle.isSelected()) {
            for (Drawable drawable : savedPoints) {
                drawable.draw(context);
            }
        }

        // Drawing the three lines that make up the zoom scale
        try {
            lineExtras.add(new Line(
                    transform.inverseTransform(canvas.getWidth() - 20, canvas.getHeight() -30),
                    transform.inverseTransform(canvas.getWidth() - 100, canvas.getHeight() -30)));
            
            lineExtras.add(new Line(transform.inverseTransform(canvas.getWidth() - 100, canvas.getHeight() -30),
                    transform.inverseTransform(canvas.getWidth() - 100, canvas.getHeight() -35)));
            
            lineExtras.add(new Line(transform.inverseTransform(canvas.getWidth() - 20, canvas.getHeight() -30),
                    transform.inverseTransform(canvas.getWidth() - 20, canvas.getHeight() -35)));
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        // Draw extra UI elements
        for (Line line : lineExtras) {
            line.drawAndSetWidth(context, 2.5 / Math.sqrt(Math.abs(transform.determinant())));
        }

        pointOfInterest.draw(context);
    }

    /** Draws the given nodes at the correct coords using pin point images.*/
    public void paintPoints(OSMNode nodeTo, OSMNode nodeFrom) throws NoAddressMatchException {
        context.setTransform(transform);
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
        
        if (nodeTo == null && nodeFrom == null) {
            throw new NoAddressMatchException();
        } else if (nodeTo != null && nodeFrom == null) {
            searchedDrawables.add(new Point(nodeTo, transform));
            paintMap();
            throw new NoAddressMatchException();
        } else if (nodeTo == null) {
            searchedDrawables.add(new Point(nodeFrom, transform));
            paintMap();
            throw new NoAddressMatchException();
        } else {
            searchedDrawables.add(new Point(nodeTo, transform));
            searchedDrawables.add(new Point(nodeFrom, transform));
            paintMap();
        }
    }

    /** Draws the user's saved points on the map. */
    public void paintSavedPoints() throws NoSavedPointsException {
        if (savedPoints.isEmpty()) {
            throw new NoSavedPointsException();
        } else {
            for (Drawable drawable : savedPoints) {
                drawable.draw(context);
            }
        }
    }

    /** Draws the given route on the map. */
    public void paintRoute(List<DirectedEdge> edges) {
        context.setLineWidth(3.0 / Math.sqrt(Math.abs(transform.determinant())));
        
        for (DirectedEdge edge : edges) {
            routeDrawables.add(edge);
            edge.draw(context);
        }
        
        for (Drawable drawable : searchedDrawables) {
            drawable.draw(context);
        }
    }
    
    /** Fills the route description box with labels based on the current route, and makes the box visible. */
    public void openRouteDescription() {
        routeDescription.getChildren().clear();
        routeMenu.setVisible(true);
        routeDescription.setSpacing(5);
        
        RouteDescription description = new RouteDescription(controller.getRouteEdges(), model, this, controller);
        List<String> routeDescriptionList = description.createRouteDescription();

        Label timeLabel = new Label(description.getRouteTime());
        Label distanceLabel = new Label(RouteDescription.routeDistanceToString(description.getRouteDistance(controller.getRouteEdges())));
        routeDescription.getChildren().add(timeLabel);
        routeDescription.getChildren().add(distanceLabel);
        timeLabel.setId("timeLabel");
        distanceLabel.setId("distanceLabel");

        for (int i = 1; i <= routeDescriptionList.size(); i++) {
            Label label = new Label();
            label.setText(i + ": " + routeDescriptionList.get(i - 1));
            routeDescription.getChildren().add(label);
        }
    }
    
    /** Swaps the addresses in the two text fields. */
    public void swapAddress() {
        String temp = fromSearchField.getText();
        fromSearchField.setText(toSearchField.getText());
        toSearchField.setText(temp);
    }

    /** Clears the canvas. */
    private void clearCanvas() {
        context.setTransform(new Affine());
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /** Calculates the amount of meters per given amount of pixels. */
    private double getMetersPerPixels(int pixels) {
        double metersPerPixel = 111111 * getLatPerPixel(); // 111111 is roughly meters per 1 degree lat
        return pixels * metersPerPixel;
    }

    /** Calculates latitude degrees per pixel. */
    private double getLatPerPixel() {
        return 1 / Math.sqrt(Math.abs(context.getTransform().determinant()));
    }

    /** 
     * Draws a way - but skips drawing a line from it if the line's 
     * length is less than the length of a pixel.
     * */
    private void skipInvisibleWays(OSMWay way) {
        OSMNode previousNode = way.getNodes().get(0);

        context.beginPath();
        context.moveTo(previousNode.getLon(), previousNode.getLat());

        for (OSMNode node : way.getNodes().subList(1, way.getNodes().size())) {
            if (node.distance(previousNode) > getLatPerPixel()) {
                context.lineTo(node.getLon(), node.getLat());

                previousNode = node;
            }
        }

        context.stroke();
        
        if (way.getType().shouldBeFilled()) {
            context.fill();
        }
    }

    /** Clears the search fields and removes focus. */
    public void resetSearchField() {
        toSearchField.clear();
        fromSearchField.clear();
        rootPane.requestFocus();
    }

    public void setClosestRoad(String t) {
        closestRoad.setText(t);
    }

    public void setPointOfInterest(Point p) {
        pointOfInterest = p;
    }
    
    public Point getPointOfInterest() {
        return pointOfInterest;
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
    
    public Label getClosestRoad() {
        return closestRoad;
    }

    public RadioButton getCar() {
        return car;
    }
    public RadioButton getBike() {
        return bike;
    }
    public RadioButton getWalk() {
        return walk;
    }
    
    public RadioButton getHelicopter() {
        return helicopter;
    }

    public Affine getTransform() {
        return transform;
    }

    public VBox getRouteMenu() {
        return routeMenu;
    }

    public List<Drawable> getRouteDrawables() {
        return routeDrawables;
    }
}
