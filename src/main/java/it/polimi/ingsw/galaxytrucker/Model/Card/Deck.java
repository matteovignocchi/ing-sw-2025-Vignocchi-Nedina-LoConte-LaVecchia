package it.polimi.ingsw.galaxytrucker.Model.Card;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a deck of Card objects.
 * The deck follows a FIFO (First-In-First-Out) logic using a LinkedList internally.
 * This class is used to organize and manage the adventure cards drawn during the game.
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */

public class Deck  implements Serializable {
    private final Queue<Card> cards;

    /**
     * Constructs an empty deck.
     */

    public Deck() {
        this.cards = new LinkedList<>();
    }

    /**
     * Adds a single card to the deck if it is not null.
     *
     * @param card the Card to be added.
     */

    public void add(Card card) {
        if(card != null) {
            cards.offer(card);
        }
    }

    /**
     * Adds a collection of cards to the deck.
     * Ignores null cards inside the collection.
     *
     * @param cards: a Collection of Card objects to add.
     * @throws IllegalArgumentException: if the collection itself is null.
     */

    public void addAll(Collection<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("Card collection cannot be null");

        for (Card card : cards) {
            add(card);
        }
    }

    /**
     * Randomly shuffles the order of the cards in the deck.
     */

    public void shuffle() {
        List<Card> temp = new ArrayList<>(cards);
        Collections.shuffle(temp);
        cards.clear();
        cards.addAll(temp);
    }

    /**
     * Draws the card at the front of the deck and removes it.
     *
     * @return the next Card in the deck.
     * @throws InvalidSizeException: if the deck is empty.
     */

    public Card draw() throws CardEffectException{
        if(cards.isEmpty()) throw new InvalidSizeException("The deck is empty");
        return cards.poll();
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck contains no cards, false otherwise.
     */

    public boolean isEmpty() {return cards.isEmpty();}

    /**
     * Returns the number of cards currently in the deck.
     *
     * @return the size of the deck.
     */

    public int size() {return cards.size();}

    /**
     * Returns a copy of the cards currently in the deck.
     *
     * @return a List of the cards in the deck.
     */

    public List<Card> getCards() {return new ArrayList<>(cards);}
}