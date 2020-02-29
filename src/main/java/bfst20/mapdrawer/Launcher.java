package bfst20.mapdrawer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Google Map'nt");
        TextArea label = new TextArea("Hello World!");
        StackPane root = new StackPane();
        Scene primaryScene = new Scene(root, 1000, 800);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }
}
