package bfst20.mapdrawer.drawing;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;

public enum Type {

    // First type (unknown) will be drawn first, bottom type will be last.

    UNKNOWN(null, null, Color.LIGHTBLUE, Color.LIGHTGREY, false, 0, Integer.MAX_VALUE),
    COASTLINE("natural", "coastline", Color.LIGHTBLUE, Color.LIGHTGREY, false, 0, Integer.MAX_VALUE),
    //COMMERCIAL("landuse", "commercial", Color.LIGHTPINK, true, 0, 4000),
    //CONSTRUCTION("landuse", "construction", Color.LIGHTGREY, true, 0, 4000),
    //INDUSTRIAL("landuse", "industrial", Color.LIGHTGREY, true, 0, 4000),

    // TODO: "residential" duplicate value, atm we just draw roads
    //RESIDENTIAL("landuse", "residential", Color.LIGHTPINK, true, 0, 4000),

    //RETAIL("landuse", "retail", Color.LIGHTPINK, true, 0, 4000),
    //MILITARY("landuse", "military", Color.TOMATO, true, 0, 4000),
    ALLOTMENTS("landuse", "allotments", Color.LIGHTGREEN, Color.LIGHTGREY, true, 0, 18000),
    WETLAND("natural", "wetland", Color.DARKSEAGREEN, Color.DARKGREY, true, 0, 18000),

    // TODO: Scuffed grass relations
    //GRASS("landuse", "grass", Color.LAWNGREEN, true, 0, 18000),

    FARMLAND("landuse", "farmland", Color.LIGHTGOLDENRODYELLOW, Color.GAINSBORO, true, 0, 6000),
    BROWNFIELD("landuse", "brownfield", Color.DARKKHAKI, Color.GAINSBORO, true, 0, 18000),
    LANDFILL("landuse", "landfill", Color.DARKKHAKI, Color.DIMGRAY, true, 0, 18000),
    GRASSLAND("natural", "grassland", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    FOREST("landuse", "forest", Color.LIGHTGREEN, Color.DIMGREY, true, 0, 6000),
    HEATH("natural", "heath", Color.WHEAT, Color.GAINSBORO, true, 0, 18000),
    MEADOW("landuse", "meadow", Color.LIGHTGREEN, Color.GAINSBORO, true, 0, 6000),
    QUARRY("landuse", "quarry", Color.LIGHTGREY, Color.LIGHTGREY, true, 0, 18000),
    WOOD("natural", "wood", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    CEMETERY("landuse", "cemetery", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    ORCHARD("landuse", "orchard", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    FARMYARD("landuse", "farmyard", Color.LIGHTGOLDENRODYELLOW, Color.GAINSBORO, true, 0, 18000),
    AERODROME("aeroway", "aerodrome", Color.LIGHTGREY, Color.LIGHTGREY, true, 0, 18000),
    APRON("aeroway", "apron", Color.GREY, Color.GREY, true, 0, 18000),
    RUNWAY("aeroway", "runway", Color.DARKGREY, Color.DARKGREY, true, 2, 18000),
    BASIN("landuse", "basin", Color.LIGHTBLUE, Color.SILVER, true, 0, 6000),
    RESERVOIR("landuse", "reservoir", Color.LIGHTBLUE, Color.SILVER, true, 0, 6000),
    PARKING("amenity", "parking", Color.LIGHTGREY, Color.LIGHTGREY, true, 0, 18000),
    VILLAGE_GREEN("landuse", "village_green", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    SCRUB("natural", "scrub", Color.LIGHTGREEN, Color.GREY, true, 0, 18000),
    BEACH("natural", "beach", Color.KHAKI, Color.GAINSBORO, true, 0, 18000),
    WATER("natural", "water", Color.LIGHTBLUE, Color.SILVER, true, 0, 6000),
    STREAM("waterway", "stream", Color.LIGHTBLUE, Color.SILVER, false, 2, 6000),
    PIER("man_made", "pier", Color.LIGHTGREY, Color.LIGHTGREY, false, 2, 18000),

    // roads
    HIGHWAY("highway", "highway", Color.GAINSBORO, Color.DARKGREY, false, 1, 80000),
    MOTORWAY("highway", "motorway", Color.SALMON, Color.DARKGREY, false, 4, 1000),
    MOTORWAY_LINK("highway", "motorway_link", Color.SALMON, Color.DARKGREY, false, 4, 1000),
    PRIMARY("highway", "primary", Color.LIGHTPINK, Color.DARKGREY, false, 4, 1000),
    PRIMARY_LINK("highway", "primary_link", Color.LIGHTPINK, Color.DARKGREY, false, 4, 1000),
    SECONDARY("highway", "secondary", Color.BISQUE, Color.DARKGREY, false, 4, 1000),
    SECONDAY_LINK("highway", "seconday", Color.BISQUE, Color.DARKGREY, false, 3, 1000),
    TERTIARY("highway", "tertiary", Color.GAINSBORO, Color.DARKGREY, false, 4, 6000),
    TERTIARY_LINK("highway", "tertiary_link", Color.GAINSBORO, Color.DARKGREY, false, 3, 6000),
    UNCLASSIFIED("highway", "unclassified", Color.GAINSBORO, Color.DARKGREY, false, 3, 10000),
    RESIDENTIAL_ROAD("highway", "residential", Color.GAINSBORO, Color.DARKGREY, false, 2, 18000),
    LIVING_STREET("highway", "living_street", Color.GAINSBORO, Color.DARKGREY, false, 2, 30000),
    SERVICE("highway", "service", Color.GAINSBORO, Color.DARKGREY, false, 1, 80000),
    PEDESTRIAN("highway", "pedestrian", Color.GAINSBORO, Color.DARKGREY, false, 1, 80000),
    TRACK("highway", "track", Color.GAINSBORO, Color.DARKGREY, false, 1, 80000),

    SEARCHRESULT("highway", "searchresult", Color.RED, Color.RED, false, 3, 1000),
    BUILDING("building", "building", Color.DARKGREY, Color.GREY, true, 0, 80000),
    NONE(null, null, Color.BLACK, Color.BLACK, false, 0, 0);

    // key should be exactly what is read from the 'key' field in a tag in the osm file eg. "landuse" or "natural"
    private final String key;
    // value should be exactly what is read from the 'value' field in a tag in the osm file eg. "farmland" or "scrub"
    private final String value;
    private final Paint color;
    private final Paint alternateColor;
    private final boolean fill;
    // For relations and types that should be filled lineWidth = 0
    // For ways that should be drawn with different widths a lineWidth can be specified. 1 is the narrowest.
    private final int lineWidth;
    // zoomLevel 0 is the baseline and will always be drawn, larger numbers = larger (closer) zoomLevel
    private final int zoom;

    Type(String key, String value, Paint color, Paint alternateColor, boolean fill, int lineWidth, int zoom) {
        this.key = key;
        this.value = value;
        this.color = color;
        this.alternateColor = alternateColor;
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
        for (Type type : Type.values()) {
            map.put(type.value, type);
        }
    }

    // If map contains a mapping for the specified value return true
    // Used to check if Type class contains the given type in a <tag> element from osm file
    public static boolean containsType(String value) {
        return map.containsKey(value);
    }

    // Return the Type object corresponding to given value
    public static Type getType(String value) {
        return map.get(value);
    }

    public Paint getColor() {
        return color;
    }

    public Paint getAlternateColor() {
        return alternateColor;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public boolean shouldBeFilled() {
        return fill;
    }

    // If the type's zoom < transform.getMxx() returns true, draw
    public boolean shouldPaint(double mxx) {
        return zoom <= mxx;
    }
}
