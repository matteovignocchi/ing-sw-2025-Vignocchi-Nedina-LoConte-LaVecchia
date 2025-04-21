package it.polimi.ingsw.galaxytrucker.Server.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the SecondWarzoneCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class SecondWarzoneCard implements Card {
    private final int days;
    private final int num_goods;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    /**
     * SecondWarzoneCard constructor with specific values.
     * @param days: days of flight that the player with the least firepower loses.
     * @param num_goods: number of goods lost by the player with the least engine power.
     * @param shots_directions: list of attack directions for the player with the least number of crewmates.
     * @param shots_size: Attack size list for the player with the least number of crewmates.
     */

    @JsonCreator
    public SecondWarzoneCard(
            @JsonProperty("days") int days,
            @JsonProperty("num_goods") int num_goods,
            @JsonProperty("shots_directions") List<Integer> shots_directions,
            @JsonProperty("shots_size") List<Boolean> shots_size
    ){
        if(shots_directions == null || shots_directions.isEmpty()) throw new NullPointerException("List shots_directions is null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new NullPointerException("List shots_size is null or empty");
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");
        if(days <= 0) throw new IllegalArgumentException("Days must be greater than 0");
        if(num_goods <= 0) throw new IllegalArgumentException("Number of goods must be greater than 0");

        this.days = days;
        this.num_goods = num_goods;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    /**
     * @return flight days shown on the card.
     */

    public int getDays() {return days;}

    /**
     * @return number of goods shown on the card.
     */

    public int getNumGoods() {return num_goods;}

    /**
     * @return list of attack directions shown on the card.
     */

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    /**
     * @return attack size list shown on the card.
     */

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}
}
