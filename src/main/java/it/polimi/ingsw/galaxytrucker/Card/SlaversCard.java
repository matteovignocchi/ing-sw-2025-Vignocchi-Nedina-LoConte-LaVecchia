package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.List;

/**
 * "Slavers" adventure card description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class SlaversCard implements Card{
    private final int days;
    private final int credits;
    private final int num_crewmates;
    private final int fire_power;

    public SlaversCard(int days, int credits, int num_crewmates, int fire_power) {
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
        this.fire_power = fire_power;
    }

    /**
     * Activates the card's effect: it scrolls through the list in order starting from the leader.
     * If the player's firepower is higher than the slavers' one, he has the option to lose "days"
     * flight days to gain "credits" space credits. Regardless of whether he accepts or not, the slavers
     * are defeated and the method ends. If the player has less firepower, he loses "num_crewmates" crewmates.
     * If it has the same, nothing happens. In both cases the slavers attack the next player (we remain in the for loop)
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     */
    @Override
    public void activate(List<Player> players, FlightCardBoard f){
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(Player p : players){
            if(p.getFirePower() > fire_power){
                if(p.askPlayerDecision()){
                    f.moveRocket(-days, p, players);
                    p.addCredits(credits);
                }
                break;
            } else if(p.getFirePower() < fire_power){
                p.removeCrewmates(num_crewmates);
            }
        }
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }

    public int getFirePower(){ return fire_power; }
}
