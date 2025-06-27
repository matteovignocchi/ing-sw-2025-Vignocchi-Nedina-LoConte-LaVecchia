package it.polimi.ingsw.galaxytrucker.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main socket-based server that listens for incoming client connections
 * and dispatches each connection to a ClientHandler.
 * @author Francesco Lo Conte
 */
public class ServerSocketMain implements Runnable {
    private static final Logger log = Logger.getLogger(ServerSocketMain.class.getName());
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final GameManager gameManager;
    private final int port;
    private ServerSocket serverSocket;


    /**
     * Constructs the socket server with the specified GameManager and port.
     *
     * @param gameManager the GameManager instance to handle game logic for clients
     * @param port the TCP port on which the server will listen
     */
    public ServerSocketMain(GameManager gameManager, int port) {
        this.gameManager = gameManager;
        this.port = port;
    }

    /**
     * Starts the server loop: opens the ServerSocket, accepts client connections,
     * and submits a new ClientHandler for each accepted socket. The loop
     * checks for interruption every second to allow graceful shutdown.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1_000);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientPool.submit(new ClientHandler(clientSocket, gameManager));
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error in server socket setup or accept loop", e);
        } finally {
            shutdown();
        }
    }

    /**
     * Shuts down the server by closing the ServerSocket and terminating the client thread pool.
     * Waits up to 5 seconds for existing tasks to complete before forcing shutdown.
     */
    private void shutdown() {
        log.info("Shutting down ServerSocketMain...");
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ServerSocketMain: pool and socket closed.");
    }
}
