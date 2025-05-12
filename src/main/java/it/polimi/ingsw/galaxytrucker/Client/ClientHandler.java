package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualViewAdapter;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
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

        // APRI PRIMA L'OUTPUT, POI L'INPUT per evitare deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                Message req = (Message) in.readObject();

                // solo TYPE_REQUEST è accettata
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

            switch (op) {
                case Message.OP_LEAVE_GAME:         return handleLeaveGame(p);
                case Message.OP_LOGIN:              return handleLogin(p);
                case Message.OP_CREATE_GAME:        return handleCreateGame(p);
                case Message.OP_LIST_GAMES:         return handleListGames();
                case Message.OP_ENTER_GAME:         return handleEnterGame(p);
                case Message.OP_GET_TILE:           return handleGetTile(p);
                case Message.OP_GET_UNCOVERED:      return handleGetUncovered(p);
                case Message.OP_GET_UNCOVERED_LIST: return handleGetUncoveredList(p);
                case Message.OP_RETURN_TILE:        return handleReturnTile(p);
                case Message.OP_POSITION_TILE:      return handlePositionTile(p);
                case Message.OP_GET_CARD:           return handleDrawCard(p);
                case Message.OP_ROTATE_GLASS:       return handleRotateGlass(p);
                case Message.OP_SET_READY:          return handleSetReady(p);
                case Message.OP_LOOK_DECK:          return handleLookDeck(p);
                case Message.OP_LOOK_SHIP:          return handleLookShip(p);
                case Message.OP_LOGOUT:             return handleLogout(p);
                default:
                    return Message.error("Unknown operation: " + op);
            }
        } catch (BusinessLogicException e) {
            return Message.error("Logic error: " + e.getMessage());
        } catch (IOException e) {
            return Message.error("I/O error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during dispatch: " + e.getMessage());
            return Message.error("Unexpected error: " + e.getMessage());
        }
    }

    private Message handleLogin(Object p) throws BusinessLogicException {
        String nickname = (String) p;
        gameManager.login(nickname);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleCreateGame(Object p) throws BusinessLogicException, Exception {
        List<Object> payload = (List<Object>) p;
        boolean isDemo = (Boolean) payload.get(0);
        String nickname = (String)  payload.get(1);
        int maxPlayers = (Integer) payload.get(2);
        int gameId = gameManager.createGame(isDemo,this, nickname, maxPlayers);
        return Message.response(gameId);
    }

    private Message handleListGames() {
        Map<Integer,int[]> map = gameManager.listActiveGames();
        return Message.response(map);
    }

    @SuppressWarnings("unchecked")
    private Message handleEnterGame(Object p) throws BusinessLogicException, Exception {
        List<Object> args = (List<Object>) p;
        int gameId = (Integer) args.get(0);
        String nickname = (String)  args.get(1);
        gameManager.joinGame(gameId,this, nickname);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleGetTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        Tile t = gameManager.getCoveredTile(gameId, nickname);
        return Message.response(t);
    }

    @SuppressWarnings("unchecked")
    private Message handleGetUncovered(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        int idTile = (Integer) payload.get(2);
        Tile t = gameManager.chooseUncoveredTile(gameId, nickname, idTile);
        return Message.response(t);
    }

    @SuppressWarnings("unchecked")
    private Message handleGetUncoveredList(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        List<Tile> list = gameManager.getUncoveredTilesList(gameId, nickname);
        return Message.response(list);
    }

    @SuppressWarnings("unchecked")
    private Message handleReturnTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        Tile tile = (Tile) payload.get(2);
        gameManager.dropTile(gameId, nick, tile);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handlePositionTile(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        Tile tile = (Tile) payload.get(2);
        int[] coordinate = (int[]) payload.get(3);
        gameManager.placeTile(gameId, nick, tile, coordinate);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleDrawCard(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.drawCard(gameId, nickname);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleRotateGlass(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.flipHourglass(gameId, nick);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleSetReady(Object p) throws BusinessLogicException, RemoteException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nick = (String) payload.get(1);
        gameManager.setReady(gameId, nick);
        return Message.response("OK");
    }

    @SuppressWarnings("unchecked")
    private Message handleLookDeck(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        int idxDeck = (Integer) payload.get(1);
        List<Card> deck = gameManager.showDeck(gameId, idxDeck);
        return Message.response(deck);
    }

    @SuppressWarnings("unchecked")
    private Message handleLookShip(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        Tile [][] ship = gameManager.lookAtDashBoard(nickname, gameId);
        return Message.response(ship);
    }

    @SuppressWarnings("unchecked")
    private Message handleLeaveGame(Object p) throws BusinessLogicException {
        List<Object> payload = (List<Object>) p;
        int gameId = (Integer) payload.get(0);
        String nickname = (String) payload.get(1);
        gameManager.quitGame(gameId, nickname);
        return Message.response("OK");
    }

    private Message handleLogout(Object p) throws BusinessLogicException {
        String nickname = (String) p;
        gameManager.logout(nickname);
        return Message.response("OK");
    }

    @Override
    public void inform(String message) throws IOException {
        out.writeObject(Message.request(Message.TYPE_NOTIFICATION, message));
        out.flush();
    }

    @Override
    public void reportError(String error) throws IOException {
        out.writeObject(Message.error(error));
        out.flush();
    }

    @Override
    public void updateGameState(GamePhase fase) throws IOException {
        out.writeObject(Message.update(Message.OP_GAME_PHASE, fase));
        out.flush();
    }

    @Override
    public void showUpdate(
            String nickname,
            double firePower,
            int powerEngine,
            int credits,
            int position,
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
                position,
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
    public void printCard(Card card) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_CARD, card));
        out.flush();
    }

    @Override
    public void printListOfTileCovered(List<Tile> tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_COVERED, tiles));
        out.flush();
    }

    @Override
    public void printListOfTileShown(List<Tile> tiles) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_SHOWN, tiles));
        out.flush();
    }

    @Override
    public void printListOfGoods(List<Colour> goods) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_GOODS, goods));
        out.flush();
    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DASHBOARD, dashboard));
        out.flush();
    }

    @Override
    public void printDeck(List<Card> deck) throws IOException {
        out.writeObject(Message.update(Message.OP_PRINT_DECK, deck));
        out.flush();
    }

    @Override
    public void printTile(Tile tile) throws IOException {
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
        out.writeObject(Message.update(Message.OP_MAP_POSITION, map));
        out.flush();
    }

    @Override
    public void setFlagStart() throws IOException {
        out.writeObject(Message.update(Message.OP_SET_FLAG_START, null));
        out.flush();
    }

    @Override
    public void setStart() throws IOException {
        out.writeObject(Message.update(Message.OP_START_GAME, null));
        out.flush();
    }

}

