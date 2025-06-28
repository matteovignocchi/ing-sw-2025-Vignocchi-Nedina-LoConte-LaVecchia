package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.application.Platform;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class manages the client-side game logic and communication with the server.
 * It handles messages received from the server, manages the local player state,
 * and delegates user interaction to the active View (GUI or TUI).
 * The controller is responsible for updating the dashboard, handling tile placement and selection,
 * managing goods, resolving card effects, and ensuring the client state remains consistent
 * with the server state throughout the game phases.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 * @author Francesco Lo Conte
 * @author Gabriele la Vecchia
 */
public class ClientController {
    private final View view;
    private final VirtualView virtualClient;
    private ClientTile tmpTile;
    private List<ClientTile> tmpList;
    private boolean isConnected = false;
    private ClientTile[][] Dash_Matrix;
    private ClientGamePhase currentGamePhase;
    private String nickname;
    private final ClientTileFactory clientTileFactory;
    private final ClientCardFactory clientCardFactory;
    private final ClientEnumFactory clientEnumFactory;
    public String json = "boh";

    /**
     * Initializes the ClientController with a specific View instance.
     * @param view the view implementation (GUIView or CLIView) used for interacting with the player
     */
    public ClientController(View view, VirtualView virtualClient) {
        this.view = view;
        this.virtualClient = virtualClient;
        tmpTile = new ClientTile();
        this.clientTileFactory = new ClientTileFactory();
        this.clientCardFactory = new ClientCardFactory();
        this.clientEnumFactory = new ClientEnumFactory();
        try {
            virtualClient.setClientController(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Dash_Matrix = new ClientTile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                ClientTile tile = new ClientTile();
                tile.type = "EMPTYSPACE";
                Dash_Matrix[i][j] = tile;
            }
        }



    }

    /**
     * Entry point for starting the client controller logic.
     * Initializes the view, performs user login, and enters the game loop.
     * Depending on the view type (GUI or TUI), it handles menu interaction differently:
     * - In GUI mode, waits for menu selection via asynchronous input resolution.
     * - In TUI mode, delegates to a standard main menu loop.
     * If the login process returns a valid game ID, it sets the game context and starts the game.
     * The loop continues until the user chooses to disconnect or exit.
     * @throws Exception if there are issues during view initialization or thread interruptions
     */
    public void start() throws Exception {
        view.start();
        isConnected = true;
        view.inform("Connected with success");



        int gameId = loginLoop();

        if (gameId > 0) {
            virtualClient.setGameId(gameId);
            startGame();
        }

        while (isConnected) {
            switch (view) {
                case GUIView g -> {

                    // Lascio la GUI controllare cosa inviare con resolveMenuChoice o simili
                    while (!g.hasResolvedMenuChoice()) {
                        Thread.sleep(100);
                    }
                    String cmd = g.consumeMenuChoice();

                    switch (cmd) {
                        case "1" -> createNewGame();
                        case "2" -> joinExistingGame();
                        case "3" -> {
                            virtualClient.logOut();
                            isConnected = false;
                            return;
                        }
                        default -> view.reportError("Enter 1, 2 or 3.");
                    }
                }

                default -> {
                    mainMenuLoop(); // fallback per TUI
                }
            }
        }

    }

