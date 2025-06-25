package it.polimi.ingsw.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.InvalidPlayerException;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class FlightCardBoardTest {

    private FlightCardBoard board;
    private StubController stubCtrl;

    static class StubController extends Controller {
        String lastNick;
        String lastMessage;
        StubController() throws Exception {
            super(true, 0, 4, __ -> {}, ConcurrentHashMap.newKeySet());
        }
        @Override
        public String getNickByPlayer(Player p) {
            return "nick" + p.getId();
        }
        @Override
        public void inform(String msg, String nick) {
            lastNick = nick;
            lastMessage = msg;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        stubCtrl = new StubController();
        board = new FlightCardBoard(stubCtrl);
    }

    @Test
    void testBonusesAndMalus() {
        assertEquals(4, board.getBonusRedCargo());
        assertEquals(3, board.getBonusYellowCargo());
        assertEquals(2, board.getBonusGreenCargo());
        assertEquals(1, board.getBonusBlueCargo());
        assertEquals(-1, board.getBrokenMalus());
        assertEquals(2, board.getBonusBestShip());
        assertEquals(4, board.getBonusFirstPosition());
        assertEquals(3, board.getBonusSecondPosition());
        assertEquals(2, board.getBonusThirdPosition());
        assertEquals(1, board.getBonusFourthPosition());
    }

    @Test
    void testAddPlayerAndGetPosition() {
        Player p = new Player(1, true, 0);
        assertThrows(IllegalArgumentException.class, () -> board.addPlayer(null));
        board.addPlayer(p);
        assertThrows(IllegalArgumentException.class, () -> board.addPlayer(p));
        board.addPlayer(new Player(2, true, 0));
        board.addPlayer(new Player(3, true, 0));
        board.addPlayer(new Player(4, true, 0));
        assertThrows(RuntimeException.class, () -> board.addPlayer(new Player(5, true, 0)));
        assertEquals(0, board.getPositionOfPlayer(p));
        assertThrows(IllegalArgumentException.class, () -> board.getPositionOfPlayer(new Player(99, true, 0)));
    }

    @Test
    void testSetPlayerReadyToFlyDemoAndRegular() throws Exception {
        Player d0 = new Player(10, true, 0);
        Player d1 = new Player(11, true, 0);
        board.setPlayerReadyToFly(d0, true);
        assertEquals(1, d0.getLap());
        assertEquals(5, d0.getPos());
        board.setPlayerReadyToFly(d1, true);
        assertEquals(1, d1.getLap());
        assertEquals(3, d1.getPos());

        setUp();
        Player r0 = new Player(20, false, 0);
        Player r1 = new Player(21, false, 0);
        board.setPlayerReadyToFly(r0, false);
        assertEquals(1, r0.getLap());
        assertEquals(7, r0.getPos());
        board.setPlayerReadyToFly(r1, false);
        assertEquals(1, r1.getLap());
        assertEquals(4, r1.getPos());
    }

    @Test
    void testOrderPlayersInFlightList() {
        Player a = new Player(1, true, 0);
        Player b = new Player(2, true, 0);
        a.setLap(2); a.setPos(1);
        b.setLap(1); b.setPos(18);
        board.orderedPlayersInFlight.add(b);
        board.orderedPlayersInFlight.add(a);
        board.orderPlayersInFlightList();
        assertEquals(a, board.getOrderedPlayers().get(0));

        b.setLap(2); b.setPos(1);
        assertThrows(RuntimeException.class, () -> board.orderPlayersInFlightList());
    }

    @Test
    void testCheckOverLap() {
        Player p = new Player(3, true, 0);
        p.setLap(0);
        // forward wrap
        int pos = board.checkOverLap(p, board.spacesNumber + 5);
        assertEquals(5, pos);
        assertEquals(1, p.getLap());
        // backward wrap
        p.setLap(1);
        pos = board.checkOverLap(p, 0);
        assertEquals(board.spacesNumber, pos);
        assertEquals(0, p.getLap());
    }

    @Test
    void testMoveRocketAndInvalids() {
        Player p = new Player(4, true, 0);
        p.setPos(10);
        board.orderedPlayersInFlight.add(p);
        assertThrows(IllegalArgumentException.class, () -> board.moveRocket(5, null));
        Player q = new Player(5, true, 0);
        assertThrows(InvalidPlayerException.class, () -> board.moveRocket(1, q));
        board.moveRocket(2, p);
        assertEquals(12, p.getPos());
        Player o = new Player(6, true, 0);
        o.setPos(13); board.orderedPlayersInFlight.add(o);
        p.setPos(12);
        board.moveRocket(2, p);
        assertTrue(p.getPos() > 13);
    }

    @Test
    void testCheckIfPlayerOverlappedAndEliminate() {
        Player p = new Player(7, true, 0);
        Player o = new Player(8, true, 0);
        p.setLap(1); p.setPos(5);
        o.setLap(2); o.setPos(6);
        board.orderedPlayersInFlight.add(p);
        board.orderedPlayersInFlight.add(o);
        board.checkIfPlayerOverlapped();
        assertTrue(p.isEliminated());
        List<Player> removed = board.eliminatePlayers();
        assertEquals(1, removed.size());
        assertEquals(p, removed.get(0));
        assertFalse(board.getOrderedPlayers().contains(p));
    }

    @Test
    void testCheckIfPlayerNoHumansLeft() throws BusinessLogicException {
        Player p = new Player(9, true, 0);
        board.orderedPlayersInFlight.add(p);
        board.checkIfPlayerNoHumansLeft();
        assertTrue(p.isEliminated());
        assertEquals("nick9", stubCtrl.lastNick);
        assertTrue(stubCtrl.lastMessage.contains("lost all your crewmates"));
    }

    @Test
    void testSetPlayerReadyToFly_Case2And3_DemoAndRegular() throws Exception {
        Player d2 = new Player(100, true, 0);
        Player d3 = new Player(101, true, 0);
        board.setPlayerReadyToFly(new Player(99, true, 0), true); // size 0
        board.setPlayerReadyToFly(new Player(98, true, 0), true); // size 1
        board.setPlayerReadyToFly(d2, true);                     // size 2
        board.setPlayerReadyToFly(d3, true);                     // size 3
        assertEquals(2, d2.getPos(), "demo size=2 deve pos=2");
        assertEquals(1, d3.getPos(), "demo size=3 deve pos=1");

        setUp();

        Player r2 = new Player(200, false, 0);
        Player r3 = new Player(201, false, 0);
        board.setPlayerReadyToFly(new Player(199, false, 0), false);
        board.setPlayerReadyToFly(new Player(198, false, 0), false);
        board.setPlayerReadyToFly(r2, false);
        board.setPlayerReadyToFly(r3, false);
        assertEquals(2, r2.getPos(), "regular size=2 deve pos=2");
        assertEquals(1, r3.getPos(), "regular size=3 deve pos=1");
    }

    @Test
    void testOrderPlayersInFlightList_Returns1Branch() {
        Player low  = new Player(1, true, 0);
        Player high = new Player(2, true, 0);
        low.setLap(1);   low.setPos(1);
        high.setLap(1);  high.setPos(2);

        board.orderedPlayersInFlight.add(low);
        board.orderedPlayersInFlight.add(high);

        board.orderPlayersInFlightList();
        List<Player> ord = board.getOrderedPlayers();

        assertEquals(high, ord.get(0), "Poiché high.getPos()>low.getPos(), high va in testa");
        assertEquals(low,  ord.get(1), "Low deve finire in seconda posizione");
    }

    @Test
    void testMoveRocket_WrapAroundPositiveAndNegativeOvertaking() {
        Player p = new Player(10, true, 0);
        Player o1 = new Player(11, true, 0);
        p.setPos(17);
        o1.setPos(2);
        board.orderedPlayersInFlight.add(p);
        board.orderedPlayersInFlight.add(o1);
        board.moveRocket(5, p);
        assertTrue(p.getLap() > 0, "dopo wrap-around deve incrementare lap");
        assertTrue(p.getPos() >= 1 && p.getPos() <= board.spacesNumber);

        p.setLap(1);
        p.setPos(2);
        o1.setPos(1);
        board.moveRocket(-3, p);
        assertEquals(0, p.getLap(), "dopo wrap sotto deve decrementare lap");
        assertTrue(p.getPos() >= 1 && p.getPos() <= board.spacesNumber);
    }

    @Test
    void testMoveRocketBackwardOvertakesOne() {
        FlightCardBoard board = new FlightCardBoard(null);
        Player p     = new Player(1, true, 0);
        Player other = new Player(2, true, 0);
        p.setPos(10);
        other.setPos(7);

        board.addPlayer(p);
        board.addPlayer(other);

        board.moveRocket(-5, p);
        assertEquals(4, p.getPos(), "Il ramo backward overtaking deve portare p da 10 a 4");
    }

    @Test
    void testOrderPlayersInFlightList_ReturnsBranch() {
        FlightCardBoard board = new FlightCardBoard(null);
        Player p1 = new Player(1, true, 0);
        Player p2 = new Player(2, true, 0);
        p1.setLap(1); p1.setPos(1);
        p2.setLap(2); p2.setPos(1);

        board.addPlayer(p1);
        board.addPlayer(p2);

        board.orderPlayersInFlightList();
        List<Player> ord = board.getOrderedPlayers();

        assertEquals(p2, ord.get(0), "Quando p1.lap < p2.lap il comparator ritorna 1 → p2 in testa");
        assertEquals(p1, ord.get(1), "p1 finisce in seconda posizione");
    }
}

