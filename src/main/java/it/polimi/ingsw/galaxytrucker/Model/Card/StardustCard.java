package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;

import java.io.Serializable;

/**
 * This class handles the StardustCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 */
public class StardustCard implements Card, Serializable {

    private final String idCard;

    @JsonCreator
    public StardustCard(
            @JsonProperty("id_card") String idCard
    ) {
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");

        this.idCard = idCard;
    }

    @Override
    public void accept(CardVisitor visitor) throws BusinessLogicException {
        visitor.visit(this);
    }

    /** Restituisce l’ID univoco di questa carta, es. "0_03" */
    public String getIdCard() { return idCard; }
}
