package bfst20.mapdrawer;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Type {
    UNKNOWN,
    BUILDING,
    HIGHWAY,
    COASTLINE,
    WATER,
    GREEN;

    public static Paint getColor(Type type){
        switch(type){
            case WATER:
                return Color.BLUE;
            case GREEN:
                return Color.GREEN;
            case BUILDING:
                return Color.BROWN;
            case HIGHWAY:
                return Color.YELLOW;
            case COASTLINE:
                return Color.GREY;
            case UNKNOWN:
                return Color.WHITE;
            default:
                return Color.BLACK;
        }
    }
}