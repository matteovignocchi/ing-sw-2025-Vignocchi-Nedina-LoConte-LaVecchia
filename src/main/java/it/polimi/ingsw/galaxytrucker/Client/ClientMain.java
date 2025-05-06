package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientRmi;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientSocket;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import static it.polimi.ingsw.galaxytrucker.GamePhase.*;
import static java.lang.String.valueOf;

public class ClientMain {
    private static View view;
    private static VirtualView virtualClient;
    private static boolean isConnected;
    private static Tile tmpTile;
    private static int idCurrentGame;

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {

        Scanner input = new Scanner(System.in);
        System.out.println("> Choose the type of protocol:\n 1 - RMI \n2 - SOCKET");
        int protocolChoice = input.nextInt();
        System.out.println("> Choose the type of view:\n 1 - TUI \n2 - GUI");
        int viewChoice = input.nextInt();

        try{
            view = (viewChoice == 1) ? new TUIView() : new GUIView();
            String host = args.length > 0 ? args[0] : "localhost";

            if (protocolChoice == 1) {
                Registry registry = LocateRegistry.getRegistry(host, 1099);
                ServerRmi server = (ServerRmi) registry.lookup("GameServer");
                virtualClient = new VirtualClientRmi(server, view);
//                server.registerClient(virtualClient);
                view.start();

            }else{
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
                virtualClient = new VirtualClientSocket(host , port , view);
                view.start();
            }
            isConnected = true;
            view.inform("Connected with success");
            while(isConnected){
                view.inform("Insert your username :");
                String username = virtualClient.askString();
                boolean LoginSuccess = virtualClient.sendLogin(username);
                if(LoginSuccess){
                    view.inform("Login successful");
                    virtualClient.setNickname(username);
                    break;
                }else{
                    view.reportError("Credential not valid");
                }
            }
            while(isConnected){
                view.inform("-----MENU-----");
                view.inform("1. Create new game");
                view.inform("2. Enter in a game");
                view.inform("3. Logout");
                int choice = virtualClient.askIndex();
                switch (choice) {
                    case 1:
                        createNewGame();
                    case 2:
                        joinExistingGame();
                    case 3:
                        System.exit(0);
                        break;
                    default:
                        view.inform("Choice not valid");
                }
            }
        } catch (Exception e) {
            System.err.println("> Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createNewGame() throws Exception{
        view.inform("Creating New Game");
        int response = virtualClient.sendGameRequest("CREATE");
        if(response != 0){
            view.inform("Game created successfully");
            virtualClient.setGameId(response);
            waitForPlayers();
        }else{
            view.inform("Game creation failed");
        }
    }

    private static void waitGameStart() throws Exception{
        view.inform("Waiting for game started");
        while(true){
            String status = virtualClient.waitForGameUpadate();
            if(status.contains("start")){
                startGame();
                break;
            }
            view.inform(status);
        }
    }

    private static void waitForPlayers() throws Exception{
        view.inform("waiting for player in lobby");
        while(true){
            String status = virtualClient.waitForGameUpadate();
            if(status.contains("Start")){
                startGame();
                break;
            }
            view.inform(status);
        }
    }

    private static void joinExistingGame() throws Exception{
        int response= virtualClient.sendGameRequest("JOIN_");
        if(response != 0){
            view.inform("Joining existing game");
            virtualClient.setGameId(response);
            waitGameStart();
        }else{
            view.inform("Game not entered");
        }

    }

    private static void choosePossibleActions() throws Exception{
        String key = view.sendAvailableChoices();
        switch (key) {
            case "getblankettile"->  tmpTile = virtualClient.getTileServer();
            case "takediscoverytile"-> tmpTile = virtualClient.getUncoveredTile();
            case "returntile" -> virtualClient.getBackTile(tmpTile);
            case "placetile" -> virtualClient.positionTile(tmpTile);
            case "drawcard" -> virtualClient.drawCard();
            case "spinthehourglass"-> virtualClient.rotateGlass();
            case "declareready" -> virtualClient.setReady();
            case "watchadeck" -> virtualClient.lookDeck();
            case "watchaship" -> virtualClient.lookDashBoard();
            case "rightrotatetile" -> rightRotatedTile(tmpTile);
            case "leftrotatetile" -> leftRotatedTile(tmpTile);
            case " logout" -> {
                virtualClient.logOut();
                idCurrentGame = 0;
            }
        }
    }

    private static void rightRotatedTile(Tile tile) throws Exception{
        tile.RotateRight();
        view.inform("Rotated tile");
        view.printTile(tile);
    }
    private static void leftRotatedTile(@NotNull Tile tile) throws Exception{
        tile.RotateLeft();
        view.inform("Rotated tile");
        view.printTile(tile);
    }

    //metodo gestione partita
    private static void startGame() throws Exception {
        GamePhase gameState;
        do {
            choosePossibleActions();
            gameState = virtualClient.getGameFase();
        } while (!gameState.equals(EXIT));

    }

    private void showTitle(){
        System.out.println(
                                " _____       _                    _____               _             \n"+
                                "|  __ \\     | |                  |_   _|             | |            \n"+
                                "| |  \\/ __ _| | __ ___  ___   _    | |_ __ _   _  ___| | _____ _ __ \n"+
                                "| | __ / _` | |/ _` \\ \\/ / | | |   | | '__| | | |/ __| |/ / _ \\ '__|\n"+
                                "| |_\\ \\ (_| | | (_| |>  <| |_| |   | | |  | |_| | (__|   <  __/ |   \n"+
                                " \\____/\\__,_|_|\\__,_/_/\\_\\\\__, |   \\_/_|   \\__,_|\\___|_|\\_\\___|_|   \n"+
                                "                           __/ |                                    \n"+
                                "                          |___/                                     \n"
                );
    }
}


