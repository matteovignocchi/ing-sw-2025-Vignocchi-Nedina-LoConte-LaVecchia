package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the PlanetsCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PlanetsCard implements Card, Serializable {
    private final String idCard;
    private final List<List<Colour>> reward_goods;
    private final int days;

    /**
     * PlanetsCard constructor with specific values.
     * @param reward_goods: List of lists of goods, in order from first to last planet.
     * @param days: flight days lost by players who decide to land on a planet.
     */

    @JsonCreator
    public PlanetsCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("reward_goods") List<List<Colour>> reward_goods,
            @JsonProperty("days") int days
    ) {
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(reward_goods == null || reward_goods.isEmpty()) throw new IllegalArgumentException("reward_goods is null");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");

        List<List<Colour>> temp = new ArrayList<>(reward_goods.size());
        for(List<Colour> innerList : reward_goods) {
            if(innerList.isEmpty()) throw new IllegalArgumentException("Null innerList in reward_goods");
            else temp.add(innerList);
        }

        this.idCard = idCard;
        this.reward_goods = temp;
        this.days = days;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException {
        try {
            visitor.visit(this);
        } catch (BusinessLogicException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return flight days shown on the card.
     */

    public int getDays() {return days;}

    /**
     * @return A copy of the list of goods lists shown on the card.
     */

    public List<List<Colour>> getRewardGoods() {
        List<List<Colour>> copy = new ArrayList<>(reward_goods.size());
        for (List<Colour> inner : reward_goods) {
            copy.add(new ArrayList<>(inner));
        }
        return copy;
    }

    public String getIdCard() {return idCard;}
}