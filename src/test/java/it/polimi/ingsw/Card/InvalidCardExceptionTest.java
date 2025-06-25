package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.InvalidCardException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCardExceptionTest {

    @Test
    void messageIsPreservedAndIsCardEffectException() {
        String msg = "qualcosa è andato storto";
        InvalidCardException ex = new InvalidCardException(msg);
        assertEquals(msg, ex.getMessage());
        assertTrue(ex instanceof CardEffectException);
    }
}

