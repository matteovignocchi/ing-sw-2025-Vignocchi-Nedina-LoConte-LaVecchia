package it.polimi.ingsw.galaxytrucker.Server;

import java.rmi.RemoteException;
import java.rmi.registry.*;

public class ServerMain {
    public static void main(String[] args) throws RemoteException {

        //INDIRIZZO IP CORRENTE. A SECONDA LA RETE, OVVIAMENTE CAMBIA. BUONO FARE COSI PER DEBUG E PROVA INIZIALE
        //TODO: PER PRESENTAZIONE, SOLUZIONE PIU ROBUSTA (PASSARE TRAMINE ARGS O LEGGERE FILE DI CONFIGURAZIONE)
        System.setProperty("java.rmi.server.hostname", "192.168.1.51");

        GameManager gameManager = new GameManager();
        int socketPort = 30001;
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("RmiServer", new ServerRmi(gameManager));
        System.out.println("Server-Rmi ready on port 1099");

        ServerSocketMain socketMain = new ServerSocketMain(gameManager, socketPort);
        Thread socketThread = new Thread(socketMain, "Socket-Listener");
        socketThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            socketThread.interrupt();
            try { socketThread.join();
            } catch (InterruptedException ignored) {}
        }));
    }
}

