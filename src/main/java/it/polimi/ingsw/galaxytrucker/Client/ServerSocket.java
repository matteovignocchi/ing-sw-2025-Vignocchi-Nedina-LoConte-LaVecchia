package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

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

    @Override
    public List<String> getAvaibleGames() throws RemoteException {
        return List.of();
    }

    @Override
    public Tile getTileServer() throws RemoteException {
        return null;
    }

    @Override
    public boolean authenticate(String username, String password) throws RemoteException {
        return false;
    }

    @Override
    public Void handleGameRequest(String message) throws RemoteException {
        return null;
    }

    @Override
    public String waitForResponse() throws RemoteException {
        return "";
    }

    @Override
    public void handlePlayerAction(String message) throws RemoteException {

    }

    @Override
    public void registerClient(VirtualView client) throws RemoteException {

    }

    @Override
    public void getUncoveredTile() throws RemoteException {

    }

    @Override
    public void rotateGlass() throws RemoteException {

    }

    @Override
    public void setReady() throws RemoteException {

    }

    @Override
    public void lookDeck() throws RemoteException {

    }

    @Override
    public void lookDashBoard() throws RemoteException {

    }

    @Override
    public void logOut() throws RemoteException {

    }

    @Override
    public void activateCard() throws RemoteException {

    }

    @Override
    public void getBackTile() throws RemoteException {

    }

    @Override
    public void positionTile() throws RemoteException {

    }

    @Override
    public void drawCard() throws RemoteException {

    }

    @Override
    public void rightRotatedTile() throws RemoteException {

    }

    @Override
    public void leftRotatedTile() throws RemoteException {

    }
}
