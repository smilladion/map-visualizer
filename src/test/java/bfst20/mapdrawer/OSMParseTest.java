package bfst20.mapdrawer;

import bfst20.mapdrawer.drawing.Type;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.Assert.*;

public class OSMParseTest {
    
    // Node 1: Right on bounds
    // Node 2: Outside of bounds
    // Node 3-10: Normal

    @Test
    void parseBounds() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        assertEquals(-56.0f, map.getMinLat(), 0);
        assertEquals(0.56f * 10.4f, map.getMinLon(), 0);
        assertEquals(-55.7f, map.getMaxLat(), 0);
        assertEquals(0.56f * 10.7f, map.getMaxLon(), 0);
    }

    @Test
    void parseAddressSize() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        assertEquals(8, map.getAddressNodes().size(), 0);
    }

    @Test
    void parseAddressContent() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        
        for (OSMNode node : map.getAddressNodes()) {
            if (node.getAsLong() != 1 && node.getAsLong() != 2) {
                assertNotNull(node.getAddress());
            }
        }
        
        // Three of these are "not equal" because of the comma and space placements
        assertEquals("Tranemosevej 2, 8305 Pillemark, Samsø", map.getAddressNodes().get(0).getAddress());
        assertEquals("Tranemosevej 2, 8305 Samsø", map.getAddressNodes().get(1).getAddress());
        assertNotEquals("Tranemosevej 2, 8305 Pillemark", map.getAddressNodes().get(2).getAddress());
        assertEquals("8305 Pillemark, Samsø", map.getAddressNodes().get(3).getAddress());
        assertNotEquals("Tranemosevej 2, 8305", map.getAddressNodes().get(4).getAddress());
        assertEquals("Tranemosevej 2, Pillemark, Samsø", map.getAddressNodes().get(5).getAddress());
        assertEquals("8305 Samsø", map.getAddressNodes().get(6).getAddress());
        assertNotEquals("Tranemosevej, Samsø", map.getAddressNodes().get(7).getAddress());
    }

    @Test
    void parseAddressList() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);

        assertTrue(map.getAddressList().contains("Tranemosevej 2, 8305 Pillemark, Samsø"));
        assertTrue(map.getAddressList().contains("Tranemosevej 2, 8305 Samsø"));
        assertTrue(map.getAddressList().contains("Tranemosevej 2, 8305 Pillemark, "));
        assertTrue(map.getAddressList().contains("8305 Pillemark, Samsø"));
        assertTrue(map.getAddressList().contains("Tranemosevej 2, 8305 "));
        assertTrue(map.getAddressList().contains("Tranemosevej 2, Pillemark, Samsø"));
        assertTrue(map.getAddressList().contains("8305 Samsø"));
        assertTrue(map.getAddressList().contains("Tranemosevej Samsø"));
    }

    @Test
    void parseTypes() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        
        assertEquals(6, map.getTypeToTree().size(), 0);
        
        assertTrue(map.getTypeToTree().containsKey(Type.BUILDING));
        assertTrue(map.getTypeToTree().containsKey(Type.RESIDENTIAL_ROAD));
        assertTrue(map.getTypeToTree().containsKey(Type.TERTIARY));
        assertTrue(map.getTypeToTree().containsKey(Type.FOREST));
        assertTrue(map.getTypeToTree().containsKey(Type.WATER));
        assertTrue(map.getTypeToTree().containsKey(Type.COASTLINE)); // Gets put in list no matter what
    }
}
