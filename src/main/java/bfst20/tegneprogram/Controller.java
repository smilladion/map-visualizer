package bfst20.tegneprogram;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class Controller {
    public MapCanvas mapCanvas;
    Model model;
    Line dragged;
    Point2D lastMouse;

    public void initialize(Model model) {
        this.model = model;
        mapCanvas.initialize(model);
    }

    public void setMousePressed(MouseEvent e) {
        Point2D mc = mapCanvas.toModelCoords(e.getX(), e.getY());
        if (e.isShiftDown()) {
            lastMouse = new Point2D(e.getX(), e.getY());
        } else {
            dragged = new Line(mc.getX(), mc.getY(), mc.getX(), mc.getY());
            model.add(dragged);
        }
    }

    public void setMouseDragged(MouseEvent e) {
        if (dragged == null) {
            mapCanvas.pan(e.getX() - lastMouse.getX(), e.getY() - lastMouse.getY());
            lastMouse = new Point2D(e.getX(), e.getY());
        } else {
            Point2D mc = mapCanvas.toModelCoords(e.getX(), e.getY());
            dragged.x2 = mc.getX();
            dragged.y2 = mc.getY();
            model.notifyObservers();
        }
    }

    public void setMouseReleased(MouseEvent e) {
        dragged = null;
    }

    public void setScrolled(ScrollEvent e) {
        double factor = Math.pow(1.001, e.getDeltaY());
        mapCanvas.zoom(factor, e.getX(), e.getY());
    }

    public void buttonAction(ActionEvent actionEvent) {
        new Alert(AlertType.INFORMATION, "du klikkede på knappen").showAndWait();
    }

}
