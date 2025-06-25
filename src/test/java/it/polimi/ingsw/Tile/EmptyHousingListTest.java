package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptyHousingList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmptyHousingListTest {

    @Test
    @DisplayName("getMessage restituisce esattamente il messaggio passato")
    void testMessageStored() {
        String msg = "Nessuna housing disponibile";
        EmptyHousingList ex = new EmptyHousingList(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    @DisplayName("EmptyHousingList è unchecked (RuntimeException)")
    void testIsRuntimeException() {
        EmptyHousingList ex = new EmptyHousingList("oops");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("toString include il nome della classe e il messaggio")
    void testToString() {
        String msg = "vuota";
        EmptyHousingList ex = new EmptyHousingList(msg);
        String s = ex.toString();
        assertTrue(s.contains("EmptyHousingList"));
        assertTrue(s.contains(msg));
    }
}

