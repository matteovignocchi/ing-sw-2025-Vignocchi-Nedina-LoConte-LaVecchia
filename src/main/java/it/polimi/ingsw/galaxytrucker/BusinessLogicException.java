package it.polimi.ingsw.galaxytrucker;

import java.io.Serializable;

//TODO: mettere tale classe in un jar condiviso, in modo che anche il client la può chiamare
public class BusinessLogicException extends Exception implements Serializable {
    public BusinessLogicException(String message) {
        super(message);
    }
    public BusinessLogicException(String msg, Throwable cause) { super(msg, cause); }
}
