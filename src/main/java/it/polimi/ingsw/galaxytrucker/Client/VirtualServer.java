package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VirtualServer extends Remote {

    int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException;
    void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException;
    void logOut(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    Tile getCoveredTile(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    List<Tile> getUncoveredTilesList(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws RemoteException, BusinessLogicException;
    void dropTile (int gameId, String nickname, Tile tile) throws RemoteException, BusinessLogicException;
    void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws RemoteException, BusinessLogicException;
    void setReady(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    void rotateGlass(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    List<Card> showDeck(int gameId, int idxDeck) throws IOException, BusinessLogicException;
    Tile[][] lookAtDashBoard(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    void drawCard(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    Map<Integer,int[]> requestGamesList() throws RemoteException, BusinessLogicException;
}