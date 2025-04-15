package it.polimi.ingsw.galaxytrucker.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * "Pirates" card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public class PiratesCard implements Card {
    private final int fire_power;
    private final int days;
    private final int credits;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    public PiratesCard(int fire_power, int days, int credits, List<Integer> shots_directions, List<Boolean> shots_size){
        if(shots_directions == null || shots_directions.isEmpty()) throw new IllegalArgumentException("List shots_directions cannot be null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new IllegalArgumentException("List shots_size cannot be null or empty");
        if(fire_power < 0) throw new IllegalArgumentException("fire_power cannot be negative");
        if(days < 0) throw new IllegalArgumentException("days cannot be negative");
        if(credits < 0) throw new IllegalArgumentException("credits cannot be negative");

        this.fire_power = fire_power;
        this.days = days;
        this.credits = credits;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    /**
     * The method activates the card's effect: It scrolls down the list of players starting from the leader, checks if
     * the player has a higher firepower than the pirates and, if so, if the player decides to redeem the reward
     * he receives "credits" credits and loses "days" days of flight. If the player has a lower firepower than the pirates,
     * he is added to the list of the defeated. Once the pirates are defeated (or the list of players is finished),
     * the first defeated player rolls the dice and determines the row and/or column that will be attacked by the cannons.
     * This column and/or row is valid for each defeated player.
     */

    // @Override
    // public void activate (List<Player> players, FlightCardBoard f){
    // if(players == null) throw new NullPointerException("Null players list");
    // else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
    // else if(f == null) throw new NullPointerException("Null flight card board");
    //
    // List<Player> losers = new ArrayList<>();
    // for(Player p : players) {
    // if(p.getFirePower() > fire_power){
    // if(p.askPlayerDecision()){
    // f.moveRocket(-days, p, players);
    // p.addCredits(credits);
    // }
    // break;
    // } else if (p.getFirePower() < fire_power)
    // losers.add(p);
    // }
    // if(losers.getFirst() != null){
    // int res = losers.getFirst().throwDice() + losers.getFirst().throwDice();
    // for(Player p : losers){
    // for(int i = 0; i < shots_directions.size(); i++){
    // p.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res);
    // }
    // }
    // }
    // }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
            visitor.visit(this);
    }

    public int getFirePower(){return fire_power;}

    public int getDays(){return days;}

    public int getCredits(){return credits;}

    public List<Integer> getShots_directions(){return new ArrayList<>(shots_directions);}

    public List<Boolean> getShots_size(){return new ArrayList<>(shots_size);}
}

