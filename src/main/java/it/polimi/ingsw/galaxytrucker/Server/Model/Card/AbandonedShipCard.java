package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * This class handles the AbandonedShipCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedShipCard implements Card{

    private final int days;
    private final int credits;
    private final int num_crewmates;

    /**
     * Constructs an {@code AbandonedShipCard} with the specified values.
     * @param days: flight days that the player who accepts loses.
     * @param credits: credits earned by the player who accepts.
     * @param num_crewmates: crewmates that loses the player who accepts.
     */

    @JsonCreator
    public AbandonedShipCard(
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("num_crewmates") int num_crewmates
    ){
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");
        if(credits <= 0) throw new IllegalArgumentException("credits cannot be negative");

        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException {
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }
}
