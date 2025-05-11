package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.Message;
import it.polimi.ingsw.galaxytrucker.Server.VirtualClientSocket;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

        // APRI PRIMA L'OUTPUT, POI L'INPUT per evitare deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());

        // CREO LA VIEW lato server: useremo solo out per inviare UPDATE/NOTIFY
        this.view = new VirtualClientSocket(this.out);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                // 1) LEGGO la richiesta dal client
                Message request = (Message) in.readObject();
                // 2) DISPATCH al GameManager, ottenendo una response
                Message response = dispatch(request);
                // 3) SCRIVO la response indietro al client
                out.writeObject(response);
                out.flush();
            }
        } catch (EOFException eof) {
            // Client chiude la connessione volontariamente
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore socket con "
                    + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            // Assicuro la chiusura della socket
            try { socket.close(); } catch (IOException ignored) {}
            // Se vogliamo, qui possiamo interrompere il thread:
            Thread.currentThread().interrupt();
        }
    }

    private Message dispatch(Message req) {
        try {
            String op = req.getOperation();
            Object p  = req.getPayload();

            switch (op) {
                case Message.OP_LOGOUT: {
                    String nick = (String) p;
                    int newGameId = gameManager.createGame(false, view, nick, 4);
                    return Message.response(newGameId);
                }
                case Message.OP_LIST_GAMES: {
                    Map<Integer,int[]> map = gameManager.listActiveGames();
                    return Message.response(map);
                }
                case Message.OP_ENTER_GAME: {
                    Object[] args = (Object[]) p;
                    int    gameId = (Integer) args[0];
                    String nick2  = (String)  args[1];
                    gameManager.joinGame(gameId, view, nick2);
                    return Message.response("OK");
                }
                // … gestisci qui tutti gli altri OP_* …
                default:
                    return Message.error("unknown operation: " + op);
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

