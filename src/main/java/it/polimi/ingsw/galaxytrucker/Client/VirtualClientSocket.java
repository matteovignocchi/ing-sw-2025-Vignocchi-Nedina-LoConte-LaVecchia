package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class VirtualClientSocket implements Runnable, VirtualView {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private int gameId = 0;
    private String nickname;
    private boolean active = true;
    private String start = "false";
    private final ResponseHandler responseHandler = new ResponseHandler();
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private ClientController ciccio;


    /// METODI DI INIZIALIZZAZIONE ///

    public VirtualClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (active) {
            try {
                Message msg = (Message) in.readObject();

                switch (msg.getMessageType()) {
                    case Message.TYPE_NOTIFICATION ->{
                        String note = (String) msg.getPayload();
                        this.inform(note);
                        if (note.contains("has abandoned")) {
                            updateGameState(GamePhase.EXIT);
                        }
                    }
                    case Message.TYPE_REQUEST -> handleRequest(msg);
                    case Message.TYPE_RESPONSE -> responseHandler.handleResponse(msg.getRequestId(), msg);
                    case Message.TYPE_UPDATE -> handleUpdate(msg);
                    case Message.TYPE_ERROR -> this.reportError((String) msg.getPayload());
                    default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
                }

            } catch (IOException | ClassNotFoundException e) {
                if (active) {
                    try {
                        ciccio.reportErrorByController("Connection error: " + e.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
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
//            case Message.OP_SET_VIEW -> this.setView((View) msg.getPayload());
            case Message.OP_SET_GAMEID -> this.setGameId((int) msg.getPayload());
            case Message.OP_MAP_POSITION -> this.updateMapPosition((Map<String, Integer>) msg.getPayload());
            case Message.OP_SET_IS_DEMO -> this.setIsDemo((boolean) msg.getPayload());
            case Message.OP_SET_CENTRAL_TILE -> this.setTile((Tile) msg.getPayload());
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
            case Message.OP_UPDATE_DA -> this.updateDashMatrix((Tile[][]) msg.getPayload());
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
        System.out.print("\n");
        ciccio.informByController(message);
    }

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        ciccio.showUpdateByController(nickname , firePower , powerEngine , credits , /*position ,*/ purpleAline , brownAlien , numberOfHuman , numberOfEnergy);
    }

    @Override
    public void reportError(String error){
        ciccio.reportErrorByController(error);
    }
    @Override
    public void printListOfTileCovered(List<Tile> tiles) {
        ciccio.printListOfTileCoveredByController();
    }
    @Override
    public void printListOfTileShown(List<Tile> tiles)  {
        ciccio.printListOfTileShownByController(tiles);
    }
    @Override
    public void printListOfGoods(List<Colour> listOfGoods) {
        ciccio.printListOfGoodsByController(listOfGoods);
    }
    @Override
    public void printCard(Card card){
        ciccio.printCardByController(card);
    }
    @Override
    public void printTile(Tile tile){
        ciccio.printTileByController(tile);
    }
    @Override
    public void printPlayerDashboard(Tile[][] dashBoard){
        ciccio.printPlayerDashboardByController(dashBoard);
    }
    @Override
    public void printDeck(List<Card> deck){
        ciccio.printDeckByController(deck);
    }

//    @Override
//    public void setView(View view)  {
////        this.view=view;
//    }

    @Override
    public void setGameId(int gameId) {
        this.gameId=gameId;
    }

    /// METODI PER CHIEDERE AL CLIENT DA PARTE DEL SERVER ///

    @Override
    public boolean ask(String message){
        return ciccio.askByController(message);
    }
    @Override
    public int askIndex(){
        return ciccio.askIndexByController();
    }
    @Override
    public int[] askCoordinate() {
        return ciccio.askCoordinateByController();
    }
    @Override
    public String askString(){
        return ciccio.askStringByController();
    }


    /// METODI PER SETTARE COSA AL CLIENT ///

    @Override
    public void startMach() {
    }

    @Override
    public void updateGameState(GamePhase phase){
        ciccio.updateGameStateByController(phase);
    }

    @Override
    public GamePhase getCurrentGameState() throws IOException, InterruptedException {
        Message request = Message.request(Message.OP_GAME_PHASE,null);
        Message msg = sendRequestWithResponse(request);
        return (GamePhase) msg.getPayload();
    }

    @Override
    public GamePhase getGameFase(){
        return null;
    }
    /// METODI PER IL LOGIN ///


    @Override
    public int sendGameRequest(String message , int numPlayer , Boolean isDemo2) throws IOException, InterruptedException {
        if(message.equals("CREATE")){
                        List<Object> payloadGame = new ArrayList<>();
                        payloadGame.add(isDemo2);
                        payloadGame.add(nickname);
                        payloadGame.add(numPlayer);
                        Message createGame = Message.request(Message.OP_CREATE_GAME, payloadGame);
                        Message msg = sendRequestWithResponse(createGame);
                        return (int) msg.getPayload();
        }
        if(message.equals("JOIN")) {
            while (true) {
                        Message gameRequest = Message.request(Message.OP_LIST_GAMES, message);
                        Message msg = sendRequestWithResponse(gameRequest);
                        @SuppressWarnings("unchecked")
                        Map<Integer, int[]> availableGames = (Map<Integer, int[]>) msg.getPayload();

                        if (availableGames.isEmpty()) {
                            ciccio.informByController("**No available games**");
                            return -1;
                        }

                        int choice = ciccio.printAvailableGames(availableGames);
                        if (choice == 0) return 0;
                        if (availableGames.containsKey(choice)) {
                            List<Object> payloadJoin = List.of(choice, nickname);
                            Message gameChoice = Message.request(Message.OP_ENTER_GAME, payloadJoin);
                            sendRequest(gameChoice);
                            return choice;
                        }
                }

            }
        return 0;
    }


    @Override
    public int sendLogin(String username) throws IOException, InterruptedException {
        if(username == null || username.trim().isEmpty()){
           throw new IllegalArgumentException("Username cannot be null or empty");
        }
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
    public Tile getUncoveredTile() throws IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);

        Message listRequest = Message.request(Message.OP_GET_UNCOVERED_LIST, payloadGame);
        Message listResponse = sendRequestWithResponse(listRequest);
        Object listPayload = listResponse.getPayload();

        List<Tile> tmp = List.of();

        try {
            @SuppressWarnings("unchecked")
            List<Tile> casted = (List<Tile>) listPayload;
            tmp = casted;
        } catch (ClassCastException e) {
            switch (listPayload) {
                case String error:
                    throw new IOException("Server error: " + error);
                default:
                    new IOException("Unexpected payload type while reading tile list.", e);
                    break;
            }
        }
        if (tmp.isEmpty()) {
            throw new IOException("The list of shown tiles is empty.");
        }


        ciccio.printListOfTileShownByController(tmp);
        ciccio.informByController("Select a tile");

        while (true) {
            int index;
            while (true) {
                index = askIndex();
                if (index >= 0 && index < tmp.size()) break;
                ciccio.informByController("Invalid index. Try again.");
            }

            List<Object> tileRequestPayload = new ArrayList<>();
            tileRequestPayload.add(gameId);
            tileRequestPayload.add(nickname);


            Message tileRequest = Message.request(Message.OP_GET_UNCOVERED, tileRequestPayload);
            Message tileResponse = sendRequestWithResponse(tileRequest);
            Object tilePayload = tileResponse.getPayload();

            try {
                return (Tile) tilePayload;
            } catch (ClassCastException e) {
                switch (tilePayload){
                    case String error: throw new IOException("Server error: " + error);
                    default:  throw new IOException("Unexpected payload type when fetching tile.", e);
                }
            }
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
        ciccio.printMyDashBoardByController();
        int[] tmp;

        while (true) {
            ciccio.informByController("Choose coordiante");
            tmp = ciccio.askCoordinateByController();

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
                    ciccio.reportErrorByController("Errorr from server: " + error);
                    continue;
                }
                default -> throw new IOException("Unexptected payload: ");
            }
            break;
        }
        ciccio.setTileInMatrix(tile , tmp[0] ,  tmp[1]);
        ciccio.printMyDashBoardByController();
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
            ciccio.informByController("Choose deck : 1 / 2 / 3");
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
                        ciccio.printDeckByController(deck);
                        return;
                    } catch (ClassCastException e) {
                        ciccio.reportErrorByController("Error: " + e.getMessage());
                        return;
                    }
                }
                case String error -> {
                    ciccio.reportErrorByController("Invalid index: " + error);
                }
                default -> {
                    ciccio.reportErrorByController("Unexpected value: " );
                    return;
                }
            }
        }
    }



    @Override
    public void lookDashBoard() throws Exception {
        while (true) {
            String tmp = ciccio.choocePlayerByController();


            List<Object> payload = new ArrayList<>();
            payload.add(gameId);
            payload.add(tmp);

            Message request = Message.request(Message.OP_LOOK_SHIP, payload);
            Message response = sendRequestWithResponse(request);
            Object payloadResponse = response.getPayload();

            switch (payloadResponse) {
                case Tile[][] dashPlayer -> {
                    ciccio.informByController("Space Ship di: " + tmp);
                    ciccio.printPlayerDashboardByController(dashPlayer);
                    ciccio.printListOfCommands();
                    return;
                }
                case String error -> {
                    ciccio.reportErrorByController("Invalid player name: " + error);
                }
                default -> {
                    ciccio.reportErrorByController("Unexpected payload type: " + payloadResponse.getClass().getName());

                }
            }
        }
    }

    @Override
    public void leaveGame() throws Exception {
        if (gameId != 0) {
            Message req = Message.request(Message.OP_LEAVE_GAME, List.of(gameId, nickname));
            Message resp = sendRequestWithResponse(req);
            if (!"OK".equals(resp.getPayload())) {
                throw new IOException("Error from server: " + resp.getPayload());
            }
            gameId = 0;
        }
    }

    @Override
    public void logOut() throws Exception {
        Message req = Message.request(Message.OP_LOGOUT, nickname);
        Message resp = sendRequestWithResponse(req);
        if (!"OK".equals(resp.getPayload())) {
            throw new IOException("Error from server: " + resp.getPayload());
        }
        active = false;
        socket.close();
        System.out.println("Goodbye!");
        System.exit(0);
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void updateMapPosition(Map<String, Integer> Position)  {
        ciccio.updateMapPositionByController(Position);
    }

    @Override
    public void setStart() {
        start = "start";
        startLatch.countDown();
    }

    @Override
    public void setClientController(ClientController clientController) {
        this.ciccio = clientController;
    }



    @Override
    public String askInformationAboutStart() throws Exception {
        startLatch.await();
        return "start";
    }


    @Override
    public void setTile(Tile tile) throws Exception {
        ciccio.setCurrentTile(tile);
    }

    @Override
    public void setIsDemo(Boolean demo) {
        ciccio.setIsDemoByController(demo);
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

    @Override
    public Tile takeReservedTile() throws IOException, BusinessLogicException, InterruptedException {
        if(!ciccio.returOKAY(0,5) && !ciccio.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        ciccio.printMyDashBoardByController();
        ciccio.informByController("Select a tile");
        int[] index;
        Tile tmpTile = null;
        while(true) {
            index = askCoordinate();
            if(index[0]!=0 || !ciccio.returOKAY(0 , index[1])) ciccio.informByController("Invalid coordinate");
            else if(index[1]!=5 && index[1]!=6) ciccio.informByController("Invalid coordinate");
            else break;
        }
        List<Object> payload = new ArrayList<>();
        payload.add(gameId);
        payload.add(nickname);
        Tile tmp = ciccio.getSomeTile(index[0], index[1]);
        payload.add(tmp.idTile);
        ciccio.setTileInMatrix(new EmptySpace(), index[0], index[1]);
        ciccio.printMyDashBoardByController();
        Message request = Message.request(Message.OP_GET_RESERVED_TILE, payload);
        Message response = sendRequestWithResponse(request);
        Object payloadResponse = response.getPayload();
        try {
            return (Tile) payloadResponse;
        } catch (ClassCastException e) {
            switch (payloadResponse){
                case String error: throw new IOException("Server error: " + error);
                default:  throw new IOException("Unexpected payload type when fetching tile.", e);
            }
        }
    }

    @Override
    public void updateDashMatrix(Tile[][] data) throws IOException, BusinessLogicException, InterruptedException {
        ciccio.newShip(data);
    }
}

