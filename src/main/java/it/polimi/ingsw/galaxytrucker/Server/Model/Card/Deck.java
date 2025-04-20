package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import java.util.*;

/**
 * Deck class description
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class Deck {
    private final Queue<Card> cards;

    public Deck() {
        this.cards = new LinkedList<>();
    }

    public void add(Card card) {
        if(card != null) {
            cards.offer(card);
        }
    }

    public void addAll(Collection<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("Card collection cannot be null");

        for (Card card : cards) {
            add(card);
        }
    }

    public void shuffle() {
        List<Card> temp = new ArrayList<>(cards);
        Collections.shuffle(temp);
        cards.clear();
        cards.addAll(temp);
    }

    public Card draw() throws CardEffectException{
        if(cards.isEmpty()) throw new InvalidSizeException("The deck is empty");
        return cards.poll();
    }

    public boolean isEmpty() {return cards.isEmpty();}

    public int size() {return cards.size();}

    public List<Card> getCards() {return new ArrayList<>(cards);}
}