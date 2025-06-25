package it.polimi.ingsw.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard2;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightCardBoard2Test {

    private FlightCardBoard2 board2;

    @BeforeEach
    void setUp() {
        board2 = new FlightCardBoard2(null);
    }

    @Test
    void testConstructorOverridesParameters() {
        assertEquals(24, board2.spacesNumber,       "spacesNumber deve essere 24");
        assertEquals(8,  board2.getBonusFirstPosition(),  "bonusFirstPosition deve essere 8");
        assertEquals(6,  board2.getBonusSecondPosition(), "bonusSecondPosition deve essere 6");
        assertEquals(4,  board2.getBonusThirdPosition(),  "bonusThirdPosition deve essere 4");
        assertEquals(2,  board2.getBonusFourthPosition(), "bonusFourthPosition deve essere 2");
        assertEquals(4,  board2.getBonusBestShip(),       "bonusBestShip deve essere 4");
    }

    @Test
    void testInheritedBonusesUnchanged() {
        assertEquals(4, board2.getBonusRedCargo(),    "redGoodBonus ereditato da FlightCardBoard");
        assertEquals(3, board2.getBonusYellowCargo(), "yellowGoodBonus ereditato da FlightCardBoard");
        assertEquals(2, board2.getBonusGreenCargo(),  "greenGoodBonus ereditato da FlightCardBoard");
        assertEquals(1, board2.getBonusBlueCargo(),   "blueGoodBonus ereditato da FlightCardBoard");
        assertEquals(-1, board2.getBrokenMalus(),     "malusBrokenTile ereditato da FlightCardBoard");
    }

    @Test
    void testInheritedAddAndOrderPlayers() {
        Player p1 = new Player(1, true, 0);
        Player p2 = new Player(2, true, 0);
        board2.addPlayer(p1);
        board2.addPlayer(p2);
        p1.setLap(1); p1.setPos(2);
        p2.setLap(2); p2.setPos(1);
        board2.orderPlayersInFlightList();
        List<Player> ord = board2.getOrderedPlayers();
        assertEquals(p2, ord.get(0), "p2 deve venire primo (lap maggiore)");
        assertEquals(p1, ord.get(1), "p1 deve venire secondo");
    }

    @Test
    void testCheckOverLapWrapsAroundCorrectly() {
        Player p = new Player(1, true, 0);
        p.setLap(0);
        int pos = board2.checkOverLap(p, 25);
        assertEquals(1, pos, "25 wrap attorno a 1 su board2");
        assertEquals(1, p.getLap(), "lap incrementato di 1");

        p.setLap(1);
        pos = board2.checkOverLap(p, 0);
        assertEquals(24, pos, "0 wrap attorno a 24 su board2");
        assertEquals(0, p.getLap(), "lap decrementato di 1");
    }
}
