package it.polimi.ingsw.galaxytrucker.Server;

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
    private GameFase gameFase;

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
    public boolean askDecision() throws RemoteException {
        return true;

    }

    @Override
    public int askIndex() throws RemoteException {
        return 2;

    }

    @Override
    public int[] askCoordinates() throws RemoteException {
        return new int[] {1,2};

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

    @Override
    public String askString() throws Exception {
        return "";
    }

    @Override
    public void startMach() throws Exception {

    }

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        return server.authenticate(username, password);
    }

    @Override
    public void sendGameRequest(String message) throws Exception {
        server.handleGameRequest(message);
    }

    @Override
    public String waitForResponce() throws Exception {
        return server.waitForResponnse();
    }

    @Override
    public String waitForGameUpadate() throws Exception {
        return "";
    }

    @Override
    public List<String> requestGameList() throws Exception {
        return server.getAvaibleGames();
    }

    @Override
    public List<String> getAvailableAction() throws Exception {
        return List.of();
    }

    @Override
    public String sendAction(String message) throws Exception {
        return server.handlePlayerAction(message);
    }

    @Override
    public GameFase getCurrentGameState() throws Exception {
        return gameFase;
    }

    public void updateGamesState(GameFase gameFase){
        this.gameFase = gameFase;
        view.updateState(gameFase);
    }
}
