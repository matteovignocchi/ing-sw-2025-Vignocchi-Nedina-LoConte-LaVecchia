package it.polimi.ingsw.galaxytrucker.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;


import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStreamReader;
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
                    startGame();
                }else{
                    v.reportError("Game creation failed");
                }
            }
            default -> {}
        }
    }

    public int printAvailableGames(Map<Integer, int[]> availableGames) {
        int choice = 0;

        view.inform("**Available Games:**");
        view.inform("0. Return to main menu");

        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
            int id = entry.getKey();
            int[] info = entry.getValue();
            boolean isDemo = info[2] == 1;
            switch(view){
                case TUIView v -> {
                    String suffix = isDemo ? " DEMO" : "";
                    v.inform(id + ". Players in game : " + info[0] + "/" + info[1] + suffix);
                }
                case GUIView v -> {
                    Platform.runLater(() -> {
                        v.displayAvailableGames(availableGames);
                    });
                }
                default -> {}

            }
            while (true) {
                try {
                    choice = view.askIndex();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (choice == 0 || availableGames.containsKey(choice)) {
                    break;
                }
                view.reportError("Invalid choice, try again.");
            }
        }
        return choice;
    }


//            case GUIView v -> {
//
//                Platform.runLater(() -> {
//                    v.displayAvailableGames(availableGames);

    public void joinExistingGame() throws Exception {
        view.inform("Joining Existing Game...");
        int gameId = sendGameRequestFromController("JOIN" , 0 , true);
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

    public int sendGameRequestFromController(String msg , int numberOfPlayer , boolean isDemo) throws Exception {
         return virtualClient.sendGameRequest(msg , numberOfPlayer , isDemo);
    }


    /**
    private boolean waitForFlightStart() throws Exception {

        if (currentGamePhase == ClientGamePhase.WAITING_FOR_PLAYERS) {
            return true;
        }

        view.inform("Waiting for other players to finish their ship…\n");
        view.inform("Possible actions while waiting for flight:");
        view.inform(" 1: Watch a player's ship");
        view.inform(" 2: Logout");
        view.inform("Insert index:");

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (currentGamePhase == ClientGamePhase.WAITING_FOR_PLAYERS) {
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

    private void startGame(){

        if(view.getGamePhase() == ClientGamePhase.TILE_MANAGEMENT) view.printTile(tmpTile);

        view.printListOfCommand();

        while (true) {
            ClientGamePhase temp = currentGamePhase;

            if(temp ==ClientGamePhase.CARD_EFFECT) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    view.reportError("Error in sleep");
                }
                continue;
            }

            String key = null;
            try {
                key = view.sendAvailableChoices();
            } catch (Exception e) {
                view.reportError(e.getMessage());
            }

            if(key == null){
                if(temp != currentGamePhase) view.printListOfCommand();
                continue;
            }
                switch (key) {
                    case "getacoveredtile" -> {

                        try {
                            tmpTile = clientTileFactory.fromJson(virtualClient.getTileServer());
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                        view.printTile(tmpTile);
                    }
                    case "getashowntile" -> {
                        String piedino = "PIEDONIPRADELLA";
                        try {
                            piedino = virtualClient.getUncoveredTile();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                        if(!piedino.equals("PIEDONIPRADELLA")){
                            try {
                                tmpTile = clientTileFactory.fromJson(piedino);
                                view.printTile(tmpTile);
                            } catch (IOException e) {
                                view.reportError(e.getMessage());
                            }
                        }
                    }
                    case "returnthetile" -> {
                        try {
                            virtualClient.getBackTile(clientTileFactory.toJson(tmpTile));
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "placethetile" -> {
                        try {
                            virtualClient.positionTile(clientTileFactory.toJson(tmpTile));
                        } catch (Exception e) {
                            view.reportError("Invalid position");
                        }
                    }
                    case "drawacard" -> {
                        try {
                            virtualClient.drawCard();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "spinthehourglass" -> {
                        try {
                            virtualClient.rotateGlass();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "declareready" -> {
                        try {
                            virtualClient.setReady();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
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
                            view.reportError(e.getMessage());
                        }
                    }
                    case "watchaplayersship" -> {
                        try {
                            virtualClient.lookDashBoard();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "rightrotatethetile" -> {
                        try {
                            rotateRight();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "leftrotatethetile" -> {
                        try {
                            rotateLeft();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                    }
                    case "takereservedtile" -> {
                        try {
                            tmpTile = clientTileFactory.fromJson(virtualClient.takeReservedTile());
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                        if (tmpTile != null) {
                            view.printTile(tmpTile);
                        }
                    }
                    case "logout" -> {
                        try {
                            virtualClient.leaveGame();
                        } catch (Exception e) {
                            view.reportError(e.getMessage());
                        }
                        view.inform("Returned to main menu");
                        return;
                    }
                    default -> view.reportError("Action not recognized");
                }
            //if (currentGamePhase != ClientGamePhase.DRAW_PHASE)
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

    public void setTileInMatrix(String jsonTile , int a , int b) {
        if (jsonTile.equals("PIEDINIPRADELLA")) {
            ClientTile tmpEmpty = new ClientTile();
            tmpEmpty.type = "EMPTYSPACE";
            Dash_Matrix[a][b] = tmpEmpty;
        } else {

            try {
                if(view.ReturnValidity(a,b)) Dash_Matrix[a][b] = clientTileFactory.fromJson(jsonTile);
                else {
                    view.reportError("Invalid position");
                    return;
                }
            } catch (IOException e) {
                view.reportError(e.getMessage());
            }
            view.setValidity(a, b);
        }
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

    public int printListOfTileShownByController(String jsonTiles){
        //TODO aggiungere i metodi per refactor

        try {
            tmpList = clientTileFactory.fromJsonList(jsonTiles);
            view.printPileShown(tmpList);
            return tmpList.size();
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
        return 0;
    }

    public int clientTileFromList(int index){
        return tmpList.get(index).id;
    }

    public void printListOfTileCoveredByController() {
        view.printPileCovered();
    }

    public void printListOfGoodsByController(List<String> listOfGoods) {
        //TODO aggiungere i metodi per refactor
        view.printListOfGoods(listOfGoods);
    }

    public void printCardByController(String jsonCard){
        //TODO aggiungere i metodi per refactor
        try {
            view.printCard(clientCardFactory.fromJson(jsonCard));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printTileByController(String jsonTile){
        //TODO aggiungere i metodi per refactor
        try {
            view.printTile(clientTileFactory.fromJson(jsonTile));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printPlayerDashboardByController(String[][] jsonDashboard){
        //TODO aggiungere i metodi per refactor
        try {
            view.printDashShip(clientTileFactory.fromJsonMatrix(jsonDashboard));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }

    public void printMyDashBoardByController(){
        view.printDashShip(Dash_Matrix);
    }

    public void printDeckByController( String jsonDeck) {
        //TODO aggiungere i metodi per refactor
        try {
            view.printDeck(clientCardFactory.fromJsonList(jsonDeck));
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
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

    public void updateGameStateByController(String phase){

        ClientGamePhase gamePhase = clientEnumFactory.describeGamePhase(phase);
        view.updateState(gamePhase);


        currentGamePhase = gamePhase;
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

    public String getSomeTile(int a, int b){
        try {
            return clientTileFactory.toJson(Dash_Matrix[a][b]);
        } catch (JsonProcessingException e) {
            view.reportError(e.getMessage());
        }
        return null;
    }

    public void updateMapPositionByController(Map<String, Integer> Position){
        view.updateMap(Position);
    }

    public void setIsDemoByController(Boolean isDemo){
        view.setIsDemo(isDemo);
    }

    public void newShip(String[][] jsonData){
        try {
            Dash_Matrix = clientTileFactory.fromJsonMatrix(jsonData);
        } catch (IOException e) {
            view.reportError(e.getMessage());
        }
    }
    public void resetValidityByController(int a , int b){
        if(a == 0 && (b == 5 || b ==6)){
            view.resetValidity(a,b);
        }
    }

    public void printMapPositionByController() { view.printMapPosition();}

    public ClientGamePhase getGamePhaseByController() { return view.getGamePhase();}

    public int returnIdOfTile(int a , int b){
        return Dash_Matrix[a][b].id;
    }



 }


