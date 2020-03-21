package bfst20.mapdrawer.map;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum PathColor {

    UNKNOWN(Color.WHITE),
    BUILDING(Color.SADDLEBROWN),
    HIGHWAY(Color.YELLOW),
    COASTLINE(Color.GREY),
    WATER(Color.LIGHTBLUE),
    BEACH(Color.YELLOW),
    FOREST(Color.FORESTGREEN),
    COMMERCIAL(Color.LIGHTPINK),
    CONSTRUCTION(Color.LIGHTGREY),
    ALLOTMENTS(Color.LIGHTGREEN),
    FARMLAND(Color.LIGHTGOLDENRODYELLOW),
    MEADOW(Color.LIGHTGREEN),
    ORCHARD(Color.GREEN),
    BASIN(Color.LIGHTBLUE),
    BROWNFIELD(Color.DARKKHAKI),
    CEMETERY(Color.LIGHTGREEN),
    GRASS(Color.LAWNGREEN),
    RESERVOIR(Color.LIGHTBLUE),
    VILLAGE_GREEN(Color.LIGHTGREEN),
    PARK(Color.LIGHTGREEN),
    DANGER_AREA(Color.TOMATO),
    NONE(Color.BLACK);

    private final Paint color;

    PathColor(Paint color) {
        this.color = color;
    }

    public Paint getColor() {
        return color;
    }
}
