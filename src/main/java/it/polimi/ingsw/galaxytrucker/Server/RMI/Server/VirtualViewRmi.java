package it.polimi.ingsw.galaxytrucker.Server.RMI.Server;

import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VirtualViewRmi extends Remote, VirtualView {
    @Override
    void showUpdate() throws RemoteException;
    @Override
    void reportError(String error) throws RemoteException;
    @Override
    void ask(String question) throws RemoteException;
    @Override
    void printPileOfTile(List<Tile> pile) throws RemoteException;
}
