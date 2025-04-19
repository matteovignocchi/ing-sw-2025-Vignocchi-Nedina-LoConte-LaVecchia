package it.polimi.ingsw.galaxytrucker.Server.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Server.Model.Colour;

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

    @JsonCreator
    public FirstWarzoneCard(
            @JsonProperty("days") int days,
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("shots_directions") List<Integer> shots_directions,
            @JsonProperty("shots_size") List<Boolean> shots_size
    ){
        if(shots_directions == null || shots_directions.isEmpty()) throw new IllegalArgumentException("List shots_directions is null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new IllegalArgumentException("List shots_size is null or empty");
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");
        if(days <= 0) throw new IllegalArgumentException("Days must be greater than 0");
        if(num_crewmates <= 0) throw new IllegalArgumentException("Number of Crewmates must be greater than 0");

        this.days = days;
        this.num_crewmates = num_crewmates;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    @Override
    public String toString(){
        return "FirstWarzoneCard{" + "days:" + days + ", num_crewmates:" + num_crewmates + ", shots_directions:" + shots_directions
        + ", shots_size:" + shots_size + "}";
    }

    public int getDays() {return days;}

    public int getNumCrewmates() {return num_crewmates;}

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}
}
