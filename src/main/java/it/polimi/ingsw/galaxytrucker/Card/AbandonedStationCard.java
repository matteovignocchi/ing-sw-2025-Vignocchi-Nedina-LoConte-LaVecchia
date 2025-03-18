package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the behavior of the "Abandoned Station" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedStationCard implements Card {
    private final int num_crewmates;
    private final int days;
    private final List<Good> station_goods;

    public AbandonedStationCard(int num_crewmates, int days, List<Good> station_goods){
        this.num_crewmates = num_crewmates;
        this.days = days;
        this.station_goods = new ArrayList<>(station_goods);
    }

    /**
     * Activate the card's effect: It goes through the list of players in order starting from the leader and checks,
     * one at a time, if the number of crewmates is greater than or equal to "num_crewmates" and, if so, then asks
     * the player if he wants to lose "days" days of flight and load the list of goods "station_goods" on his ship.
     * If the player accepts, the loop is interrupted.
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */

    @Override
    public void activate(List<Player> players, FlightCardBoard f){
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(Player p: players) {
            if(p.getCrewmates()>=num_crewmates){
                if(p.askPlayerDecision()){
                    f.moveRocket(-days, p, players);
                    p.addGoods(station_goods);
                }
                break;
            }
        }
    }
    public int getDays(){ return days; }

    public int getNumCrewmates(){ return num_crewmates; }

    public List<Good> getStationGoods(){ return new ArrayList<>(station_goods); }
}
