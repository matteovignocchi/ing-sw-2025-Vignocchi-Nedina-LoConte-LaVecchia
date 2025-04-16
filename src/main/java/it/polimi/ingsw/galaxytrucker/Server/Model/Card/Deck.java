package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import java.util.*;

/**
 * Deck class description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */
//public class Deck {
//  protected List<Card> cards;
//      public Deck(List<Card> cards) {
//      this.cards = new LinkedList<>(cards);
//      }
//
//      public Card draw(){return cards.removeFirst();}
//
//      public List<Card> getDeck(){ return new LinkedList<>(cards);
//      }

//Logica FIFO, ricorda la complessità
public class Deck {
    private final Queue<Card> cards;

    public Deck() {
        this.cards = new LinkedList<>();
    }

    public void add(Card card) {
        if(card != null) {
            cards.offer(card); //offer è un metodo per le queue che aggiunge alla fine
        }
    }

    public void addAll(Collection<Card> cards) {
        for(Card card : cards){
            add(card);
        }
    }

    public void shuffle() {
        List<Card> temp = new ArrayList<>(cards);
        Collections.shuffle(temp);
        cards.clear(); //elimino tutti i vecchi elementi in cards
        cards.addAll(temp);
    }

    public Card draw() throws CardEffectException{
        if(cards.isEmpty()) throw new InvalidSizeException("The deck is empty");
        return cards.poll(); //metodo che rimuove la carta in cima
    }

    //Non necessario, ma se faremo dei controlli si fa molto velocemente invece di chiamare size
    //e vedere se effettivamente il mazzo è vuoto. Evita eccezioni che sono smell
    //e non fa parte delle best practice
    public boolean isEmpty() {return cards.isEmpty();}

    //utile per capire dimensione del deck, e quindi capire quante carte mancano
    public int size() {return cards.size();}

    //restituisce una copia delle carte che ho ancora nel deck, molto utile per debugging,
    //Ma può essere tranquillamente elminato
    public List<Card> getCards() {return new ArrayList<>(cards);}
}