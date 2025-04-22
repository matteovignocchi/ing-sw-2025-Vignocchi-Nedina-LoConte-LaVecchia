package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;

public class GUIView implements View {
    @Override
    public void inform(String message) {

    }

    @Override
    public boolean ask() {
        return false;
    }


    @Override
    public int[] askCordinate() {

    }

    @Override
    public int askindex() {

    }

    @Override
    public void setInt() {

    }

    @Override
    public void start() {

    }

    @Override
    public void printFirePower(float power) {

    }

    @Override
    public void printEnginePower(int power) {

    }

    @Override
    public void printNumOfCredits(int credits) {

    }

    @Override
    public void printListOfGoods(List<Colour> Goods) {

    }

    @Override
    public void printDashShip(Tile[][] ship) {

    }

    @Override
    public void printBonusBrown(boolean bonusBrown) {

    }

    @Override
    public void printBonusPurple(boolean bonusPurple) {

    }

    @Override
    public void printList(String key, List<Object> list) {

    }

    @Override
    public void printNewFase(GameFase gameFase) {

    }

    @Override
    public void printLap(int i) {

    }

    @Override
    public String askString() {
        return "";
    }

    @Override
    public void reportError(String message) {

    }

    @Override
    public void updateState(GameFase gameFase) {

    }
}
