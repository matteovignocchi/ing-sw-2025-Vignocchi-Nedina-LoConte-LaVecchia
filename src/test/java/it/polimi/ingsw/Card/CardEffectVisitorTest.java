package it.polimi.ingsw.Card;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardEffectVisitorTest {

    @Mock Controller controller;
    @Mock FlightCardBoard board;

    Player p1, p2;
    LinkedList<Player> players;
    CardEffectVisitor visitor;

    @BeforeEach
    void init() throws BusinessLogicException {
        p1 = spy(new Player(1, false, 11));
        p2 = spy(new Player(2, false, 22));

        players = new LinkedList<>(List.of(p1, p2));

        when(controller.getFlightCardBoard()).thenReturn(board);
        when(board.getOrderedPlayers()).thenReturn(players);

        lenient().when(controller.getNickByPlayer(p1)).thenReturn("A");
        lenient().when(controller.getNickByPlayer(p2)).thenReturn("B");

        visitor = new CardEffectVisitor(controller);
    }

    @Test
    void ctorNullControllerThrows() {
        assertThrows(NullPointerException.class, () -> new CardEffectVisitor(null));
    }

    //——— OpenSpaceCard —————————————————————————————————————

    @Test
    void visitOpenSpace_powerZero_eliminated() throws BusinessLogicException {
        when(controller.getPowerEngineForCard(p1)).thenReturn(0);
        when(controller.getPowerEngineForCard(p2)).thenReturn(3);

        visitor.visit(new OpenSpaceCard("X"));

        assertTrue(p1.isEliminated());
        verify(controller).inform("SERVER: Your engine power is 0", "A");
        verify(board).moveRocket(3, p2);
        verify(controller, times(2)).changeMapPosition(any(), any());
        verify(controller, times(2)).updatePositionForEveryBody();
    }

    @Test
    void visitOpenSpace_powerPositive_moves() throws BusinessLogicException {
        when(controller.getPowerEngineForCard(p1)).thenReturn(2);
        when(controller.getPowerEngineForCard(p2)).thenReturn(0);

        visitor.visit(new OpenSpaceCard("X"));

        verify(board).moveRocket(2, p1);
        verify(controller).inform(
                "SERVER: Your engine power is 2. You move forward by those spaces.", "A"
        );
    }

    //——— StardustCard —————————————————————————————————————

    @Test
    void visitStardust_movesBackwardByExposed() throws BusinessLogicException {
        doReturn(1).when(p1).countExposedConnectors();
        doReturn(2).when(p2).countExposedConnectors();

        visitor.visit(new StardustCard("S"));

        InOrder in = inOrder(board);
        in.verify(board).moveRocket(-2, p2);
        in.verify(board).moveRocket(-1, p1);
    }


    //——— SlaversCard —————————————————————————————————————

    @Test
    void visitSlavers_defeatedRedeem() throws BusinessLogicException {
        SlaversCard card = new SlaversCard("L", 5, 10, 1, 2);
        when(controller.getFirePowerForCard(p1)).thenReturn(3.0);
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        visitor.visit(card);

        verify(board).moveRocket(-card.getDays(), p1);
        assertEquals(10, p1.getCredits());
        verify(controller).broadcastInform("SERVER: Slavers defeated by A!");
    }

    @Test
    void visitSlavers_beatenLoseCrew() throws BusinessLogicException {
        SlaversCard card = new SlaversCard("L", 5, 10, 1, 2);
        when(controller.getFirePowerForCard(p1)).thenReturn(1.0);

        visitor.visit(card);
        verify(controller).removeCrewmates(p1, card.getNumCrewmates());
    }

    @Test
    void visitSlavers_drawNothing() throws BusinessLogicException {
        SlaversCard card = new SlaversCard("L", 5, 10, 1, 2);
        when(controller.getFirePowerForCard(p1)).thenReturn(2.0);

        visitor.visit(card);

        verify(controller).inform("SERVER: You have the same firepower as the slavers. Draw, nothing happens\n" + "SERVER: Checking other players", "A");
    }

    //——— FirstWarzoneCard —————————————————————————————————————

    @Test
    void visitFirstWarzone_singlePlayer_skipped() throws BusinessLogicException {
        when(board.getOrderedPlayers()).thenReturn(new LinkedList<>(List.of(p1)));
        visitor = new CardEffectVisitor(controller);

        visitor.visit(new FirstWarzoneCard("F", 2, 1, List.of(0), List.of(true)));
        verify(controller).inform("SERVER: You are flying alone. warzone card effect not activated ", "A");
    }

    @Test
    void visitFirstWarzone_fullFlow() throws BusinessLogicException {
        when(board.getOrderedPlayers()).thenReturn(new LinkedList<>(List.of(p1, p2)));
        when(controller.getNumCrew(p1)).thenReturn(2);
        when(controller.getNumCrew(p2)).thenReturn(1);
        when(controller.getPowerEngineForCard(p1)).thenReturn(3);
        when(controller.getPowerEngineForCard(p2)).thenReturn(2);
        when(controller.getFirePowerForCard(p1)).thenReturn(5.0);
        when(controller.getFirePowerForCard(p2)).thenReturn(1.0);
        visitor = new CardEffectVisitor(controller);

        FirstWarzoneCard card = new FirstWarzoneCard("F", 1, 2, List.of(4, 2), List.of(false, true));
        visitor.visit(card);
        verify(board).moveRocket(-1, p2);
        verify(controller).removeCrewmates(p2, 2);

        when(p2.throwDice()).thenReturn(3);

        Mockito.clearInvocations(controller, board);

        visitor.visit(card);
        verify(controller, times(2)).defenceFromCannon(anyInt(), anyBoolean(), eq(6), eq(p2));
    }


    //——— SecondWarzoneCard —————————————————————————————————————

    @Test
    void visitSecondWarzone_singlePlayer_skipped() throws BusinessLogicException {
        when(board.getOrderedPlayers()).thenReturn(new LinkedList<>(List.of(p1)));
        visitor = new CardEffectVisitor(controller);

        visitor.visit(new SecondWarzoneCard("S", 2, 1, List.of(0), List.of(false)));
        verify(controller).inform("SERVER: You are flying alone. warzone card effect not activated ", "A");
    }

    @Test
    void visitSecondWarzone_fullFlow() throws BusinessLogicException {
        when(board.getOrderedPlayers()).thenReturn(new LinkedList<>(List.of(p1, p2)));
        when(controller.getFirePowerForCard(p1)).thenReturn(1.0);
        when(controller.getFirePowerForCard(p2)).thenReturn(2.0);
        when(controller.getPowerEngineForCard(p1)).thenReturn(3);
        when(controller.getPowerEngineForCard(p2)).thenReturn(1);
        when(controller.getNumCrew(p1)).thenReturn(2);
        when(controller.getNumCrew(p2)).thenReturn(1);
        visitor = new CardEffectVisitor(controller);

        SecondWarzoneCard card = new SecondWarzoneCard("S", 2, 5, List.of(4, 2), List.of(true, false));
        visitor.visit(card);
        verify(board).moveRocket(-2, p1);
        verify(controller).removeGoods(p2, 5);

        when(p2.throwDice()).thenReturn(3);

        Mockito.clearInvocations(controller, board);

        visitor.visit(card);
        verify(controller, times(2)).defenceFromCannon(anyInt(), anyBoolean(), eq(6), eq(p2));
    }


    //——— SmugglersCard —————————————————————————————————————

    @Test
    void visitSmugglers_defeatThenRedeem() throws BusinessLogicException {
        SmugglersCard card = new SmugglersCard("M", 1, 2, 3, List.of(Colour.RED));
        when(controller.getFirePowerForCard(p1)).thenReturn(4.0);
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        visitor.visit(card);

        verify(board).moveRocket(-card.getDays(), p1);
        verify(controller).manageGoods(p1, card.getRewardGoods());
        verify(controller).broadcastInform("\nSERVER: Smugglers defeated by A!");
    }

    @Test
    void visitSmugglers_loseGoods() throws BusinessLogicException {
        SmugglersCard card = new SmugglersCard("M", 1, 2, 5, List.of(Colour.BLUE));
        when(controller.getFirePowerForCard(p1)).thenReturn(1.0);

        visitor.visit(card);
        verify(controller).removeGoods(p1, card.getNumRemovedGoods());
    }

    //——— AbandonedShipCard —————————————————————————————————————

    @Test
    void visitAbandonedShip_redeemByFirst() throws BusinessLogicException {
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);
        AbandonedShipCard card = new AbandonedShipCard("A", 2, 5, 1);

        visitor.visit(card);

        verify(board).moveRocket(-card.getDays(), p1);
        assertEquals(5, p1.getCredits());
        verify(controller).removeCrewmates(p1, card.getNumCrewmates());
    }

    @Test
    void visitAbandonedShip_skipFirst_thenSecond() throws BusinessLogicException {
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(false);
        when(controller.askPlayerDecision(anyString(), eq(p2))).thenReturn(true);
        AbandonedShipCard card = new AbandonedShipCard("A", 2, 5, 1);

        visitor.visit(card);

        verify(controller).inform("SERVER: Asking other players...", "A");
        verify(board).moveRocket(-card.getDays(), p2);
    }

    //——— AbandonedStationCard —————————————————————————————————————

    @Test
    void visitAbandonedStation_insufficientCrew() throws BusinessLogicException {
        when(controller.getNumCrew(p1)).thenReturn(1);
        AbandonedStationCard card = new AbandonedStationCard("S", 5, 2, List.of(Colour.GREEN));

        visitor.visit(card);

        verify(controller).inform("SERVER: You don't have enough crewmates to be able to redeem the card's reward", "A");
    }

    @Test
    void visitAbandonedStation_redeem() throws BusinessLogicException {
        when(controller.getNumCrew(p1)).thenReturn(2);
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);
        AbandonedStationCard card = new AbandonedStationCard("S", 1, 2, List.of(Colour.GREEN));

        visitor.visit(card);

        verify(board).moveRocket(-card.getDays(), p1);
        verify(controller).manageGoods(p1, card.getStationGoods());
    }

    //——— MeteoritesRainCard —————————————————————————————————————

    @Test
    void visitMeteoritesRain_skippedWhenNoneConnected() throws BusinessLogicException {
        p1.setConnected(false);
        p2.setConnected(false);
        visitor.visit(new MeteoritesRainCard("R", List.of(1,2), List.of(true,false)));
    }

    @Test
    void visitMeteoritesRain_hitsAll() throws BusinessLogicException {
        p1.setConnected(true);
        p2.setConnected(true);
        visitor.visit(new MeteoritesRainCard("R", List.of(1,2), List.of(true,false)));
        verify(controller, times(2)).defenceFromMeteorite(anyInt(), anyBoolean(), anyInt(), anyList(), anyInt());
    }

    //——— PiratesCard —————————————————————————————————————

    @Test
    void visitPirates_defeatThenStop() throws BusinessLogicException {
        PiratesCard card = new PiratesCard("P", 3, 2, 5, List.of(1), List.of(true));
        when(controller.getFirePowerForCard(p1)).thenReturn(10.0);
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        visitor.visit(card);
        verify(controller).broadcastInform("SERVER: Pirates defeated by A!");
    }

    @Test
    void visitPirates_loseThenCannon() throws BusinessLogicException {
        PiratesCard card = new PiratesCard("P", 3, 2, 5, List.of(1,2), List.of(true,false));
        when(controller.getFirePowerForCard(p1)).thenReturn(1.0);
        p1.setConnected(true);

        visitor.visit(card);
        verify(controller, atLeastOnce()).defenceFromCannon(anyInt(), anyBoolean(), anyInt(), eq(p1));
    }

    //——— PlanetsCard —————————————————————————————————————

    @Test
    void visitPlanets_someLandAndWrapAround() throws BusinessLogicException {
        List<List<Colour>> rewardGoods = Arrays.asList(
                Collections.singletonList(Colour.BLUE),
                Collections.singletonList(Colour.RED)
        );
        PlanetsCard card = new PlanetsCard("PL", rewardGoods, 1);
        when(controller.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        visitor.visit(card);
        verify(controller).manageGoods(p1, card.getRewardGoods().get(0));
    }

    //——— PlaugeCard —————————————————————————————————————

    @Test
    void visitPlauge_startsOnAll() throws BusinessLogicException {
        visitor.visit(new PlagueCard("Z"));
        verify(controller).startPlauge(p1);
        verify(controller).startPlauge(p2);
    }
}

