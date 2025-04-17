package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckManager {
    private final CardGeneration generation;

    public DeckManager(CardGeneration generation) {
        this.generation = generation;
    }

    public Deck FirstLevelDeck() {
        List<Card> special = new ArrayList<>(generation.getDemoCards());
        Collections.shuffle(special);
        Deck deck = new Deck();
        deck.addAll(special);
        return deck;
    }

    public List<Deck> SecondLevelDeck() throws CardEffectException {
        int numberOfDecks = 4;
        List<Card> level1 = new ArrayList<>(generation.getLevel1Cards());
        List<Card> level2 = new ArrayList<>(generation.getLevel2Cards());
        List<Deck> decks = new ArrayList<>();

        Collections.shuffle(level1);
        Collections.shuffle(level2);

        //per sicurezza, ma penso abbastanza inutile
        if (level2.size() < numberOfDecks * 2 || level1.size() < numberOfDecks) {
            throw new InvalidSizeException("Not enough cards to create 4 decks");
        }

        for (int i = 0; i < numberOfDecks; i++) {
            Deck deck = new Deck();
            deck.add(level2.removeFirst());
            deck.add(level2.removeFirst());
            deck.add(level1.removeFirst());
            deck.shuffle(); //rimischio il singolo minideck, ma è omettibile
            decks.add(deck);
        }
        return decks;
    }
}
