package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * This class handles the SlaversCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class SlaversCard implements Card{
    private final int days;
    private final int credits;
    private final int num_crewmates;
    private final int fire_power;

    /**
     * SlaversCard constructor with specific values.
     * @param days: flight days lost by the player who defeats the slavers and accepts the reward.
     * @param credits: credits earned by the player who defeats the slavers and accepts the reward.
     * @param num_crewmates: crewmates lost by the player who defeats the slavers and accepts the reward.
     * @param fire_power: firepower needed to beat the slavers card.
     */

    @JsonCreator
    public SlaversCard(
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("fire_power") int fire_power
    ){
        if(days <= 0) throw new IllegalArgumentException("days must be greater than 0");
        if(credits <= 0) throw new IllegalArgumentException("credits must be greater than 0");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power must be greater than 0");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates must be greater than 0");

        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
        this.fire_power = fire_power;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    /**
     * @return flight days shown on the card.
     */

    public int getDays(){ return days; }

    /**
     * @return credits shown on the card.
     */

    public int getCredits(){ return credits; }

    /**
     * @return number of crewmates shown on the card.
     */

    public int getNumCrewmates(){ return num_crewmates; }

    /**
     * @return firepower shown on the card.
     */

    public int getFirePower(){ return fire_power; }
}
