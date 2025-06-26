package it.polimi.ingsw.galaxytrucker.Model;

/**
 * Exception thrown when there are no more available energy.
 * This exception is typically used to indicate that a requested energy cannot
 * be retrieved or assigned because the supply has been exhausted.
 */

public class NoEnergyLeft extends RuntimeException {
    public NoEnergyLeft(String message) {
        super(message);
    }
}
