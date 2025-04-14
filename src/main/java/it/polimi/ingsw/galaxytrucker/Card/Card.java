package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.List;

/**
 * Card interface
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public interface Card {
    void accept (CardVisitor visitor);
}
