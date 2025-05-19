package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
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
    private final java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);

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
                    case Message.TYPE_RESPONSE -> responseHandler.handleResponse(msg.getRequestId(), msg);
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
                    int index = this.askIndex();
                    Message response = Message.response(index, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_COORDINATE -> {
                    this.inform((String) msg.getPayload());
                    int[] coordinate = this.askCoordinate();
                    Message response = Message.response(coordinate, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_STRING-> {
                    this.inform((String) msg.getPayload());
                    String answer = this.askString();
                    Message response = Message.response(answer, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_ASK -> {
                    boolean decision = this.ask((String)msg.getPayload());
                    Message response = Message.response(decision, msg.getRequestId());
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
            case Message.OP_SET_FLAG_START -> this.setStart();
            default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
        }
    }


    private void sendRequest(Message message) throws IOException {
        responseHandler.expect(message.getRequestId());
        out.writeObject(message);
        out.flush();
    }

    public Message sendRequestWithResponse(Message msg) throws IOException, InterruptedException {
        responseHandler.expect(msg.getRequestId());
        sendRequest(msg);
        return responseHandler.waitForResponse(msg.getRequestId());
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
    public void updateGameState(GamePhase phase){
        this.gamePhase = phase;
        view.updateState(gamePhase);
    }

    @Override
    public GamePhase getCurrentGameState() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GAME_PHASE,null);
        Message msg = sendRequestWithResponse(request);
        return (GamePhase) msg.getPayload();
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
                    case TUIView v -> {
                        boolean demo = v.ask("Would you like a demo version?");
                        v.inform("Select a number of players between 2 and 4");
                        int numberOfPlayer;
                        while (true) {
                            numberOfPlayer = v.askIndex() + 1;
                            if (numberOfPlayer >= 2 && numberOfPlayer <= 4) {
                                break;
                            }
                            v.reportError("Invalid number of players. Please enter a value between 2 and 4.");
                        }
                        // una volta uscito dal loop, invii la richiesta
                        List<Object> payloadGame = new ArrayList<>();
                        payloadGame.add(demo);
                        payloadGame.add(nickname);
                        payloadGame.add(numberOfPlayer);
                        Message createGame = Message.request(Message.OP_CREATE_GAME, payloadGame);
                        Message msg = sendRequestWithResponse(createGame);
                        return (int) msg.getPayload();
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
                        Message msg = sendRequestWithResponse(createGame);
                        return ((int) msg.getPayload());
                    }
                    default -> {}
                }
            }
        }
        if(message.equals("JOIN")) {
            while (true) {
                Message gameRequest = Message.request(Message.OP_LIST_GAMES, message);
                Message msg = sendRequestWithResponse(gameRequest);
                view.inform("Available Games");
                Map<Integer, int[]> availableGames = (Map<Integer, int[]>) msg.getPayload();
                if(availableGames.isEmpty()){
                    view.inform("No available games");
                    return -1;
                }else{
                    for (Integer i : availableGames.keySet()) {
                        int[] info = availableGames.get(i);
                        boolean isDemo = info[2] == 1;
                        String suffix = isDemo ? " DEMO" : "";
                        view.inform(i + ". Players in game : " + info[0] + "/" + info[1] + suffix);
                    }
                }

                int choice = askIndex() + 1;
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
        Message msg = sendRequestWithResponse(Message.request(Message.OP_LOGIN, username));
        Integer resp = (Integer) msg.getPayload();
        try {
            return resp;
        } catch (ClassCastException e) {
            throw new IOException("Login: unexpected payload from server: " + resp.getClass().getName(), e);
        }
    }




    /// METODI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public Tile getTileServer() throws IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_TILE, payloadGame);
        Message msg = sendRequestWithResponse(request);



        return switch (msg.getPayload()) {
            case Tile t -> t;
            case String error -> throw new IOException("Error: " + error);
            default -> throw new IOException("Unexpected payload: " + msg.getPayload().getClass().getName());
        };
    }


    @Override
    public Tile getUncoveredTile() throws Exception {

        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message listRequest = Message.request(Message.OP_GET_UNCOVERED_LIST, payloadGame);
        Message listResponse = sendRequestWithResponse(listRequest);

        List<Tile> listTile = switch (listResponse.getPayload()) {
            case List<?> rawList -> {
                try {
                    @SuppressWarnings("unchecked")
                    List<Tile> casted = (List<Tile>) rawList;
                    yield casted;
                } catch (ClassCastException e) {
                    throw new IOException("Error : " + e.getMessage(), e);
                }
            }
            case String error -> throw new IOException("Error from server: " + error);
            default -> throw new IOException("Unexpected: " + listResponse.getPayload().getClass().getName());
        };

        if (listTile.isEmpty()) {
            throw new IOException("Empty list");
        }

        view.printPileShown(listTile);
        while (true) {
            int index = askIndex();
            if (index < 0 || index >= listTile.size()) {
                view.reportError("Invalid index: ");
                continue;
            }

            Tile tile = listTile.get(index);

            // Richiesta della tessera selezionata
            Message request = Message.request(Message.OP_GET_UNCOVERED, tile);
            Message tileResponse = sendRequestWithResponse(request);

            return switch (tileResponse.getPayload()) {
                case Tile t -> t;
                case String error -> throw new IOException("Error from server: " + error);
                default -> throw new IOException("Unexpected payload: " + tileResponse.getPayload().getClass().getName());
            };
        }
    }


    @Override
    public void getBackTile(Tile tile) throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        payloadGame.add(tile);
        Message request = Message.request(Message.OP_RETURN_TILE , payloadGame);
        Message response = sendRequestWithResponse(request);

        Object payload = response.getPayload();
        switch (payload) {
            case String p when p.equals("OK") -> {
            }
            case String error -> {
                throw new IOException("Error from server: " + error);
            }
            default -> throw new IOException("Unexpected payload: " );
        }

    }

    @Override
    public void positionTile(Tile tile) throws Exception {
        view.printDashShip(Dash_Matrix);
        int[] tmp;

        while (true) {
            view.inform("Choose coordiante");
            tmp = view.askCoordinate();

            List<Object> payloadGame = new ArrayList<>();
            payloadGame.add(gameId);
            payloadGame.add(nickname);
            payloadGame.add(tile);
            payloadGame.add(tmp);

            Message request = Message.request(Message.OP_POSITION_TILE, payloadGame);
            Message response = sendRequestWithResponse(request);

            Object payload = response.getPayload();

            switch (payload) {
                case String p when p.equals("OK") -> {}
                case String error -> {
                    view.reportError("Errorr from server: " + error);
                    continue;
                }
                default -> throw new IOException("Unexptected payload: ");
            }
            break;
        }
        Dash_Matrix[tmp[0]][tmp[1]] = tile;
        view.printDashShip(Dash_Matrix);
    }

    @Override
    public void drawCard() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_CARD, payloadGame);
        Message response = sendRequestWithResponse(request);
        Object payload = response.getPayload();
        switch (payload) {
            case String p when p.equals("OK") -> {
            }
            case String error -> throw new IOException("Error from server: " + error);

            default -> throw new IOException("Unexpected payload: " );
        }
    }

    @Override
    public void rotateGlass() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);

        Message request = Message.request(Message.OP_ROTATE_GLASS, payloadGame);
        Message response = sendRequestWithResponse(request);

        Object payload = response.getPayload();

        switch (payload) {
            case String p when p.equals("OK") -> {
            }
            case String error -> {
                throw new IOException("Error from server: " + error);
            }
            default -> throw new IOException("Unexpected payload: " );
        }
    }

    @Override
    public void setReady() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_SET_READY, payloadGame);
        Message response = sendRequestWithResponse(request);
        Object payload = response.getPayload();
        switch (payload) {
            case String p when p.equals("OK") -> {
            }
            case String error -> {
                throw new IOException("Error from server: " + error);
            }
            default -> throw new IOException("Unexpected payload: " );
        }
    }

    @Override
    public void lookDeck() throws Exception {
        while (true) {
            view.inform("Choose deck: 1 / 2 / 3");
            int index = askIndex();

            List<Object> payload = new ArrayList<>();
            payload.add(gameId);
            payload.add(index);

            Message request = Message.request(Message.OP_LOOK_DECK, payload);
            Message response = sendRequestWithResponse(request);

            Object payloadResponse = response.getPayload();

            switch (payloadResponse) {
                case List<?> rawList -> {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Card> deck = (List<Card>) rawList;
                        view.printDeck(deck);
                        return;
                    } catch (ClassCastException e) {
                        view.reportError("Error: " + e.getMessage());
                        return;
                    }
                }
                case String error -> {
                    view.reportError("Invalid index: " + error);
                }
                default -> {
                    view.reportError("Unexpected value: " );
                    return;
                }
            }
        }
    }



    @Override
    public void lookDashBoard() throws Exception {
        while (true) {
            String tmp = view.choosePlayer();

            List<Object> payload = new ArrayList<>();
            payload.add(gameId);
            payload.add(tmp);

            Message request = Message.request(Message.OP_LOOK_SHIP, payload);
            Message response = sendRequestWithResponse(request);
            Object payloadResponse = response.getPayload();

            switch (payloadResponse) {
                case Tile[][] dashPlayer -> {
                    view.inform("Space Ship di: " + tmp);
                    view.printDashShip(dashPlayer);
                    view.printListOfCommand();

                }
                case String error -> {
                    view.reportError("Invalid player name: " + error);
                }
                default -> {
                    view.reportError("Unexpected payload type: " + payloadResponse.getClass().getName());

                }
            }
        }
    }




    @Override
    public void logOut() throws Exception {
        if(gameId != 0) {
            List<Object> payloadGame = new ArrayList<>();
            payloadGame.add(gameId);
            payloadGame.add(nickname);
            Message request = Message.request(Message.OP_LEAVE_GAME, payloadGame);
            Message response = sendRequestWithResponse(request);
            Object payload = response.getPayload();
            switch (payload) {
                case String p when p.equals("OK") -> {
                }
                case String error -> {
                    throw new IOException("Error from server: " + error);
                }
                default -> throw new IOException("Unexpected payload: " );
            }
        }else{
            List<Object> payloadGame = new ArrayList<>();
            payloadGame.add(nickname);
            Message request = Message.request(Message.OP_LOGOUT, payloadGame);
            Message response = sendRequestWithResponse(request);
            Object payload = response.getPayload();
            switch (payload) {
                case String p when p.equals("OK") -> {
                }
                case String error -> {
                    throw new IOException("Error from server: " + error);
                }
                default -> throw new IOException("Unexpected payload: " );
            }
        }
        active = false;
        socket.close();
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
    public void setStart() {
        start = "start";
        startLatch.countDown();
    }


    @Override
    public String askInformationAboutStart() {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Interrupted";
        }
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

        List<Object> payload = new ArrayList<>();
        payload.add(gameId);
        payload.add(nickname);
        Message request = Message.request(Message.OP_ENTER_GAME, payload);
        Message response = sendRequestWithResponse(request);
        Object payloadResponse = response.getPayload();
        switch (payloadResponse) {
            case String p when p.equals("OK") -> {
            }
            case String error -> {
                throw new IOException("Error from server: " + error);
            }
            default -> throw new IOException("Unexpected payload: " );
        }

    }

}

