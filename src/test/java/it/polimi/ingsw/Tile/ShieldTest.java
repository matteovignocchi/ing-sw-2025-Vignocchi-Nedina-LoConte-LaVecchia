package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Shield;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShieldTest {
    private Shield shield;

    @BeforeEach
    void setUp() {
        shield = new Shield(1,2,3,4, 99);
    }

    @Test
    void testConstructorInitializesCornersAndProtected() {
        assertArrayEquals(new int[]{1,2,3,4}, shield.getCorners());
        assertEquals(99, shield.getIdTile());
        assertEquals(8, shield.getProtectedCorner(0));
        assertEquals(8, shield.getProtectedCorner(1));
        assertEquals(0, shield.getProtectedCorner(2));
        assertEquals(0, shield.getProtectedCorner(3));
    }

    @Test
    void testRotateRightRotatesBothArrays() {
        int[] oldCorners = shield.getCorners().clone();
        int[] oldProtected = {shield.getProtectedCorner(0),shield.getProtectedCorner(1),
                shield.getProtectedCorner(2),shield.getProtectedCorner(3)};
        shield.rotateRight();
        assertArrayEquals(new int[]{oldCorners[3],oldCorners[0],oldCorners[1],oldCorners[2]}, shield.getCorners());
        assertEquals(oldProtected[3], shield.getProtectedCorner(0));
        assertEquals(oldProtected[0], shield.getProtectedCorner(1));
        assertEquals(oldProtected[1], shield.getProtectedCorner(2));
        assertEquals(oldProtected[2], shield.getProtectedCorner(3));
    }

    @Test
    void testRotateLeftRotatesBothArrays() {
        int[] oldCorners = shield.getCorners().clone();
        int[] oldProtected = {shield.getProtectedCorner(0),shield.getProtectedCorner(1),
                shield.getProtectedCorner(2),shield.getProtectedCorner(3)};
        shield.rotateLeft();
        assertArrayEquals(new int[]{oldCorners[1],oldCorners[2],oldCorners[3],oldCorners[0]}, shield.getCorners());
        assertEquals(oldProtected[1], shield.getProtectedCorner(0));
        assertEquals(oldProtected[2], shield.getProtectedCorner(1));
        assertEquals(oldProtected[3], shield.getProtectedCorner(2));
        assertEquals(oldProtected[0], shield.getProtectedCorner(3));
    }

    @Test
    void testGetAndSetProtectedCorner() {
        shield.setProtectedCorner(2, 42);
        assertEquals(42, shield.getProtectedCorner(2));
    }
}

