package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
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

    public SecondWarzoneCard(int days, int num_goods, List<Integer> shots_directions, List<Boolean> shots_size){
        if(shots_directions == null || shots_size == null) throw new NullPointerException("List is null");
        else if(shots_directions.isEmpty() || shots_size.isEmpty()) throw new IllegalArgumentException("List is empty");
        else if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");

        this.days = days;
        this.num_goods = num_goods;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays() {return days;}

    public int getNumGoods() {return num_goods;}

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}
}
