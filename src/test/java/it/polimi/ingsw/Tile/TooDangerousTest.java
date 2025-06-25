package it.polimi.ingsw.Tile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.TooDangerous;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TooDangerousTest {

    @Test
    @DisplayName("Il messaggio passato al costruttore deve essere recuperabile via getMessage()")
    void testMessageIsStored() {
        String msg = "Attenzione: troppa instabilità!";
        TooDangerous ex = new TooDangerous(msg);
        assertEquals(msg, ex.getMessage(), "getMessage() deve restituire esattamente la stringa passata al costruttore");
    }

    @Test
    @DisplayName("toString() include il nome della classe e il messaggio")
    void testToStringContainsClassNameAndMessage() {
        String msg = "explode";
        TooDangerous ex = new TooDangerous(msg);
        String s = ex.toString();

        assertTrue(s.contains("TooDangerous"), "toString() dovrebbe contenere il nome della classe");
        assertTrue(s.contains(msg), "toString() dovrebbe contenere il messaggio dell'eccezione");
    }

    @Test
    @DisplayName("Si può lanciare e catturare come RuntimeException")
    void testThrowAndCatch() {assertThrows(TooDangerous.class, () -> {
            throw new TooDangerous("pericolo");
        }, "dovrebbe essere lanciabile e catturabile come TooDangerous");
    }
}

