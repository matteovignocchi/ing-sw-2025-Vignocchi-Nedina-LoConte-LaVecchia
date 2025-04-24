package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import java.rmi.RemoteException;
import java.rmi.registry.*;

//TODO: gestione Rmi fatta, gestione Socket da fare

public class ServerMain {
    public static void main (String[] args) throws RemoteException {
        try{
            GameManager gameManager = new GameManager();
            Registry registry = LocateRegistry.createRegistry(1099);
            ServerRmi rmiServer = new ServerRmi(gameManager);
            registry.rebind("RmiServer", rmiServer);
            System.out.println("Server Rmi ready on port 1099");
        } catch (Exception e) {
            System.out.println("Error in ServerMain initialization");
            e.printStackTrace(); //va bene come stampa d'errore ?
        }
    }
}
