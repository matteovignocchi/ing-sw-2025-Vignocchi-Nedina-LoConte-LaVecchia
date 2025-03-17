package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.List;

/**
 * This class describes the behavior of the "Plauge Card" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PlaugeCard implements Card {

    /**
     * Activate the card's effect: It scrolls through the list of players and, for each of them, calls the method that
     * searches for HousingUnits that are interconnected to other HousingUnits and, for each of them, eliminates
     * (if there is at least one) 1 crew member (human or alien).
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     * @throws IllegalArgumentException
     */

    @Override
    public void activate(List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");

        for(Player p : players){
            p.startPlauge();
        }
    }
}
