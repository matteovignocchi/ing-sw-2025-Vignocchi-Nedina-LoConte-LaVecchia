package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the MeteoritesRainCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class MeteoritesRainCard implements Card, Serializable {
    private final String idCard;
    private final List<Integer> directions;
    private final List<Boolean> size;

    /**
     * MeteoritesRainCard constructor with specific values.
     * @param directions: list of attack directions for all players.
     * @param size:Attack size list for all players.
     */

    @JsonCreator
    public MeteoritesRainCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("directions") List<Integer> directions,
            @JsonProperty("size") List<Boolean> size
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(directions == null || directions.isEmpty()) throw new IllegalArgumentException("List directions cannot be empty or null");
        if(size == null || size.isEmpty()) throw new IllegalArgumentException("List size cannot be empty or null");
        if(directions.size() != size.size()) throw new IllegalArgumentException("List directions size does not match size size");

        this.idCard = idCard;
        this.directions = new ArrayList<>(directions);
        this.size = new ArrayList<>(size);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    /**
     * @return list of attack directions shown on the card.
     */

    public List<Integer> getMeteorites_directions(){ return new ArrayList<>(directions); }

    /**
     * @return attack size list shown on the card.
     */

    public List<Boolean> getMeteorites_size(){ return new ArrayList<>(size); }
    public String getIdCard(){ return idCard; }
}
