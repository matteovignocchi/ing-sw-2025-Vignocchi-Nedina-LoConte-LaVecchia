package it.polimi.ingsw.galaxytrucker.Card;

import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.ArrayList;
import java.util.List;

public class PiratesCard implements Card {
    private final int fire_power;
    private final int days;
    private final int credits;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    public PiratesCard(int fire_power, int days, int credits, List<Integer> shots_directions, List<Boolean> shots_size) throws IllegalArgumentException {
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");

        this.fire_power = fire_power;
        this.days = days;
        this.credits = credits;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }


    @Override
    public void activate (List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");

        for(Player p : players) {
            if(p.getFirePower() > fire_power){
                if(p.askPlayerDecision()){
                    f.moveRocket(-days, p, players);
                    p.addCredits(credits);
                }
                break;
            } else if (p.getFirePower() < fire_power){
                for(int i = 0; i < shots_directions.size(); i++){
                    p.defenceFromCannon(shots_directions.get(i), shots_size.get(i));
                }
            }
        }
    }
}
