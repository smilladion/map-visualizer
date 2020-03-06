package bfst20.mapdrawer;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

        // Opretter en linked list af alle adresser i streetnames.txt dokumentet. De er i sorteret rækkefølge (med tal først)
        createStreetNameList();

        model = new Model();
        controller = new Controller(model);

        primaryStage.setTitle("Google Map'nt");
        searchBar = new TextField("Search here...");
        lastSearchedLabel = new Label("Last searched: ");

        HBox hbox = new HBox(searchBar, lastSearchedLabel, editButton);

        // Når man trykker på edit knappen, ændres search feltet til det man sidst har søgt på.
        editButton.setOnAction(e -> {
            edit();
        });

        // Når man trykker ENTER efter en søgning.
        searchBar.setOnAction(e -> {
            if (streetNames.contains(searchBar.getText())) {
            }
            updateLastSearchedText();
            addressUpdate();
        });

        hbox.setSpacing(10);

        // sætter det sorte maps logo i hjørnet.
        primaryStage.getIcons().add(model.getIcon());

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
