package it.polimi.ingsw;
import it.polimi.ingsw.galaxytrucker.Model.InvalidIndex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidIndexTest {

    @Test
    void testIsRuntimeException() {
        InvalidIndex ex = new InvalidIndex("indice non valido");
        assertTrue(ex instanceof RuntimeException,
                "InvalidIndex deve estendere RuntimeException");
    }

    @Test
    void testMessageIsPreserved() {
        String msg = "Indice fuori dal range";
        InvalidIndex ex = new InvalidIndex(msg);
        assertEquals(msg, ex.getMessage(),
                "Il messaggio passato al costruttore deve essere restituito da getMessage()");
    }
}
