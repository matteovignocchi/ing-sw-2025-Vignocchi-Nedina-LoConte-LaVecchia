package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;


public interface InterfaceView {

    public void inform(String message);
    public boolean ask(String message);
    public int[] askCordinate();
    public int askindex();
    public int setint();

    public void printFirePower(float power);
    public void printEnginePower(int power);
    public void printNumOfCredits(int credits);
    public void printListOfGoods(List<Colour> Goods);
    public void printDashShip(Tile[][] ship);
    public void printBonusBrown(boolean bonusBrown);
    public void printBonuspurple(boolean bonuspurple);
    public void printList(String key,List<Object> list);
    public void printNewFase(GameFase gameFase);
    public void printLap(int i);


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
//}