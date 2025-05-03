package it.polimi.ingsw.galaxytrucker.Model.Card;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;

/**
 *Custom exception that handles anomalous behaviors in cards.
 * It is extended by other exceptions and returns a message describing the type of error that led to its generation.
 */

public abstract class CardEffectException extends BusinessLogicException {
    public CardEffectException(String message) {
        super(message);
    }
}
