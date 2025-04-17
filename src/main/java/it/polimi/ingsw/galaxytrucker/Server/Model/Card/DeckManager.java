package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import java.util.Collections;
import java.util.List;

public class DeckManager {

    public static Deck create_level1_deck(){
        List<Card> level1Cards = CardFactory.loadlevel1Cards();
        Deck deck = new Deck();
        deck.addAll(level1Cards);
        deck.shuffle();
        return deck;
    }

    public static Deck create_level2_deck(){
        List<Card> allCards = CardFactory.loadAllCards();
        Collections.shuffle(allCards);

        List<Card> selected = allCards.subList(0, 12);
        Deck deck = new Deck();
        deck.addAll(selected);
        deck.shuffle();
        return deck;
    }
}
