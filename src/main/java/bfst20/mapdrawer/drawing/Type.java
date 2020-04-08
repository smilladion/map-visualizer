package bfst20.mapdrawer.drawing;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Type {

    // First type (unknown) should be drawn first, bottom type will be last.

    UNKNOWN(null, Color.TRANSPARENT, false),
    COASTLINE("coastline", Color.TRANSPARENT, false),
    COMMERCIAL("commercial", Color.LIGHTPINK, true),
    CONSTRUCTION("construction", Color.LIGHTGREY, true),
    INDUSTRIAL("industrial", Color.LIGHTGREY, true),
    RESIDENTIAL("residential", Color.LIGHTPINK, true),
    RETAIL("retail", Color.PALETURQUOISE, true),
    MILITARY("military", Color.TOMATO, true),
    ALLOTMENTS("allotments", Color.LIGHTGREEN, true),
    WETLAND("wetland", Color.CADETBLUE, true),
    //GRASS("grass", Color.LAWNGREEN, true),
    FARMLAND("farmland", Color.LIGHTGOLDENRODYELLOW, true),
    BROWNFIELD("brownfield", Color.DARKKHAKI, true),
    LANDFILL("landfill", Color.DARKKHAKI, true),
    GRASSLAND("grassland", Color.LAWNGREEN, true),
    FOREST("forest", Color.FORESTGREEN, true),
    HEATH("heath", Color.WHEAT, true),
    MEADOW("meadow", Color.LIGHTGREEN, true),
    QUARRY("quarry", Color.LIGHTGREY, true),
    WOOD("wood", Color.FORESTGREEN, true), 
    CEMETERY("cemetery", Color.LIGHTGREEN, true),
    ORCHARD("orchard", Color.GREEN, true),
    FARMYARD("farmyard", Color.DARKSALMON, true),
    AERODROME("aerodrome", Color.LIGHTGREY, true),
    APRON("apron", Color.GREY, true),
    RUNWAY("runway", Color.DARKGREY, true),
    BASIN("basin", Color.LIGHTBLUE, true),
    RESERVOIR("reservoir", Color.LIGHTBLUE, true),
    PARKING("parking", Color.LIGHTGREY, true),
    VILLAGE_GREEN("village_green", Color.LIGHTGREEN, true),
    SCRUB("scrub", Color.DARKOLIVEGREEN, true),
    BEACH("beach", Color.YELLOW, true),
    WATER("water", Color.LIGHTBLUE, true),
    STREAM("stream", Color.LIGHTBLUE, false),
    PIER("pier", Color.LIGHTGREY, false),
    HIGHWAY("highway", Color.DIMGRAY, false),
    SEARCHRESULT("searchresult", Color.RED, false),
    BUILDING("building", Color.SADDLEBROWN, true),
    NONE("none", Color.BLACK, false);

    private final String key;
    private final Paint color;
    private final boolean fill;

    Type(String key, Paint color, boolean fill) {
        this.key = key;
        this.color = color;
        this.fill = fill;
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
    // Used to check if Type class contains the given type in a <tag> element from OSM data
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

    public boolean shouldBeFilled(){
        return fill;
    }
    
}
