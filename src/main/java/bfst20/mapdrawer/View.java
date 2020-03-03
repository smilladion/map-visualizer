package bfst20.mapdrawer;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class View {

    public View(Stage primaryStage) {
        primaryStage.setTitle("Google Map'nt");
        Canvas canvas = new Canvas(1920, 1080);
        StackPane root = new StackPane(canvas);
        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }
}
