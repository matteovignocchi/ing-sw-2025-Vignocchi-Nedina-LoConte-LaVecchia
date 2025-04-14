package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.Colour;
import java.util.ArrayList;
import java.util.List;

/**
 * "Planets" adventure card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */
public class PlanetsCard implements Card {
    private final List<List<Colour>> reward_goods;
    private final int days;

    public PlanetsCard(List<List<Colour>> reward_goods, int days) {
        if(reward_goods == null) throw new NullPointerException("List is null");
        else if(reward_goods.isEmpty()) throw new IllegalArgumentException("List is empty");

        List<List<Colour>> temp = new ArrayList<>(reward_goods.size());
        for(List<Colour> innerList : reward_goods) {
            if(innerList.isEmpty()) throw new IllegalArgumentException("List is empty");
            else temp.add(innerList);
        }
        this.reward_goods = temp;
        this.days = days;
    }

    /**
     * The method activates the card's effect: it scrolls through the players' list, in order starting from the leader.
     * He can decide whether he wants to go down to the first planet and take the goods (losing the indicated flight days)
     * or not. If he decides to go down, the planet is occupied, and you move on to the next one, otherwise it remains free.
     * Once he has made his decision, you move on to the next player who in turn must choose.
     * If all the planets on the card are occupied by players, the method ends.
     */
    //@Override
    // public void activate(List<Player> players, FlightCardBoard f) {
    // if(players == null) throw new NullPointerException("Null players list");
    // else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
    // else if(f == null) throw new NullPointerException("Null flight card board");

    //    int j = 0;
    //    for(Player p : players){
    //        if(p.askPlayerDecision()){
    //          f.moveRocket(-days, p, players);
    //          p.addGoods(reward_goods.get(j));
    //          j++;
    //          if(j > reward_goods.size()) break;
    //          }
    //       }
    //   }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays() {return days;}

    public List<List<Colour>> getRewardGoods() {
        List<List<Colour>> copy = new ArrayList<>(reward_goods.size());
        for (List<Colour> inner : reward_goods) {
            copy.add(new ArrayList<>(inner));
        }
        return copy;
    }

}
