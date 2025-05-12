package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Model.TileParserLoader;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.util.List;


public class TuiTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;
    private Tile[][] tmp = new Tile[5][7];
    Player p = new Player(123456 , true , 0);
    private TileParserLoader pileMaker = new TileParserLoader();
    List<Tile> pileOfTile = pileMaker.loadTiles();


//    @BeforeEach
//    public void setUpStreams() {
//        outContent = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outContent));
//    }
//
//    @AfterEach
//    public void restoreStreams() {
//        System.setOut(originalOut);
//        System.setIn(originalIn);
//    }

    @Test
    void testTUIViewAskString() {
        String input = "TestString\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        TUIView tui = new TUIView();
        String result = tui.askString();

        assertEquals("TestString", result);
    }

    @Test
    void testTUIViewAskIndex() {
        String input = "2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        TUIView tui = new TUIView();
        int index = tui.askIndex();

        assertEquals(1, index);
    }

    @Test
    void testTUIViewAskCoordinate() {
        String input = "5\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        TUIView tui = new TUIView();
        int[] coords = tui.askCordinate();

        assertArrayEquals(new int[]{0, 0}, coords);
    }

    @Test
    void testViewStart(){
        TUIView tui = new TUIView();
        tui.start();
        tui.inform("ciao");

    }

    @Test
    void testPrintDashBoard(){
        TUIView tui = new TUIView();
        Tile[][] tmp = p.getDashMatrix();
        tmp[1][3] = new Cannon(4,3,3,3,false,0);
        tmp[1][4]= new StorageUnit(2,1,2,3,3,true,0);
        switch (tmp[1][4]){
            case StorageUnit s -> {
                s.addGood(Colour.RED);
                s.addGood(Colour.GREEN);
                s.addGood(Colour.BLUE);
            }
            default -> {}
        }
        Tile tmp2 = new Shield(2,2,1,0,0);
        tmp2.rotateRight();
        tmp2.rotateRight();
        tmp2.rotateRight();
        tmp[1][2]=tmp2;
        tmp[2][2] = new EnergyCell(0 , 1, 2, 3 , 2,0);
        tmp[2][4] = new MultiJoint(3,3,3,3,0);
        tmp[2][5] = new HousingUnit(1,2,1,2,Human.HUMAN,0);
        tmp[3][2] = new MultiJoint(3,3,3,3,0);
        tmp[4][2] = new Engine(2,3,6,1,false,0);
        tmp[3][1] = new Engine(3,3,6,1,false,0);
        tmp[4][1] = new MultiJoint(3,3,3,3,0);
        tmp[2][1] = new Cannon(4,3,3,3,false,0);
        tmp[1][1] = new MultiJoint(3,3,3,3,0);
        tmp[3][5] = new EnergyCell(0,1,0,3, 2,0);
        tmp[4][5] = new Shield(1,1,2,1,0);
//        tmp[3][4] = new MultiJoint(3,3,1,1);
//        tmp[4][4] = new EnergyCell(0,1,1,3, 2);
//        tmp[0][1] = new MultiJoint(1,1,1,1);
//        tmp[0][2] = new MultiJoint(3,3,3,3);
        tui.printDashShip(tmp);
        p.modifyDASH(tmp);
        p.setGamePhase(GamePhase.CARD_EFFECT);
        p.controlAssembly(2,3);
        tmp =p.getDashMatrix();
        tui.printDashShip(tmp);
        tui.printPileCovered();
        tui.printPileShown(pileOfTile);
        tui.updateState(GamePhase.BOARD_SETUP);
        tui.updateView("paperino" , 3 , 2 , 5 , true , false , 11 , 2);

    }







}
