package it.polimi.ingsw.galaxytrucker.Server.RMI.Client;

import it.polimi.ingsw.galaxytrucker.PlayerView;
import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.RMI.Server.VirtualViewRmi;
import it.polimi.ingsw.galaxytrucker.Server.TUIView;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RMIClient extends UnicastRemoteObject implements VirtualViewRmi {
    final VirtualServerRmi server;
    //View view;
    //view
    public RMIClient(VirtualServerRmi server) throws RemoteException {
        super();
        this.server = server;
    }



    private void run() throws RemoteException {
        //this.server.connect(this);
        //this.graphicInterface(type);

    }

    private void startInterface(String type) throws RemoteException {
        PlayerView view;
        boolean flag = true;
        while(flag) {
            if (type.equalsIgnoreCase("TUI")) {
                //view = new TUIView(service, playerId);
                flag = false;
            } else if(type.equalsIgnoreCase("GUI")) {
                //view = new GUIView(service, playerId);
                flag = false;
            } else {
                reportError("The response entered is invalid. Try again:");
            }
        }

        //view.start();
    }


    //metodo send move, che chiama la view il suo metodo send move, che chiama il metodo send move del virtual server che lo manda al controller
    public void sendMove() throws RemoteException {//qui generico
        //virtualServerRmi.sendMove(playerId, move);

    }

    @Override
    public void showUpdate() throws RemoteException {
        //penso di mettere un tipo, cpsì fa print dashboard eccc
    }

    @Override
    public void reportError(String error) throws RemoteException {
        //gestire datarace per il report error, forse così va bene ma non sono sicuro
        synchronized (System.err){
            //view.reportError(error);
        }
    }

    @Override
    public void ask(String question) throws RemoteException {
        synchronized (System.out){
            view.inform(question);
        }

    }

    @Override
    public void printPileOfTile(List<Tile> pile) throws RemoteException {
        //view.printTiles(tiles); la TUI o GUI gestisce la stampa
    }

}
