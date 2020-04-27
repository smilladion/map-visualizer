package bfst20.mapdrawer.map;

import bfst20.mapdrawer.dijkstra.DirectedEdge;
import bfst20.mapdrawer.dijkstra.RouteDescription;
import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.drawing.Line;
import bfst20.mapdrawer.drawing.Point;
import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.osm.NodeProvider;
import bfst20.mapdrawer.kdtree.Rectangle;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
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
import java.util.LinkedList;
import java.util.List;


public class MapView {

    private final Affine transform = new Affine();

    private final CheckMenuItem showKdTree = new CheckMenuItem("Vis KD-Træ");

    private final MapController controller;
    private OSMMap model;

    private final Canvas canvas;
    private final StackPane rootPane;
    private final GraphicsContext context;
    
    private final List<Line> lineExtras = new ArrayList<>(); // Extra UI elements
    private final List<Drawable> searchedDrawables = new ArrayList<>(); // User search results currently visible

    private final List<Drawable> savedPoints = new ArrayList<>(); // Search results that have been saved

    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("Fil");
    private final Menu optionsMenu = new Menu("Indstillinger");

    private final TextField toSearchField = new TextField();
    private final TextField fromSearchField = new TextField();
    private final ToggleSwitch myPointsToggle = new ToggleSwitch(); // from the ControlsFX library

    private final ToggleSwitch colorToggle = new ToggleSwitch(); 
    private final ToggleSwitch nearestToggle = new ToggleSwitch();

    private final RadioButton car;
    private final RadioButton bike;
    private final RadioButton walk;

    private final Label zoomDisplay = new Label();

    private final Label closestRoad = new Label();
    private Point pointOfInterest = new Point();

