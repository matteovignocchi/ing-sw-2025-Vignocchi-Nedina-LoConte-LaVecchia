package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Server.Model.Player;
import it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard.InvalidPlayerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightCardBoardTest {

    private FlightCardBoard board;
    private Player p1, p2, p3, p4, p5;

    @BeforeEach
    void setUp() {
        board = new FlightCardBoard();
        p1 = new Player(1, true);
        p2 = new Player(2, true);
        p3 = new Player(3, true);
        p4 = new Player(4, true);
        p5 = new Player(5, true);
        p1.setPos(0);
        p1.setLap(0);
        p2.setPos(0);
        p2.setLap(0);
        p3.setPos(0);
        p3.setLap(0);
        p4.setPos(0);
        p4.setLap(0);
        p5.setPos(0);
        p5.setLap(0);
    }

    @Test
    @DisplayName("addPlayer: aggiunge un giocatore valido")
    void testAddPlayer() {
        board.addPlayer(p1);
        List<Player> list = board.getOrderedPlayers();
        assertEquals(1, list.size());
        assertTrue(list.contains(p1));
    }

    @Test
    @DisplayName("addPlayer: null lancia IllegalArgumentException")
    void testAddPlayerNull() {
        assertThrows(IllegalArgumentException.class,
                () -> board.addPlayer(null));
    }

    @Test
    @DisplayName("addPlayer: duplicate lancia IllegalArgumentException")
    void testAddPlayerDuplicate() {
        board.addPlayer(p1);
        assertThrows(IllegalArgumentException.class,
                () -> board.addPlayer(p1));
    }

    @Test
    @DisplayName("addPlayer: più di 4 giocatori lancia RuntimeException")
    void testAddPlayerTooMany() {
        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);
        assertThrows(RuntimeException.class,
                () -> board.addPlayer(p5));
    }

    @Test
    @DisplayName("addPlayer: aggiunta di 4 giocatori")
    void testAddPlayerClassic(){
        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);
        List<Player> list = board.getOrderedPlayers();
        assertEquals(4, list.size());
        assertTrue(list.contains(p1));
        assertTrue(list.contains(p2));
        assertTrue(list.contains(p3));
        assertTrue(list.contains(p4));
    }

    @Test
    @DisplayName("orderPlayersInFlightList ordina per lap e pos")
    void testOrderPlayersInFlightList() {
        p1.setLap(1);
        p2.setLap(0);
        p3.setLap(0);
        p1.setPos(2);
        p2.setPos(1);
        p3.setPos(3);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);

        board.orderPlayersInFlightList();
        List<Player> ordered = board.getOrderedPlayers();

        assertEquals(p1, ordered.get(1));
        assertEquals(p2, ordered.get(2));
        assertEquals(p3, ordered.get(0));
    }

    @Test
    @DisplayName("orderPlayersInFlightList lancia RuntimeException")
    void testOrderPlayersException() {
        p1.setLap(0);
        p2.setLap(0);
        p3.setLap(1);
        p1.setPos(1);
        p2.setPos(1);
        p3.setPos(3);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);

        assertThrows(RuntimeException.class,
                () -> board.orderPlayersInFlightList());
    }

    @Test
    @DisplayName("checkOverLap: nessun incremento del lap")
    void testCheckOverLap1() {
        p1.setLap(0);
        p1.setPos(10);

        int temp = board.checkOverLap(p1, 13);

        assertEquals(0, p1.getLap());
        assertEquals(13, temp);
    }

    @Test
    @DisplayName("checkOverLap: incrementa lap, nuova pos")
    void testCheckOverLap2() {
        p1.setLap(0);
        p1.setPos(12);

        int temp = board.checkOverLap(p1, 19); //conferma che le posizioni le contiamo da 1 a 18 e non da 0 a 17

        assertEquals(1, p1.getLap());
        assertEquals(1, temp);
    }

    @Test
    @DisplayName("checkOverLap: decrementa lap, nuova pos")
    void testCheckOverLap3() {
        p1.setLap(0);
        p1.setPos(2);


        int temp = board.checkOverLap(p1, -5); //conferma che le posizioni le contiamo da 1 a 18 e non da 0 a 17

        assertEquals(-1, p1.getLap());
        assertEquals(13, temp);
    }

    //testare flightcardboard di liv 2

    @Test
    @DisplayName("moveRocket: su lista vuota non fa nulla")
    void testMoveRocketEmpty() throws InvalidPlayerException {
        // non aggiungo nessun player
        // non deve sollevare eccezioni
        board.moveRocket(5, p1);
    }

    @Test
    @DisplayName("moveRocket: con player non in flight lancia InvalidPlayerException")
    void testMoveRocketNotInFlight() {
        board.addPlayer(p2);
        assertThrows(InvalidPlayerException.class,
                () -> board.moveRocket(3, p1));
    }

    @Test
    @DisplayName("moveRocket: muove in avanti senza overlap")
    void testMoveRocket1() throws InvalidPlayerException {
        p1.setPos(2);
        p1.setLap(0);
        board.addPlayer(p1);

        board.moveRocket(3, p1);
        assertEquals(5, p1.getPos());
    }

    @Test
    @DisplayName("moveRocket: muove in avanti con overlap")
    void testMoveRocket2() throws InvalidPlayerException {
        p1.setPos(16);
        p1.setLap(0);
        board.addPlayer(p1);

        board.moveRocket(3, p1);
        assertEquals(1, p1.getPos());
        assertEquals(1, p1.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove indietro senza overlap")
    void testMoveRocket3() {
        p1.setPos(16);
        p1.setLap(1);
        board.addPlayer(p1);

        board.moveRocket(-5, p1);
        assertEquals(11, p1.getPos());
        assertEquals(1, p1.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove indietro con overlap")
    void testMoveRocket4() {
        p1.setPos(3);
        p1.setLap(0);
        board.addPlayer(p1);

        board.moveRocket(-5, p1);
        assertEquals(16, p1.getPos());
        assertEquals(-1, p1.getLap());
    }


    @Test
    @DisplayName("moveRocket: muove avanti con players in mezzo, no overlap")
    void testMoveRocket5() throws InvalidPlayerException {
        p1.setPos(8);
        p2.setPos(13);
        p3.setPos(10);
        p4.setPos(6);
        p1.setLap(0);
        p2.setLap(0);
        p3.setLap(0);
        p4.setLap(0);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(5, p4);

        assertEquals(14, p4.getPos());
        assertEquals(0, p4.getLap());
    }



    @Test
    @DisplayName("moveRocket: muove avanti con players in mezzo, overlap")
    void testMoveRocket6() throws InvalidPlayerException {
        p1.setPos(14);
        p2.setPos(1);
        p3.setPos(16);
        p4.setPos(12);
        p1.setLap(1);
        p2.setLap(2);
        p3.setLap(1);
        p4.setLap(1);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(5, p4);

        assertEquals(2, p4.getPos());
        assertEquals(2, p4.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove indietro con players in mezzo, no overlap, tutti players stesso lap")
    void testMoveRocket7() {
        p1.setPos(7);
        p2.setPos(4);
        p3.setPos(9);
        p4.setPos(5);
        p1.setLap(1);
        p2.setLap(1);
        p3.setLap(1);
        p4.setLap(1);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(-3, p3);

        assertEquals(3, p3.getPos());
        assertEquals(1, p3.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove indietro con players in mezzo, no overlap, overlappato da altro player fermo")
    void testMoveRocket8() {
        p1.setPos(7);
        p2.setPos(4);
        p3.setPos(9);
        p4.setPos(5);
        p1.setLap(1);
        p2.setLap(2);
        p3.setLap(1);
        p4.setLap(1);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(-3, p3);

        assertEquals(3, p3.getPos());
        assertEquals(1, p3.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove indietro con players in mezzo, con overlap, tutti players stesso lap")
    void testMoveRocket9() {
        p1.setPos(4);
        p2.setPos(1);
        p3.setPos(6);
        p4.setPos(2);
        p1.setLap(1);
        p2.setLap(1);
        p3.setLap(1);
        p4.setLap(1);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(-3, p3);

        assertEquals(18, p3.getPos());
        assertEquals(0, p3.getLap());
    }

    @Test
    @DisplayName("moveRocket: muove avanti con players in mezzo, no overlap personale, overlappa altro player")
    void testMoveRocket10() throws InvalidPlayerException {
        p1.setPos(8);
        p2.setPos(13);
        p3.setPos(10);
        p4.setPos(6);
        p1.setLap(0);
        p2.setLap(-1);
        p3.setLap(0);
        p4.setLap(0);

        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);

        board.moveRocket(5, p4);

        assertEquals(14, p4.getPos());
        assertEquals(0, p4.getLap());
    }

    @Test
    @DisplayName("eliminateOverlappedPlayers rimuove i player overlappati")
    void testEliminateOverlappedPlayers() {
        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);
        p1.setLap(-1);
        p2.setLap(0);
        p3.setLap(0);
        p4.setLap(0);
        p1.setPos(4);
        p2.setPos(6);
        p3.setPos(9);
        p4.setPos(10);
        board.eliminateOverlappedPlayers();
        List<Player> remaining = board.getOrderedPlayers();
        assertFalse(remaining.contains(p1));
        assertTrue(remaining.contains(p2));
        assertTrue(remaining.contains(p3));
        assertTrue(remaining.contains(p4));
        assertEquals(3, remaining.size());
    }

    @Test
    @DisplayName("eliminateOverlappedPlayers rimuove i player overlappati")
    void testEliminateOverlappedPlayers2() {
        board.addPlayer(p1);
        board.addPlayer(p2);
        board.addPlayer(p3);
        board.addPlayer(p4);
        p1.setLap(1);
        p2.setLap(1);
        p3.setLap(2);
        p4.setLap(1);
        p1.setPos(4);
        p2.setPos(2);
        p3.setPos(10);
        p4.setPos(9);
        board.eliminateOverlappedPlayers();
        List<Player> remaining = board.getOrderedPlayers();
        assertFalse(remaining.contains(p1));
        assertFalse(remaining.contains(p2));
        assertTrue(remaining.contains(p3));
        assertFalse(remaining.contains(p4));
        assertEquals(1, remaining.size());
    }
}



