package it.polimi.ingsw.galaxytrucker.Model.Tile;

/**
 * Represents the types of crew tokens and alien connections used in housing units.
 * This enum is used both to identify the type of occupant inside a housing unit
 * and to define the type of connection between two connected housing tiles.
 * Types include:
 * - HUMAN: a standard human crew member
 * - PURPLE_ALIEN: a purple alien occupant or connection
 * - BROWN_ALIEN: a brown alien occupant or connection
 * - HUMAN234: a special or placeholder variant of HUMAN, possibly for extended behavior
 */
public enum Human {
    HUMAN, PURPLE_ALIEN, BROWN_ALIEN , HUMAN234;
}
