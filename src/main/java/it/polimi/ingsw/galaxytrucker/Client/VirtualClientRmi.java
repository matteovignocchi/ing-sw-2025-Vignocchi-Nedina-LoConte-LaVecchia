package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of the {@link VirtualView} interface using Java RMI (Remote Method Invocation) for network communication.
 * This class acts as a remote proxy representing a connected client on the server side.
 * It is bound via RMI and exposed to the server, which can invoke methods to push game updates or request input from the user.
 * The {@code VirtualClientRmi} instance receives commands and data from the server and delegates all logic to the
 * associated {@link ClientController}, which handles GUI/TUI rendering and input collection.
 * It also manages game participation, such as joining or creating a match, executing in-game actions (like placing tiles or drawing cards),
 * and synchronizing the client view with the game state.
 * This class complements {@code VirtualClientSocket}, offering the same functionality over RMI instead of raw sockets.
 *
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {

    private String nickname;
    private int gameId = 0;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private transient ClientController clientController;
    private final VirtualServer server;

    // === Initialization ===
    /**
     * Constructs a new {@code VirtualClientRmi} instance and binds it to the given RMI server.
     *
     * @param server the {@code VirtualServer} instance to associate with this client
     * @throws RemoteException if the RMI object cannot be exported
     */
    public VirtualClientRmi(VirtualServer server) throws RemoteException {
        super();
        this.server = server;
    }

    /**
     * Sets the nickname of the client. This name is used to identify the player in the game.
     *
     * @param nickname the chosen nickname of the player
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void setNickname(String nickname) throws RemoteException {
        this.nickname = nickname;
    }

    /**
     * Sets the game ID that the client is currently associated with.
     *
     * @param gameId the ID of the game session
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void setGameId(int gameId) throws RemoteException {
        this.gameId = gameId;
    }

    /**
     * Links the client to a local {@code ClientController} that handles rendering and input logic.
     *
     * @param clientController the controller responsible for local user interface interaction
     */
    @Override
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Set the client view if is demo or not.
     *
     * @param demo {@code true} to enable demo mode, {@code false} to disable it
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void setIsDemo(Boolean demo) throws RemoteException{
        clientController.setIsDemoByController(demo);
    }

    // === Rendering Methods ===

    /**
     * Displays a generic message to the client.
     *
     * @param message the message to show
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void inform(String message) throws RemoteException {
        clientController.informByController(message);
    }

    /**
     * Displays an error message to the client.
     *
     * @param error the error message
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void reportError(String error) throws RemoteException {
        clientController.reportErrorByController(error);
    }

    /**
     * Updates the player's status with detailed dashboard information.
     *
     * @param nickname the name of the player
     * @param firePower current firepower value
     * @param powerEngine current engine power value
     * @param credits number of credits
     * @param purpleAline whether a purple alien is present
     * @param brownAlien whether a brown alien is present
     * @param numberOfHuman number of human crew members
     * @param numberOfEnergy amount of available energy
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits,
                           boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        clientController.showUpdateByController(nickname, firePower, powerEngine, credits,
                purpleAline, brownAlien, numberOfHuman, numberOfEnergy);
    }

    /**
     * Replaces the local dashboard matrix with a new one received from the server.
     *
     * @param data the dashboard matrix
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateDashMatrix(String[][] data) throws RemoteException {
        clientController.newShip(data);
    }

    /**
     * Sets the current tile in the client's view.
     *
     * @param jsonTmp the tile in JSON or string-encoded format
     */
    @Override
    public void setTile(String jsonTmp) {
        clientController.setCurrentTile(jsonTmp);
    }

    /**
     * Displays the list of covered tiles to the client.
     *
     * @param jsonTiles the tiles as string or JSON representation
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printListOfTileCovered(String jsonTiles) throws RemoteException {
        clientController.printListOfTileCoveredByController();
    }

    /**
     * Displays the list of shown (uncovered) tiles to the client.
     *
     * @param jsonTiles the tiles as string or JSON representation
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printListOfTileShown(String jsonTiles) throws RemoteException {
        clientController.printListOfTileShownByController(jsonTiles);
    }

    /**
     * Displays the list of goods (cargo) to the client.
     *
     * @param listOfGoods a list of strings representing goods
     */
    @Override
    public void printListOfGoods(List<String> listOfGoods) {
        clientController.printListOfGoodsByController(listOfGoods);
    }

    /**
     * Displays the current event card to the client.
     *
     * @param jsonCard the card data as string or JSON
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printCard(String jsonCard) throws RemoteException {
        clientController.printCardByController(jsonCard);
    }

    /**
     * Displays a single tile to the client.
     *
     * @param jsonTile the tile data
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printTile(String jsonTile) throws RemoteException {
        clientController.printTileByController(jsonTile);
    }

    /**
     * Displays the player's ship dashboard to the client.
     *
     * @param jsonDashboard the dashboard matrix
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printPlayerDashboard(String[][] jsonDashboard) throws RemoteException {
        clientController.printPlayerDashboardByController(jsonDashboard);
    }

    /**
     * Displays the content of a deck to the client.
     *
     * @param jsonDeck the deck data as a string or JSON
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void printDeck(String jsonDeck) throws RemoteException {
        clientController.printDeckByController(jsonDeck);
    }

    /**
     * Updates the current game phase in the client's view.
     *
     * @param phase the name or code of the current game phase
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void updateGameState(String phase) throws RemoteException {
        if (phase == null || "PING".equalsIgnoreCase(phase)) return;
        clientController.updateGameStateByController(phase);
    }


    // === Player Input Methods ===
    /**
     * Asks the user a yes/no question and returns their response.
     *
     * @param message the question to present
     * @return {@code true} if the user answered yes, {@code false} otherwise
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public Boolean ask(String message) throws RemoteException {
        return clientController.askByController(message);
    }

    /**
     * Prompts the user to select an index from a list.
     *
     * @return the selected index, or {@code null} if cancelled or interrupted
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Override
    public Integer askIndex() throws IOException, InterruptedException {
        return clientController.askIndexByController();
    }

    /**
     * Prompts the user to select a coordinate on their dashboard.
     *
     * @return an array of two integers representing the selected coordinate
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public int[] askCoordinate() throws RemoteException {
        return clientController.askCoordinateByController();
    }

    /**
     * Prompts the user to input a string (e.g., a name ...).
     *
     * @return the user-provided string
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public String askString() throws RemoteException {
        return clientController.askStringByController();
    }

    // === Input with Timeout ===

    /**
     * Asks the user a yes/no question with a timeout.
     * If the user does not respond within the allowed time, a default response is returned.
     *
     * @param question the question to be presented to the user
     * @return {@code true} or {@code false} based on user input or timeout
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public boolean askWithTimeout(String question) throws RemoteException {
        return clientController.askWithTimeoutByController(question);
    }

    /**
     * Prompts the user to select a coordinate on the dashboard within a limited time.
     *
     * @return an array representing the selected coordinate, or {@code null} on timeout
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public int[] askCoordsWithTimeout() throws RemoteException {
        return clientController.askCoordinatesWithTimeoutByController();
    }

    /**
     * Prompts the user to select an index (e.g., from a list) within a limited time.
     *
     * @return the selected index, or {@code null} on timeout
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public Integer askIndexWithTimeout() throws RemoteException {
        return clientController.askIndexWithTimeoutByController();
    }

    // === Login & Game Setup ===

    /**
     * Sends a login request to the server using the given username.
     * Associates this RMI client as the handler for future server calls.
     *
     * @param username the username provided by the player
     * @return the player's ID if login is successful, or {@code -1} if rejected
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public int sendLogin(String username) throws RemoteException {
        try {
            return server.logIn(username, this);
        } catch (BusinessLogicException e) {
            return -1;
        }
    }

    /**
     * Handles game creation or joining, depending on the request message content.
     * If {@code message} contains {@code "CREATE"}, a new game is requested with the given parameters.
     * If {@code message} contains {@code "JOIN"}, the client queries the server for a list of available games,
     * lets the player choose one, and attempts to join.
     *
     * @param message either {@code "CREATE"} or {@code "JOIN"}
     * @param numberOfPlayer the desired number of players (used for game creation)
     * @param isDemo whether the game is a demo match
     * @return the game ID joined or created, {@code -1} if failed, or {@code 0} if canceled
     * @throws IOException if input/output errors occur
     * @throws InterruptedException if the thread is interrupted while waiting for user input
     */
    @Override
    public int sendGameRequest(String message, int numberOfPlayer, Boolean isDemo) throws IOException, InterruptedException {
        if (message.contains("CREATE")) {
            try {
                return server.createNewGame(isDemo, this, nickname, numberOfPlayer);
            } catch (BusinessLogicException e) {
                clientController.reportErrorByController("Server Error" + e.getMessage());
            }
        }

        if (message.contains("JOIN")) {
            while (true) {
                Map<Integer, int[]> availableGames;
                try {
                    availableGames = server.requestGamesList();
                } catch (BusinessLogicException e) {
                    clientController.reportErrorByController("Server error: " + e.getMessage());
                    return -1;
                }

                if (availableGames.isEmpty()) {
                    clientController.informByController("**No available games**");
                    return -1;
                }

                int choice = clientController.printAvailableGames(availableGames);
                if (choice == 0) return 0;

                try {
                    server.enterGame(choice, this, nickname);
                    return choice;
                } catch (Exception e) {
                    clientController.reportErrorByController("Cannot join: " + e.getMessage());
                    return -1;
                }
            }
        }

        return 0;
    }

    /**
     * Blocks until the server signals that the game can begin.
     * Used to coordinate the client-side waiting logic during lobby or game setup.
     *
     * @return the string {@code "start"} when the signal is received
     * @throws RemoteException if a remote communication error occurs or the wait is interrupted
     */
    @Override
    public String askInformationAboutStart() throws RemoteException {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for start", e);
        }
        return "start";
    }

    /**
     * Signals the client that the game has started.
     * This releases the latch previously blocking {@code askInformationAboutStart}.
     *
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void setStart() throws RemoteException {
        startLatch.countDown();
    }

    // === In-Game Actions ===
    /**
     * Requests a covered tile from the server for the current player.
     *
     * @return the string representation of the tile
     * @throws RemoteException if RMI communication fails
     * @throws BusinessLogicException if the server refuses to send a tile
     */
    @Override
    public String getTileServer() throws RemoteException , BusinessLogicException {
            try {
                String tmp = server.getCoveredTile(gameId, nickname);
                return tmp;
            } catch (BusinessLogicException e) {
                throw new RemoteException(e.getMessage());
            }
    }

    /**
     * Prompts the user to select an uncovered tile and requests it from the server.
     *
     * @return the selected tile
     * @throws BusinessLogicException if the list is empty or server-side logic fails
     * @throws IOException if user input fails
     * @throws InterruptedException if the thread is interrupted
     */
    @Override
    public String getUncoveredTile() throws BusinessLogicException, IOException, InterruptedException {
        String tmp;
        try {
            tmp = server.getUncoveredTilesList(gameId, nickname);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to fetch tile list: " + e.getMessage());
        }
        if (tmp == null || tmp.equals("CODE404")) {
            throw new BusinessLogicException("The list of shown tiles is empty.");
        }

        int size =  clientController.printListOfTileShownByController(tmp);
        clientController.informByController("Select a tile");
        while (true) {
            Integer indexObj = askIndex();
            if(indexObj == null) { return null; }
            int index = indexObj;
            if (index >= 0 && index < size) {
                try {
                    return server.chooseUncoveredTile(gameId, nickname, clientController.clientTileFromList(index));
                } catch (BusinessLogicException e) {
                    clientController.reportErrorByController("You missed: " + e.getMessage() + ". Select a new index.");
                }
            } else {
                clientController.informByController("Invalid index. Try again.");
            }
        }
    }

    /**
     * Returns a previously drawn tile back to the server.
     *
     * @param jsontile the tile to be returned
     * @throws RemoteException if communication fails
     * @throws BusinessLogicException if the server rejects the operation
     */
    @Override
    public void getBackTile(String jsontile) throws RemoteException , BusinessLogicException{
            try {
                server.dropTile(gameId,nickname,jsontile);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
    }

    /**
     * Sends a tile placement request to the server after the user chooses a coordinate.
     * Updates the local dashboard immediately after placement.
     *
     * @param jsonTile the tile to place
     * @throws RemoteException if a communication error occurs
     */
    @Override
    public void positionTile(String jsonTile) throws RemoteException{
        clientController.printMyDashBoardByController();
        clientController.informByController("Choose coordinate!");
        int[] tmp;
        tmp = clientController.askCoordinateByController();
        if(tmp == null) { return; }
        clientController.setTileInMatrix(jsonTile, tmp[0], tmp[1]);
        try {
            server.placeTile(gameId, nickname, jsonTile, tmp);
        } catch (BusinessLogicException e) {
            return;
        }
        clientController.printMyDashBoardByController();
    }

    /**
     * Requests the server to draw a new card for the current player.
     *
     * @throws RemoteException if communication fails
     * @throws BusinessLogicException if card drawing fails on the server
     */
    @Override
    public void drawCard() throws RemoteException , BusinessLogicException {
            try {
                server.drawCard(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
    }

    /**
     * Informs the server that the player has rotated their glass module.
     *
     * @throws RemoteException if communication fails
     * @throws BusinessLogicException if the rotation cannot be performed
     */
    @Override
    public void rotateGlass() throws RemoteException ,  BusinessLogicException {
            try {
                server.rotateGlass(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
    }

    /**
     * Notifies the server that the player is ready to proceed.
     *
     * @throws RemoteException if communication fails
     * @throws BusinessLogicException if the ready state cannot be set
     */
    @Override
    public void setReady() throws RemoteException , BusinessLogicException{
            try {
                server.setReady(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
    }

    /**
     * Allows the user to view the deck of a specified round.
     * Loops until a valid deck is chosen.
     *
     * @throws IOException if user input fails
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public void lookDeck() throws IOException, InterruptedException {
        while (true) {
            clientController.informByController("Choose deck : 1 / 2 / 3");
            Integer indexObj = askIndex();
            if(indexObj == null) { return; }

            int index = indexObj;
            if (index < 0 || index > 2) {
                clientController.reportErrorByController("Invalid choice: please enter 1, 2 or 3.");
                continue;
            }

            try {
                String deck = server.showDeck(gameId, index);
                clientController.printDeckByController(deck);
                return;
            } catch (BusinessLogicException e) {
                clientController.reportErrorByController("Index not valid.");
            } catch (IOException e) {
                clientController.reportErrorByController("Input error: please enter a number.");
            }
        }
    }

    /**
     * Shows the dashboard of a selected player by requesting it from the server.
     *
     * @throws RemoteException if communication fails
     * @throws IOException if user input fails
     * @throws InterruptedException if the thread is interrupted
     */
    @Override
    public void lookDashBoard() throws RemoteException,IOException, InterruptedException {
        String tmp;

        tmp = clientController.choosePlayerByController();
        if(tmp == null) return;

       String[][] dashPlayer;
        try {
            dashPlayer = server.lookAtDashBoard(gameId,tmp);
        } catch (Exception e) {
            clientController.reportErrorByController("player not valid");
            return;
        }
        clientController.informByController("Space Ship of :" + tmp);
        clientController.printPlayerDashboardByController(dashPlayer);
    }

    /**
     * Allows the player to retrieve a reserved tile from their ship.
     *
     * @return the reserved tile as string
     * @throws RemoteException if RMI communication fails
     * @throws BusinessLogicException if no reserved tile is available or coordinates are invalid
     */
    @Override
    public String takeReservedTile() throws RemoteException  , BusinessLogicException{
        if(clientController.returOKAY(0,5) && clientController.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        clientController.printMyDashBoardByController();
        clientController.informByController("Select a tile");
        int[] index;
        String tmpTile = null;
        while(true) {
            index = clientController.askCoordinateByController();
            if (index == null) {return null;}
            if(index[0]!=0 || clientController.returOKAY(0 , index[1])) clientController.informByController("Invalid coordinate");
            else break;
        }
            try {
                int tmp = clientController.returnIdOfTile(index[0], index[1]);
                clientController.setTileInMatrix("COD1234", index[0], index[1]);
                clientController.resetValidityByController(index[0], index[1]);
                clientController.printMyDashBoardByController();
                tmpTile = server.getReservedTile(gameId,nickname,tmp);
            } catch (BusinessLogicException e) {
                clientController.reportErrorByController("you miss " + e.getMessage() + "select new command" );
                throw new BusinessLogicException(e.getMessage());
            }
        return tmpTile;
    }

    /**
     * Signals the server that the player is leaving the game.
     *
     * @throws RemoteException if communication fails
     * @throws BusinessLogicException if the server rejects the leave request
     */
    @Override
    public void leaveGame() throws RemoteException, BusinessLogicException {
        if (gameId != 0) {
            try {
                server.LeaveGame(gameId, nickname);
            } catch (BusinessLogicException e) {
            }
            gameId = 0;
        }
    }

    /**
     * Logs the player out of the game and exits the application.
     *
     * @throws RemoteException if communication fails
     */
    @Override
    public void logOut() throws RemoteException {
        try {
            server.logOut(nickname);
        } catch (BusinessLogicException e) {
            clientController.reportErrorByController("Server error: " + e.getMessage());
        }
        System.out.println("Goodbye!");
        System.exit(0);
    }

    /**
     * Updates the client view with the latest map position of all players.
     *
     * @param Position the map of player names to positions
     * @throws RemoteException if communication fails
     */
    @Override
    public void updateMapPosition(Map<String, int [] > Position) throws RemoteException {
        clientController.updateMapPositionByController(Position);
    }



}
