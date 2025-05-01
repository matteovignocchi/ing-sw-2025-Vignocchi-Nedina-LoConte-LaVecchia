package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;
import java.util.Map;


public interface View {

    void inform(String message);
    boolean ask(String message);
    int[] askCordinate();
    int askIndex();
    void setInt();
    void start();
    void printListOfGoods(List<Colour> Goods);
    void printDashShip(Tile[][] ship);
    void updateView(String nickname, double firePower, int powerEngine, int credits, int position, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy); //metodo poi per gui per vedere ordine di gioco (mappa position e stirnga del player)
    void printNewFase(GameFase gameFase);
    void printDeck(List<Card> deck);
    void printPileCovered();
    void printPileShown(List<Tile> tiles);
    String askString();
    void reportError(String message);
    void updateState(GameFase gameFase);
    void printTile(Tile tile);
    void printCard(Card card);
    String sendAvailableChoices() throws Exception;
    void updateMap(Map<String, Integer> map);
    String choosePlayer();

    //meetodo per richiedere


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