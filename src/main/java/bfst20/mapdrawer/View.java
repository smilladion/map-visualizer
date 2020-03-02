package bfst20.mapdrawer;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class View {

    public View(Stage primaryStage) {
        primaryStage.setTitle("Google Map'nt");
        TextField textField = new TextField("Search here...");
        HBox hbox = new HBox(textField);
        Canvas canvas = new Canvas(640, 480);
        StackPane root = new StackPane(canvas);
        root.getChildren().add(hbox);
        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }
}
