package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.List;

/**
 * "Stardust" adventure card description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class StardustCard implements Card {

    /**
     * Activates the card's effect: scrolls through the players list in reverse order, from
     * the last player to the leader, and for each one gets the number of exposed connectors "x"
     * and moves the rocket on the board by "-x" spaces.
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     */
    @Override
    public void activate(List<Player> players, FlightCardBoard f) {
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(int i = players.size() - 1; i >= 0; i--){
            Player p = players.get(i);
            int x = p.countExposedConnectors();
            f.moveRocket(-x, p, players);
        }
    }
}
