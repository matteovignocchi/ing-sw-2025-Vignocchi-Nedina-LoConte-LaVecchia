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

/**
 * Represents the socket-based client component that handles communication between
 * the local client (GUI or TUI) and the remote server in a Galaxy Trucker game session.
 * Implements the {@code VirtualView} interface to provide game output and input capabilities,
 * and manages asynchronous request/response communication via serialized {@link Message} objects.
 * The {@code VirtualClientSocket} runs a dedicated thread to listen for incoming messages
 * from the server and dispatches them based on their type (REQUEST, RESPONSE, UPDATE, etc.).
 * This class acts as a passive intermediary, delegating game logic and state rendering to
 * the {@link ClientController}, and remaining agnostic of the actual view implementation.
 * @author Matteo Vignocchi && Francesco Lo Conte
 */
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

    // === Initialization ===
    /**
     * Creates a new VirtualClientSocket that connects to the specified server host and port.
     * This constructor initializes both output and input object streams for sending and receiving
     * serialized {@link Message} objects over the network. It also starts a new thread to listen
     * for incoming messages by invoking the {@code run()} method.
     *
     * @param host the hostname or IP address of the server
     * @param port the port number the server is listening on
     * @throws IOException if an I/O error occurs when opening the socket or streams
     */
    public VirtualClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        new Thread(this).start();
    }

    /**
     * Sets the {@link ClientController} that manages this client's view logic.
     *
     * @param clientController the controller to associate
     */
    @Override
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Sets the player's nickname for this client instance.
     *
     * @param nickname the nickname to assign
     */
    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Sets the current game ID for this client instance.
     *
     * @param gameId the ID of the game the player is currently in
     */
    @Override
    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    // === Main Runtime Loop ===
    /**
     * Listens for incoming {@link Message} objects from the server and dispatches them
     * based on their type.
     * This method runs in a dedicated thread and handles the main reception loop.
     * If an error or disconnection occurs, the loop exits and the client is considered inactive.
     * Any fatal exception is wrapped into a {@link RuntimeException}.
     */
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

    // === Message Handling (Request/Response/Update/Error) ===
    /**
     * Handles an incoming {@link Message} of type {@code REQUEST} by processing the server's request
     * for user input and sending a corresponding {@code RESPONSE} message back.
     * For each operation, the appropriate user input method is called via the {@link ClientController},
     * and the collected value is wrapped in a {@link Message#response(Object, String)} and sent back to the server.
     * If the user input is {@code null}, no response is sent.
     *
     * @param msg the incoming request message containing the operation and payload
     */
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
                    Boolean decision = this.askWithTimeout((String) msg.getPayload());
                    sendRequest(Message.response(decision, msg.getRequestId()));
                }
                case Message.OP_COORDINATE_TO -> {
                    int[] coordinate = this.askCoordsWithTimeout();
                    sendRequest(Message.response(coordinate, msg.getRequestId()));
                }
                case Message.OP_INDEX_TO -> {
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

    /**
     * Handles a server-initiated {@link Message} of type {@code UPDATE}, applying the corresponding update
     * to the client’s state or view based on the message operation.
     * For any unrecognized operation, an {@link IllegalStateException} is thrown.
     *
     * @param msg the incoming update message
     * @throws Exception if any update-related logic throws or an invalid payload is encountered
     */
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

    /**
     * Sends a {@link Message} to the server through the output stream.
     *
     * @param message the message to send to the server
     * @throws IOException if an I/O error occurs while writing the message to the stream
     */
    private void sendRequest(Message message) throws IOException {
        responseHandler.expect(message.getRequestId());
        out.writeObject(message);
        out.flush();
    }

    /**
     * Sends a {@link Message} request to the server and waits for a corresponding response.
     *
     * @param msg the request message to send
     * @return the response {@link Message} received from the server
     * @throws IOException if an I/O error occurs while sending the message
     * @throws InterruptedException if the waiting thread is interrupted while waiting for the response
     */
    public Message sendRequestWithResponse(Message msg) throws IOException, InterruptedException {
        responseHandler.expect(msg.getRequestId());
        sendRequest(msg);
        return responseHandler.waitForResponse(msg.getRequestId());
    }

    // === Client to Server Communication ===
    /**
     * Sends a request to the server to either create or join a game, depending on the specified command.
     * If the {@code message} is {@code "CREATE"}, the client will send a request to create a new game
     * with the given number of players and demo mode flag.
     * If the {@code message} is {@code "JOIN"}, the client enters a loop requesting the list of available games
     * from the server. The user is prompted to select one; the chosen game ID is returned if valid.
     *
     * @param message either "CREATE" or "JOIN"
     * @param numPlayer the number of players for the game (used only for creation)
     * @param isDemo2 {@code true} if the game should be created in demo mode
     * @return the game ID of the joined or created game, {@code -1} if no games were available, or {@code 0} if the user decides to not join {@code choice == 0}
     * @throws IOException if an I/O error occurs during communication with the server
     * @throws InterruptedException if the thread is interrupted while waiting for a response
     */
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

    /**
     * Sends a login request to the server using the provided username.
     * The method validates the username, sends a {@link Message} with operation {@code OP_LOGIN},
     * and waits for a response. If the server responds with an integer, it is interpreted as the
     * game ID the user has joined or created. If the server returns an error string, it is thrown
     * as a {@link BusinessLogicException}.
     *
     * @param username the player's chosen nickname; must not be null or blank
     * @return the game ID returned by the server
     * @throws IllegalArgumentException if the username is null or empty
     * @throws IOException if an I/O error occurs or the server returns an invalid payload
     * @throws InterruptedException if the thread is interrupted while waiting for a response
     * @throws BusinessLogicException if the server returns a domain-specific login error
     */
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

    /**
     * Sends a request to the server to leave the current game.
     * If {@code gameId} is not 0, a {@code OP_LEAVE_GAME} message is sent with the game ID and nickname.
     * The method waits for the server's response. If the response is not {@code "OK"}, the method exits silently.
     * If the server throws an {@link IOException}, it is rethrown with a detailed message.
     * On success, the local {@code gameId} is reset to 0.
     * @throws Exception if a communication or server error occurs
     */
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
                    return;
                }
            } catch (IOException e) {
                throw new IOException("Error from server: " + e);
            }
            gameId = 0;
        }
    }

    /**
     * Sends a logout request to the server and terminates the client session.
     * Sends a {@code OP_LOGOUT} message with the player's nickname. If the response payload
     * is not {@code "OK"}, an {@link IOException} is thrown. On successful logout, the socket is closed,
     * the client is marked as inactive, and the application exits with status code 0.
     *
     * @throws Exception if the server returns an error or a communication failure occurs
     */
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

    /**
     * Notifies the server that the player is ready to proceed (e.g., at the end of the building phase).
     * Sends a {@link Message} with operation {@code OP_SET_READY} including the current {@code gameId}
     * and {@code nickname}. Waits for the server's response.
     * Response handling:
     * If the payload is {@code "OK"}, the readiness has been successfully registered.
     * If the payload is any other {@code String}, it is treated as an error and thrown as an {@link IOException}.
     * If the payload is of an unexpected type, a generic {@link IOException} is thrown.
     *
     * @throws IOException if the server responds with an error message or an unexpected payload
     * @throws Exception if any other exception occurs during the communication
     */
    @Override
    public void setReady() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_SET_READY, payloadGame);
        Message response = sendRequestWithResponse(request);
        Object payload = response.getPayload();
        switch (payload) {
            case String p when p.equals("OK") -> {}
            case String error -> throw new IOException("Error from server: " + error);
            default -> throw new IOException("Unexpected payload: " );
        }
    }

    /**
     * Sends a request to the server to draw the next card in the game sequence for the current player.
     * Constructs and sends a {@link Message} with operation {@code OP_GET_CARD} and payload containing
     * the current game ID and player nickname. Waits for a response from the server.
     * Response handling:
     * If the payload is {@code "OK"}, the card was drawn successfully and no further action is required.
     * If the payload is a different {@code String}, it is treated as an error and thrown as an {@link IOException}.
     * If the payload is of an unexpected type, a generic {@link IOException} is thrown.
     *
     * @throws IOException if the server returns an error message or an invalid payload
     * @throws Exception if a general error occurs during the request or response processing
     */
    @Override
    public void drawCard() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);
        Message request = Message.request(Message.OP_GET_CARD, payloadGame);
        Message response = sendRequestWithResponse(request);
        Object payload = response.getPayload();
        switch (payload) {
            case String p when p.equals("OK") -> {}
            case String error -> throw new IOException("Error from server: " + error);
            default -> throw new IOException("Unexpected payload: " );
        }
    }

    /**
     * Sends a request to the server to rotate (flip) the hourglass, indicating the player is speeding up their turn.
     * This method builds a {@link Message} with operation {@code OP_ROTATE_GLASS} and includes
     * the current game ID and player nickname in the payload. It waits for the server's response.
     * Response handling:
     * If the payload is {@code "OK"}, the rotation is acknowledged.
     * If the payload is a different string, it is treated as an error and thrown as an {@link IOException}.
     * If the payload is of an unexpected type, a generic {@link IOException} is thrown.
     *
     * @throws IOException if the server returns an error message or an unexpected payload type
     * @throws Exception if any other error occurs during the interaction
     */
    @Override
    public void rotateGlass() throws Exception {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);

        Message request = Message.request(Message.OP_ROTATE_GLASS, payloadGame);
        Message response = sendRequestWithResponse(request);

        Object payload = response.getPayload();

        switch (payload) {
            case String p when p.equals("OK") -> {}
            case String error -> throw new IOException("Error from server: " + error);
            default -> throw new IOException("Unexpected payload: " );
        }
    }

    /**
     * Sends a request to the server to place a tile at a specific coordinate on the player's dashboard.
     * The method performs the following steps:
     * Displays the current dashboard to the player.
     * Prompts the player to choose a coordinate for placement.
     * If a valid coordinate is selected, sends a {@link Message} with operation {@code OP_POSITION_TILE}
     * containing the game ID, player nickname, tile identifier, and chosen coordinates.
     * Processes the server response: if the payload is {@code "OK"}, the tile is placed and the dashboard is updated;
     * otherwise, an error message is shown.
     *
     * @param tile the tile (as a string) to be placed on the dashboard
     * @throws IOException if the server returns an error or sends an unexpected payload type
     * @throws Exception if any other error occurs during communication or user interaction
     */
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

    /**
     * Sends a request to return a previously picked tile back to the central pool.
     * Constructs a {@link Message} with operation {@code OP_RETURN_TILE} and a payload containing:
     * the current game ID,the player's nickname, the string representation of the tile to return.
     * Awaits a response from the server:
     * If the response payload is {@code "OK"}, the operation succeeded silently.
     * If the payload is a different string, it's treated as an error and an {@link IOException} is thrown.
     * If the payload is of an unexpected type, a generic {@link IOException} is also thrown.
     *
     * @param tile the tile string to return to the deck
     * @throws IOException if the server responds with an error message or an unexpected payload type
     * @throws Exception if any other exception occurs during the request/response process
     */
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
            case String p when p.equals("OK") -> {}
            case String error -> throw new IOException("Error from server: " + error);
            default -> throw new IOException("Unexpected payload: ");
        }
    }

    /**
     * Requests a reserved tile from the server based on the user's selection from row 0.
     * If both reserved positions at column 5 and 6 are empty (as determined by {@code returOKAY}),
     * a {@link BusinessLogicException} is thrown to indicate that no tile is available.
     * The method displays the dashboard and prompts the user to select a coordinate.
     * It validates that the coordinate is in row 0 and that the position is not already empty.
     * A message with operation {@code OP_GET_RESERVED_TILE} is sent with the game ID, nickname,
     * and the ID of the selected tile. After invalidating the tile locally, the updated dashboard is shown.
     * If the response payload is a {@code String}, it is returned as the selected tile.
     * If the response is an error string, an {@link IOException} is thrown.
     * If the payload is not a string, a generic {@link IOException} is thrown.
     *
     * @return the string representation of the reserved tile
     * @throws IOException if the server returns an error or the payload is invalid
     * @throws BusinessLogicException if there are no reserved tiles available
     * @throws InterruptedException if the thread is interrupted while waiting for input or response
     */
    @Override
    public String takeReservedTile() throws IOException, BusinessLogicException, InterruptedException {
        if(clientController.returOKAY(0,5) && clientController.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        clientController.printMyDashBoardByController();
        clientController.informByController("Select a tile");
        int[] index;
        while(true) {
            index = clientController.askCoordinateByController();
            if (index == null) {return null;}
            if(index[0]!=0 || clientController.returOKAY(0 , index[1])) clientController.informByController("Invalid coordinate");
            else break;
        }
        List<Object> payload = new ArrayList<>();
        payload.add(gameId);
        payload.add(nickname);
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

    // === Server to Client Rendering ===
    /**
     * Displays a message or notification to the user.
     *
     * @param message the message content to display
     */
    @Override
    public void inform(String message) {
        clientController.informByController(message);
    }

    /**
     * Displays an error message to the user.
     *
     * @param error the error description
     */
    @Override
    public void reportError(String error) {
        clientController.reportErrorByController(error);
    }

    /**
     * Updates the client's view with new ship and player statistics.
     *
     * @param nickname the player's nickname
     * @param firePower the ship's firepower
     * @param powerEngine the engine's power level
     * @param credits the player's current credits
     * @param purpleAline whether the ship contains a purple alien
     * @param brownAlien whether the ship contains a brown alien
     * @param numberOfHuman the number of humans onboard
     * @param numberOfEnergy the number of energy units onboard
     * @throws RemoteException if an error occurs while updating the client remotely
     */
    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        clientController.showUpdateByController(nickname, firePower, powerEngine, credits, purpleAline, brownAlien, numberOfHuman, numberOfEnergy);
    }

    /**
     * Displays the currently drawn card to the user.
     *
     * @param card a textual representation of the card
     */
    @Override
    public void printCard(String card) {
        clientController.printCardByController(card);
    }

    /**
     * Displays a tile to the user.
     *
     * @param tile the tile to show
     */
    @Override
    public void printTile(String tile) {
        clientController.printTileByController(tile);
    }

    /**
     * Displays the list of covered tiles to the user.
     *
     * @param tiles a textual representation of the covered tiles
     */
    @Override
    public void printListOfTileCovered(String tiles) {
        clientController.printListOfTileCoveredByController();
    }

    /**
     * Displays the list of shown tiles to the user.
     *
     * @param tiles a textual representation of the shown tiles
     */
    @Override
    public void printListOfTileShown(String tiles) {
        clientController.printListOfTileShownByController(tiles);
    }

    /**
     * Displays the list of goods collected by the player.
     *
     * @param listOfGoods a list of string representations of goods
     */
    @Override
    public void printListOfGoods(List<String> listOfGoods) {
        clientController.printListOfGoodsByController(listOfGoods);
    }

    /**
     * Displays the player's dashboard (ship layout).
     *
     * @param dashBoard the matrix representing the ship's dashboard
     */
    @Override
    public void printPlayerDashboard(String[][] dashBoard) {
        clientController.printPlayerDashboardByController(dashBoard);
    }

    /**
     * Displays the current deck of cards.
     *
     * @param deck a textual representation of the deck
     */
    @Override
    public void printDeck(String deck) {
        clientController.printDeckByController(deck);
    }

    /**
     * Updates the current game phase on the client side, unless the phase is null or a heartbeat signal ("PING").
     * This method is typically triggered by the server via a {@code Message.OP_GAME_PHASE} update.
     *
     * @param phase the new game phase to display (e.g., BUILDING, FLIGHT, EXIT)
     */
    @Override
    public void updateGameState(String phase){
        if (phase == null || "PING".equalsIgnoreCase(phase)) return;
        clientController.updateGameStateByController(phase);
    }

    /**
     * Updates the position of all players on the game map.
     *
     * @param Position a map of player names to their coordinates
     */
    @Override
    public void updateMapPosition(Map<String, int[]> Position) {
        clientController.updateMapPositionByController(Position);
    }

    /**
     * Updates the current tile being manipulated by the user.
     *
     * @param tile the tile to set as current
     * @throws Exception if an error occurs during the update
     */
    @Override
    public void setTile(String tile) throws Exception {
        clientController.setCurrentTile(tile);
    }

    /**
     * Sets the demo mode status for this client.
     *
     * @param demo true if in demo mode, false otherwise
     */
    @Override
    public void setIsDemo(Boolean demo) {
        clientController.setIsDemoByController(demo);
    }

    /**
     * Updates the client's internal dashboard matrix with the provided data.
     * This method is typically called when the server pushes an updated ship layout,
     * for example after tile placement or loading a saved state.
     * Delegates the update to {@code clientController.newShip(data)}.
     *
     * @param data a 2D array representing the updated dashboard matrix
     */
    @Override
    public void updateDashMatrix(String[][] data){
        clientController.newShip(data);
    }

    /**
     * Allows the player to view the content of one of the three decks (1, 2, or 3).
     * Prompts the player to select a deck index. If the input is invalid, an error message is shown
     * and the player is prompted again. Once a valid choice is made, a message with operation
     * {@code OP_LOOK_DECK} is sent to the server containing the {@code gameId} and selected deck index.
     * If the server returns a valid deck string, it is displayed. If the response contains
     * {@code "ERROR"}, an error is shown and the user is asked to choose again.
     *
     * @throws Exception if an error occurs during input or communication with the server
     */
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
            String deck = (String) payloadResponse;
            if (deck.contains("ERROR")) {
                clientController.reportErrorByController("Server error: " + deck);
                continue;
            }
            clientController.printDeckByController(deck);
            return;
        }
    }

    /**
     * Allows the player to view the dashboard (ship layout) of another player in the game.
     * Prompts the user to select a player name. If {@code null} is returned (e.g., the user cancels),
     * the method exits. Otherwise, a message with operation {@code OP_LOOK_SHIP} is sent to the server
     * containing the {@code gameId} and selected player name.
     * If the server responds with a {@code String[][]}, it is interpreted as the player's dashboard and shown.
     * If the server responds with a {@code String}, it is assumed to be an error message and shown to the user.
     * If the payload is of an unexpected type, a generic error is displayed.
     *
     * @throws Exception if a communication or controller-related error occurs
     */
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
            case String error -> clientController.reportErrorByController("Invalid player name: " + error);
            default -> clientController.reportErrorByController("Unexpected payload type: " + payloadResponse.getClass().getName());
        }
    }

    // === Input Requests from Server ===
    /**
     * Asks the user a yes/no question and returns their response.
     *
     * @param message the question to ask
     * @return {@code true} if the user responds affirmatively, {@code false} otherwise
     */
    @Override
    public Boolean ask(String message) {
        return clientController.askByController(message);
    }

    /**
     * Asks the user to select an index (e.g., from a list of goods).
     *
     * @return the index selected by the user
     * @throws IOException if an I/O error occurs during interaction
     * @throws InterruptedException if the thread is interrupted while waiting for input
     */
    @Override
    public Integer askIndex() throws IOException, InterruptedException {
        return clientController.askIndexByController();
    }

    /**
     * Asks the user to select a coordinate (e.g., on the dashboard grid).
     *
     * @return the coordinate selected by the user as an int array [row, column]
     */
    @Override
    public int[] askCoordinate() {
        return clientController.askCoordinateByController();
    }

    /**
     * Asks the user to input a string (e.g., a username).
     *
     * @return the string entered by the user
     */
    @Override
    public String askString() {
        return clientController.askStringByController();
    }

    /**
     * Asks the user a yes/no question with a timeout.
     *
     * @param question the question to ask
     * @return the user's answer, or a default in case of timeout
     */
    @Override
    public boolean askWithTimeout(String question) {
        return clientController.askWithTimeoutByController(question);
    }

    /**
     * Prompts the user for coordinates with a timeout.
     *
     * @return the selected coordinates, or a default/null if timeout occurs
     * @throws Exception if an error occurs while collecting input
     */
    @Override
    public int[] askCoordsWithTimeout() throws Exception {
        return clientController.askCoordinatesWithTimeoutByController();
    }

    /**
     * Prompts the user for an index with a timeout.
     *
     * @return the selected index, or a default if timeout occurs
     * @throws Exception if an error occurs while collecting input
     */
    @Override
    public Integer askIndexWithTimeout() throws Exception {
        return clientController.askIndexWithTimeoutByController();
    }

    /**
     * Blocks until the game is marked as started, then returns the string "start".
     *
     * @return the string "start" once the game has begun
     * @throws Exception if interrupted while waiting
     */
    @Override
    public String askInformationAboutStart() throws Exception {
        startLatch.await();
        return "start";
    }


    // === Game Actions ===
    /**
     * Requests a covered tile from the server for the current player and game.
     * Sends a {@link Message} with operation {@code OP_GET_TILE}, containing the current {@code gameId}
     * and {@code nickname} as payload. The server responds with a string representation of the tile.
     *
     * @return the string representing the covered tile assigned by the server
     * @throws IOException if the response payload is not a string or if a communication error occurs
     * @throws InterruptedException if the thread is interrupted while waiting for the server's response
     */
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

    /**
     * Retrieves the list of shown (uncovered) tiles from the server and allows the user to select one.
     * The method performs the following steps:
     * Sends a {@code OP_GET_UNCOVERED_LIST} request to obtain the list of shown tiles for the current game and player.
     * If the server returns {@code "CODE404"} or {@code null}, a {@link BusinessLogicException} is thrown indicating the list is empty.
     * The list is displayed to the user, and they are prompted to select a tile index.
     * The selected tile is requested from the server via {@code OP_GET_UNCOVERED} using the tile's ID.
     * If the server returns {@code "CODE404"}, the selection was invalid, and the user is prompted again.
     * If successful, the tile string is returned.
     *
     * @return the string representation of the selected tile
     * @throws BusinessLogicException if the shown tile list is empty or not available
     * @throws IOException if a communication error occurs or the payload from the server is invalid
     * @throws InterruptedException if the thread is interrupted while waiting for input or response
     */
    @Override
    public String getUncoveredTile() throws BusinessLogicException, IOException, InterruptedException {
        List<Object> payloadGame = new ArrayList<>();
        payloadGame.add(gameId);
        payloadGame.add(nickname);

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

        if (tmp == null || tmp.equals("CODE404")) {
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
                            if (tile.equals("CODE404")) {
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

    /**
     * Marks the game as started and releases any waiting threads.
     */
    @Override
    public void setStart() {
        start = "start";
        startLatch.countDown();
    }






}

