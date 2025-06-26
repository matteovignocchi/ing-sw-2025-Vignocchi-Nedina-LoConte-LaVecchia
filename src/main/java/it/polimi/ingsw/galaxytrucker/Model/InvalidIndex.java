package it.polimi.ingsw.galaxytrucker.Model;
/**
 * Exception thrown whenever an invalid index is encountered.
 * Typically used to signal that a requested index is out of bounds
 * or does not correspond to a valid element in the current context.
 */

public class InvalidIndex extends RuntimeException {
    public InvalidIndex(String message) {
        super(message);
    }
}
