package it.polimi.ingsw.galaxytrucker.Model.Tile;
/**
 * Exception thrown when an operation is attempted on a housing unit
 * that contains no humans or is otherwise empty.
 * Typically used to signal invalid access or removal from an empty housing unit.
 */
public class EmptyHousingList extends RuntimeException {
    public EmptyHousingList(String message) {
        super(message);
    }
}
