package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Server.CommunicationException;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, VirtualServer {
    private Socket client;
    private GameManager gameManager;

    public ClientHandler(Socket client, GameManager gameManager) {
        this.client = client;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        try( ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(client.getInputStream()))
        {
            while(true){
                Message request = (Message) in.readObject();
                try{
                    Message response = gameManager.handle(request); //gestione qui o nel gamemanager
                    out.writeObject(response);
                    out.flush();
                } catch (CommunicationException e){
                    e.printStackTrace(); //gestione più robusta ?
                    Message errorMsg = Message.error(e.getMessage()); //formattare bene l'errore secondo il modello
                    out.writeObject(errorMsg);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try{
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //gameManager.notifyClientDisconnected(client); capire come gestire
        }
    }
}
