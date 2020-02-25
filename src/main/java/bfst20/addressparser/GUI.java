package bfst20.addressparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * GUI
 */
public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var pane = new BorderPane();
        var input = new TextField();
        var output = new TextArea();
        input.setOnAction(e -> {
            var raw = input.getText();
            var parsed = Address.parse(raw);
            input.clear();
            output.appendText(parsed + "\n\n");
        });
        input.setFont(new Font(30));
        output.setFont(new Font(30));
        pane.setTop(input);
        pane.setCenter(output);
        primaryStage.setTitle("Awesomest Address Aligner 2.0");
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

	public static void main(String[] args) {
        launch(args);
	}
}