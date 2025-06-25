package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.PlanetsCard;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanetsCardTest {

    @Test
    void ctor_nullOrBlankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard(null, List.of(List.of(Colour.BLUE)), 1));
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("  ", List.of(List.of(Colour.BLUE)), 1));
    }

    @Test
    void ctor_nullOrEmptyRewardGoods_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("P", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("P", Collections.emptyList(), 1));
    }

    @Test
    void ctor_innerListEmpty_throws() {
        List<List<Colour>> goods = List.of(Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("P", goods, 1));
    }

    @Test
    void ctor_nonPositiveDays_throws() {
        List<List<Colour>> goods = List.of(List.of(Colour.RED));
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("P", goods, 0));
        assertThrows(IllegalArgumentException.class, () -> new PlanetsCard("P", goods, -5));
    }

    @Test
    void getters_returnDeepCopy() {
        List<Colour> inner1 = new ArrayList<>(List.of(Colour.BLUE));
        List<Colour> inner2 = new ArrayList<>(List.of(Colour.GREEN, Colour.RED));
        List<List<Colour>> goods = new ArrayList<>();
        goods.add(inner1);
        goods.add(inner2);

        PlanetsCard card = new PlanetsCard("PL", goods, 7);

        List<List<Colour>> fetched = card.getRewardGoods();
        assertEquals(2, fetched.size());
        assertEquals(List.of(Colour.BLUE), fetched.get(0));
        assertEquals(List.of(Colour.GREEN, Colour.RED), fetched.get(1));

        fetched.get(0).set(0, Colour.YELLOW);
        fetched.get(1).add(Colour.YELLOW);

        List<List<Colour>> reFetched = card.getRewardGoods();
        assertEquals(List.of(Colour.BLUE), reFetched.get(0));
        assertEquals(List.of(Colour.GREEN, Colour.RED), reFetched.get(1));
    }

    @Test
    void getDays_and_getIdCard() {
        PlanetsCard card = new PlanetsCard("X1", List.of(List.of(Colour.YELLOW)), 3);
        assertEquals(3, card.getDays());
        assertEquals("X1", card.getIdCard());
    }

    @Test
    void accept_invokesVisit_and_wrapsBLException() throws BusinessLogicException {
        PlanetsCard card = new PlanetsCard("X2", List.of(List.of(Colour.GREEN)), 2);
        CardVisitor visitor = mock(CardVisitor.class);

        BusinessLogicException ble = new BusinessLogicException("fail");
        doNothing()
                .doThrow(ble)
                .when(visitor).visit(card);

        card.accept(visitor);
        verify(visitor, times(1)).visit(card);

        RuntimeException rex = assertThrows(RuntimeException.class, () -> card.accept(visitor));
        assertSame(ble, rex.getCause());
    }
}
