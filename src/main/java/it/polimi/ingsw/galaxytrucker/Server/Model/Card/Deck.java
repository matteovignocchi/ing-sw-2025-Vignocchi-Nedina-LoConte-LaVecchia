package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import java.util.LinkedList;
import java.util.List;

/**
 * Deck class description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */
public class Deck {
    protected List<Card> cards;

    public Deck(List<Card> cards) {
        this.cards = new LinkedList<>(cards);
    }

    public Card draw(){return cards.removeFirst();}

    public List<Card> getDeck(){ return new LinkedList<>(cards);}

}
