package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.Message;
import it.polimi.ingsw.galaxytrucker.Client.UpdateViewRequest;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler extends VirtualViewAdapter implements Runnable {
    private final Socket socket;
    private final GameManager gameManager;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ClientHandler(Socket socket, GameManager gameManager) throws IOException {
        this.socket = socket;
        this.gameManager = gameManager;

        // APRO PRIMA L'OUTPUT, POI L'INPUT per evitare deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                Message req = (Message) in.readObject();

                if (!Message.TYPE_REQUEST.equals(req.getMessageType())) {
                    out.writeObject(Message.error("Expected REQUEST but got " + req.getMessageType()));
                    out.flush();
                    continue;
                }

                Message resp = dispatch(req);
                out.writeObject(resp);
                out.flush();
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
        }
    }

    private Message dispatch(Message req) {
        try {
            String op = req.getOperation();
            Object p  = req.getPayload();

            if (Message.OP_COORDINATE_TO.equals(op)) {
                int[] coords = askCoordsWithTimeout();
                return wrap(req, coords);
            }
            if (Message.OP_ASK_TO.equals(op)) {
                boolean yesNo = askWithTimeout((String) p);
                return wrap(req, yesNo);
            }
            if (Message.OP_INDEX_TO.equals(op)) {
                Integer idx = askIndexWithTimeout();
                return wrap(req, idx);
            }

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

    private Message wrap(Message request, Object payload) {
        return Message.response(payload, request.getRequestId());
    }

    private Object handleLogin(Object p) throws Exception {
        String nickname = (String) p;
        return gameManager.login(nickname, this);
    }

    @SuppressWarnings("unchecked")
    private Object handleCreateGame(Object p) throws BusinessLogicException, Exception {
        List<Object> payload = (List<Object>) p;
        boolean isDemo = (boolean) payload.get(0);
        String nickname = (String)  payload.get(1);
        int maxPlayers = (int) payload.get(2);
        this.setIsDemo(isDemo);
        return gameManager.createGame(isDemo,this, nickname, maxPlayers);

    }

    private Object handleListGames() {
        return gameManager.listActiveGames();
    }

    @SuppressWarnings("unchecked")
    private Object handleGetReservedTile(Object p) throws BusinessLogicException{
        List<Object> args = (List<Object>) p;
        int gameId = (Integer) args.get(0);
        String nickname = (String)  args.get(1);
        int IdTile = (Integer) args.get(2);
        return gameManager.getReservedTile(gameId, nickname, IdTile);
    }

    @SuppressWarnings("unchecked")
    private Object handleEnterGame(Object p) throws BusinessLogicException, Exception {
        List<Object> args = (List<Object>) p;
        int gameId = (Integer) args.get(0);
        String nickname = (String)  args.get(1);
        gameManager.joinGame(gameId,this, nickname);
        return "OK";
    }

    @SuppressWarnings("unchecked")
    private Object handleGetTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        return gameManager.getCoveredTile(gameId, nickname);

    }

    @SuppressWarnings("unchecked")
    private Object handleGetUncovered(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        int idTile = (Integer) payload.get(2);
        return gameManager.chooseUncoveredTile(gameId, nickname, idTile);

    }

    @SuppressWarnings("unchecked")
    private Object handleGetUncoveredList(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        return gameManager.getUncoveredTilesList(gameId, nickname);

    }

    @SuppressWarnings("unchecked")
    private Object handleReturnTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        String tile = (String) payload.get(2);
        gameManager.dropTile(gameId, nick, tile);
        return "OK";
    }

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

    @SuppressWarnings("unchecked")
    private Object handleDrawCard(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.drawCard(gameId, nickname);
        return "OK";
    }

    @SuppressWarnings("unchecked")
    private Object handleRotateGlass(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.flipHourglass(gameId, nick);
        return "OK";
    }

    @SuppressWarnings("unchecked")
    private Object handleSetReady(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.setReady(gameId, nick);
        return "OK";
    }

    @SuppressWarnings("unchecked")
    private Object handleLookDeck(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        int idxDeck = (Integer) payload.get(1);
        return gameManager.showDeck(gameId, idxDeck);

    }

    @SuppressWarnings("unchecked")
    private Object handleLookShip(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        return gameManager.lookAtDashBoard(nickname, gameId);

    }

    @SuppressWarnings("unchecked")
    private Object handleLeaveGame(Object p) throws BusinessLogicException,Exception {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.quitGame(gameId, nickname);
        return "OK";
    }

    private Object handleLogout(Object p) throws BusinessLogicException {
        String nickname = (String) p;
        gameManager.logout(nickname);
        return "OK";
    }

    @Override
    public void inform(String message) throws IOException {
        out.writeObject(Message.notify(message));
        out.flush();
    }

    @Override
    public void reportError(String error) throws IOException {
        out.writeObject(Message.error(error));
        out.flush();
    }

    @Override
    public void updateGameState(String fase) throws IOException {
        out.writeObject(Message.update(Message.OP_GAME_PHASE, fase));
        out.flush();
    }

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

    @Override
    public void printCard(String card) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_CARD, card));
        out.flush();
    }

    @Override
    public void printListOfTileCovered(String tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_COVERED, tiles));
        out.flush();
    }

    @Override
    public void printListOfTileShown(String tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_SHOWN, tiles));
        out.flush();
    }

    @Override
    public void printListOfGoods(List<String> goods) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_GOODS, goods));
        out.flush();
    }

    @Override
    public void printPlayerDashboard(String[][] dashboard) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DASHBOARD, dashboard));
        out.flush();
    }

    @Override
    public void printDeck(String deck) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DECK, deck));
        out.flush();
    }

    @Override
    public void printTile(String tile) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_TILE, tile));
        out.flush();
    }

    @Override
    public void setGameId(int gameId) throws RemoteException {
        try {
            out.writeObject(Message.update(Message.OP_SET_GAMEID, gameId));
            out.flush();
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void setNickname(String nickname) throws IOException {
        out.writeObject(Message.update(Message.OP_SET_NICKNAME, nickname));
        out.flush();
    }

    @Override
    public void updateMapPosition(Map<String,Integer> map) throws IOException {
        out.writeObject(Message.update(Message.OP_MAP_POSITION, new HashMap<>(map)));
        out.flush();
    }

    @Override
    public void setStart() throws IOException {
        out.writeObject(Message.update(Message.OP_SET_FLAG_START, null));
        out.flush();
    }

    @Override
    public void setTile(String tile) throws IOException {
        out.writeObject(Message.update(Message.OP_SET_CENTRAL_TILE, tile));
        out.flush();
    }

    @Override
    public void setIsDemo(Boolean demo) throws Exception {
        out.writeObject(Message.update(Message.OP_SET_IS_DEMO, demo));
        out.flush();
    }

    @Override
    public void updateDashMatrix(String[][] data) throws Exception {
        out.writeObject(Message.update(Message.OP_UPDATE_DA, data));
        out.flush();
    }

    @Override
    public boolean askWithTimeout(String question) throws IOException {
        Message req = Message.request(Message.OP_ASK_TO, question);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_ASK_TIMEOUT", e);
        }
        return (Boolean) resp.getPayload();
    }

    @Override
    public int[] askCoordsWithTimeout() throws IOException {
        Message req = Message.request(Message.OP_COORDINATE_TO, null);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_COORDS_TIMEOUT", e);
        }
        return (int[]) resp.getPayload();
    }

    @Override
    public Integer askIndexWithTimeout() throws IOException {
        Message req = Message.request(Message.OP_INDEX_TO, null);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_INDEX_TIMEOUT", e);
        }
        return (Integer) resp.getPayload();
    }


    @Override
    public Boolean ask(String question) throws IOException {
        // identico a askWithTimeout
        Message req = Message.request(Message.OP_ASK, question);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_ASK", e);
        }
        return (Boolean) resp.getPayload();
    }

    @Override
    public Integer askIndex() throws IOException {
        Message req = Message.request(Message.OP_INDEX, null);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_INDEX", e);
        }
        return (Integer) resp.getPayload();
    }

    @Override
    public int[] askCoordinate() throws IOException {
        Message req = Message.request(Message.OP_COORDINATE, null);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_COORDINATE", e);
        }
        return (int[]) resp.getPayload();
    }

    @Override
    public String askString() throws IOException {
        Message req = Message.request(Message.OP_STRING, null);
        out.writeObject(req);
        out.flush();

        Message resp;
        try {
            resp = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid response for OP_STRING", e);
        }
        return (String) resp.getPayload();
    }


}

