package it.polimi.ingsw.galaxytrucker.Model.Tile;
/**
 * Exception thrown when attempting to store a dangerous (red) good
 * in a non-advanced storage unit.
 * Used to enforce game rules that restrict hazardous goods to advanced containers only.
 * @author Oleg Nedina
 */

public class TooDangerous extends RuntimeException {
    public TooDangerous(String message) {
        super(message);
    }
}
