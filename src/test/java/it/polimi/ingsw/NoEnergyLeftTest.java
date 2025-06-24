package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Model.NoEnergyLeft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoEnergyLeftTest {

    @Test
    void testIsRuntimeException() {
        NoEnergyLeft ex = new NoEnergyLeft("test");
        assertTrue(ex instanceof RuntimeException, "NoEnergyLeft deve estendere RuntimeException");
    }

    @Test
    void testMessageIsPreserved() {
        String msg = "Non hai più energia!";
        NoEnergyLeft ex = new NoEnergyLeft(msg);
        assertEquals(msg, ex.getMessage(), "Il messaggio passato al costruttore deve essere restituito da getMessage()");
    }
}