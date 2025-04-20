package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

/**
 * Exception that extends CardEffectException and takes care of handling cases where
 * the size of the parameters is not admissible.
 */

public class InvalidSizeException extends CardEffectException {
    public InvalidSizeException(String message) {
        super(message);
    }
}
