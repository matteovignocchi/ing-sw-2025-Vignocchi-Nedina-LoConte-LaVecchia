package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.List;

/**
 * This class describes the behavior of the "Abandoned Ship" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedShipCard implements Card{

    private final int days;
    private final int credits;
    private final int num_crewmates;

    public AbandonedShipCard(int days, int credits, int num_crewmates) {
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
    }

    /**
     * Activate the card's effect: It scrolls down the list of players in order starting from the leader
     * and asks the player if he wants to activate the card effect. If he accepts, the rocket is moved back "days"
     * spaces on the FlightCardBoard, "credits" cosmic credits are added and "num_crewmates" crew tokens are removed
     * and the loop is exited since only one player can use the card effect
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */

    @Override
    public void activate(List<Player> players, FlightCardBoard f){
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(Player p : players){
            if(p.askPlayerDecision()) {
                f.moveRocket(-days, p, players);
                p.addCredits(credits);
                p.removeCrewmates(num_crewmates);
                break;
            }
        }
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }
}
