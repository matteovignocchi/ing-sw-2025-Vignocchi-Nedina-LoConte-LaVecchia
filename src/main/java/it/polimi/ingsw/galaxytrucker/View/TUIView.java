package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TUIView implements View {
    private GamePhase game;
    private boolean isDemo;
    private boolean[][] maschera;
    private Scanner scanner = new Scanner(System.in);
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String BROWN = "\u001B[33m";
    private static final String PEFOH = "\u001B[36m";
    private static Map<String , Integer> mapPosition = new ConcurrentHashMap<>();


    //per ora lascio il server come int
    //alla fine di ogni comando scritto dagli altri una show update
    @Override
    public void start() {
        System.out.println(
                YELLOW+" _____       _                    _____               _             \n"+
                        "|  __ \\     | |                  |_   _|             | |            \n"+
                        "| |  \\/ __ _| | __ ___  ___   _    | |_ __ _   _  ___| | _____ _ __ \n"+
                        "| | __ / _` | |/ _` \\ \\/ / | | |   | | '__| | | |/ __| |/ / _ \\ '__|\n"+
                        "| |_\\ \\ (_| | | (_| |>  <| |_| |   | | |  | |_| | (__|   <  __/ |   \n"+
                        " \\____/\\__,_|_|\\__,_/_/\\_\\\\__, |   \\_/_|   \\__,_|\\___|_|\\_\\___|_|   \n"+
                        "                           __/ |                                    \n"+
                        "                          |___/                                     \n"+RESET
        );
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
        inform("HU stands for housing unit, when it is white is for humans, when it is"+PURPLE+" purple"+RESET+" it is for humans or the purple alien, when it is"+BROWN+ " brown"+RESET+" it is for humans or brown alien. After HU there is the counter of tokens on the unit");
        inform("EC stands for energy cell, after that there is the number of tokens on the cell");
        inform("CAN stands for cannon, the side with 4 (single cannon) or 5 (double cannon), it indicates the direction the cannon is facing");
        inform("ENG stands for engine, the side with 6 (single engine) or 7 (double engine), it indicates the direction the engine is facing");
        inform("MTJ stands for multi joint");
        inform("SHL stands for shield, the green connectors are the protected side of the shield");
        inform("SU stands for storage unit, when is white is the standard unit, when it is"+RED+" red"+RESET+" it is advanced, after that there is the max capacity of the unit. In each corner, there is a counter showing how many goods of that color are present.");
    }





    @Override
    public void inform(String message) {System.out.println("> " + message);}
    @Override
    public void reportError(String message) {System.out.println(RED+ "\n[ERROR] " + message + RESET);}
    @Override
    public void updateState(GamePhase gamePhase) {
        game = gamePhase;
    }
    @Override
    public void updateMap(Map<String, Integer> map) {
        mapPosition = map;
    }
    @Override
    public String choosePlayer(){
        for(String s : mapPosition.keySet()){
            System.out.println(s + ": " + mapPosition.get(s));
        }
        System.out.println();
        String nickname;
        while(true){
            inform("Selecet nickname of the player");
            nickname = askString();
            for (String key : mapPosition.keySet()) {
                if (key.equalsIgnoreCase(nickname)) {
                    return key;
                }
            }
            reportError("Please enter a nickname of some player in game");
        }
    }
    @Override
    public boolean ask(String message) {
        boolean decision = false;
        while(true) {
            inform(message+ "(Yes/No)");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("yes")) {
                decision = true;
                break;
            } else if (response.equals("no")) {
                break;
            }
            else {
                reportError("The response entered is invalid. Try again: \n");
            }
        }
        return decision;
    }

    @Override
    public int[] askCoordinate() {
            int[] coordinate = new int[2];

                while (true) {
                    inform("Insert the row:");
                    try {
                        coordinate[0] = scanner.nextInt();
                        scanner.nextLine(); // consuma il newline
                    } catch (InputMismatchException e) {
                        inform("Invalid input. Please enter a number for the row.");
                        scanner.nextLine(); // consuma l'input errato
                    }

                    if(coordinate[0] >=5 && coordinate[0] <=9) break;
                    else inform("Invalid input. Please enter a number for the row.");
                }
            while (true) {
                inform("Insert the column:");
                try {
                    coordinate[1] = scanner.nextInt();
                    scanner.nextLine(); // consuma il newline
                } catch (InputMismatchException e) {
                    inform("Invalid input. Please enter a number for the column.");
                    scanner.nextLine(); // consuma l'input errato
                }
                if(coordinate[1] >=4 && coordinate[1] <=10) break;
                else inform("Invalid input. Please enter a number for the column.");
            }
            coordinate[0] = coordinate[0] - 5;
            coordinate[1] = coordinate[1] - 4;

            return coordinate;
        }

    @Override
    public int askIndex() {
        while (true) {
            inform("Insert index:");
            String line = askString();
            try {
                int value = Integer.parseInt(line.trim());
                return value - 1;
            } catch (NumberFormatException e) {
                reportError("Invalid input. Please enter a number.");
            }
        }
    }


    @Override
    public String askString() {
        return scanner.nextLine();
    }


    @Override
    public void setInt() {

    }

    @Override
    public void printListOfGoods(List<Colour> Goods) {
        inform("List of goods: ");
        for(Colour colour : Goods) {
            switch (colour){
                case BLUE -> System.out.println(BLUE+"Blue "+RESET);
                case RED -> System.out.println(RED+"Red "+RESET);
                case GREEN -> System.out.println(GREEN+"Green "+RESET);
                case YELLOW -> System.out.println(YELLOW+"Yellow "+RESET);
            }
        }
    }


    @Override
    public void printDashShip(Tile[][] dashboard) {
//        if (this.maschera == null
//                || this.maschera.length != dashboard.length
//                || this.maschera[0].length != dashboard[0].length) {
//            this.maschera = new boolean[ dashboard.length ][ dashboard[0].length ];
//            // se ti serve azzerarla, fallo subito:
//            for (int i = 0; i < maschera.length; i++) {
//                Arrays.fill(maschera[i], false);
//            }
//        }

        System.out.print("    ");
        for (int col = 0; col < 7; col++) {
            System.out.printf("    %2d    ", col + 4);
        }
        System.out.println();
        for (int row = 0; row < 5; row++) {
            StringBuilder border = new StringBuilder("    ");
            StringBuilder mid = new StringBuilder(String.format("%2d  ", row + 5));
            StringBuilder top = new StringBuilder("    ");
            StringBuilder bot = new StringBuilder("    ");

            for (int col = 0; col < 7; col++) {
                Tile tile = dashboard[row][col];

                border.append("+---------");
                switch (tile) {
                    case EmptySpace x -> {
                        if (!maschera[row][col]) {
                            String block = "█████████"; // 9 caratteri pieni
                            top.append("|").append(block);
                            mid.append("|").append(block);
                            bot.append("|").append(block);
                        } else {
                            if (row == 0 && (col == 5 || col == 6)) {
                                String block = YELLOW+"█████████"+RESET; // 9 caratteri pieni
                                top.append("|").append(block);
                                mid.append("|").append(block);
                                bot.append("|").append(block);
                            } else {
                                top.append("|         ");
                                mid.append("|         ");
                                bot.append("|         ");
                            }
                        }
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
            System.out.println(top.append("|"));
            System.out.println(mid.append("|  ").append(row + 5));
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
            System.out.printf("    %2d    ", col + 4);
        }
        System.out.println();
    }

    //TODO: Assicurarsi che l'ordine sia corretto, altrimenti il vecchio metodo è scritto sotto
    public void printMapPosition() {
        System.out.println("\nPlayers in game:");
        mapPosition.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())    // ordina per posizione
                .forEachOrdered(entry -> {
                    int pos = entry.getValue();
                    String nick = entry.getKey();
                    inform(String.format("  %d - %s", pos, nick));
                });
    }
//    public void printMapPosition(){
//        StringBuilder string = new StringBuilder();
//        for(String key : mapPosition.keySet()){
//            string.append(" /").append(key).append(": ").append(mapPosition.get(key));
//        }
//        string.append("/");
//        inform(string.toString());
//    }

    /// position diventa una mappa stringa intero
    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        switch(game){
            case WAITING_IN_LOBBY -> inform("Nickame : " + nickname);
            case BOARD_SETUP -> inform(" -Nickname: "+nickname+"\n-Position : Too early to know where you'll finish!"+"\n-Credits : too rich!"+"\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy : "+numberOfEnergy);
            case TILE_MANAGEMENT, DRAW_PHASE -> {}
            case WAITING_FOR_PLAYERS -> inform(" -Nickname: "+nickname+ /*" -Position : " +position+ */"\n-Credits : "+ credits+ "\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy : "+numberOfEnergy);
            case WAITING_FOR_TURN, CARD_EFFECT -> inform(" -Nickname: "+nickname+ /*" -Position : "+position+*/"\n-Credits : "+credits+"\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy : "+numberOfEnergy);
            case SCORING -> inform(" -Nickname: "+nickname/*+" -Position : "+position*/);
            case EXIT -> inform("Goodbye!");
        }
        printMapPosition();
        System.out.println();
    }

    //metodo che riceve una lista, in cui prendi
    @Override
    public void printNewFase(GamePhase gamePhase) {

    }

    @Override
    public void printDeck(List<Card> deck) {
        inform("Deck: ");
        for(Card card : deck) {
            printCard(card);
            System.out.println();
        }
    }

    @Override
    public void printCard(Card card) {
        switch (card){
            case AbandonedShipCard c ->{
                inform("===Abandoned Ship===\n"+"-Days: "+c.getDays()+"\n-Crew mates: "+c.getNumCrewmates()+"\n-Credits: "+c.getCredits());
            }
            case AbandonedStationCard c ->{
                inform("===Abandoned Station===\n"+"-Days: "+c.getDays()+"\n-Crew mates: "+c.getNumCrewmates()+"\n-");
                printListOfGoods(c.getStationGoods());
            }
            case FirstWarzoneCard c ->{
                inform("===War Zone===\n");
                System.out.println("-Player with less crew mates loses "+c.getDays()+"flight days\n");
                System.out.println("-Player with less engine power loses "+c.getNumCrewmates()+"crew mates\n");
                System.out.println("-Player with less fire power gets: \n");
                for(int i = 0; i < c.getShotsDirections().size(); i++){
                    System.out.println("- Cannon shot "+(i+1)+": Direction "+c.getShotsDirections().get(i)+",Size "+c.getShotsSize().get(i)+"\n");
                }
            }
            case SecondWarzoneCard c ->{
                inform("===War Zone===\n");
                System.out.println("-Player with less fire power loses "+c.getDays()+"flight days\n");
                System.out.println("-Player with less engine power loses"+c.getNumGoods()+"goods\n");
                System.out.println("-Player with less crew mates gets: \n");
                for(int i = 0; i < c.getShotsDirections().size(); i++){
                    System.out.println("- Cannon shot "+(i+1)+": Direction "+c.getShotsDirections().get(i)+",Size "+c.getShotsSize().get(i)+"\n");
                }
            }
            case MeteoritesRainCard c ->{
                inform("===Meteorites Rain===\n");
                for(int i = 0; i < c.getMeteorites_directions().size(); i++){
                    System.out.println("- Meteorite "+(i+1)+": Direction "+c.getMeteorites_directions().get(i)+",Size "+c.getMeteorites_size().get(i)+"\n");
                }
            }
            case OpenSpaceCard c -> inform("===Open Space===\n");
            case StardustCard c -> inform("===Stardust===\n");
            case PiratesCard c ->{
                inform("===Pirates===\n");
                System.out.println("- Fire power: "+c.getFirePower()+"\n"+"- Credits: "+c.getCredits()+"\n"+"- Days: "+c.getDays()+"\n");
                for(int i = 0; i < c.getShots_directions().size(); i++){
                    System.out.println("- Cannon shot "+(i+1)+": Direction "+c.getShots_directions().get(i)+",Size "+c.getShots_size().get(i)+"\n");
                }
            }
            case PlanetsCard c->{
                inform("===Planets===\n");
                System.out.println("- Days: "+c.getDays()+"\n");
                for(int i =0; i< c.getRewardGoods().size(); i++){
                    System.out.println("- Planet "+(i+1)+": ");
                    printListOfGoods(c.getRewardGoods().get(i));
                    System.out.println();
                }
            }
            case PlaugeCard c-> inform("===Plauge===\n");

            case SlaversCard c->{
                inform("===Slavers===\n");
                System.out.println("- Fire power: "+c.getFirePower()+"\n"+"- Crew mates: "+c.getNumCrewmates()+"\n"+"- Credits: "+c.getCredits()+"\n"+"- Days: "+c.getDays()+"\n");
            }
            case SmugglersCard c->{
                inform("===Smugglers===\n");
                System.out.println("- Fire power: "+c.getFirePower()+"\n"+"- Goods: "+c.getNumRemovedGoods()+"\n-");
                printListOfGoods(c.getRewardGoods());
                System.out.println("\n- Days: "+c.getDays()+"\n");
            }
            default -> {}
        }
    }


    @Override
    public void printTile(Tile tile) {
        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        StringBuilder border = new StringBuilder();
        String[] rendered = renderTile(tile);
        border.append("+---------+ ");
        top.append("|").append(rendered[0]).append("| ");
        mid.append("|").append(rendered[1]).append("| ");
        bot.append("|").append(rendered[2]).append("| ");
        System.out.println(border);
        System.out.println(top);
        System.out.println(mid);
        System.out.println(bot);
        System.out.println(border);
    }

    @Override
    public void printPileCovered() {
        StringBuilder topBorder = new StringBuilder();
        StringBuilder mid1 = new StringBuilder();
        StringBuilder mid2 = new StringBuilder();
        StringBuilder mid3 = new StringBuilder();
        StringBuilder bottomBorder = new StringBuilder();
        int printed = 0;
        int maxPerRow = 7;
        int size = 20;
        for (int i = 0; i < size; i++) {
            topBorder.append("+---------+ ");
            mid1.append("|         | ");
            mid2.append(String.format("|  %3d    | ", i + 1));
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
            case Engine x ->{ return "ENG";}
            case Cannon x ->{ return "CAN";}
            case HousingUnit x ->{
                if(x.getType() == Human.HUMAN){
                    return "HU"+x.returnLenght();
                }else if(x.getType() == Human.BROWN_ALIEN){
                    return BROWN+"HU"+RESET+x.returnLenght()+" ";
                }else if(x.getType() == Human.PURPLE_ALIEN){
                    return PURPLE+"HU"+RESET+x.returnLenght()+" ";
                }
            }
            case MultiJoint x ->{return "MTJ";}
            case Shield x ->{return "SHL";}
            case StorageUnit x -> {
                if(x.isAdvanced()){
                    return RED+"SU"+RESET+PEFOH+x.getMax()+RESET;
                }
                return "SU"+PEFOH+x.getMax()+RESET;
            }
            default -> throw new IllegalStateException("Unexpected value: " + tile);
        }
        return null;
    }




    private String[] renderTile(Tile tile) {
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
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case Cannon x -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case EnergyCell x ->{
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s  %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case MultiJoint x ->{
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case HousingUnit x ->{
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case Shield x ->{
                if(x.getProtectedCorner(0)==8 && x.getProtectedCorner(1)==8){
                    out[0] = String.format("    %s%d%s    ",GREEN, a, RESET);
                    out[1] = String.format("%d  %-4s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %d    ", c);
                }else if(x.getProtectedCorner(1)==8 && x.getProtectedCorner(2)==8){
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%d  %-4s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %s%d%s    ", GREEN,c,RESET);
                }else if(x.getProtectedCorner(2)==8 && x.getProtectedCorner(3)==8){
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%s%d%s  %-4s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %s%d%s    ", GREEN,c,RESET);
                }else if(x.getProtectedCorner(3)==8 && x.getProtectedCorner(0)==8){
                    out[0] = String.format("    %s%d%s    ",GREEN, a, RESET);
                    out[1] = String.format("%s%d%s  %-4s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %d    ", c);
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
                out[0] = String.format("%s%d%s   %d   %s%d%s",RED,red,RESET, a,YELLOW,yellow,RESET);
                out[1] = String.format("%d  %-4s  %d",d,label,b);
                out[2] = String.format("%s%d%s   %d   %s%d%s",BLUE,blue,RESET, c,GREEN,green,RESET);
            }
            default ->{}
        }
        return out;

    }



    /// ///// DA QUI IN BASSO LAVORO IO ///////

    private List<String> commandConstructor(){
        List<String> listOfOptions = new ArrayList<>();
        switch (game) {
            case WAITING_IN_LOBBY ->  listOfOptions.add("LogOut");

            case BOARD_SETUP -> {
                listOfOptions.add("Get a covered tile");
                listOfOptions.add("Get a shown tile");
                listOfOptions.add("Declare Ready");
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("Take Reserved Tile");
                if(!isDemo){
                    listOfOptions.add("Watch a deck");
                    listOfOptions.add("Spin the hourglass");
                }
                listOfOptions.add("LogOut");
            }
            case TILE_MANAGEMENT -> {
                listOfOptions.add("Return the tile");
                listOfOptions.add("Place the tile");
                listOfOptions.add("Left rotate the tile");
                listOfOptions.add("Right rotate the tile");
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("LogOut");
            }
            case WAITING_FOR_PLAYERS -> {
                if(!isDemo) {
                    listOfOptions.add("Spin the hourglass");
                    listOfOptions.add("Watch a deck");
                }
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("logOut");
            }
            case WAITING_FOR_TURN, CARD_EFFECT  -> {
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("LogOut");
            }
            case DRAW_PHASE -> {
                listOfOptions.add("Draw a card");
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("LogOut");
            }
            case SCORING  -> listOfOptions.add("logOut");
            default -> {}
        }
        return listOfOptions;
    }


    @Override
    public String sendAvailableChoices() {
        List<String> options = commandConstructor();

        while (true) {
            System.out.print("Insert index: ");
            System.out.flush();             // forza lo sblocco del prompt
            String line = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(line) - 1;
                if (idx >= 0 && idx < options.size()) {
                    return options.get(idx)
                            .toLowerCase()
                            .replaceAll("[^a-z0-9]", "");
                }
            } catch (NumberFormatException ignored) { }
            System.out.println("[ERROR] Invalid choice, try again.");
        }
    }




    @Override
    public void printListOfCommand(){
        List<String> listOfOptions = commandConstructor();
        inform("Possible actions:");
        for(int i = 0 ; i < listOfOptions.size(); i++) {
            inform((i + 1) + ":" + listOfOptions.get(i));
        }
    }

    @Override
    public void setIsDemo(Boolean demo) {
        boolean[][] validStatus = new boolean[5][7];
        this.isDemo = demo;
        if (isDemo) {
            //first row
            validStatus[0][0]  = false;
            validStatus[0][1]  = false;
            validStatus[0][2]  = false;
            validStatus[0][3]  = true;
            validStatus[0][4]  = false;
            validStatus[0][5]  = false;
            validStatus[0][6]  = false;
            //second row
            validStatus[1][0]  = false;
            validStatus[1][1]  = false;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = false;
            validStatus[1][6]  = false;
            //third row
            validStatus[2][0]  = false;
            validStatus[2][1]  = true;
            validStatus[2][2]  = true;
            validStatus[2][3]  = true;
            validStatus[2][4]  = true;
            validStatus[2][5]  = true;
            validStatus[2][6]  =false;
            //fourth row
            validStatus[3][0]  = false;
            validStatus[3][1]  = true;
            validStatus[3][2]  = true;
            validStatus[3][3]  = true;
            validStatus[3][4]  = true;
            validStatus[3][5]  = true;
            validStatus[3][6]  = false;
            //fifth row
            validStatus[4][0]  = false;
            validStatus[4][1]  = true;
            validStatus[4][2]  = true;
            validStatus[4][3]  = false;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = false;
        } else {
            //first row
            validStatus[0][0]  = false;
            validStatus[0][1]  = false;
            validStatus[0][2]  = true;
            validStatus[0][3]  = false;
            validStatus[0][4]  = true;
            validStatus[0][5]  = true;
            validStatus[0][6]  = true;
            //second row
            validStatus[1][0]  = false;
            validStatus[1][1]  = true;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = true;
            validStatus[1][6]  = false;
            //third row
            validStatus[2][0]  = true;
            validStatus[2][1]  = true;
            validStatus[2][2]  = true;
            validStatus[2][3]  = true;
            validStatus[2][4]  = true;
            validStatus[2][5]  = true;
            validStatus[2][6]  = true;
            //fourth row
            validStatus[3][0]  = true;
            validStatus[3][1]  = true;
            validStatus[3][2]  = true;
            validStatus[3][3]  = true;
            validStatus[3][4]  = true;
            validStatus[3][5]  = true;
            validStatus[3][6]  = true;
            //fifth row
            validStatus[4][0]  = true;
            validStatus[4][1]  = true;
            validStatus[4][2]  = true;
            validStatus[4][3]  = false;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = true;
        }
        this.maschera = validStatus;

    }
    @Override
    public boolean ReturnValidity(int a , int b){
        return maschera[a][b];
    }
    @Override
    public void setValidity(int a , int b){
        boolean tmp =  !maschera[a][b];
        if((a == 0 && b ==5)||(a==0 && b==6)) maschera[a][b] = tmp;
        maschera[a][b] = false;
    }

    public GamePhase getGamePhase() {return game;}

}


