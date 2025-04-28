package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import it.polimi.ingsw.galaxytrucker.Client.ServerSocketMain;
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class ServerMain {
    public static void main (String[] args) throws RemoteException {
        try{
            GameManager gameManager = new GameManager();
            String host = args.length > 0 ? args[0] : "localhost";
            int socketPort = args.length > 1 ? Integer.parseInt(args[1]) : 9999;

            Registry registry = LocateRegistry.createRegistry(1099);
            ServerRmi rmiServer = new ServerRmi(gameManager);
            registry.rebind("RmiServer", rmiServer);
            System.out.println("Server Rmi ready on port 1099");

            Thread socketThread = new Thread(new ServerSocketMain(gameManager,socketPort), host);
            socketThread.start();
            System.out.println("Server Socket ready on port " + socketPort);

        } catch (Exception e) {
            System.out.println("Error in ServerMain initialization");
            e.printStackTrace();
        }
    }
}
