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
    VirtualServerRmi virtualServerRmiClient;
    public VirtualClientRmi(VirtualServerRmi virtualServerRmi) throws RemoteException {
        super();
    }

    @Override
    public void inform(String message) throws Exception {

    }

    @Override
    public void showUpdate() throws Exception {

    }

    @Override
    public void reportError(String error) throws Exception {

    }

    @Override
    public void askDecision() throws Exception {

    }

    @Override
    public void askIndex() throws Exception {

    }

    @Override
    public void askCoordinates() throws Exception {

    }

    @Override
    public void printList(List<Objects> pile) throws Exception {

    }

    @Override
    public void setFase(GameFase fase) throws Exception {

    }

    @Override
    public void printCard(Card card) throws Exception {

    }
}
