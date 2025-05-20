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
            handleWaitForGameStart();
            startGame();
        }

        while (isConnected) {
            switch (view){
                case TUIView v -> mainMenuLoop();
                default -> {}
            }

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
                    v.inform("Waiting for players in lobby");
                    handleWaitForGameStart();
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
            handleWaitForGameStart();
            startGame();
        } else if (gameId==0) {
            mainMenuLoop();
        } else {
            view.reportError("Cannot join game");
        }
    }


    public void waitForGameStart() throws Exception {
        System.out.println("Waiting for the game to start... (type 'exit' to return to the main menu)");
        while (true) {
            if (exitWaitingQueue) {
                mainMenuLoop();
                return;
            }
            String status = virtualClient.askInformationAboutStart();
            if (status.contains("start")) {
                return;
            }
            Thread.sleep(500); // evita spam
        }
    }

    public void exitQueue() {
        exitWaitingQueue = true;
    }

    public void handleWaitForGameStart() {
        ClientController controller = this;
        Thread waitingThread = new Thread(() -> {
            try {
                controller.waitForGameStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        waitingThread.start();
        Scanner scanner = new Scanner(System.in);
        while (waitingThread.isAlive()) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input.trim())) {
                controller.exitQueue();
                break;
            }
        }
    }


    private void startGame() throws Exception {
        view.inform("Game started");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            GamePhase gameState = virtualClient.getGameFase();
            if (gameState == GamePhase.EXIT) {
                view.inform("Returned to main menù...");
                return;
            }

            String key = null;
            while (key == null) {
                if (virtualClient.getGameFase() == GamePhase.EXIT) {
                    view.inform("Returned to main menù...");
                    return;
                }
                if (stdin.ready()) {
                    key = view.sendAvailableChoices();
                } else {
                    Thread.sleep(100);
                }
            }

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


    //SOLUZIONE E PROBLEMA SECONDO ME:
//Il problema vero è che, mentre tu sei dentro a String key = view.sendAvailableChoices();
//sei bloccato finché l’utente non digita qualcosa, e quindi non puoi mai ripassare al controllo di if (virtualClient.getGameFase() == GamePhase.EXIT) { … }
//La soluzione più rapida è trasformare quell’input da bloccante a un piccolo polling loop: invece di chiamare una volta sola sendAvailableChoices(),
// fai un ciclo in cui ogni 100 ms controlli se ti è arrivato l’EXIT; solo se non c’è entri a leggere l’input.


//    private void startGame() throws Exception {
//        view.inform("game started");
//        GamePhase gameState;
//        do {
//            String key = view.sendAvailableChoices();
//            switch (key) {
//                case "getacoveredtile" -> {
//                    try {
//                        tmpTile = virtualClient.getTileServer();
//                        view.printTile(tmpTile);
//                    }catch (BusinessLogicException e) {
//                        view.reportError(e.getMessage());
//                    }
//                }
//                case "getashowntile" -> {
//                    try {
//                        tmpTile = virtualClient.getUncoveredTile();
//                        view.printTile(tmpTile);
//                    } catch (BusinessLogicException | IOException | InterruptedException e) {
//                        view.reportError(e.getMessage());
//                    }
//                    view.printListOfCommand();
//                }
//                case "returnthetile" ->{
//                    try {
//                        virtualClient.getBackTile(tmpTile);
//                    }catch (BusinessLogicException e) {
//                        view.reportError(e.getMessage());
//                    }
//                }
//                case "placethetile" -> virtualClient.positionTile(tmpTile);
//                case "drawacard" -> {
//                    try {
//                        virtualClient.drawCard();
//                    }catch (BusinessLogicException e) {
//                        view.reportError(e.getMessage());
//                    }
//                }
//                case "spinthehourglass" -> {
//                    try {
//                        virtualClient.rotateGlass();
//                    }catch (BusinessLogicException e) {
//                        view.reportError(e.getMessage());
//                    }
//                }
//                case "declareready" -> {
//                    try {
//                        virtualClient.setReady();
//                    }catch (BusinessLogicException e) {
//                        view.reportError(e.getMessage());
//                    }
//                }
//                case "watchadeck" -> virtualClient.lookDeck();
//                case "watchaplayersship" -> virtualClient.lookDashBoard();
//                case "rightrotatethetile" -> rotateRight();
//                case "leftrotatethetile" -> rotateLeft();
//                case "logout" -> {
//                    virtualClient.leaveGame();
//                    return;
//                }
//                default -> view.inform("Action not recognized");
//            }
//            gameState = virtualClient.getGameFase();
//        } while (gameState != GamePhase.EXIT);
//    }

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


