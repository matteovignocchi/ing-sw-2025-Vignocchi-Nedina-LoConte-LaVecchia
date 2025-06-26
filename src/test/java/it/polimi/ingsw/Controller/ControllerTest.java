package it.polimi.ingsw.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.*;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectVisitor;
import it.polimi.ingsw.galaxytrucker.Model.Card.Deck;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard2;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
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
        assertThrows(BusinessLogicException.class, () -> controller.addPlayer("Alice", view2));
    }

    @Test
    void testAddPlayerGameFull() throws Exception {
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
    void testInformAndReportError() throws Exception {
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
        it.polimi.ingsw.galaxytrucker.Model.Player pA = controller.getPlayerByNickname("A");
        controller.broadcastInformExcept("hi", pA);
        verify(view1, never()).inform(anyString());
        verify(view2).inform("hi");
    }

    @Test
    void testPrintAndUpdate() throws Exception {
        controller.addPlayer("X", view1);
        controller.printPlayerDashboard(view1, controller.getPlayerByNickname("X"), "X");
        verify(view1).printPlayerDashboard(any(String[][].class));

        List<Colour> goods = Arrays.asList(Colour.RED, Colour.BLUE);
        controller.printListOfGoods(goods, "X");
        verify(view1).printListOfGoods(anyList());

        controller.updateGamePhase("X", view1, GamePhase.BOARD_SETUP);
        verify(view1, atLeastOnce()).updateGameState(anyString());
    }

    @Test
    void testCountAndGameState() throws Exception {
        assertEquals(0, controller.countConnectedPlayers());
        controller.addPlayer("U", view1);
        assertEquals(1, controller.countConnectedPlayers());
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

        controller.markReconnected("Z", view2);
        assertTrue(controller.getPlayerByNickname("Z").isConnected());
        verify(view2).updateGameState(anyString());
    }

    @Test
    void testHandleElimination() throws Exception {
        controller.addPlayer("E", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("E");
        p.setEliminated(); p.setConnected(true);
        reset(view1);
        controller.handleElimination(p);
        verify(view1).inform(anyString());
        verify(view1).updateGameState(anyString());
    }

    @Test
    void testReinitializeAfterLoad() throws Exception {
        controller.addPlayer("A1", view1);
        controller.reinitializeAfterLoad(h -> {}, id -> {});
        assertTrue(controller.getPlayersByNickname().containsKey("A1"));
        assertNotNull(controller.pingScheduler);
    }

    @Test
    void testGetCoveredTileEmpty() throws Exception {
        controller.pileOfTile.clear();
        controller.addPlayer("P", view1);
        assertThrows(BusinessLogicException.class, () -> controller.getCoveredTile("P"));
    }

    @Test
    void testChooseUncoveredTileExceptions() throws Exception {
        controller.addPlayer("A", view1);
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
        assertEquals(5, dash.length);
        assertEquals(7, dash[0].length);
    }

    @Test
    void testAskPlayerDecisionAndCoordinatesAndIndex() throws Exception {
        controller.addPlayer("P", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("P");
        when(view1.askWithTimeout(anyString())).thenReturn(true);
        assertTrue(controller.askPlayerDecision("q?", p));
        p.setConnected(true);
        doThrow(new IOException("fail")).when(view1).askWithTimeout(anyString());
        assertFalse(controller.askPlayerDecision("q?", p));
        assertTrue(p.isConnected());
        p.setConnected(true);
        int[] coords = new int[]{1,2};
        when(view1.askCoordsWithTimeout()).thenReturn(coords);
        int[] got = controller.askPlayerCoordinates(p);
        assertArrayEquals(coords, got);
        p.setConnected(true);
        doThrow(new IOException()).when(view1).askCoordsWithTimeout();
        assertNull(controller.askPlayerCoordinates(p));
        assertFalse(p.isConnected());
        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(null);
        assertNull(controller.askPlayerIndex(p, 5));
        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(-1, 7, 3);
        doNothing().when(view1).reportError(anyString());
        assertEquals(3, controller.askPlayerIndex(p, 5));
    }

    @Test
    void testPileAndShownTilesMethods() throws Exception {
        int initialPile = controller.getPileOfTile().size();
        Tile t = controller.getTile(0);
        assertNotNull(t);
        assertEquals(initialPile -1, controller.getPileOfTile().size());
        assertTrue(controller.getShownTiles().isEmpty());
        assertEquals("PIEDONIPRADELLA", controller.jsongetShownTiles());
        controller.addToShownTile(t);
        List<Tile> shown = controller.getShownTiles();
        assertEquals(1, shown.size());
        assertEquals(t, shown.get(0));
        String json = controller.jsongetShownTiles();
        assertTrue(json.contains("type"));
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

    @Test
    void testPingAllClientsAndGetGamePhaseForAll() throws Exception {
        controller = new Controller(false, 3, 2, endedGames::add, loggedInUsers);
        assertTrue(controller.getIsDemo() == false);
        assertNotNull(controller.getFlightCardBoard());
        assertNotNull(controller.decks);
        assertNotNull(controller.deck);

        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        Method ping = Controller.class.getDeclaredMethod("pingAllClients");
        ping.setAccessible(true);
        doThrow(new RuntimeException("fail")).when(view1).updateGameState("PING");
        doNothing().when(view2).updateGameState("PING");
        ping.invoke(controller);
        assertFalse(controller.getPlayerByNickname("A").isConnected());
        verify(view2).updateGameState("PING");
        Method getPhase = Controller.class.getDeclaredMethod("getGamePhaseForAll");
        getPhase.setAccessible(true);
        Object gp = getPhase.invoke(controller);
        assertEquals(it.polimi.ingsw.galaxytrucker.Model.GamePhase.WAITING_IN_LOBBY, gp);
    }

    @Test
    void testShutdownPingScheduler() throws Exception {
        controller.addPlayer("P1", view1);
        ScheduledExecutorService sched = controller.pingScheduler;
        controller.shutdownPing();
        assertTrue(sched.isShutdown());
    }

    @Test
    void testNotifyViewSetsTile() throws Exception {
        controller.addPlayer("X", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player p = controller.getPlayerCheck("X");
        p.setGamePhase(it.polimi.ingsw.galaxytrucker.Model.GamePhase.TILE_MANAGEMENT);
        Tile sample = controller.getTile(0);
        p.setLastTile(sample);
        reset(view1);
        controller.notifyView("X");
        verify(view1).updateGameState(anyString());
        verify(view1).updateMapPosition(anyMap());
        verify(view1).showUpdate(eq("X"), anyDouble(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyInt(), anyInt());
        ArgumentCaptor<String> capt = ArgumentCaptor.forClass(String.class);
        verify(view1).setTile(capt.capture());
        assertTrue(capt.getValue().contains("\"type\""));
    }

    @Test
    void testInformExceptionAndInformAndNotifyException() throws Exception {
        controller.addPlayer("Z", view1);
        it.polimi.ingsw.galaxytrucker.Model.Player pZ = controller.getPlayerCheck("Z");
        doThrow(new IOException("ioerr")).when(view1).inform("test");
        controller.inform("test", "Z");
        assertTrue(pZ.isConnected());
        controller.addPlayer("Y", view2);
        it.polimi.ingsw.galaxytrucker.Model.Player pY = controller.getPlayerCheck("Y");
        doThrow(new RuntimeException("err")).when(view2).inform("hello");
        controller.informAndNotify("hello", "Y");
        assertTrue(pY.isConnected());
    }

    @Test
    void testStartGameDemoAndNonDemo() throws Exception {
        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        reset(view1, view2);
        controller.startGame();
        verify(view1, atLeastOnce()).updateMapPosition(anyMap());
        verify(view1).setIsDemo(true);
        verify(view1).setStart();
        verify(view2).setStart();

        Controller nonDemo = new Controller(false, 5, 2, endedGames::add, loggedInUsers);
        nonDemo.addPlayer("X", view1);
        nonDemo.addPlayer("Y", view2);
        reset(view1, view2);
        nonDemo.startGame();
        verify(view1, atLeastOnce()).updateMapPosition(anyMap());
        verify(view2, atLeastOnce()).updateMapPosition(anyMap());
    }

    @Test
    void testGetCoveredTilePaths() throws Exception {
        controller.addPlayer("P1", view1);
        controller.pileOfTile.clear();
        assertThrows(BusinessLogicException.class, () -> controller.getCoveredTile("P1"));

        controller.pileOfTile = new ArrayList<>();
        controller.pileOfTile.addAll(new TileParserLoader().loadTiles());

        doThrow(new RuntimeException("fail")).when(view1).updateGameState(anyString());
        assertThrows(RuntimeException.class, () -> controller.getCoveredTile("P1"));

        reset(view1);
        String json = controller.getCoveredTile("P1");
        assertNotNull(json);
        assertTrue(json.contains("\"type\""));
    }

    @Test
    void testChooseDropPlaceAndReservedTile() throws Exception {
        controller.addPlayer("P2", view1);
        Player real = controller.getPlayerCheck("P2");
        Player spyPlayer = spy(real);
        controller.getPlayersByNickname().put("P2", spyPlayer);
        Tile t = controller.getTile(0);
        String goodJson = controller.tileSerializer.toJson(t);

        // --- CHOOSE UNCOVERED TILE ---

        reset(view1);
        controller.shownTile.clear();
        assertThrows(BusinessLogicException.class, () -> controller.chooseUncoveredTile("P2", 999));
        controller.shownTile.add(t);
        assertThrows(BusinessLogicException.class, () -> controller.chooseUncoveredTile("P2", t.getIdTile() + 1));
        String jsonChosen = controller.chooseUncoveredTile("P2", t.getIdTile());
        assertTrue(jsonChosen.contains("\"type\""));

        // --- DROP TILE ---

        reset(view1);
        assertThrows(BusinessLogicException.class, () -> controller.dropTile("P2", "not-a-json"));
        controller.dropTile("P2", goodJson);
        verify(view1, atLeastOnce()).updateGameState(anyString());

        // --- PLACE TILE ---

        reset(view1);
        assertThrows(BusinessLogicException.class, () -> controller.placeTile("P2", "nope", new int[]{0,0}));

        doNothing().when(spyPlayer).addTile(anyInt(), anyInt(), any(Tile.class));
        controller.placeTile("P2", goodJson, new int[]{2,3});
        verify(view1, atLeastOnce()).updateGameState(anyString());

        // --- RESERVED TILE ---

        reset(view1);
        assertThrows(BusinessLogicException.class, () -> controller.getReservedTile("P2", 123));
        spyPlayer.getTilesInDiscardPile().add(t);
        String jsonReserved = controller.getReservedTile("P2", t.getIdTile());
        assertTrue(jsonReserved.contains("\"type\""));
        verify(view1, atLeastOnce()).updateGameState(anyString());
    }



    @Test
    void testSetReadyAndStartFlight() throws Exception {
        Controller c2 = new Controller(false, 6, 2, endedGames::add, loggedInUsers);
        c2.addPlayer("U1", view1);
        c2.addPlayer("U2", view2);
        FlightCardBoard2 board = (FlightCardBoard2) c2.getFlightCardBoard();

        c2.setReady("U1");
        assertEquals(GamePhase.WAITING_FOR_PLAYERS, c2.getPlayerCheck("U1").getGamePhase());
        verify(view1, atLeastOnce()).updateGameState(anyString());
    }

    @Test
    void testStartFlightDemo() throws Exception {
        Controller demo = spy(controller);
        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        when(fbdMock.getOrderedPlayers()).thenReturn(Collections.emptyList());
        setField(demo, "fBoard", fbdMock);

        demo.addPlayer("A", view1);
        demo.addPlayer("B", view2);
        reset(view1, view2);

        doNothing().when(demo).checkPlayerAssembly(anyString(), anyInt(), anyInt());
        doNothing().when(demo).addHuman();
        doNothing().when(demo).activateDrawPhase();

        demo.startFlight();
        verify(demo, never()).mergeDecks();
        verify(fbdMock, times(2)).setPlayerReadyToFly(any(), eq(true));

        verify(view1).inform(startsWith("SERVER: Flight started"));
        verify(view2).inform(startsWith("SERVER: Flight started"));

        verify(demo, times(2)).checkPlayerAssembly(anyString(), eq(2), eq(3));

        assertEquals(GamePhase.WAITING_FOR_TURN, demo.getPlayerCheck("A").getGamePhase());
        assertEquals(GamePhase.WAITING_FOR_TURN, demo.getPlayerCheck("B").getGamePhase());
        verify(demo).activateDrawPhase();
    }

    @Test
    void testStartFlightNonDemo() throws Exception {
        Controller nonDemo = new Controller(false, 2, 4, endedGames::add, loggedInUsers);
        nonDemo = spy(nonDemo);
        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        when(fbdMock.getOrderedPlayers()).thenReturn(Collections.emptyList());
        setField(nonDemo, "fBoard", fbdMock);
        Hourglass hgMock = mock(Hourglass.class);
        setField(nonDemo, "hourglass", hgMock);

        nonDemo.addPlayer("C", view1);
        nonDemo.addPlayer("D", view2);
        reset(view1, view2);

        doNothing().when(nonDemo).checkPlayerAssembly(anyString(), anyInt(), anyInt());
        doNothing().when(nonDemo).addHuman();
        doNothing().when(nonDemo).activateDrawPhase();

        nonDemo.startFlight();

        verify(hgMock).cancel();
        verify(nonDemo).mergeDecks();
    }

    @Test
    void testActivateDrawPhase_NoPlayers() {
        assertThrows(BusinessLogicException.class, () -> controller.activateDrawPhase());
    }

    @Test
    void testActivateDrawPhase_NormalFlow() throws Exception {
        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        reset(view1, view2);

        controller.getPlayerCheck("A").setConnected(true);
        controller.getPlayerCheck("B").setConnected(true);
        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        when(fbdMock.getOrderedPlayers()).thenReturn(Arrays.asList(controller.getPlayerCheck("A"), controller.getPlayerCheck("B")));
        setField(controller, "fBoard", fbdMock);

        controller.activateDrawPhase();
        verify(view1).inform(contains("You're the leader"));
        verify(view1).updateGameState(anyString());
        verify(view2).updateGameState(anyString());
    }

    @Test
    void testFlipHourglass_AllBranches() throws Exception {
        controller.addPlayer("P", view1);
        controller.getPlayerCheck("P").setConnected(true);

        Hourglass hgMock = mock(Hourglass.class);
        doReturn(1).when(hgMock).getFlips();
        doReturn(HourglassState.ONGOING).when(hgMock).getState();
        setField(controller, "hourglass", hgMock);
        controller.flipHourglass("P");
        verify(view1).reportError(contains("still running"));

        reset(view1);
        doReturn(1).when(hgMock).getFlips();
        doReturn(HourglassState.EXPIRED).when(hgMock).getState();
        controller.flipHourglass("P");
        verify(hgMock, times(1)).flip();
        verify(view1).inform(contains("flipped a second time"));
        reset(view1, hgMock);
        doReturn(2).when(hgMock).getFlips();
        doReturn(HourglassState.ONGOING).when(hgMock).getState();
        controller.flipHourglass("P");
        verify(view1).reportError(contains("still running"));
        reset(view1, hgMock);
        doReturn(2).when(hgMock).getFlips();
        doReturn(HourglassState.EXPIRED).when(hgMock).getState();
        controller.flipHourglass("P");
        verify(view1).reportError(contains("not ready"));

        reset(view1, hgMock);

        controller.getPlayerCheck("P").setGamePhase(GamePhase.WAITING_FOR_PLAYERS);
        doReturn(2).when(hgMock).getFlips();
        doReturn(HourglassState.EXPIRED).when(hgMock).getState();
        controller.flipHourglass("P");
        verify(hgMock, times(1)).flip();
        verify(view1).inform(contains("flipped the last time"));

        reset(view1, hgMock);
        doReturn(42).when(hgMock).getFlips();
        assertThrows(BusinessLogicException.class, () -> controller.flipHourglass("P"));
    }

    @Test
    void testOnHourglassStateChange() throws Exception {
        Controller spyCtrl = spy(controller);

        spyCtrl.addPlayer("P", view1);
        reset(view1);
        doNothing().when(spyCtrl).startFlight();

        Hourglass hMock = mock(Hourglass.class);

        doReturn(1).when(hMock).getFlips();
        spyCtrl.onHourglassStateChange(hMock);
        verify(view1).inform(contains("First Hourglass expired"));

        reset(view1);
        doReturn(2).when(hMock).getFlips();
        spyCtrl.onHourglassStateChange(hMock);
        verify(view1).inform(contains("Second Hourglass expired"));

        reset(view1);
        doReturn(3).when(hMock).getFlips();
        spyCtrl.onHourglassStateChange(hMock);
        verify(view1).inform(contains("Time’s up"));
        verify(spyCtrl).startFlight();
    }



    @Test
    void testDrawCardManagement_SimpleAndEmptyDeck() throws Exception {
        Controller real = new Controller(false, 99, 4, endedGames::add, loggedInUsers);
        Controller spyCtrl = spy(real);

        spyCtrl.addPlayer("X", view1);
        spyCtrl.addPlayer("Y", view2);
        reset(view1, view2);
        Deck deckMock = mock(Deck.class);
        Card cardMock = mock(Card.class);
        doReturn(cardMock).when(deckMock).draw();
        doReturn(true).when(deckMock).isEmpty();
        setField(spyCtrl, "deck", deckMock);
        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        doReturn(List.of(spyCtrl.getPlayerCheck("X"))).when(fbdMock).getOrderedPlayers();
        setField(spyCtrl, "fBoard", fbdMock);
        doNothing().when(spyCtrl).activateCard(any());

        spyCtrl.drawCardManagement("X");

        verify(view1).printCard(anyString());
        verify(view1).inform(contains("Card drawn!"));

        assertTrue(endedGames.contains(99), "onGameEnd non è stato chiamato");
    }

    @Test
    void testDrawCardManagement_AllPlayersEliminatedBranch() throws Exception {
        Controller c2 = spy(new Controller(false, 100, 4, endedGames::add, loggedInUsers));
        c2.addPlayer("X", view1);
        reset(view1);

        Deck deckMock = mock(Deck.class);
        Card cardMock = mock(Card.class);
        doReturn(cardMock).when(deckMock).draw();
        doReturn(false).when(deckMock).isEmpty();
        setField(c2, "deck", deckMock);

        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        doReturn(Collections.emptyList()).when(fbdMock).getOrderedPlayers();
        setField(c2, "fBoard", fbdMock);
        doNothing().when(c2).activateCard(any());
        c2.drawCardManagement("X");

        verify(view1).inform(contains("All players eliminated"));
        assertTrue(endedGames.contains(100));
    }

    @Test
    void testDrawCardManagement_DecisionBranchAllStay() throws Exception {
        Controller c2 = spy(new Controller(false, 101, 4, endedGames::add, loggedInUsers));
        c2.addPlayer("A", view1);
        c2.addPlayer("B", view2);
        reset(view1, view2);

        Deck deckMock = mock(Deck.class);
        Card cardMock = mock(Card.class);
        doReturn(cardMock).when(deckMock).draw();
        doReturn(false).when(deckMock).isEmpty();
        setField(c2, "deck", deckMock);

        FlightCardBoard fbdMock = mock(FlightCardBoard2.class);
        doReturn(List.of(c2.getPlayerCheck("A"), c2.getPlayerCheck("B"))).when(fbdMock).getOrderedPlayers();
        doReturn(Collections.emptyList()).when(fbdMock).eliminatePlayers();
        doNothing().when(fbdMock).orderPlayersInFlightList();
        setField(c2, "fBoard", fbdMock);
        doNothing().when(c2).activateCard(any());
        doNothing().when(c2).activateDrawPhase();
        doReturn(false).when(c2).askPlayerDecision(anyString(), any());
        c2.drawCardManagement("A");
        assertFalse(endedGames.contains(101));
        verify(view1, atLeastOnce()).updateGameState(contains("WAITING_FOR_TURN"));
        verify(view2, atLeastOnce()).updateGameState(contains("WAITING_FOR_TURN"));
    }

    @Test
    void testShowDeck_getNumCrew_shutdownHourglass() throws Exception {
        Controller c3 = new Controller(false, 200, 4, endedGames::add, loggedInUsers);
        String json0 = c3.showDeck(0);
        assertNotNull(json0);
        assertTrue(json0.startsWith("["));
        assertThrows(IndexOutOfBoundsException.class, () -> c3.showDeck(999));

        c3.addPlayer("Z", view1);
        Player pZ = c3.getPlayerCheck("Z");
        int crew = c3.getNumCrew(pZ);
        assertTrue(crew >= 0);

        Hourglass hgSpy = spy(new Hourglass(h -> {}));
        setField(controller, "hourglass", hgSpy);
        controller.shutdownHourglass();
        verify(hgSpy).shutdown();
        Field hf = Controller.class.getDeclaredField("hourglass");
        hf.setAccessible(true);
        assertNull(hf.get(controller));
    }

    @Test
    void testChangeMapPositionAndUpdatePositionForEverybody() throws Exception {
        controller.addPlayer("U", view1);
        controller.changeMapPosition("U", controller.getPlayerCheck("U"));
        controller.getPlayerCheck("U").setConnected(true);
        reset(view1);
        controller.updatePositionForEveryBody();
        verify(view1).updateMapPosition(anyMap());

        reset(view1);
        controller.getPlayerCheck("U").setConnected(true);
        doThrow(new IOException("fail")).when(view1).updateMapPosition(anyMap());
        controller.updatePositionForEveryBody();
        assertFalse(controller.getPlayerCheck("U").isConnected());
    }

    @Test
    void testSetExit_getPlayersPosition_getGamePhase_getDashJson() throws Exception {
        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        reset(view1, view2);

        controller.setExit();

        verify(view1).updateGameState(eq("\"EXIT\""));
        verify(view2).updateGameState(eq("\"EXIT\""));

        assertTrue(endedGames.contains(1));

        Map<String,int[]> pos = controller.getPlayersPosition();
        assertTrue(pos.containsKey("A"));
        assertTrue(pos.containsKey("B"));

        assertEquals("\"EXIT\"", controller.getGamePhase("A"));
        assertEquals("\"EXIT\"", controller.getGamePhase("B"));

        String[][] dash = controller.getDashJson("A");
        assertNotNull(dash);
        assertEquals(5, dash.length);
        assertEquals(7, dash[0].length);
    }

    @Test
    void testGetGamePhaseAndDashJson_ExceptionPaths() throws Exception {
        controller.addPlayer("U", view1);
        Field enumF = Controller.class.getDeclaredField("enumSerializer");
        enumF.setAccessible(true);
        var enumMock = mock(it.polimi.ingsw.galaxytrucker.Controller.EnumSerializer.class);
        doThrow(new JsonProcessingException("err"){}).when(enumMock).serializeGamePhase(any());
        enumF.set(controller, enumMock);
        String gp = controller.getGamePhase("U");
        assertEquals("EXIT", gp);
        Field tileF = Controller.class.getDeclaredField("tileSerializer");
        tileF.setAccessible(true);
        var tileMock = mock(it.polimi.ingsw.galaxytrucker.Controller.TileSerializer.class);
        doThrow(new JsonProcessingException("err"){}).when(tileMock).toJsonMatrix(any());
        tileF.set(controller, tileMock);
        String[][] dash = controller.getDashJson("U");
        assertNull(dash);
    }

    @Test
    void testSetExit_ExceptionInViewAndNormal() throws Exception {
        controller.addPlayer("A", view1);
        controller.addPlayer("B", view2);
        reset(view1, view2);
        doThrow(new IOException("fail")).when(view1).updateGameState(anyString());

        controller.setExit();

        assertFalse(controller.getPlayerByNickname("A").isConnected());
        verify(view2).updateGameState("\"EXIT\"");
        assertTrue(endedGames.contains(1));
        Controller c2 = new Controller(true, 2, 2, endedGames::add, loggedInUsers);
        reset(view1);
        c2.addPlayer("X", view1);
        reset(view1);

        c2.setExit();
        verify(view1).updateGameState("\"EXIT\"");
        assertTrue(endedGames.contains(2));
    }



    @Test
    void testUpdatePositionForEveryBody_NormalAndIOException() throws Exception {
        controller.addPlayer("P", view1);
        controller.changeMapPosition("P", controller.getPlayerCheck("P"));
        controller.getPlayerByNickname("P").setConnected(true);
        reset(view1);
        controller.updatePositionForEveryBody();
        verify(view1).updateMapPosition(anyMap());

        controller.getPlayerByNickname("P").setConnected(true);
        doThrow(new IOException("io")).when(view1).updateMapPosition(anyMap());
        controller.updatePositionForEveryBody();
        assertFalse(controller.getPlayerByNickname("P").isConnected());
    }

    @Test
    void testAskPlayerIndex_AllPaths() throws Exception {
        controller.addPlayer("Q", view1);
        Player p = controller.getPlayerCheck("Q");

        p.setConnected(false);
        assertNull(controller.askPlayerIndex(p, 5));

        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(null);
        assertNull(controller.askPlayerIndex(p, 5));

        p.setConnected(true);
        when(view1.askIndexWithTimeout()).thenReturn(-1, 7, 3);
        doNothing().when(view1).reportError(anyString());
        Integer result = controller.askPlayerIndex(p, 5);
        assertEquals(3, result);

        p.setConnected(true);
        doThrow(new IOException()).when(view1).askIndexWithTimeout();
        Integer r2 = controller.askPlayerIndex(p, 5);
        assertNull(r2);
        assertFalse(p.isConnected());
    }

    @Test
    void testLookAtDashBoard_ExceptionAndNormal() throws Exception {
        controller.addPlayer("D", view1);
        String[][] dash = controller.lookAtDashBoard("D");
        assertNotNull(dash);
        assertEquals(5, dash.length);
        Field tileF = Controller.class.getDeclaredField("tileSerializer");
        tileF.setAccessible(true);
        var tileMock = mock(it.polimi.ingsw.galaxytrucker.Controller.TileSerializer.class);
        doThrow(new JsonProcessingException("err"){}).when(tileMock).toJsonMatrix(any());
        tileF.set(controller, tileMock);
        assertThrows(BusinessLogicException.class, () -> controller.lookAtDashBoard("D"));
    }

    @Test
    void testShowDeck_ExceptionAndNormal() throws Exception {
        Controller c3 = new Controller(false, 300, 2, endedGames::add, loggedInUsers);
        String json = c3.showDeck(0);
        assertNotNull(json);
        Field cardF = Controller.class.getDeclaredField("cardSerializer");
        cardF.setAccessible(true);
        var cardMock = mock(it.polimi.ingsw.galaxytrucker.Controller.CardSerializer.class);
        doThrow(new JsonProcessingException("boom"){}).when(cardMock).toJsonList(any());
        cardF.set(c3, cardMock);
        assertNull(c3.showDeck(0));
    }

    @Test
    void testActivateCard_InvokesFBoardAndHandleElimination() throws Exception {
        class TestController extends Controller {
            public TestController() throws Exception {
                super(false, 400, 2, endedGames::add, loggedInUsers);
            }
            @Override
            public void activateCard(Card card) throws BusinessLogicException {
                fBoard.checkIfPlayerOverlapped();
                fBoard.checkIfPlayerNoHumansLeft();
                List<Player> eliminated = fBoard.eliminatePlayers();
                for (Player p : eliminated) handleElimination(p);
                fBoard.orderPlayersInFlightList();
            }
        }
        TestController tc = spy(new TestController());
        tc.addPlayer("A", view1);
        Player pA = tc.getPlayerCheck("A");
        FlightCardBoard fbd = mock(FlightCardBoard2.class);
        doNothing().when(fbd).checkIfPlayerOverlapped();
        doNothing().when(fbd).checkIfPlayerNoHumansLeft();
        doReturn(List.of(pA)).when(fbd).eliminatePlayers();
        doNothing().when(fbd).orderPlayersInFlightList();
        setField(tc, "fBoard", fbd);
        doNothing().when(tc).handleElimination(pA);

        Card cardMock = mock(Card.class);
        tc.activateCard(cardMock);
        verify(fbd).checkIfPlayerOverlapped();
        verify(fbd).checkIfPlayerNoHumansLeft();
        verify(fbd).eliminatePlayers();
        verify(tc).handleElimination(pA);
        verify(fbd).orderPlayersInFlightList();
    }

    @Test
    void testActivateCard_AllBranches() throws Exception {
        Controller ctl = new Controller(false, 500, 2, endedGames::add, new java.util.HashSet<>()) {
            @Override
            public void activateCard(Card card) throws BusinessLogicException {
                fBoard.checkIfPlayerOverlapped();
                fBoard.checkIfPlayerNoHumansLeft();
                List<Player> eliminated = fBoard.eliminatePlayers();
                for (Player p : eliminated) handleElimination(p);
                fBoard.orderPlayersInFlightList();
            }
        };
        Controller spyCtl = spy(ctl);

        spyCtl.addPlayer("P", view1);
        Player p = spyCtl.getPlayerCheck("P");
        var fbd = mock(FlightCardBoard2.class);
        doNothing().when(fbd).checkIfPlayerOverlapped();
        doNothing().when(fbd).checkIfPlayerNoHumansLeft();
        doReturn(List.of(p)).when(fbd).eliminatePlayers();
        doNothing().when(fbd).orderPlayersInFlightList();
        {
            Field f = Controller.class.getDeclaredField("fBoard");
            f.setAccessible(true);
            f.set(spyCtl, fbd);
        }
        doNothing().when(spyCtl).handleElimination(p);

        Card cardMock = mock(Card.class);
        spyCtl.activateCard(cardMock);

        verify(fbd).checkIfPlayerOverlapped();
        verify(fbd).checkIfPlayerNoHumansLeft();
        verify(fbd).eliminatePlayers();
        verify(spyCtl).handleElimination(p);
        verify(fbd).orderPlayersInFlightList();
    }

    @Test
    void testGetPowerEngineForCard_and_getPowerEngine_AllBranches() throws Exception {
        controller.addPlayer("U", view1);
        Player realP = controller.getPlayerCheck("U");
        Player p = spy(realP);
        controller.getPlayersByNickname().put("U", p);

        assertEquals(0, controller.getPowerEngineForCard(p));
        assertEquals(0, controller.getPowerEngine(p));

        Engine eng = mock(Engine.class);
        when(eng.isDouble()).thenReturn(false);
        doAnswer(inv -> {
            int i = inv.getArgument(0), j = inv.getArgument(1);
            if (i==0 && j==0) return eng;
            else return inv.callRealMethod();
        }).when(p).getTile(anyInt(), anyInt());
        Controller ctrlSpy = spy(controller);
        ctrlSpy.getPlayersByNickname().put("U", p);
        assertEquals(1, ctrlSpy.getPowerEngineForCard(p));
        assertEquals(1, ctrlSpy.getPowerEngine(p));

        when(eng.isDouble()).thenReturn(true);
        doReturn(false).when(ctrlSpy).manageEnergyCell(eq("U"), anyString());
        assertEquals(0, ctrlSpy.getPowerEngineForCard(p));

        doReturn(true).when(ctrlSpy).manageEnergyCell(eq("U"), anyString());
        doReturn(false).when(p).presenceBrownAlien();
        assertEquals(2, ctrlSpy.getPowerEngineForCard(p));
        assertEquals(2, ctrlSpy.getPowerEngine(p));

        doReturn(true).when(p).presenceBrownAlien();
        assertEquals(4, ctrlSpy.getPowerEngineForCard(p));
        assertEquals(4, ctrlSpy.getPowerEngine(p));
    }

    @Test
    void testGetFirePowerForCard_and_getFirePower_AllBranches() throws Exception {
        controller.addPlayer("V", view1);
        Player realP = controller.getPlayerCheck("V");
        Player p = spy(realP);
        controller.getPlayersByNickname().put("V", p);

        assertEquals(0.0, controller.getFirePowerForCard(p));
        assertEquals(0.0, controller.getFirePower(p));

        Cannon can = mock(Cannon.class);
        when(can.isDouble()).thenReturn(false);
        when(can.controlCorners(0)).thenReturn(2);
        doAnswer(inv -> {
            int i=inv.getArgument(0), j=inv.getArgument(1);
            if(i==0&&j==0) return can;
            else return inv.callRealMethod();
        }).when(p).getTile(anyInt(), anyInt());
        Controller cs = spy(controller);
        cs.getPlayersByNickname().put("V", p);

        doReturn(false).when(p).presencePurpleAlien();
        assertEquals(0.5, cs.getFirePowerForCard(p));
        assertEquals(0.5, cs.getFirePower(p));

        when(can.controlCorners(0)).thenReturn(4);
        assertEquals(1.0, cs.getFirePowerForCard(p));
        assertEquals(1.0, cs.getFirePower(p));

        when(can.isDouble()).thenReturn(true);
        doReturn(false).when(cs).manageEnergyCell(eq("V"), anyString());
        assertEquals(0.0, cs.getFirePowerForCard(p));

        doReturn(true).when(cs).manageEnergyCell(eq("V"), anyString());
        when(can.controlCorners(0)).thenReturn(3);
        assertEquals(1.0, cs.getFirePowerForCard(p));

        when(can.controlCorners(0)).thenReturn(5);
        assertEquals(2.0, cs.getFirePowerForCard(p));

        when(p.presencePurpleAlien()).thenReturn(true);
        assertEquals(4.0, cs.getFirePowerForCard(p));
        assertEquals(4.0, cs.getFirePower(p));
    }

    @Test
    void testRemoveGoods_AllEqual() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);

        doNothing().when(spyC).autoCommandForRemoveGoods(any(), anyInt());
        doNothing().when(spyC).inform(anyString(), anyString());

        doReturn(3).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.RED, Colour.BLUE, Colour.GREEN)).when(p).getTotalListOfGood();

        spyC.removeGoods(p, 3);
        verify(spyC).autoCommandForRemoveGoods(p, 3);
        verify(spyC).inform("SERVER: You have lost all your goods", "P");
    }

    @Test
    void testRemoveGoods_PartialRemoval() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);

        doNothing().when(spyC).autoCommandForRemoveSingleGood(any(), any());
        doReturn(3).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.RED, Colour.BLUE, Colour.GREEN)).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();
        doReturn(null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);
        verify(spyC).autoCommandForRemoveSingleGood(p, Colour.RED);
    }

    @Test
    void testRemoveGoods_GreaterRemovalConnected() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);

        doNothing().when(spyC).autoCommandForRemoveGoods(any(), anyInt());
        doNothing().when(spyC).autoCommandForBattery(any(), anyInt());
        doNothing().when(spyC).inform(anyString(), anyString());
        doNothing().when(spyC).printPlayerDashboard(any(), any(), anyString());

        doReturn(3).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.RED, Colour.BLUE, Colour.GREEN)).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();
        doReturn(null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 6);

        verify(spyC).autoCommandForRemoveGoods(p, 3);
        verify(spyC).inform("SERVER: You have lost all your goods", "P");
        verify(spyC).inform("SERVER: You will lose 3 battery/ies", "P");
        verify(spyC, times(3)).inform("SERVER: Select an energy cell to remove a battery from", "P");
        verify(spyC, times(3)).autoCommandForBattery(p, 1);
        verify(spyC, times(4)).printPlayerDashboard(view1, p, "P");
    }

    @Test
    void testRemoveGoods_GreaterRemovalDisconnected() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);

        Player real = spyC.getPlayerCheck("P");
        Player p = spy(real);
        spyC.getPlayersByNickname().put("P", p);
        clearInvocations(spyC);
        doNothing().when(spyC).autoCommandForRemoveGoods(any(), anyInt());
        doNothing().when(spyC).autoCommandForBattery(any(), anyInt());

        doReturn(3).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.RED, Colour.BLUE, Colour.GREEN)).when(p).getTotalListOfGood();
        doReturn(false).when(p).isConnected();

        spyC.removeGoods(p, 6);
        verify(spyC).autoCommandForRemoveGoods(p, 3);
        verify(spyC).autoCommandForBattery(p, 3);
        verify(spyC, never()).inform(anyString(), anyString());
    }

    @Test
    void testRemoveGoods_RedRemovalConnected() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);

        Player real = spyC.getPlayerCheck("P");
        Player p = spy(real);
        spyC.getPlayersByNickname().put("P", p);
        clearInvocations(spyC);
        StorageUnit su = mock(StorageUnit.class);
        List<Colour> goods = new ArrayList<>(List.of(Colour.RED));
        when(su.getListOfGoods()).thenReturn(goods);
        doReturn(new int[]{0,0}).when(spyC).askPlayerCoordinates(p);
        doReturn(su).when(p).getTile(0,0);
        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(List.of(Colour.RED, Colour.BLUE)).when(p).getTotalListOfGood();
        doReturn(0).when(spyC).getTotalEnergy(p);

        doReturn(true).when(p).isConnected();
        spyC.removeGoods(p, 1);
        verify(spyC).inform(contains("remove a red good"), eq("P"));
        verify(spyC, atLeastOnce()).printPlayerDashboard(view1, p, "P");
        verify(su).removeGood(0);
    }

    @Test
    void testRemoveGoods_RedRemovalDisconnected_ScanningBranch() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        reset(view1);
        Player realP = spyC.getPlayerCheck("P");
        Player spyP  = spy(realP);
        spyC.getPlayersByNickname().put("P", spyP);

        doReturn(2).when(spyC).getTotalGood(spyP);
        doReturn(5).when(spyC).getTotalEnergy(spyP);
        doReturn(List.of(Colour.RED, Colour.BLUE)).when(spyP).getTotalListOfGood();

        doReturn(true, false).when(spyP).isConnected();
        StorageUnit realSU = new StorageUnit(0,0,0,0, 2, false, 42);
        realSU.getListOfGoods().add(Colour.RED);
        StorageUnit spySU = spy(realSU);
        doAnswer(inv -> new ArrayList<>((List<Colour>)inv.callRealMethod())).when(spySU).getListOfGoods();
        doAnswer(inv -> {
            int i = inv.getArgument(0), j = inv.getArgument(1);
            return (i==2 && j==3) ? spySU : new EmptySpace();
        }).when(spyP).getTile(anyInt(), anyInt());

        spyC.removeGoods(spyP, 1);
        assertTrue(realSU.getListOfGoods().isEmpty(), "La RED doveva essere tolta");
        verify(view1, never()).reportError(anyString());
        verify(view1, never()).inform(anyString());
        verify(view1, atLeastOnce()).printPlayerDashboard(any(String[][].class));
    }


    @Test
    void testRemoveGoods_RedRemovalConnected_InteractiveBranch() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        reset(view1);

        Player real = spyC.getPlayerCheck("P");
        Player spyP = spy(real);
        spyC.getPlayersByNickname().put("P", spyP);

        doReturn(2).when(spyC).getTotalGood(spyP);
        doReturn(5).when(spyC).getTotalEnergy(spyP);
        doReturn(List.of(Colour.RED, Colour.BLUE)).when(spyP).getTotalListOfGood();
        doReturn(true).when(spyP).isConnected();
        StorageUnit realSU = new StorageUnit(0,0,0,0, 2, false, 42);
        realSU.getListOfGoods().add(Colour.RED);
        StorageUnit spySU = spy(realSU);
        doAnswer(inv -> new ArrayList<>((List<Colour>)inv.callRealMethod())).when(spySU).getListOfGoods();
        doAnswer(inv -> {
            int i = inv.getArgument(0), j = inv.getArgument(1);
            return (i==2 && j==3) ? spySU : new EmptySpace();
        }).when(spyP).getTile(anyInt(), anyInt());
        when(view1.askCoordsWithTimeout()).thenReturn(new int[]{2,3});
        spyC.removeGoods(spyP, 1);
        verify(view1).inform(contains("remove a red good"));
        verify(view1, atLeast(2)).printPlayerDashboard(any(String[][].class));
        assertTrue(realSU.getListOfGoods().isEmpty(), "Il RED doveva essere rimosso dalla storage unit");
    }


    @Test
    void testRemoveGoods_YellowRemovalConnected_NullCoordinate() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        reset(view1);
        Player realP = spyC.getPlayerCheck("P");
        Player spyP  = spy(realP);
        spyC.getPlayersByNickname().put("P", spyP);

        doReturn(2).when(spyC).getTotalGood(spyP);
        doReturn(5).when(spyC).getTotalEnergy(spyP);
        doReturn(List.of(Colour.YELLOW, Colour.GREEN)).when(spyP).getTotalListOfGood();
        doReturn(true).when(spyP).isConnected();
        doReturn(null).when(spyC).askPlayerCoordinates(spyP);
        doNothing().when(spyC).autoCommandForRemoveSingleGood(spyP, Colour.YELLOW);

        spyC.removeGoods(spyP, 1);

        verify(spyC).inform(contains("remove a yellow good"), eq("P"));
        verify(spyC).autoCommandForRemoveSingleGood(spyP, Colour.YELLOW);
        verify(spyC, atLeastOnce()).printPlayerDashboard(view1, spyP, "P");
    }

    @Test
    void testRemoveGoods_GreenRemovalDisconnected_Bulk() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        Mockito.clearInvocations(spyC);

        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(3).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.GREEN, Colour.BLUE)).when(p).getTotalListOfGood();
        doReturn(false).when(p).isConnected();

        StorageUnit su = mock(StorageUnit.class);
        List<Colour> greens = new ArrayList<>(List.of(Colour.GREEN));
        when(su.getListOfGoods()).thenReturn(greens);
        doAnswer(inv -> { greens.remove(0); return null; }).when(su).removeGood(anyInt());
        when(p.getTile(anyInt(), anyInt())).thenReturn(su);
        spyC.removeGoods(p, 1);

        assertTrue(greens.isEmpty());
        verify(spyC, never()).reportError(anyString(), anyString());
        verify(spyC, never()).inform(anyString(), anyString());
    }


    @Test
    void testRemoveGoods_BlueRemovalConnected_InvalidCell() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        Mockito.clearInvocations(view1, spyC);

        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.BLUE, Colour.BLUE)).when(p).getTotalListOfGood();

        doReturn(true).when(p).isConnected();
        StorageUnit su = mock(StorageUnit.class);
        when(su.getListOfGoods()).thenReturn(new ArrayList<>());
        when(p.getTile(eq(1), eq(1))).thenReturn(su);
        when(p.getTile(not(eq(1)), anyInt())).thenReturn(mock(EmptySpace.class));
        doReturn(new int[]{1,1}, (int[]) null).when(spyC).askPlayerCoordinates(p);

        doNothing().when(spyC).autoCommandForRemoveSingleGood(p, Colour.BLUE);
        spyC.removeGoods(p, 1);
        verify(view1).reportError(contains("There are not blue goods"));
        verify(spyC).autoCommandForRemoveSingleGood(p, Colour.BLUE);
    }

    @Test
    void testRemoveGoods_YellowRemovalConnected_ValidCell() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        Mockito.clearInvocations(view1, spyC);
        doReturn(1).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(true).when(p).isConnected();
        doReturn(List.of(Colour.YELLOW)).when(p).getTotalListOfGood();
        StorageUnit su = mock(StorageUnit.class);
        List<Colour> goods = new ArrayList<>(List.of(Colour.YELLOW));
        when(su.getListOfGoods()).thenReturn(goods);
        when(p.getTile(eq(1), eq(1))).thenReturn(su);
        when(p.getTile(not(eq(1)), anyInt())).thenReturn(mock(EmptySpace.class));
        doReturn(new int[]{1,1}, (int[]) null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);
        verify(su).removeGood(0);
        verify(view1, never()).reportError(anyString());
        verify(view1, atLeastOnce()).printPlayerDashboard(any(String[][].class));
    }

    @Test
    void testRemoveGoods_YellowRemovalConnected_CellValidoEInvalido() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        Mockito.clearInvocations(view1, spyC);
        doReturn(true).when(p).isConnected();
        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.YELLOW, Colour.GREEN)).when(p).getTotalListOfGood();

        StorageUnit yellowSU = mock(StorageUnit.class);
        List<Colour> yellowGoods = new ArrayList<>(List.of(Colour.YELLOW));
        when(yellowSU.getListOfGoods()).thenReturn(yellowGoods);
        when(p.getTile(eq(1), eq(1))).thenReturn(yellowSU);
        when(p.getTile(not(eq(1)), anyInt())).thenReturn(mock(EmptySpace.class));

        doReturn(new int[]{1,1}, (int[])null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);
        verify(yellowSU).removeGood(0);
        verify(view1, never()).reportError(anyString());
        verify(view1, atLeastOnce()).printPlayerDashboard(any(String[][].class));
        yellowGoods.clear();
        doReturn(new int[]{2,2}, (int[])null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);
        verify(view1).reportError(contains("Not valid cell"));
    }

    @Test
    void testRemoveGoods_GreenRemovalConnected_CellValidoEInvalido() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        Mockito.clearInvocations(view1, spyC);
        doReturn(true).when(p).isConnected();
        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(5).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.GREEN, Colour.BLUE)).when(p).getTotalListOfGood();

        StorageUnit greenSU = mock(StorageUnit.class);
        List<Colour> greenGoods = new ArrayList<>(List.of(Colour.GREEN));
        when(greenSU.getListOfGoods()).thenReturn(greenGoods);
        when(p.getTile(eq(2), eq(2))).thenReturn(greenSU);
        when(p.getTile(not(eq(2)), anyInt())).thenReturn(mock(EmptySpace.class));

        doReturn(new int[]{2,2}, (int[])null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);

        verify(greenSU).removeGood(0);
        verify(view1, never()).reportError(anyString());
        verify(view1, atLeastOnce()).printPlayerDashboard(any(String[][].class));
        greenGoods.clear();
        doReturn(new int[]{0,0}, (int[])null).when(spyC).askPlayerCoordinates(p);
        spyC.removeGoods(p, 1);
        verify(view1).reportError(contains("Not valid cell"));
    }


    @Test
    void testRemoveGoods_BlueRemovalConnected_CellValido() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        doReturn(1).when(spyC).getTotalGood(p);
        doReturn(2).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.BLUE)).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();
        StorageUnit su = mock(StorageUnit.class);
        List<Colour> goods = new ArrayList<>(List.of(Colour.BLUE));
        when(su.getListOfGoods()).thenReturn(goods);
        doAnswer(inv -> {
            goods.remove(0);
            return null;
        }).when(su).removeGood(0);
        EmptySpace empty = mock(EmptySpace.class);
        when(p.getTile(eq(2), eq(3))).thenReturn(su);
        when(p.getTile(not(eq(2)), anyInt())).thenReturn(empty);
        when(p.getTile(anyInt(), not(eq(3)))).thenReturn(empty);
        doReturn(new int[]{2, 3}).when(spyC).askPlayerCoordinates(p);

        spyC.removeGoods(p, 1);
        verify(su).removeGood(0);
        verify(view1, never()).reportError(anyString());
        verify(view1).printPlayerDashboard(any(String[][].class));
    }

    @Test
    void testRemoveGoods_BlueRemovalConnected_CellNonValidaPoiNull() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);
        doReturn(2).when(spyC).getTotalGood(p);
        doReturn(2).when(spyC).getTotalEnergy(p);
        doReturn(List.of(Colour.BLUE)).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();

        doReturn(new int[]{4, 4}, (int[]) null).when(spyC).askPlayerCoordinates(p);

        EmptySpace empty = mock(EmptySpace.class);
        when(p.getTile(4, 4)).thenReturn(empty);
        doNothing().when(view1).reportError(anyString());
        doNothing().when(spyC).autoCommandForRemoveSingleGood(p, Colour.BLUE);

        spyC.removeGoods(p, 1);
        verify(view1).reportError(contains("Not valid cell. Try again"));
        verify(spyC).autoCommandForRemoveSingleGood(p, Colour.BLUE);
    }

    @Test
    void testRemoveGoods_BatteryRemoval_ValidEnergyCell() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);

        doReturn(0).when(spyC).getTotalGood(p);
        doReturn(2).when(spyC).getTotalEnergy(p);
        doReturn(Collections.emptyList()).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();
        EnergyCell ec = mock(EnergyCell.class);
        when(ec.getCapacity()).thenReturn(1);
        when(p.getTile(eq(0), eq(0))).thenReturn(ec);
        EmptySpace empty = mock(EmptySpace.class);
        when(p.getTile(not(eq(0)), anyInt())).thenReturn(empty);
        doReturn(new int[]{0,0}, (int[])null).when(spyC).askPlayerCoordinates(p);

        spyC.removeGoods(p, 1);
        verify(ec).useBattery();
        verify(view1, never()).reportError(anyString());
        verify(view1, atLeastOnce()).printPlayerDashboard(any(String[][].class));
    }

    @Test
    void testRemoveGoods_BatteryRemoval_InvalidThenAuto() throws Exception {
        Controller spyC = spy(controller);
        spyC.addPlayer("P", view1);
        Player p = spy(spyC.getPlayerCheck("P"));
        spyC.getPlayersByNickname().put("P", p);

        doReturn(0).when(spyC).getTotalGood(p);
        doReturn(2).when(spyC).getTotalEnergy(p);
        doReturn(Collections.emptyList()).when(p).getTotalListOfGood();
        doReturn(true).when(p).isConnected();

        EnergyCell ecEmpty = mock(EnergyCell.class);
        when(ecEmpty.getCapacity()).thenReturn(0);
        when(p.getTile(eq(1), eq(1))).thenReturn(ecEmpty);
        EmptySpace empty = mock(EmptySpace.class);
        when(p.getTile(not(eq(1)), anyInt())).thenReturn(empty);

        doReturn(new int[]{1,1}, (int[])null).when(spyC).askPlayerCoordinates(p);
        doNothing().when(view1).reportError(anyString());
        doNothing().when(spyC).autoCommandForBattery(p, 1);
        spyC.removeGoods(p, 1);
        verify(view1).reportError(contains("Empty energy cell"));
        verify(spyC).autoCommandForBattery(p, 1);
    }


    private void setField(Object target, String name, Object value) throws Exception {
        Field f = Controller.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}

