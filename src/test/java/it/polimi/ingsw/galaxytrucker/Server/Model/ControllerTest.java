package it.polimi.ingsw.galaxytrucker.Server.Model;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Card.OpenSpaceCard;
import it.polimi.ingsw.galaxytrucker.Model.Card.StardustCard;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;


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
        //controller.getFlightCardBoard().addPlayer(p1);
        //controller.getFlightCardBoard().addPlayer(p2);
        //controller.getFlightCardBoard().addPlayer(p3);
        //controller.getFlightCardBoard().addPlayer(p4);
    }

    @Test
    @DisplayName("activate openSpaceCard")
    void activateCardTest1() throws BusinessLogicException, IOException {
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
        assertEquals(2, p3.getLap());
        assertEquals(2, p3.getPos());
    }

    @Test
    @DisplayName("activate StardustCard")
    void activateCardTest2() throws BusinessLogicException, IOException {
        StardustCard card = new StardustCard();
        Player spyP1, spyP2, spyP3, spyP4;
        spyP1 = Mockito.spy(p1);
        spyP2 = Mockito.spy(p2);
        spyP3 = Mockito.spy(p3);
        spyP4 = Mockito.spy(p4);
        doReturn(11).when(spyP1).countExposedConnectors();
        doReturn(14).when(spyP2).countExposedConnectors();
        doReturn(6).when(spyP3).countExposedConnectors();
        doReturn(18).when(spyP4).countExposedConnectors();
        controller.getFlightCardBoard().addPlayer(spyP1);
        controller.getFlightCardBoard().addPlayer(spyP2);
        controller.getFlightCardBoard().addPlayer(spyP3);
        controller.getFlightCardBoard().addPlayer(spyP4);

        controller.getFlightCardBoard().orderPlayersInFlightList();
        controller.activateCard(card);

        assertEquals(1, spyP3.getLap());
        assertEquals(10, spyP3.getPos());
        assertEquals(1, spyP4.getLap());
        assertEquals(1, spyP4.getPos());
        assertEquals(1, spyP1.getLap());
        assertEquals(24, spyP1.getPos());
        assertEquals(1, spyP2.getLap());
        assertEquals(22, spyP2.getPos());
    }

}
