package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckManagerTest {

    DeckManager manager;
    CardGeneration mockGen;
    Card c1, c2;

    @BeforeEach
    void setUp() throws Exception {
        manager = new DeckManager();

        mockGen = mock(CardGeneration.class);

        c1 = new AbandonedShipCard("D1",1,1,1);
        c2 = new AbandonedShipCard("D2",1,1,1);
        when(mockGen.getDemoCards()).thenReturn(List.of(c1, c2));

        List<Card> lvl1 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {lvl1.add(new AbandonedShipCard("L1-"+i,1,1,1));}
        when(mockGen.getLevel1Cards()).thenReturn(lvl1);

        List<Card> lvl2 = new ArrayList<>();
        for (int i = 0; i < 8; i++) {lvl2.add(new AbandonedShipCard("L2-"+i,1,1,1));}
        when(mockGen.getLevel2Cards()).thenReturn(lvl2);

        Field genField = DeckManager.class.getDeclaredField("generator");
        genField.setAccessible(true);
        genField.set(manager, mockGen);
    }

    @Test
    void createDemoDeck_shouldContainAllDemoCards() {
        Deck deck = manager.CreateDemoDeck();
        assertEquals(2, deck.size());

        // Verifico che il mazzo contenga esattamente i due stub c1 e c2
        List<Card> contents = deck.getCards();
        assertTrue(contents.contains(c1), "Il mazzo deve contenere c1");
        assertTrue(contents.contains(c2), "Il mazzo deve contenere c2");
    }

    @Test
    void createSecondLevelDeck_success_path() throws CardEffectException {
        List<Deck> deqs = manager.CreateSecondLevelDeck();
        assertEquals(4, deqs.size());
        deqs.forEach(d -> assertEquals(3, d.size()));
        int sum = deqs.stream().mapToInt(Deck::size).sum();
        assertEquals(12, sum);
    }

    @Test
    void createSecondLevelDeck_notEnough_throws() throws Exception {
        when(mockGen.getLevel1Cards()).thenReturn(List.of(new AbandonedShipCard("only1",1,1,1)));
        when(mockGen.getLevel2Cards()).thenReturn(List.of(new AbandonedShipCard("a",1,1,1)));
        assertThrows(InvalidSizeException.class, () -> manager.CreateSecondLevelDeck());
    }
}

