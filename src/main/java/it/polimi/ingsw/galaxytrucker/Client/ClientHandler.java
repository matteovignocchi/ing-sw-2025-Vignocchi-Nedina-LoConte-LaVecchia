package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.Message;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientSocket;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameManager gameManager;
    private final VirtualClientSocket view;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ClientHandler(Socket socket, GameManager gameManager) throws IOException {
        this.socket      = socket;
        this.gameManager = gameManager;
        // 1) apro lo stream di output PRIMA di quello di input
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in  = new ObjectInputStream(socket.getInputStream());
        // 2) creo la view lato server usando lo stesso out
        this.view = new VirtualClientSocket(this.out);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message request = (Message) in.readObject();
                Message response = dispatch(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (EOFException eof) {
            System.out.println("Client disconnesso: " + socket.getRemoteSocketAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore socket con " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            try { socket.close(); }
            catch (IOException ignored) {}
        }
    }

    private Message dispatch(Message req) {
        String op = req.getOperation();
        Object p  = req.getPayload();

        try {
            switch (op) {
                case Message.OP_LOGIN: {

                }
                case Message.OP_LIST_GAMES: {
                    Map<Integer,int[]> map = gameManager.listActiveGames();
                    return Message.response(map);
                }
                case Message.OP_ENTER_GAME: {
                    // payload = Object[]{ Integer gameId, String nickname }
                    Object[] arr = (Object[]) p;
                    int    gid  = (Integer) arr[0];
                    String nick2= (String)  arr[1];
                    gameManager.joinGame(gid, view, nick2);
                    gameManager.notifyAllViews();
                    return Message.response("OK");
                }
                case Message.OP_GET_TILE: {
                    int    gid      = view.getGameId();
                    String nickname = view.getNickname();
                    Tile t = gameManager.getCoveredTile(gid, nickname);
                    return Message.response(t);
                }
                case Message.OP_GET_UNCOVERED: {
                    int gid        = view.getGameId();
                    String nickn   = view.getNickname();
                    List<Tile> lst = gameManager.getUncoveredTilesList(gid, nickn);
                    return Message.response(lst);
                }
                case Message.OP_PRINT_SHOWN: {
                    // alias di LOOK_SHOWN?
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    List<Tile> lst = gameManager.getUncoveredTilesList(gid, nickn);
                    return Message.response(lst);
                }
                case Message.OP_CHOOSE_TILE: { // custom op
                    Object[] arr = (Object[]) p;
                    int idTile   = (Integer) arr[0];
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    Tile chosen = gameManager.chooseUncoveredTile(gid, nickn, idTile);
                    return Message.response(chosen);
                }
                case Message.OP_DROP_TILE: {
                    Object[] arr = (Object[]) p;
                    Tile tile    = (Tile) arr[0];
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.dropTile(gid, nickn, tile);
                    return Message.response("OK");
                }
                case Message.OP_PLACE_TILE: {
                    Object[] arr = (Object[]) p;
                    Tile tile    = (Tile) arr[0];
                    int[] cord   = (int[])  arr[1];
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.placeTile(gid, nickn, tile, cord);
                    return Message.response("OK");
                }
                case Message.OP_SET_READY: {
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.setReady(gid, nickn);
                    return Message.response("OK");
                }
                case Message.OP_ROTATE_GLASS: {
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.flipHourglass(gid, nickn);
                    return Message.response("OK");
                }
                case Message.OP_SHOW_DECK: {
                    // payload = Integer idxDeck
                    int idxDeck  = (Integer) p;
                    int gid      = view.getGameId();
                    List<Card> deck = gameManager.showDeck(gid, idxDeck);
                    return Message.response(deck);
                }
                case Message.OP_LOOK_SHIP: {
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    Tile[][] board = gameManager.lookAtDashBoard(gid, nickn);
                    return Message.response(board);
                }
                case Message.OP_DRAW_CARD: {
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.drawCard(gid, nickn);
                    return Message.response("OK");
                }
                case Message.OP_LIST_GAMES: {
                    // già gestito
                    return Message.response(gameManager.listActiveGames());
                }
                case Message.OP_LOGOUT: {
                    int gid      = view.getGameId();
                    String nickn = view.getNickname();
                    gameManager.quitGame(gid, nickn);
                    return Message.response("OK");
                }
                default:
                    return Message.error("Not valid operation: " + op);
            }

        } catch (BusinessLogicException e) {
            return Message.error("Logic error: " + e.getMessage());
        } catch (IOException e) {
            return Message.error("I/O error: " + e.getMessage());
        } catch (Exception e) {
            return Message.error("Unexpected error: " + e.getMessage());
        }
    }
}

