package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;


public interface View {

    public void inform(String message);
    public void ask(String message);
    public void askCordinate();
    public void askindex();
    public void setint();

    public void printFirePower(float power);
    public void printEnginePower(int power);
    public void printNumOfCredits(int credits);
    public void printListOfGoods(List<Colour> Goods);
    public void printDashShip(Tile[][] ship);
    public void printBonusBrown(boolean bonusBrown);
    public void printBonusPurple(boolean bonusPurple);
    public void printList(String key,List<Object> list);
    public void printNewFase(GameFase gameFase);
    public void printLap(int i);
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