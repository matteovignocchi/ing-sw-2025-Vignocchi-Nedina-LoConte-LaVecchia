package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the AbandonedStationCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedStationCard implements Card {
    private final int num_crewmates;
    private final int days;
    private final List<Colour> station_goods;

    /**
     * Constructs an {@code AbandonedStationCard} with the specified values.
     * @param num_crewmates: crewmates needed to be able to decide to redeem the rewards.
     * @param days: flight days that the player who accepts loses.
     * @param station_goods: List of goods, the reward for those who accept.
     */

    @JsonCreator
    public AbandonedStationCard(
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("days") int days,
            @JsonProperty("station_goods") List<Colour> station_goods
    ){
        if(station_goods == null) throw new IllegalArgumentException("reward_goods cannot be null");
        if(station_goods.isEmpty()) throw new IllegalArgumentException("reward_goods cannot be empty");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");

        this.num_crewmates = num_crewmates;
        this.days = days;
        this.station_goods = new ArrayList<>(station_goods);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException {
            visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getNumCrewmates(){ return num_crewmates; }

    public List<Colour> getStationGoods(){ return new ArrayList<>(station_goods); }
}
