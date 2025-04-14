package it.polimi.ingsw.galaxytrucker.Card;

import javax.smartcardio.CardException;

public class InvalidPlayerException extends RuntimeException {
    public InvalidPlayerException(String message) {
        super(message);
    }
}
