package it.polimi.ingsw.galaxytrucker.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;


import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;


//trasporto il client da un'altra parte per una questione di scalabilità e correttezza :
//non dovendo lavorare con static diventa più testabile e se plice

public class ClientController {
    private final View view;
    private final VirtualView virtualClient;
    private ClientTile tmpTile;
    private List<ClientTile> tmpList;
    private boolean isConnected = false;
    private ClientTile[][] Dash_Matrix;
    private ClientGamePhase currentGamePhase;
    private String nickname;


    //Cose per il refactor dei json
    private final ClientTileFactory clientTileFactory;
    private final ClientCardFactory clientCardFactory;
    private final ClientEnumFactory clientEnumFactory;

    public String json = "boh";


    private static ClientController instance;


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
                   String cmd = g.sendAvailableChoices();  // blocca fino a quando viene risolta


                    String cmd2 = g.consumeMenuChoice();

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


    private void printMainMenu() {
        view.inform("-----MENU-----");
        view.inform("1. Create new game");
        view.inform("2. Enter in a game");
        view.inform("3. Logout");
        view.inform("Insert index:");
    }


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

    public int printAvailableGames(Map<Integer, int[]> availableGames) {
        try {
            return view.askGameToJoin(availableGames);
        } catch (Exception e) {
            System.out.println("Error while trying to read available games.");
        }
        return 0;
    }


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

    private boolean waitForGameStart() throws Exception {
        view.inform("Waiting for other players...");
        virtualClient.askInformationAboutStart();
        return true;
    }

    public int sendGameRequestFromController(String msg, int numberOfPlayer, boolean isDemo) throws Exception {
        return virtualClient.sendGameRequest(msg, numberOfPlayer, isDemo);
    }


    /**
     * private boolean waitForFlightStart() throws Exception {
     * <p>
     * if (currentGamePhase == ClientGamePhase.WAITING_FOR_PLAYERS) {
     * return true;
     * }
     * <p>
     * view.inform("Waiting for other players to finish their ship…\n");
     * view.inform("Possible actions while waiting for flight:");
     * view.inform(" 1: Watch a player's ship");
     * view.inform(" 2: Logout");
     * view.inform("Insert index:");
     * <p>
     * BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
     * while (currentGamePhase == ClientGamePhase.WAITING_FOR_PLAYERS) {
     * if (console.ready()) {
     * String line = console.readLine().trim();
     * switch (line) {
     * case "1" -> virtualClient.lookDashBoard();
     * case "2" -> {
     * virtualClient.leaveGame();
     * view.inform("Returned to main menu");
     * return false;
     * }
     * default -> view.reportError("Invalid choice. Please enter 1 or 2.");
     * }
     * } else {
     * Thread.sleep(200);
     * }
     * }
     * System.out.println("");
     * view.inform("Flight is starting! Good luck!\n");
     * return true;
     * }
     */


