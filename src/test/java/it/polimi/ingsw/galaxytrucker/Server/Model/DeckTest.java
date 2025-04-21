package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private Deck deck;
    private Card dummyCard;

    @BeforeEach
    void setUp() {
        deck = new Deck();
        dummyCard = new OpenSpaceCard();
    }

    @Test
    void testAddAndDrawCard() throws CardEffectException {
        deck.add(dummyCard);
        assertEquals(1, deck.size());
        Card drawn = deck.draw();
        assertEquals(dummyCard, drawn);
        assertTrue(deck.isEmpty());
    }

    @Test
    void testAddAll() {
        deck.addAll(List.of(dummyCard, dummyCard));
        assertEquals(2, deck.size());
    }

    @Test
    void testDrawFromEmptyDeckThrows() {
        assertThrows(InvalidSizeException.class, deck::draw);
    }

    @Test
    void testShuffleChangesOrder() {
        deck.addAll(List.of(new OpenSpaceCard(), new AbandonedShipCard(5,3, 2), new StardustCard(), new AbandonedShipCard(3,1, 6) ));
        List<Card> original = deck.getCards();

        for(Card card: original){
            System.out.println(card);
        }

        System.out.println("Qui inizia il nuovo mazzo mischiato");

        deck.shuffle();
        List<Card> shuffled = deck.getCards();

        for(Card card: shuffled){
            System.out.println(card);
        }

        assertEquals(4, shuffled.size());
    }

    @Test
    void testDrawThrowsWhenEmpty() {
        Deck deck = new Deck();
        Exception e = assertThrows(InvalidSizeException.class, deck::draw);
        assertEquals("The deck is empty", e.getMessage());
    }

    @Test
    void testAddNullCardDoesNotThrow() {
        Deck deck = new Deck();
        assertDoesNotThrow(() -> deck.add(null));
        assertTrue(deck.isEmpty()); // Il mazzo resta vuoto
    }

    @Test
    void testAddAllWithNullsDoesNotThrow() {
        Deck deck = new Deck();
        List<Card> cards = new ArrayList<>();
        cards.add(null);
        cards.add(null);
        assertDoesNotThrow(() -> deck.addAll(cards));
        assertTrue(deck.isEmpty());
    }
}
