package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface VirtualServer extends Remote {

    int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException;
    void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException;
    void LeaveGame(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    String getCoveredTile(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    String getUncoveredTilesList(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    String chooseUncoveredTile(int gameId, String nickname, int idTile) throws RemoteException, BusinessLogicException;
    void dropTile (int gameId, String nickname, String tile) throws RemoteException, BusinessLogicException;
    void placeTile(int gameId, String nickname, String tile, int[] cord) throws RemoteException, BusinessLogicException;
    void setReady(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    void rotateGlass(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    String showDeck(int gameId, int idxDeck) throws IOException, BusinessLogicException;
    String[][] lookAtDashBoard(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    void drawCard(int gameId, String nickname) throws RemoteException, BusinessLogicException;
    Map<Integer,int[]> requestGamesList() throws RemoteException, BusinessLogicException;
    int logIn(String nickname, VirtualView v) throws RemoteException, BusinessLogicException;
    void logOut(String nickname) throws RemoteException, BusinessLogicException;
    String getReservedTile(int gameId, String nickname , int id) throws RemoteException, BusinessLogicException;
}