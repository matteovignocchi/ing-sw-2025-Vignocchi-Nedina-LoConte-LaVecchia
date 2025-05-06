package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import javafx.application.Platform;

import java.util.List;
import java.util.Map;

public class GUIView implements View {
    private Stage gameStage;

    public GUIView(){
        Platform.startup(()->{});
    }





    @Override
    public void inform(String message) {

    }

    @Override
    public boolean ask(String message) {
        return false;
    }

    @Override
    public int[] askCordinate() {
        return new int[0];
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
    public void printListOfGoods(List<Colour> Goods) {

    }

    @Override
    public void printDashShip(Tile[][] ship) {

    }

    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, int position, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {

    }

    @Override
    public void printNewFase(GamePhase gamePhase) {

    }

    @Override
    public void printDeck(List<Card> deck) {

    }

    @Override
    public void printPileCovered() {

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
    public void updateState(GamePhase gamePhase) {

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

    @Override
    public void updateMap(Map<String, Integer> map) {

    }

    @Override
    public String choosePlayer() {
        return "";
    }
}
