package bfst20.mapdrawer.map;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum PathColor {

    UNKNOWN(Color.WHITE),
    BUILDING(Color.SADDLEBROWN),
    HIGHWAY(Color.YELLOW),
    COASTLINE(Color.GREY),
    WATER(Color.BLUE),
    FOREST(Color.LIGHTGREEN),
    NONE(Color.BLACK);

    private final Paint color;

    PathColor(Paint color) {
        this.color = color;
    }

    public Paint getColor() {
        return color;
    }
}
