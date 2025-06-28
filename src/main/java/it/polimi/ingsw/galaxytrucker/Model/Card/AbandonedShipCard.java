package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;

import java.io.Serializable;


/**
 * This class handles the AbandonedShipCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */

public class AbandonedShipCard implements Card, Serializable {

    private final String idCard;
    private final int days;
    private final int credits;
    private final int num_crewmates;

    /**
     * AbandonedShipCard constructor with specific values.
     * @param days: flight days that the player who accepts loses.
     * @param credits: credits earned by the player who accepts.
     * @param num_crewmates  number of crewmates lost by the player who accepts the card. Must be positive.
     * @throws IllegalArgumentException if {@code idCard} is null or blank, or if {@code days}, {@code credits}, or {@code num_crewmates} are not positive.
     */

    @JsonCreator
    public AbandonedShipCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("num_crewmates") int num_crewmates
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");
        if(credits <= 0) throw new IllegalArgumentException("credits cannot be negative");

        this.idCard = idCard;
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
    }

    /**
     * Accepts a CardVisitor to process this card.
     *
     * @param visitor the CardVisitor that will handle this card
     * @throws BusinessLogicException if a business logic error occurs during processing
     */
    @Override
    public void accept(CardVisitor visitor) throws BusinessLogicException {
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
     * @return card's id
     */
    public String getIdCard(){ return idCard; }


}
