package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    private final ServerRmi server;
    private View view;
    private GameFase gameFase;

    public VirtualClientRmi(ServerRmi server, View view) throws RemoteException {
        super();
        this.server = server;
        this.view = view;
    }

    //PARTE VIEW
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void showUpdate() throws RemoteException {
        view.updateState(gameFase);
    }

    @Override
    public void inform(String message) throws RemoteException {
        view.inform(message);
    }


    @Override
    public void reportError(String error) throws RemoteException {
        view.reportError(error);
    }

    @Override
    public boolean ask(String message) throws RemoteException {
        return view.ask(message);
    }

    @Override
    public int askIndex() throws RemoteException {
        return view.askIndex();
    }

    @Override
    public int[] askCoordinates() throws RemoteException {
        return view.askCordinate();

    }

    @Override
    public String askString() throws RemoteException {
        return view.askString();
    }

    @Override
    public int[] askCoordinate() throws RemoteException {
        return view.askCordinate();
    }

    @Override
    public void printCard(Card card) throws RemoteException {
        view.printCard(card);
    }

    @Override
    public void printListOfTileCovered(List<Tile> tiles) throws RemoteException {
        view.printPileCovered(tiles);
    }

    @Override
    public void printListOfTileShown(List<Tile> tiles) throws RemoteException {
        view.printPileShown(tiles);
    }

    @Override
    public void printListOfGoods(List<Colour> listOfGoods) {
        view.printListOfGoods(listOfGoods);
    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws RemoteException {
        this.view.printDashShip(dashboard);
    }


    //FASI DI GIOCO
    @Override
    public void updateGameState(GameFase fase) throws RemoteException{
        this.gameFase = fase;
        showUpdate();
    }



    //PARTI DI GIOCO
    @Override
    public void startMach() throws RemoteException {

    }

    @Override
    public List<String> requestGameList() throws RemoteException {
         return server.getAvaibleGames();
    }

    @Override
    public List<String> getAvailableAction() throws RemoteException {
        return server.getAvailableChoices();
    }
    //tutti questi metodi get non sono inutili dato che è sempre una
    //lista di comandi, tanto io mando il comando e il server me la printa

    @Override
    public GameFase getCurrentGameState() throws RemoteException {
        return gameFase;
    }

    @Override
    public Tile getTile() throws RemoteException {
        return server.getTileServer();
    }



    //PARTE COMUNICAZIONE CON IL SERVER
    @Override
    public boolean sendRegistration(String username, String password) throws RemoteException {
        return server.registerCredential(username, password);
    }

    @Override
    public boolean sendLogin(String username, String password) throws RemoteException {
        return server.authenticate(username, password);
    }

    @Override
    public void sendGameRequest(String message) throws RemoteException {
        server.handleGameRequest(message);
    }

    @Override
    public String waitForResponce() throws RemoteException {
        return server.waitForResponse();
    }

    @Override
    public String waitForGameUpadate() throws RemoteException {
        return server.waitForGameStart();
    }

    @Override
    public void sendAction(int key) throws RemoteException {
         server.handlePlayerAction("key");
    }


}
