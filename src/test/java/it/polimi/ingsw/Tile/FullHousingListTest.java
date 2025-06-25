package it.polimi.ingsw.Tile;

import it.polimi.ingsw.galaxytrucker.Model.Tile.FullHousingList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullHousingListTest {

    @Test
    @DisplayName("FullHousingList è unchecked e mantiene il messaggio")
    void testRuntimeExceptionProperties() {
        String message = "Unità abitativa piena";
        FullHousingList ex = new FullHousingList(message);
        assertTrue(ex instanceof RuntimeException);
        assertEquals(message, ex.getMessage());
        String s = ex.toString();
        assertTrue(s.contains("FullHousingList"));
        assertTrue(s.contains(message));
    }
}
