package bfst20.mapdrawer.drawing;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Type {

    // First type (unknown) will be drawn first, bottom type will be last.

    UNKNOWN(null, null, Color.TRANSPARENT, false, 0, 0),
    COASTLINE("natural", "coastline", Color.TRANSPARENT, false, 0, 0),
    COMMERCIAL("landuse", "commercial", Color.LIGHTPINK, true, 0, 0),
    CONSTRUCTION("landuse", "construction", Color.LIGHTGREY, true, 0, 0),
    INDUSTRIAL("landuse", "industrial", Color.LIGHTGREY, true, 0, 0),
    RESIDENTIAL("landuse", "residential", Color.LIGHTPINK, true, 0, 0),
    RETAIL("landuse", "retail", Color.LIGHTPINK, true, 0, 0),
    MILITARY("landuse", "military", Color.TOMATO, true, 0, 0),
    ALLOTMENTS("landuse", "allotments", Color.LIGHTGREEN, true, 0, 18000),
    WETLAND("natural", "wetland", Color.CADETBLUE, true, 0, 18000),
    //GRASS("landuse", "grass", Color.LAWNGREEN, true, 0, 18000),
    FARMLAND("landuse", "farmland", Color.LIGHTGOLDENRODYELLOW, true, 0, 18000),
    BROWNFIELD("landuse", "brownfield", Color.DARKKHAKI, true, 0, 18000),
    LANDFILL("landuse", "landfill", Color.DARKKHAKI, true, 0, 18000),
    GRASSLAND("natural", "grassland", Color.LAWNGREEN, true, 0, 18000),
    FOREST("landuse", "forest", Color.FORESTGREEN, true, 0, 18000),
    HEATH("natural", "heath", Color.WHEAT, true, 0, 18000),
    MEADOW("landuse", "meadow", Color.LIGHTGREEN, true, 0, 18000),
    QUARRY("landuse", "quarry", Color.LIGHTGREY, true, 0, 18000),
    WOOD("natural", "wood", Color.FORESTGREEN, true, 0, 18000), 
    CEMETERY("landuse", "cemetery", Color.LIGHTGREEN, true, 0, 18000),
    ORCHARD("landuse", "orchard", Color.GREEN, true, 0, 18000),
    FARMYARD("landuse", "farmyard", Color.DARKSALMON, true, 0, 18000),
    AERODROME("aeroway", "aerodrome", Color.LIGHTGREY, true, 0, 18000),
    APRON("aeroway", "apron", Color.GREY, true, 0, 18000),
    RUNWAY("aeroway", "runway", Color.DARKGREY, true, 2, 38000),
    BASIN("landuse", "basin", Color.LIGHTBLUE, true, 0, 6000),
    RESERVOIR("landuse", "reservoir", Color.LIGHTBLUE, true, 0, 6000),
    PARKING("amenity", "parking", Color.LIGHTGREY, true, 0, 18000),
    VILLAGE_GREEN("landuse", "village_green", Color.LIGHTGREEN, true, 0, 18000),
    SCRUB("natural", "scrub", Color.DARKOLIVEGREEN, true, 0, 18000),
    BEACH("natural", "beach", Color.YELLOW, true, 0, 18000),
    WATER("natural", "water", Color.LIGHTBLUE, true, 0, 6000),
    STREAM("waterway", "stream", Color.LIGHTBLUE, false, 2, 6000),
    PIER("man_made", "pier", Color.LIGHTGREY, false, 2, 18000),

    // roads
    HIGHWAY("highway", "highway", Color.DIMGRAY, false, 1, 80000),
    PRIMARY("highway", "primary", Color.YELLOW, false, 4, 0),
    SECONDARY("highway", "secondary", Color.DARKBLUE, false, 2, 0),
    TERTIARY("highway", "tertiary", Color.DARKGREEN, false, 2, 0),
    
    SEARCHRESULT("highway", "searchresult", Color.RED, false, 3, 0),
    BUILDING("building", "building", Color.SADDLEBROWN, true, 0, 80000),
    NONE(null, null, Color.BLACK, false, 0, 0);

    // key should be exactly what is read from the 'key' field in a tag in the osm file eg. "landuse" or "natural"
    private final String key;
    // value should be exactly what is read from the 'value' field in a tag in the osm file eg. "farmland" or "scrub"
    private final String value;
    private final Paint color;
    private final boolean fill;
    // For relations and types that should be filled lineWidth = 0
    // For ways that should be drawn with different widths a lineWidth can be specified. 1 is the narrowest.
    private final int lineWidth;
    // zoomLevel 0 is the baseline and will always be drawn, larger numbers = larger (closer) zoomLevel
    private final int zoom;

    Type(String key, String value, Paint color, boolean fill, int lineWidth, int zoom) {
        this.key = key;
        this.value = value;
        this.color = color;
        this.fill = fill;
        this.lineWidth = lineWidth;
        this.zoom = zoom;
    }

    private static final Map<String, Type> map;

    // Initialise static HashMap with mappings from the Type's value to the Type itself.
    // Used for lookup in containsType() method.
    static {
        map = new HashMap<String, Type>();

        // Add every type and their value to the map
        for(Type type : Type.values()){
            map.put(type.value, type);
        }
    }

    // If map contains a mapping for the specified value return true
    // Used to check if Type class contains the given type in a <tag> element from osm file
    public static boolean containsType(String value){
        return map.containsKey(value);
    }

    // Return the Type object corresponding to given value
    public static Type getType(String value){
        return map.get(value);
    }

    public Paint getColor(){
        return color;
    }

    public String getKey(){
        return key;
    }

    public String getValue(){
        return value;
    }

    public int getLineWidth(){
        return lineWidth;
    }

    public boolean shouldBeFilled(){
        return fill;
    }

    // If the type's zoom < transform.getMxx() returns true, draw
    public boolean shouldPaint(double mxx){
        return zoom < mxx;
    }
    
}
