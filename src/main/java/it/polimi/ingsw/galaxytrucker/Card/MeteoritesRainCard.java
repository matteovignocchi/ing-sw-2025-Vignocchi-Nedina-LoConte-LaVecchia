package it.polimi.ingsw.galaxytrucker.Card;

import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * "MeteoritesRain" adventure card description
 * @author Gabriele La Vecchia && Francesco "El Matador" Lo Conte
 */
public class MeteoritesRainCard implements Card{
    private final List<Integer> directions;
    private final List<Boolean> size;

    public MeteoritesRainCard(List<Integer> directions, List<Boolean> size) {
        if(directions == null || size == null) throw new NullPointerException("List is null");
        else if(directions.isEmpty() || size.isEmpty()) throw new IllegalArgumentException("List is empty");
        else if(directions.size() != size.size()) throw new IllegalArgumentException("Different Lists' dimensions");

        this.directions = new ArrayList<>(directions);
        this.size = new ArrayList<>(size);
    }

    /**
     * The method activates the card's effect: It scrolls through the list of players starting from the leader and each
     * of them is hit by each meteorite detected by scrolling through the list of the direction and size of meteorites.
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     */
    @Override
    public void activate(List<Player> players, FlightCardBoard f) {
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for (int i = 0; i < directions.size(); i++) {
            int res = players.getFirst().throwDice() + players.getFirst().throwDice();
            for(Player p : players){
                p.defenceFromMeteorite(directions.get(i), size.get(i), res);
            }
        }
    }

    public List<Integer> getMeteorites_directions(){ return new ArrayList<>(directions); }

    public List<Boolean> getMeteorites_size(){ return new ArrayList<>(size); }
}
