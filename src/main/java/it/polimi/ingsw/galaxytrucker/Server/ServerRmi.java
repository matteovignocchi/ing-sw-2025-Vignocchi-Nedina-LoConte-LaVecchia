package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * RMI server implementation of the VirtualServer interface.
 * Delegates game operations to a GameManager instance and exposes them over RMI,
 * centralizing exception handling and input validation.
 *
 * @author Gabriele La Vecchia
 * @author Francesco Lo Conte
 */
public class ServerRmi extends UnicastRemoteObject implements VirtualServer {
    private final GameManager gameManager;

    /**
     * Constructs a ServerRmi with the given GameManager.
     *
     * @param gameManager the GameManager to which calls will be forwarded
     * @throws RemoteException if stub export fails
     */
    public ServerRmi(GameManager gameManager) throws RemoteException {
        super();
        this.gameManager = gameManager;
    }

    /**
     * Helper that invokes a GameManager method and translates exceptions
     * into RemoteException or BusinessLogicException with contextual messages.
     *
     * @param methodName the name of the GameManager method being called
     * @param call       the functional interface encapsulating the GameManager call
     * @param <T>        the return type of the GameManager method
     * @return the result of the GameManager call
     * @throws RemoteException         if a network or I/O error occurs
     * @throws BusinessLogicException  if the GameManager signals a game logic error
     */
    private <T> T handleGameManagerCall(String methodName, GameManagerCall<T> call) throws RemoteException, BusinessLogicException {
        try {
            return call.execute(); // Esegue la chiamata al metodo del GameManager
        } catch (BusinessLogicException e) {
            throw new BusinessLogicException("Logic error in " + methodName + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteException("I/O error in " + methodName + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RemoteException("Unexpected error in " + methodName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Functional interface for wrapping a call to a GameManager method.
     *
     * @param <T> the return type of the wrapped call
     */
    private interface GameManagerCall<T> {
        /**
         * Executes the encapsulated GameManager call.
         *
         * @return the result of the call
         * @throws Exception if any error occurs during execution
         */
        T execute() throws Exception;
    }

    /**
     * Creates a new game session and returns its ID.
     * Validates inputs before delegating to GameManager.createGame.
     *
     * @param isDemo     true to run in demo mode, false otherwise
     * @param v          the VirtualView for the creating player
     * @param nickname   the nickname of the creating player
     * @param maxPlayers the maximum number of players (2–4)
     * @return the newly created game ID
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if a game logic error occurs during creation
     */
    @Override
    public int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException {
        if (v == null) { throw new RemoteException("VirtualView cannot be null"); }
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (maxPlayers < 2 || maxPlayers > 4) { throw new RemoteException("the maximum number of players must be between 2 and 4"); }

        return handleGameManagerCall("createNewGame", () -> gameManager.createGame(isDemo, v, nickname, maxPlayers));
    }

    /**
     * Adds a player to an existing game.
     *
     * @param gameId   the ID of the game to join
     * @param v        the VirtualView for the joining player
     * @param nickname the nickname of the joining player
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if a game logic error prevents joining
     */
    @Override
    public void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException {
        if (v == null) { throw new RemoteException("VirtualView cannot be null"); }
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("enterGame", () -> {
            gameManager.joinGame(gameId, v, nickname);
            return null;
        });
    }


    /**
     * Removes a player from a game and ends the session.
     *
     * @param gameId   the ID of the game to leave
     * @param nickname the nickname of the leaving player
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if a game logic error occurs during quit
     */
    @Override
    public void LeaveGame(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("logOut", () -> {
            gameManager.quitGame(gameId, nickname);
            return null;
        });
    }

    /**
     * Retrieves the covered tile for the specified player.
     *
     * @param gameId   the ID of the game
     * @param nickname the nickname of the player
     * @return a JSON string representing the covered tile
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if a game logic error occurs
     */
    @Override
    public String getCoveredTile(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty");}

        return handleGameManagerCall("getCoveredTile", () -> gameManager.getCoveredTile(gameId, nickname));
    }

    /**
     * Retrieves the list of uncovered tiles available for the player to choose from.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @return a JSON string containing the list of uncovered tiles
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the uncovered tiles list is empty or game state is invalid
     */
    @Override
    public String getUncoveredTilesList(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        return handleGameManagerCall("getUncoveredTilesList", () -> gameManager.getUncoveredTilesList(gameId));
    }


    /**
     * Allows a player to choose one of the uncovered tiles by its ID.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param idTile   the index of the uncovered tile to choose
     * @return a JSON string representing the chosen tile
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the tile ID is invalid or game state is invalid
     */
    @Override
    public String chooseUncoveredTile(int gameId, String nickname, int idTile) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (idTile < 0) { throw new RemoteException("Tile must be greater than 0"); }

        return handleGameManagerCall("chooseUncoveredTile", () -> gameManager.chooseUncoveredTile(gameId, nickname, idTile));
    }

    /**
     * Drops a specified tile from the player's ship.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param tile     the JSON string representing the tile to drop
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the tile cannot be dropped due to game rules
     */
    @Override
    public void dropTile(int gameId, String nickname, String tile) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (tile == null) { throw new RemoteException("Tile cannot be null"); }

        handleGameManagerCall("dropTile", () -> {
            gameManager.dropTile(gameId, nickname, tile);
            return null;
        });
    }

    /**
     * Places a specified tile on the player's ship at given coordinates.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param tile     the JSON string representing the tile to place
     * @param cord     the [x, y] coordinates on the ship grid where the tile should be placed
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the tile cannot be placed due to game rules or invalid coordinates
     */
    @Override
    public void placeTile(int gameId, String nickname, String tile, int[] cord) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (tile == null) { throw new RemoteException("Tile cannot be null"); }
        if (cord == null || cord.length != 2 || cord[0] < 0 || cord[1] < 0) { throw new RemoteException("Invalid parameters"); }

        handleGameManagerCall("placeTile", () -> {
            gameManager.placeTile(gameId, nickname, tile, cord);
            return null;
        });
    }

    /**
     * Marks a player as ready to proceed to the flight phase.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the player cannot be marked ready due to game state
     */
    @Override
    public void setReady(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("setReady", () -> {
            gameManager.setReady(gameId, nickname);
            return null;
        });
    }

    /**
     * Flips the hourglass timer for a player.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the timer cannot be flipped due to game state
     */
    @Override
    public void rotateGlass(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        handleGameManagerCall("flipHourglass", () -> {
            gameManager.flipHourglass(gameId, nickname);
            return null;
        });
    }


    /**
     * Returns the JSON representation of a specific card deck.
     *
     * @param gameId   the identifier of the game
     * @param idxDeck  the index of the deck to show (e.g., 0, 1, 2)
     * @return a JSON string representing the cards in the requested deck
     * @throws RemoteException        if an RMI error occurs
     * @throws BusinessLogicException if the deck index is invalid
     */
    @Override
    public String showDeck(int gameId, int idxDeck) throws RemoteException, BusinessLogicException {
        return handleGameManagerCall("showDeck", () -> gameManager.showDeck(gameId, idxDeck));
    }

    /**
     * Retrieves a map of games currently waiting for more players.
     *
     * @return a map from game ID to a three-element array:
     *         [connected players, max players, demo flag (1 for demo, 0 otherwise)]
     * @throws RemoteException        if an RMI error occurs
     * @throws BusinessLogicException never thrown in this implementation
     */
    @Override
    public Map<Integer,int[]> requestGamesList() throws RemoteException, BusinessLogicException {
        return handleGameManagerCall("requestGamesList", gameManager::listActiveGames);
    }

    /**
     * Returns the dashboard matrix for a given player.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @return a 2D array representing the player's dashboard
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the game or player is invalid
     */
    @Override
    public String[][] lookAtDashBoard(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        return handleGameManagerCall("lookDashBoard", () -> gameManager.lookAtDashBoard(nickname, gameId));
    }

    /**
     * Draws a card for the specified player.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the player cannot draw a card due to game state
     */
    @Override
    public void drawCard(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        handleGameManagerCall("drawCard", () -> { gameManager.drawCard(gameId, nickname); return null; });
    }

    /**
     * Logs out a player from the server.
     *
     * @param nickname the nickname of the player to log out
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException never thrown in this implementation
     */
    @Override
    public void logOut(String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        handleGameManagerCall("logout", () -> {
            gameManager.logout(nickname);
            return null;
        });
    }

    /**
     * Attempts to log in a player and returns their game ID if reconnecting.
     *
     * @param nickname the nickname of the player logging in
     * @param v        the VirtualView associated with the player
     * @return the existing game ID if reconnecting, or 0 for a new session
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the nickname is already in use
     */
    @Override
    public int logIn(String nickname, VirtualView v) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        return handleGameManagerCall("login", () -> gameManager.login(nickname, v));
    }


    /**
     * Retrieves a reserved tile for a player by its index.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param id       the index of the reserved tile to retrieve
     * @return a JSON string representing the reserved tile
     * @throws RemoteException        if validation fails or an RMI error occurs
     * @throws BusinessLogicException if the reserved tile index is invalid
     */
    @Override
    public String getReservedTile(int gameId, String nickname , int id) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        return handleGameManagerCall("getReservedTile", () -> gameManager.getReservedTile(gameId, nickname, id));
    }

}

