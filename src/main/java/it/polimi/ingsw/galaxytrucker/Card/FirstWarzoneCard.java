package it.polimi.ingsw.galaxytrucker.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the behavior of the "Warzone" adventure card at level 1.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class FirstWarzoneCard implements Card {
    private final int days;
    private final int num_crewmates;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    public FirstWarzoneCard(int days, int num_crewmates, List<Integer> shots_directions, List<Boolean> shots_size){
        if(shots_directions == null || shots_size == null) throw new NullPointerException("List is null");
        else if(shots_directions.isEmpty() || shots_size.isEmpty()) throw new IllegalArgumentException("List is empty");
        else if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");

        this.days = days;
        this.num_crewmates = num_crewmates;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays() {return days;}

    public int getNumCrewmates() {return num_crewmates;}

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}
}
