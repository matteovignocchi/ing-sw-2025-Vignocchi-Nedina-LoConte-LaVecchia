package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.MeteoritesRainCard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeteoritesRainCardTest {

    @Test
    void ctor_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard(null, List.of(1), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("  ", List.of(1), List.of(true)));
    }

    @Test
    void ctor_badLists_throws() {
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("M", null, List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("M", Collections.emptyList(), List.of(true)));
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("M", List.of(1), null));
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("M", List.of(1), Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new MeteoritesRainCard("M", List.of(1,2), List.of(true)));
    }

    @Test
    void getters_returnCopy() {
        List<Integer> dirs = Arrays.asList(1, 2, 3);
        List<Boolean> sizes = Arrays.asList(true, false, true);
        MeteoritesRainCard card = new MeteoritesRainCard("M1", dirs, sizes);

        dirs.set(0, 99);
        sizes.set(1, true);

        List<Integer> outDirs = card.getMeteorites_directions();
        List<Boolean> outSizes = card.getMeteorites_size();

        assertEquals(Arrays.asList(1,2,3), outDirs);
        assertEquals(Arrays.asList(true,false,true), outSizes);

        outDirs.set(1, 42);
        outSizes.set(0, false);
        assertEquals(Arrays.asList(1,2,3), card.getMeteorites_directions());
        assertEquals(Arrays.asList(true,false,true), card.getMeteorites_size());
    }

    @Test
    void accept_invokesVisit() throws BusinessLogicException {
        MeteoritesRainCard card = new MeteoritesRainCard("RZ", List.of(5,7), List.of(true,false));
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);
    }

    @Test
    void accept_propagatesBusinessLogicException() throws BusinessLogicException {
        MeteoritesRainCard card = new MeteoritesRainCard("RZ", List.of(1), List.of(true));
        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("err");
        doThrow(ble).when(visitor).visit(card);

        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () -> card.accept(visitor));
        assertSame(ble, ex);
    }

    @Test
    void getIdCard_returnsCorrectId() {
        List<Integer> dirs = List.of(42);
        List<Boolean> sizes = List.of(false);
        MeteoritesRainCard card = new MeteoritesRainCard("MY_ID", dirs, sizes);

        assertEquals("MY_ID", card.getIdCard());
    }
}

