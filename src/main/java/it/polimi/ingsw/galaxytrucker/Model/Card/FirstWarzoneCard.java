package it.polimi.ingsw.galaxytrucker.Model.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the FirstWarzoneCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */

public class FirstWarzoneCard implements Card, Serializable {
    private final String idCard;
    private final int days;
    private final int num_crewmates;
    private final List<Integer> shots_directions;
    private final List<Boolean> shots_size;

    /**
     * FirstWarzoneCard constructor with specific values.
     * @param days: flight days lost by the player with the least crewmates.
     * @param num_crewmates: number of crewmates the player with the least engine power loses.
     * @param shots_directions: list of attack directions for the player with the least firepower.
     * @param shots_size: Attack size list for the player with the least firepower.
     */

    @JsonCreator
    public FirstWarzoneCard(
            @JsonProperty("id_card") String idCard,
            @JsonProperty("days") int days,
            @JsonProperty("num_crewmates") int num_crewmates,
            @JsonProperty("shots_directions") List<Integer> shots_directions,
            @JsonProperty("shots_size") List<Boolean> shots_size
    ){
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");
        if(shots_directions == null || shots_directions.isEmpty()) throw new IllegalArgumentException("List shots_directions is null or empty");
        if(shots_size == null || shots_size.isEmpty()) throw new IllegalArgumentException("List shots_size is null or empty");
        if(shots_directions.size() != shots_size.size()) throw new IllegalArgumentException("Different Lists' dimensions");
        if(days <= 0) throw new IllegalArgumentException("Days must be greater than 0");
        if(num_crewmates <= 0) throw new IllegalArgumentException("Number of Crewmates must be greater than 0");

        this.idCard = idCard;
        this.days = days;
        this.num_crewmates = num_crewmates;
        this.shots_directions = new ArrayList<>(shots_directions);
        this.shots_size = new ArrayList<>(shots_size);
    }

    /**
     * Accepts a CardVisitor to process this card.
     *
     * @param visitor the CardVisitor that will handle this card
     * @throws BusinessLogicException if a business logic error occurs during processing
     */

    @Override
    public void accept(CardVisitor visitor) throws BusinessLogicException {
        visitor.visit(this);
    }

    /**
     * @return flight days shown on the card.
     */

    public int getDays() {return days;}

    /**
     * @return number of crewmates shown on the card.
     */

    public int getNumCrewmates() {return num_crewmates;}

    /**
     * @return list of attack directions shown on the card.
     */

    public List<Integer> getShotsDirections() {return new ArrayList<>(shots_directions);}

    /**
     * @return attack size list shown on the card.
     */

    public List<Boolean> getShotsSize() {return new ArrayList<>(shots_size);}

    /**
     * @return card's id
     */

    public String getIdCard() {return idCard;}
}
