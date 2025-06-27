package it.polimi.ingsw.galaxytrucker.Exception;

import java.io.Serializable;

/**
 * Checked Exception thrown for logic game exceptions
 */
public class BusinessLogicException extends Exception implements Serializable {
    public BusinessLogicException(String message) {
        super(message);
    }
    public BusinessLogicException(String msg, Throwable cause) { super(msg, cause); }
}
