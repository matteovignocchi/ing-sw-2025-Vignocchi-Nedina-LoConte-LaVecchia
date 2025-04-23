package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
        try {
            while (true) {
                Message msg = (Message) in.readObject();

                switch (msg.getMessageType()) {
                    case Message.TYPE_UPDATE -> {
                        switch (msg.getOperation()) {
                            case Message.OP_GET_BOARD -> {
                                gameFase = (GameFase) msg.getPayload();
                                showUpdate();
                            }
                        }
                    }
                    case Message.TYPE_RESPONSE -> {
                        synchronized (this) {
                            lastResponse = msg.getPayload() != null ? msg.getPayload().toString() : null;
                            notifyAll();
                        }
                    }
                    case Message.TYPE_ERROR -> {
                        view.reportError("ERROR: " + msg.getPayload());
                    }
                    case Message.TYPE_NOTIFICATION -> {
                        view.inform("NOTIFY: " + msg.getPayload());
                    }
                    default -> {
                        view.reportError("Unknown message type: " + msg.getMessageType());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            try {
                view.reportError("Connection error: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void inform(String message){
        this.view.inform(message);
    }

    @Override
    public void showUpdate(){
        this.view.updateState(gameFase);
    }

    @Override
    public void reportError(String error){
        this.view.reportError(error);
    }

    @Override
    public boolean ask(String message){
        return view.ask(message);
    }

    @Override
    public void printListOfGoods(List<Colour> listOfGoods){
        view.printListOfGoods(listOfGoods);
    }

    @Override
    public int askIndex(){
        return this.view.askIndex();
    }

    @Override
    public int[] askCoordinates() {
        return this.view.askCordinate();
    }

    @Override
    public String askString(){
        return this.view.askString();
    }



    @Override
    public void printCard(Card card){
        this.view.printCard(card);
    }

    @Override
    public void printListOfTileCovered(List<Tile> tiles) {

    }

    @Override
    public void printListOfTileShown(List<Tile> tiles)  {

    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard)  {
        this.view.printDashShip(dashboard);
    }

    @Override
    public void startMach() {

    }

    @Override
    public void updateGameState(GameFase fase){
        this.gameFase = fase;
        showUpdate();
    }

    @Override
    public boolean sendRegistration(String username, String password) throws Exception {
        Message registrationRequest = Message.request(Message.OP_REGISTER, new LoginRequest(username,password));
        sendRequest(registrationRequest);
        return Boolean.parseBoolean(waitForResponce());
    }

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        Message loginRequest = Message.request(Message.OP_LOGIN, new LoginRequest(username, password));
        sendRequest(loginRequest);
        return Boolean.parseBoolean(waitForResponce());

    }

    @Override
    public void sendGameRequest(String message){

    }

    @Override
    public  synchronized String waitForResponce() throws Exception {
        while (lastResponse == null) wait();
        String response = lastResponse;
        lastResponse = null;
        return response;
    }

    @Override
    public String waitForGameUpadate() {
        return "";
    }

    @Override
    public List<String> requestGameList()  {
        return List.of();
    }

    @Override
    public List<String> getAvailableAction()  {
        return List.of();
    }

    @Override
    public List<Tile> getPileOfTile()  {
        return List.of();
    }

    @Override
    public void sendAction(int key)  {

    }


    @Override
    public GameFase getCurrentGameState() {
        return null;
    }

    @Override
    public Tile getTile()  {
        return null;
    }

    @Override
    public int[] askCoordinate() {
        return new int[0];
    }

    public void sendRequest(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }


}
