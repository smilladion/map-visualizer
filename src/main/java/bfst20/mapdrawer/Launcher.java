package bfst20.mapdrawer;

import bfst20.mapdrawer.map.View;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Temporarily load the "samsoe.osm" for testing
        OSMMap.fromFile(new File("src/main/resources/samsoe.osm"));

        new View(primaryStage);
    }
}
