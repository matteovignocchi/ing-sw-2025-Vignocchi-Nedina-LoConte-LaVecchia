package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VirtualView extends Remote {

    /// METODI PER FARE LE PRINT A SCHERMO ///
    void inform(String message) throws Exception;

    void showUpdate(String nickname, double firePower, int powerEngine, int credits, /*int position,*/ boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc

    void reportError(String error) throws Exception;

    Boolean ask(String message) throws Exception;

    void printCard(String card) throws Exception;

    void printListOfTileCovered(String tiles) throws Exception;

    void printListOfTileShown(String tiles) throws Exception;

    void printPlayerDashboard(String[][] dashboard) throws Exception;

    void printListOfGoods(List<String> listOfGoods) throws Exception;

    void printTile(String tile) throws Exception;

    void printDeck(String deck) throws Exception;

//    void setView(View view) throws Exception;

    void setGameId(int gameId) throws RemoteException;

    /// METODI PER RICHIEDERE COSE ///

    Integer askIndex() throws Exception;

    String askString() throws Exception;

    int[] askCoordinate() throws Exception;

    /// METODI PER AVERE INFORMAZIONI SULLO STATO DEL GIOCO///
    void updateGameState(String fase) throws Exception;

    void startMach() throws Exception;

    int sendLogin(String username) throws Exception;

    int sendGameRequest(String message ,int numberOfPlayer , Boolean isdemo) throws Exception;

//    GamePhase getCurrentGameState() throws Exception;
//
//    GamePhase getGameFase() throws Exception;

    /// METODI CHE CHIAMO DIRETTAMENTE AL SERVER ///
    String getTileServer() throws Exception;

    String getUncoveredTile() throws Exception;

    void getBackTile(String tile) throws Exception;

    void positionTile(String tile) throws Exception;

    void drawCard() throws Exception;

    void rotateGlass() throws Exception;

    void setReady() throws Exception;

    void lookDeck() throws Exception;

    void lookDashBoard() throws Exception;

    void logOut() throws Exception;

    void setNickname(String nickname) throws Exception;

    void updateMapPosition(Map<String, Integer> Position) throws Exception;

    void setStart() throws Exception;

    String askInformationAboutStart() throws Exception;

    void setTile(String jsonTile) throws Exception;

    void setIsDemo(Boolean demo) throws Exception;

    void enterGame(int gameId) throws Exception;

    void leaveGame() throws Exception;

    String takeReservedTile() throws Exception;

    void updateDashMatrix(String[][] data) throws Exception;

    void setClientController(ClientController clientController) throws Exception;

    boolean askWithTimeout(String question) throws Exception;

    int[] askCoordsWithTimeout() throws Exception;

    Integer askIndexWithTimeout() throws Exception;
}
