package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
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
    private Tile tmpTile;
    private boolean isConnected = false;
    private Tile[][] Dash_Matrix;
    private GamePhase currentGamePhase;


    private static ClientController instance;


    public ClientController(View view, VirtualView virtualClient) {
        this.view = view;
        this.virtualClient = virtualClient;
        try {
            virtualClient.setClientController(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
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
            mainMenuLoop();
            String cmd = view.askString();
            switch (cmd) {
                case "1" -> createNewGame();
                case "2" -> joinExistingGame();
                case "3" -> {
                    virtualClient.logOut();
                    isConnected = false;
                }
                default  -> view.reportError("Enter 1, 2 or 3.");
            }
        }
    }

    private int loginLoop() throws Exception {
        while (true) {
            view.inform("Insert your username:");
            switch (view){
                case GUIView v -> v.setSceneEnum(SceneEnum.NICKNAME_DIALOG);
                default -> {}
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

            if (res > 0) {
                return res;
            } else {
                view.inform("Login successful");
                switch (view) {
                    case GUIView g -> g.setSceneEnum(SceneEnum.MAIN_MENU);
                    default -> {}
                }
                return 0;
            }
        }
    }


    private void mainMenuLoop() throws Exception {
        while (isConnected) {
            int choice = 0;
            while (true) {
                printMainMenu();
                String line = "";
                switch (view) {
                    case TUIView v -> line = v.askString();
                    case GUIView g -> line = g.askString();
                    default -> line = view.askString();
                }

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
                break;
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
                int numberOfPlayer ;
                while (true) {
                    numberOfPlayer = v.askIndex() + 1;
                    if (numberOfPlayer >= 2 && numberOfPlayer <= 4) {
                        break;
                    }
                    v.reportError("Invalid number of players. Please enter a value between 2 and 4.\n");
                }

                int gameId = virtualClient.sendGameRequest("CREATE" , numberOfPlayer , demo);
                if (gameId > 0) {
                    virtualClient.setGameId(gameId);
                    boolean started = waitForGameStart();
                    if (!started) return;
                    startGame();
                } else {
                    view.reportError("Game creation failed");
                }
            }
            case GUIView v -> {
                List<Object> data = v.getDataForGame();
                boolean demo = (boolean) data.get(0);
                int numberOfPlayer = (int) data.get(1);
                int response = virtualClient.sendGameRequest("CREATE" , numberOfPlayer , demo);
                if (response > 0) {
                    virtualClient.setGameId(response);
                    handleWaitForGameStart();
                }else{
                    v.reportError("Game creation failed");
                }
            }
            default -> {}
        }
    }

    public int printAvailableGames(Map<Integer, int[]> availableGames) {
        int choice = 0;

        switch (view) {
            case TUIView v -> {
                v.inform("**Available Games:**");
                v.inform("0. Return to main menu");

                for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
                    int id = entry.getKey();
                    int[] info = entry.getValue();
                    boolean isDemo = info[2] == 1;
                    String suffix = isDemo ? " DEMO" : "";
                    v.inform(id + ". Players in game : " + info[0] + "/" + info[1] + suffix);
                }

                while (true) {
                    choice = v.askIndex() + 1;
                    if (choice == 0 || availableGames.containsKey(choice)) break;
                    v.reportError("Invalid choice, try again.");
                }
            }

            case GUIView v -> {
                Platform.runLater(() -> {
                    try {
                        v.setMainScene(SceneEnum.JOIN_GAME_MENU);
                        v.displayAvailableGames(availableGames);
                    } catch (IOException e) {
                        v.reportError("Failed to open join menu: " + e.getMessage());
                    }
                });

                choice = v.waitForGameChoice(); // blocca in attesa
            }


            default -> {
            }
        }

        return choice;
    }


    public void joinExistingGame() throws Exception {
        view.inform("Joining Existing Game...");
        int gameId = virtualClient.sendGameRequest("JOIN" , 0 , true);
        if (gameId > 0) {
            virtualClient.setGameId(gameId);
            boolean started = waitForGameStart();
            if (!started) return;
            startGame();
        }
    }

    private boolean waitForGameStart() throws Exception {
        view.inform("Waiting for other players…");
        virtualClient.askInformationAboutStart();
        return true;
    }

    /**
    private boolean waitForFlightStart() throws Exception {

        if (currentGamePhase != GamePhase.WAITING_FOR_PLAYERS) {
            return true;
        }

        view.inform("Waiting for other players to finish their ship…\n");
        view.inform("Possible actions while waiting for flight:");
        view.inform(" 1: Watch a player's ship");
        view.inform(" 2: Logout");
        view.inform("Insert index:");

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (currentGamePhase == GamePhase.WAITING_FOR_PLAYERS) {
            if (console.ready()) {
                String line = console.readLine().trim();
                switch (line) {
                    case "1" -> virtualClient.lookDashBoard();
                    case "2" -> {
                        virtualClient.leaveGame();
                        view.inform("Returned to main menu");
                        return false;
                    }
                    default -> view.reportError("Invalid choice. Please enter 1 or 2.");
                }
            } else {
                Thread.sleep(200);
            }
        }
        System.out.println("");
        view.inform("Flight is starting! Good luck!\n");
        return true;
    }
     */


    private boolean handleWaitForGameStart() throws Exception {
        view.inform("Waiting for other players…");
        view.inform("Type 'exit' to abandon the lobby and return to main menu.");

        while (true) {
            if (currentGamePhase != GamePhase.WAITING_FOR_PLAYERS) {
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

    private void startGame() throws Exception {

        if(view.getGamePhase() == GamePhase.TILE_MANAGEMENT) view.printTile(tmpTile);

        view.printListOfCommand();

        while (true) {
            GamePhase temp = currentGamePhase;

            if(currentGamePhase == GamePhase.CARD_EFFECT) {
                Thread.sleep(100);
                continue;
            }

            String key = view.sendAvailableChoices();

            if(key == null){
                if(temp != currentGamePhase) view.printListOfCommand();
                continue;
            }

            try {
                switch (key) {
                    case "getacoveredtile"    -> {
                        tmpTile = virtualClient.getTileServer();
                        view.printTile(tmpTile);
                    }
                    case "getashowntile"      -> {
                        tmpTile = virtualClient.getUncoveredTile();
                        if(tmpTile != null) {
                            view.printTile(tmpTile);
                        }
                    }
                    case "returnthetile"      -> virtualClient.getBackTile(tmpTile);
                    case "placethetile"       -> virtualClient.positionTile(tmpTile);
                    case "drawacard"          -> virtualClient.drawCard();
                    case "spinthehourglass"   -> virtualClient.rotateGlass();
                    case "declareready"       -> {
                        virtualClient.setReady();
                        /**
                        if (!waitForFlightStart()) return;
                        if (currentGamePhase == GamePhase.DRAW_PHASE) {
                            view.printListOfCommand();
                            continue;
                        }
                         */
                    }
                    case "watchadeck"         -> virtualClient.lookDeck();
                    case "watchaplayersship"  -> virtualClient.lookDashBoard();
                    case "rightrotatethetile" -> rotateRight();
                    case "leftrotatethetile"  -> rotateLeft();
                    case "takereservedtile"   -> {
                        tmpTile = virtualClient.takeReservedTile();
                        if(tmpTile != null) {
                            view.printTile(tmpTile);
                        }
                    }
                    case "logout"             -> {
                        virtualClient.leaveGame();
                        view.inform("Returned to main menu");
                        return;
                    }
                    default -> view.reportError("Action not recognized");
                }
            } catch (BusinessLogicException | IOException | InterruptedException e) {
                view.reportError(e.getMessage());
            }

            //if (currentGamePhase != GamePhase.DRAW_PHASE)
            view.printListOfCommand();
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


    ////metodi che mi servono per la gui///

    public static ClientController getInstance() {
        return instance;
    }

    public VirtualView getViewInterface() {
        return virtualClient;
    }

    public void logOutGUI() throws Exception {
        virtualClient.logOut();
    }

    public void setCurrentTile(Tile tile) {
        if (currentGamePhase == GamePhase.TILE_MANAGEMENT) {
            this.tmpTile = tile;
        } else {
            this.setTileInMatrix(tile, 2, 3);
        }
    }

    public void setTileInMatrix(Tile tile , int a , int b) {
        Dash_Matrix[a][b] = tile;
    }


    //TODO SPOSTO TUTTI I METODI CHE CHIAMAVANO DIRETTAMENTE LA VIEW QUA

    public void showUpdateByController(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        view.updateView(nickname,firePower,powerEngine,credits,purpleAline,brownAlien,numberOfHuman,numberOfEnergy);
    }

    public void informByController(String message) {
        view.inform(message);
    }

    public void reportErrorByController(String message) {
        view.reportError(message);
    }

    public void printListOfTileShownByController(List<Tile> tiles) {
        //TODO aggiungere i metodi per refactor
        view.printPileShown(tiles);
    }

    public void printListOfTileCoveredByController() {
        view.printPileCovered();
    }

    public void printListOfGoodsByController(List<Colour> listOfGoods) {
        //TODO aggiungere i metodi per refactor
        view.printListOfGoods(listOfGoods);
    }

    public void printCardByController(Card card) {
        //TODO aggiungere i metodi per refactor
        view.printCard(card);
    }

    public void printTileByController(Tile tile) {
        //TODO aggiungere i metodi per refactor
        view.printTile(tile);
    }

    public void printPlayerDashboardByController(Tile[][] dashboard){
        //TODO aggiungere i metodi per refactor
        view.printDashShip(dashboard);
    }

    public void printMyDashBoardByController(){
        view.printDashShip(Dash_Matrix);
    }

    public void printDeckByController(List<Card> deck) {
        //TODO aggiungere i metodi per refactor
        view.printDeck(deck);
    }

    public Boolean askByController(String message){
        return view.ask(message);
    }

    public boolean askWithTimeoutByController(String message){return view.askWithTimeout(message);}

    public Integer askIndexByController() throws IOException, InterruptedException {
        return view.askIndex();
    }
    public int[] askCoordinateByController(){
        try {
            return view.askCoordinate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public String askStringByController(){
        return view.askString();
    }

    public void updateGameStateByController(GamePhase phase){
        view.updateState(phase);
        currentGamePhase = phase;
    }

    public String choosePlayerByController() throws IOException, InterruptedException {
        return view.choosePlayer();
    }

    public void printListOfCommands(){
        view.printListOfCommand();
    }

    public boolean returOKAY(int a , int b){
        return view.ReturnValidity(a,b);
    }

    public Tile getSomeTile(int a, int b){
        return Dash_Matrix[a][b];
    }

    public void updateMapPositionByController(Map<String, Integer> Position){
        view.updateMap(Position);
    }

    public void setIsDemoByController(Boolean isDemo){
        view.setIsDemo(isDemo);
    }

    public void newShip(Tile[][] data){
        Dash_Matrix = data;
    }

    public void printMapPositionByController() { view.printMapPosition();}

    public GamePhase getGamePhaseByController() { return view.getGamePhase();}
 }


