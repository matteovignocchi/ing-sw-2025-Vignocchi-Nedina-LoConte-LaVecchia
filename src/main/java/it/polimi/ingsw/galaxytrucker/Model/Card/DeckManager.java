package it.polimi.ingsw.galaxytrucker.Model.Card;

import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The DeckManager class is responsible for creating playable decks of adventure cards using cards loaded
 * via the CardGeneration class. This class can be used by the game controller to generate shuffled decks.
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */

public class DeckManager implements Serializable {
    private final CardGeneration generator;

    /**
     * Constructs a new DeckManager by initializing a CardGeneration instance to load cards from JSON resources.
     * @throws IOException if loading card data files fails.
     */

    public DeckManager() throws IOException {
        this.generator = new CardGeneration();
    }

    /**
     * Creates a demo deck to use for simplified or demo games.
     * The deck is made up of all the cards in the demo JSON, shuffled randomly.
     *
     * @return a "Deck" object containing the shuffled demo cards.
     */

    public Deck CreateDemoDeck() {
        List<Card> special = new ArrayList<>(generator.getDemoCards());
        Collections.shuffle(special);
        Deck deck = new Deck();
        deck.addAll(special);
        return deck;
    }

    /**
     * Creates 4 "Second level" decks, each consisting of 2 level 2 cards and 1 level 1 card.
     * The method ensures that the cards are shuffled before generating the deck.
     * @return a "List<Deck>" containing 4 individual decks
     * @throws CardEffectException if there are not enough cards to generate the decks
     */

    public List<Deck> CreateSecondLevelDeck() throws CardEffectException {
        int numberOfDecks = 4;
        List<Card> level1 = new ArrayList<>(generator.getLevel1Cards());
        List<Card> level2 = new ArrayList<>(generator.getLevel2Cards());
        List<Deck> decks = new ArrayList<>();

        Collections.shuffle(level1);
        Collections.shuffle(level2);

        if (level2.size() < numberOfDecks * 2 || level1.size() < numberOfDecks) {
            throw new InvalidSizeException("Not enough cards to create 4 decks");
        }

        for (int i = 0; i < numberOfDecks; i++) {
            Deck deck = new Deck();
            deck.add(level2.removeFirst());
            deck.add(level2.removeFirst());
            deck.add(level1.removeFirst());
            deck.shuffle();
            decks.add(deck);
        }
        return decks;
    }
}
