package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DummyView implements View {

    private boolean isDemo;

    public DummyView(boolean isDemo) {
        this.isDemo = isDemo;
    }

    @Override
    public void inform(String message) {
        // Simula la visualizzazione di un messaggio
        System.out.println(message);
    }

    @Override
    public boolean ask(String message) {
        return false;
    }

    @Override
    public int[] askCoordinate() {
        return new int[0];
    }

    @Override
    public int askIndex() {
        // Simula l'input dell'utente per un numero
        // In un test, possiamo far ritornare un numero predeterminato
        // Simuliamo un input corretto, ad esempio "1"
        return 1; // Modificabile per testare diverse opzioni
    }

    @Override
    public void setInt() {

    }

    @Override
    public void start() {
        inform("Welcome to the Galaxy Trucker!");
        inform("This is the conversion table regarding our gameplay and display conventions.");
        inform("The tile is presented with four numbers, one for each direction, representing either the number of connectors or special parts of the tile, which we will explain later.");
        inform("Each tile contains an acronym that indicates its type.");
        inform("These are the numbers that the sides of a tile can take:");
        inform("0 means no connectors");
        inform("1 means one connector");
        inform("2 means two connectors");
        inform("3 means universal connector");
        inform("4 means single cannon");
        inform("5 means double cannon");
        inform("6 means single rocket");
        inform("7 means double rocket");
        inform("These are the acronyms for each type of tile:");
        inform("HU stands for housing unit, when it is white is for humans, when it is"+" purple" +" it is for humans or the purple alien, when it is"+ " brown"+" it is for humans or brown alien. After HU there is the counter of tokens on the unit");
        inform("EC stands for energy cell, after that there is the number of tokens on the cell");
        inform("CAN stands for cannon, the side with 4 (single cannon) or 5 (double cannon), it indicates the direction the cannon is facing");
        inform("ENG stands for engine, the side with 6 (single engine) or 7 (double engine), it indicates the direction the engine is facing");
        inform("MTJ stands for multi joint");
        inform("SHL stands for shield, the green connectors are the protected side of the shield");
        inform("SU stands for storage unit, when is white is the standard unit, when it is"+" red"+" it is advanced, after that there is the max capacity of the unit. In each corner, there is a counter showing how many goods of that color are present.");
    }

    @Override
    public void printListOfGoods(List<Colour> Goods) {

    }

    @Override
    public void printDashShip(Tile[][] ship) {

    }

    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {

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
    public String sendAvailableChoices() {
        // Simula l'ottenimento delle scelte disponibili
        List<String> options = commandConstructor();
        inform("Insert the command number");
        // Per simulare l'input, ritorniamo il primo elemento dell'elenco
        return options.get(0).trim().toLowerCase();  // Modificabile per simulare scelte diverse
    }

    @Override
    public void updateMap(Map<String, Integer> map) {

    }

    @Override
    public String choosePlayer() {
        return "";
    }


    public void printListOfCommand() {
        List<String> options = commandConstructor();
        inform("Possible actions:");
        for (int i = 0; i < options.size(); i++) {
            inform((i + 1) + ": " + options.get(i));
        }
    }

    @Override
    public void setIsDemo(Boolean demo) {

    }

    @Override
    public boolean ReturnValidity(int a, int b) {
        return false;
    }

    @Override
    public void setValidity(int a, int b) {

    }

    // Metodo per costruire le opzioni in base alla fase del gioco
    private List<String> commandConstructor() {
        List<String> listOfOptions = new ArrayList<>();
        // Simulazione di varie opzioni, a seconda dello stato del gioco
        listOfOptions.add("Get a covered tile");
        listOfOptions.add("Get a shown tile");
        listOfOptions.add("Declare Ready");
        listOfOptions.add("Watch a player's ship");
        if (!isDemo) {
            listOfOptions.add("Watch a deck");
            listOfOptions.add("Spin the hourglass");
        }
        listOfOptions.add("LogOut");
        return listOfOptions;
    }

    // Aggiungi altri metodi dummy se necessario per i test
}
