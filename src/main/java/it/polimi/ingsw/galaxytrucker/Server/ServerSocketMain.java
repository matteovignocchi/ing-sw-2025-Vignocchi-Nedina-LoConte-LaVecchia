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

public class ServerSocketMain implements Runnable {
    private static final Logger log = Logger.getLogger(ServerSocketMain.class.getName());
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final GameManager gameManager;
    private final int port;
    private ServerSocket serverSocket;

    public ServerSocketMain(GameManager gameManager, int port) {
        this.gameManager = gameManager;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1_000);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientPool.submit(new ClientHandler(clientSocket, gameManager));
                } catch (SocketTimeoutException e) {
                    // nessuna azione: serve solo a tornare a while() e controllare isInterrupted() ogni secondo
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error in server socket setup or accept loop", e);
        } finally {
            shutdown();
        }
    }

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
