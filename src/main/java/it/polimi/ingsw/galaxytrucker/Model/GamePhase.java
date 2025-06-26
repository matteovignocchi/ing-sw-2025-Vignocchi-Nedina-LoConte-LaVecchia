package it.polimi.ingsw.galaxytrucker.Model;
/**
 * Enumeration representing the various phases of the game and the corresponding actions
 * that can be displayed to the client.
 * Each enum constant defines a specific game phase, allowing the client-side view to
 * adapt its interface and behavior accordingly.
 * @author Oleg Nedina
 */

public enum GamePhase {
    WAITING_IN_LOBBY,
    BOARD_SETUP,
    TILE_MANAGEMENT,
    TILE_MANAGEMENT_AFTER_RESERVED,
    WAITING_FOR_PLAYERS,
    WAITING_FOR_TURN ,
    DRAW_PHASE,
    CARD_EFFECT,
    SCORING ,
    EXIT,
}