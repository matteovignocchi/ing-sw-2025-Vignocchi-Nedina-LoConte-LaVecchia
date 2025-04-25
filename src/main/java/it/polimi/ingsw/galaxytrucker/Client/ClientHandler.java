package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, VirtualServer {
    private Socket clientSocket;
    private GameManager gameManager;

    public ClientHandler(Socket clientSocket, GameManager gameManager) {
        this.clientSocket = clientSocket;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            while(true){
                Message request = (Message) in.readObject();
                try{
                    Message response = this.handle(request);
                    out.writeObject(response);
                    out.flush();
                } catch (BusinessLogicException e){
                    Message errorMsg = Message.error(e.getMessage());
                    out.writeObject(errorMsg);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Message handle(Message request) throws BusinessLogicException {
        //gestire i diversi tipi di richieste
    }

}
