package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.PiratesCard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PiratesCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard(null, 1,1,1, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard(" ", 1,1,1, List.of(1), List.of(true)));
    }

    @Test
    void ctor_badListsOrParams_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,1, null, List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,1, Collections.emptyList(), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,1, List.of(1), null));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,1, List.of(1), Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,1, List.of(1,2), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 0,1,1, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,0,1, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new PiratesCard("P1", 1,1,0, List.of(1), List.of(true)));
    }

    @Test
    void getters_returnCopy() {
        List<Integer> dirs = Arrays.asList(3,4);
        List<Boolean> sizes = Arrays.asList(false,true);
        PiratesCard card = new PiratesCard("PX", 2,3,4, dirs, sizes);

        dirs.set(0,99);
        sizes.set(1,false);

        assertEquals(List.of(3,4), card.getShots_directions());
        assertEquals(List.of(false,true), card.getShots_size());
        assertEquals(2, card.getFirePower());
        assertEquals(3, card.getDays());
        assertEquals(4, card.getCredits());

        List<Integer> outDirs = card.getShots_directions();
        List<Boolean> outSizes = card.getShots_size();
        outDirs.set(1, 42);
        outSizes.set(0, true);
        assertEquals(List.of(3,4), card.getShots_directions());
        assertEquals(List.of(false,true), card.getShots_size());
    }

    @Test
    void getIdCard_returnsIt() {
        PiratesCard card = new PiratesCard("PR", 1,2,3, List.of(7), List.of(false));
        assertEquals("PR", card.getIdCard());
    }

    @Test
    void accept_invokesVisit() throws BusinessLogicException {
        PiratesCard card = new PiratesCard("PA", 1,2,3, List.of(9), List.of(true));
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);
    }

    @Test
    void accept_wrapsBusinessLogicException() {
        PiratesCard card = new PiratesCard("PB", 1,2,3, List.of(5), List.of(false));
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("fail");
        try {
            doThrow(ble).when(visitor).visit(card);
        } catch(BusinessLogicException ign) {}

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                card.accept(visitor)
        );
        assertSame(ble, ex.getCause());
    }
}

