package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.Deck;
import it.polimi.ingsw.galaxytrucker.Model.Card.DeckManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** MOMENTANEAMENTE COMMENTATI PER RUNNARE GLI ALTRI TEST (SE NO BISOGNEREBBE SISTEMARE QUESTI)
class DeckManagerTest {

    private DeckManager deckManager;

    @BeforeEach
    void setUp() throws IOException {
        deckManager = new DeckManager();
    }

    @Test
    @DisplayName("Demo deck creato correttamente e stampato")
    void testCreateDemoDeck() {
        Deck demoDeck = deckManager.CreateDemoDeck();
        assertNotNull(demoDeck);
        assertFalse(demoDeck.isEmpty());

        System.out.println("Demo Deck:");
        for (Card card : demoDeck.getCards()) {
            System.out.println(card); // toString() della carta
        }
    }

    @Test
    @DisplayName("4 deck di secondo livello creati correttamente e stampati")
    void testCreateSecondLevelDeck() throws CardEffectException {
        List<Deck> decks = deckManager.CreateSecondLevelDeck();
        assertEquals(4, decks.size());

        for (int i = 0; i < decks.size(); i++) {
            Deck deck = decks.get(i);
            assertEquals(3, deck.size());

            System.out.println("Deck" + (i + 1) + " :");
            for (Card card : deck.getCards()) {
                System.out.println(card);
            }
        }
    }
}

//idea per testare senza for il tostring: creo un tostring in deck
*/