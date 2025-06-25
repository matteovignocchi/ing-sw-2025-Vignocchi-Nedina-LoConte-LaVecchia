package it.polimi.ingsw.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private Deck deck;
    private final Card shipCard = new AbandonedShipCard("X",1,1,1);
    private final Card stationCard = new AbandonedStationCard("Y",1,1,List.of(Colour.BLUE));

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    void newDeck_isEmpty() {
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
        assertTrue(deck.getCards().isEmpty());
    }

    @Test
    void add_nullIgnored() {
        deck.add(null);
        assertTrue(deck.isEmpty());
    }

    @Test
    void add_nonNull() throws Exception {
        deck.add(shipCard);
        assertFalse(deck.isEmpty());
        assertEquals(1, deck.size());
        assertSame(shipCard, deck.draw());
    }

    @Test
    void addAll_nullCollection_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> deck.addAll(null));
    }

    @Test
    void addAll_ignoresNullElements() throws Exception {
        var list = new ArrayList<Card>();
        list.add(shipCard);
        list.add(null);
        list.add(stationCard);
        deck.addAll(list);
        assertEquals(2, deck.size());
        assertEquals(List.of(shipCard, stationCard), deck.getCards());
    }

    @Test
    void shuffle_preservesContents() {
        deck.add(shipCard);
        deck.add(stationCard);
        var before = deck.getCards();
        deck.shuffle();
        var after = deck.getCards();
        assertEquals(2, after.size());
        assertTrue(after.containsAll(before));
    }

    @Test
    void draw_empty_throws() {
        assertThrows(CardEffectException.class, () -> deck.draw());
    }

    @Test
    void draw_orderAndRemoval() throws Exception {
        deck.add(shipCard);
        deck.add(stationCard);
        assertSame(shipCard, deck.draw());
        assertEquals(1, deck.size());
        assertSame(stationCard, deck.draw());
        assertTrue(deck.isEmpty());
    }

    @Test
    void getCards_returnsCopy() {
        deck.add(shipCard);
        var copy = deck.getCards();
        assertEquals(1, copy.size());
        copy.clear();
        assertEquals(1, deck.size(), "clear() sulla copia non deve modificare il mazzo");
    }
}


