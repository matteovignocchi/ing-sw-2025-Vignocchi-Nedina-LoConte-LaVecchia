package it.polimi.ingsw.galaxytrucker.Model.Tile;

/**
 * Exception thrown when trying to add a human to a housing unit that has reached its capacity.
 * Used to enforce the limit of occupants allowed in a housing tile.
 */
public class FullHousingList extends RuntimeException {
    public FullHousingList(String message) {
        super(message);
    }
}
