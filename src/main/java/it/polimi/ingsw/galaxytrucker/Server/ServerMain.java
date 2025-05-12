package it.polimi.ingsw.galaxytrucker.Server;

import java.rmi.RemoteException;
import java.rmi.registry.*;

public class ServerMain {
    public static void main(String[] args) throws RemoteException {
        GameManager gameManager = new GameManager();
        int socketPort = args.length > 1 ? Integer.parseInt(args[1]) : 9999;

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("RmiServer", new ServerRmi(gameManager));

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

