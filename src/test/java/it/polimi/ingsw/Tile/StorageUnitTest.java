package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.InvalidIndex;
import it.polimi.ingsw.galaxytrucker.Model.Tile.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.Model.Tile.StorageUnit;
import it.polimi.ingsw.galaxytrucker.Model.Tile.TooDangerous;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageUnitTest {
    private StorageUnit advanced;
    private StorageUnit basic;

    @BeforeEach
    void setUp() {
        advanced = new StorageUnit(0,0,0,0, 2, true, 10);
        basic = new StorageUnit(0,0,0,0, 1, false, 20);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(2, advanced.getMax());
        assertTrue(advanced.isAdvanced());
        assertEquals(10, advanced.getIdTile());
        assertEquals(1, basic.getMax());
        assertFalse(basic.isAdvanced());
    }

    @Test
    void testAddGoodAndIsFullAndListSize() throws Exception {
        advanced.addGood(Colour.BLUE);
        advanced.addGood(Colour.GREEN);
        assertEquals(2, advanced.getListSize());
        assertTrue(advanced.isFull());
        List<Colour> goods = advanced.getListOfGoods();
        assertEquals(List.of(Colour.BLUE, Colour.GREEN), goods);
    }

    @Test
    void testAddGoodFullThrows() {
        assertThrows(FullGoodsList.class, () -> {
            basic.addGood(Colour.YELLOW);
            basic.addGood(Colour.BLUE);
        });
    }

    @Test
    void testAddGoodTooDangerousThrows() {
        assertThrows(TooDangerous.class, () -> basic.addGood(Colour.RED));
    }

    @Test
    void testRemoveGoodHappyAndInvalid() throws Exception {
        advanced.addGood(Colour.RED);
        advanced.addGood(Colour.YELLOW);
        assertEquals(Colour.YELLOW, advanced.removeGood(1));
        assertEquals(1, advanced.getListSize());
        assertThrows(IndexOutOfBoundsException.class, () -> advanced.removeGood(5));
    }

    @Test
    void testIsFullAfterRemove() throws Exception {
        basic.addGood(Colour.BLUE);
        assertTrue(basic.isFull());
        basic.removeGood(0);
        assertFalse(basic.isFull());
    }

    @Test
    void testRemoveGoodThrowsInvalidIndexForNullSlot() throws Exception {
        StorageUnit su = new StorageUnit(0,0,0,0, 5, true, 99);
        su.addGood(null);
        assertThrows(InvalidIndex.class, () -> su.removeGood(0));
    }
}
