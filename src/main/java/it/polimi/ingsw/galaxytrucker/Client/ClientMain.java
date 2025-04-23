package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientRmi;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientSocket;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import static it.polimi.ingsw.galaxytrucker.GameFase.*;
import static java.lang.String.valueOf;

public class ClientMain {

    private static GameFase gameFase;
    private static View view;
    private static VirtualView virtualClient;
    private static boolean isConnected;

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
                VirtualServerRmi server = (VirtualServerRmi) registry.lookup("GameServer");
                virtualClient = new VirtualClientRmi(server, view);
                server.registerClient(virtualClient);
                view.start();

            }else{
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
                virtualClient = new VirtualClientSocket(host , port , view);
                view.start();

            }

            isConnected = true;
            view.inform("Connected with success");
            view.inform("-----LOGIN-----");

            while(isConnected){
                view.inform("Insert your username and password:");
                String username = virtualClient.askString();
                String password = virtualClient.askString();
                boolean LoginSuccess = virtualClient.sendLogin(username, password);
                if(LoginSuccess){
                    view.inform("Login successful");
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

    public void setGameFase(GameFase gameFase) {
        ClientMain.gameFase = gameFase;
    }

    private static void createNewGame() throws Exception{

        view.inform("Creating New Game");
        virtualClient.sendGameRequest("CREATE");

        String response = virtualClient.waitForResponce();
        view.inform(response);

        if(response.contains("create")){
            waitForPlayers();
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
        view.inform("waiting for min 2 player in lobby");
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
        view.inform("Available Games");
        List<String> availableGames = virtualClient.requestGameList();
        for(int i = 0 ; i < availableGames.size(); i++){
            view.inform((i+1) + "." + availableGames.get(i));
        }
        int choice = virtualClient.askIndex();
        virtualClient.sendGameRequest("JOIN_" + availableGames.get(choice-1));
        String response = virtualClient.waitForResponce();
        view.inform(response);
        if(response.contains("join")){
            waitGameStart();
        }

    }

    private static void choosePossibleActions() throws Exception{
        view.inform("Possible actions:");
        List<String> possibleActions = virtualClient.getAvailableAction();

        for(int i = 0 ; i < possibleActions.size(); i++){
            view.inform(possibleActions.get(i));
        }
        int choice = virtualClient.askIndex();
        //chiedere perchè send action non è un void ma è un strin
        virtualClient.sendAction(choice-1);
    }

    //metodo gestione partita
    private static void startGame() throws Exception{
        GameFase gameState =  virtualClient.getCurrentGameState();

        do {
            switch (gameState) {
                case FASE0 -> {
                    view.updateState(FASE0);
                    choosePossibleActions();
                }
                case FASE1 -> {
                    view.updateState(FASE1);
                    choosePossibleActions();
                }
                case FASE2 -> {
                    view.updateState(FASE2);
                    choosePossibleActions();
                }
//                case FASE3 -> {
//                    view.updateState(FASE3);
//                    choosePossibleActions();
//                }
                case FASE4 -> {
                    view.updateState(FASE4);
                    choosePossibleActions();
                }
                case FASE5 -> {
                    view.updateState(FASE5);
                    choosePossibleActions();
                }
                case FASE6 -> {
                    view.updateState(FASE6);
                    choosePossibleActions();
                }
                case FASE7 -> {
                    view.updateState(FASE7);
                    choosePossibleActions();
                }
                case FASE8 -> {
                    view.updateState(FASE8);
                    choosePossibleActions();
                }
                case FASE9 -> {
                    view.updateState(FASE9);
                    choosePossibleActions();
                }
                case FASE10 -> {
                    view.updateState(FASE10);
                    choosePossibleActions();
                }
                case FASE11 -> {
                    view.updateState(FASE11);
                    choosePossibleActions();
                }
                case FASE12 -> {
                    view.updateState(FASE12);
                    choosePossibleActions();
                }
                case FASE13 -> {
                    view.updateState(FASE13);
                    choosePossibleActions();
                }
                case FASE14 -> {
                    view.updateState(FASE14);
                    choosePossibleActions();
                }
                default -> view.reportError("Problem with communication server");
            }

        } while (!gameState.equals(GameFase.FASE14));




    }



//    //metodo gestione di che posso fare
//    private static void handleMainActionPhase() throws Exception{
//        //chiedo ad oleg domani, prima non devo fare l'update della view perchè prima vedo
//        //la mainActionPhase poi scelgo cosa fare
//        view.updateState(FASE0);
//        view.inform("Possible actions:");
//        List<String> possibleActions = virtualClient.getAvailableAction();
//
//        for(int i = 0 ; i < possibleActions.size(); i++){
//            view.inform((i+1)+"."+possibleActions.get(i));
//        }
//        int choice = virtualClient.askIndex();
//        //chiedere perchè send action non è un void ma è un string
//        String result = virtualClient.sendAction(possibleActions.get(choice-1));
//    }

//    private static void handleChoosingCoveredTile() throws Exception{
//        view.updateState(FASE1);
//        //per fare più easy possiamo che ci da soltanto il numero di tessere coperte, che tanto passo da 151 circa,
//        //quindi mandare ogni volta la lista che risulta pesante
//        List<Tile> pile = virtualClient.getPileOfTile();
//        view.printList("pile", pile);
//        //VERSIONE 2
//        //int totalTile = virtualClient.getNumOfTile();
//        //view.printCovered(totalTile);
//        view.inform("Choose one of covered tile and give the index");
//        int index = virtualClient.askIndex();
//        //l'ho pensato così forse è sbagliato
//        Tile tile = virtualClient.getTile(index -1);
//        view.printTile(tile);
//        view.inform("Possible actions:");
//        List<String> possibleActions = virtualClient.getAvailableAction();
//        for(int i = 0 ; i < possibleActions.size(); i++){
//            view.inform((i+1)+"."+possibleActions.get(i));
//        }
//        int choice = virtualClient.askIndex();
//        String result = virtualClient.sendAction(possibleActions.get(choice -1));
//
//    }
//
//    private static void handleBuildingPhase() throws Exception{
//        view.updateState(FASE2);
//        view.inform("Choose one of slots and give the indexes");
//        int[] coordinate = view.askCordinate();
//        //qua credo ci vada il send coordinates però dobbiamo creare il metodo
//    }
//

}


