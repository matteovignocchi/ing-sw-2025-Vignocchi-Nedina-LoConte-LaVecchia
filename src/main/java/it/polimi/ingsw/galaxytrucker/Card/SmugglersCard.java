package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.Colour;
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

    public SmugglersCard(int days, int fire_power, int r_goods, List<Colour> reward_goods) {
        this.days = days;
        this.fire_power = fire_power;
        this.num_removed_goods = r_goods;
        this.reward_goods = new ArrayList<>(r_goods); //capire come inizializzare
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getFirePower(){ return fire_power; }

    public int getNumRemovedGoods() { return num_removed_goods; }

    public List<Colour> getRewardGoods() { return new ArrayList<>(reward_goods); }
}
