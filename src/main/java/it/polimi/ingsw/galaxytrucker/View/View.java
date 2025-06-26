package it.polimi.ingsw.galaxytrucker.View;
import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents a generic view interface for the client.
 * Defines the contract between the controller and the user interface,
 * whether it's textual (TUI) or graphical (GUI).
 * The methods in this interface allow the view to:
 * - Display game elements (tiles, cards, ship, deck)
 * - Prompt the user for input (strings, coordinates, indices)
 * - Report errors and state changes
 * - Update game visuals like player dashboards and map positions
 * Implementations must ensure passive behavior: input must be awaited or buffered
 * without active control logic.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */

public interface View {

    /**
     * Displays a generic message to the user (e.g. updates, notifications).
     * @param message the message to display
     */
    void inform(String message);
    /**
     * Asks the user a yes/no question and returns their response.
     * @param message the question to ask
     * @return true if the user answers affirmatively, false otherwise
     */
    Boolean ask(String message);
    /**
     * Prompts the user to enter a pair of coordinates.
     * @return an array of two integers: [row, column]
     * @throws IOException if an input error occurs
     * @throws InterruptedException if the input process is interrupted
     */
    int[] askCoordinate() throws IOException, InterruptedException;
    /**
     * Prompts the user to select an index from a list of elements.
     * @return the chosen index
     * @throws IOException if an input error occurs
     * @throws InterruptedException if the input process is interrupted
     */
    Integer askIndex() throws IOException, InterruptedException;
    /**
     * Starts the view, activating input listeners and rendering logic.
     * Called once during the initialization phase.
     */
    void start();
    /**
     * Displays a list of available goods to the user.
     * @param Goods the list of goods as string representations
     */
    void printListOfGoods(List<String> Goods);
    /**
     * Renders the current state of the player's ship dashboard.
     * @param ship a 2D array representing the ship's tile layout
     */
    void printDashShip(ClientTile[][] ship);
    /**
     * Updates the view with the player's current status.
     * @param nickname the player's name
     * @param firePower the player's total firepower
     * @param powerEngine the player's total engine power
     * @param credits the number of credits the player has
     * @param purpleAlien true if a purple alien is present on the ship
     * @param brownAlien true if a brown alien is present on the ship
     * @param numberOfHuman the total number of human tokens on the ship
     * @param numberOfEnergy the total amount of energy available
     */
    void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy);

    /**
     * Displays the current deck of cards visible to the player.
     * @param deck the list of cards in the deck
     */
    void printDeck(List<ClientCard> deck);
    /**
     * Displays a generic message indicating that the tile pile is covered.
     * Typically shown when tiles are not visible to the player.
     */
    void printPileCovered();
    /**
     * Displays the currently revealed tiles in the shared pile.
     * @param tiles the list of visible tiles
     */
    void printPileShown(List<ClientTile> tiles);
    /**
     * Prompts the user to input a string.
     * @return the string entered by the user
     */
    String askString();
    /**
     * Displays an error message to the user.
     * @param message the error message
     */
    void reportError(String message);
    /**
     * Updates the view to reflect a change in the current game phase.
     * @param gamePhase the new game phase
     */
    void updateState(ClientGamePhase gamePhase);
    /**
     * Displays information about a single tile.
     * @param tile the tile to display
     */
    void printTile(ClientTile tile);
    /**
     * Displays information about a single card.
     * @param card the card to display
     */
    void printCard(ClientCard card);

    /**
     * Returns the command chosen by the user from the available options.
     * Typically used in GUI to resolve passive input as a string command.
     * @return the selected command as a string
     * @throws Exception if an error occurs during input
     */
    String sendAvailableChoices() throws Exception;

    /**
     * Updates the visual representation of the map with player positions.
     *
     * @param map a mapping of player nicknames to their [row, column] positions
     */
    void updateMap(Map<String, int[] > map);

    /**
     * Prompts the user to select another player (e.g., for targeting).
     * @return the selected player's name
     * @throws IOException if an input error occurs
     * @throws InterruptedException if the input is interrupted
     */
    String choosePlayer() throws IOException, InterruptedException;

    /**
     * Displays the list of available commands or actions the player can perform.
     */
    void printListOfCommand();

    /**
     * Sets the view in demo mode or normal mode.
     * Demo mode may alter visual feedback, interactivity, or board layout.
     * @param demo true to enable demo mode, false for normal mode
     */
    void setIsDemo(Boolean demo);

    /**
     * Checks whether a specific cell on the ship is marked as valid.
     * @param a the row index
     * @param b the column index
     * @return true if the position is valid for placement, false otherwise
     */
    boolean returnValidity(int a , int b);

    /**
     * Marks a specific cell on the ship as valid (used).
     * @param a the row index
     * @param b the column index
     */
    void setValidity(int a , int b);

    /**
     * Resets a specific cell on the ship to invalid or free.
     * @param a the row index
     * @param b the column index
     */
    void resetValidity(int a , int b);

    /**
     * Returns the current game phase stored in the view.
     * @return the current ClientGamePhase
     */
    ClientGamePhase getGamePhase();

    /**
     * Displays the current map with all players' positions.
     * Used to visualize player progress or locations during the flight phase.
     */
    void printMapPosition();

    /**
     * Asks the user a yes/no question with a time limit.
     * @param message the question to display
     * @return true if the user answers affirmatively in time, false otherwise
     */
    boolean askWithTimeout(String message);

    /**
     * Prompts the user to select an index within a timeout window.
     * @return the chosen index, or null if no response was given in time
     */
    Integer askIndexWithTimeout();

    /**
     * Prompts the user to enter coordinates within a timeout window.
     * @return an array of two integers: [row, column]
     */
    int[] askCoordinatesWithTimeout();

    /**
     * Displays a list of available games that the user can join.
     * @param availableGames a map where the key is the game ID, and the value is game metadata
     */
    void displayAvailableGames(Map<Integer, int[]> availableGames);

    /**
     * Sets a tile at the specified position on the player's ship dashboard.
     * @param tile the tile to place
     * @param row the row index
     * @param col the column index
     */
    void setTile(ClientTile tile, int row, int col);

    /**
     * Sets the currently selected or drawn tile.
     * @param tile the tile to set as current
     */
    void setCurrentTile(ClientTile tile);

    /**
     * Sets the nickname of the current player.
     * @param cognomePradella the player's nickname
     */
    void setNickName(String cognomePradella);

    /**
     * Prompts the user to choose a game to join from the list of available games.
     * @param availableGames a map where the key is the game ID, and the value is game metadata
     * @return the ID of the selected game
     * @throws IOException if an input error occurs
     * @throws InterruptedException if the input is interrupted
     */
    int askGameToJoin(Map<Integer, int[]> availableGames) throws IOException, InterruptedException;

}