package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.PlagueCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlagueCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlagueCard(null));
        assertThrows(IllegalArgumentException.class, () -> new PlagueCard(" "));
    }

    @Test
    void getIdCard_returnsIt() {
        PlagueCard card = new PlagueCard("Z1");
        assertEquals("Z1", card.getIdCard());
    }

    @Test
    void accept_invokesVisit_and_propagatesBLException() throws BusinessLogicException {
        PlagueCard card = new PlagueCard("Z2");
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        BusinessLogicException ble = new BusinessLogicException("oops");
        doThrow(ble).when(visitor).visit(card);

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () -> card.accept(visitor));
        assertSame(ble, ex);
    }
}

