package bfst20.mapdrawer;

import java.io.File;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        File file = new File("src/main/resources/samsoe.osm.zip");
        String filename = file.getName();
        String fileExt = filename.substring(filename.lastIndexOf("."));
        switch(fileExt){
        case ".osm":
            new MapView(
                OSMMap.fromFile(file),
                primaryStage);
            break;
        case ".bin":
            
            break;
        case ".zip":
            new MapView(
                OSMMap.fromFile(OSMMap.unZip(file.getPath(), "src/main/resources/")),
                primaryStage);
            break;
        }
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }
}
