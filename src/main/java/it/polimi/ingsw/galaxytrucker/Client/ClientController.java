package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import static java.lang.String.valueOf;


//trasporto il client da un'altra parte per una questione di scalabilità e correttezza :
//non dovendo lavorare con static diventa più testabile e se plice

public class ClientController {
    private final View view;
    private final VirtualView virtualClient;
    private Tile tmpTile;
    private boolean isConnected = false;
    private int idCurrentGame;

    private static ClientController instance;


    public ClientController(View view, VirtualView virtualClient) {
        this.view = view;
        this.virtualClient = virtualClient;
    }

    public void start() throws Exception {
        view.start();
        isConnected = true;
        view.inform("Connected with success");

        int gameId = loginLoop();
        if(gameId<0) mainMenuLoop();
        else virtualClient.enterGame(gameId);
    }

    private int loginLoop() throws Exception {
        int ans = -1;
        while (isConnected) {
            view.inform("Insert your username :");
            String username = virtualClient.askString();
            ans  = virtualClient.sendLogin(username);
            if (ans == -1) {
                view.reportError("Credential not valid, enter a new username:  ");
            } else if (ans == -2) {
                view.inform("Login successful");
                virtualClient.setNickname(username);
                break;
            } else {
                //view.inform("Reconnection successful");
                virtualClient.setNickname(username);
                break;
            }
        }
        return ans;
    }

    private void mainMenuLoop() throws Exception {
        while (isConnected) {
            view.inform("-----MENU-----");
            view.inform("1. Create new game");
            view.inform("2. Enter in a game");
            view.inform("3. Logout");
            int choice = 0;
            while (true) {
                switch (view) {
                    case TUIView v -> {
                        choice = virtualClient.askIndex() + 1;
                        if (choice > 0 && choice < 4) break;
                        view.inform("Invalid choice");
                    }
                    case GUIView v -> {}
                    default -> {}
                }

                switch (choice) {
                    case 1 -> createNewGame();
                    case 2 -> joinExistingGame();
                    case 3 -> {
                        virtualClient.logOut();
                        isConnected = false;
                    }
                    default -> view.inform("Choice not valid");
                }
            }
        }
    }

    public void createNewGame() throws Exception {
        switch (view) {
            case TUIView v -> {
                v.inform("Creating New Game");
                int response = virtualClient.sendGameRequest("CREATE");
                if (response != 0) {
                    v.inform("Game created successfully");
                    virtualClient.setGameId(response);
                    v.inform("Waiting for players in lobby");
                    waitForGameStart();
                } else {
                    v.reportError("Game creation failed");
                }
            }
            case GUIView v -> {
                int response = virtualClient.sendGameRequest("CREATE");
                if (response != 0) {
                    virtualClient.setGameId(response);
                    waitForGameStart();
                }else{
                    v.reportError("Game creation failed");
                }
            }
            default -> {}
        }
    }
    public void joinExistingGame() throws Exception {
        int response = virtualClient.sendGameRequest("JOIN");
        if (response != 0) {
            virtualClient.setGameId(response);
            waitForGameStart();
        } else {
            view.inform("Game not entered");
        }
    }

    private void waitForGameStart() throws Exception {
        while (true) {
            switch (view){
                case GUIView v -> v.setMainScene(SceneEnum.WAITING_QUEUE);
                default -> {}
            }
            String status = virtualClient.askInformationAboutStart();
            if (status.contains("start")) {
                startGame();
                break;
            }
        }
        view.inform("game started");
    }

    private void startGame() throws Exception {
        GamePhase gameState;
        do {
            String key = view.sendAvailableChoices();
            switch (key) {
                case "getacoveredtile" -> {
                    try {
                        tmpTile = virtualClient.getTileServer();
                        view.printTile(tmpTile);
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "getashowntile" ->{
                    try {
                        tmpTile = virtualClient.getUncoveredTile();
                        view.printTile(tmpTile);
                        view.printListOfCommand();
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "returnthetile" ->{
                    try {
                        virtualClient.getBackTile(tmpTile);
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "placethetile" -> virtualClient.positionTile(tmpTile);
                case "drawacard" -> {
                    try {
                        virtualClient.drawCard();
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "spinthehourglass" -> {
                    try {
                        virtualClient.rotateGlass();
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "declareready" -> {
                    try {
                        virtualClient.setReady();
                    }catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "watchadeck" -> virtualClient.lookDeck();
                case "watchaplayersship" -> virtualClient.lookDashBoard();
                case "rightrotatethetile" -> rotateRight();
                case "leftrotatethetile" -> rotateLeft();
                case "logout" -> {
                    virtualClient.logOut();
                    idCurrentGame = 0;
                }
                default -> view.inform("Action not recognized");
            }
            gameState = virtualClient.getGameFase();
        } while (!gameState.equals(GamePhase.EXIT));
    }

    private void rotateRight() throws Exception {
        if (tmpTile != null) {
            tmpTile.rotateRight();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
            view.printListOfCommand();
        } else {
            view.reportError("No tile selected to rotate");
        }
    }

    private void rotateLeft() throws Exception {
        if (tmpTile != null) {
            tmpTile.rotateLeft();
            view.inform("Rotated tile");
            view.printTile(tmpTile);
            view.printListOfCommand();
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



}


