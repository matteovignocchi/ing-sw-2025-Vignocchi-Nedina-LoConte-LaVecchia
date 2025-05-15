package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
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

        loginLoop();
        mainMenuLoop();
    }

    private void loginLoop() throws Exception {
        while (isConnected) {
            view.inform("Insert your username :");
            String username = virtualClient.askString();
            if (virtualClient.sendLogin(username)) {
                view.inform("Login successful");
                virtualClient.setNickname(username);
                break;
            } else {
                view.reportError("Credential not valid");
            }
        }
    }

    private void mainMenuLoop() throws Exception {
        while (isConnected) {
            view.inform("-----MENU-----");
            view.inform("1. Create new game");
            view.inform("2. Enter in a game");
            view.inform("3. Logout");
            int choice;
            while(true){
                choice = virtualClient.askIndex() + 1;
                if(choice > 0 && choice<4) break;
                view.inform("Invalid choice");
            }
            switch (choice) {
                case 1 -> createNewGame();
                case 2 -> joinExistingGame();
                case 3 -> {
                    virtualClient.logOut();
                    System.exit(0);
                }
                default -> view.inform("Choice not valid");
            }
        }
    }

    private void createNewGame() throws Exception {
        view.inform("Creating New Game");
        int response = virtualClient.sendGameRequest("CREATE");
        if (response != 0) {
            view.inform("Game created successfully");
            virtualClient.setGameId(response);
            view.inform("Waiting for players in lobby");
            waitForGameStart();
        } else {
            view.inform("Game creation failed");

        }
    }

    private void joinExistingGame() throws Exception {
        int response = virtualClient.sendGameRequest("JOIN");
        if (response != 0) {
            view.inform("Joining existing game");
            virtualClient.setGameId(response);
            view.inform("Waiting for game start");
            waitForGameStart();
        } else {
            view.inform("Game not entered");
        }
    }

//    private void waitForPlayers() throws Exception {
//        while (true) {
//            String status = virtualClient.askInformationAboutStart();
//            if (status.contains("start")) {
//                startGame();
//                break;
//            }
////            view.inform(status);
//        }
//    }

    private void waitForGameStart() throws Exception {
        while (true) {
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
                case "getacoveredtile" -> tmpTile = virtualClient.getTileServer();
                case "getashowntile" -> tmpTile = virtualClient.getUncoveredTile();
                case "returnthetile" -> virtualClient.getBackTile(tmpTile);
                case "placethetile" -> virtualClient.positionTile(tmpTile);
                case "drawacard" -> virtualClient.drawCard();
                case "spinthehourglass" -> virtualClient.rotateGlass();
                case "declareready" -> virtualClient.setReady();
                case "watchadeck" -> virtualClient.lookDeck();
                case "watchaplayersship" -> virtualClient.lookDashBoard();
                case "rightrotatethetile" -> rotateRight();
                case "leftrotatethetile" -> rotateLeft();
                case "logout" -> {
                    virtualClient.logOut();
                    idCurrentGame = 0;
                }
                default -> view.inform("Action not recognized");
            }            gameState = virtualClient.getGameFase();
        } while (!gameState.equals(GamePhase.EXIT));
    }

//    private void choosePossibleActions() throws Exception {
//        String key = view.sendAvailableChoices();
//        switch (key) {
//            case "getblankettile" -> tmpTile = virtualClient.getTileServer();
//            case "takediscoverytile" -> tmpTile = virtualClient.getUncoveredTile();
//            case "returntile" -> virtualClient.getBackTile(tmpTile);
//            case "placetile" -> virtualClient.positionTile(tmpTile);
//            case "drawcard" -> virtualClient.drawCard();
//            case "spinthehourglass" -> virtualClient.rotateGlass();
//            case "declareready" -> virtualClient.setReady();
//            case "watchadeck" -> virtualClient.lookDeck();
//            case "watchaship" -> virtualClient.lookDashBoard();
//            case "rightrotatetile" -> rotateRight();
//            case "leftrotatetile" -> rotateLeft();
//            case "logout" -> {
//                virtualClient.logOut();
//                idCurrentGame = 0;
//            }
//            default -> view.inform("Action not recognized");
//        }
//    }

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



}


