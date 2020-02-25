package bfst20.tegneprogram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * LineTest
 */
public class LineTest {

    @Test
    public void testConstructor() {
        var line = new Line("LINE 1 2 3 4");
        assertEquals(1, line.x1);
        assertEquals(2, line.y1);
        assertEquals(3, line.x2);
        assertEquals(4, line.y2);
    }
}