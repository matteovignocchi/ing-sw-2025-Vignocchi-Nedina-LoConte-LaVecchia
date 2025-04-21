package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Model.TileParserLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileParserLoaderTest {
    private TileParserLoader tileParserLoader;
    Controller controller;
    @BeforeEach
    void setUp() throws CardEffectException, IOException {
        tileParserLoader = new TileParserLoader();
        controller = new Controller(false, 0);
    }
    private boolean sameElementsWithSameCount(List<Tile> list1, List<Tile> list2) {
        if (list1.size() != list2.size()) return false;
        List<String> signatures1 = list1.stream().map(this::getTileSignature).toList();
        List<String> signatures2 = list2.stream().map(this::getTileSignature).toList();

        return signatures1.containsAll(signatures2) && signatures2.containsAll(signatures1);
    }

    private String getTileSignature(Tile tile) {
        if (tile == null) return "null";

        int[] corners = tile.getCorners();
        String cornerStr = Arrays.toString(corners);

        return switch (tile) {
            case Cannon t -> "Cannon:" + cornerStr + "|double=" + t.isDouble();
            case Engine t -> "Engine:" + cornerStr + "|double=" + t.isDouble();
            case EnergyCell t -> "EnergyCell:" + cornerStr + "|max=" + t.getCapacity();
            case HousingUnit t -> "HousingUnit:" + cornerStr + "|type=" + t.getType();
            case MultiJoint t -> "MultiJoint:" + cornerStr;
            case Shield t -> "Shield:" + cornerStr;
            case StorageUnit t -> "StorageUnit:" + cornerStr + "|max=" + t.getMax() + "|adv=" + t.isAdvanced();
            default -> throw new IllegalStateException("Unexpected tile type");
        };
    }
    private void printTile(List<Tile> tiles) {
        int counter = 0;
        for (Tile tile : tiles) {
            switch(tile){
                case Cannon t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Cannon:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + " "+t.isDouble() +"\n");
                }
                case Engine t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Engine:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + " "+t.isDouble() +"\n");
                }
                case EnergyCell t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Energy Cell:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + " "+t.getCapacity() +"\n");
                }
                case HousingUnit t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Housing Unit:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + " "+t.getType() +"\n");
                }
                case MultiJoint t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Multi Joint:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] +"\n");
                }
                case Shield t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Shield:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + "\n");
                }
                case StorageUnit t ->{
                    int[] conrners = t.getCorners();
                    System.out.println("Storage Unit:\na: " + conrners[0]+" b: " + conrners[1]+" c: " + conrners[2]+" d: " + conrners[3] + " "+ t.getMax() + " " + t.isAdvanced() +"\n");
                }
                default -> throw new IllegalStateException("Unexpected value: " + tile);
            }
            counter++;
        }
        System.out.println(counter);
    }
    @Test
    void loadTiles() throws CardEffectException, IOException {
        List<Tile> list = controller.getPileOfTile();
        List<Tile> expected = tileParserLoader.loadTiles();
        printTile(list);
        printTile(expected);

        assertTrue(sameElementsWithSameCount(list, expected));
    }
}