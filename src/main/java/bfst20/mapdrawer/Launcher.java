package bfst20.mapdrawer;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

public class Launcher extends Application {
    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        File file = new File("src/main/resources/maps/samsoe.osm");
        String fileName = file.getName();
        String fileExt = fileName.substring(fileName.lastIndexOf("."));

        if (fileExt.equals(".osm") || fileExt.equals(".zip")) {
            if (OSMMap.fromFile(file) != null) {
                new MapView(OSMMap.fromFile(file), primaryStage);
            }
        } else if (fileExt.equals(".bin")) {
            new MapView(OSMMap.loadBinary(file), primaryStage);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
