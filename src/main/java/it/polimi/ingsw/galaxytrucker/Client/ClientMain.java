package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientRmi;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientSocket;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientMain {

    public GameFase gameFase;

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {
        Scanner input = new Scanner(System.in);
        System.out.println("Choose the type of protocol: 1 - RMI ; 2 - SOCKET");
        int protocolChoice = input.nextInt();
        System.out.println("Choose the type of view: 1 - TUI ; 2 - GUI");
        int viewChoice = input.nextInt();

        try{
            View view;

            if (viewChoice == 1) {
                view = new TUIView();
            } else {
                view = new GUIView();
            }
            if (protocolChoice == 1) {

                String host = args.length > 0 ? args[0] : "localhost";
                Registry registry = LocateRegistry.getRegistry(host, 1234);
                VirtualServerRmi server = (VirtualServerRmi) registry.lookup("GameServer");

                VirtualClientRmi client = new VirtualClientRmi(server, view);
                server.registerClient(client);
                view.start();

            }else {
                // SOCKET
                String host = args.length > 0 ? args[0] : "localhost";
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 9999;
                new VirtualClientSocket(host , port , view);
                view.start();
            }




        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
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
