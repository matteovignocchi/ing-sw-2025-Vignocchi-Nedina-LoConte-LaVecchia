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
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class DeckManager implements Serializable {
    private final CardGeneration generator;

    /**
     * Constructs a new DeckManager by initializing a CardGeneration instance to load cards from JSON resources.
     * @throws IOException: if loading card data files fails.
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

    //TODO: SOLO PER DEBUGGING, ELIMINARE TUTTI STI METODI UNA VOLTA FINITI

    public List<Deck> CreateOpenSpaceDecks() throws CardEffectException {
        Card c1 = new OpenSpaceCard("1");
        Card c2 = new OpenSpaceCard("2");
        Card c3 = new OpenSpaceCard("3");
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreateStardustDecks() throws CardEffectException {
        Card c1 = new StardustCard("1");
        Card c2 = new StardustCard("2");
        Card c3 = new StardustCard("3");
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreateSlaversDecks() throws CardEffectException {
        Card c1 = new SlaversCard("1", 13, 5, 2, 1);
        Card c2 = new SlaversCard("2", 13, 5, 2, 1);
        Card c3 = new SlaversCard("3", 13, 5, 2, 1);
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreateAbandonedShipDecks() throws CardEffectException {
        Card c1 = new AbandonedShipCard("1", 3, 6, 2);
        Card c2 = new AbandonedShipCard("2", 3, 6, 2);
        Card c3 = new AbandonedShipCard("3", 3, 6, 2);
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreateAbandonedStationDecks() throws CardEffectException {
        List<Colour> list1= new ArrayList<>();
        list1.add(Colour.RED);
        list1.add(Colour.RED);
        list1.add(Colour.YELLOW);
        list1.add(Colour.YELLOW);
        list1.add(Colour.BLUE);
        list1.add(Colour.BLUE);
        list1.add(Colour.GREEN);
        list1.add(Colour.GREEN);
        Card c1 = new AbandonedStationCard("1", 3, 2, list1);
        Card c2 = new AbandonedStationCard("2", 3, 2, list1);
        Card c3 = new AbandonedStationCard("3", 3, 2, list1);

        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreatePlagueDeck() throws CardEffectException {
        Card c1 = new PlaugeCard("1");
        Card c2 = new PlaugeCard("2");
        Card c3 = new PlaugeCard("3");
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    public List<Deck> CreatePiratesDeck() throws CardEffectException {
        List<Integer> dir1 = new ArrayList<>();
        dir1.add(0);
        dir1.add(1);
        dir1.add(2);
        dir1.add(3);
        List<Boolean> size1 = new ArrayList<>();
        size1.add(false);
        size1.add(false);
        size1.add(true);
        size1.add(true);


        Card c1 = new PiratesCard("1", 2, 3, 5, dir1, size1);
        Card c2 = new PiratesCard("2", 2, 3, 5, dir1, size1);
        Card c3 = new PiratesCard("3", 2, 3, 5, dir1, size1);
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }

    /**
    public List<Deck> CreateSmugglersDecks() throws CardEffectException {
        List<Colour> list1= new ArrayList<>();
        List<Colour> list2= new ArrayList<>();
        list1.add(Colour.RED);
        list1.add(Colour.RED);
        list1.add(Colour.YELLOW);
        list1.add(Colour.BLUE);
        list2.add(Colour.RED);
        list2.add(Colour.GREEN);
        list2.add(Colour.GREEN);
        Card c1 = new SmugglersCard("1", 4, 1, 3, list1);
        Card c2 = new SmugglersCard("1", 4, 1, 3, list2);
        Card c3 = new SmugglersCard("1", 4, 1, 3, list2);
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        Deck deck3 = new Deck();
        deck1.add(c1);
        deck2.add(c2);
        deck3.add(c3);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);
        return decks;
    }
    */

}
