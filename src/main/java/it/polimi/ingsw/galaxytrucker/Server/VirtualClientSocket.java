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
    private Object lastResponse;

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
                            case Message.OP_GAME_PHASE -> {
                                gameFase = (GameFase) msg.getPayload();
                                showUpdate();
                            }
                            case Message.OP_PRINT_CARD -> {
                                view.printCard((Card) msg.getPayload());
                            }
                            case Message.OP_PRINT_COVERED -> {
                                view.printPileCovered((List<Tile>) msg.getPayload());
                            }
                            case Message.OP_PRINT_SHOWN -> {
                                view.printPileShown((List<Tile>) msg.getPayload());
                            }
                            case Message.OP_PRINT_GOODS -> {
                                view.printListOfGoods((List<Colour>) msg.getPayload());
                            }
                            case Message.OP_PRINT_DASHBOARD -> {
                                view.printDashShip((Tile[][]) msg.getPayload());
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
                        view.reportError((String) msg.getPayload());
                    }
                    case Message.TYPE_NOTIFICATION -> {
                        view.inform((String) msg.getPayload());
                    }
                    case Message.TYPE_REQUEST -> {
                        switch (msg.getOperation()) {
                            case Message.OP_INDEX -> {
                                view.inform((String) msg.getPayload());
                                view.askIndex();
                            }
                            case Message.OP_COORDINATE -> {
                                view.inform((String) msg.getPayload());
                                view.askCordinate();
                            }
                            case Message.OP_STRING-> {
                                view.inform((String) msg.getPayload());
                                view.askString();
                            }

                        }
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
    public void inform(String message) throws IOException {
        view.inform(message);
    }

    @Override
    public void showUpdate(){
        view.updateState(gameFase);
    }

    @Override
    public void reportError(String error){
        view.reportError(error);
    }

    @Override
    public boolean ask(String message){
        return view.ask(message);
    }



    @Override
    public void printListOfTileCovered(List<Tile> tiles) {
        view.printPileCovered(tiles);
    }

    @Override
    public void printListOfTileShown(List<Tile> tiles)  {
        view.printListOfTiles(tiles);
    }

    @Override
    public int askIndex(){
        return view.askIndex();
    }

    @Override
    public int[] askCoordinates() {
        return view.askCordinate();
    }

    @Override
    public String askString(){
        return view.askString();
    }


    @Override
    public int[] askCoordinate() {
        return view.askCordinate();
    }


    @Override
    public void printListOfGoods(List<Colour> listOfGoods) throws Exception {
        view.printListOfGoods(listOfGoods);
    }


    @Override
    public void printCard(Card card){
        view.printCard(card);
    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard)  {
        view.printDashShip(dashboard);
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
        return Boolean.parseBoolean((String) waitForResponce());
    }

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        Message loginRequest = Message.request(Message.OP_LOGIN, new LoginRequest(username, password));
        sendRequest(loginRequest);
        return Boolean.parseBoolean((String) waitForResponce());

    }

    @Override
    public void sendGameRequest(String message) throws IOException {
        Message gameRequest = Message.request(Message.OP_LOGIN, message);
        sendRequest(gameRequest);
    }

    @Override
    public  synchronized Object waitForResponce() throws InterruptedException {
        while (lastResponse == null) wait();
        Object response = lastResponse;
        lastResponse = null;
        return response;
    }

    @Override
    public String waitForGameUpadate() throws InterruptedException {
        while (lastResponse == null) wait();
        String response = (String) lastResponse;
        lastResponse = null;
        return response;
    }

    @Override
    public List<String> requestGameList() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_LIST_GAMES, null);
        sendRequest(request);
        Object response = waitForResponce();
        return (List<String>) response;
    }

    @Override
    public List<String> getAvailableAction() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_ACTIONS, null);
        sendRequest(request);
        Object response = waitForResponce();
        return (List<String>) response;
    }



    @Override
    public void sendAction(int key)  {

    }


    @Override
    public GameFase getCurrentGameState() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GAME_PHASE,null);
        sendRequest(request);
        Object response = waitForResponce();
        return (GameFase) response;
    }

    @Override
    public Tile getTile() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GET_TILE, null);
        sendRequest(request);
        Object response = waitForResponce();
        return (Tile) response;
    }

    public void sendRequest(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }





}
