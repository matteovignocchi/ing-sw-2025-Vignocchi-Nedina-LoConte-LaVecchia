package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EnergyCell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnergyCellTest {

    @Test
    @DisplayName("costruttore e getter capacity funzionano")
    void testConstructorAndGetCapacity() {
        EnergyCell cell = new EnergyCell(0,1,2,3, 5, 42);
        assertEquals(5, cell.getCapacity(), "capacity iniziale deve essere 5");
        assertEquals(42, cell.getIdTile(), "idTile deve essere settato correttamente");
        // i quattro angoli
        assertEquals(0, cell.controlCorners(0));
        assertEquals(1, cell.controlCorners(1));
        assertEquals(2, cell.controlCorners(2));
        assertEquals(3, cell.controlCorners(3));
    }

    @Test
    @DisplayName("useBattery diminuisce capacity e restituisce true finché >0")
    void testUseBattery() {
        EnergyCell cell = new EnergyCell(0,0,0,0, 2, 1);
        assertTrue(cell.useBattery(), "prima chiamata: capacity 2->1, restituisce true");
        assertEquals(1, cell.getCapacity());
        assertTrue(cell.useBattery(), "seconda: 1->0, restituisce true");
        assertEquals(0, cell.getCapacity());
        assertFalse(cell.useBattery(), "terza: capacity già 0, restituisce false");
        assertEquals(0, cell.getCapacity(), "capacity resta 0");
    }
}

