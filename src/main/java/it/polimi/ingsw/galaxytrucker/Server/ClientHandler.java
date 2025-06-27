package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Client.ResponseHandler;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.Message;
import it.polimi.ingsw.galaxytrucker.Client.UpdateViewRequest;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages a single client’s session over a socket/RMI connection.
 * Reads incoming Message objects, dispatches requests to the GameManager,
 * and sends back responses or updates via a dedicated ObjectOutputStream.
 * Runs in its own thread for each connected player.
 *
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */

public class ClientHandler extends VirtualViewAdapter implements Runnable {
    private final Socket socket;
    private final GameManager gameManager;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final ResponseHandler responseHandler = new ResponseHandler();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private String nickname;
    private int gameId;

    /**
     * Sends a request and waits for the corresponding response.
     * @param req the request message
     * @return the response message received
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the waiting thread is interrupted
     */
    private Message sendRequestAndWait(Message req) throws IOException, InterruptedException {
        responseHandler.expect(req.getRequestId());
        out.writeObject(req);
        out.flush();
        return responseHandler.waitForResponse(req.getRequestId());
    }

    /**
     * Constructs a new client handler and opens I/O streams on the socket.
     * @param socket the client connection socket
     * @param gameManager the game manager handling match logic
     * @throws IOException if an I/O error occurs while opening the streams
     */
    public ClientHandler(Socket socket, GameManager gameManager) throws IOException {
        this.socket = socket;
        this.gameManager = gameManager;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Loop listening for messages as long as the connection is active.
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                Message msg = (Message) in.readObject();
                switch (msg.getMessageType()) {
                    case Message.TYPE_REQUEST:
                        worker.submit(() -> {
                            try {
                                Message reply = dispatch(msg);
                                synchronized(out) {
                                    out.writeObject(reply);
                                    out.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Errore dispatch: " + e.getMessage());
                            }
                        });
                        break;
                    case Message.TYPE_RESPONSE:
                        responseHandler.handleResponse(msg.getRequestId(), msg);
                        break;
                    case Message.TYPE_UPDATE:
                    case Message.TYPE_NOTIFICATION:
                        break;
                    default:
                }
            }
        } catch (EOFException eof) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (Exception e) {
            System.err.println("Socket error with " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            try { in .close(); } catch (IOException ignored) {}
            try { out.close(); } catch (IOException ignored) {}
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Connection closed: " + socket.getRemoteSocketAddress());
            if (nickname != null) {
                try {
                    Controller ctrl = gameManager.getControllerCheck(gameId);
                    ctrl.markDisconnected(nickname);
                } catch (Exception e) {
                    System.err.println("Error with markdisconnected for " + nickname + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Dispatches the incoming request to the appropriate handler.
     * @param req the request message
     * @return the reply message to send back
     */
    private Message dispatch(Message req) {
        try {
            String op = req.getOperation();
            Object p  = req.getPayload();
            return switch (op) {
                case Message.OP_GET_RESERVED_TILE  -> wrap(req, handleGetReservedTile(p));
                case Message.OP_LEAVE_GAME         -> wrap(req, handleLeaveGame(p));
                case Message.OP_LOGIN              -> wrap(req, handleLogin(p));
                case Message.OP_CREATE_GAME        -> wrap(req, handleCreateGame(p));
                case Message.OP_LIST_GAMES         -> wrap(req, handleListGames());
                case Message.OP_ENTER_GAME         -> wrap(req, handleEnterGame(p));
                case Message.OP_GET_TILE           -> wrap(req, handleGetTile(p));
                case Message.OP_GET_UNCOVERED      -> wrap(req, handleGetUncovered(p));
                case Message.OP_GET_UNCOVERED_LIST -> wrap(req, handleGetUncoveredList(p));
                case Message.OP_RETURN_TILE        -> wrap(req, handleReturnTile(p));
                case Message.OP_POSITION_TILE      -> wrap(req, handlePositionTile(p));
                case Message.OP_GET_CARD           -> wrap(req, handleDrawCard(p));
                case Message.OP_ROTATE_GLASS       -> wrap(req, handleRotateGlass(p));
                case Message.OP_SET_READY          -> wrap(req, handleSetReady(p));
                case Message.OP_LOOK_DECK          -> wrap(req, handleLookDeck(p));
                case Message.OP_LOOK_SHIP          -> wrap(req, handleLookShip(p));
                case Message.OP_LOGOUT             -> wrap(req, handleLogout(p));
                default -> Message.error("Unknown operation: " + op, req.getRequestId());
            };
        } catch (BusinessLogicException e) {
            return Message.response(e.getMessage(), req.getRequestId());
        } catch (IOException e) {
            return Message.error("I/O error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during dispatch: " + e.getMessage());
            return Message.error("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Wraps a raw payload in a response message.
     * @param request the original request message
     * @param payload the result of handling the request
     * @return a response message containing the payload
     */
    private Message wrap(Message request, Object payload) {
        return Message.response(payload, request.getRequestId());
    }

    /**
     * Processes a login request.
     * @param p the login payload (nickname)
     * @return the assigned game ID
     * @throws Exception on login failure
     */
    private Object handleLogin(Object p) throws Exception {
        String nickname = (String) p;
        this.nickname = nickname;
        int returnedGameId = gameManager.login(nickname, this);
        this.gameId = returnedGameId;
        return returnedGameId;
    }

    /**
     * Creates a new game (demo or real).
     * @param p list of parameters: isDemo, nickname, maxPlayers
     * @return the new game ID
     * @throws BusinessLogicException on invalid game parameters
     * @throws Exception for other errors
     */
    @SuppressWarnings("unchecked")
    private Object handleCreateGame(Object p) throws BusinessLogicException, Exception {
        List<Object> payload = (List<Object>) p;
        boolean isDemo = (boolean) payload.get(0);
        String nickname = (String)  payload.get(1);
        int maxPlayers = (int) payload.get(2);
        this.setIsDemo(isDemo);
        return gameManager.createGame(isDemo,this, nickname, maxPlayers);

    }

    /**
     * Retrieves the list of active games.
     * @return a list of active game IDs
     */
    private Object handleListGames() {
        return gameManager.listActiveGames();
    }

    /**
     * Retrieves a reserved tile for a player.
     * @param p list of parameters: gameId, nickname, tileId
     * @return the reserved tile
     * @throws BusinessLogicException if the tile cannot be reserved

     */
    @SuppressWarnings("unchecked")
    private Object handleGetReservedTile(Object p) throws BusinessLogicException{
        List<Object> args = (List<Object>) p;
        int gameId = (Integer) args.get(0);
        String nickname = (String)  args.get(1);
        int IdTile = (Integer) args.get(2);
        return gameManager.getReservedTile(gameId, nickname, IdTile);
    }

    /**
     * Joins a player into an existing game.
     * @param p list of parameters: gameId, nickname
     * @return "OK" on successful join
     * @throws BusinessLogicException if join is invalid
     * @throws Exception for other errors
     */
    @SuppressWarnings("unchecked")
    private Object handleEnterGame(Object p) throws BusinessLogicException, Exception {
        List<Object> args = (List<Object>) p;
        int gameId = (Integer) args.get(0);
        String nickname = (String)  args.get(1);
        this.gameId = gameId;
        this.nickname = nickname;
        gameManager.joinGame(gameId,this, nickname);
        return "OK";
    }

    /**
     * Draws a covered tile.
     * @param p list of parameters: gameId, nickname
     * @return the drawn covered tile
     * @throws BusinessLogicException if draw is invalid
     */
    @SuppressWarnings("unchecked")
    private Object handleGetTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        return gameManager.getCoveredTile(gameId, nickname);

    }

    /**
     * Chooses an uncovered tile.
     * @param p list of parameters: gameId, nickname, tileId
     * @return the chosen uncovered tile
     * @throws BusinessLogicException if choice is invalid
     */
    @SuppressWarnings("unchecked")
    private Object handleGetUncovered(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        int idTile = (Integer) payload.get(2);
        return gameManager.chooseUncoveredTile(gameId, nickname, idTile);

    }

    /**
     * Retrieves all currently uncovered tiles.
     * @param p list of parameters: gameId, nickname
     * @return list of uncovered tiles
     * @throws BusinessLogicException on retrieval failure
     */
    @SuppressWarnings("unchecked")
    private Object handleGetUncoveredList(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1); //con il fatto che ho rimosso il nick dal metodo del gamemanager perchè non usato, qui mi da senza uso. si dovrebbe poter levare
        return gameManager.getUncoveredTilesList(gameId);

    }

    /**
     * Returns a tile back to the pile.
     * @param p list of parameters: gameId, nickname, tileKey
     * @return "OK" on success
     * @throws BusinessLogicException if return is invalid
     */
    @SuppressWarnings("unchecked")
    private Object handleReturnTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        String tile = (String) payload.get(2);
        gameManager.dropTile(gameId, nick, tile);
        return "OK";
    }

    /**
     * Places a tile on the ship.
     * @param p list of parameters: gameId, nickname, tileKey, coords
     * @return "OK" on success
     * @throws BusinessLogicException if placement is invalid
     */
    @SuppressWarnings("unchecked")
    private Object handlePositionTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        String tile = (String) payload.get(2);
        int[] coordinate = (int[]) payload.get(3);
        gameManager.placeTile(gameId, nick, tile, coordinate);
        return "OK";
    }

    /**
     * Draws an adventure card.
     * @param p list of parameters: gameId, nickname
     * @return "OK" on success
     * @throws BusinessLogicException if draw is invalid
     */
    @SuppressWarnings("unchecked")
    private Object handleDrawCard(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.drawCard(gameId, nickname);
        return "OK";
    }

    /**
     * Flips the hourglass for the player.
     * @param p list of parameters: gameId, nickname
     * @return "OK" on success
     * @throws BusinessLogicException if flip is invalid
     * @throws RemoteException on communication error
     */
    @SuppressWarnings("unchecked")
    private Object handleRotateGlass(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.flipHourglass(gameId, nick);
        return "OK";
    }

    /**
     * Marks the player as ready for the flight phase.
     * @param p list of parameters: gameId, nickname
     * @return "OK" on success
     * @throws BusinessLogicException if readying is invalid
     * @throws RemoteException on communication error
     */
    @SuppressWarnings("unchecked")
    private Object handleSetReady(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.setReady(gameId, nick);
        return "OK";
    }

    /**
     * Views a specific deck during ship construction.
     * @param p list of parameters: gameId, deckIndex
     * @return the contents of the chosen deck
     * @throws BusinessLogicException on invalid access
     */
    @SuppressWarnings("unchecked")
    private Object handleLookDeck(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        int idxDeck = (Integer) payload.get(1);
        return gameManager.showDeck(gameId, idxDeck);

    }

    /**
     * Views an opponent’s ship dashboard.
     * @param p list of parameters: gameId, nickname
     * @return the opponent’s dashboard view
     * @throws BusinessLogicException on invalid access
     */
    @SuppressWarnings("unchecked")
    private Object handleLookShip(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        return gameManager.lookAtDashBoard(nickname, gameId);

    }

    /**
     * Player quits the current game.
     * @param p list of parameters: gameId, nickname
     * @return "OK" on success
     * @throws BusinessLogicException if quit is invalid
     * @throws Exception for other errors
     */
    @SuppressWarnings("unchecked")
    private Object handleLeaveGame(Object p) throws BusinessLogicException,Exception {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.quitGame(gameId, nickname);
        return "OK";
    }

    /**
     * Logs out the player and closes their session.
     * @param p the nickname to logout
     * @return "OK" on success
     * @throws BusinessLogicException on invalid logout
     */
    private Object handleLogout(Object p) throws BusinessLogicException {
        String nickname = (String) p;
        gameManager.logout(nickname);
        return "OK";
    }

    /**
     * Sends a notification message to the client.
     * @param message the text of the notification
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void inform(String message) throws IOException {
        out.writeObject(Message.notify(message));
        out.flush();
    }

    /**
     * Sends an error message to the client.
     * @param error the error description
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void reportError(String error) throws IOException {
        out.writeObject(Message.error(error));
        out.flush();
    }

    /**
     * Sends the current game phase to the client.
     * @param fase the identifier of the current game phase
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void updateGameState(String fase) throws IOException {
        out.writeObject(Message.update(Message.OP_GAME_PHASE, fase));
        out.flush();
    }

    /**
     * Sends a detailed view update to the client.
     *
     * @param nickname the player’s nickname
     * @param firePower the current firepower
     * @param powerEngine the current engine power
     * @param credits the current credits
     * @param purpleAlien true if a purple alien is present
     * @param brownAlien true if a brown alien is present
     * @param numberOfHuman  number of human crew members
     * @param numberOfEnergy number of energy units
     * @throws RemoteException if sending the update fails
     */
    @Override
    public void showUpdate(
            String nickname,
            double firePower,
            int powerEngine,
            int credits,
            boolean purpleAlien,
            boolean brownAlien,
            int numberOfHuman,
            int numberOfEnergy
    ) throws RemoteException {
        UpdateViewRequest upd = new UpdateViewRequest(
                nickname,
                firePower,
                powerEngine,
                credits,
                /*position,*/
                purpleAlien,
                brownAlien,
                numberOfHuman,
                numberOfEnergy
        );
        try {
            out.writeObject(Message.update(Message.OP_UPDATE_VIEW, upd));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Sends an adventure card to the client.
     * @param card the card representation
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printCard(String card) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_CARD, card));
        out.flush();
    }

    /**
     * Sends the list of covered tiles to the client.
     * @param tiles the covered tiles representation
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printListOfTileCovered(String tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_COVERED, tiles));
        out.flush();
    }

    /**
     * Sends the list of shown tiles to the client.
     * @param tiles the shown tiles representation
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printListOfTileShown(String tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_SHOWN, tiles));
        out.flush();
    }

    /**
     * Sends the list of goods from an adventure card to the client.
     * @param goods the list of goods strings
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printListOfGoods(List<String> goods) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_GOODS, goods));
        out.flush();
    }

    /**
     * Sends the player’s ship dashboard to the client.
     * @param dashboard the 2D array representing the dashboard
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printPlayerDashboard(String[][] dashboard) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DASHBOARD, dashboard));
        out.flush();
    }

    /**
     * Sends the adventure card deck to the client.
     * @param deck the deck representation
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printDeck(String deck) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DECK, deck));
        out.flush();
    }

    /**
     * Sends a single tile representation to the client.
     * @param tile the tile identifier
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void printTile(String tile) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_TILE, tile));
        out.flush();
    }

    /**
     * Notifies the client of the current game ID.
     * @param gameId the ID of the game
     * @throws RemoteException if sending the update fails
     */
    @Override
    public void setGameId(int gameId) throws RemoteException {
        this.gameId = gameId;
        super.setGameId(gameId);
        try {
            out.writeObject(Message.update(Message.OP_SET_GAMEID, gameId));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * Notifies the client of the player’s nickname.
     * @param nickname the player’s nickname
     * @throws Exception if sending the update fails
     */
    @Override
    public void setNickname(String nickname) throws Exception {
        this.nickname = nickname;
        super.setNickname(nickname);
        out.writeObject(Message.update(Message.OP_SET_NICKNAME, nickname));
        out.flush();
    }

    /**
     * Sends the map of player positions to the client.
     * @param map mapping of nicknames to [row, column] coordinates
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void updateMapPosition(Map<String, int [] > map) throws IOException {
        out.writeObject(Message.update(Message.OP_MAP_POSITION, new HashMap<>(map)));
        out.flush();
    }

    /**
     * Notifies the client that the game has started.
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void setStart() throws IOException {
        out.writeObject(Message.update(Message.OP_SET_FLAG_START, null));
        out.flush();
    }

    /**
     * Sends the central tile information to the client.
     * @param tile the central tile identifier
     * @throws IOException if an I/O error occurs while sending
     */
    @Override
    public void setTile(String tile) throws IOException {
        out.writeObject(Message.update(Message.OP_SET_CENTRAL_TILE, tile));
        out.flush();
    }

    /**
     * Notifies the client of demo mode status.
     * @param demo true if the game is in demo mode, false otherwise
     * @throws Exception if sending the update fails
     */
    @Override
    public void setIsDemo(Boolean demo) throws Exception {
        out.writeObject(Message.update(Message.OP_SET_IS_DEMO, demo));
        out.flush();
    }

    /**
     * Sends the player’s dashboard matrix to the client.
     * @param data the dashboard matrix
     * @throws Exception if sending the update fails
     */
    @Override
    public void updateDashMatrix(String[][] data) throws Exception {
        out.writeObject(Message.update(Message.OP_UPDATE_DA, data));
        out.flush();
    }

    /**
     * Sends a yes/no question to the client.
     * @param question the question text
     * @return the client’s boolean answer
     * @throws IOException on I/O error
     */
    @Override
    public Boolean ask(String question) throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_ASK, question));
            return (Boolean) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Sends an index request to the client.
     * @return the client’s integer answer
     * @throws IOException on I/O error
     */
    @Override
    public Integer askIndex() throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_INDEX, null));
            return (Integer) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Requests ship coordinates from the client.
     * @return an array [row, column] chosen by the client
     * @throws IOException on I/O error
     */
    @Override
    public int[] askCoordinate() throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_COORDINATE, null));
            return (int[]) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Sends a string request to the client.
     * @return the client’s string response
     * @throws IOException on I/O error
     */
    @Override
    public String askString() throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_STRING, null));
            return (String) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Sends a yes/no question with timeout.
     * @param question the question text
     * @return the client’s boolean answer or default on timeout
     * @throws IOException on I/O error
     */
    @Override
    public boolean askWithTimeout(String question) throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_ASK_TO, question));
            return (Boolean) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Sends an index request with timeout.
     * @return the client’s integer answer or default on timeout
     * @throws IOException on I/O error
     */
    @Override
    public Integer askIndexWithTimeout() throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_INDEX_TO, null));
            return (Integer) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Requests ship coordinates with timeout.
     * @return an array [row, column] or null on timeout
     * @throws IOException on I/O error
     */
    @Override
    public int[] askCoordsWithTimeout() throws IOException {
        try {
            Message resp = sendRequestAndWait(Message.request(Message.OP_COORDINATE_TO, null));
            return (int[]) resp.getPayload();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

