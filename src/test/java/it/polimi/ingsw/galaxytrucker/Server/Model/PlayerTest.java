package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;
    @BeforeEach
    void setUp() {
        player = new Player(1,false);
    }
    @Test
    void testAddTileException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MultiJoint multiJoint = new MultiJoint(0,3,3,0);
            player.addTile(1,0,multiJoint);});
    }
    @Test
    void testAddTile() {
        MultiJoint multiJoint = new MultiJoint(0,3,3,0);
        player.addTile(3,3, multiJoint);
        assertEquals(Status.USED,player.validityCheck(3,3));
    }

    @Test void testRemoveTile() {
        MultiJoint multiJoint = new MultiJoint(0,3,3,0);
        player.addTile(3,3, multiJoint);
        assertEquals(Status.USED,player.validityCheck(3,3));
        player.removeTile(3,3);
        assertEquals(Status.FREE,player.validityCheck(3,3));
    }

    @Test
    void testControlCannonRemoving() {
        MultiJoint multiJoint = new MultiJoint(0, 0 ,1, 2);
        MultiJoint multiJoint1 = new MultiJoint(0, 3, 0, 0);
        Cannon cannon0 = new Cannon(4, 2,0,1,false);
        player.addTile(3,5,cannon0);
        player.addTile(2,5,multiJoint);
        player.addTile(1,5,multiJoint1);
        player.controlCannon();
        assertAll(
                () -> assertEquals(Status.FREE, player.validityCheck(2,5)),
                () -> assertEquals(Status.FREE, player.validityCheck(1,5))
        );
        Cannon cannon1 = new Cannon(2, 4,0,1,false);
        player.addTile(3,1,cannon1);
        player.addTile(3,2,multiJoint);
        player.addTile(3,3,multiJoint1);
        player.controlCannon();
        assertAll(
                () -> assertEquals(Status.FREE, player.validityCheck(3,2)),
                () -> assertEquals(Status.FREE, player.validityCheck(3,3))
        );
        Cannon cannon2 = new Cannon(0, 2,4,1,false);
        player.addTile(1,2,cannon2);
        player.addTile(2,2,multiJoint);
        player.addTile(3,2,multiJoint1);
        player.controlCannon();
        assertAll(
                () -> assertEquals(Status.FREE, player.validityCheck(2,2)),
                () -> assertEquals(Status.FREE, player.validityCheck(3,2))
        );
        Cannon cannon3 = new Cannon(3, 1,0,4,false);
        player.addTile(1,5,cannon3);
        player.addTile(1,4,multiJoint);
        player.addTile(1,3,multiJoint1);
        player.controlCannon();
        assertAll(
                () -> assertEquals(Status.FREE, player.validityCheck(1,4)),
                () -> assertEquals(Status.FREE, player.validityCheck(1,3))
        );
    }

    @Test
    void testControlEngine() {
        Engine engine0 = new Engine(6,2,0,3,false);
        Engine engine1 = new Engine(2,6,0,3,false);
        Engine engine3 = new Engine(1,2,0,6,false);
        player.addTile(3,3,engine0);
        player.addTile(3,1,engine1);
        player.addTile(3,5,engine3);
        player.controlEngine();
        assertAll(
                () -> assertEquals(Status.FREE, player.validityCheck(3,3)),
                () -> assertEquals(Status.FREE, player.validityCheck(3,1)),
                () -> assertEquals(Status.FREE, player.validityCheck(3,5))
        );
    }

    @Test
    void testCheckNoConnectors() {
        MultiJoint multiJoint0 = new MultiJoint(0,2,3,1);
        MultiJoint multiJoint1 = new MultiJoint(1,2,3,1);
        MultiJoint multiJoint2 = new MultiJoint(2,2,0,1);
        MultiJoint multiJoint3 = new MultiJoint(3,2,0,1);
        player.addTile(1,2,multiJoint0);
        player.addTile(1,5,multiJoint1);
        player.addTile(3,4,multiJoint2);
        player.addTile(4,1,multiJoint3);
        assertAll(
                () -> assertTrue(player.checkNoConnector(0, 6)),
                () -> assertFalse(player.checkNoConnector(1,9)),
                () -> assertTrue(player.checkNoConnector(2,8)),
                () -> assertFalse(player.checkNoConnector(3,9))
        );
    }

    @Test
    void testFirstCheckAssembly(){
        MultiJoint multiJoint0 = new MultiJoint(3,3,3,3);
        MultiJoint multiJoint1 = new MultiJoint(1,2,1,2);
        MultiJoint multiJoint2 = new MultiJoint(2,1,2,1);
        MultiJoint multiJoint3 = new MultiJoint(0,1,2,3);
        HousingUnit h = new HousingUnit(3,3,3,3 ,Human.HUMAN);
        player.addTile(2,3,h);
        player.addTile(1,1,multiJoint0);
        player.addTile(1,2,multiJoint0);
        player.addTile(1,3,multiJoint0);
        player.addTile(1,5,multiJoint0);
        player.addTile(2,5,multiJoint0);
        player.addTile(4,2,multiJoint0);
        player.addTile(3,6,multiJoint0);
        player.addTile(4,4,multiJoint0);
        player.addTile(1,4,multiJoint1);
        player.addTile(2,1,multiJoint1);
        player.addTile(2,4,multiJoint1);
        player.addTile(3,4,multiJoint1);
        player.addTile(3,5,multiJoint3);
        player.addTile(2,2,multiJoint3);
        player.addTile(3,1,multiJoint3);
        player.addTile(4,1,multiJoint3);
        player.controlAssembly();
        player.controlAssemblyWithCordinate(2,3);
        assertAll(
                ()->assertSame(Status.FREE, player.validityCheck(0,0)),
                ()->assertSame(Status.FREE, player.validityCheck(0,1)),
                ()->assertSame(Status.FREE, player.validityCheck(0,2)),
                ()->assertSame(Status.FREE, player.validityCheck(0,3)),
                ()->assertSame(Status.FREE, player.validityCheck(0,4)),
                ()->assertSame(Status.FREE, player.validityCheck(0,5)),
                ()->assertSame(Status.FREE, player.validityCheck(0,6)),
                ()->assertSame(Status.FREE, player.validityCheck(1,0)),
                ()->assertSame(Status.FREE, player.validityCheck(1,6)),
                ()->assertSame(Status.FREE, player.validityCheck(2,0)),
                ()->assertSame(Status.FREE, player.validityCheck(2,6)),
                ()->assertSame(Status.FREE, player.validityCheck(3,0)),
                ()->assertSame(Status.FREE, player.validityCheck(3,1)),
                ()->assertSame(Status.FREE, player.validityCheck(3,2)),
                ()->assertSame(Status.FREE, player.validityCheck(3,3)),
                ()->assertSame(Status.FREE, player.validityCheck(3,5)),
                ()->assertSame(Status.FREE, player.validityCheck(3,6)),
                ()->assertSame(Status.FREE, player.validityCheck(4,0)),
                ()->assertSame(Status.FREE, player.validityCheck(4,1)),
                ()->assertSame(Status.FREE, player.validityCheck(4,2)),
                ()->assertSame(Status.FREE, player.validityCheck(4,3)),
                ()->assertSame(Status.FREE, player.validityCheck(4,5)),
                ()->assertSame(Status.FREE, player.validityCheck(4,6)),

                ()->assertSame(Status.USED, player.validityCheck(1,1)),
                ()->assertSame(Status.USED, player.validityCheck(1,2)),
                ()->assertSame(Status.USED, player.validityCheck(1,3)),
                ()->assertSame(Status.USED, player.validityCheck(1,4)),
                ()->assertSame(Status.USED, player.validityCheck(1,5)),
                ()->assertSame(Status.USED, player.validityCheck(2,1)),
                ()->assertSame(Status.USED, player.validityCheck(2,2)),
                ()->assertSame(Status.USED, player.validityCheck(2,3)),
                ()->assertSame(Status.USED, player.validityCheck(2,4)),
                ()->assertSame(Status.USED, player.validityCheck(2,5)),
                ()->assertSame(Status.USED, player.validityCheck(3,4)),
                ()->assertSame(Status.USED, player.validityCheck(4,4))
                );


    }

}