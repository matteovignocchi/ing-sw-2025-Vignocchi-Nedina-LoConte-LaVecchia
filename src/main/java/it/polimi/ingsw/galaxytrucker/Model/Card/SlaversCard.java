package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;

import java.io.Serializable;


/**
 * This class handles the SlaversCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class SlaversCard implements Card, Serializable {
    private final String idCard;
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
            @JsonProperty("id_card") String idCard,
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("fire_power") int fire_power
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(days <= 0) throw new IllegalArgumentException("days must be greater than 0");
        if(credits <= 0) throw new IllegalArgumentException("credits must be greater than 0");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power must be greater than 0");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates must be greater than 0");

        this.idCard = idCard;
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
        this.fire_power = fire_power;
    }

    /**
     * Accepts a CardVisitor to process this card.
     *
     * @param visitor the CardVisitor that will handle this card
     * @throws BusinessLogicException if a business logic error occurs during processing
     */
    @Override
    public void accept(CardVisitor visitor) throws BusinessLogicException {
        try {
            visitor.visit(this);
        } catch (BusinessLogicException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * @return card's id
     */
    public String getIdCard(){ return idCard; }
}