package bfst20.mapdrawer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import bfst20.mapdrawer.map.MapView;
import bfst20.mapdrawer.osm.OSMMap;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        File file = new File("src/main/resources/samsoe.osm");
        String filename = file.getName();
        String fileExt = filename.substring(filename.lastIndexOf("."));
        switch(fileExt){
        case ".osm":
            new MapView(
            OSMMap.fromFile(file),
                primaryStage);
            break;
        case ".txt":
            
            break;
        case ".bin":
            
            break;
        case ".zip":
            unzip(file.getPath(), "src/main/resources/");
        }
    }

    private static void unzip(String zipFilePath, String destDir) {
        // Buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            FileInputStream in = new FileInputStream(zipFilePath);
            ZipInputStream zipInputStream = new ZipInputStream(in);
            ZipEntry zippedFile = zipInputStream.getNextEntry();
            while(zippedFile != null){
                String fileName = zippedFile.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                FileOutputStream out = new FileOutputStream(newFile);
                int herp;
                while ((herp = zipInputStream.read(buffer)) > 0)
                    out.write(buffer, 0, herp);
                
                out.close();
                // Close this ZipEntry
                zipInputStream.closeEntry();
                zippedFile = zipInputStream.getNextEntry();
            }
            // Close last ZipEntry
            zipInputStream.closeEntry();
            zipInputStream.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
