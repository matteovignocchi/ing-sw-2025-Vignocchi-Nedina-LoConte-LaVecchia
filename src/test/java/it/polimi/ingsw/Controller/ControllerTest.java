package it.polimi.ingsw.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerTest {
    private Controller controller;
    @Mock
    private VirtualView view1;
    @Mock
    private VirtualView view2;
    private Set<String> loggedInUsers;
    private List<Integer> endedGames;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        loggedInUsers = new HashSet<>();
        endedGames = new ArrayList<>();
        Consumer<Integer> onGameEnd = endedGames::add;
        controller = new Controller(true, 1, 4, onGameEnd, loggedInUsers);
    }

    @Test
    void testAddPlayerAndGetters() throws Exception {
        controller.addPlayer("Alice", view1);
        assertTrue(controller.getPlayersByNickname().containsKey("Alice"));
        assertEquals(view1, controller.getViewCheck("Alice"));
        assertEquals(controller.getPlayerByNickname("Alice"), controller.getPlayerCheck("Alice"));
        // Duplicate
        assertThrows(BusinessLogicException.class, () -> controller.addPlayer("Alice", view2));
    }

    @Test
    void testAddPlayerGameFull() throws Exception {
        // Fill to max
        controller = new Controller(true, 2, 2, endedGames::add, loggedInUsers);
        controller.addPlayer("P1", view1);
        controller.addPlayer("P2", view2);
        assertThrows(BusinessLogicException.class, () -> controller.addPlayer("P3", view1));
    }

    @Test
    void testGetViewAndPlayerExceptions() {
        assertThrows(BusinessLogicException.class, () -> controller.getPlayerCheck("Nobody"));
        assertThrows(BusinessLogicException.class, () -> controller.getViewCheck("Nobody"));
    }

    @Test
    void testGetNickByPlayer() throws Exception {
        controller.addPlayer("Bob", view1);
        assertEquals("Bob", controller.getNickByPlayer(controller.getPlayerByNickname("Bob")));
        assertThrows(BusinessLogicException.class, () -> controller.getNickByPlayer(new it.polimi.ingsw.galaxytrucker.Model.Player(99, true, 0)));
    }

    @Test
    void testInformMethods() throws Exception {
        controller.addPlayer("A", view1);
        controller.inform("msg", "A");
        verify(view1).inform("msg");
        controller.informAndNotify("msg2", "A");
        verify(view1).inform("msg2");
        verify(view1, atLeastOnce()).updateGameState(anyString());
        controller.reportError("err", "A");
        verify(view1).reportError("err");
    }

    @Test
    void testBroadcasts() throws Exception {
        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        reset(view1, view2);
        controller.broadcastInform("hello");
        verify(view1).inform("hello");
        verify(view2).inform("hello");
        reset(view1, view2);
        // Except
        it.polimi.ingsw.galaxytrucker.Model.Player pA = controller.getPlayerByNickname("A");
        controller.broadcastInformExcept("hi", pA);
        verify(view1, never()).inform(anyString());
        verify(view2).inform("hi");
    }

    @Test
    void testPrintAndUpdate() throws Exception {
        controller.addPlayer("X", view1);
        // printPlayerDashboard should receive a String[][]
        controller.printPlayerDashboard(view1, controller.getPlayerByNickname("X"), "X");
        verify(view1).printPlayerDashboard(any(String[][].class));

        // printListOfGoods should receive a List<String>
        List<Colour> goods = Arrays.asList(Colour.RED, Colour.BLUE);
        controller.printListOfGoods(goods, "X");
        verify(view1).printListOfGoods(anyList());

        // updateGamePhase
        controller.updateGamePhase("X", view1, GamePhase.BOARD_SETUP);
        // può essere chiamato più volte (addPlayer e updateGamePhase)
        verify(view1, atLeastOnce()).updateGameState(anyString());
    }

    @Test
    void testCountAndGameState() throws Exception {
        assertEquals(0, controller.countConnectedPlayers());
        controller.addPlayer("U", view1);
        assertEquals(1, controller.countConnectedPlayers());
        // After adding, PLAYER is in WAITING_IN_LOBBY != WAITING_FOR_PLAYERS, so game is started
        assertTrue(controller.isGameStarted());
        assertEquals(4, controller.getMaxPlayers());
    }

    @Test
    void testDisconnectReconnect() throws Exception {
        controller.addPlayer("Z", view1);
        loggedInUsers.add("Z");
        controller.markDisconnected("Z");
        assertFalse(controller.getPlayerByNickname("Z").isConnected());
        assertFalse(loggedInUsers.contains("Z"));
        // Reconnect
        controller.markReconnected("Z", view2);
        assertTrue(controller.getPlayerByNickname("Z").isConnected());
        verify(view2).updateGameState(anyString());
    }

    @Test
    void testHandleElimination() throws Exception {
        controller.addPlayer("E", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("E");
        // mark as eliminated and connected
        p.setEliminated();
        p.setConnected(true);
        reset(view1);
        controller.handleElimination(p);
        // Should inform eliminated player and update state
        verify(view1).inform(anyString());
        verify(view1).updateGameState(anyString());
    }

    @Test
    void testReinitializeAfterLoad() throws Exception {
        controller.addPlayer("A1", view1);
        controller.reinitializeAfterLoad(hourglass -> {}, id -> {});
        assertTrue(controller.getPlayersByNickname().containsKey("A1")); // players remain
        assertNotNull(controller.pingScheduler);
    }

    @Test
    void testGetCoveredTileEmpty() throws BusinessLogicException {
        controller.pileOfTile.clear();
        assertThrows(BusinessLogicException.class, () -> controller.getCoveredTile("noone"));
        // after adding player and pile
    }

    @Test
    void testChooseUncoveredTileExceptions() throws Exception {
        controller.addPlayer("A", view1);
        // No shown tiles yet
        assertThrows(BusinessLogicException.class, () -> controller.chooseUncoveredTile("A", 123));
    }

    @Test
    void testGetReservedTileException() throws Exception {
        controller.addPlayer("R", view1);
        assertThrows(BusinessLogicException.class, () -> controller.getReservedTile("R", 1));
    }

    @Test
    void testLookAtDashBoard() throws Exception {
        controller.addPlayer("D", view1);
        String[][] dash = controller.lookAtDashBoard("D");
        assertNotNull(dash);
    }

    @Test
    void testAskPlayerDecisionAndCoordinatesAndIndex() throws Exception {
        controller.addPlayer("P", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("P");
        // decision true
        when(view1.askWithTimeout(anyString())).thenReturn(true);
        assertTrue(controller.askPlayerDecision("q?", p));
        // decision IOException -> false and disconnect
        p.setConnected(true);
        doThrow(new IOException("fail")).when(view1).askWithTimeout(anyString());
        assertFalse(controller.askPlayerDecision("q?", p));
        assertFalse(p.isConnected());
        // coordinates
        p.setConnected(true);
        int[] coords = new int[]{1,2};
        when(view1.askCoordsWithTimeout()).thenReturn(coords);
        int[] got = controller.askPlayerCoordinates(p);
        assertArrayEquals(coords, got);
        // coords IOException -> null and disconnect
        p.setConnected(true);
        doThrow(new IOException()).when(view1).askCoordsWithTimeout();
        assertNull(controller.askPlayerCoordinates(p));
        assertFalse(p.isConnected());
        // index null
        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(null);
        assertNull(controller.askPlayerIndex(p, 5));
        // index out of bounds then valid
        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(-1, 7, 3);
        // stub reportError to no-op
        doNothing().when(view1).reportError(anyString());
        assertEquals(3, controller.askPlayerIndex(p, 5));
    }

    @Test
    void testPileAndShownTilesMethods() throws Exception {
        // initial pile size
        int initialPile = controller.getPileOfTile().size();
        Tile t = controller.getTile(0);
        assertNotNull(t);
        assertEquals(initialPile -1, controller.getPileOfTile().size());
        // shown empty
        assertTrue(controller.getShownTiles().isEmpty());
        assertEquals("PIEDONIPRADELLA", controller.jsongetShownTiles());
        // add shown
        controller.addToShownTile(t);
        List<Tile> shown = controller.getShownTiles();
        assertEquals(1, shown.size());
        assertEquals(t, shown.get(0));
        // json now list
        String json = controller.jsongetShownTiles();
        assertTrue(json.contains("type"));
        // getShownTile
        Tile t2 = controller.getShownTile(0);
        assertEquals(t, t2);
        assertTrue(controller.getShownTiles().isEmpty());
    }

    @Test
    void testGetIsDemoAndTotals() throws Exception {
        controller.addPlayer("X2", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("X2");
        assertTrue(controller.getIsDemo());
        assertEquals(p.getTotalEnergy(), controller.getTotalEnergy(p));
        assertEquals(p.getTotalGood(),  controller.getTotalGood(p));
    }
}
