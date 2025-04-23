package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;

public interface VirtualView {
    void inform(String message) throws Exception;
    void showUpdate() throws Exception; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc
    void reportError(String error) throws Exception;
    boolean ask(String message) throws Exception;
    int askIndex() throws Exception;
    int[] askCoordinates() throws Exception;
    void updateGameState(GameFase fase) throws Exception;
    void printCard(Card card) throws Exception;
    void printListOfTileCovered(List<Tile> tiles) throws Exception;
    void printListOfTileShown(List<Tile> tiles) throws Exception;
    void printPlayerDashboard(Tile[][] dashboard) throws Exception;
    String askString() throws Exception;
    void startMach() throws Exception;
    public boolean sendRegistration(String username, String password) throws Exception;
    public boolean sendLogin(String username, String password) throws Exception;
    void sendGameRequest(String message) throws Exception;
    String waitForResponce() throws Exception;
    String waitForGameUpadate() throws Exception;
    List<String> requestGameList() throws Exception;
    List<String> getAvailableAction() throws Exception;
    List<Tile> getPileOfTile() throws Exception;
    void sendAction(int key) throws Exception;
    GameFase getCurrentGameState() throws Exception;

    Tile getTile() throws Exception;


    int[] askCoordinate() throws Exception;

    void printListOfGoods(List<Colour> listOfGoods) throws Exception;
}
