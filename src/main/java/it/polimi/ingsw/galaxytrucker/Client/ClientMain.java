package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Server.ServerRmi;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUI.*;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import static java.lang.String.valueOf;

public class ClientMain {

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {

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

        //codice di inizializzazione del client

        //INDIRIZZO IP CORRENTE. A SECONDA LA RETE, OVVIAMENTE CAMBIA. BUONO FARE COSI PER DEBUG E PROVA INIZIALE
        //TODO: PER PRESENTAZIONE, SOLUZIONE PIU ROBUSTA (PASSARE TRAMINE ARGS O LEGGERE FILE DI CONFIGURAZIONE)
        String host = "192.168.179.16";
        int port = 30001;

        Scanner input = new Scanner(System.in);
        System.out.println("> Choose the type of protocol:\n 1 - RMI \n2 - SOCKET");
        int protocolChoice = input.nextInt();
        System.out.println("> Choose the type of view:\n 1 - TUI \n2 - GUI");
        int viewChoice = input.nextInt();

        View view = (viewChoice == 1) ? new TUIView() : new GUIView();

        try {
            VirtualView virtualClient;
            if (protocolChoice == 1) {
                Registry registry = LocateRegistry.getRegistry(host, 1099);
                //ServerRmi server = (ServerRmi) registry.lookup("RmiServer");
                VirtualServer server = (VirtualServer) registry.lookup("RmiServer");
                virtualClient = new VirtualClientRmi(server, view);
            } else {
                virtualClient = new VirtualClientSocket(host, port, view);
            }
            ClientController controller = new ClientController(view, virtualClient);

            controller.start();

        }catch (Exception e){
        System.err.println("error:"+e.getMessage());
        e.printStackTrace();}
    }

}


