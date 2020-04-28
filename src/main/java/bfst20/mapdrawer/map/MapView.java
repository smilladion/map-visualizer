package bfst20.mapdrawer.map;

import bfst20.mapdrawer.dijkstra.*;
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
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapView {

    private final Affine transform = new Affine();
    private final MapController controller;
    private OSMMap model;

    private final Canvas canvas;
    private final GraphicsContext context;
    private final StackPane rootPane;

    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("Fil");
    private final Menu optionsMenu = new Menu("Indstillinger");
    private final CheckMenuItem showKdTree = new CheckMenuItem("Vis KD-Træ");
    private final MenuItem showRouteDescription = new MenuItem("Åben Rutefindings Menu");


    private final TextField toSearchField = new TextField();
    private final TextField fromSearchField = new TextField();

    private final Label zoomDisplay = new Label();

    private final Label closestRoad = new Label();

    private final RadioButton car = new RadioButton("Bil");
    private final RadioButton bike = new RadioButton("Cykel");
    private final RadioButton walk = new RadioButton("Gå");

    private final ToggleSwitch myPointsToggle = new ToggleSwitch(); // from the ControlsFX library
    private final ToggleSwitch colorToggle = new ToggleSwitch();
    private final ToggleSwitch nearestToggle = new ToggleSwitch();

    private final List<Line> lineExtras = new ArrayList<>(); // Extra UI elements
    private final List<Drawable> searchedDrawables = new ArrayList<>(); // User search results currently visible
    private final List<Drawable> savedPoints = new ArrayList<>(); // Search results that have been saved

    private VBox routeDescription = new VBox(); //Empty VBox gets filled with Labels from routedescription
    private Button closeRouteMenu = new Button("Luk");
    private Button reloadRoute = new Button("Opdatér rute");
    private HBox routeDescriptionTopBar = new HBox(reloadRoute, closeRouteMenu);
    private ScrollPane scrollPane = new ScrollPane(routeDescription);
    private VBox routeMenu = new VBox(routeDescriptionTopBar, scrollPane);

    private Point pointOfInterest = new Point();

    public MapView(OSMMap model, Stage window) throws NoAddressMatchException {

        this.model = model;
        controller = new MapController(model, this, window);

        // Application options.
        window.setTitle("Google Map'nt");
        window.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));

        // Creating the JavaFX basics.
        canvas = new Canvas(1280, 720);
        context = canvas.getGraphicsContext2D();
        rootPane = new StackPane(canvas); // StackPane makes sure UI elements can go on top of the map itself

        // The top menu and its items.
        VBox menuBox = new VBox(menuBar);
        menuBox.setPickOnBounds(false);

        MenuItem loadFile = new MenuItem("Åbn...      (.zip, .osm, .bin)");
        MenuItem saveFile = new MenuItem("Gem...                      (.bin)");

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
        closestRoad.setVisible(false);
        rootPane.getChildren().add(roadBox);

        // The different types of routes.
        // ToggleGroup ensures you can only choose one button at a time.
        ToggleGroup radioGroup = new ToggleGroup();
        car.setToggleGroup(radioGroup);
        bike.setToggleGroup(radioGroup);
        walk.setToggleGroup(radioGroup);
        car.setSelected(true);

        HBox routeType = new HBox(car, bike, walk);
        routeType.setAlignment(Pos.TOP_CENTER);
        routeType.setSpacing(20.0);

        // The upper row for address searching.
        HBox searchRow = new HBox(clearButton, fromSearchField, toSearchField, savePointButton);
        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.TOP_CENTER);
        searchRow.setPadding(new Insets(10.0));

        // The search row and types of route put together vertically.
        VBox searchUI = new VBox(searchRow, routeType);
        searchUI.setPadding(new Insets(25.0));
        searchUI.setPickOnBounds(false); // Transparent areas of the HBox are ignored - zoom/pan now works in those areas

        rootPane.getChildren().add(searchUI);

        // The toggles on the right side of the screen.
        myPointsToggle.setText("Vis gemte punkter");
        colorToggle.setText("Sort/hvid tema");
        nearestToggle.setText("Nærmeste vej til mus");

        VBox toggles = new VBox(myPointsToggle, colorToggle, nearestToggle);
        toggles.setId("toggleBox");
        toggles.setAlignment(Pos.TOP_RIGHT);
        toggles.setPickOnBounds(false);
        rootPane.getChildren().add(toggles);

        // All of the events and listeners from the controller, attached to UI elements.
        myPointsToggle.setOnMouseClicked(controller.getToggleAction());
        colorToggle.setOnMouseClicked(controller.getColorToggleAction());
        nearestToggle.setOnMouseClicked(controller.getNearestToggleAction());

        toSearchField.setOnAction(controller.getSearchActionDijkstra());
        fromSearchField.setOnAction(controller.getSearchActionDijkstra());
        savePointButton.setOnAction(controller.getSaveAddressAction());
        clearButton.setOnAction(controller.getClearAction());

        loadFile.setOnAction(controller.getLoadFileAction());
        saveFile.setOnAction(controller.getSaveFileAction());

        showKdTree.addEventHandler(EventType.ROOT, event -> paintMap());

        car.setOnAction(controller.getSearchActionDijkstra());
        bike.setOnAction(controller.getSearchActionDijkstra());
        walk.setOnAction(controller.getSearchActionDijkstra());



        canvas.setOnMouseClicked(controller.getClickAction());
        canvas.setOnMouseDragged(controller.getPanAction());
        canvas.setOnScroll(controller.getScrollAction());
        canvas.setOnMouseMoved(controller.getRoadFinderAction());

        Scene scene = new Scene(rootPane);

        String styles = "-fx-border-color: transparent;" + "-fx-background-color: transparent;";
        optionsMenu.getItems().add(showRouteDescription);
        showRouteDescription.setOnAction(controller.getShowRouteFinding());
        closeRouteMenu.setOnAction(controller.getCloseRouteMenu());
        reloadRoute.setOnAction(controller.getShowRouteFinding());

        routeMenu.setVisible(false);
        routeMenu.setMaxSize(350, 550);
        routeMenu.setStyle(styles);
        routeMenu.setSpacing(20);

        routeDescriptionTopBar.alignmentProperty().setValue(Pos.BOTTOM_CENTER);
        routeDescriptionTopBar.setSpacing(20);
        rootPane.alignmentProperty().setValue(Pos.CENTER_LEFT);
        rootPane.getChildren().add(routeMenu);

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
        


        // Remove focus from search field on startup
        canvas.requestFocus();
    }

    void pan(double dx, double dy) {
        transform.prependTranslation(dx, dy);
        paintMap();
    }

    void zoom(double factor, double x, double y) {
        transform.prependScale(factor, factor, x, y);
        
        if (getMetersPerPixels(80) >= 1000) {
            zoomDisplay.setText(String.format("%.2f", (getMetersPerPixels(80)) / 1000) + " km");
        } else {
            zoomDisplay.setText(String.format("%.0f", getMetersPerPixels(80)) + " m");
        }
        
        paintMap();
    }

    private void resetPanZoom() {
        pan(-model.getMinLon(), -model.getMinLat());
        zoom(canvas.getWidth() / (model.getMaxLat() - model.getMinLat()), 0, 0);
    }

    // Updates and repaints the whole map
    public void paintMap() {
        clearCanvas();
        lineExtras.clear();
        
        // Using identity matrix (no transform)
        context.setTransform(new Affine());

        // Paint background light blue
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
                topLeft = transform.inverseTransform(0.0f, 0.0f);
                bottomRight = transform.inverseTransform(canvas.getWidth(), canvas.getHeight());
            }
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

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
                        if (type == Type.COASTLINE) {
                            skipInvisibleCoastlines(p);
                        } else {
                            p.draw(context);
                        }
                    }
                }
            }
        }

        // Draw search results
        for (Drawable drawable : searchedDrawables) {
            drawable.draw(context);
        }

        // Set line width back to normal
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));

        // Draws saved searches so they are updated on pan/zoom
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

    public void paintPoints(String addressTo, String addressFrom, boolean onPurposeNull) throws NoAddressMatchException {
        context.setTransform(transform);
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
            if (addressFrom == null && addressTo == null && !onPurposeNull) {
                throw new NoAddressMatchException();
            } else if (addressFrom == null && addressTo != null) {
                for (OSMNode node : model.getAddressNodes()) {
                    if (node.getAddress().contains(addressTo)) {
                        searchedDrawables.add(new Point(node, transform));
                    }
                }
            } else if (addressTo != null && addressFrom != null) {
                for (OSMNode node : model.getAddressNodes()) {
                    if (node.getAddress().equals(addressTo) || node.getAddress().equals(addressFrom)) {
                        searchedDrawables.add(new Point(node, transform));
                    }
                }
            } else if (addressFrom != null && addressTo == null) {
                for (OSMNode node : model.getAddressNodes()) {
                    if (node.getAddress().contains(addressFrom)) {
                        searchedDrawables.add(new Point(node, transform));
                    }
                }
            }
        paintMap();
    }

    public void createRouteDescription(LinkedList<DirectedEdge> edgeList) {

        RouteDescription routeDescription = new RouteDescription(edgeList, model, this);
        routeDescription.createRouteDescription();
    }

    public void paintSavedAddresses() throws NoSavedPointsException {
        if (savedPoints.isEmpty()) {
            throw new NoSavedPointsException();
        } else {
            for (Drawable drawable : savedPoints) {
                drawable.draw(context);
            }
        }
    }

    public void paintRoute(List<DirectedEdge> edges) {
        context.setLineWidth(2.5 / Math.sqrt(Math.abs(transform.determinant())));
        
        for (DirectedEdge edge : edges) {
            searchedDrawables.add(edge);
            edge.draw(context);
        }
    }

    public void openRouteDescription() {
        routeMenu.setVisible(true);
        VBox scrollRoutes = new VBox();
        scrollRoutes.setSpacing(5);
        routeDescription.getChildren().add(scrollRoutes);
        RouteDescription description = new RouteDescription(controller.getRouteEdges(), model, this);
        List<String> routeDescriptionList = description.createRouteDescription();

        double distance = 0;

        for (DirectedEdge edge : controller.getRouteEdges()) {
            distance = distance + (edge.getDistance());
        }

        distance = distance * 10000;
        distance = Math.ceil(distance);

        String time = ("Tid: " + distance + " min");

        Label timeLabel = new Label();
        timeLabel.setText(time);
        scrollRoutes.getChildren().add(timeLabel);

        int j = 1;

        for(int i = 0; i < routeDescriptionList.size(); i++){
            Label label = new Label();
            label.setText(j + ": " + routeDescriptionList.get(i));
            scrollRoutes.getChildren().add(label);
            j++;
        }
    }

    private void clearCanvas() {
        context.setTransform(new Affine());
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    private double getMetersPerPixels(int pixels) {
        double metersPerPixel = 111111 * getLatPerPixel(); // 111111 is roughly meters per 1 degree lat
        return pixels * metersPerPixel;
    }

    private double getLatPerPixel() {
        return 1 / Math.sqrt(Math.abs(context.getTransform().determinant()));
    }

    // Does not draw a coastline if its length is less than the length of two pixels.
    private void skipInvisibleCoastlines(NodeProvider provider) {
        if (provider instanceof OSMWay) {
            OSMWay way = (OSMWay) provider;
            OSMNode previousNode = way.getNodes().get(0);

            context.beginPath();
            context.moveTo(previousNode.getLon(), previousNode.getLat());

            for (OSMNode node : way.getNodes().subList(1, way.getNodes().size())) {
                if (node.distance(previousNode) > getLatPerPixel() * 2) {
                    context.lineTo(node.getLon(), node.getLat());

                    previousNode = node;
                }
            }

            context.stroke();
            context.fill();
        }
    }

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

    public ToggleSwitch getColorToggle() {
        return colorToggle;
    }
    
    public ToggleSwitch getNearestToggle() {
        return nearestToggle;
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

    public Affine getTransform() {
        return transform;
    }

    public VBox getRouteMenu() {
        return routeMenu;
    }

    public VBox getRouteDescription() {
        return routeDescription;
    }

    public OSMMap getModel() {
        return model;
    }
}
