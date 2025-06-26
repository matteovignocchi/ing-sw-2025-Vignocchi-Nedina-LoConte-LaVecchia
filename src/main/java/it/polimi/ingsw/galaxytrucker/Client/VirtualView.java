package it.polimi.ingsw.galaxytrucker.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Defines the contract between the server and the client (TUI or GUI) for all game interactions.
 * This interface is used remotely by the server to push updates or request input from the client.
 * Each implementation (e.g., TUI or GUI) should react accordingly to render views or collect input.
 * @author Oleg Nedin && Matteo Vignocchi
 */
public interface VirtualView extends Remote {

    // === Rendering Methods ===

    /** Displays a generic message to the user. */
    void inform(String message) throws Exception;

    /** Shows updated player statistics and ship values. */
    void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException;

    /** Displays an error message to the user. */
    void reportError(String error) throws Exception;

    /** Prints a card description or visual to the screen. */
    void printCard(String card) throws Exception;

    /** Displays the list of covered tiles. */
    void printListOfTileCovered(String tiles) throws Exception;

    /** Displays the list of shown (uncovered) tiles. */
    void printListOfTileShown(String tiles) throws Exception;

    /** Displays the player's dashboard matrix. */
    void printPlayerDashboard(String[][] dashboard) throws Exception;

    /** Displays a list of collected goods. */
    void printListOfGoods(List<String> listOfGoods) throws Exception;

    /** Displays a single tile representation. */
    void printTile(String tile) throws Exception;

    /** Displays the deck currently being viewed. */
    void printDeck(String deck) throws Exception;

    /** Sets the game ID associated with this client. */
    void setGameId(int gameId) throws RemoteException;


    // === Input Request Methods ===

    /** Asks the user a yes/no question and returns their answer. */
    Boolean ask(String message) throws Exception;

    /** Prompts the user to select an index (e.g., from a list). */
    Integer askIndex() throws Exception;

    /** Prompts the user to input a string (e.g., a name). */
    String askString() throws Exception;

    /** Prompts the user to select a coordinate on the dashboard. */
    int[] askCoordinate() throws Exception;


    // === Game Status and Lifecycle ===

    /** Updates the displayed game phase (e.g., BUILDING, FLIGHT, END). */
    void updateGameState(String fase) throws Exception;

    /** Sends a login request to the server with the chosen username. */
    int sendLogin(String username) throws Exception;

    /** Requests to create or join a game from the client menu. */
    int sendGameRequest(String message, int numberOfPlayer, Boolean isdemo) throws Exception;


    // === Server Interaction Methods ===

    /** Requests a covered tile from the central deck. */
    String getTileServer() throws Exception;

    /** Prompts the user to choose and retrieve a tile from the uncovered tile list. */
    String getUncoveredTile() throws Exception;

    /** Sends back a tile the player chose not to place. */
    void getBackTile(String tile) throws Exception;

    /** Requests to place a tile on the dashboard at a given coordinate. */
    void positionTile(String tile) throws Exception;

    /** Requests to draw the next event card. */
    void drawCard() throws Exception;

    /** Requests to flip the hourglass. */
    void rotateGlass() throws Exception;

    /** Signals that the player is ready to proceed. */
    void setReady() throws Exception;

    /** Requests to view the content of a deck. */
    void lookDeck() throws Exception;

    /** Requests to view another player's dashboard. */
    void lookDashBoard() throws Exception;

    /** Requests to leave the current game. */
    void leaveGame() throws Exception;

    /** Logs out from the server and terminates the session. */
    void logOut() throws Exception;

    /** Sets the player nickname. */
    void setNickname(String nickname) throws Exception;

    /** Updates the map with current player positions. */
    void updateMapPosition(Map<String, int[]> Position) throws Exception;

    /** Notifies the client that the game has officially started. */
    void setStart() throws Exception;

    /** Blocks until {@code setStart()} is called, then returns "start". */
    String askInformationAboutStart() throws Exception;

    /** Sets the current tile to be manipulated. */
    void setTile(String jsonTile) throws Exception;

    /** Enables or disables demo mode in the client. */
    void setIsDemo(Boolean demo) throws Exception;

    /** Requests to retrieve a reserved tile from the ship. */
    String takeReservedTile() throws Exception;

    /** Updates the ship's dashboard matrix with a new state. */
    void updateDashMatrix(String[][] data) throws Exception;


    // === Client Controller Setup ===

    /** Sets the {@link ClientController} instance managing this view. */
    void setClientController(ClientController clientController) throws Exception;


    // === Input Request with Timeout ===

    /** Asks the user a yes/no question with timeout. */
    boolean askWithTimeout(String question) throws Exception;

    /** Prompts for coordinates with timeout. */
    int[] askCoordsWithTimeout() throws Exception;

    /** Prompts for index selection with timeout. */
    Integer askIndexWithTimeout() throws Exception;
}
