package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Client.VirtualServerRmi;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    private final VirtualServerRmi server;
    private View view;

    public VirtualClientRmi(VirtualServerRmi server, View view) throws RemoteException {
        super();
        this.server = server;
        this.view = view;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void inform(String message) throws RemoteException {

    }

    @Override
    public void showUpdate() throws RemoteException {

    }

    @Override
    public void reportError(String error) throws RemoteException {

    }

    @Override
    public void askDecision() throws RemoteException {

    }

    @Override
    public void askIndex() throws RemoteException {

    }

    @Override
    public void askCoordinates() throws RemoteException {

    }

    @Override
    public void printList(List<Objects> pile) throws RemoteException {

    }

    @Override
    public void setFase(GameFase fase) throws RemoteException {

    }

    @Override
    public void printCard(Card card) throws RemoteException {

    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws Exception {

    }
}
