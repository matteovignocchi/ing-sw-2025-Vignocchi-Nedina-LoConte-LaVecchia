package it.polimi.ingsw.galaxytrucker.Server.Model;
import it.polimi.ingsw.galaxytrucker.Server.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

class ControllerTest {
    Controller controller, spyController;
    Player p1, p2, p3, p4;
    @BeforeEach
    void setUp() throws CardEffectException, IOException {
        controller = new Controller(false, 999);
        spyController = Mockito.spy(controller);
        p1 = new Player(1, false);
        p2 = new Player(2, false);
        p3 = new Player(3, false);
        p4 = new Player(4, false);
        p1.setPos(13);
        p1.setLap(2);
        p2.setPos(15);
        p2.setLap(2);
        p3.setPos(18);
        p3.setLap(1);
        p4.setPos(22);
        p4.setLap(1);
        controller.getFlightCardBoard().addPlayer(p1);
        controller.getFlightCardBoard().addPlayer(p2);
        controller.getFlightCardBoard().addPlayer(p3);
        controller.getFlightCardBoard().addPlayer(p4);
    }
    @Test
    @DisplayName("activate openSpaceCard")
    void activateCardTest() throws CardEffectException, IOException {
        OpenSpaceCard card = new OpenSpaceCard();
        doReturn(2).when(spyController).getPowerEngine(p1);
        doReturn(4).when(spyController).getPowerEngine(p2);
        doReturn(6).when(spyController).getPowerEngine(p3);
        doReturn(3).when(spyController).getPowerEngine(p4);

        //List<Player> actual = List.of(p1, p2, p3, p4);
        //assertEquals(actual, spyController.getFlightCardBoard().getOrderedPlayers());
        //List<Player> expected = List.of(p2, p1, p4, p3);
        spyController.getFlightCardBoard().orderPlayersInFlightList();
        //assertEquals(expected, spyController.getFlightCardBoard().getOrderedPlayers());
        spyController.activateCard(card);

        assertEquals(2, p2.getLap());
        assertEquals(20, p2.getPos());
        assertEquals(2, p1.getLap());
        assertEquals(15, p1.getPos());
        assertEquals(2, p4.getLap());
        assertEquals(1, p4.getPos());
        assertEquals(2, p3.getLap()); // test: actual 1   DOVE ERRORE?
        assertEquals(2, p3.getPos()); // test: actual 24
    }

}
