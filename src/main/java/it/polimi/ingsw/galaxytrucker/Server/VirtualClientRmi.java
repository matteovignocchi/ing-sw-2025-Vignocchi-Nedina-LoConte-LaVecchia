package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;

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
        this.view.updateState(gameFase);
    }

    @Override
    public void inform(String message) throws RemoteException {
        this.view.inform(message);
    }


    @Override
    public void reportError(String error) throws RemoteException {
        this.view.reportError(error);
    }

    @Override
    public boolean askDecision() throws RemoteException {
        return this.view.ask();
    }

    @Override
    public int askIndex() throws RemoteException {
        return this.view.askindex();

    }

    @Override
    public int[] askCoordinates() throws RemoteException {
        return this.view.askCordinate();

    }

    @Override
    public String askString() throws Exception {
        return this.view.askString();
    }

    @Override
    public void printCard(Card card) throws RemoteException {
        this.view.printCard(card);
    }

    @Override
    public void printList(List<Objects> pile) throws RemoteException {
        this.view.printList("",pile);
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
        return List.of();
    }

    @Override
    public List<Tile> getPileOfTile() throws RemoteException {
        return List.of();
    }

    @Override
    public GameFase getCurrentGameState() throws RemoteException {
        return gameFase;
    }

    @Override
    public Tile getTile() throws RemoteException {
        return server.getTileServer();
    }

    @Override
    public int[] askCoordinate() {
        return new int[0];
    }

    @Override
    public boolean ask(String s) {
        return false;
    }

    @Override
    public void printListOFGoods(List<Colour> listOfGoods) {

    }


    //PARTE COMUNICAZIONE CON IL SERVER

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
