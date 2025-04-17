package it.polimi.ingsw.galaxytrucker.Server.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Server.Model.Colour;
import java.util.ArrayList;
import java.util.List;

/**
 * "Smugglers" adventure card's description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public class SmugglersCard implements Card{
    private final int days;
    private final int fire_power;
    private final int num_removed_goods;
    private final List<Colour> reward_goods;

    @JsonCreator
    public SmugglersCard(
            @JsonProperty("days") int days,
            @JsonProperty("fire_power") int fire_power,
            @JsonProperty("num_removed_goods") int num_removed_goods,
            @JsonProperty("reward_goods") List<Colour> reward_goods
    ){
        if(days <= 0) throw new IllegalArgumentException("days must be greater than 0");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power must be greater than 0");
        if(num_removed_goods <= 0) throw new IllegalArgumentException("num_removed_goods must be greater than 0");
        if(reward_goods == null || reward_goods.isEmpty()) throw new IllegalArgumentException("reward_goods list is null or empty");

        this.days = days;
        this.fire_power = fire_power;
        this.num_removed_goods = num_removed_goods;
        this.reward_goods = new ArrayList<>(num_removed_goods); //capire come inizializzare
    }

    @Override
    public void accept(CardVisitor visitor)throws CardEffectException{
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getFirePower(){ return fire_power; }

    public int getNumRemovedGoods() { return num_removed_goods; }

    public List<Colour> getRewardGoods() { return new ArrayList<>(reward_goods); }
}
