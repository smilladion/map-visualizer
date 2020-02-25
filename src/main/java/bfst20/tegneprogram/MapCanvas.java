package bfst20.tegneprogram;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.List;

public class MapCanvas extends Canvas {

    private Model model;
    private GraphicsContext gc;
    private Affine trans;

    public void initialize(Model model) {
        this.model = model;
        this.gc = getGraphicsContext2D();
        this.trans = new Affine();
        model.addObserver(this::repaint);
        resetView();
        widthProperty().bind(getScene().widthProperty());
        heightProperty().bind(getScene().heightProperty());
        widthProperty().addListener((a,b,c) -> {
            repaint();
        });
        heightProperty().addListener((a,b,c) -> {
            repaint();
        });
    }

    public void resetView() {
        pan(-model.minlon, -model.minlat);
        zoom(getWidth() / (model.maxlat - model.minlat), 0, 0);
        repaint();
    }

    public void repaint() {
        gc.setTransform(new Affine());
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setFill(Color.LIGHTGREEN);
        for (Drawable island : model.islands) {
            island.draw(gc);
            gc.fill();
        }
        double pixelwidth = 1/Math.sqrt(Math.abs(trans.determinant()));
        gc.setLineWidth(pixelwidth);
        gc.setFillRule(FillRule.EVEN_ODD);
        // GREEN
        paintDrawables(model.getDrawablesOfType(Type.GREEN), true, 1);

        // WATER
        paintDrawables(model.getDrawablesOfType(Type.WATER), true, 1);

        // HIGHWAY
        paintDrawables(model.getDrawablesOfType(Type.HIGHWAY), false, 3 * pixelwidth);

        // BUILDING
        paintDrawables(model.getDrawablesOfType(Type.BUILDING), true, 1);
    }

    private void paintDrawables(List<Drawable> drawables, boolean fill, double linewidth) {
        if (drawables.size() != 0) {
            Type type =drawables.get(0).getType();
            gc.setStroke(Type.getColor(type));
            if (fill) gc.setFill(Type.getColor(type));
            for (Drawable drawable : drawables) {
                drawable.draw(gc);
                if (fill) gc.fill();
            }
        }

    }

    public Point2D toModelCoords(double x, double y) {
        try {
            return trans.inverseTransform(x, y);
        } catch (NonInvertibleTransformException e) {
            // Troels siger at det her ikke kan ske
            e.printStackTrace();
            return null;
        }
    }

    public void zoom(double factor, double x, double y) {
        trans.prependScale(factor, factor, x, y);
        repaint();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

}
