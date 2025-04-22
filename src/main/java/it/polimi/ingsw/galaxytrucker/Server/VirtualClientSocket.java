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

    public VirtualClientSocket(String host, int port , View view) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.view = view;
        new Thread(this).start();
    }

    @Override
    public void run() {

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

    }

    @Override
    public int askIndex() throws Exception {

    }

    @Override
    public int[] askCoordinates() throws Exception {

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

}
