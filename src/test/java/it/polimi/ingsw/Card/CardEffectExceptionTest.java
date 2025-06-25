package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardEffectExceptionTest {

    static class DummyException extends CardEffectException {
        DummyException(String msg) {super(msg);}
    }

    @Test
    void message_propagation() {
        DummyException ex = new DummyException("mi sono sbagliato");
        assertEquals("mi sono sbagliato", ex.getMessage(), "getMessage() deve restituire il messaggio passato al costruttore");
    }

    @Test
    void stacktrace_available() {
        DummyException ex = new DummyException("x");
        StackTraceElement[] stack = ex.getStackTrace();
        assertNotNull(stack);
        assertTrue(stack.length > 0, "getStackTrace() deve contenere almeno un elemento");
    }
}

