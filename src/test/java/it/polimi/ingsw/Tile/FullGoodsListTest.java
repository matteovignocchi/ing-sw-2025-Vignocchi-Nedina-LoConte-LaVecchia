package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.FullGoodsList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullGoodsListTest {

    @Test
    @DisplayName("FullGoodsList è unchecked e mantiene il messaggio")
    void testRuntimeExceptionProperties() {
        String message = "Magazzino pieno";
        FullGoodsList ex = new FullGoodsList(message);
        // è RuntimeException
        assertTrue(ex instanceof RuntimeException);
        // mantiene il messaggio
        assertEquals(message, ex.getMessage());
        // toString lo include
        String s = ex.toString();
        assertTrue(s.contains("FullGoodsList"));
        assertTrue(s.contains(message));
    }
}
