package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;

import java.io.Serializable;

/**
 * This class handles the OpenSpaceCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 */

public class OpenSpaceCard implements Card, Serializable {

    private final String idCard;

    @JsonCreator
    public OpenSpaceCard(
            @JsonProperty("id_card") String idCard
    ) {
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");

        this.idCard = idCard;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException {
        try {
            visitor.visit(this);
        } catch (BusinessLogicException e) {
            throw new RuntimeException(e);
        }
    }

    /** Restituisce l’ID univoco di questa carta, es. "0_01" */
    public String getIdCard() { return idCard; }
}
