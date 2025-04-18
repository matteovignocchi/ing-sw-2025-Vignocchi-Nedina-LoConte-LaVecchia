package it.polimi.ingsw.galaxytrucker.Server.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Lv. 2 "Warzone" adventure card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public class SecondWarzoneCard implements Card {
    private final int days;
    private final int num_goods;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

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

    public int getDays() {return days;}

    public int getNumGoods() {return num_goods;}

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}
}