    /**
     * Handles the login phase of the client, asking the user to enter a valid username.
     * Continuously prompts the user (via view) until a valid username is entered and accepted by the server.
     * If the login is successful, it sets the nickname locally and updates the view accordingly.
     * - In GUI mode, sets the scene to the nickname dialog and later to the main menu.
     * - In TUI mode, uses console input and standard feedback.
     * @return the game ID assigned by the server if joining an existing game, or 0 if creating a new game
     * @throws Exception if there are interruptions during user interaction or remote call failures
     */
    private int loginLoop() throws Exception {
        while (true) {
            view.inform("Insert your username:");
            switch (view) {

                case GUIView v -> Platform.runLater(() -> {
                    v.setSceneEnum(SceneEnum.NICKNAME_DIALOG);
                });
                default -> {
                }
            }
            String username = virtualClient.askString();

            if (username == null || username.trim().isEmpty()) {
                view.reportError(" Username cannot be empty. Please enter a valid username.");
                continue;
            }

            int res;
            try {
                res = virtualClient.sendLogin(username);
            } catch (BusinessLogicException ex) {
                view.reportError(ex.getMessage());
                continue;
            }

            if (res == -1) {
                view.reportError("Credential not valid, try again.");
                continue;
            }
            virtualClient.setNickname(username);
            view.setNickName(username);
            nickname = username;

            if (res > 0) {
                return res;
            } else {
                view.inform("Login successful");
                switch (view) {
                    case GUIView g -> Platform.runLater(() -> {
                        g.setSceneEnum(SceneEnum.MAIN_MENU);
                    });
                    default -> {
                    }
                }
                return 0;
            }
        }
    }

