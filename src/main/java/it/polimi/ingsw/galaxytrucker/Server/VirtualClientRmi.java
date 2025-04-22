package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Client.VirtualServerRmi;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    final VirtualServerRmi virtualServerRmiClient;

    public VirtualClientRmi(VirtualServerRmi virtualServerRmi) throws RemoteException {
        super();
        this.virtualServerRmiClient = virtualServerRmi;
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
}
