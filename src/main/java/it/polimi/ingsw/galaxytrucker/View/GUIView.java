package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;

public class GUIView implements View {
    @Override
    public void inform(String message) {

    }

    @Override
    public boolean ask(String message) {
        return false;
    }


    @Override
    public int[] askCordinate() {
     return null;
    }

    @Override
    public int askIndex() {
return 0;
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
    public void printListOfTiles(List<Tile> Tiles) {

    }

    @Override
    public void printDashShip(Tile[][] ship) {

    }

    @Override
    public void updateView(String nickname, Float firePower, int powerEngine, int credits, int position, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {

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
    public void printDeck(List<Card> deck) {

    }

    @Override
    public void printLap(int i) {

    }

    @Override
    public void printPileCovered(List<Tile> tiles) {

    }

    @Override
    public void printPileShown(List<Tile> tiles) {

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

    @Override
    public void printTile(Tile tile) {

    }

    @Override
    public void printCard(Card card) {

    }

    @Override
    public String sendAvailableChoices() throws Exception {
        return "";
    }
}
