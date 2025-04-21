package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class VirtualClientRmi extends UnicastRemoteObject {
    VirtualServerRmi virtualServerRmiClient;
    public VirtualClientRmi(VirtualServerRmi virtualServerRmi) throws RemoteException {
        super();
    }
}
