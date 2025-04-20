package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * "MeteoritesRain" adventure card description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class MeteoritesRainCard implements Card{
    private final List<Integer> directions;
    private final List<Boolean> size;

    @JsonCreator
    public MeteoritesRainCard(
            @JsonProperty("directions") List<Integer> directions,
            @JsonProperty("size") List<Boolean> size
    ){
        if(directions == null || directions.isEmpty()) throw new IllegalArgumentException("List directions cannot be empty or null");
        if(size == null || size.isEmpty()) throw new IllegalArgumentException("List size cannot be empty or null");
        if(directions.size() != size.size()) throw new IllegalArgumentException("List directions size does not match size size");
        this.directions = new ArrayList<>(directions);
        this.size = new ArrayList<>(size);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
            visitor.visit(this);
    }

    public List<Integer> getMeteorites_directions(){ return new ArrayList<>(directions); }

    public List<Boolean> getMeteorites_size(){ return new ArrayList<>(size); }
}
