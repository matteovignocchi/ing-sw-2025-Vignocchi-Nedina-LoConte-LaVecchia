package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.FirstWarzoneCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirstWarzoneCardTest {

    @Test
    void constructor_and_getters_work() {
        String id = "FWZ";
        int days = 3, crew = 2;
        var dirs  = List.of(0, 1, 2);
        var sizes = List.of(true, false, true);

        FirstWarzoneCard card = new FirstWarzoneCard(id, days, crew, dirs, sizes);

        assertEquals(id,   card.getIdCard());
        assertEquals(days, card.getDays());
        assertEquals(crew, card.getNumCrewmates());
        assertEquals(dirs,  card.getShotsDirections());
        assertEquals(sizes, card.getShotsSize());

        assertNotSame(dirs,  card.getShotsDirections());
        assertNotSame(sizes, card.getShotsSize());
    }

    @Test
    void illegal_arguments_in_constructor_throw() {
        var goodDirs  = List.of(5);
        var goodSizes = List.of(false);

        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard(null, 1, 1, goodDirs, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("",   1, 1, goodDirs, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard(" ",  1, 1, goodDirs, goodSizes));

        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, 1, null, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, 1, goodDirs, null));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, 1, List.of(),  List.of()));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, 1, List.of(1,2), List.of(true)));

        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 0, 1, goodDirs, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, 0, goodDirs, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", -1,1, goodDirs, goodSizes));
        assertThrows(IllegalArgumentException.class, () -> new FirstWarzoneCard("X", 1, -5, goodDirs, goodSizes));
    }

    @Test
    void accept_shouldInvokeVisitOnVisitor() throws BusinessLogicException {
        FirstWarzoneCard card = new FirstWarzoneCard("F", 1, 1, List.of(0), List.of(true));
        CardVisitor visitor = mock(CardVisitor.class);

        card.accept(visitor);

        verify(visitor, times(1)).visit(card);
    }

    @Test
    void accept_whenVisitorThrowsBusinessLogicException_shouldWrapInRuntimeException() throws BusinessLogicException {
        FirstWarzoneCard card = new FirstWarzoneCard("F", 1, 1, List.of(0), List.of(true));

        CardVisitor visitor = mock(CardVisitor.class);
        BusinessLogicException ble = new BusinessLogicException("boom");
        doThrow(ble).when(visitor).visit(card);

        RuntimeException rex = assertThrows(
                RuntimeException.class,
                () -> card.accept(visitor),
                "se visit lancia BusinessLogicException, accept deve rilanciare RuntimeException"
        );
        assertSame(
                ble,
                rex.getCause(),
                "il cause della RuntimeException dev'essere il BusinessLogicException originale"
        );
    }

}

