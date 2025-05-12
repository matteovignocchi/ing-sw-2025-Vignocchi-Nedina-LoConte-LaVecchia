package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the SmugglersCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */


public class SmugglersCard implements Card{
    private final String idCard;
    private final int days;
    private final int fire_power;
    private final int num_removed_goods;
    private final List<Colour> reward_goods;

    /**
     * AbandonedShipCard constructor with specific values.
     * @param days: flight days lost by the player who defeats the smugglers and accepts the reward.
     * @param fire_power: firepower needed to beat the smugglers card.
     * @param num_removed_goods: number of goods lost by the player who defeats the smugglers and accepts the reward.
     * @param reward_goods: list of goods that represent the reward for those who defeat the smugglers.
     */

    @JsonCreator
    public SmugglersCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("days") int days,
            @JsonProperty("fire_power") int fire_power,
            @JsonProperty("num_removed_goods") int num_removed_goods,
            @JsonProperty("reward_goods") List<Colour> reward_goods
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(days <= 0) throw new IllegalArgumentException("days must be greater than 0");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power must be greater than 0");
        if(num_removed_goods <= 0) throw new IllegalArgumentException("num_removed_goods must be greater than 0");
        if(reward_goods == null || reward_goods.isEmpty()) throw new IllegalArgumentException("reward_goods list is null or empty");

        this.idCard = idCard;
        this.days = days;
        this.fire_power = fire_power;
        this.num_removed_goods = num_removed_goods;
        this.reward_goods = new ArrayList<>(reward_goods);
    }

    @Override
    public void accept(CardVisitor visitor)throws CardEffectException{
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
     * @return firepower shown on the card.
     */

    public int getFirePower(){ return fire_power; }

    /**
     * @return number of goods to delete in case of penalty shwon on the card.
     */

    public int getNumRemovedGoods() { return num_removed_goods; }

    /**
     * @return list of goods shown on the card.
     */

    public List<Colour> getRewardGoods() { return new ArrayList<>(reward_goods); }
    public String getIdCard() { return idCard; }
}
