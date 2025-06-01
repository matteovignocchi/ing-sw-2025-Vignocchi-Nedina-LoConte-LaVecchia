package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

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
    void printListOfGoods(List<Colour> Goods);
    void printDashShip(Tile[][] ship);
    void updateView(String nickname, double firePower, int powerEngine, int credits, /*int position,*/ boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy); //metodo poi per gui per vedere ordine di gioco (mappa position e stirnga del player)
    void printNewFase(GamePhase gamePhase);
    void printDeck(List<Card> deck);
    void printPileCovered();
    void printPileShown(List<Tile> tiles);
    String askString();
    void reportError(String message);
    void updateState(GamePhase gamePhase);
    void printTile(Tile tile);
    void printCard(Card card);
    String sendAvailableChoices() throws Exception;
    void updateMap(Map<String, Integer> map);
    String choosePlayer() throws IOException, InterruptedException;
    void printListOfCommand();
    void setIsDemo(Boolean demo);
    boolean ReturnValidity(int a , int b);
    void setValidity(int a , int b);
    GamePhase getGamePhase();
    void printMapPosition();
    boolean askWithTimeout(String message);
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