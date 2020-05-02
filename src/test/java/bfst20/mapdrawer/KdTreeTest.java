package bfst20.mapdrawer;

import bfst20.mapdrawer.kdtree.Rectangle;
import bfst20.mapdrawer.osm.NodeProvider;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.transform.Affine;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class KdTreeTest {
    
    @Test
    void nearestHighway() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        
        OSMWay way = map.getHighwayTree().nearest(6.44, 22.2, true, new Affine());
        assertEquals(12, way.getAsLong(), 0);

        OSMWay way2 = map.getHighwayTree().nearest(30.1, 12.4, true, new Affine());
        assertEquals(13, way2.getAsLong(), 0);
    }
    
    @Test
    void searchHighway() throws Exception {
        OSMMap map = OSMMap.fromFile(new File("src/main/resources/parseTest.osm"));

        assertNotNull(map);
        
        List<NodeProvider> providers = map.getHighwayTree().search(new Rectangle(-4.0f, -20.0f, 45.0f, 37.0f));
        
        assertEquals(2, providers.size(), 0);
        assertNotEquals(providers.get(0), providers.get(1));
        
        boolean isWay12 = false;
        boolean isWay13 = false;
        
        for (NodeProvider p : providers) {
            if (p.getAsLong() == 12) {
                isWay12 = true;
            } else if (p.getAsLong() == 13) {
                isWay13 = true;
            }
        }
        
        assertTrue(isWay12);
        assertTrue(isWay13);

        List<NodeProvider> providers2 = map.getHighwayTree().search(new Rectangle(-50.0f, -50.0f, -40.0f, -15.0f));
        assertEquals(1, providers2.size(), 0);
        assertEquals(12, providers2.get(0).getAsLong(), 0);
    }
}
