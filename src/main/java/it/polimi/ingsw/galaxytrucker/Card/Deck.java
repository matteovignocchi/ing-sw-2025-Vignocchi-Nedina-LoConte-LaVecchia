package it.polimi.ingsw.galaxytrucker.Card;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    protected List<Card> cards;

    public Deck() {
        cards = new LinkedList<>();

        //instanziazione oggetti della lista e inserimento in lista

        Collections.shuffle(cards);
    }

    public Card draw(){return cards.removeFirst();}

    public List<Card> getDeck(){ return new LinkedList<>(cards);}

}
