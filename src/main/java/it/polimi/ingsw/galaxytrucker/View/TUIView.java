package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TUIView implements View {
    private int idPlayer;
    private int server;
    private GameFase game;
    private boolean isDemo;
    private Scanner scanner = new Scanner(System.in);
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[43m";
    private static final String RED = "\u001B[41m";
    private static final String GREEN = "\u001B[42m";
    private static final String BLUE = "\u001B[44m";
    private static final String PURPLE = "\u001B[35m";
    private static final String BROWN = "\u001B[33m";



    //per ora lascio il server come int
    public TUIView()  {

    }
    @Override
    public void start() {

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
        game = gameFase;
    }
    @Override
    public void printTile(Tile tile) {
    }

    @Override
    public void printCard(Card card) {
        //ricordardi di chiedere al franci se esiste allora il toString allinterno della carta e come printa

        System.out.println(card);
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
        StringBuilder topBorder = new StringBuilder();
        StringBuilder mid1 = new StringBuilder();
        StringBuilder mid2 = new StringBuilder();
        StringBuilder mid3 = new StringBuilder();
        StringBuilder bottomBorder = new StringBuilder();
        int printed = 0;
        int maxPerRow = 7;
        int size = tiles.size();
        for (int i = 0; i < size; i++) {
            topBorder.append("+---------+ ");
            mid1.append("|         | ");
            mid2.append(String.format("|   %3d   | ", i + 1));
            mid3.append("|         | ");
            printed++;
            if (printed == maxPerRow) {
                bottomBorder.append(topBorder);
                System.out.println(topBorder);
                System.out.println(mid1);
                System.out.println(mid2);
                System.out.println(mid3);
                System.out.println(bottomBorder);
                topBorder.setLength(0);
                mid1.setLength(0);
                mid2.setLength(0);
                mid3.setLength(0);
                bottomBorder.setLength(0);
                printed = 0;
            }
        }
        if (printed > 0) {
            bottomBorder.append(topBorder);
            System.out.println(topBorder);
            System.out.println(mid1);
            System.out.println(mid2);
            System.out.println(mid3);
            System.out.println(bottomBorder);
        }

    }

    @Override
    public void printPileShown(List<Tile> tiles) {
        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        StringBuilder border = new StringBuilder();
        int size = tiles.size();
        int printed = 0;
        int maxPerRow = 7;
        for (int i = 0; i < size; i++) {
            Tile tile = tiles.get(i);
            String[] rendered = renderTile(tile);


            border.append("+---------+ ");
            top.append("|").append(rendered[0]).append("| ");
            mid.append("|").append(rendered[1]).append("| ");
            bot.append("|").append(rendered[2]).append("| ");


            if ((i + 1) % 7 == 0 || i == size - 1) {

                System.out.println(border);
                System.out.println(top);
                System.out.println(mid);
                System.out.println(bot);
                System.out.println(border);


                border.setLength(0);
                top.setLength(0);
                mid.setLength(0);
                bot.setLength(0);
            }
        }

    }


    private String getTileContent (Tile tile){
        switch(tile){
            case EmptySpace x ->{ return "   ";}
            case EnergyCell x ->{ return "EC"+GREEN+x.getCapacity()+RESET;}
            case Engine x ->{ return "ENG";
                //oppure facciamo così, cioè il fatto che nel lato ci sia 6/7 spero basti?
//                if(x.isDouble()){
//                    return "EN2";
//                }else{
//                    return "EN1";
//                }
            }
            case Cannon x ->{ return "CAN";
//                if(x.isDouble()){
//                    return "CN2";
//                }else{
//                    return "CN1";
//                }
            }
            case HousingUnit x ->{
                if(x.getType() == Human.HUMAN){
                    return "HU"+x.returnLenght();
                }else if(x.getType() == Human.BROWN_ALIEN){
                    return BROWN+"HU"+RESET+x.returnLenght();
                }else if(x.getType() == Human.PURPLE_ALIEN){
                    return PURPLE+"HU"+RESET+x.returnLenght();
                }
            }
            case MultiJoint x ->{return "MTJ";}
            case Shield x ->{
                //ricordarsi che nella costruzione della tile come i storage unit mettiamo gli angoli protetti
                    return "SHL";
            }
            case StorageUnit x -> {
                if(x.isAdvanced()){
                    return RED+"SU"+RESET+x.getMax();
                }
                return "SU"+x.getMax();
            }
            default -> throw new IllegalStateException("Unexpected value: " + tile);
        }
        return null;
    }

    public void updateView(Tile[][] dashboard) {
        System.out.print("    ");
        for (int col = 0; col < 7; col++) {
            System.out.printf("   %2d    ", col + 4);
        }
        System.out.println();
        for (int row = 0; row < 5; row++) {
            StringBuilder border = new StringBuilder("    ");
            StringBuilder top = new StringBuilder(String.format("%2d  ", row + 5));
            StringBuilder mid = new StringBuilder("    ");
            StringBuilder bot = new StringBuilder("    ");

            for (int col = 0; col < 7; col++) {
                Tile tile = dashboard[row][col];

                border.append("+---------");
                switch (tile) {
                    case EmptySpace x -> {
                        top.append("|         ");
                        mid.append("|         ");
                        bot.append("|         ");
                    }
                    default -> {
                        String[] rendered = renderTile(tile);
                        top.append("|").append(rendered[0]);
                        mid.append("|").append(rendered[1]);
                        bot.append("|").append(rendered[2]);
                    }
                }
            }
            border.append("+");
            System.out.println(border);
            System.out.println(top.append("| ").append(row + 1));
            System.out.println(mid.append("|"));
            System.out.println(bot.append("|"));
        }

        StringBuilder bottom = new StringBuilder("    ");
        for (int col = 0; col < 7; col++) {
            bottom.append("+---------");
        }
        bottom.append("+");
        System.out.println(bottom);


        System.out.print("    ");
        for (int col = 0; col < 7; col++) {
            System.out.printf("   %2d    ", col + 4);
        }
        System.out.println();
    }

    public String[] renderTile(Tile tile) {
        String[] out = new String[3];
        int a = tile.controlCorners(0);
        int b = tile.controlCorners(1);
        int c = tile.controlCorners(2);
        int d = tile.controlCorners(3);
        String label = getTileContent(tile);
        switch (tile){
            case EmptySpace x -> {
                out[0] = String.format("         ");
                out[1] = String.format("         ");
                out[2] = String.format("         ");
            }
            case Engine x -> {
                out[0] = String.format("    %d     ", a);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("    %d     ", c);
            }
            case Cannon x -> {
                out[0] = String.format("    %d     ", a);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("    %d     ", c);
            }
            case EnergyCell x ->{
                out[0] = String.format("    %d     ", a);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("    %d     ", c);
            }
            case MultiJoint x ->{
                out[0] = String.format("    %d     ", a);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("    %d     ", c);
            }
            case HousingUnit x ->{
                out[0] = String.format("    %d     ", a);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("    %d     ", c);
            }
            case Shield x ->{
                if(x.getProtectedCorner(0)==8 && x.getProtectedCorner(1)==8){
                    out[0] = String.format("    %s%d%s     ",GREEN, a, RESET);
                    out[1] = String.format("%d %-5s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %d     ", c);
                }else if(x.getProtectedCorner(1)==8 && x.getProtectedCorner(2)==8){
                    out[0] = String.format("    %d     ", a);
                    out[1] = String.format("%d %-5s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %s%d%s     ", GREEN,c,RESET);
                }else if(x.getProtectedCorner(2)==8 && x.getProtectedCorner(3)==8){
                    out[0] = String.format("    %d     ", a);
                    out[1] = String.format("%s%d%s %-5s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %s%d%s     ", GREEN,c,RESET);
                }else if(x.getProtectedCorner(2)==8 && x.getProtectedCorner(0)==8){
                    out[0] = String.format("    %s%d%s     ",GREEN, a, RESET);
                    out[1] = String.format("%s%d%s %-5s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %d     ", c);
                }
            }
            case StorageUnit x ->{
                List<Colour> listOfGoods = x.getListOfGoods();
                int green = 0;
                int red = 0;
                int yellow = 0;
                int blue = 0;
                for (Colour good : listOfGoods) {
                    switch (good) {
                        case RED -> red++;
                        case YELLOW -> yellow++;
                        case GREEN -> green++;
                        case BLUE -> blue++;
                    }
                }
                out[0] = String.format("%s%d%s  %d   %s%d%s",RED,red,RESET, a,YELLOW,yellow,RESET);
                out[1] = String.format("%d %-5s %d",d,label,b);
                out[2] = String.format("%s%d%s  %d   %s%d%s",BLUE,blue,RESET, c,GREEN,green,RESET);
            }
            default ->{}
        }
        return out;

    }



    /// ///// DA QUI IN BASSO LAVORO IO ///////


    //metodo per gestire i comandi da mandare al server
    public String sendAvailableChoices() throws RemoteException {
        List<String> listOfOptions = new ArrayList<>();
        inform("Possible actions:");
        switch (game) {
            case BOARD_SETUP -> {
                listOfOptions.add("get Blanket Tile");
                listOfOptions.add("take Discovery Tile");
                listOfOptions.add("Declare Ready");
                listOfOptions.add("LogOut");
                listOfOptions.add("Watch A Ship");
                if(!isDemo){
                    listOfOptions.add("Watch A Deck");
                    listOfOptions.add("Spin The Hourglass");
                }
            }
            case TILE_MANAGEMENT -> {
                listOfOptions.add("return Tile");
                listOfOptions.add("place Tile");
                listOfOptions.add("LogOut");
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("RightRotate Tile");
                listOfOptions.add("LeftRotate Tile");
            }
            case WAITING_FOR_PLAYERS -> {
                if(!isDemo) {
                    listOfOptions.add("Spin The Hourglass");
                    listOfOptions.add("Watch A Deck");
                }
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("logOut");
            }
            case WAITING_FOR_TURN  -> {
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("LogOut");
            }
            case DRAW_PHASE -> {
                listOfOptions.add("Draw Card");
                listOfOptions.add("LogOut");
                listOfOptions.add("Guarda Una Nave");
            }
            case CARD_EFFECT -> {
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("LogOut");
            }
            case SCORING  -> listOfOptions.add("logOut");
            default -> listOfOptions.add("error-404");
        }
        printListOfCommand(listOfOptions);
        inform("select the command number");
        int tmp = askIndex();
        return listOfOptions.get(tmp).trim().toLowerCase();
        }
    private void printListOfCommand(List<String> listOfOptions){

        for(int i = 0 ; i < listOfOptions.size(); i++) {
            inform((i + 1) + ":" + listOfOptions.get(i));
        }
    }
}
