package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import it.polimi.ingsw.galaxytrucker.Token.Good;
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
    private final List<Good> reward_goods;

    public SmugglersCard(int days, int fire_power, int r_goods, List<Good> reward_goods) {
        this.days = days;
        this.fire_power = fire_power;
        this.num_removed_goods = r_goods;
        this.reward_goods = new ArrayList<>(r_goods);
    }

    /**
     * The method activates the card's effect: it scrolls through the players' list in order, starting from the leader.
     * If the player's firepower is higher than the Smugglers' one, he can decide whether to get the reward_goods and lose
     * "days" flight days or not. Regardless of whether he accepts or not, the smugglers are defeated and the method ends.
     * If the player has less firepower than the smugglers, he has to lose "num_removed_goods" goods (if they are not
     * enough, he loses batteries). If he has the same, nothing happens. In both these two final cases, the smugglers
     * attack the next player in the list (we remain in for loop).
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     */
    @Override
    public void activate (List<Player> players, FlightCardBoard f){
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for (Player p : players){
            if(p.getFirePower() > fire_power){
                if(p.askPlayerDecision()){
                    f.moveRocket(-days, p, players);
                    p.addGoods(reward_goods);
                }
                break;
            } else if (p.getFirePower() < fire_power){
                p.removeGoods(num_removed_goods);
            }
        }
    }

    public int getDays(){ return days; }

    public int getFirePower(){ return fire_power; }

    public int getNumRemovedGoods() { return num_removed_goods; }

    public List<Good> getRewardGoods() { return new ArrayList<>(reward_goods); }
}
