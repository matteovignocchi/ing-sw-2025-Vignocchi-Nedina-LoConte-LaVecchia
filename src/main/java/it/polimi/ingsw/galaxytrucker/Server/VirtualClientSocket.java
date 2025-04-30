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
import java.rmi.RemoteException;
import java.util.List;

public class VirtualClientSocket implements Runnable, VirtualView {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final View view;
    private GameFase gameFase;
    private Object lastResponse;
    private String nickname;

    /// METODI DI INIZIALIZZAZIONE ///

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
                            case Message.OP_GAME_PHASE -> this.updateGameState((GameFase)  msg.getPayload());
                            case Message.OP_PRINT_CARD -> this.printCard((Card) msg.getPayload());
                            case Message.OP_PRINT_COVERED -> this.printListOfTileCovered((List<Tile>) msg.getPayload());
                            case Message.OP_PRINT_SHOWN -> this.printListOfTileShown((List<Tile>) msg.getPayload());
                            case Message.OP_PRINT_GOODS -> this.printListOfGoods((List<Colour>) msg.getPayload());
                            case Message.OP_PRINT_DASHBOARD -> this.printPlayerDashboard((Tile[][]) msg.getPayload());
                            case Message.OP_PRINT_DECK -> this.printDeck((List<Card>) msg.getPayload());
                            case Message.OP_PRINT_TILE -> this.printTile((Tile) msg.getPayload());
                            case Message.OP_SET_NICKNAME -> this.setNickname((String)msg.getPayload());
                            case Message.OP_UPDATE_VIEW -> {
                                UpdateViewRequest payload = (UpdateViewRequest) msg.getPayload();
                                this.showUpdate(
                                        payload.getNickname(),
                                        payload.getFirePower(),
                                        payload.getPowerEngine(),
                                        payload.getCredits(),
                                        payload.getPosition(),
                                        payload.hasPurpleAlien(),
                                        payload.hasBrownAlien(),
                                        payload.getNumberOfHuman(),
                                        payload.getNumberOfEnergy()
                                );
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
                        }
                    }
                    case Message.TYPE_RESPONSE -> {
                        synchronized (this) {
                            lastResponse = msg.getPayload() != null ? msg.getPayload().toString() : null;
                            notifyAll();
                        }
                    }
                    case Message.TYPE_ERROR -> this.reportError((String) msg.getPayload());
                    case Message.TYPE_NOTIFICATION -> this.inform((String) msg.getPayload());
                    case Message.TYPE_REQUEST -> {
                        switch (msg.getOperation()) {
                            case Message.OP_INDEX -> {
                                this.inform((String) msg.getPayload());
                                this.askIndex();
                                Message response = new Message(Message.TYPE_RESPONSE, null, msg.getPayload());
                                sendRequest(response);
                            }
                            case Message.OP_COORDINATE -> {
                                this.inform((String) msg.getPayload());
                                int[] x = this.askCoordinate();
                                Message response = new Message(Message.TYPE_RESPONSE, null, x);
                                sendRequest(response);
                            }
                            case Message.OP_STRING-> {
                                this.inform((String) msg.getPayload());
                                 String s = this.askString();
                                Message response = new Message(Message.TYPE_RESPONSE, null,s);
                                sendRequest(response);
                            }
                            case Message.OP_ASK -> {
                                boolean x = this.ask((String)msg.getPayload());
                                Message response = new Message(Message.TYPE_RESPONSE, null, x);
                                sendRequest(response);
                            }
                        }
                    }
                    default -> {
                        this.reportError("Unknown message type: " + msg.getMessageType());
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

    private void sendRequest(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    /// METODI  PER PRINTARE A CLIENT ///


    @Override
    public void inform(String message){
        view.inform(message);
    }

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, int position, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        view.updateView(nickname , firePower , powerEngine , credits , position , purpleAline , brownAlien , numberOfHuman , numberOfEnergy);
    }

    @Override
    public void reportError(String error){
        view.reportError(error);
    }
    @Override
    public void printListOfTileCovered(List<Tile> tiles) {
        view.printPileCovered();
    }
    @Override
    public void printListOfTileShown(List<Tile> tiles)  {
        view.printPileShown(tiles);
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
    public void printTile(Tile tile){
        view.printTile(tile);
    }
    @Override
    public void printPlayerDashboard(Tile[][] dashBoard){
        view.printDashShip(dashBoard);
    }
    @Override
    public void printDeck(List<Card> deck){
        view.printDeck(deck);
    }

    /// METODI PER CHIEDERE AL CLIENT DA PARTE DEL SERVER ///

    @Override
    public boolean ask(String message){
        return view.ask(message);
    }
    @Override
    public int askIndex(){
        return view.askIndex();
    }
    @Override
    public int[] askCoordinate() {
        return view.askCordinate();
    }
    @Override
    public String askString(){
        return view.askString();
    }


    /// METODI PER SETTARE COSA AL CLIENT ///

    @Override
    public void startMach() {
    }
    @Override
    public void updateGameState(GameFase fase) throws RemoteException {
        this.gameFase = fase;
        view.updateState(gameFase);
    }
    @Override
    public GameFase getCurrentGameState() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GAME_PHASE,null);
        sendRequest(request);
        Object response = waitForResponce();
        return (GameFase) response;
    }
    @Override
    public GameFase getGameFase(){
        return gameFase;
    }

    /// METODI PER IL LOGIN ///

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        Message loginRequest = Message.request(Message.OP_LOGIN, new LoginRequest(username, password));
        sendRequest(loginRequest);
        return Boolean.parseBoolean((String) waitForResponce());

    }
    @Override
    public boolean sendGameRequest(String message) throws IOException {
        Message gameRequest = Message.request(Message.OP_LOGIN, message);
        sendRequest(gameRequest);
        return true;
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

    /// METODI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public Tile getTileServer() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GET_TILE, null);
        sendRequest(request);
        Object response = waitForResponce();
        return (Tile) response;
    }

    @Override
    public Tile getUncoveredTile() throws Exception {
        Message request = Message.request(Message.OP_GET_UNCOVERED, null);
        sendRequest(request);
        Object response = waitForResponce();
        return (Tile) response;
    }

    @Override
    public void getBackTile(Tile tile) throws Exception {
        Message request =  Message.request(Message.OP_RETURN_TILE , tile);
        sendRequest(request);
    }
    @Override
    public void positionTile(Tile tile) throws Exception {
        Message request = Message.request(Message.OP_POSITION_TILE , tile);
        sendRequest(request);
    }

    @Override
    public void drawCard() throws Exception {
        Message request = Message.request(Message.OP_GET_CARD, null);
        sendRequest(request);
    }

    @Override
    public void rotateGlass() throws Exception {
        Message request = Message.request(Message.OP_ROTATE_GLASS , null);
        sendRequest(request);
    }
    @Override
    public void setReady() throws Exception {
        Message request = Message.request(Message.OP_SET_READY, null);
        sendRequest(request);
    }

    @Override
    public void lookDeck() throws Exception {
        Message request = Message.request(Message.OP_LOOK_DECK, null);
        sendRequest(request);
    }

    @Override
    public void lookDashBoard() throws Exception {
        Message request = Message.request(Message.OP_LOOK_SHIP, null);
        sendRequest(request);
    }

    @Override
    public void logOut() throws Exception {
        Message request = Message.request(Message.OP_LOGOUT, null);
        sendRequest(request);
    }
    @Override
    public void setNickname(String nickname) throws Exception {
        this.nickname = nickname;
    }
}

