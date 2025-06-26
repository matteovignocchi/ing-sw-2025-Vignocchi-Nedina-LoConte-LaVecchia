package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptyHousingList;
import it.polimi.ingsw.galaxytrucker.Model.Tile.HousingUnit;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Human;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HousingUnitTest {

    private HousingUnit humanUnit;
    private HousingUnit brownAlienUnit;
    private HousingUnit purpleAlienUnit;

    @BeforeEach
    void setUp() {
        humanUnit       = new HousingUnit(1,2,3,4, Human.HUMAN,       10);
        brownAlienUnit  = new HousingUnit(1,2,3,4, Human.BROWN_ALIEN, 11);
        purpleAlienUnit = new HousingUnit(1,2,3,4, Human.PURPLE_ALIEN,12);
    }

    @Test
    @DisplayName("Costruttore imposta a, b, c, d, tipo, id e max corretti")
    void testConstructorAndGetters() {
        assertEquals(Human.HUMAN, humanUnit.getType());
        assertEquals(Human.HUMAN234, humanUnit.getTypeOfConnections());
        assertEquals(10, humanUnit.getIdTile());

        assertEquals(Human.BROWN_ALIEN, brownAlienUnit.getType());
        assertEquals(Human.BROWN_ALIEN, brownAlienUnit.getTypeOfConnections());
        assertEquals(11, brownAlienUnit.getIdTile());

        assertEquals(Human.PURPLE_ALIEN, purpleAlienUnit.getType());
        assertEquals(Human.PURPLE_ALIEN, purpleAlienUnit.getTypeOfConnections());
        assertEquals(12, purpleAlienUnit.getIdTile());

        assertArrayEquals(new int[]{1,2,3,4}, humanUnit.getCorners());
    }

    @Test
    @DisplayName("addHuman e returnLength funzionano per umano")
    void testAddAndRemoveHuman() throws Exception {
        assertEquals(0, humanUnit.returnLenght());
        humanUnit.addHuman(Human.HUMAN);
        assertEquals(1, humanUnit.returnLenght());
        int code = humanUnit.removeHumans(0);
        assertEquals(1, code);
        assertEquals(0, humanUnit.returnLenght());
    }

    @Test
    @DisplayName("addHuman e removeHumans gestiscono alieni con codice corretto")
    void testAddAndRemoveAlien() throws Exception {
        brownAlienUnit.addHuman(Human.BROWN_ALIEN);
        assertEquals(1, brownAlienUnit.returnLenght());
        assertEquals(2, brownAlienUnit.removeHumans(0));
        assertEquals(0, brownAlienUnit.returnLenght());

        purpleAlienUnit.addHuman(Human.PURPLE_ALIEN);
        assertEquals(1, purpleAlienUnit.returnLenght());
        assertEquals(3, purpleAlienUnit.removeHumans(0));
        assertEquals(0, purpleAlienUnit.returnLenght());
    }

    @Test
    @DisplayName("removeHumans su lista vuota lancia EmptyHousingList")
    void testRemoveHumansEmptyThrows() {
        assertTrue(humanUnit.getListOfToken().isEmpty());
        assertThrows(EmptyHousingList.class, () -> humanUnit.removeHumans(0));
    }

    @Test
    @DisplayName("setConnected / isConnected e setTypeOfConnections / getTypeOfConnections")
    void testConnectionAndTypeOfConnections() {
        assertFalse(humanUnit.isConnected());
        humanUnit.setConnected(true);
        assertTrue(humanUnit.isConnected());

        humanUnit.setTypeOfConnections(Human.BROWN_ALIEN);
        assertEquals(Human.BROWN_ALIEN, humanUnit.getTypeOfConnections());
    }

    @Test
    @DisplayName("getListOfToken restituisce la lista interna")
    void testGetListOfToken() throws BusinessLogicException {
        humanUnit.addHuman(Human.HUMAN);
        List<Human> list = humanUnit.getListOfToken();
        assertEquals(1, list.size());
        assertSame(Human.HUMAN, list.get(0));
    }

    @Test
    @DisplayName("setSize imposta correttamente il campo max")
    void testSetSizeViaReflection() throws Exception {
        Field maxField = HousingUnit.class.getDeclaredField("max");
        maxField.setAccessible(true);
        assertEquals(2, maxField.getInt(humanUnit));

        humanUnit.setSize(7);
        assertEquals(7, maxField.getInt(humanUnit));
    }
}

