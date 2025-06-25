package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.StardustCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StardustCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new StardustCard(null));
        assertThrows(IllegalArgumentException.class, () -> new StardustCard("  "));
    }

    @Test
    void getIdCard_returnsValue() {
        StardustCard card = new StardustCard("SD1");
        assertEquals("SD1", card.getIdCard());
    }

    @Test
    void accept_invokesVisit_and_propagatesBLException() throws BusinessLogicException {
        StardustCard card = new StardustCard("SD2");
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("boom");

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        doThrow(ble).when(visitor).visit(card);
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> card.accept(visitor));
        assertSame(ble, ex);
    }
}

