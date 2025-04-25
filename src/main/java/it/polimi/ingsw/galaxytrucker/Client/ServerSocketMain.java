package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketMain implements Runnable {
    private final GameManager gameManager;
    private final int port;

    public ServerSocketMain(GameManager gameManager, int port) {
        this.gameManager = gameManager;
        this.port = port;
    }

    @Override
    public void run() {
        try(ServerSocket server = new ServerSocket(port)) {
            while(true){
                Socket clientSocket = server.accept();
                new Thread (new ClientHandler(clientSocket, gameManager)).start();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
