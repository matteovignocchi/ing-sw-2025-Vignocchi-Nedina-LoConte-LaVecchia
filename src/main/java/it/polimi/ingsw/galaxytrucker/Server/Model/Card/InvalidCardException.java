package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

public class InvalidCardException extends CardEffectException {
    public InvalidCardException(String message) {
        super(message);
    }
}
