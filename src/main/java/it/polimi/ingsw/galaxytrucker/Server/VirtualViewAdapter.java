package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public abstract class VirtualViewAdapter implements VirtualView {
    @Override public void inform(String message) throws Exception {}
    @Override public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {}
    @Override public void reportError(String error) throws Exception {}
    @Override public boolean ask(String message) throws Exception { return false; }
    @Override public void printCard(Card card) throws Exception {}
    @Override public void printListOfTileCovered(List<Tile> tiles) throws Exception {}
    @Override public void printListOfTileShown(List<Tile> tiles) throws Exception {}
    @Override public void printPlayerDashboard(Tile[][] dashboard) throws Exception {}
    @Override public void printListOfGoods(List<Colour> listOfGoods) throws Exception {}
    @Override public void printTile(Tile tile) throws Exception {}
    @Override public void printDeck(List<Card> deck) throws Exception {}
    @Override public void setView(View view) throws Exception {}
    @Override public void setGameId(int gameId) throws RemoteException {}
    @Override public void setCentralTile(Tile tile) throws Exception {}

    @Override public void leaveGame() throws Exception{}
    @Override public int askIndex() throws Exception { return 0; }
    @Override public String askString() throws Exception { return null; }
    @Override public int[] askCoordinate() throws Exception { return null; }

    @Override public void updateGameState(GamePhase fase) throws Exception {}
    @Override public void startMach() throws Exception {}
    @Override public int sendLogin(String username) throws Exception { return 0; }
    @Override public int sendGameRequest(String message) throws Exception { return 0; }
    @Override public GamePhase getCurrentGameState() throws Exception { return null; }
    @Override public GamePhase getGameFase() throws Exception { return null; }

    @Override public Tile getTileServer() throws Exception { return null; }
    @Override public Tile getUncoveredTile() throws Exception { return null; }
    @Override public void getBackTile(Tile tile) throws Exception {}
    @Override public void positionTile(Tile tile) throws Exception {}
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
}
