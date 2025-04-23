package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class VirtualClientSocket implements Runnable, VirtualView {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final View view;
    private GameFase gameFase;
    private String lastResponse;

    public VirtualClientSocket(String host, int port , View view) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.view = view;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try{
            while(true){
                Object received = in.readObject();
                switch(received){
                    case GameFase g ->
                        gameFase = (GameFase) received;
                    case String s ->{
                        this.lastResponse = (String) received;
                        this.notifyAll();
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + received);
                }
            }
        } catch (IOException e) {
            view.reportError("CONNECTION ERROR: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            view.reportError("COMMUNICATION ERROR: " + e.getMessage());
        }

    }

    @Override
    public void inform(String message) throws Exception {

    }

    @Override
    public void showUpdate() throws Exception {

    }

    @Override
    public void reportError(String error) throws Exception {

    }

    @Override
    public boolean askDecision() throws Exception {
       return true;
    }

    @Override
    public int askIndex() throws Exception {
     return 2;
    }

    @Override
    public int[] askCoordinates() throws Exception {
      return new int[] {1, 2};
    }

    @Override
    public void printList(List<Objects> pile) throws Exception {

    }

    @Override
    public void setFase(GameFase fase) throws Exception {

    }

    @Override
    public void printCard(Card card) throws Exception {

    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws Exception {

    }

    @Override
    public String askString() throws Exception {
        return "";
    }

    @Override
    public void startMach() throws Exception {

    }

    @Override
    public boolean sendLogin(String username, String password) throws Exception {
        out.writeObject(new LoginRequest(username , password));
        return Boolean.parseBoolean(waitForResponce());

    }

    @Override
    public void sendGameRequest(String message) throws Exception {

    }

    @Override
    public  synchronized String waitForResponce() throws Exception {
        while (lastResponse == null) wait();
        String response = lastResponse;
        lastResponse = null;
        return response;
    }

    @Override
    public String waitForGameUpadate() throws Exception {
        return "";
    }

    @Override
    public List<String> requestGameList() throws Exception {
        return List.of();
    }

    @Override
    public List<String> getAvailableAction() throws Exception {
        return List.of();
    }

    @Override
    public void sendAction(int key) throws Exception {
//        out.writeObject(new ActionRequest(message) );
    }

    @Override
    public GameFase getCurrentGameState() throws Exception {
        return null;
    }

}
