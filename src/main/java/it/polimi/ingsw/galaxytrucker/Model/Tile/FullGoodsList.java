package it.polimi.ingsw.galaxytrucker.Model.Tile;

/**
 * Exception thrown when trying to add a good to a storage unit that is already full.
 * Used to prevent exceeding the maximum capacity of a tile that holds goods.
 */

public class FullGoodsList extends RuntimeException {
    public FullGoodsList(String message) {
        super(message);
    }
}
