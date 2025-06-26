package it.polimi.ingsw.galaxytrucker.Model.Tile;
/**
 * Represents the status of a tile position on the player's ship dashboard.
 * Used to define whether a specific position can be used, is already occupied,
 * or is permanently blocked.
 * Values:
 * - BLOCK: the position is permanently unavailable for tile placement
 * - FREE: the position is currently available for placing a tile
 * - USED: the position is already occupied by a tile
 * @author Oleg Nedina
 * @author Matteo VIgnocchi
 */
public enum Status {
    BLOCK, FREE , USED ;
}
