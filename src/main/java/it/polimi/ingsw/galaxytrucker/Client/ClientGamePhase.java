package it.polimi.ingsw.galaxytrucker.Client;
/**
 * Represents the different phases of the game as seen by the client.
 * These values are used to control UI rendering and client-side logic,
 * ensuring the interface matches the current server-side game state.
 * Phases include:
 * - MAIN_MENU: the initial menu screen
 * - WAITING_IN_LOBBY: waiting for players before starting the game
 * - BOARD_SETUP: placing the tiles
 * - TILE_MANAGEMENT: standard tile placement phase
 * - TILE_MANAGEMENT_AFTER_RESERVED: continued tile placement after reserved tiles
 * - WAITING_FOR_PLAYERS: waiting for others to finish their actions
 * - WAITING_FOR_TURN: waiting for this player's turn
 * - DRAW_PHASE: drawing and handling new cards
 * - CARD_EFFECT: resolving effects of an active card
 * - SCORING: final scoring and ranking
 * - EXIT: the game has ended or the player left
 * @author Oleg Nedina
 */

public enum ClientGamePhase {
    MAIN_MENU,
    WAITING_IN_LOBBY,
    BOARD_SETUP,
    TILE_MANAGEMENT,
    TILE_MANAGEMENT_AFTER_RESERVED,
    WAITING_FOR_PLAYERS,
    WAITING_FOR_TURN,
    DRAW_PHASE,
    CARD_EFFECT,
    SCORING ,
    EXIT,
}