    private boolean handleWaitForGameStart() throws Exception {
        view.inform("Waiting for other players…");
        view.inform("Type 'exit' to abandon the lobby and return to main menu.");

        while (true) {
            if (currentGamePhase != ClientGamePhase.WAITING_FOR_PLAYERS) {
                return true;
            }

            String line = view.askString().trim();
            if ("exit".equalsIgnoreCase(line)) {
                virtualClient.leaveGame();
                view.inform("Returned to main menu");
                return false;
            }
        }
    }

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
                            String piedino = "PIEDONIPRADELLA";
                            try {
                                piedino = virtualClient.getUncoveredTile();
                            } catch (Exception e) {
                                v.reportError(e.getMessage());
                            }
                            if (!piedino.equals("PIEDONIPRADELLA")) {
                                try {
                                    tmpTile = clientTileFactory.fromJson(piedino);
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
                            String piedino = "PIEDONIPRADELLA";
                            try {
                                piedino = virtualClient.getUncoveredTile();
                            } catch (Exception e) {
                                g.reportError(e.getMessage());
                            }
                            if (!piedino.equals("PIEDONIPRADELLA")) {
                                try {
                                    tmpTile = clientTileFactory.fromJson(piedino);
                                    g.printTile(tmpTile);
                                } catch (IOException e) {
                                    g.reportError(e.getMessage());
                                }
                            }
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
                                g.ErPuzzo();
                                g.updateState(ClientGamePhase.MAIN_MENU);
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

    private void rotateRight() throws Exception {
        if (tmpTile != null) {
            tmpTile.rotateRight();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
        } else {
            view.reportError("No tile selected to rotate");
        }
    }

    private void rotateLeft() throws Exception {
        if (tmpTile != null) {
            tmpTile.rotateLeft();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
        } else {
            view.reportError("No tile selected to rotate");
        }
    }


    /// /metodi che mi servono per la gui///

    public static ClientController getInstance() {
        return instance;
    }

    public VirtualView getViewInterface() {
        return virtualClient;
    }

    public void logOutGUI() throws Exception {
        virtualClient.logOut();
    }

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

    public void setTileInMatrix(String jsonTile, int a, int b) {
        if (jsonTile.equals("PIEDINIPRADELLA")) {
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


    public void showUpdateByController(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        view.updateView(nickname, firePower, powerEngine, credits, purpleAline, brownAlien, numberOfHuman, numberOfEnergy);
    }

    public void informByController(String message) {
        view.inform(message);
    }

    public void reportErrorByController(String message) {
        view.reportError(message);
    }

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

    public int clientTileFromList(int index) {
        return tmpList.get(index).id;
    }

    public void printListOfTileCoveredByController() {
        view.printPileCovered();
    }

    public void printListOfGoodsByController(List<String> listOfGoods) {
        view.printListOfGoods(listOfGoods);
    }

    public void printCardByController(String jsonCard) {
        try {
            view.printCard(clientCardFactory.fromJson(jsonCard));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printTileByController(String jsonTile) {
        try {
            view.printTile(clientTileFactory.fromJson(jsonTile));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printPlayerDashboardByController(String[][] jsonDashboard) {
        try {
            ClientTile[][] newMatrix = clientTileFactory.fromJsonMatrix(jsonDashboard);
            this.Dash_Matrix = newMatrix;
            view.printDashShip(newMatrix);
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printMyDashBoardByController() {
        view.printDashShip(Dash_Matrix);
    }

    public void printDeckByController(String jsonDeck) {
        try {
            view.printDeck(clientCardFactory.fromJsonList(jsonDeck));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public Boolean askByController(String message) {
        return view.ask(message);
    }

    public boolean askWithTimeoutByController(String message) {
        return view.askWithTimeout(message);
    }

    public Integer askIndexByController() throws IOException, InterruptedException {
        return view.askIndex();
    }

    public Integer askIndexWithTimeoutByController() {
        return view.askIndexWithTimeout();
    }

    public int[] askCoordinateByController() {
        try {
            return view.askCoordinate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public int[] askCoordinatesWithTimeoutByController() {
        return view.askCoordinatesWithTimeout();
    }

    public String askStringByController() {
        return view.askString();
    }

    public void updateGameStateByController(String phase) {

        ClientGamePhase gamePhase = clientEnumFactory.describeGamePhase(phase);
        this.currentGamePhase = gamePhase;
        view.updateState(gamePhase);
    }

    public String choosePlayerByController() throws IOException, InterruptedException {
        return view.choosePlayer();
    }

    public void printListOfCommands() {
        view.printListOfCommand();
    }

    public boolean returOKAY(int a, int b) {
        return view.returnValidity(a, b);
    }

    public String getSomeTile(int a, int b) {
        try {
            return clientTileFactory.toJson(Dash_Matrix[a][b]);
        } catch (JsonProcessingException e) {
            view.reportError(e.getMessage());
        }
        return null;
    }

    public void updateMapPositionByController(Map<String, int[]> Position) {
        view.updateMap(Position);
    }

    public void setIsDemoByController(Boolean isDemo) {
        view.setIsDemo(isDemo);
    }

    public void newShip(String[][] jsonData) {
        try {
            Dash_Matrix = clientTileFactory.fromJsonMatrix(jsonData);
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void resetValidityByController(int a, int b) {
        if (a == 0 && (b == 5 || b == 6)) {
            view.resetValidity(a, b);
        }
    }

    public void printMapPositionByController() {
        view.printMapPosition();
    }

    public ClientGamePhase getGamePhaseByController() {
        return view.getGamePhase();
    }

    public int returnIdOfTile(int a, int b) {
        return Dash_Matrix[a][b].id;
    }


    private void handleGUIInteraction(GUIView guiView, ClientGamePhase lastPhase) {
        while (true) {
            ClientGamePhase temp = currentGamePhase;

            if (temp == ClientGamePhase.CARD_EFFECT) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    view.reportError("Error in sleep");
                }
                continue;
            }

            try {
                // Aspetta asincronamente il comando dalla GUI
//                String key = guiView.sendAvailableChoices2().get();
                processCommand(""); // Estrai questa logica in un metodo separato
            } catch (Exception e) {
                view.reportError(e.getMessage());
            }
        }
    }

    private void processCommand(String key) throws Exception {
        switch (key) {
            case "getacoveredtile" -> {
                tmpTile = clientTileFactory.fromJson(virtualClient.getTileServer());
                view.printTile(tmpTile);
            }
            case "getashowntile" -> {
                String piedino = virtualClient.getUncoveredTile();
                if (!piedino.equals("PIEDONIPRADELLA")) {
                    tmpTile = clientTileFactory.fromJson(piedino);
                    view.printTile(tmpTile);
                }
            }
            // ... altri casi come nello switch originale
        }
    }

    public void setTileFromGui(ClientTile tile) {
        tmpTile = tile;
        try {
            virtualClient.positionTile(clientTileFactory.toJson(tmpTile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}




