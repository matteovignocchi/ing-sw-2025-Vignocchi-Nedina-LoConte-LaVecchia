package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Server.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.Deck;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.DeckManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckManagerTest {

    private DeckManager deckManager;

    @BeforeEach
    void setUp() throws IOException {
        deckManager = new DeckManager();
    }

    @Test
    void testCreateDemoDeck() {
        Deck demoDeck = deckManager.CreateDemoDeck();
        assertNotNull(demoDeck);
        assertFalse(demoDeck.isEmpty());
    }

    @Test
    void testCreateSecondLevelDeck() throws CardEffectException {
        List<Deck> decks = deckManager.CreateSecondLevelDeck();
        assertEquals(4, decks.size());
        for (Deck deck : decks) {
            assertEquals(3, deck.size());
        }
    }
}
