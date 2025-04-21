package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the PlanetsCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PlanetsCard implements Card {
    private final List<List<Colour>> reward_goods;
    private final int days;

    /**
     * PlanetsCard constructor with specific values.
     * @param reward_goods: List of lists of goods, in order from first to last planet.
     * @param days: flight days lost by players who decide to land on a planet.
     */

    @JsonCreator
    public PlanetsCard(
            @JsonProperty("reward_goods") List<List<Colour>> reward_goods,
            @JsonProperty("days") int days
    ) {
        if(reward_goods == null || reward_goods.isEmpty()) throw new IllegalArgumentException("reward_goods is null");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");

        List<List<Colour>> temp = new ArrayList<>(reward_goods.size());
        for(List<Colour> innerList : reward_goods) {
            if(innerList.isEmpty()) throw new IllegalArgumentException("Null innerList in reward_goods");
            else temp.add(innerList);
        }
        this.reward_goods = temp;
        this.days = days;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException {
            visitor.visit(this);
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

}


/**
 * The method activates the card's effect: it scrolls through the players' list, in order starting from the leader.
 * He can decide whether he wants to go down to the first planet and take the goods (losing the indicated flight days)
 * or not. If he decides to go down, the planet is occupied, and you move on to the next one, otherwise it remains free.
 * Once he has made his decision, you move on to the next player who in turn must choose.
 * If all the planets on the card are occupied by players, the method ends.
 */