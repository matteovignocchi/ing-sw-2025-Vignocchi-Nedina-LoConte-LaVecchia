package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

/**
 *Exception that extends CardEffectException and takes care of handling cases where a card is not suitable
 */

public class InvalidCardException extends CardEffectException {
    public InvalidCardException(String message) {
        super(message);
    }
}
