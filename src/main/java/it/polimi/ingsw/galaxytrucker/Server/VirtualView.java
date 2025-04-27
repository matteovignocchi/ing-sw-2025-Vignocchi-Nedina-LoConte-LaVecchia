package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.util.List;

public interface VirtualView {

    /// METODI PER FARE LE PRINT A SCHERMO ///
    void inform(String message);
    void showUpdate(String nickname, Double firePower, int powerEngine, int credits, int position, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws Exception; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc
    void reportError(String error) throws Exception;
    boolean ask(String message) throws Exception;
    void printCard(Card card) throws Exception;
    void printListOfTileCovered(List<Tile> tiles) throws Exception;
    void printListOfTileShown(List<Tile> tiles) throws Exception;
    void printPlayerDashboard(Tile[][] dashboard) throws Exception;
    void printListOfGoods(List<Colour> listOfGoods) throws Exception;
    void printTile(Tile tile) throws Exception;
    void printDeck(List<Card> deck) throws Exception;

    /// METODI PER RICHIEDERE COSE ///
    int askIndex() throws Exception;
    String askString() throws Exception;
    int[] askCoordinate() throws Exception;

    /// METODI PER AVERE INFORMAZIONI SULLO STATO DEL GIOCO///
    void updateGameState(GameFase fase);
    void startMach() throws Exception;
    boolean sendLogin(String username, String password) throws Exception;
    boolean sendGameRequest(String message) throws Exception;
    Object waitForResponce() throws Exception;
    String waitForGameUpadate() throws Exception;
    GameFase getCurrentGameState() throws Exception;
    GameFase getGameFase() throws Exception;

    /// METODI CHE CHIAMO DIRETTAMENTE AL SERVER ///
    Tile getTileServer() throws Exception;
    Tile getUncoveredTile() throws Exception;
    void getBackTile(Tile tile) throws Exception;
    void positionTile(Tile tile) throws Exception;
    void drawCard() throws Exception;
    void rotateGlass() throws Exception;
    void setReady() throws Exception;
    void lookDeck() throws Exception;
    void lookDashBoard() throws Exception;
    void logOut() throws Exception;
    void setNickname(String nickname) throws Exception;
}
