package bfst20.mapdrawer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import java.util.Optional;

public class Controller {

    View view;
    Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public void updateLastSearchedText() {
        view.getLabel().setText(view.getSearchBar().getText());
    }
}


