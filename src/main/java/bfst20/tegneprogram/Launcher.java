package bfst20.tegneprogram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("View.fxml"));
        Scene scene = loader.load();
        Controller controller = loader.getController();
        controller.initialize(model);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

	public static void main(String[] args) {
        launch(args);
	}
}