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
        // TODO: inizializza i tuoi Player con id, pos, lap, ecc.
        p1 = new Player(1, true);
        p1.setLap(0);
        p1.setPos(0);
        p2 = new Player(2, true);
        p2.setLap(0);
        p2.setPos(0);
        p3 = new Player(3, true);
        p3.setLap(0);
        p3.setPos(0);
        p4 = new Player(4, true);
        p4.setLap(0);
        p4.setPos(0);
        p5 = new Player(5, true);
        p5.setLap(0);
        p5.setPos(0);
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
    @DisplayName("checkOverLap incrementa lap se temp supera position_number")
    void testCheckOverLap1() {
        p1.setLap(0);
        p1.setPos(10);

        int temp = board.checkOverLap(p1, 13);

        assertEquals(0, p1.getLap());
        assertEquals(13, temp);
    }

    @Test
    @DisplayName("checkOverLap incrementa lap se temp supera position_number")
    void testCheckOverLap2() {
        p1.setLap(0);
        p1.setPos(12);


        int temp = board.checkOverLap(p1, 19); //conferma che le posizioni le contiamo da 1 a 18 e non da 0 a 17

        assertEquals(1, p1.getLap());
        assertEquals(1, temp);
    }

    //testare flightcardboard di liv 2

    @Test
    @DisplayName("moveRocket muove correttamente senza overlap")
    void testMoveRocketSimple() throws InvalidPlayerException {
        p1.setPos(2);
        board.addPlayer(p1);

        board.moveRocket(3, p1);
        assertEquals(5, p1.getPos());
    }

    @Test
    @DisplayName("moveRocket muove correttamente con overlap")
    void testMoveRocket2() throws InvalidPlayerException {
        p1.setPos(16);
        board.addPlayer(p1);

        board.moveRocket(3, p1);
        assertEquals(1, p1.getPos());
        assertEquals(1, p1.getLap());
    }

    @Test
    @DisplayName("moveRocket su lista vuota non fa nulla")
    void testMoveRocketEmpty() throws InvalidPlayerException {
        // non aggiungo nessun player
        // non deve sollevare eccezioni
        board.moveRocket(5, p1);
    }

    @Test
    @DisplayName("moveRocket con player non in flight lancia InvalidPlayerException")
    void testMoveRocketNotInFlight() {
        board.addPlayer(p2);
        assertThrows(InvalidPlayerException.class,
                () -> board.moveRocket(3, p1));
    }

    @Test
    @DisplayName("moveRocket muove caso difficile")
    void testMoveRocket3() throws InvalidPlayerException {
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
    @DisplayName("moveRocket muove caso difficile con overlap")
    void testMoveRocket4() throws InvalidPlayerException {
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
    @DisplayName("eliminateOverlappedPlayers rimuove i player overlappati")
    void testEliminateOverlappedPlayers() {
        // TODO: imposta pos/ lap su p1, p2 in modo che p2 overlappi p1
        board.addPlayer(p1);
        board.addPlayer(p2);
        // ad es. p1.lap=0,pos=3; p2.lap=1,pos=2
        board.eliminateOverlappedPlayers();
        List<Player> remaining = board.getOrderedPlayers();
        assertFalse(remaining.contains(p1));
        assertTrue(remaining.contains(p2));
    }
}
