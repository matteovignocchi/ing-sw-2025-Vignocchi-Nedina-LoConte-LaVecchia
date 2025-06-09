package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public abstract class VirtualViewAdapter implements VirtualView {
    @Override public void inform(String message) throws Exception {}
    @Override public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {}
    @Override public void reportError(String error) throws Exception {}
    @Override public Boolean ask(String message) throws Exception { return false; }
    @Override public void printCard(String card) throws Exception {}
    @Override public void printListOfTileCovered(String tiles) throws Exception {}
    @Override public void printListOfTileShown(String tiles) throws Exception {}
    @Override public void printPlayerDashboard(String[][] dashboard) throws Exception {}
    @Override public void printListOfGoods(List<String> listOfGoods) throws Exception {}
    @Override public void printTile(String tile) throws Exception {}
    @Override public void printDeck(String deck) throws Exception {}
//    @Override public void setView(View view) throws Exception {}
    @Override public void setGameId(int gameId) throws RemoteException {}
    @Override public void setTile(String tile) throws Exception {}

    @Override public void leaveGame() throws Exception{}
    @Override public Integer askIndex() throws Exception { return 0; }
    @Override public String askString() throws Exception { return null; }
    @Override public int[] askCoordinate() throws Exception { return null; }

    @Override public void updateGameState(String fase) throws Exception {}
    @Override public void startMach() throws Exception {}
    @Override public int sendLogin(String username) throws Exception { return 0; }
    @Override public int sendGameRequest(String message , int num , Boolean isDemo) throws Exception { return 0; }
//    @Override public String getCurrentGameState() throws Exception { return null; }
//    @Override public String getGameFase() throws Exception { return null; }

    @Override public String getTileServer() throws Exception { return null; }
    @Override public String getUncoveredTile() throws Exception { return null; }
    @Override public void getBackTile(String tile) throws Exception {}
    @Override public void positionTile(String tile) throws Exception {}
    @Override public void drawCard() throws Exception {}
    @Override public void rotateGlass() throws Exception {}
    @Override public void setReady() throws Exception {}
    @Override public void lookDeck() throws Exception {}
    @Override public void lookDashBoard() throws Exception {}
    @Override public void logOut() throws Exception {}
    @Override public void setNickname(String nickname) throws Exception {}
    @Override public void updateMapPosition(Map<String, Integer> Position) throws Exception {}
//    @Override public void setFlagStart() throws Exception {}
    @Override public void setStart() throws Exception {}
    @Override public String askInformationAboutStart() throws Exception { return null; }
    @Override public void setIsDemo(Boolean demo) throws Exception {};
    @Override public void enterGame(int gameId) throws Exception {};
    @Override public void updateDashMatrix(String[][] dashMatrix) throws Exception {}
    @Override public String takeReservedTile() throws Exception { return null; }
    @Override public void setClientController(ClientController clientController) throws Exception{}
}
