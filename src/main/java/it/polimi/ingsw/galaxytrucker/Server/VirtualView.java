package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VirtualView extends Remote {

    /// METODI PER FARE LE PRINT A SCHERMO ///
    void inform(String message) throws Exception;

    void showUpdate(String nickname, double firePower, int powerEngine, int credits, /*int position,*/ boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc

    void reportError(String error) throws Exception;

    boolean ask(String message) throws Exception;

    void printCard(Card card) throws Exception;

    void printListOfTileCovered(List<Tile> tiles) throws Exception;

    void printListOfTileShown(List<Tile> tiles) throws Exception;

    void printPlayerDashboard(Tile[][] dashboard) throws Exception;

    void printListOfGoods(List<Colour> listOfGoods) throws Exception;

    void printTile(Tile tile) throws Exception;

    void printDeck(List<Card> deck) throws Exception;

    void setView(View view) throws Exception;

    void setGameId(int gameId) throws RemoteException;

    /// METODI PER RICHIEDERE COSE ///
    int askIndex() throws Exception;

    String askString() throws Exception;

    int[] askCoordinate() throws Exception;

    /// METODI PER AVERE INFORMAZIONI SULLO STATO DEL GIOCO///
    void updateGameState(GamePhase fase) throws Exception;

    void startMach() throws Exception;

    boolean sendLogin(String username) throws Exception;

    int sendGameRequest(String message) throws Exception;

    GamePhase getCurrentGameState() throws Exception;

    GamePhase getGameFase() throws Exception;

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

    void updateMapPosition(Map<String, Integer> Position) throws Exception;

    void setFlagStart() throws Exception;

    void setStart() throws Exception;

    String askInformationAboutStart() throws Exception;
}
