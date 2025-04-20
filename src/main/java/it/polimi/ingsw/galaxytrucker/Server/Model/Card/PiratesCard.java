package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the PiratesCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PiratesCard implements Card {
    private final int fire_power;
    private final int days;
    private final int credits;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    /**
     * Constructs an {@code PiratesCard} with the specified values.
     * @param fire_power: firepower needed to beat the pirate card.
     * @param days: flight days that the player who accepts loses.
     * @param credits: credits earned by the player who accepts.
     * @param shots_directions: list of attack directions for players beaten by pirates.
     * @param shots_size: Attack size list for players beaten by pirates.
     */

    @JsonCreator
    public PiratesCard(
            @JsonProperty("fire_power") int fire_power,
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("shots_directions") List<Integer> shots_directions,
            @JsonProperty("shots_size") List<Boolean> shots_size
    ){
        if(shots_directions == null || shots_directions.isEmpty()) throw new IllegalArgumentException("List shots_directions cannot be null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new IllegalArgumentException("List shots_size cannot be null or empty");
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("List shots_directions and shots_size must be the same size");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");
        if(credits <= 0) throw new IllegalArgumentException("credits cannot be negative");

        this.fire_power = fire_power;
        this.days = days;
        this.credits = credits;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

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



/**
 * The method activates the card's effect: It scrolls down the list of players starting from the leader, checks if
 * the player has a higher firepower than the pirates and, if so, if the player decides to redeem the reward
 * he receives "credits" credits and loses "days" days of flight. If the player has a lower firepower than the pirates,
 * he is added to the list of the defeated. Once the pirates are defeated (or the list of players is finished),
 * the first defeated player rolls the dice and determines the row and/or column that will be attacked by the cannons.
 * This column and/or row is valid for each defeated player.
 */
