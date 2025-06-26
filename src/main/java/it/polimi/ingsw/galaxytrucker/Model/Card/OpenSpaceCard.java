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

    /**
     * OpenSpaceCard constructor
     * @param idCard card's id
     */
    @JsonCreator
    public OpenSpaceCard(
            @JsonProperty("id_card") String idCard
    ) {
        if (idCard == null || idCard.isBlank()) throw new IllegalArgumentException("id_card cannot be null or empty");

        this.idCard = idCard;
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
     * @return card's id
     */
    public String getIdCard() { return idCard; }
}
