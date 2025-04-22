package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GameFase;
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

public class ClientMain {

    private static GameFase gameFase;
    private static View view;
    private static VirtualView virtualClient;
    private static boolean isConnected;

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {

        Scanner input = new Scanner(System.in);
        System.out.println("Choose the type of protocol: 1 - RMI ; 2 - SOCKET");
        int protocolChoice = input.nextInt();

        System.out.println("Choose the type of view: 1 - TUI ; 2 - GUI");
        int viewChoice = input.nextInt();

        try{

            view = (viewChoice == 1) ? new TUIView() : new GUIView();

            String host = args.length > 0 ? args[0] : "localhost";

            if (protocolChoice == 1) {
                Registry registry = LocateRegistry.getRegistry(host, 1234);
                VirtualServerRmi server = (VirtualServerRmi) registry.lookup("GameServer");
                virtualClient = new VirtualClientRmi(server, view);
                server.registerClient(virtualClient);
                view.start();

            }else {
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
                virtualClient = new VirtualClientSocket(host , port , view);
                view.start();
            }

            isConnected = true;
            view.inform("Connected with success");
            view.inform("\n-----LOGIN-----");

            while(isConnected){
                String username = virtualClient.askString();
                String password = virtualClient.askString();

                boolean LoginSuccess = virtualClient.sendLogin(username, password);
                if(LoginSuccess){
                    view.inform("Login successful");
                    break;
                }else{
                    view.reportError("credenziali non valide");
                }
            }

            while(isConnected){
                view.inform("menu del server");
                view.inform("1. Crea nuova partita");
                view.inform("2 . unisci a una partita");
                view.inform("3 . logOut");

                int choice = virtualClient.askIndex();
                switch (choice) {
                    case 1:
                        createnewgame();
                        break;
                    case 2:
                        joinExistingGame();
                        break;
                    case 3:
                        System.exit(0);
                    default:
                        view.inform("scelta non valida");
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setGameFase(GameFase gameFase) {
        this.gameFase = gameFase;
    }

    private static void createnewgame() throws Exception{
        view.inform("Creating New Game");
        virtualClient.sendGameRequest("CREATE");

        String response = virtualClient.waitForResponce();
        view.inform(response);

        if(response.contains("create")){
            wairForPlayers();
        }
    }

    private static void waitGameStart() throws Exception{
        view.inform("Waiting for game started");
        while(true){
            String status = virtualClient.waitForGameUpadate();
            if(status.contains("start")){
                startgame();
                break;
            }
            view.inform(status);
        }
    }

    private static void wairForPlayers() throws Exception{
        view.inform("waiting for min 2 player in lobby");
        while(true){
            String status = virtualClient.waitForGameUpadate();
            if(status.contains("Start")){
                startgame();
                break;
            }
            view.inform(status);
        }
    }

    private static void joinExistingGame() throws Exception{
        view.inform("\n partite disponibili");
        List<String> avaibleGames = virtualClient.requestGameList();
        for(int i = 0 ; i < avaibleGames.size(); i++){
            view.inform((i+1) + "." + avaibleGames.get(i));
        }
        int choice = virtualClient.askIndex();
        virtualClient.sendGameRequest("JOIN_" + avaibleGames.get(choice-1));
        String response = virtualClient.waitForResponce();
        view.inform(response);
        if(response.contains("join")){
            waitGameStart();
        }


    }




    //metodo gestione partita
    private static void startgame() throws Exception{
        GameFase gameState =  virtualClient.getCurrentGameState();
        switch (gameState){
            case FASE0 -> handleMainActionPhase();

        }
    }



    //metodo gestione di che posso fare
    private static void handleMainActionPhase() throws Exception{
        view.inform("Possible actions:");
        List<String> possibleActions = virtualClient.getAvailableAction();

        for(int i = 0 ; i < possibleActions.size(); i++){
            view.inform((i+1)+"."+possibleActions.get(i));
        }
        int choice = virtualClient.askIndex();
        String result = virtualClient.sendAction(possibleActions.get(choice-1));
    }









}


//public class RMIClient extends UnicastRemoteObject implements VirtualViewRmi {
//    final VirtualServerRmi server;
//    //View view;
//    //view
//    public RMIClient(VirtualServerRmi server) throws RemoteException {
//        super();
//        this.server = server;
//    }
//
//
//
//    private void run() throws RemoteException {
//        //this.server.connect(this);
//        //this.graphicInterface(type);
//
//    }
//
//    private void startInterface(String type) throws RemoteException {
//        PlayerView view;
//        boolean flag = true;
//        while(flag) {
//            if (type.equalsIgnoreCase("TUI")) {
//                //view = new TUIView(service, playerId);
//                flag = false;
//            } else if(type.equalsIgnoreCase("GUI")) {
//                //view = new GUIView(service, playerId);
//                flag = false;
//            } else {
//                reportError("The response entered is invalid. Try again:");
//            }
//        }
//
//        //view.start();
//    }
//
//
//    //metodo send move, che chiama la view il suo metodo send move, che chiama il metodo send move del virtual server che lo manda al controller
//    public void sendMove() throws RemoteException {//qui generico
//        //virtualServerRmi.sendMove(playerId, move);
//
//    }
//
//    @Override
//    public void showUpdate() throws RemoteException {
//        //penso di mettere un tipo, cpsì fa print dashboard eccc
//    }
//
//    @Override
//    public void reportError(String error) throws RemoteException {
//        //gestire datarace per il report error, forse così va bene ma non sono sicuro
//        synchronized (System.err){
//            //view.reportError(error);
//        }
//    }
//
//    @Override
//    public void ask(String question) throws RemoteException {
//        synchronized (System.out){
//            //view.inform(question);
//        }
//
//    }
//
//    @Override
//    public void printPileOfTile(List<Tile> pile) throws RemoteException {
//        //view.printTiles(tiles); la TUI o GUI gestisce la stampa
//    }
//
//}
