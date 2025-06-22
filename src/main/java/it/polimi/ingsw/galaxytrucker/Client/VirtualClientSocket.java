package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
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
    private ClientController clientController;


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
                            updateGameState("EXIT"  );
                        }
                    }
                    case Message.TYPE_REQUEST -> handleRequest(msg);
                    case Message.TYPE_RESPONSE -> {
                        if (responseHandler.hasPending(msg.getRequestId())) {
                            responseHandler.handleResponse(msg.getRequestId(), msg);
                        }
                    }
                    case Message.TYPE_UPDATE -> handleUpdate(msg);
                    case Message.TYPE_ERROR -> this.reportError((String) msg.getPayload());
                    default -> throw new IllegalStateException("Unexpected value: " + msg.getOperation());
                }

            } catch (IOException | ClassNotFoundException e) {
                if (active) {
                    try {
                        clientController.reportErrorByController("Connection error: " + e.getMessage());
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
                    Integer index = this.askIndex();
                    if(index == null) { return; }
                    Message response = Message.response(index, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_COORDINATE -> {
                    this.inform((String) msg.getPayload());
                    int[] coordinate = this.askCoordinate();
                    if (coordinate == null) { return; }
                    Message response = Message.response(coordinate, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_STRING-> {
                    this.inform((String) msg.getPayload());
                    String answer = this.askString();
                    if(answer == null) { return; }
                    Message response = Message.response(answer, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_ASK -> {
                    Boolean decision = this.ask((String)msg.getPayload());
                    if(decision == null) { return; }
                    Message response = Message.response(decision, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_ASK_TO -> {
                    String prompt = (String) msg.getPayload();
                    if (prompt != null) this.inform(prompt);
                    Boolean decision = this.askWithTimeout(prompt);
                    Message response = Message.response(decision, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_COORDINATE_TO -> {
                    String prompt = (String) msg.getPayload();
                    if (prompt != null) this.inform(prompt);
                    int[] coordinate = this.askCoordsWithTimeout();
                    Message response = Message.response(coordinate, msg.getRequestId());
                    sendRequest(response);
                }
                case Message.OP_INDEX_TO -> {
                    String prompt = (String) msg.getPayload();
                    if (prompt != null) this.inform(prompt);
                    Integer answer = clientController.askIndexWithTimeoutByController();
                    sendRequest(Message.response(answer, msg.getRequestId()));
                }
            }
    } catch (IOException e) {
            this.reportError(": " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void handleUpdate(Message msg) throws Exception {
        switch (msg.getOperation()) {
            case Message.OP_GAME_PHASE -> this.updateGameState((String) msg.getPayload());
            case Message.OP_PRINT_CARD -> this.printCard((String) msg.getPayload());
            case Message.OP_PRINT_COVERED -> this.printListOfTileCovered((String) msg.getPayload());
            case Message.OP_PRINT_SHOWN -> this.printListOfTileShown((String) msg.getPayload());
            case Message.OP_PRINT_GOODS -> this.printListOfGoods((List<String>) msg.getPayload());
            case Message.OP_PRINT_DASHBOARD -> {
                String[][] dash = (String[][]) msg.getPayload();
                clientController.newShip(dash);
                clientController.printPlayerDashboardByController(dash);
            }
            case Message.OP_PRINT_DECK -> this.printDeck((String) msg.getPayload());
            case Message.OP_PRINT_TILE -> this.printTile((String) msg.getPayload());
            case Message.OP_SET_NICKNAME -> this.setNickname((String) msg.getPayload());
//            case Message.OP_SET_VIEW -> this.setView((View) msg.getPayload());
            case Message.OP_SET_GAMEID -> this.setGameId((int) msg.getPayload());
            case Message.OP_MAP_POSITION -> this.updateMapPosition((Map<String, int[] >) msg.getPayload());
            case Message.OP_SET_IS_DEMO -> this.setIsDemo((boolean) msg.getPayload());
            case Message.OP_SET_CENTRAL_TILE -> this.setTile((String) msg.getPayload());
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
            case Message.OP_UPDATE_DA -> this.updateDashMatrix((String[][]) msg.getPayload());
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
        clientController.informByController(message);
    }

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        clientController.showUpdateByController(nickname , firePower , powerEngine , credits , /*position ,*/ purpleAline , brownAlien , numberOfHuman , numberOfEnergy);
    }

    @Override
    public void reportError(String error){
        clientController.reportErrorByController(error);
    }
    @Override
    public void printListOfTileCovered(String tiles) {
        clientController.printListOfTileCoveredByController();
    }
    @Override
    public void printListOfTileShown(String tiles)  {
        clientController.printListOfTileShownByController(tiles);
    }
    @Override
    public void printListOfGoods(List<String> listOfGoods) {
        clientController.printListOfGoodsByController(listOfGoods);
    }
    @Override
    public void printCard(String card){
        clientController.printCardByController(card);
    }
    @Override
    public void printTile(String tile){
        clientController.printTileByController(tile);
    }
    @Override
    public void printPlayerDashboard(String[][] dashBoard){
        clientController.printPlayerDashboardByController(dashBoard);
    }
    @Override
    public void printDeck(String deck){
        clientController.printDeckByController(deck);
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
    public Boolean ask(String message){
        return clientController.askByController(message);
    }
    @Override
    public Integer askIndex() throws IOException, InterruptedException {
        return clientController.askIndexByController();
    }
    @Override
    public int[] askCoordinate() {
        return clientController.askCoordinateByController();
    }
    @Override
    public String askString(){
        return clientController.askStringByController();
    }


    /// METODI PER SETTARE COSA AL CLIENT ///

    @Override
    public void startMach() {
    }

    @Override
    public void updateGameState(String phase){
        if (phase == null || "PING".equalsIgnoreCase(phase)) return;
        clientController.updateGameStateByController(phase);
    }
//TODO:gabri succhia, vedere se va

//    @Override
//    public String getCurrentGameState() throws IOException, InterruptedException {
//        Message request = Message.request(Message.OP_GAME_PHASE,null);
//        Message msg = sendRequestWithResponse(request);
//        return (String) msg.getPayload();
//    }

//    @Override
//    public GamePhase getGameFase(){
//        return null;
//    }
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
                            clientController.informByController("**No available games**");
                            return -1;
                        }

                        int choice = clientController.printAvailableGames(availableGames);
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
    public int sendLogin(String username) throws IOException, InterruptedException, BusinessLogicException{
        if(username == null || username.trim().isEmpty()){
           throw new IllegalArgumentException("Username cannot be null or empty");
        }
        Message msg = sendRequestWithResponse(Message.request(Message.OP_LOGIN, username));
        Object resp = msg.getPayload();
        try {
            return (Integer) resp;
        } catch (ClassCastException e) {
            try {
                String err = (String) resp;
                throw new BusinessLogicException(err);
            } catch (ClassCastException e2) {
                throw new IOException("Login: unexpected payload from server: " + resp.getClass().getName());
            }
        }
    }


    /// METODI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public String getTileServer() throws IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_TILE, payloadGame);
        Message msg = sendRequestWithResponse(request);



        return switch (msg.getPayload()) {
            case String t -> t;
            default -> throw new IOException("Unexpected payload: " + msg.getPayload().getClass().getName());
        };
    }


    @Override
    public String getUncoveredTile() throws BusinessLogicException, IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);

        // Richiesta lista tile scoperte
        Message listRequest = Message.request(Message.OP_GET_UNCOVERED_LIST, payloadGame);
        Message listResponse = sendRequestWithResponse(listRequest);
        Object listPayload = listResponse.getPayload();

        String tmp;
        try {
            switch (listPayload) {
                case String list:
                    tmp = list;
                    break;
                default:
                    throw new IOException("Unexpected payload type while reading tile list.");
            }
        } catch (ClassCastException e) {
            throw new IOException("Payload type mismatch while casting tile list.", e);
        }

        if (tmp == null || tmp.equals("PIEDONIPRADELLA")) {
            throw new BusinessLogicException("The list of shown tiles is empty.");
        }

        int size = clientController.printListOfTileShownByController(tmp);
        clientController.informByController("Select a tile");

        while (true) {
            Integer indexObj = askIndex();
            if (indexObj == null) {
                return null;
            }

            int index = indexObj;
            if (index >= 0 && index < size) {
                List<Object> tileRequestPayload = new ArrayList<>();
                tileRequestPayload.add(gameId);
                tileRequestPayload.add(nickname);
                tileRequestPayload.add(clientController.clientTileFromList(index));

                Message tileRequest = Message.request(Message.OP_GET_UNCOVERED, tileRequestPayload);
                Message tileResponse = sendRequestWithResponse(tileRequest);
                Object tilePayload = tileResponse.getPayload();

                try {
                    switch (tilePayload) {
                        case String tile -> {
                            if (tile.equals("PIEDONIPRADELLA")) {
                                clientController.reportErrorByController("You missed: " + tile + ". Select a new index.");
                                continue;
                            }
                            return tile;
                        }
                        default -> throw new IOException("Unexpected payload type when fetching tile.");
                    }
                } catch (ClassCastException e) {
                    throw new IOException("Payload type mismatch when fetching tile.", e);
                }
            } else {
                clientController.informByController("Invalid index. Try again.");
            }
        }
    }



    @Override
    public void getBackTile(String tile) throws Exception {
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
    public void positionTile(String tile) throws Exception {
        clientController.printMyDashBoardByController();
        clientController.informByController("Choose coordinate!");
        int[] tmp;
        tmp = clientController.askCoordinateByController();
        if(tmp == null) return;

        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        payloadGame.add(tile);
        payloadGame.add(tmp);

        Message request = Message.request(Message.OP_POSITION_TILE, payloadGame);
        Message response = sendRequestWithResponse(request);


        Object raw = response.getPayload();
        String result;
        try {
            result = (String) raw;
        } catch (ClassCastException e) {
            throw new IOException("Unexpected payload type from server: " + raw.getClass().getName(), e);
        }
        if ("OK".equals(result)) {
            clientController.setTileInMatrix(tile, tmp[0], tmp[1]);
            clientController.printMyDashBoardByController();
        } else {
            clientController.reportErrorByController("Error from server: " + result);
        }
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
//TODO: verificare se va
    @Override
    public void lookDeck() throws Exception {
        while (true) {
            clientController.informByController("Choose deck : 1 / 2 / 3");
            Integer indexObj = askIndex();
            if(indexObj == null) { return; }

            int index = indexObj + 1;
            if (index < 1 || index > 3) {
                clientController.reportErrorByController("Invalid choice: please enter 1, 2 or 3.");
                continue;
            }

            List<Object> payload = new ArrayList<>();
            payload.add(gameId);
            payload.add(index);
            Message request  = Message.request(Message.OP_LOOK_DECK, payload);
            Message response = sendRequestWithResponse(request);

            Object payloadResponse = response.getPayload();
//            if (payloadResponse instanceof List<?> rawList) {
                @SuppressWarnings("unchecked")
               String deck = (String) payloadResponse;
                // tutto OK, esci
            if (deck.contains("ERROR")) {
                clientController.reportErrorByController("Server error: " + deck);
                // in teoria, qui non dovresti mai ricadere perché il pre-check ha già escluso
                // gli indici invalidi, ma se il server ti manda un errore lo riporti e riprovi
                continue;
            }

            clientController.printDeckByController(deck);
            return;

            // caso “strano” ma possibile
//            clientController.reportErrorByController("Unexpected response from server.");
        }
    }

    @Override
    public void lookDashBoard() throws Exception {
        String tmp = clientController.choosePlayerByController();
        if(tmp == null) return;

        List<Object> payload = new ArrayList<>();
        payload.add(gameId);
        payload.add(tmp);

        Message request = Message.request(Message.OP_LOOK_SHIP, payload);
        Message response = sendRequestWithResponse(request);
        Object payloadResponse = response.getPayload();

            switch (payloadResponse) {
                case String[][] dashPlayer -> {
                    clientController.informByController("Space Ship di: " + tmp);
                    clientController.printPlayerDashboardByController(dashPlayer);
                }
                case String error -> {
                    clientController.reportErrorByController("Invalid player name: " + error);
                }
                default -> {
                    clientController.reportErrorByController("Unexpected payload type: " + payloadResponse.getClass().getName());

                }
            }
        }

    @Override
    public void leaveGame() throws Exception {
        if (gameId != 0) {
            try{
                Message req = Message.request(Message.OP_LEAVE_GAME, List.of(gameId, nickname));
                Message resp = sendRequestWithResponse(req);
                Object payload = resp.getPayload();
                String s;
                try {
                    s = (String) payload;
                } catch (ClassCastException e) {
                    s = "";
                }
                if (!"OK".equals(s)) {
                }
            } catch (IOException e) {
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
    public void updateMapPosition(Map<String, int[]> Position)  {
        clientController.updateMapPositionByController(Position);
    }

    @Override
    public void setStart() {
        start = "start";
        startLatch.countDown();
    }

    @Override
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @Override
    public boolean askWithTimeout(String question) {
        return clientController.askWithTimeoutByController(question);
    }

    @Override
    public int[] askCoordsWithTimeout() throws Exception {
        return clientController.askCoordinatesWithTimeoutByController();
    }

    @Override
    public Integer askIndexWithTimeout() throws Exception {
        return clientController.askIndexWithTimeoutByController();
    }

    @Override
    public String askInformationAboutStart() throws Exception {
        startLatch.await();
        return "start";
    }


    @Override
    public void setTile(String tile) throws Exception {
        clientController.setCurrentTile(tile);
    }

    @Override
    public void setIsDemo(Boolean demo) {
        clientController.setIsDemoByController(demo);
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

    //TODO: quando il metodo cambierà per l'aggiornamento fatto da oleg ricordati di gestire bene la situazione!
    @Override
    public String takeReservedTile() throws IOException, BusinessLogicException, InterruptedException {
        if(clientController.returOKAY(0,5) && clientController.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        clientController.printMyDashBoardByController();
        clientController.informByController("Select a tile");
        int[] index;
        String tmpTile = null;
        while(true) {
            index = clientController.askCoordinateByController();
            if (index == null) {return null;}
            if(index[0]!=0 || clientController.returOKAY(0 , index[1])) clientController.informByController("Invalid coordinate");
            else break;
        }
        List<Object> payload = new ArrayList<>();
        payload.add(gameId);
        payload.add(nickname);
        String tmp = clientController.getSomeTile(index[0], index[1]);
        payload.add(clientController.returnIdOfTile(index[0], index[1]));
        clientController.setTileInMatrix("PIEDINIPRADELLA", index[0], index[1]);
        clientController.resetValidityByController(index[0], index[1]);
        clientController.printMyDashBoardByController();
        Message request = Message.request(Message.OP_GET_RESERVED_TILE, payload);
        Message response = sendRequestWithResponse(request);
        Object payloadResponse = response.getPayload();
        try {
            return (String) payloadResponse;
        } catch (ClassCastException e) {
            switch (payloadResponse){
                case String error: throw new IOException("Server error: " + error);
                default:  throw new IOException("Unexpected payload type when fetching tile.", e);
            }
        }
    }

    @Override
    public void updateDashMatrix(String[][] data) throws IOException, BusinessLogicException, InterruptedException {
        clientController.newShip(data);
    }
}

