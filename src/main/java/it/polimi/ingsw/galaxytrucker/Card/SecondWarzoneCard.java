package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Lv. 2 "Warzone" adventure card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

//AGGIUNGERE LE ECCEZIONI, COMMENTI, GETTER
public class SecondWarzoneCard implements Card {
    private final int days;
    private final int num_goods;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    public SecondWarzoneCard(int days, int num_goods, List<Integer> shots_directions, List<Boolean> shots_size)throws IllegalArgumentException {
        if
        this.days = days;
        this.num_goods = num_goods;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void activate (List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");

        int index_p_less_firepower = 0;
        int index_p_less_powerengine = 0;
        int index_p_less_crewmates = 0;
        for(int i = 1; i < players.size(); i++) {
            if(players.get(i).getFirePower() < players.get(index_p_less_firepower).getFirePower())
                index_p_less_firepower = i;
            if(players.get(i).getPowerEngine() < players.get(index_p_less_powerengine).getPowerEngine())
                index_p_less_powerengine = i;
            if(players.get(i).getCrewmates() < players.get(index_p_less_crewmates).getCrewmates())
                index_p_less_crewmates = i;
        }
        f.moveRocket(-days, players.get(index_p_less_firepower), players);
        players.get(index_p_less_powerengine).removeGoods(num_goods);
        for (int i = 0; i < shots_directions.size(); i++) {
            players.get(index_p_less_crewmates).defenseFromCannon(shots_directions.get(i), shots_size.get(i));
        }
    }
}
