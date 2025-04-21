package it.polimi.ingsw.galaxytrucker.Model.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckManager {
    private final CardGeneration generator;

    public DeckManager() throws IOException {
        this.generator = new CardGeneration();
    }

    public Deck CreateDemoDeck() {
        List<Card> special = new ArrayList<>(generator.getDemoCards());
        Collections.shuffle(special);
        Deck deck = new Deck();
        deck.addAll(special);
        return deck;
    }

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
