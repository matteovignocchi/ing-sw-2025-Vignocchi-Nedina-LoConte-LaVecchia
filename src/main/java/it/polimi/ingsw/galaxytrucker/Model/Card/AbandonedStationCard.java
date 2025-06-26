package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the AbandonedStationCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedStationCard implements Card, Serializable {
    private final String idCard;
    private final int num_crewmates;
    private final int days;
    private final List<Colour> station_goods;

    /**
     * AbandonedStationCard constructor with specific values.
     * @param num_crewmates: crewmates needed to be able to decide to redeem the rewards.
     * @param days: flight days that the player who accepts loses.
     * @param station_goods: List of goods, the reward for those who accept.
     */

    @JsonCreator
    public AbandonedStationCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("days") int days,
            @JsonProperty("station_goods") List<Colour> station_goods
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(station_goods == null) throw new IllegalArgumentException("reward_goods cannot be null");
        if(station_goods.isEmpty()) throw new IllegalArgumentException("reward_goods cannot be empty");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");

        this.idCard = idCard;
        this.num_crewmates = num_crewmates;
        this.days = days;
        this.station_goods = new ArrayList<>(station_goods);
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
     * @return number of crewmates shown on the card.
     */

    public int getNumCrewmates(){ return num_crewmates; }

    /**
     * @return list of goods shown on the card.
     */

    public List<Colour> getStationGoods(){ return new ArrayList<>(station_goods); }

    /**
     * @return card's id
     */
    public String getIdCard(){ return idCard; }
}