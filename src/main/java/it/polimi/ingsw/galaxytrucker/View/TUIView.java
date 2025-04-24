package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;
import java.util.Scanner;

public class TUIView implements View {
    private int idPlayer;
    private int server;
    private Scanner scanner = new Scanner(System.in);


    //per ora lascio il server come int
    public TUIView()  {

    }
    @Override
    public void inform(String message) {
        System.out.println("> " + message + "\n");
    }
    @Override
    public void reportError(String message) {
        System.err.print("\n[ERROR] " + message + "\n> ");
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
    public boolean ask(String message) {
        boolean flag = true;
        boolean decision = false;
        inform(message+ "(Yes/No)");
        String response = scanner.nextLine().trim().toLowerCase();
        while(flag) {
            if (response.equals("yes")) {
                flag = false;
                decision = true;
            } else if (response.equals("no")) {
                flag = false;
                decision = false;
            }
            else {
                reportError("The response entered is invalid. Try again: ");
            }
        }
        return decision;
    }

    @Override
    public int[] askCordinate() {
        int[] coordinate = new int[2];
        inform("Insert the row:");
        coordinate[0] = scanner.nextInt();
        inform("Insert the column:");
        coordinate[1] = scanner.nextInt();
        return coordinate;
    }

    @Override
    public int askIndex() {
        int index;
        inform("Insert index:");
        index = scanner.nextInt();
        return index;
    }

    @Override
    public String askString() {
        return scanner.nextLine();
    }


   @Override
    public void printFirePower(float power){
        System.out.print("> " + power +"\n");
    }
    @Override

    public void printEnginePower(int x){
        System.out.print("> "+x+"\n");
    }
    @Override
    public void printNumOfCredits(int credits){
        System.out.print("> "+credits+"\n");
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
    public void printListOfTiles(List<Tile> Tiles) {

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
    public void printPileCovered(List<Tile> tiles) {

    }

    @Override
    public void printPileShown(List<Tile> tiles) {

    }


}
