package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Cannon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CannonTest {

    @Test
    @DisplayName("Costruttore e getters fondamentali")
    void testConstructorAndGetters() {
        Cannon cannon = new Cannon(1, 2, 3, 4, true, 42);

        assertEquals(1, cannon.controlCorners(0), "angolo 0");
        assertEquals(2, cannon.controlCorners(1), "angolo 1");
        assertEquals(3, cannon.controlCorners(2), "angolo 2");
        assertEquals(4, cannon.controlCorners(3), "angolo 3");

        assertTrue(cannon.isDouble(), "dev'essere doppio");
        assertEquals(42, cannon.getIdTile(), "id del tile");
    }

    @Test
    @DisplayName("Cannon singolo")
    void testSingleCannon() {
        Cannon cannon = new Cannon(7, 0, 5, 0, false, 99);

        assertFalse(cannon.isDouble(), "dev'essere singolo");
        assertEquals(7, cannon.controlCorners(0), "angolo nord");
        assertEquals(0, cannon.controlCorners(1), "angolo est");
        assertEquals(5, cannon.controlCorners(2), "angolo sud");
        assertEquals(0, cannon.controlCorners(3), "angolo ovest");
        assertEquals(99, cannon.getIdTile());
    }

    @Test
    @DisplayName("controlCorners lancia se index fuori range")
    void testControlCornersOutOfBounds() {
        Cannon cannon = new Cannon(1,2,3,4, true, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> cannon.controlCorners(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cannon.controlCorners(4));
    }
}

