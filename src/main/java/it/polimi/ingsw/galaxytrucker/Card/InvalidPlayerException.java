package it.polimi.ingsw.galaxytrucker.Card;

import javax.smartcardio.CardException;

public class InvalidPlayerException extends CardEffectException {
    public InvalidPlayerException(String message) {
        super(message);
    }
}
