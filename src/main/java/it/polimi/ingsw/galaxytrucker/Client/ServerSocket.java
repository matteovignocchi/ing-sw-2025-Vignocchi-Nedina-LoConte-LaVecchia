package it.polimi.ingsw.galaxytrucker.Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerSocket extends UnicastRemoteObject implements VirtualServer {
    protected ServerSocket() throws RemoteException {
    }

    @Override
    public void login(String username, String password) throws RemoteException {

    }

    @Override
    public void logout(String username) throws RemoteException {

    }

    @Override
    public void createNewGame(String username) throws RemoteException {

    }

    @Override
    public void enterGame(String username, int gameId) throws RemoteException {

    }

    @Override
    public void sendIndex(String username, int index) throws RemoteException {

    }

    @Override
    public void sendChoice(String username, boolean choice) throws RemoteException {

    }

    @Override
    public void sendCoordinates(String username, int x, int y) throws RemoteException {

    }

    @Override
    public void drawCard(String username) throws RemoteException {

    }

    @Override
    public void sendPlayerDash(String username, int dash) throws RemoteException {

    }
}
