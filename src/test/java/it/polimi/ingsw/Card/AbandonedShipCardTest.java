package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.AbandonedShipCard;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbandonedShipCardTest {

    @Test
    void constructor_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard(null, 1, 1, 1), "idCard null should throw");
    }

    @Test
    void constructor_blankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("   ", 1, 1, 1), "blank idCard should throw");
    }

    @Test
    void constructor_nonPositiveNumCrew_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", 1, 1, 0), "num_crewmates ≤ 0 should throw");
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", 1, 1, -5), "negative num_crewmates should throw");
    }

    @Test
    void constructor_nonPositiveDays_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", 0, 1, 1), "days ≤ 0 should throw");
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", -3, 1, 1), "negative days should throw");
    }

    @Test
    void constructor_nonPositiveCredits_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", 1, 0, 1), "credits ≤ 0 should throw");
        assertThrows(IllegalArgumentException.class, () -> new AbandonedShipCard("ID", 1, -7, 1), "negative credits should throw");
    }

    @Test
    void getters_returnConstructorValues() {
        AbandonedShipCard c = new AbandonedShipCard("CARD42", 8, 17, 3);
        assertEquals("CARD42", c.getIdCard());
        assertEquals(8, c.getDays());
        assertEquals(17, c.getCredits());
        assertEquals(3, c.getNumCrewmates());
    }

    @Test
    void accept_invokesVisitorVisit() throws Exception {
        CardVisitor visitor = mock(CardVisitor.class);
        AbandonedShipCard c = new AbandonedShipCard("X", 2, 5, 1);

        doNothing().when(visitor).visit(c);
        c.accept(visitor);

        verify(visitor, times(1)).visit(c);
    }

    @Test
    void accept_wrapsBusinessLogicExceptionInRuntime() throws Exception {
        CardVisitor visitor = mock(CardVisitor.class);
        AbandonedShipCard c = new AbandonedShipCard("X", 2, 5, 1);

        doThrow(new BusinessLogicException("boom")).when(visitor).visit(c);

        RuntimeException re = assertThrows(RuntimeException.class,
                () -> c.accept(visitor), "se visit lancia BusinessLogicException, accept deve rilanciare RuntimeException");
        assertTrue(re.getCause() instanceof BusinessLogicException, "il cause della RuntimeException dev'essere il BusinessLogicException originale");
    }
}


