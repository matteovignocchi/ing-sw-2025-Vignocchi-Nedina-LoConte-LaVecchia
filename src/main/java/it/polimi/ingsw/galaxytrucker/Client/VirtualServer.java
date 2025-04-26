package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {

    public int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException;
    public void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException;
    public boolean authenticate(String username, String password) throws RemoteException;
    public String waitForResponse() throws RemoteException;
    public void registerClient(VirtualView client) throws RemoteException;
    public int[] requestGamesList() throws RemoteException;
    public String waitForGameStart() throws Exception;
    public void rotateGlass() throws RemoteException;
    public void setReady()  throws RemoteException;
    public void lookDeck() throws RemoteException;
    public void lookDashBoard() throws RemoteException;
    public void logOut() throws RemoteException;
    public void getBackTile() throws RemoteException;
    public void positionTile() throws RemoteException;
    public void drawCard() throws RemoteException;
    public Tile getCoveredTileServer(String nickname) throws RemoteException;
    public Tile getUncoveredTileServer(String nickname) throws RemoteException;
}