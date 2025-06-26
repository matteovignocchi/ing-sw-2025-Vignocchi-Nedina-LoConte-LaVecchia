package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.SecondWarzoneCard;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecondWarzoneCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SecondWarzoneCard(null, 1, 1, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new SecondWarzoneCard(" ", 1, 1, List.of(1), List.of(true)));
    }

    @Test
    void ctor_badLists_throws() {
        assertThrows(NullPointerException.class, () -> new SecondWarzoneCard("S", 1, 1, null, List.of(true)));
        assertThrows(NullPointerException.class, () -> new SecondWarzoneCard("S", 1, 1, Collections.emptyList(), List.of(true)));
        assertThrows(NullPointerException.class, () -> new SecondWarzoneCard("S", 1, 1, List.of(1), null));
        assertThrows(NullPointerException.class, () -> new SecondWarzoneCard("S", 1, 1, List.of(1), Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new SecondWarzoneCard("S", 1, 1, List.of(1,2), List.of(true)));
    }

    @Test
    void ctor_nonPositiveDaysOrGoods_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SecondWarzoneCard("S", 0, 1, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new SecondWarzoneCard("S", 1, 0, List.of(1), List.of(true)));
    }

    @Test
    void getters_returnCopy_and_basic() {
        List<Integer> dirs = List.of(5, 7);
        List<Boolean> sizes = List.of(false, true);
        SecondWarzoneCard card = new SecondWarzoneCard("W1", 3, 4, dirs, sizes);

        assertEquals("W1", card.getIdCard());
        assertEquals(3, card.getDays());
        assertEquals(4, card.getNumGoods());

        var outDirs = card.getShotsDirections();
        var outSizes = card.getShotsSize();
        assertEquals(dirs, outDirs);
        assertEquals(sizes, outSizes);

        outDirs.set(0, 99);
        outSizes.set(1, false);
        assertEquals(dirs, card.getShotsDirections());
        assertEquals(sizes, card.getShotsSize());
    }

    @Test
    void accept_invokesVisit_and_wrapsBLException() throws BusinessLogicException {
        SecondWarzoneCard card = new SecondWarzoneCard("W2", 2, 2, List.of(9), List.of(false));
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        BusinessLogicException ble = new BusinessLogicException("blip");
        try {
            doThrow(ble).when(visitor).visit(card);
        } catch (BusinessLogicException ignored) {}

        BusinessLogicException thrown = assertThrows(
                BusinessLogicException.class,
                () -> card.accept(visitor)
        );
        assertSame(ble, thrown);
    }
}
