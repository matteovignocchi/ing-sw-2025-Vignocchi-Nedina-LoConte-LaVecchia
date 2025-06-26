package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.AbandonedStationCard;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbandonedStationCardTest {

    @Test
    void constructor_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard(null, 1, 1, List.of(Colour.RED)), "idCard null should throw");
    }

    @Test
    void constructor_blankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("   ", 1, 1, List.of(Colour.RED)), "blank idCard should throw");
    }

    @Test
    void constructor_nullGoods_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", 1, 1, null), "null station_goods should throw");
    }

    @Test
    void constructor_emptyGoods_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", 1, 1, Collections.emptyList()), "empty station_goods should throw");
    }

    @Test
    void constructor_nonPositiveNumCrew_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", 0, 1, List.of(Colour.BLUE)), "num_crewmates ≤ 0 should throw");
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", -5, 1, List.of(Colour.BLUE)), "negative num_crewmates should throw");
    }

    @Test
    void constructor_nonPositiveDays_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", 1, 0, List.of(Colour.GREEN)), "days ≤ 0 should throw");
        assertThrows(IllegalArgumentException.class, () -> new AbandonedStationCard("ID", 1, -3, List.of(Colour.GREEN)), "negative days should throw");
    }

    @Test
    void getters_returnConstructorValues() {
        List<Colour> goods = List.of(Colour.YELLOW, Colour.RED);
        AbandonedStationCard c = new AbandonedStationCard("CARD99", 4, 7, goods);

        assertEquals("CARD99", c.getIdCard());
        assertEquals(4, c.getNumCrewmates());
        assertEquals(7, c.getDays());

        List<Colour> returned = c.getStationGoods();
        assertEquals(goods, returned);
        assertNotSame(goods, returned);
    }

    @Test
    void accept_callsVisitorVisit() throws Exception {
        CardVisitor visitor = mock(CardVisitor.class);
        AbandonedStationCard c = new AbandonedStationCard("X1", 2, 3, List.of(Colour.BLUE));

        doNothing().when(visitor).visit(c);

        c.accept(visitor);
        verify(visitor, times(1)).visit(c);
    }

    @Test
    void accept_wrapsBusinessLogicException_Station() throws BusinessLogicException {
        CardVisitor visitor = mock(CardVisitor.class);
        AbandonedStationCard c = new AbandonedStationCard("X2", 5, 9, List.of(Colour.GREEN));

        BusinessLogicException ble = new BusinessLogicException("fail");
        doThrow(ble).when(visitor).visit(c);

        BusinessLogicException thrown = assertThrows(
                BusinessLogicException.class,
                () -> c.accept(visitor),
                "se visit lancia BusinessLogicException, accept deve rilanciare lo stesso BusinessLogicException"
        );
        assertSame(
                ble,
                thrown,
                "l'eccezione rilanciata deve essere esattamente quella originale"
        );
    }
}

