package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import java.util.ArrayList;
import java.util.List;

/**
 * "Planets" adventure card description
 * @author Gabriele La vecchia && Francesco Lo Conte
 */
public class PlanetsCard implements Card {
    private final List<List<Good>> reward_goods;
    private final int days;

    public PlanetsCard(List<List<Good>> reward_goods, int days) throws IllegalArgumentException{
        if(reward_goods == null) throw new IllegalArgumentException("List is null");
        else if(reward_goods.isEmpty()) throw new IllegalArgumentException("List is empty");

        List<List<Good>> temp = new ArrayList<>(reward_goods.size());
        for(List<Good> innerList : reward_goods) {
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
     * @param players Players list sorted by position, from the leader onwards
     * @param f FlightCardBoard
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     */
    @Override
    public void activate(List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");

        int j = 0;
        for(Player p : players){
            if(p.askPlayerDecision()){
                f.moveRocket(-days, p, players);
                p.addGoods(reward_goods.get(j));
                j++;
                if(j > reward_goods.size()) break;
            }
        }
    }

    public int getDays() {return days;}

    public List<List<Good>> getRewardGoods() {
        List<List<Good>> copy = new ArrayList<>(reward_goods.size());
        for (List<Good> inner : reward_goods) {
            copy.add(new ArrayList<>(inner));
        }
        return copy;
    }

}
