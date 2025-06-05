package it.polimi.ingsw.galaxytrucker.Server.Model;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.*;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;


class ControllerTest {
    Controller controller, spyController;
    FlightCardBoard fBoard;
    Player p1, p2, p3, p4;
    VirtualView v1, v2, v3, v4;
    Consumer onGameEnd;
    Set<String> loggedInUsers;
    boolean isDemo = false;

    @BeforeEach
    void setUp() throws Exception {
        onGameEnd = mock(Consumer.class);

        loggedInUsers= new HashSet<>();
        loggedInUsers.add("Gabri");
        loggedInUsers.add("Franci");
        loggedInUsers.add("Oleg");
        loggedInUsers.add("Teo");

        v1 = mock(VirtualView.class);
        v2 = mock(VirtualView.class);
        v3 = mock(VirtualView.class);
        v4 = mock(VirtualView.class);

        controller = new Controller(isDemo, 999, 4, onGameEnd, loggedInUsers);
        spyController = Mockito.spy(controller);

        fBoard = controller.getFlightCardBoard();

        controller.addPlayer("Gabri", v1);
        controller.addPlayer("Franci", v2);
        controller.addPlayer("Oleg", v3);
        controller.addPlayer("Teo", v4);

        p1 = Mockito.spy(controller.getPlayerCheck("Gabri"));
        p2 = Mockito.spy(controller.getPlayerCheck("Franci"));
        p3 = Mockito.spy(controller.getPlayerCheck("Oleg"));
        p4 = Mockito.spy(controller.getPlayerCheck("Teo"));

        controller.getPlayersByNickname().put("Gabri", p1);
        controller.getPlayersByNickname().put("Franci", p2);
        controller.getPlayersByNickname().put("Oleg", p3);
        controller.getPlayersByNickname().put("Teo", p4);

        p1.setPos(13);
        p1.setLap(2);
        p2.setPos(18);
        p2.setLap(1);
        p3.setPos(11);
        p3.setLap(1);
        p4.setPos(12);
        p4.setLap(1);

    }

    @Test
    @DisplayName("activate openSpaceCard")
    void activateCardTest1() throws BusinessLogicException, IOException {
        OpenSpaceCard card = new OpenSpaceCard("1");
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
        StardustCard card = new StardustCard("2");
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

    @Test
    @DisplayName ("Start awards phase")
    void startAwardsPhaseTest() throws Exception {

        doNothing().when(v1).inform(anyString());
        doNothing().when(v2).inform(anyString());
        doNothing().when(v3).inform(anyString());
        doNothing().when(v4).inform(anyString());

        p1.setGamePhase(GamePhase.WAITING_FOR_TURN);
        p2.setGamePhase(GamePhase.WAITING_FOR_TURN);
        p3.setGamePhase(GamePhase.WAITING_FOR_TURN);
        p4.setGamePhase(GamePhase.WAITING_FOR_TURN);

        fBoard.addPlayer(p1);
        fBoard.addPlayer(p2);
        fBoard.addPlayer(p3);
        fBoard.addPlayer(p4);
        fBoard.orderPlayersInFlightList();
        fBoard.setOverlappedPlayersEliminated();
        fBoard.eliminatePlayers();

        assertFalse(p1.isEliminated());
        assertFalse(p2.isEliminated());
        assertTrue(p3.isEliminated());
        assertTrue(p4.isEliminated());

        /**
        assertEquals(fBoard.getBonusFirstPosition(), 8);
        assertEquals(fBoard.getBonusSecondPosition(), 6);
        assertEquals(fBoard.getBonusThirdPosition(), 4);
        assertEquals(fBoard.getBonusFourthPosition(), 2);
        assertEquals(fBoard.getBonusBestShip(), 4);
        assertEquals(fBoard.getBonusRedCargo(), 4);
        assertEquals(fBoard.getBonusGreenCargo(), 2);
        assertEquals(fBoard.getBrokenMalus(), -1);
         */

        when(p1.countExposedConnectors()).thenReturn(6);
        when(p2.countExposedConnectors()).thenReturn(5);
        when(p3.countExposedConnectors()).thenReturn(10);
        when(p4.countExposedConnectors()).thenReturn(5);

        List<Colour> colours1 = new ArrayList<>();
        List<Colour> colours2 = List.of(Colour.YELLOW, Colour.YELLOW, Colour.RED);
        List<Colour> colours3 = List.of(Colour.GREEN, Colour.BLUE);
        List<Colour> colours4 = List.of(Colour.RED, Colour.RED, Colour.GREEN);

        when(p1.getTotalListOfGood()).thenReturn(colours1);
        when(p2.getTotalListOfGood()).thenReturn(colours2);
        when(p3.getTotalListOfGood()).thenReturn(colours3);
        when(p4.getTotalListOfGood()).thenReturn(colours4);

        when(p1.checkDiscardPile()).thenReturn(0);
        when(p2.checkDiscardPile()).thenReturn(4);
        when(p3.checkDiscardPile()).thenReturn(1);
        when(p4.checkDiscardPile()).thenReturn(10);

        controller.startAwardsPhase();

        assertEquals(8, p1.getCredits());
        assertEquals(16, p2.getCredits());
        assertEquals(1, p3.getCredits());
        assertEquals(-1, p4.getCredits());


        assertEquals(GamePhase.EXIT, p1.getGamePhase());
        assertEquals(GamePhase.EXIT, p2.getGamePhase());
        assertEquals(GamePhase.EXIT, p3.getGamePhase());
        assertEquals(GamePhase.EXIT, p4.getGamePhase());

        verify(v1).inform("SERVER: " + "Your total credits are: " + p1.getCredits() + " You won!");
        verify(v2).inform("SERVER: " + "Your total credits are: " + p2.getCredits() + " You won!");
        verify(v3).inform("SERVER: " + "Your total credits are: " + p3.getCredits() + " You won!");
        verify(v4).inform("SERVER: " + "Your total credits are: " + p4.getCredits() + " You lost!");

    }

}
