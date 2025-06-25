package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileTest {
    private static class StubTile extends Tile {
        StubTile(int id) { this.idTile = id; corners = new int[]{1,2,3,4}; }
    }

    @Test
    void testRotateRightAndLeft() {
        StubTile t = new StubTile(5);
        t.rotateRight();
        assertArrayEquals(new int[]{4,1,2,3}, t.getCorners());
        t.rotateLeft();
        assertArrayEquals(new int[]{1,2,3,4}, t.getCorners());
    }

    @Test
    void testControlCornersAndGetCorners() {
        StubTile t = new StubTile(7);
        assertEquals(1, t.controlCorners(0));
        assertArrayEquals(new int[]{1,2,3,4}, t.getCorners());
        assertEquals(7, t.getIdTile());
    }

    @Test
    void testEqualsAndHashCode() {
        StubTile a = new StubTile(1);
        StubTile b = new StubTile(1);
        StubTile c = new StubTile(2);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
    }

    @Test
    void testLoadImageByIdThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> Tile.loadImageById(9999));
        assertTrue(ex.getMessage().contains("Failed to load tile image for ID: 9999"));
    }

    @Test
    void testGetImageDelegatesToStatic() {
        StubTile t = new StubTile(1234);
        RuntimeException ex = assertThrows(RuntimeException.class, t::getImage);
        assertTrue(ex.getMessage().contains("Failed to load tile image for ID: 1234"));
    }
}

