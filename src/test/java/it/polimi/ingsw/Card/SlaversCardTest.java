package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.SlaversCard;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlaversCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard(null, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard("  ", 1, 1, 1, 1));
    }

    @Test
    void ctor_nonPositiveFields_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard("S", 0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard("S", 1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard("S", 1, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new SlaversCard("S", 1, 1, 1, 0));
    }

    @Test
    void getters_returnCorrectValues() {
        SlaversCard c = new SlaversCard("L1", 2, 5, 3, 4);
        assertEquals("L1", c.getIdCard());
        assertEquals(2, c.getDays());
        assertEquals(5, c.getCredits());
        assertEquals(3, c.getNumCrewmates());
        assertEquals(4, c.getFirePower());
    }

    @Test
    void accept_invokesVisit_and_wrapsBLException() throws BusinessLogicException {
        SlaversCard card = new SlaversCard("L1", 2, 5, 3, 4);
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("boom");

        doNothing()
                .doThrow(ble)
                .when(visitor).visit(card);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        RuntimeException rex = assertThrows(RuntimeException.class, () -> card.accept(visitor));
        assertSame(ble, rex.getCause());
    }
}

