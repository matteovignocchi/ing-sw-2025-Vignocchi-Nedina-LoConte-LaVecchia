package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Engine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    @Test
    @DisplayName("costruttore e isDouble funzionano")
    void testConstructorAndIsDouble() {
        Engine single = new Engine(1,2,3,4, false, 7);
        assertFalse(single.isDouble(), "engine single: isDouble==false");
        assertEquals(7, single.getIdTile());

        Engine dbl = new Engine(5,6,7,0, true, 99);
        assertTrue(dbl.isDouble(), "engine double: isDouble==true");
        assertEquals(99, dbl.getIdTile());
    }

    @Test
    @DisplayName("controlCorners restituisce i valori passati a, b, c, d")
    void testControlCorners() {
        Engine e = new Engine(9,8,7,6, true, 11);
        assertEquals(9, e.controlCorners(0));
        assertEquals(8, e.controlCorners(1));
        assertEquals(7, e.controlCorners(2));
        assertEquals(6, e.controlCorners(3));
    }
}

