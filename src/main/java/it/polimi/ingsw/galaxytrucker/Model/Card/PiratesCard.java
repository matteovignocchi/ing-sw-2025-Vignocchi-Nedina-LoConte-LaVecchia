package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the PiratesCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PiratesCard implements Card, Serializable {
    private final String idCard;
    private final int fire_power;
    private final int days;
    private final int credits;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    /**
     * PiratesCard constructor with specific values.
     * @param fire_power: firepower needed to beat the pirate card.
     * @param days: flight days that the player who accepts loses.
     * @param credits: credits earned by the player who accepts.
     * @param shots_directions: list of attack directions for players beaten by pirates.
     * @param shots_size: Attack size list for players beaten by pirates.
     */

    @JsonCreator
    public PiratesCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("fire_power") int fire_power,
            @JsonProperty("days") int days,
            @JsonProperty("credits") int credits,
            @JsonProperty("shots_directions") List<Integer> shots_directions,
            @JsonProperty("shots_size") List<Boolean> shots_size
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(shots_directions == null || shots_directions.isEmpty()) throw new IllegalArgumentException("List shots_directions cannot be null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new IllegalArgumentException("List shots_size cannot be null or empty");
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("List shots_directions and shots_size must be the same size");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power cannot be negative");
        if(days <= 0) throw new IllegalArgumentException("days cannot be negative");
        if(credits <= 0) throw new IllegalArgumentException("credits cannot be negative");

        this.idCard = idCard;
        this.fire_power = fire_power;
        this.days = days;
        this.credits = credits;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        try {
            visitor.visit(this);
        } catch (BusinessLogicException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return firepower shown on the card.
     */

    public int getFirePower(){return fire_power;}

    /**
     * @return flight days shown on the card.
     */

    public int getDays(){return days;}

    /**
     * @return credits shown on the card.
     */

    public int getCredits(){return credits;}

    /**
     * @return list of attack directions shown on the card.
     */

    public List<Integer> getShots_directions(){return new ArrayList<>(shots_directions);}

    /**
     * @return attack size list shown on the card.
     */

    public List<Boolean> getShots_size(){return new ArrayList<>(shots_size);}
    public String getIdCard(){return idCard;}
}
