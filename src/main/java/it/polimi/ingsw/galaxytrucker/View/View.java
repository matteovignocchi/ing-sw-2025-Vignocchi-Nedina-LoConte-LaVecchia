package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;


public interface View {

    void inform(String message);
    boolean ask(String message);
    int[] askCordinate();
    int askIndex();
    void setInt();
    void start();
    void printFirePower(float power);
    void printEnginePower(int power);
    void printNumOfCredits(int credits);
    void printListOfGoods(List<Colour> Goods);
    void printListOfTiles(List<Tile> Tiles);
    void printDashShip(Tile[][] ship);
    void printBonusBrown(boolean bonusBrown);
    void printBonusPurple(boolean bonusPurple);
    void printNewFase(GameFase gameFase);
    void printLap(int i);
    void printPileCovered(List<Tile> tiles);
    void printPileShown(List<Tile> tiles);
    String askString();
    void reportError(String message);
    void updateState(GameFase gameFase);
    void printTile(Tile tile);
    void printCard(Card card);
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