    /**
     * Handles the main menu loop for the client in TUI or fallback mode.
     * Displays the menu and waits for the user's input to:
     * The method validates the input and invokes the appropriate action.
     * If the user chooses to exit, it logs out from the server and terminates the loop.
     * @throws Exception if user input or remote communication fails
     */
    private void mainMenuLoop() throws Exception {
        while (isConnected) {
            printMainMenu();

            String line;
            switch (view) {
                case TUIView v -> line = v.askString();
                case GUIView g -> line = g.askString();
                default -> line = view.askString();
            }

            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                view.reportError("Invalid input. Please enter a number between 1 and 3.\n");
                continue;
            }

            if (choice < 1 || choice > 3) {
                view.reportError("Invalid choice. Please enter a number between 1 and 3.\n");
                continue;
            }

            switch (choice) {
                case 1 -> createNewGame();
                case 2 -> joinExistingGame();
                case 3 -> {
                    virtualClient.logOut();
                    isConnected = false;
                    return;
                }
            }
        }
    }

    /**
     * Displays the main menu options to the user through the current view.
     * The menu includes:
     *  1. Create new game
     *  2. Enter in a game
     *  3. Logout
     * Prompts the user to insert a choice index.
     */
    private void printMainMenu() {
        view.inform("-----MENU-----");
        view.inform("1. Create new game");
        view.inform("2. Enter in a game");
        view.inform("3. Logout");
        view.inform("Insert index:");
    }

    /**
     * Handles the creation of a new game session, supporting both TUI and GUI modes.
     * In TUI:
     * - Asks the user whether to create a demo game.
     * - Prompts for a number of players between 2 and 4.
     * - Sends a game creation request to the server.
     * In GUI:
     * - Switches to the create game menu.
     * - Collects user input via a form (demo flag and number of players).
     * - Sends a game creation request.
     * If the creation is successful, waits for the game to start and then initiates the client-side logic.
     * @throws Exception if communication with the server fails or view interaction encounters an error
     */
    public void createNewGame() throws Exception {
        switch (view) {
            case TUIView v -> {
                view.inform("Creating New Game...");
                boolean demo = askByController("Would you like a demo version?");
                v.inform("Select a number of players between 2 and 4");
                int numberOfPlayer;
                while (true) {
                    numberOfPlayer = v.askIndex() + 1;
                    if (numberOfPlayer >= 2 && numberOfPlayer <= 4) {
                        break;
                    }
                    v.reportError("Invalid number of players. Please enter a value between 2 and 4.\n");
                }

                int gameId = virtualClient.sendGameRequest("CREATE", numberOfPlayer, demo);
                if (gameId > 0) {
                    virtualClient.setGameId(gameId);
                    boolean started = waitForGameStart();
                    if (!started) return;
                    startGame();
                } else {
                    view.reportError("Game creation failed");
                }
            }
            case GUIView g -> {
                Platform.runLater(() -> {
                    g.setSceneEnum(SceneEnum.CREATE_GAME_MENU);
                });

                List<Object> data = g.askCreateGameData();
                if (data.isEmpty()) {
                    Platform.runLater(() -> g.setSceneEnum(SceneEnum.MAIN_MENU));
                    return; // esce e torna al ciclo principale
                }
                boolean demo = (boolean) data.get(0);
                int numberOfPlayer = (int) data.get(1);
                int response = virtualClient.sendGameRequest("CREATE", numberOfPlayer, demo);
                if (response > 0) {
                    virtualClient.setGameId(response);
                    boolean started = waitForGameStart();
                    if (!started) return;
                    startGame();
                } else {
                    g.reportError("Game creation failed");
                }
        }
            default -> {
            }
        }
    }

    /**
     * Displays the list of available games to the user and asks which one to join.
     * Delegates the rendering and selection to the view.
     * @param availableGames a map where the key is the game ID and the value is an array containing player count info
     * @return the ID of the selected game, or 0 if the selection fails
     */
    public int printAvailableGames(Map<Integer, int[]> availableGames) {
        try {
            return view.askGameToJoin(availableGames);
        } catch (Exception e) {
            System.out.println("Error while trying to read available games.");
        }
        return 0;
    }

    /**
     * Handles the process of joining an existing game, supporting both GUI and TUI.
     * In GUI mode, it switches the scene to the join game menu.
     * In TUI mode, it informs the user through text output.
     * Sends a join request to the server. If the game is valid and accepted,
     * the client waits for the game to start and initiates the client-side logic.
     * @throws Exception if communication with the server or view interaction fails
     */
    public void joinExistingGame() throws Exception {
        switch (view){
            case GUIView g -> {
                Platform.runLater(() -> {
                    g.setSceneEnum(SceneEnum.JOIN_GAME_MENU);
                });
            }
            case TUIView v -> {
                v.inform("Joining Existing Game...");
            }
            default -> {}
        }

        int gameId = sendGameRequestFromController("JOIN", 0, true);
        if (gameId > 0) {
            virtualClient.setGameId(gameId);
            boolean started = waitForGameStart();
            if (!started) return;
            startGame();
        }
    }

    /**
     * Waits for the server to confirm that the game can start.
     * Informs the user that the client is waiting for other players,
     * then invokes a blocking call to retrieve the server's confirmation.
     * @return true when the server signals the game can start
     * @throws Exception if communication with the server fails
     */
    private boolean waitForGameStart() throws Exception {
        view.inform("Waiting for other players...");
        virtualClient.askInformationAboutStart();
        return true;
    }

    /**
     * Sends a game-related request (create or join) to the server through the virtual client.
     * This method acts as a proxy from the controller to the virtual client's `sendGameRequest`.
     * @param msg the type of request ("CREATE" or "JOIN")
     * @param numberOfPlayer the number of players (used when creating a game)
     * @param isDemo true if the game is a demo version, false otherwise
     * @return the game ID assigned by the server, or -1 if the request fails
     * @throws Exception if communication with the server fails
     */
    public int sendGameRequestFromController(String msg, int numberOfPlayer, boolean isDemo) throws Exception {
        return virtualClient.sendGameRequest(msg, numberOfPlayer, isDemo);
    }


    /**
     * Main game loop entry point for the client after the game has been initialized.
     * Handles interaction differently for TUI and GUI:
     * - For TUI: enters a loop waiting for user commands, rendering the state, and sending requests to the server.
     * - For GUI: listens for command selections from the interface and reacts accordingly.
     * The method manages both tile actions (draw, rotate, place, return, reserve) and game commands
     * (draw card, spin hourglass, declare ready, inspect decks or dashboards, logout).
     * It maintains responsiveness to phase changes and updates the UI accordingly.
     */
    private void startGame() {
        if (view.getGamePhase() == ClientGamePhase.TILE_MANAGEMENT) view.printTile(tmpTile);
        view.printListOfCommand();
        ClientGamePhase lastPhase = currentGamePhase;
        switch (view) {
            case TUIView v -> {
                while (true) {
                    ClientGamePhase temp = currentGamePhase;
                    if (lastPhase == ClientGamePhase.CARD_EFFECT
                            && temp != ClientGamePhase.CARD_EFFECT) {
                        v.printListOfCommand();
                    }
                    lastPhase = temp;

                    if (temp == ClientGamePhase.CARD_EFFECT) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            v.reportError("Error in sleep");
                        }
                        continue;
                    }

                    String key = null;
                    try {
                        key = v.sendAvailableChoices();
                    } catch (Exception e) {
                        v.reportError(e.getMessage());
                    }

                    if (key == null) {
                        if (temp != currentGamePhase) v.printListOfCommand();
                        continue;
                    }
                    switch (key) {
                        case "getacoveredtile" -> {

                            try {
                                tmpTile = clientTileFactory.fromJson(virtualClient.getTileServer());
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                            v.printTile(tmpTile);
                        }
                        case "getashowntile" -> {
                            String tile = "CODE404";
                            try {
                                tile = virtualClient.getUncoveredTile();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                            if (!tile.equals("CODE404")) {
                                try {
                                    tmpTile = clientTileFactory.fromJson(tile);
                                    v.printTile(tmpTile);
                                } catch (IOException e) {
                                    v.reportError(e.getMessage());
                                }
                            }
                        }
                        case "returnthetile" -> {
                            try {
                                virtualClient.getBackTile(clientTileFactory.toJson(tmpTile));
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "placethetile" -> {
                            try {
                                virtualClient.positionTile(clientTileFactory.toJson(tmpTile));
                            } catch (BusinessLogicException e) {
                                v.reportError("Invalid position. Try again");
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "drawacard" -> {
                            try {
                                virtualClient.drawCard();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "spinthehourglass" -> {
                            try {
                                virtualClient.rotateGlass();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "declareready" -> {
                            try {
                                virtualClient.setReady();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                            /**
                             if (!waitForFlightStart()) return;
                             if (currentGamePhase == GamePhase.DRAW_PHASE) {
                             view.printListOfCommand();
                             continue;
                             }
                             */
                        }
                        case "watchadeck" -> {
                            try {
                                virtualClient.lookDeck();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "watchaplayersship" -> {
                            try {
                                virtualClient.lookDashBoard();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "rightrotatethetile" -> {
                            try {
                                rotateRight();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "leftrotatethetile" -> {
                            try {
                                rotateLeft();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "takereservedtile" -> {
                            try {
                                String json = virtualClient.takeReservedTile();
                                if (json != null) {
                                    tmpTile = clientTileFactory.fromJson(json);
                                    v.printTile(tmpTile);
                                }
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                        }
                        case "logout" -> {
                            try {
                                virtualClient.leaveGame();
                                resetModel();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                            v.inform("Returned to main menu");
                            return;
                        }
                        default -> v.reportError("Action not recognized");
                    }
                    if (currentGamePhase != ClientGamePhase.CARD_EFFECT) {
                        v.printListOfCommand();
                    }
                }
            }
            case GUIView g -> {
                printMyDashBoardByController();
                while (true) {
                    String key = g.sendAvailableChoices();
                    System.out.println("[DEBUG] Comando ricevuto da GUI: " + key);

                    if (key == null) continue;

                    switch (key) {
                        case "getacoveredtile" -> {

                            try {
                                tmpTile = clientTileFactory.fromJson(virtualClient.getTileServer());
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                            g.printTile(tmpTile);
                        }
                        case "getashowntile" -> {
                            String tile = "CODE404";
                            try {
                                tile = virtualClient.getUncoveredTile();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                            if (!tile.equals("CODE404")) {
                                try {
                                    tmpTile = clientTileFactory.fromJson(tile);
                                    g.printTile(tmpTile);
                                } catch (IOException e) {
                                    g.reportError(e.getMessage());
                                }
                            }
                            g.showNotification("You got the tile in time!");
                        }
                        case "returnthetile" -> {
                            try {
                                virtualClient.getBackTile(clientTileFactory.toJson(tmpTile));
                                tmpTile = null;
                                view.printTile(null);
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "placethetile" -> {
                            try {
                                virtualClient.positionTile(clientTileFactory.toJson(tmpTile));
                            } catch (BusinessLogicException e) {
                                g.reportError("Invalid position. Try again");
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "drawacard" -> {
                            try {
                                virtualClient.drawCard();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "spinthehourglass" -> {
                            try {
                                virtualClient.rotateGlass();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "declareready" -> {
                            try {
                                virtualClient.setReady();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "watchadeck" -> {
                            try {
                                virtualClient.lookDeck();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "watchaplayersship" -> {
                            try {
                                virtualClient.lookDashBoard();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "rightrotatethetile" -> {
                            try {
                                rotateRight();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "leftrotatethetile" -> {
                            try {
                                rotateLeft();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "takereservedtile" -> {
                            try {
                                String json = virtualClient.takeReservedTile();
                                if (json != null) {
                                    tmpTile = clientTileFactory.fromJson(json);
                                    g.printTile(tmpTile);
                                }
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                        }
                        case "logout" -> {
                            try {
                                virtualClient.leaveGame();
                                resetModel();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                            return;
                        }
                        default -> g.reportError("Action not recognized");
                    }

                    if (currentGamePhase != ClientGamePhase.CARD_EFFECT) {
                        g.printListOfCommand();
                    }
                }
            }
            default -> view.reportError("User Interface incorrect");
        }
    }

    /**
     * Rotates the currently selected tile 90 degrees to the right.
     * Updates the UI with the rotated tile, or reports an error if no tile is selected.
     */
    private void rotateRight() {
        if (tmpTile != null) {
            tmpTile.rotateRight();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
        } else {
            view.reportError("No tile selected to rotate");
        }
    }

    /**
     * Rotates the currently selected tile 90 degrees to the left.
     * Updates the UI with the rotated tile, or reports an error if no tile is selected.
     */
    private void rotateLeft() {
        if (tmpTile != null) {
            tmpTile.rotateLeft();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
        } else {
            view.reportError("No tile selected to rotate");
        }
    }

    /**
     * Sets the current tile in the client based on the received JSON.
     * If the game is in the TILE_MANAGEMENT phase, the tile is stored as the temporary tile (`tmpTile`)
     * for manipulation or placement. Otherwise, it is placed directly in the dashboard matrix at (2,3).
     * @param jsonTile the tile serialized in JSON format
     * @throws RuntimeException if the JSON cannot be parsed into a tile
     */
    public void setCurrentTile(String jsonTile) {
        if (currentGamePhase == ClientGamePhase.TILE_MANAGEMENT) {
            try {
                this.tmpTile = clientTileFactory.fromJson(jsonTile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.setTileInMatrix(jsonTile, 2, 3);
        }
    }

    /**
     * Places a tile in the dashboard matrix at the specified coordinates.
     * If the input string is "COD1234", an empty space tile is placed at (a, b).
     * Otherwise, attempts to parse the JSON string into a ClientTile and place it in the matrix,
     * after validating the position using the view.
     * @param jsonTile the tile in JSON format, or the special string "COD1234" for empty space
     * @param a the row coordinate in the dashboard matrix
     * @param b the column coordinate in the dashboard matrix
     */
    public void setTileInMatrix(String jsonTile, int a, int b) {
        if (jsonTile.equals("COD1234")) {
            ClientTile tmpEmpty = new ClientTile();
            tmpEmpty.type = "EMPTYSPACE";
            tmpEmpty.id = 0;
            Dash_Matrix[a][b] = tmpEmpty;
        } else {

            try {
                if (view.returnValidity(a, b)) {
                    view.setTile(clientTileFactory.fromJson(jsonTile), a, b);
                    Dash_Matrix[a][b] = clientTileFactory.fromJson(jsonTile);
                } else {
                    view.reportError("Invalid position");
                    return;
                }
            } catch (IOException e) {
                view.reportError(e.getMessage());
            }
            view.setValidity(a, b);
        }
    }

    /**
     * Updates the player's view with the latest ship statistics.
     * Delegates the update to the view with the player's current state, including firepower,
     * engine power, credits, alien presence, crew size, and energy.
     * @param nickname        the nickname of the player to update
     * @param firePower       the current firepower of the player's ship
     * @param powerEngine     the total engine power of the player's ship
     * @param credits         the number of credits the player has
     * @param purpleAline     true if a purple alien is on board
     * @param brownAlien      true if a brown alien is on board
     * @param numberOfHuman   total number of humans on the ship
     * @param numberOfEnergy  total number of energy units available
     */
    public void showUpdateByController(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        view.updateView(nickname, firePower, powerEngine, credits, purpleAline, brownAlien, numberOfHuman, numberOfEnergy);
    }

    /**
     * Sends a generic informational message to the user via the view.
     * @param message the message to be displayed
     */
    public void informByController(String message) {
        view.inform(message);
    }

    /**
     * Sends a generic informational message to the user via the view.
     * @param message the message to be displayed
     */
    public void reportErrorByController(String message) {
        view.reportError(message);
    }

    /**
     * Parses a JSON list of tiles and displays the currently revealed tiles to the user.
     * @param jsonTiles the JSON string representing the list of shown tiles
     * @return the number of tiles shown, or 0 if parsing fails
     */
    public int printListOfTileShownByController(String jsonTiles) {

        try {
            tmpList = clientTileFactory.fromJsonList(jsonTiles);
            view.printPileShown(tmpList);
            return tmpList.size();
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the tile ID of the tile at the specified index in the temporary list of shown tiles.
     * @param index the index in the shown tile list
     * @return the tile ID at the specified index
     */
    public int clientTileFromList(int index) {
        return tmpList.get(index).id;
    }

    /**
     * Informs the view to display that the tile pile is currently covered (hidden).
     */
    public void printListOfTileCoveredByController() {
        view.printPileCovered();
    }

    /**
     * Displays a list of goods to the user via the view.
     * @param listOfGoods the list of goods as strings to display
     */
    public void printListOfGoodsByController(List<String> listOfGoods) {
        view.printListOfGoods(listOfGoods);
    }

    /**
     * Parses a JSON string representing a card and displays it to the user.
     * @param jsonCard the JSON string of the card
     */
    public void printCardByController(String jsonCard) {
        try {
            view.printCard(clientCardFactory.fromJson(jsonCard));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    /**
     * Parses a JSON string representing a tile and displays it to the user.
     * @param jsonTile the JSON string of the tile
     */
    public void printTileByController(String jsonTile) {
        try {
            view.printTile(clientTileFactory.fromJson(jsonTile));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    /**
     * Parses a JSON matrix of tiles and displays the player's dashboard on the view.
     * @param jsonDashboard a 2D array of JSON strings representing the dashboard tiles
     */
    public void printPlayerDashboardByController(String[][] jsonDashboard) {
        try {
            ClientTile[][] newMatrix = clientTileFactory.fromJsonMatrix(jsonDashboard);
            view.printDashShip(newMatrix);
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    /**
     * Displays the client's current dashboard matrix on the view.
     */
    public void printMyDashBoardByController() {
        view.printDashShip(Dash_Matrix);
    }

    /**
     * Parses a JSON list of cards and displays the deck to the user.
     * @param jsonDeck the JSON string representing the list of cards
     */
    public void printDeckByController(String jsonDeck) {
        try {
            view.printDeck(clientCardFactory.fromJsonList(jsonDeck));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    /**
     * Asks the user a yes/no question via the view.
     * @param message the question to display
     * @return true if the user answers yes, false otherwise
     */
    public Boolean askByController(String message) {
        return view.ask(message);
    }

    /**
     * Asks the user a yes/no question with a time limit.
     * @param message the question to display
     * @return true if the user answers yes within the timeout, false otherwise
     */
    public boolean askWithTimeoutByController(String message) {
        return view.askWithTimeout(message);
    }

    /**
     * Prompts the user to select an index from a list.
     * @return the selected index
     * @throws IOException if input fails
     * @throws InterruptedException if the input is interrupted
     */
    public Integer askIndexByController() throws IOException, InterruptedException {
        return view.askIndex();
    }

    /**
     * Prompts the user to select an index with a time limit.
     * @return the selected index or null if timeout occurs
     */
    public Integer askIndexWithTimeoutByController() {
        return view.askIndexWithTimeout();
    }

    /**
     * Prompts the user to input coordinates (row and column).
     * @return an int array with [row, column]
     * @throws RuntimeException if input or thread is interrupted
     */
    public int[] askCoordinateByController() {
        try {
            return view.askCoordinate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Prompts the user to input coordinates with a timeout.
     * @return an int array with [row, column] or null if timeout occurs
     */
    public int[] askCoordinatesWithTimeoutByController() {
        return view.askCoordinatesWithTimeout();
    }

    /**
     * Prompts the user to input a string.
     * @return the input string
     */
    public String askStringByController() {
        return view.askString();
    }

    /**
     * Updates the current game phase in the client controller and view.
     * If the phase has not changed, the update is skipped. Otherwise, the new phase is
     * parsed, stored, and passed to the view for visual update.
     * @param phase the new game phase as a string
     */
    public void updateGameStateByController(String phase) {
        ClientGamePhase gamePhase = clientEnumFactory.describeGamePhase(phase);
        if (gamePhase == this.currentGamePhase) {
            System.out.println("[DEBUG] Fase invariata, salto updateState: " + gamePhase);
            return;
        }

        this.currentGamePhase = gamePhase;
        view.updateState(gamePhase);
    }

    /**
     * Prompts the user to select another player (e.g., for targeting or inspection).
     * @return the name of the chosen player
     * @throws IOException if input fails
     * @throws InterruptedException if input is interrupted
     */
    public String choosePlayerByController() throws IOException, InterruptedException {
        return view.choosePlayer();
    }

    /**
     * Checks whether the position (a, b) on the dashboard is valid for tile placement.
     * @param a the row index
     * @param b the column index
     * @return true if the position is valid, false otherwise
     */
    public boolean returOKAY(int a, int b) {
        return view.returnValidity(a, b);
    }

    /**
     * Updates the player's view with the latest map positions of all players.
     * @param Position a map from player names to their position arrays
     */
    public void updateMapPositionByController(Map<String, int[]> Position) {
        view.updateMap(Position);
    }

    /**
     * Sets whether the view should operate in demo mode or normal mode.
     * @param isDemo true for demo mode, false for normal mode
     */
    public void setIsDemoByController(Boolean isDemo) {
        view.setIsDemo(isDemo);
    }

    /**
     * Initializes the client's dashboard matrix from a JSON matrix of tiles.
     * @param jsonData a 2D array of JSON strings representing the ship tiles
     */
    public void newShip(String[][] jsonData) {
        try {
            Dash_Matrix = clientTileFactory.fromJsonMatrix(jsonData);
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    /**
     * Resets the validity status of the specified tile position if it is in the reserved tile area.
     * @param a the row index
     * @param b the column index (expected to be 5 or 6)
     */
    public void resetValidityByController(int a, int b) {
        if (a == 0 && (b == 5 || b == 6)) {
            view.resetValidity(a, b);
        }
    }

    /**
     * Returns the tile ID at the specified position on the dashboard matrix.
     * @param a the row index
     * @param b the column index
     * @return the tile ID
     */
    public int returnIdOfTile(int a, int b) {
        return Dash_Matrix[a][b].id;
    }

    /**
     * Resets the local dashboard matrix to an empty state,
     * filling all positions with empty space tiles.
     */
    private void resetModel() {
        Dash_Matrix = new ClientTile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                ClientTile tile = new ClientTile();
                tile.type = "EMPTYSPACE";
                Dash_Matrix[i][j] = tile;
            }
        }
    }

}




