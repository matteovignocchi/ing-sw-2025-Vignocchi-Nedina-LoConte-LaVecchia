package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TUIView implements View {
    private ClientGamePhase game;
    private boolean isDemo;
    private Boolean[][] mask;
    private final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private Scanner scanner = new Scanner(System.in);
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String BROWN = "\u001B[33m";
    private static final String PEFOH = "\u001B[36m";
    private static Map<String , int[] > mapPosition = new ConcurrentHashMap<>();
    private static final Map<String, String> ANSI_COLOR = Map.of(
            "RED",    RED,
            "GREEN",  GREEN,
            "YELLOW", YELLOW,
            "BLUE",   BLUE
    );
    private static final long TIME_OUT = 300000; //TODO: messo così per debug, capire quanto mettere in definitiva


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

    private String readLine(long timeoutMs) throws InterruptedException, IOException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (console.ready()) {
                return console.readLine();
            }
            Thread.sleep(20);
        }
        return null;
    }

    @Override
    public void inform(String message) {System.out.println("> " + message);}
    @Override
    public void reportError(String message) {System.out.println(RED+ "[ERROR] " + message + RESET);}
    @Override
    public void updateState(ClientGamePhase gamePhase) {
        game = gamePhase;
    }
    @Override
    public void updateMap(Map<String, int[]> map) {
        mapPosition = map;
    }

    @Override
    public String choosePlayer() throws IOException, InterruptedException {
        ClientGamePhase originalPhase = getGamePhase();

        System.out.println("\nPlayers in game:");
        mapPosition.entrySet().stream()
                .filter(e -> e.getValue()[2] == 0) // todo:qui filtro solo chi è ancora in gioco, lassciamo cosi o un player deve poter vedere tutti?
                .sorted(Comparator.comparingInt(e -> e.getValue()[0]))
                .forEach(entry -> {
                    String nick      = entry.getKey();
                    int[] info       = entry.getValue();
                    int   pos        = info[0];
                    int   lap        = info[1];
                    inform(String.format("  %s - Lap: %d, Position: %d", nick, lap, pos));
                });

        inform("Select nickname of the player:");

        while(true) {
            String line = readLine(200);
            if (line == null) {
                if (getGamePhase() != originalPhase) {
                    return null;
                }
                continue;
            }
            String nickname = line.trim();

            if (nickname.isEmpty()) {
                reportError("Please enter a valid nickname from the list");
                continue;
            }

            for (String key : mapPosition.keySet()) {
                if (key.equalsIgnoreCase(nickname) && mapPosition.get(key)[2] == 0) {
                    return key;
                }
            }
            reportError("Please enter a valid nickname from the list");
        }
    }

    @Override
    public Boolean ask(String message) {
        ClientGamePhase originalPhase = getGamePhase();
        inform(message + " (Yes/No)");
        try {
            while (true) {
                String line = readLine(200);
                if (line == null) {
                    if(getGamePhase() != originalPhase){
                        return null;
                    }
                    continue;
                }
                line = line.trim().toLowerCase();
                if (line.equals("yes")) return Boolean.TRUE;
                if (line.equals("no")) return Boolean.FALSE;
                reportError("Invalid response, try again.");
            }
        } catch (IOException e) {
            reportError("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean askWithTimeout(String message) {
        long end = System.currentTimeMillis() + TIME_OUT;
        try{
            inform(message + " (Yes/No)");
            while (System.currentTimeMillis() < end) {
                String line = readLine(200);
                if (line == null) continue;
                line = line.trim().toLowerCase();
                if (line.equals("yes")) return true;
                if (line.equals("no"))  return false;
                reportError("Invalid response, try again.");
            }
        } catch (IOException e) {
            reportError("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public int[] askCoordinatesWithTimeout(){
        long end = System.currentTimeMillis() + TIME_OUT;
        int flag = 0;
        int[] coordinates = new int [2];

        try{
            inform("Insert the row: ");
            while(System.currentTimeMillis() < end){
                if(flag == 0){
                    String row = readLine(200);
                    if(row == null) continue;
                    try {
                        int num = Integer.parseInt(row.trim());
                        if(num < 5 || num > 9){
                            reportError("Invalid input. Please enter a valid number for the row.");
                            continue;
                        }
                        coordinates[0] = num;
                        flag = 1;
                    } catch (NumberFormatException e) {
                        reportError("Invalid input. Please enter a number for the row.");
                        continue;
                    }
                } else if (flag == 2){
                  String column = readLine(200);
                  if(column == null) continue;
                  try{
                      int num = Integer.parseInt(column.trim());
                      if(num < 4 || num > 10){
                          reportError("Invalid input. Please enter a valid number for the row.");
                          continue;
                      }
                      coordinates[1] = num;
                      flag = 3;
                      break;
                  } catch (NumberFormatException e) {
                      reportError("Invalid input. Please enter a number for the column.");
                      continue;
                  }
                } else {
                    inform("Insert the column: ");
                    flag = 2;
                }
            }

            if(flag==3) {
                coordinates[0] = coordinates[0] - 5;
                coordinates[1] = coordinates[1] - 4;
                return coordinates;
            }

        } catch (IOException e) {
            reportError("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public Integer askIndexWithTimeout() {
        long end = System.currentTimeMillis() + TIME_OUT;
        try{
            inform("Insert index: ");
            while (System.currentTimeMillis() < end) {
                String line = readLine(200);
                if (line == null) continue;
                try{
                    int idx = Integer.parseInt(line.trim());
                    return idx - 1;
                } catch (NumberFormatException e) {
                    reportError("Invalid input. Please enter a valid index.");
                }
            }
        } catch (IOException e) {
            reportError("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public int[] askCoordinate() throws IOException, InterruptedException{
        int[] coordinate = new int[2];
        ClientGamePhase originalPhase = getGamePhase();

        inform("Insert the row:");
        while (true) {
            String row = readLine(200);
            if (row == null) {
                if(getGamePhase() != originalPhase) {
                    return null;
                } else {
                    continue;
                }
            }
            try {
                coordinate[0] = Integer.parseInt(row.trim());
            } catch (NumberFormatException e) {
                reportError("Invalid input. Please enter a number for the row.");
                continue;
            }
            if(coordinate[0] >=5 && coordinate[0] <=9) break;
            else reportError("Invalid input. Please enter a valid number for the row.");
        }

        inform("Insert the column:");
        while (true) {
            String col = readLine(200);
            if (col == null) {
                if(getGamePhase() != originalPhase) {
                    return null;
                } else {
                    continue;
                }
            }
            try {
                coordinate[1] = Integer.parseInt(col.trim());
            } catch (NumberFormatException e) {
                reportError("Invalid input. Please enter a number for the column.");
                continue;
            }
            if(coordinate[1] >=4 && coordinate[1] <=10) break;
            else reportError("Invalid input. Please enter a valid number for the column.");
        }
        coordinate[0] = coordinate[0] - 5;
        coordinate[1] = coordinate[1] - 4;
        return coordinate;
    }

    @Override
    public Integer askIndex() {
        ClientGamePhase originalPhase = getGamePhase();
        inform("Insert index:");
        try {
            while (true) {
                String line = readLine(200);
                if (line == null) {
                    if (getGamePhase() != originalPhase) {
                        return null;
                    }
                    continue;
                }
                try {
                    int value = Integer.parseInt(line.trim());
                    return value - 1;
                } catch (NumberFormatException e) {
                    reportError("Invalid input. Please enter a number.");
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String askString() {
        ClientGamePhase originalPhase = getGamePhase();
        try{
            while(true){
                String line = readLine(200);
                if(line == null){
                    if(getGamePhase() != originalPhase){
                        return null;
                    }
                    continue;
                }
                return line.trim();
            }
        }catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void setInt() {}

    @Override
    public void printListOfGoods(List<String> Goods) {
        inform("List of goods: ");
        for(String colour : Goods) {
            switch (colour){
                case "BLUE" -> System.out.println(BLUE+"Blue "+RESET);
                case "RED" -> System.out.println(RED+"Red "+RESET);
                case "GREEN" -> System.out.println(GREEN+"Green "+RESET);
                case "YELLOW" -> System.out.println(YELLOW+"Yellow "+RESET);
            }
        }
    }

    @Override
    public void printDashShip(ClientTile[][] dashboard) {
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
                ClientTile tile = dashboard[row][col];

                border.append("+---------");
                switch (tile.type) {
                    case "EMPTYSPACE"  -> {
                        if (mask[row][col] == null) {
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

    @Override
    public void printMapPosition() {
        System.out.println("\nPlayers in game:");
        mapPosition.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue()[0]))
                .forEach(entry -> {
                    String nick  = entry.getKey();
                    int[] data   = entry.getValue();
                    int pos      = data[0];
                    int lap      = data[1];
                    boolean elim = data[2] == 1;
                    String stato = elim ? "Eliminated" : "In game";
                    inform(String.format(" %s – Lap: %d, Position: %d, status: %s", nick, lap, pos, stato));
                });
    }

    //TODO: Assicurarsi che l'ordine sia corretto, altrimenti il vecchio metodo è scritto sotto
//    @Override
//    public void printMapPosition() {
//        System.out.println("\nPlayers in game:");
//        mapPosition.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue())    // ordina per posizione
//                .forEachOrdered(entry -> {
//                    int pos = entry.getValue();
//                    String nick = entry.getKey();
//                    inform(String.format("  %d - %s", pos, nick));
//                });
//    }
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
            case WAITING_IN_LOBBY  -> inform("Nickame : " + nickname);
            case BOARD_SETUP -> inform(" -Nickname: "+nickname+"\n-Position : Too early to know where you'll finish!"+"\n-Credits : too rich!"+"\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy batteries : "+numberOfEnergy);
            case TILE_MANAGEMENT -> {}
            case DRAW_PHASE -> inform(" -Nickname: "+nickname+ /*" -Position : " +position+ */"\n-Credits : "+ credits+ "\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy batteries : "+numberOfEnergy);
            case WAITING_FOR_PLAYERS -> inform(" -Nickname: "+nickname+ /*" -Position : " +position+ */"\n-Credits : "+ credits+ "\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy batteries : "+numberOfEnergy);
            case WAITING_FOR_TURN, CARD_EFFECT -> inform(" -Nickname: "+nickname+ /*" -Position : "+position+*/"\n-Credits : "+credits+"\n-Engine power : "+powerEngine+"\n-Fire power : "+firePower+"\n-Purple alien : "+(purpleAlien ? "present" : "not present")+ "\n-Brown alien : "+(brownAlien ? "present" : "not present")+"\n-Number of humans : "+numberOfHuman+"\n-Number of energy batteries: "+numberOfEnergy);
            case SCORING -> inform(" -Nickname: "+nickname/*+" -Position : "+position*/);
            case EXIT -> inform("Goodbye!");
        }
        printMapPosition();
        System.out.println();
    }

    //metodo che riceve una lista, in cui prendi
    @Override
    public void printNewFase(String gamePhase) {

    }

    @Override
    public void printDeck(List<ClientCard> deck) {
        inform("Deck: ");
        for(ClientCard card : deck) {
            printCard(card);
            System.out.println();
        }
    }

    @Override
    public void printCard(ClientCard card) {
        switch (card.type.toUpperCase()) {
            case "ABANDONEDSHIPCARD"  -> {
                inform("=== Abandoned Ship ===");
                inform(centerKV("Flight Days",    String.valueOf(card.days),    20));
                inform(centerKV("Crewmates",   String.valueOf(card.numCrewmates), 20));
                inform(centerKV("Credits", String.valueOf(card.credits), 20));
            }
            case "ABANDONEDSTATIONCARD" -> {
                inform("=== Abandoned Station ===");
                inform(centerKV("Flight Days",  String.valueOf(card.days),  20));
                inform(centerKV("Crewmates", String.valueOf(card.numCrewmates), 20));
                inform("List of goods: " + joinGoods(card.stationGoods));
            }
            case "FIRWARZONECARD"  -> {
                inform("=== First War Zone ===");
                printWarzoneTable(
                        card.directions,
                        card.sizes,
                        "- Player with less crewmates lose " + card.days + " days",
                        "- Player with less engine power lose " + card.numCrewmates + " crewmates"
                );
            }
            case "SECONDWARZONECARD"  -> {
                inform("=== Second War Zone ===");
                printWarzoneTable(
                        card.directions,
                        card.sizes,
                        "- Player with less fire power lose " + card.days + " days",
                        "- Player with less engine power lose " + card.numCrewmates + " goods"
                );
            }
            case "METEORITESRAINCARD"  -> {
                inform("=== Meteorites Rain ===");
                printAttackTable(card.directions, card.sizes);
            }
            case "PIRATESCARD"  -> {
                inform("=== Pirates ===");
                inform(centerKV("Fire power",    String.valueOf(card.firePower), 20));
                inform(centerKV("Credits", String.valueOf(card.credits),   20));
                inform(centerKV("Flight Days",    String.valueOf(card.days),      20));
                printAttackTable(card.directions, card.sizes);
            }
            case "SLAVERSCARD"  -> {
                inform("=== Slavers ===");
                inform(centerKV("Fire power", String.valueOf(card.firePower), 20));
                inform(centerKV("Crewmates", String.valueOf(card.numCrewmates), 20));
                inform(centerKV("Credits", String.valueOf(card.credits), 20));
                inform(centerKV("Flight Days", String.valueOf(card.days), 20));
            }
            case "SMUGGLERSCARD"  -> {
                inform("=== Smugglers ===");
                inform(centerKV("Fire power", String.valueOf(card.firePower), 20));
                inform(centerKV("Removed goods", String.valueOf(card.rewardGoods), 20));
                inform(centerKV("Flight Days", String.valueOf(card.days), 20));
                inform("Reward goods: " + joinGoods(card.rewardGoods));
            }
            case "PLANETSCARD"  -> {
                inform("=== Planets ===");
                inform(centerKV("Flight Days", String.valueOf(card.days), 20));
                for (int i = 0; i < card.rewardGoodsList.size(); i++) {
                    inform("Planet " + (i+1) + ": " + joinGoods(card.rewardGoodsList.get(i)));
                }
            }
            case "PLAUGECARD" -> inform("=== Plague ===");
            case "OPENSPACECARD"  -> inform("=== Open Space ===");
            case "STARDUSTCARD"  -> inform("=== Stardust ===");
            default -> inform(""); //non ci arriverò mai 100%
        }
    }

    /** Converte [RED, BLUE] in "RED, BLUE" */
    private String joinGoods(List<String> goods) {
        return goods.stream()
                .map(c -> ANSI_COLOR.getOrDefault(c, "")
                        + c
                        + RESET)
                .collect(Collectors.joining(", "));
    }

    private void printWarzoneTable(List<Integer> dirs,
                                   List<Boolean> sizes,
                                   String summary1,
                                   String summary2) {
        inform(summary1);
        inform(summary2);
        printAttackTable(dirs, sizes);
    }

    private String centerKV(String key, String val, int width) {
        String line = key + ": " + val;
        int pad = width - line.length();
        int padL = pad/2, padR = pad - padL;
        return " ".repeat(Math.max(0,padL)) + line + " ".repeat(Math.max(0,padR));
    }

    private void printAttackTable(List<Integer> dirs, List<Boolean> sizes) {
        Map<Integer,String> arrow = Map.of(0,"↑",1,"←",2,"↓",3,"→");
        Function<Boolean,String> sizeLabel = b->b?"Big":"Small";

        int shotW = 6, dirW = 10, szW = 6;
        inform(center("Shot",shotW) + "│" + center("Direction",dirW) + "│" + center("Size",szW));
        inform("──────┼──────────┼──────");
        for(int i=0;i<dirs.size();i++){
            String sh = center(String.valueOf(i+1), shotW);
            String dr = center( arrow.getOrDefault(dirs.get(i), "?"), dirW);
            String sz = center( sizeLabel.apply(sizes.get(i)), szW);
            inform(sh + "│" + dr + "│" + sz);
        }
    }

    private String center(String s, int width) {
        int pad = width - s.length();
        int padL = pad>0?pad/2:0, padR = pad>0?pad-padL:0;
        return " ".repeat(padL) + s + " ".repeat(padR);
    }

    @Override
    public void printTile(ClientTile tile) {
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
    public void printPileShown(List<ClientTile> tiles) {
        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        StringBuilder border = new StringBuilder();
        int size = tiles.size();

        for (int i = 0; i < size; i++) {
            ClientTile tile = tiles.get(i);
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


    private String getTileContent (ClientTile tile){
        switch(tile.type.trim().toUpperCase()){
            case "EMPTYSPACE"  ->{ return "   ";}
            case "ENERGYCELL"  ->{ return "EC"+GREEN+tile.capacity+RESET;}
            case "ENGINE"  ->{ return "ENG";}
            case "CANNON"  ->{ return "CAN";}
            case "HOUSINGUNIT"  ->{
                if(tile.human.equals("HUMAN")){
                    return "HU"+tile.tokens.size();
                }else if(tile.human.equals("BROWN_ALIEN")){
                    return BROWN+"HU"+tile.tokens.size()+RESET+" ";
                }else if(tile.human.equals("PURPLE_ALIEN")){
                    return PURPLE+"HU"+tile.tokens.size()+RESET+" ";
                }
            }
            case "MULTIJOINT"  ->{return "MTJ";}
            case "SHIELD" ->{return "SHL";}
            case "STORAGEUNIT"  -> {
                if(tile.advance){
                    return RED+"SU"+RESET+PEFOH+tile.max+RESET;
                }
                return "SU"+PEFOH+tile.max+RESET;
            }
            default -> throw new IllegalStateException("Unexpected value: " + tile);
        }
        return null;
    }




    private String[] renderTile(ClientTile tile) {
        String[] out = new String[3];
        int a = tile.a;
        int b = tile.b;
        int c = tile.c;
        int d = tile.d;
        String label = getTileContent(tile);
        switch (tile.type.trim().toUpperCase()){
            case "EMPTYSPACE"  -> {
                out[0] = String.format("         ");
                out[1] = String.format("         ");
                out[2] = String.format("         ");
            }
            case "ENGINE" -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case "MULTIJOINT" -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case "CANNON" -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case "ENERGYCELL"  ->{
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s  %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case "HOUSINGUNIT"  ->{
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d",d,label,b);
                out[2] = String.format("    %d    ", c);
            }
            case "SHIELD" ->{
                if(tile.protectedCorners.get(0) == 8 && tile.protectedCorners.get(1) == 8 ){
                    out[0] = String.format("    %s%d%s    ",GREEN, a, RESET);
                    out[1] = String.format("%d  %-4s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %d    ", c);
                }else if(tile.protectedCorners.get(1) == 8 && tile.protectedCorners.get(2) == 8){
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%d  %-4s %s%d%s",d,label,GREEN,b,RESET);
                    out[2] = String.format("    %s%d%s    ", GREEN,c,RESET);
                }else if(tile.protectedCorners.get(2) == 8 && tile.protectedCorners.get(3) == 8){
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%s%d%s  %-4s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %s%d%s    ", GREEN,c,RESET);
                }else if(tile.protectedCorners.get(3) == 8 && tile.protectedCorners.get(0) == 8){
                    out[0] = String.format("    %s%d%s    ",GREEN, a, RESET);
                    out[1] = String.format("%s%d%s  %-4s %d",GREEN,d,RESET,label,b);
                    out[2] = String.format("    %d    ", c);
                }
            }
            case "STORAGEUNIT"  ->{
                List<String> listOfGoods = tile.goods;
                int green = 0;
                int red = 0;
                int yellow = 0;
                int blue = 0;
                for (String good : listOfGoods) {
                    switch (good) {
                        case "RED" -> red++;
                        case "YELLOW" -> yellow++;
                        case "GREEN" -> green++;
                        case "BLUE" -> blue++;
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
                if(!isDemo){
                    listOfOptions.add("Take Reserved Tile");
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
            case TILE_MANAGEMENT_AFTER_RESERVED -> {
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
            case WAITING_FOR_TURN -> {
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("LogOut");
            }
            case CARD_EFFECT -> {}
            case DRAW_PHASE -> {
                listOfOptions.add("Draw a card");
                listOfOptions.add("Watch a player's ship");
                listOfOptions.add("LogOut");
            }
            case SCORING  -> listOfOptions.add("logOut");
            case EXIT  -> listOfOptions.add("logOut");
            default -> {}
        }
        return listOfOptions;
    }

    @Override
    public String sendAvailableChoices() throws IOException, InterruptedException {
        List<String> options = commandConstructor();
        String line = readLine(200);
        if (line == null) return null;
        try {
            int idx = Integer.parseInt(line.trim()) - 1;
            if (0 <= idx && idx < options.size())
                return options.get(idx).toLowerCase().replaceAll("[^a-z0-9]", "");
            else throw new IOException("Choose a valid command");
        } catch (NumberFormatException e) {
            reportError("Invalid choice, try again.");
        }
        return null;

        /**
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
         */
    }

    @Override
    public void printListOfCommand(){
        List<String> listOfOptions = commandConstructor();
        if (listOfOptions.isEmpty()) return;
        inform("Possible actions:");
        for(int i = 0 ; i < listOfOptions.size(); i++) {
            inform((i + 1) + ":" + listOfOptions.get(i));
        }
        inform("Insert index: ");
    }

    @Override
    public void setIsDemo(Boolean demo) {
        Boolean[][] validStatus = new Boolean[5][7];
        this.isDemo = demo;
        if (isDemo) {
            //first row
            validStatus[0][0]  = null;
            validStatus[0][1]  = null;
            validStatus[0][2]  = null;
            validStatus[0][3]  = true;
            validStatus[0][4]  = null;
            validStatus[0][5]  = null;
            validStatus[0][6]  = null;
            //second row
            validStatus[1][0]  = null;
            validStatus[1][1]  = null;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = null;
            validStatus[1][6]  = null;
            //third row
            validStatus[2][0]  = null;
            validStatus[2][1]  = true;
            validStatus[2][2]  = true;
            validStatus[2][3]  = true;
            validStatus[2][4]  = true;
            validStatus[2][5]  = true;
            validStatus[2][6]  =null;
            //fourth row
            validStatus[3][0]  = null;
            validStatus[3][1]  = true;
            validStatus[3][2]  = true;
            validStatus[3][3]  = true;
            validStatus[3][4]  = true;
            validStatus[3][5]  = true;
            validStatus[3][6]  = null;
            //fifth row
            validStatus[4][0]  = null;
            validStatus[4][1]  = true;
            validStatus[4][2]  = true;
            validStatus[4][3]  = null;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = null;
        } else {
            //first row
            validStatus[0][0]  = null;
            validStatus[0][1]  = null;
            validStatus[0][2]  = true;
            validStatus[0][3]  = null;
            validStatus[0][4]  = true;
            validStatus[0][5]  = true;
            validStatus[0][6]  = true;
            //second row
            validStatus[1][0]  = null;
            validStatus[1][1]  = true;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = true;
            validStatus[1][6]  = null;
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
            validStatus[4][3]  = null;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = true;
        }
        this.mask = validStatus;

    }
    @Override
    public void displayAvailableGames (Map<Integer, int[]> availableGames) {
        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
            int id = entry.getKey();
            int[] info = entry.getValue();
            boolean isDemo = info[2] == 1;
            String suffix = isDemo ? " DEMO" : "";
            this.inform(id + ". Players in game : " + info[0] + "/" + info[1] + suffix);
        }
    }
    @Override
    public boolean returnValidity(int a , int b){
        return mask[a][b];
    }
    @Override
    public void setValidity(int a , int b){
        mask[a][b] = false;
    }
    @Override
    public void resetValidity(int a , int b){
        mask[a][b] = true;
    }
    
    @Override
    public void setTile(ClientTile tile,  int row, int col){
        
    }
   @Override
   public void setCurrentTile(ClientTile tile){
        
   }
    
    
    @Override
    public ClientGamePhase getGamePhase() {return game;}
    
    

}


