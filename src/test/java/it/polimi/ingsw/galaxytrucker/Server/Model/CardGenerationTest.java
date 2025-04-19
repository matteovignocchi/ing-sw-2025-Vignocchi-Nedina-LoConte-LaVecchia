package it.polimi.ingsw.galaxytrucker.Server.Model;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.CardGeneration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardGenerationTest {

    private CardGeneration cardGeneration;

    @BeforeEach
    void setUp() throws IOException {
        cardGeneration = new CardGeneration();
    }

    @Test
    void testLevel1CardsLoaded() {
        List<Card> cards = cardGeneration.getLevel1Cards();
        assertNotNull(cards);
        assertFalse(cards.isEmpty(), "Level 1 cards should not be empty");

        for(Card card: cards){
            System.out.println(card);
        }
    }

    @Test
    void testLevel2CardsLoaded() {
        List<Card> cards = cardGeneration.getLevel2Cards();
        assertNotNull(cards);
        assertFalse(cards.isEmpty(), "Level 2 cards should not be empty");

        for(Card card: cards){
            System.out.println(card);
        }
    }

    @Test
    void testDemoCardsLoaded() {
        List<Card> cards = cardGeneration.getDemoCards();
        assertNotNull(cards);
        assertFalse(cards.isEmpty(), "Demo cards should not be empty");

        for(Card card: cards){
            System.out.println(card);
        }
    }

    @Test
    @DisplayName("loadCards() lancia IOException se il file non esiste")
    void testLoadCardsWithInvalidFile() throws IOException {
        CardGeneration cardGeneration = new CardGeneration();
        assertThrows(IOException.class, () -> {
            cardGeneration.loadCards("file_non_esistente.json", new ObjectMapper());
        });
    }
}