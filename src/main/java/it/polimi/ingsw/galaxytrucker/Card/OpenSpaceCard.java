package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.List;

/**
 * This class describes the behavior of the "open space" adventure card.
 * @author Gabriele La Vecchia & Francesco Lo Conte
 */

public class OpenSpaceCard implements Card {

    /**
     * Activates the card's effect: scrolls through the players list in order starting
     * from the leader, and for each one gets the engine power "x" and moves the rocket
     * on the board by "x" spaces
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */

    @Override
    public void activate(List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(Player p : players){
            int x = p.getPowerEngine(); //+ p.askDoubleEngine();
            f.moveRocket(x, p, players);
        }
    }
}
