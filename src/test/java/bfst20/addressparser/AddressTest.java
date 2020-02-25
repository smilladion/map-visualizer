package bfst20.addressparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddressTest {
    @Test
    public void testSimpleAddress() {
        var raw = "Rued Langgaards Vej 7";
        var parsed = Address.parse(raw);
        assertEquals("Rued Langgaards Vej", parsed.street);
        assertEquals("7", parsed.house);
    }
}