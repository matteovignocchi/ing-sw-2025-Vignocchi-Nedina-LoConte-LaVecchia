package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualClientSocket implements Runnable, VirtualView {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private  View view;
    private GamePhase gamePhase;
    private int gameId = 0;
    private String nickname;
    private Tile[][] Dash_Matrix;
    private boolean active = true;
    private String start = "false";
    private final ResponseHandler responseHandler = new ResponseHandler();

    /// METODI DI INIZIALIZZAZIONE ///

    public VirtualClientSocket(String host, int port , View view) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        this.view = view;
        new Thread(this).start();
        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
    }

    @Override
    public void run() {
        while (active) {
            try {
                Message msg = (Message) in.readObject();

                switch (msg.getMessageType()) {
                    case Message.TYPE_NOTIFICATION -> this.inform((String) msg.getPayload());
                    case Message.TYPE_REQUEST -> handleRequest(msg);
                    case Message.TYPE_RESPONSE -> responseHandler.handleResponse(msg);
                    case Message.TYPE_UPDATE -> handleUpdate(msg);
                    case Message.TYPE_ERROR -> this.reportError((String) msg.getPayload());
                    default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
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
    }

    public void handleRequest(Message msg){
        String operation = msg.getOperation();
        try{
            switch (operation) {
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
    } catch (IOException e) {
            this.reportError(": " + e.getMessage());
        }
    }



    private void handleUpdate(Message msg) throws Exception {
        switch (msg.getOperation()) {
            case Message.OP_GAME_PHASE -> this.updateGameState((GamePhase) msg.getPayload());
            case Message.OP_PRINT_CARD -> this.printCard((Card) msg.getPayload());
            case Message.OP_PRINT_COVERED -> this.printListOfTileCovered((List<Tile>) msg.getPayload());
            case Message.OP_PRINT_SHOWN -> this.printListOfTileShown((List<Tile>) msg.getPayload());
            case Message.OP_PRINT_GOODS -> this.printListOfGoods((List<Colour>) msg.getPayload());
            case Message.OP_PRINT_DASHBOARD -> this.printPlayerDashboard((Tile[][]) msg.getPayload());
            case Message.OP_PRINT_DECK -> this.printDeck((List<Card>) msg.getPayload());
            case Message.OP_PRINT_TILE -> this.printTile((Tile) msg.getPayload());
            case Message.OP_SET_NICKNAME -> this.setNickname((String) msg.getPayload());
            case Message.OP_SET_VIEW -> this.setView((View) msg.getPayload());
            case Message.OP_SET_GAMEID -> this.setGameId((int) msg.getPayload());
            case Message.OP_MAP_POSITION -> this.updateMapPosition((Map<String, Integer>) msg.getPayload());
            case Message.OP_SET_IS_DEMO -> this.setIsDemo((Boolean) msg.getPayload());
            case Message.OP_SET_CENTRAL_TILE -> this.setCentralTile((Tile) msg.getPayload());
            case Message.OP_UPDATE_VIEW -> {
                UpdateViewRequest payload = (UpdateViewRequest) msg.getPayload();
                try {
                    this.showUpdate(
                            payload.getNickname(),
                            payload.getFirePower(),
                            payload.getPowerEngine(),
                            payload.getCredits(),
                            payload.hasPurpleAlien(),
                            payload.hasBrownAlien(),
                            payload.getNumberOfHuman(),
                            payload.getNumberOfEnergy()
                    );
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
            case Message.OP_SET_FLAG_START -> {
                try {
                    this.setStart();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
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
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        view.updateView(nickname , firePower , powerEngine , credits , /*position ,*/ purpleAline , brownAlien , numberOfHuman , numberOfEnergy);
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
    public void printListOfGoods(List<Colour> listOfGoods) {
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

    @Override
    public void setView(View view)  {
        this.view=view;
    }

    @Override
    public void setGameId(int gameId) {
        this.gameId=gameId;
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
        return view.askCoordinate();
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
    public void updateGameState(GamePhase fase){
        this.gamePhase = fase;
        view.updateState(gamePhase);
    }

    @Override
    public GamePhase getCurrentGameState() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GAME_PHASE,null);
        sendRequest(request);
        Object response =  responseHandler.waitForResponse();
        return (GamePhase) response;
    }
    @Override
    public GamePhase getGameFase(){
        return gamePhase;
    }
    /// METODI PER IL LOGIN ///


    @Override
    public int sendGameRequest(String message) throws IOException, InterruptedException {
        if(message.equals("CREATE")){
            while (true) {
                switch (view){
                    case TUIView v ->{
                        boolean demo = v.ask("would you like a demo version?");
                        int numberOfPlayer;
                        do {
                            v.inform("select max 4 players");
                            numberOfPlayer = v.askIndex();
                        } while (numberOfPlayer > 4 || numberOfPlayer < 2);
                        List<Object> payloadGame = new ArrayList<>();
                        payloadGame.add(demo);
                        payloadGame.add(nickname);
                        payloadGame.add(numberOfPlayer);
                        Message createGame = Message.request(Message.OP_CREATE_GAME, payloadGame);
                        sendRequest(createGame);
                        return ((int) responseHandler.waitForResponse());
                    }
                    case GUIView v ->{
                        List<Object> data = v.getDataForGame();
                        boolean demo = (boolean) data.get(0);
                        int numberOfPlayer = (int) data.get(1);
                        List<Object> payloadGame = new ArrayList<>();
                        payloadGame.add(demo);
                        payloadGame.add(nickname);
                        payloadGame.add(numberOfPlayer);
                        Message createGame = Message.request(Message.OP_CREATE_GAME, payloadGame);
                        sendRequest(createGame);
                        return ((int) responseHandler.waitForResponse());
                    }
                    default -> {}
                }
            }
        }
        if(message.equals("JOIN")) {
            while (true) {
                Message gameRequest = Message.request(Message.OP_LIST_GAMES, message);
                sendRequest(gameRequest);
                view.inform("Available Games");
                Map<Integer, int[]> availableGames;
                availableGames = (Map<Integer, int[]>) responseHandler.waitForResponse();
                for (Integer i : availableGames.keySet()) {
                    if(availableGames.get(i)[2] == 1){
                        view.inform(i+". Players in game : "+availableGames.get(i)[0]+"/"+availableGames.get(i)[1] + " DEMO");
                    }
                    view.inform(i+". Players in game : "+availableGames.get(i)[0]+"/"+availableGames.get(i)[1]);
                }
                Integer choice = askIndex();
                List<Object> payloadJoin = List.of(choice, nickname);
                Message gameChoice = Message.request(Message.OP_ENTER_GAME, payloadJoin);
                sendRequest(gameChoice);
                return choice;
            }

        }

        return 0;
    }


    @Override
    public int sendLogin(String username) throws IOException, InterruptedException {
        Message loginRequest = Message.request(Message.OP_LOGIN, username);
        sendRequest(loginRequest);
        String answer = (String) responseHandler.waitForResponse();
        //TODO: sistemare questo
        //return "OK".equals(answer);
        return 0;
    }



    /// METODI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public Tile getTileServer() throws IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_TILE, payloadGame);
        sendRequest(request);
        Object response =  responseHandler.waitForResponse();
        return (Tile) response;
    }

    @Override
    public Tile getUncoveredTile() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message listRequest = Message.request(Message.OP_GET_UNCOVERED_LIST, payloadGame);
        sendRequest(listRequest);
        List<Tile> listTile = (List<Tile>) responseHandler.waitForResponse();
        view.printPileShown(listTile);
        int index = askIndex();
        Tile tile = listTile.get(index);
        Message request = Message.request(Message.OP_GET_UNCOVERED, tile);
        sendRequest(request);
        Object response =  responseHandler.waitForResponse();
        return (Tile) response;
    }

    @Override
    public void getBackTile(Tile tile) throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        payloadGame.add(tile);
        Message request =  Message.request(Message.OP_RETURN_TILE , payloadGame);
        sendRequest(request);
    }
    @Override
    public void positionTile(Tile tile) throws Exception {
        view.inform("choose coordinate");
        int[] tmp = view.askCoordinate();
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        payloadGame.add(tile);
        payloadGame.add(tmp);
        Message request = Message.request(Message.OP_POSITION_TILE , payloadGame);
        sendRequest(request);
    }

    @Override
    public void drawCard() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_CARD, payloadGame);
        sendRequest(request);
    }

    @Override
    public void rotateGlass() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_ROTATE_GLASS , payloadGame);
        sendRequest(request);
    }
    @Override
    public void setReady() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_SET_READY, payloadGame);
        sendRequest(request);
    }

    @Override
    public void lookDeck() throws Exception {
        view.inform("choose deck : 1 / 2 / 3");
        int index = askIndex();
        Message request = Message.request(Message.OP_LOOK_DECK, index);
        sendRequest(request);
    }

    @Override
    public void lookDashBoard() throws Exception {
        String player = view.choosePlayer();
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(player);
        Message request = Message.request(Message.OP_LOOK_SHIP, payloadGame);
        sendRequest(request);
    }

    @Override
    public void logOut() throws Exception {
        if(gameId != 0) {
            List<Object> payloadGame = new ArrayList<>();
            payloadGame.add(gameId);
            payloadGame.add(nickname);
            Message request = Message.request(Message.OP_LEAVE_GAME, payloadGame);
            sendRequest(request);
        }else{
            List<Object> payloadGame = new ArrayList<>();
            payloadGame.add(nickname);
            Message request = Message.request(Message.OP_LOGOUT, payloadGame);
            sendRequest(request);
        }

    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void updateMapPosition(Map<String, Integer> Position)  {
        view.updateMap(Position);
    }

    @Override
    public void setStart(){
        start = "Start";
    }

    @Override
    public String askInformationAboutStart() {
        return start;
    }

    @Override
    public void setCentralTile(Tile tile) throws Exception {
        Dash_Matrix[2][3] = tile;

    }

    @Override
    public void setIsDemo(Boolean demo) {
        view.setIsDemo(demo);
    }

    @Override
    public void enterGame(int gameId) throws Exception {

    }

}

