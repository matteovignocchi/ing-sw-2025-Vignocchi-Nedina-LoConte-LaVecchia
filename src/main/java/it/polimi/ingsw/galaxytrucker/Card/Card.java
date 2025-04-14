package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.List;

/**
 * Card interface
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public interface Card {

    /**
     * Method header to activate a card. The body is defined by each class
     * that implements the interface.
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */
    //eliminare
    void activate (List<Player> players, FlightCardBoard f);

    void accept (CardVisitor visitor);
}
