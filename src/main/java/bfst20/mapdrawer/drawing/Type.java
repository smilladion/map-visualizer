package bfst20.mapdrawer.drawing;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Type {

    // First type (unknown) will be drawn first, bottom type will be last.

    UNKNOWN(null, Color.TRANSPARENT, false, 0),
    COASTLINE("coastline", Color.TRANSPARENT, false, 0),
    COMMERCIAL("commercial", Color.LIGHTPINK, true, 0),
    CONSTRUCTION("construction", Color.LIGHTGREY, true, 0),
    INDUSTRIAL("industrial", Color.LIGHTGREY, true, 0),
    RESIDENTIAL("residential", Color.LIGHTPINK, true, 0),
    RETAIL("retail", Color.PALETURQUOISE, true, 0),
    MILITARY("military", Color.TOMATO, true, 0),
    ALLOTMENTS("allotments", Color.LIGHTGREEN, true, 0),
    WETLAND("wetland", Color.CADETBLUE, true, 0),
    //GRASS("grass", Color.LAWNGREEN, true, 0),
    FARMLAND("farmland", Color.LIGHTGOLDENRODYELLOW, true, 0),
    BROWNFIELD("brownfield", Color.DARKKHAKI, true, 0),
    LANDFILL("landfill", Color.DARKKHAKI, true, 0),
    GRASSLAND("grassland", Color.LAWNGREEN, true, 0),
    FOREST("forest", Color.FORESTGREEN, true, 0),
    HEATH("heath", Color.WHEAT, true, 0),
    MEADOW("meadow", Color.LIGHTGREEN, true, 0),
    QUARRY("quarry", Color.LIGHTGREY, true, 0),
    WOOD("wood", Color.FORESTGREEN, true, 0), 
    CEMETERY("cemetery", Color.LIGHTGREEN, true, 0),
    ORCHARD("orchard", Color.GREEN, true, 0),
    FARMYARD("farmyard", Color.DARKSALMON, true, 0),
    AERODROME("aerodrome", Color.LIGHTGREY, true, 0),
    APRON("apron", Color.GREY, true, 0),
    RUNWAY("runway", Color.DARKGREY, true, 2),
    BASIN("basin", Color.LIGHTBLUE, true, 0),
    RESERVOIR("reservoir", Color.LIGHTBLUE, true, 0),
    PARKING("parking", Color.LIGHTGREY, true, 0),
    VILLAGE_GREEN("village_green", Color.LIGHTGREEN, true, 0),
    SCRUB("scrub", Color.DARKOLIVEGREEN, true, 0),
    BEACH("beach", Color.YELLOW, true, 0),
    WATER("water", Color.LIGHTBLUE, true, 0),
    STREAM("stream", Color.LIGHTBLUE, false, 2),
    PIER("pier", Color.LIGHTGREY, false, 2),
    HIGHWAY("highway", Color.DIMGRAY, false, 1),
    SEARCHRESULT("searchresult", Color.RED, false, 3),
    BUILDING("building", Color.SADDLEBROWN, true, 0),
    NONE("none", Color.BLACK, false, 0);

    // Key should be exactly what is read from the 'value' field in a tag in the osm file eg. "farmland" or "scrub"
    private final String key;
    private final Paint color;
    private final boolean fill;
    // For relations and types that should be filled lineWidth = 0
    // For ways that should be drawn with different widths a lineWidth can be specified. 1 is the narrowest.
    private final int lineWidth;

    Type(String key, Paint color, boolean fill, int lineWidth) {
        this.key = key;
        this.color = color;
        this.fill = fill;
        this.lineWidth = lineWidth;
    }

    private static final Map<String, Type> map;

    // Initialise static HashMap with mappings from the Type's key to the Type itself.
    // Used for lookup in containsType() method.
    static {
        map = new HashMap<String, Type>();

        // Add every type and their key to the map
        for(Type type : Type.values()){
            map.put(type.key, type);
        }
    }

    // If map contains a mapping for the specified key return true
    // Used to check if Type class contains the given type in a <tag> element from osm file
    public static boolean containsType(String key){
        return map.containsKey(key);
    }

    // Return the Type object corresponding to given key
    public static Type getType(String key){
        return map.get(key);
    }

    public Paint getColor(){
        return color;
    }

    public String getKey(){
        return key;
    }

    public int getLineWidth(){
        return lineWidth;
    }

    public boolean shouldBeFilled(){
        return fill;
    }
    
}
