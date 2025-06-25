package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player demoPlayer;
    private Player normalPlayer;

    @BeforeEach
    void setUp() {
        demoPlayer = new Player(1, true, 42);
        normalPlayer = new Player(2, false, 99);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(1, demoPlayer.getId());
        assertEquals(42, demoPlayer.getIdPhoto());
        assertTrue(demoPlayer.isConnected());
        assertEquals(0, demoPlayer.getCredits());
        assertEquals(0, demoPlayer.getLap());
        assertEquals(0, demoPlayer.getPos());
        assertFalse(demoPlayer.isEliminated());
        assertNotNull(demoPlayer.getDashMatrix()[2][3]);
    }

    @Test
    void testAddTileInvalidPosition() {
        assertThrows(BusinessLogicException.class, () -> demoPlayer.addTile(2,3, new EmptySpace()));
    }

    @Test
    void testAlienFlagsToggle() {
        assertFalse(demoPlayer.presencePurpleAlien());
        demoPlayer.setPurpleAlien();
        assertTrue(demoPlayer.presencePurpleAlien());
        demoPlayer.setPurpleAlien();
        assertFalse(demoPlayer.presencePurpleAlien());

        assertFalse(demoPlayer.presenceBrownAlien());
        demoPlayer.setBrownAlien();
        assertTrue(demoPlayer.presenceBrownAlien());
        demoPlayer.setBrownAlien();
        assertFalse(demoPlayer.presenceBrownAlien());
    }

    @Test
    void testLapAndPosition() {
        normalPlayer.setLap(3);
        normalPlayer.setPos(5);
        assertEquals(3, normalPlayer.getLap());
        assertEquals(5, normalPlayer.getPos());
    }

    @Test
    void testAddCredits() {
        demoPlayer.addCredits(10);
        assertEquals(10, demoPlayer.getCredits());
        demoPlayer.addCredits(-4);
        assertEquals(6, demoPlayer.getCredits());
    }

    @Test
    void testDiscardPileAndCheck() throws BusinessLogicException {
        demoPlayer.setGamePhase(GamePhase.DRAW_PHASE);
        demoPlayer.addToDiscardPile(new EmptySpace());
        demoPlayer.addToDiscardPile(new StorageUnit(1,2,3,4,2,false,99));
        assertEquals(2, demoPlayer.getTilesInDiscardPile().size());
        int count = demoPlayer.checkDiscardPile();
        assertEquals(1, count);

        normalPlayer.setGamePhase(GamePhase.BOARD_SETUP);
        normalPlayer.addToDiscardPile(new EmptySpace());
        normalPlayer.addToDiscardPile(new EmptySpace());
        assertThrows(BusinessLogicException.class, () -> normalPlayer.addToDiscardPile(new EmptySpace()));
    }

    @Test
    void testGetTotalHumanEnergyGood() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 7; j++)
                custom[i][j] = new EmptySpace();

        HousingUnit hu = new HousingUnit(1,1,1,1, Human.HUMAN, 7);
        hu.addHuman(Human.HUMAN);
        hu.addHuman(Human.PURPLE_ALIEN);
        custom[0][0] = hu;

        EnergyCell ec = new EnergyCell(1,1,1,1, 3, 8);
        custom[1][1] = ec;

        StorageUnit su = new StorageUnit(1,1,1,1, /* max= */ 5, true, 9);
        su.addGood(Colour.BLUE);
        su.addGood(Colour.RED);
        custom[2][2] = su;

        demoPlayer.modifyDASH(custom);

        assertEquals(Status.USED, demoPlayer.validityCheck(0,0));
        assertEquals(1, demoPlayer.getTotalHuman());
        assertEquals(3, demoPlayer.getTotalEnergy());
        assertEquals(2, demoPlayer.getTotalGood());
    }

    @Test
    void testAddAndRemoveTile() throws BusinessLogicException {
        demoPlayer.addTile(0,3, new StorageUnit(1,2,3,4,2,false,99));
        assertEquals(Status.USED, demoPlayer.validityCheck(0,3));
        assertTrue(demoPlayer.getTile(0,3) instanceof StorageUnit);
        demoPlayer.removeTile(0,3);
        assertEquals(Status.FREE, demoPlayer.validityCheck(0,3));
        assertTrue(demoPlayer.getTile(0,3) instanceof EmptySpace);
    }

    @Test
    void testThrowDiceRange() {
        for(int i=0;i<100;i++){
            int r = demoPlayer.throwDice();
            assertTrue(r >= 1 && r <= 6);
        }
    }

    @Test
    void testCheckPresentValue() {
        Tile t = new Cannon(1,2,0,4,false,17);
        assertTrue(demoPlayer.checkPresentValue(t, 1));
        assertFalse(demoPlayer.checkPresentValue(t, 3));
    }

    @Test
    void testResetValidity() {
        // prepare id at (0,5)
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        EmptySpace e = new EmptySpace();
        custom[0][5] = e;
        demoPlayer.modifyDASH(custom);
        demoPlayer.resetValidity(e.getIdTile());
        assertEquals(Status.FREE, demoPlayer.validityCheck(0,5));
    }

    @Test
    void testGetTotalListOfGoodSorted() throws BusinessLogicException {
        StorageUnit su1 = new StorageUnit(1,1,1,1, 5, true,  99);
        su1.addGood(Colour.GREEN);
        su1.addGood(Colour.RED);
        StorageUnit su2 = new StorageUnit(2,2,2,2, 5, true, 100);
        su2.addGood(Colour.BLUE);

        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[0][0] = su1;
        custom[0][1] = su2;
        demoPlayer.modifyDASH(custom);

        List<Colour> goods = demoPlayer.getTotalListOfGood();
        List<Colour> expected = new ArrayList<>(goods);
        Collections.sort(expected);
        assertEquals(expected, goods);
    }


    @Test
    void testRemoveFromDirections() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        HousingUnit hu = new HousingUnit(1,1,1,1, Human.HUMAN, 7);
        custom[0][4] = hu;
        demoPlayer.modifyDASH(custom);
        assertTrue(demoPlayer.removeFrom0(8));
        assertFalse(demoPlayer.removeFrom0(8));
    }

    @Test
    void testIsOutOfBoundsAndIsolated() {
        assertTrue(demoPlayer.isIsolated(0,0));
        assertFalse(demoPlayer.isOutOfBounds(0,0));
        assertTrue(demoPlayer.isOutOfBounds(-1,0));
    }

    @Test
    void testResetValiditySecondBranch() {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        EmptySpace e = new EmptySpace();
        custom[0][6] = e;
        demoPlayer.modifyDASH(custom);

        demoPlayer.resetValidity(e.getIdTile());
        assertEquals(Status.FREE, demoPlayer.validityCheck(0,6));
    }

    @Test
    void testCheckNoConnectorNorth() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        Cannon northCannon = new Cannon(0,0,0,1,false,99);
        custom[2][4] = northCannon;
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.checkNoConnector(0, 8));
    }

    @Test
    void testCheckNoConnectorEastFalse() {
        assertFalse(demoPlayer.checkNoConnector(1, 5));
    }

    @Test
    void testRemoveFrom3WestEdge() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        custom[3][0] = new HousingUnit(1,1,1,1,Human.HUMAN,7);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.removeFrom3(8));
        assertFalse(demoPlayer.removeFrom3(8));
    }

    @Test
    void testRemoveFrom2SouthEdge() throws BusinessLogicException {
        // riga 4 e colonna tmp = dir2-4
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        custom[4][2] = new HousingUnit(1,1,1,1,Human.HUMAN,7);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.removeFrom2(6));
        assertFalse(demoPlayer.removeFrom2(6));
    }

    @Test
    void testRemoveFrom1EastEdge() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        custom[1][6] = new HousingUnit(1,1,1,1,Human.HUMAN,7);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.removeFrom1(6));
        assertFalse(demoPlayer.removeFrom1(6));
    }

    @Test
    void testCountExposedConnectorsEmpty() {
        assertEquals(4, demoPlayer.countExposedConnectors());
    }

    @Test
    void testCountExposedConnectorsAfterAddingEngine() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        Engine e = new Engine(1,1,1,1,false,7);
        custom[3][3] = e;
        demoPlayer.modifyDASH(custom);

        assertEquals(4, demoPlayer.countExposedConnectors());
    }

    @Test
    void testControlAssemblyRemovesBadConnections() throws BusinessLogicException {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        Cannon bad = new Cannon(0,4,0,0,false,7);
        Cannon victim = new Cannon(0,4,0,0,false,8);
        custom[2][3] = bad;
        custom[2][4] = victim;
        demoPlayer.modifyDASH(custom);

        demoPlayer.controlAssembly(2,3);
        assertEquals(Status.FREE, demoPlayer.validityCheck(2,4));
    }

    @Test
    void testControlOfConnectionLinksHousingUnits() {
        Tile[][] custom = new Tile[5][7];
        for(int i=0;i<5;i++) for(int j=0;j<7;j++) custom[i][j] = new EmptySpace();
        HousingUnit h1 = new HousingUnit(1,1,1,1, Human.HUMAN, 7);
        HousingUnit h2 = new HousingUnit(1,1,1,1, Human.PRADELLA, 8);
        custom[2][3] = h1;
        custom[2][4] = h2;
        demoPlayer.modifyDASH(custom);

        assertEquals(Human.PRADELLA, h1.getTypeOfConnections());
        demoPlayer.controlOfConnection();
        assertTrue(h1.isConnected());
        assertTrue(h2.isConnected());
        assertEquals(Human.PRADELLA, h1.getTypeOfConnections());
    }

    @Test
    void testConnectedSetterGetter() {
        assertTrue(demoPlayer.isConnected());
        demoPlayer.setConnected(false);
        assertFalse(demoPlayer.isConnected());
        demoPlayer.setConnected(true);
        assertTrue(demoPlayer.isConnected());

        assertTrue(normalPlayer.isConnected());
        normalPlayer.setConnected(false);
        assertFalse(normalPlayer.isConnected());
    }

    @Test
    void testLastTileAndGamePhaseAndEliminated() {
        assertNull(demoPlayer.getLastTile());
        EmptySpace e = new EmptySpace();
        demoPlayer.setLastTile(e);
        assertSame(e, demoPlayer.getLastTile());

        assertNull(demoPlayer.getGamePhase());
        demoPlayer.setGamePhase(GamePhase.BOARD_SETUP);
        assertEquals(GamePhase.BOARD_SETUP, demoPlayer.getGamePhase());

        assertFalse(demoPlayer.isEliminated());
        demoPlayer.setEliminated();
        assertTrue(demoPlayer.isEliminated());
    }

    @Test
    void testCheckNearAlienNoAdjacencies() throws BusinessLogicException {
        assertFalse(demoPlayer.presencePurpleAlien());
        assertFalse(demoPlayer.presenceBrownAlien());
        demoPlayer.checkNearAlien(2,2);
        assertFalse(demoPlayer.presencePurpleAlien());
        assertFalse(demoPlayer.presenceBrownAlien());
    }

    @Test
    void testLastTileSetterGetter() {
        Tile someTile = new EmptySpace();
        assertNull(demoPlayer.getLastTile());
        demoPlayer.setLastTile(someTile);
        assertSame(someTile, demoPlayer.getLastTile());
    }

    @Test
    void testPrivateConnectedMethodViaReflection() throws Exception {
        var m = Player.class.getDeclaredMethod("connected", int.class, int.class);
        m.setAccessible(true);
        assertFalse((boolean) m.invoke(demoPlayer, 0, 2));
        assertFalse((boolean) m.invoke(demoPlayer, 2, 0));
        assertTrue((boolean) m.invoke(demoPlayer, 5, 5));
        assertTrue((boolean) m.invoke(demoPlayer, 3, 8));
        assertTrue((boolean) m.invoke(demoPlayer, 9, 3));
        assertFalse((boolean) m.invoke(demoPlayer, 2, 4));
    }

    @Test
    void testCheckNoConnectorNorthTrue() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[0][1] = new Cannon(0, 1, 1, 1, false, 101);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.checkNoConnector(0, 5));
    }

    @Test
    void testCheckNoConnectorNorthFalse() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[0][1] = new Cannon(2, 1, 1, 1, false, 102);
        demoPlayer.modifyDASH(custom);

        assertFalse(demoPlayer.checkNoConnector(0, 5));
    }

    @Test
    void testCheckNoConnectorEastTrue() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[2][6] = new Cannon(1, 0, 1, 1, false, 103);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.checkNoConnector(1, 7));
    }

    @Test
    void testCheckNoConnectorSouthTrue() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[4][3] = new Cannon(1, 1, 0, 1, false, 105);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.checkNoConnector(2, 7));
    }

    @Test
    void testCheckNoConnectorSouthFalse() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[4][3] = new Cannon(1, 1, 5, 1, false, 106);
        demoPlayer.modifyDASH(custom);

        assertFalse(demoPlayer.checkNoConnector(2, 7));
    }

    @Test
    void testCheckNoConnectorWestTrue() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[1][0] = new Cannon(1, 1, 1, 0, false, 107);
        demoPlayer.modifyDASH(custom);

        assertTrue(demoPlayer.checkNoConnector(3, 6));
    }

    @Test
    void testCheckNoConnectorWestFalse() {
        Tile[][] custom = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) custom[i][j] = new EmptySpace();
        custom[1][0] = new Cannon(1, 1, 1, 2, false, 108);
        demoPlayer.modifyDASH(custom);

        assertFalse(demoPlayer.checkNoConnector(3, 6));
    }

    @Test
    void testCheckNoConnectorNothingUsed() {
        assertFalse(demoPlayer.checkNoConnector(0, 5));
        assertFalse(demoPlayer.checkNoConnector(1, 7));
        assertFalse(demoPlayer.checkNoConnector(2, 7));
        assertFalse(demoPlayer.checkNoConnector(3, 6));
    }

    @Test
    void testControlAssembly2_AllConnected() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) dash[i][j] = new EmptySpace();
        HousingUnit center = new HousingUnit(1,1,1,1, Human.HUMAN, 10);
        HousingUnit right  = new HousingUnit(1,1,1,1, Human.HUMAN, 11);
        dash[2][2] = center;
        dash[2][3] = right;
        demoPlayer.modifyDASH(dash);
        boolean removed = demoPlayer.controlAssembly2(2,2);
        assertFalse(removed);
        assertTrue(demoPlayer.getTile(2,2) instanceof HousingUnit);
        assertTrue(demoPlayer.getTile(2,3) instanceof HousingUnit);
    }

    @Test
    void testControlAssembly2_WrongConnection_RemovesNeighbor() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) dash[i][j] = new EmptySpace();

        HousingUnit center = new HousingUnit(1,1,1,1, Human.HUMAN, 20);
        HousingUnit neighbor = new HousingUnit(2,2,2,2, Human.BROWN_ALIEN, 21);
        dash[2][2] = center;
        dash[2][3] = neighbor;
        demoPlayer.modifyDASH(dash);
        boolean removed = demoPlayer.controlAssembly2(2,2);
        assertTrue(removed);
        assertTrue(demoPlayer.getTile(2,3) instanceof EmptySpace);
    }

    @Test
    void testControlAssembly2_OutOfBoundsAndFreeSkipped() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) dash[i][j] = new EmptySpace();
        HousingUnit center = new HousingUnit(1,1,1,1, Human.HUMAN, 10);
        dash[0][0] = center;
        demoPlayer.modifyDASH(dash);

        boolean removed = demoPlayer.controlAssembly2(0,0);
        assertTrue(removed, "Mi aspetto true perché l'isolated removal toglie il pezzo");
        assertTrue(demoPlayer.getTile(0,0) instanceof EmptySpace, "Dopo la chiamata la cella (0,0) dev'essere vuota");
    }

    @Test
    void testControlAssembly2_PartialZeroConnector_Skipped() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i = 0; i < 5; i++) for (int j = 0; j < 7; j++) dash[i][j] = new EmptySpace();
        Cannon cannon   = new Cannon(0,1,1,1,false,40);
        HousingUnit hu  = new HousingUnit(1,1,1,1, Human.HUMAN, 41);
        dash[2][2] = cannon;
        dash[1][2] = hu;
        demoPlayer.modifyDASH(dash);

        boolean removed = demoPlayer.controlAssembly2(2,2);
        assertTrue(removed, "Mi aspetto true perché viene rimosso il vicino FREE e poi l'isolated center");
        assertTrue(demoPlayer.getTile(2,2) instanceof EmptySpace, "Il cannon (2,2) deve essere rimosso");
        assertTrue(demoPlayer.getTile(1,2) instanceof EmptySpace, "L'HousingUnit (1,2) deve essere rimosso");
    }

    @Test
    void testCheckNearAlienHandlesPurpleAlien() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i=0;i<5;i++) for(int j=0;j<7;j++) dash[i][j]=new EmptySpace();

        HousingUnit hu = new HousingUnit(1,1,1,1, Human.PURPLE_ALIEN, 17);

        hu.addHuman(Human.PURPLE_ALIEN);
        hu.setTypeOfConnections(Human.PURPLE_ALIEN);

        dash[3][2] = hu;
        demoPlayer.modifyDASH(dash);

        assertFalse(demoPlayer.presencePurpleAlien());
        demoPlayer.checkNearAlien(2,2);
        assertTrue(demoPlayer.presencePurpleAlien());
        assertEquals(0, hu.getListOfToken().size());
    }

    @Test
    void testCheckNearAlienHandlesBrownAlien() throws BusinessLogicException {
        Tile[][] dash = new Tile[5][7];
        for (int i=0;i<5;i++) for(int j=0;j<7;j++) dash[i][j]=new EmptySpace();

        HousingUnit hu = new HousingUnit(1,1,1,1, Human.BROWN_ALIEN, 23);
        hu.addHuman(Human.BROWN_ALIEN);
        hu.setTypeOfConnections(Human.BROWN_ALIEN);

        dash[3][2] = hu;
        demoPlayer.modifyDASH(dash);

        assertFalse(demoPlayer.presenceBrownAlien());
        demoPlayer.checkNearAlien(2,2);
        assertTrue(demoPlayer.presenceBrownAlien());
        assertEquals(0, hu.getListOfToken().size());
    }


}