package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VirtualServer extends Remote {

    public int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException;
    public void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException;
    public void logOut(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    public Tile getCoveredTile(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    public List<Tile> getUncoveredTilesList(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    public Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws RemoteException, BusinessLogicException;
    //Il vecchio returnTile, scarta la tessera e la mette nella lista delle tiles scoperte
    public void dropTile (int gameId, String nickname, Tile tile) throws RemoteException, BusinessLogicException;
    public void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws RemoteException, BusinessLogicException;

    public String waitForResponse() throws RemoteException;
    public int[] requestGamesList() throws RemoteException;
    public String waitForGameStart() throws Exception;
    public void rotateGlass() throws RemoteException;
    public void setReady()  throws RemoteException;
    public void lookDeck() throws RemoteException;
    public void lookDashBoard() throws RemoteException;
    public void getBackTile() throws RemoteException;
    public void drawCard() throws RemoteException;
}