    public MapView(OSMMap model, Stage window) {

        window.setTitle("Google Map'nt");

        window.getIcons().add(new Image("file:src/main/resources/point_a_window.png"));

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
        MenuItem saveFile = new MenuItem("Gem...                      (.bin)");
        saveFile.setOnAction(controller.getSaveFileAction());
        fileMenu.getItems().add(saveFile);

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

        //togglegroup ensures you can only choose one button at a time.
        ToggleGroup radioGroup = new ToggleGroup();
        car = new RadioButton("Bil");
        bike = new RadioButton("Cykel");
        walk = new RadioButton("Gå");
        car.setToggleGroup(radioGroup);
        bike.setToggleGroup(radioGroup);
        walk.setToggleGroup(radioGroup);
        car.setOnAction(controller.getSearchActionDijkstra());
        bike.setOnAction(controller.getSearchActionDijkstra());
        walk.setOnAction(controller.getSearchActionDijkstra());
        car.setSelected(true);

        Button clearButton = new Button("Nulstil");
        clearButton.setOnAction(controller.getClearAction());

        Button saveToSearch = new Button("Gem punkt");

        myPointsToggle.setText("Vis gemte punkter");
        colorToggle.setText("Sort/hvid tema");
        nearestToggle.setText("Nærmeste vej til mus");

        VBox toggles = new VBox(myPointsToggle, colorToggle, nearestToggle);
        toggles.setId("toggleBox");
        toggles.setAlignment(Pos.TOP_RIGHT);
        toggles.setPickOnBounds(false);
        rootPane.getChildren().add(toggles);

        zoomDisplay.setId("zoomDisplay");
        HBox zoomLevel = new HBox(zoomDisplay);
        zoomLevel.setAlignment(Pos.BOTTOM_RIGHT);
        zoomLevel.setPickOnBounds(false);
        rootPane.getChildren().add(zoomLevel);

        closestRoad.setId("closestRoad");
        HBox roadBox = new HBox(closestRoad);
        roadBox.setPadding(new Insets(0, 0, 13, 15));
        roadBox.setAlignment(Pos.BOTTOM_LEFT);
        roadBox.setPickOnBounds(false);
        closestRoad.setVisible(false);
        rootPane.getChildren().add(roadBox);

        myPointsToggle.setOnMouseClicked(controller.getToggleAction());
        colorToggle.setOnMouseClicked(controller.getColorToggleAction());
        nearestToggle.setOnMouseClicked(controller.getNearestToggleAction());
        toSearchField.setOnAction(controller.getSearchActionDijkstra());
        fromSearchField.setOnAction(controller.getSearchActionDijkstra());
        saveToSearch.setOnAction(controller.getSaveAddressAction());

        canvas.setOnMouseClicked(controller.getClickAction());
        canvas.setOnMouseDragged(controller.getPanAction());
        canvas.setOnScroll(controller.getScrollAction());
        canvas.setOnMouseMoved(controller.getRoadFinderAction());

        HBox searchRow = new HBox(car, bike, walk, clearButton, fromSearchField, toSearchField, saveToSearch);
        searchRow.setSpacing(20.0);
        searchRow.setAlignment(Pos.TOP_CENTER);
        searchRow.setPadding(new Insets(35.0));
        searchRow.setPickOnBounds(false); // Transparent areas of the HBox are ignored - zoom/pan now works in those areas

        rootPane.getChildren().add(searchRow);

        Scene scene = new Scene(rootPane);

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
        if(colorToggle.isSelected()){
            context.setFill(Color.LIGHTGREY);
        } else {
            context.setFill(Color.LIGHTBLUE);
        }
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Pan and scale all below
        context.setTransform(transform);

        // Paint using light yellow
        if(colorToggle.isSelected()){
            context.setFill(Color.WHITE);
        } else {
            context.setFill(Color.LIGHTYELLOW);
        }

        // Line width proportionate to pan/zoom
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
        context.setFillRule(FillRule.EVEN_ODD);

        // Draw islands
        for (Drawable island : model.getIslands()) {
            if(colorToggle.isSelected()){
                context.setStroke(Color.LIGHTGREY);
            } else {
                context.setStroke(Color.LIGHTBLUE);
            }
            island.draw(context);
            context.fill();
        }

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
                if(colorToggle.isSelected()){
                    context.setStroke(type.getAlternateColor());
                    context.setFill(type.getAlternateColor());
                } else {
                    context.setStroke(type.getColor());
                    context.setFill(type.getColor());
                }
                
                if (model.getTypeToTree().containsKey(type)) {
                    for (NodeProvider p : model.getTypeToTree().get(type).search(
                            new Rectangle((float) topLeft.getX(), (float) topLeft.getY(), (float) bottomRight.getX(), (float) bottomRight.getY()))) {
                        p.draw(context);
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

    public void paintPoints(String addressTo, String addressFrom) {
        context.setTransform(transform);
        context.setLineWidth(1.0 / Math.sqrt(Math.abs(transform.determinant())));
        
        if (addressFrom == null && addressTo != null) {
            for (OSMNode node : model.getAddressNodes()) {
                if (node.getAddress().contains(addressTo)) {
                    searchedDrawables.add(new Point(node, transform));
                }
            }
        } else if (addressTo != null) {
            for (OSMNode node : model.getAddressNodes()) {
                if (node.getAddress().equals(addressTo) || node.getAddress().equals(addressFrom)) {
                    searchedDrawables.add(new Point(node, transform));
                }
            }
        }
        paintMap();
    }

    private double calculateAngle1(Point2D vectorFrom, Point2D vectorTo) {

        double dot = vectorFrom.dotProduct(vectorTo);
        double lengthFrom = (Math.sqrt(((vectorFrom.getX())*(vectorFrom.getX()))+((vectorFrom.getY())*(vectorFrom.getY()))));
        double lengthTo = (Math.sqrt(((vectorTo.getX())*(vectorTo.getX()))+((vectorTo.getY())*(vectorTo.getY()))));

        double cosv = (dot / (lengthFrom * lengthTo));

        double angle = Math.acos(cosv);
        double angle1 = Math.toDegrees(angle);

        double realAngle = 180 - angle1;

        return realAngle;
    }

    //From algs4 library
    public static int ccw(Point2D a, Point2D b, Point2D c) {
        double area2 = (b.getX()-a.getX())*(c.getY()-a.getY()) - (b.getY()-a.getY())*(c.getX()-a.getX());
        if      (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else                return  0;
    }



    public void createRouteDescription(LinkedList<DirectedEdge> edgeList) {

        RouteDescription routeDescription = new RouteDescription(edgeList, model, this);
        routeDescription.createRouteDescription();
    }

    public void paintSavedAddresses() {
        for (Drawable drawable : savedPoints) {
            drawable.draw(context);
        }
    }

    public void paintRoute(List<DirectedEdge> edges) {
        context.setLineWidth(2.5 / Math.sqrt(Math.abs(transform.determinant())));
        
        for (DirectedEdge edge : edges) {
            searchedDrawables.add(edge);
            edge.draw(context);
        }
    }

    private void clearCanvas() {
        context.setTransform(new Affine());
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    private double getMetersPerPixels(int pixels) {
        double LatPerPixel = 1 / Math.sqrt(Math.abs(context.getTransform().determinant()));
        double metersPerPixel = 111111 * LatPerPixel; // 111111 is roughly meters per 1 degree lat
        return pixels * metersPerPixel;
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

    public OSMMap getModel() {
        return model;
    }
}
