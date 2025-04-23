package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class VirtualClientSocket implements Runnable, VirtualView {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final View view;
    private GameFase gameFase;
    private String lastResponse;

    public VirtualClientSocket(String host, int port , View view) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.view = view;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try{
            while(true){
                Object received = in.readObject();
                switch(received){
                    case GameFase g ->
                        gameFase = (GameFase) received;
                    case String s ->{
                        this.lastResponse = (String) received;
                        this.notifyAll();
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + received);
                }
            }
        } catch (IOException e) {
            view.reportError("CONNECTION ERROR: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            view.reportError("COMMUNICATION ERROR: " + e.getMessage());
        }

    }

    @Override
    public void inform(String message) throws Exception {
        this.view.inform(message);
    }

    @Override
    public void showUpdate() throws Exception {
        this.view.updateState(gameFase);
    }

    @Override
    public void reportError(String error) throws Exception {
        this.view.reportError(error);
    }

    @Override
    public boolean askDecision() throws Exception {
       return this.view.ask();
    }

    @Override
    public int askIndex() throws Exception {
        return this.view.askindex();
    }

    @Override
    public int[] askCoordinates() throws Exception {
        return this.view.askCordinate();
    }

    @Override
    public String askString() throws Exception {
        return this.view.askString();
    }

    @Override
    public void printList(List<Objects> pile) throws Exception {
        this.view.printList("",pile);
    }


    @Override
    public void printCard(Card card) throws Exception {
        this.view.printCard(card);
    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws Exception {
        this.view.printDashShip(dashboard);
    }

    @Override
    public void startMach() throws Exception {

    }

    @Override
    public void updateGameState(GameFase fase) throws Exception {
        this.gameFase = fase;
        showUpdate();
    }

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        ActionRequest loginRequest = new ActionRequest("LOGIN", new LoginRequest(username, password));
        sendRequest(loginRequest);
        return Boolean.parseBoolean(waitForResponce());

    }

    @Override
    public void sendGameRequest(String message) throws Exception {

    }

    @Override
    public  synchronized String waitForResponce() throws Exception {
        while (lastResponse == null) wait();
        String response = lastResponse;
        lastResponse = null;
        return response;
    }

    @Override
    public String waitForGameUpadate() throws Exception {
        return "";
    }

    @Override
    public List<String> requestGameList() throws Exception {
        return List.of();
    }

    @Override
    public List<String> getAvailableAction() throws Exception {
        return List.of();
    }

    @Override
    public List<Tile> getPileOfTile() throws Exception {
        return List.of();
    }

    @Override
    public String sendAction(String message) throws Exception {
//        out.writeObject(new ActionRequest(message) );
        return waitForResponce();
    }

    @Override
    public GameFase getCurrentGameState() throws Exception {
        return null;
    }

    @Override
    public Tile getTile(int i) throws Exception {
        return null;
    }

}
