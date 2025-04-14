package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Lv. 2 "Warzone" adventure card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public class SecondWarzoneCard implements Card {
    private final int days;
    private final int num_goods;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    public SecondWarzoneCard(int days, int num_goods, List<Integer> shots_directions, List<Boolean> shots_size){
        if(shots_directions == null || shots_size == null) throw new NullPointerException("List is null");
        else if(shots_directions.isEmpty() || shots_size.isEmpty()) throw new IllegalArgumentException("List is empty");
        else if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");

        this.days = days;
        this.num_goods = num_goods;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    /**
     * Activate the card's effect: create 3 indices that respectively identify the player with the least firepower,
     * the player with the least engine power and the player with the least crew by scrolling through the list of players.
     * And, for each player identified, sanctions are applied which can be of three types: flight day reduction,
     * crewmates reduction and, finally, cannon shots.
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */

    @Override
    public void activate (List<Player> players, FlightCardBoard f){
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

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
            int res = players.get(index_p_less_crewmates).throwDice() + players.get(index_p_less_crewmates).throwDice();
            players.get(index_p_less_crewmates).defenceFromCannon(shots_directions.get(i), shots_size.get(i), res);
        }
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays() {return days;}

    public int getNumGoods() {return num_goods;}

    public List<Integer> getShots_directions() {return new ArrayList<>(shots_directions);}

    public List<Boolean> getShots_size() {return new ArrayList<>(shots_size);}
}
