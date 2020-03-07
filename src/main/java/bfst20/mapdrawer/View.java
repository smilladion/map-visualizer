package bfst20.mapdrawer;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class View {

    private Canvas canvas;
    private StackPane root;
    private Label lastSearchedLabel;
    private TextField searchBar;
    private Controller controller;
    private Model model;
    private TextArea addressArea = new TextArea();
    private List<String> streetNames;
    private Button editButton = new Button("Edit");
    private String lastSearched;

    public View(Stage primaryStage) throws IOException {

        createStreetNameList();

        model = new Model();
        controller = new Controller(model);

        primaryStage.setTitle("Google Map'nt");
        searchBar = new TextField("Search here...");
        lastSearchedLabel = new Label("Last searched: ");

        HBox hbox = new HBox(searchBar, lastSearchedLabel, editButton);

        editButton.setOnAction(e -> {
            edit();
        });

        searchBar.setOnAction(e -> {
            if (streetNames.contains(searchBar.getText())) {
                Button b = new Button(searchBar.getText());
                root.getChildren().add(b);
            }
            updateLastSearchedText();
            addressUpdate();
        });

        hbox.setSpacing(10);

        canvas = new Canvas(640, 480);
        root = new StackPane(canvas);
        root.getChildren().add(hbox);
        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    private void addressUpdate() {
        addressArea.clear();
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
        lastSearched = searchBar.getText();
    }

    public void createStreetNameList() throws IOException {

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("streetnames.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1")); //ISO-8859-1 gør at man kan læse specielle tegn, f.eks. ä.

        streetNames = new LinkedList<String>();

        String streetNameString;

        while ((streetNameString = (br.readLine())) != null) {
            streetNames.add(streetNameString);
        }
    }

    public void edit() {
        searchBar.setText(lastSearched);
    }

}
