package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.Colour;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the behavior of the "Abandoned Station" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedStationCard implements Card {
    private final int num_crewmates;
    private final int days;
    private final List<Colour> station_goods;

    public AbandonedStationCard(int num_crewmates, int days, List<Colour> station_goods){
        this.num_crewmates = num_crewmates;
        this.days = days;
        this.station_goods = new ArrayList<>(station_goods);
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getNumCrewmates(){ return num_crewmates; }

    public List<Colour> getStationGoods(){ return new ArrayList<>(station_goods); }
}
