package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import static java.lang.String.valueOf;


//trasporto il client da un'altra parte per una questione di scalabilità e correttezza :
//non dovendo lavorare con static diventa più testabile e se plice

public class ClientController {
    private final View view;
    private final VirtualView virtualClient;
    private Tile tmpTile;
    private boolean isConnected = false;
    private int idCurrentGame;
    private volatile boolean exitWaitingQueue = false;



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

        if (gameId > 0) {
            virtualClient.enterGame(gameId);
            waitForGameStart();
            view.inform("Game started");
            startGame();
        }

        while (isConnected) {
            switch (view){
                case TUIView v -> mainMenuLoop();

                default -> {}
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
            int res = virtualClient.sendLogin(username);
            if (res == -1) {
                view.reportError("Credential not valid, try again.");
            } else {
                // nickname accettato (nuovo o reconnect)
                virtualClient.setNickname(username);
                if (res == -2) {
                    view.inform("Login successful");
                } else {
                    view.inform("Reconnection successful, rejoining game " + res);
                }
                return res;
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
                    default -> {}
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
                int gameId = virtualClient.sendGameRequest("CREATE");
                if (gameId > 0) {
                    virtualClient.setGameId(gameId);
                    boolean started = waitForGameStart();
                    if (!started) return;
                    view.inform("Game is starting!");
                    startGame();
                } else {
                    view.reportError("Game creation failed");
                }
            }
            case GUIView v -> {
                int response = virtualClient.sendGameRequest("CREATE");
                if (response != 0) {
                    virtualClient.setGameId(response);
                    handleWaitForGameStart();
                }else{
                    v.reportError("Game creation failed");
                }
            }
            default -> {}
        }
    }

    public void joinExistingGame() throws Exception {
        view.inform("Joining Existing Game...");
        int gameId = virtualClient.sendGameRequest("JOIN");
        if (gameId > 0) {
            virtualClient.setGameId(gameId);
            boolean started = waitForGameStart();
            if (!started) return;
            startGame();
        }
    }

    private boolean waitForGameStart() throws Exception {
        view.inform("Waiting for other players… \ntype 'exit' to return to main menù");

        while (true) {
            if (virtualClient.getGameFase() == GamePhase.EXIT) {
                view.inform("Lobby closed, returning to main menu");
                return false;
            }
            if (virtualClient.getGameFase() == GamePhase.BOARD_SETUP) {
                return true;
            }
            String line = view.askString().trim();
            if ("exit".equalsIgnoreCase(line)) {
                virtualClient.leaveGame();
                view.inform("You left the lobby, returning to main menu");
                return false;
            }
        }
    }


    private boolean handleWaitForGameStart() throws Exception {
        view.inform("Waiting for other players…");
        view.inform("Type 'exit' to abandon the lobby and return to main menu.");

        while (true) {
            // il server ha avanzato lo stato: il gioco parte
            if (virtualClient.getGameFase() != GamePhase.WAITING_FOR_PLAYERS) {
                view.inform("Game is starting!");
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


//    public void handleWaitForGameStart() {
//        view.inform("Waiting for players (type 'exit' to cancel)");
//        ClientController controller = this;
//        Thread waitingThread = new Thread(() -> {
//            try {
//                controller.waitForGameStart();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        waitingThread.start();
//        Scanner scanner = new Scanner(System.in);
//        while (waitingThread.isAlive()) {
//            String input = scanner.nextLine();
//            if ("exit".equalsIgnoreCase(input.trim())) {
//                controller.exitQueue();
//                break;
//            }
//        }
//    }


    private void startGame() throws Exception {
        while (true) {
            // 1) controllo rapido di EXIT
            if (virtualClient.getGameFase() == GamePhase.EXIT) {
                view.inform("Returned to main menu...");
                return;
            }

            // 2) **qui** (e solo qui) stampo la lista dei comandi
            view.printListOfCommand();

            // 3) leggo una scelta valida SENZA ristampare il menu
            String key = view.sendAvailableChoices();
            switch (key) {
                case "getacoveredtile" -> {
                    try {
                        tmpTile = virtualClient.getTileServer();
                        view.printTile(tmpTile);
                    } catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "getashowntile" -> {
                    try {
                        tmpTile = virtualClient.getUncoveredTile();
                        view.printTile(tmpTile);
                    } catch (BusinessLogicException | IOException | InterruptedException e) {
                        view.reportError(e.getMessage());
                    }
                    view.printListOfCommand();
                }
                case "returnthetile" -> {
                    try {
                        virtualClient.getBackTile(tmpTile);
                    } catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "placethetile" -> virtualClient.positionTile(tmpTile);
                case "drawacard" -> {
                    try {
                        virtualClient.drawCard();
                    } catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "spinthehourglass" -> {
                    try {
                        virtualClient.rotateGlass();
                    } catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "declareready" -> {
                    try {
                        virtualClient.setReady();
                    } catch (BusinessLogicException e) {
                        view.reportError(e.getMessage());
                    }
                }
                case "watchadeck" -> virtualClient.lookDeck();
                case "watchaplayersship" -> virtualClient.lookDashBoard();
                case "rightrotatethetile" -> rotateRight();
                case "leftrotatethetile" -> rotateLeft();

                case "logout" -> {
                    virtualClient.leaveGame();
                    view.inform("Returned to main menù...");
                    return;
                }

                default -> view.inform("Action not recognized");
            }
        }
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


