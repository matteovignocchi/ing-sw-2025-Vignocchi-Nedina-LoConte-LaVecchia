package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface View {

    void inform(String message);
    Boolean ask(String message);
    int[] askCoordinate() throws IOException, InterruptedException;
    Integer askIndex() throws IOException, InterruptedException;
    void setInt();
    void start();
    void printListOfGoods(List<String> Goods);
    void printDashShip(ClientTile[][] ship);
    void updateView(String nickname, double firePower, int powerEngine, int credits, /*int position,*/ boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy); //metodo poi per gui per vedere ordine di gioco (mappa position e stirnga del player)
    void printNewFase(String gamePhase);
    void printDeck(List<ClientCard> deck);
    void printPileCovered();
    void printPileShown(List<ClientTile> tiles);
    String askString();
    void reportError(String message);
    void updateState(ClientGamePhase gamePhase);
    void printTile(ClientTile tile);
    void printCard(ClientCard card);
    String sendAvailableChoices() throws Exception;
    void updateMap(Map<String, Integer> map);
    String choosePlayer() throws IOException, InterruptedException;
    void printListOfCommand();
    void setIsDemo(Boolean demo);
    boolean ReturnValidity(int a , int b);
    void setValidity(int a , int b);
    void resetValidity(int a , int b);
    ClientGamePhase getGamePhase();
    void printMapPosition();
    boolean askWithTimeout(String message);

    int[] askCoordinatesWithTimeout();
    void displayAvailableGames(Map<Integer, int[]> availableGames);
}

//public interface VirtualViewRmi extends VirtualView {
//    @Override
//    void showUpdate() throws RemoteException;
//    @Override
//    void reportError(String error) throws RemoteException;
//    @Override
//    void ask(String question) throws RemoteException;
//    @Override
//    void printPileOfTile(List<Tile> pile) throws RemoteException;
//