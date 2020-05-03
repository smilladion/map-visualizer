package bfst20.mapdrawer;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Class to launch our program through JavaFX.
 */
public class LauncherFX extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) {
        long start = System.currentTimeMillis();
        System.out.println("Loading map...");

        new MapView(
                OSMMap.loadBinary(LauncherFX.class.getResourceAsStream("/denmark.bin")),
                primaryStage
        );

        System.out.println("Map loaded in " + (System.currentTimeMillis() - start) / 1000 + "s");
    }
}
