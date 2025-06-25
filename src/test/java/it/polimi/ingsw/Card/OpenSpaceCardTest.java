package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.OpenSpaceCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenSpaceCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new OpenSpaceCard(null));
        assertThrows(IllegalArgumentException.class, () -> new OpenSpaceCard(" "));
    }

    @Test
    void getIdCard_returnsIt() {
        OpenSpaceCard card = new OpenSpaceCard("OX1");
        assertEquals("OX1", card.getIdCard());
    }

    @Test
    void accept_invokesVisit() throws BusinessLogicException {
        OpenSpaceCard card = new OpenSpaceCard("OX2");
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);
    }

    @Test
    void accept_wrapsBusinessLogicException() {
        OpenSpaceCard card = new OpenSpaceCard("OX3");
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("boom");
        try {
            doThrow(ble).when(visitor).visit(card);
        } catch(BusinessLogicException ign) {}

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                card.accept(visitor)
        );
        assertSame(ble, ex.getCause());
    }
}

