package bfst20.mapdrawer;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class View {

    private Canvas canvas;
    private StackPane root;
    private Label lastSearchedLabel;
    private Label streetLabel = new Label("Street: ");
    private Label houseNumberLabel = new Label("Number: ");
    private Label floorLabel = new Label ("Floor: ");
    private Label zipCodeLabel = new Label("Zip-code: ");
    private Label cityLabel = new Label("City: ");
    private TextField searchBar;
    private Controller controller;
    private Model model;
    private TextArea addressArea = new TextArea();

    public View(Stage primaryStage) {

        model = new Model();
        controller = new Controller(model);

        primaryStage.setTitle("Google Map'nt");
        searchBar = new TextField("Search here...");
        lastSearchedLabel = new Label("Last searched: ");

        searchBar.setOnAction(e -> {
            updateLastSearchedText();
            addressUpdate();
        });

        VBox vbox = new VBox(searchBar, lastSearchedLabel, streetLabel, houseNumberLabel, floorLabel, zipCodeLabel, cityLabel, addressArea);

        streetLabel.setVisible(false);
        houseNumberLabel.setVisible(false);
        floorLabel.setVisible(false);
        zipCodeLabel.setVisible(false);
        cityLabel.setVisible(false);

        vbox.setSpacing(10);

        canvas = new Canvas(640, 480);
        root = new StackPane(canvas);
        root.getChildren().add(vbox);
        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    private void addressUpdate() {
        var raw = searchBar.getText();
        var parsed = Address.parse(raw);
        searchBar.clear();
        addressArea.appendText(parsed + "\n\n");
    }

    public Label getLabel() {
        return lastSearchedLabel;
    }

    public TextField getSearchBar() {
        return searchBar;
    }

    public void updateLastSearchedText() {
        lastSearchedLabel.setText("Last searched: " + searchBar.getText());
    }

}
