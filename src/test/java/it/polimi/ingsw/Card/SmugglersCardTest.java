package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.SmugglersCard;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmugglersCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard(null, 1, 1, 1, List.of(Colour.BLUE)));
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("  ", 1, 1, 1, List.of(Colour.BLUE)));
    }

    @Test
    void ctor_nonPositiveFields_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("S", 0, 1, 1, List.of(Colour.RED)));
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("S", 1, 0, 1, List.of(Colour.RED)));
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("S", 1, 1, 0, List.of(Colour.RED)));
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("S", 1, 1, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new SmugglersCard("S", 1, 1, 1, List.of()));
    }

    @Test
    void getters_returnCorrectValues_andDeepCopy() {
        List<Colour> rewards = new ArrayList<>(List.of(Colour.GREEN, Colour.RED));
        SmugglersCard card = new SmugglersCard("M1", 2, 3, 4, rewards);

        assertEquals("M1", card.getIdCard());
        assertEquals(2, card.getDays());
        assertEquals(3, card.getFirePower());
        assertEquals(4, card.getNumRemovedGoods());
        assertEquals(List.of(Colour.GREEN, Colour.RED), card.getRewardGoods());

        rewards.set(0, Colour.BLUE);
        assertEquals(List.of(Colour.GREEN, Colour.RED), card.getRewardGoods());

        List<Colour> fetched = card.getRewardGoods();
        fetched.add(Colour.YELLOW);
        assertEquals(List.of(Colour.GREEN, Colour.RED), card.getRewardGoods());
    }

    @Test
    void accept_invokesVisit_and_wrapsBLException() throws BusinessLogicException {
        SmugglersCard card = new SmugglersCard("M2", 1, 2, 3, List.of(Colour.BLUE));
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("fail");

        doNothing()
                .doThrow(ble)
                .when(visitor).visit(card);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        RuntimeException rex = assertThrows(RuntimeException.class,
                () -> card.accept(visitor));
        assertSame(ble, rex.getCause());
    }
}

