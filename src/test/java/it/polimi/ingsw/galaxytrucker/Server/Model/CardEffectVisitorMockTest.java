package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Server.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.CardEffectVisitor;
import it.polimi.ingsw.galaxytrucker.Server.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard.FlightCardBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardEffectVisitorMockTest {
    Controller mockCtrl;
    FlightCardBoard mockBoard;
    Player p1, p2, p3, p4;
    List<Player> players;

    @BeforeEach
    void setUp() {
        mockCtrl  = mock(Controller.class);
        mockBoard = mock(FlightCardBoard.class);
        p1 = mock(Player.class);
        p2 = mock(Player.class);
        p3 = mock(Player.class);
        p4 = mock(Player.class);
        p1.setLap(2);
        p1.setPos(3);
        p2.setLap(2);
        p2.setPos(5);
        p3.setLap(1);
        p3.setPos(8);
        p4.setLap(1);
        p4.setPos(12);
        when(mockCtrl.getFlightCardBoard()).thenReturn(mockBoard);
        players = Arrays.asList(p1, p2, p3, p4);
        when(mockBoard.getOrderedPlayers()).thenReturn(players);
    }

    @Test
    @DisplayName("costruttore: controller null -> NPE")
    void NullController() {
        assertThrows(NullPointerException.class,
                () -> new CardEffectVisitor(null));
    }

    @Test
    @DisplayName("costruttore: controller null -> NPE")
    void NullFlightCardBoard() {
        when(mockCtrl.getFlightCardBoard()).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> new CardEffectVisitor(mockCtrl));
    }

    @Test
    @DisplayName("visit(OpenSpaceCard): chiama moveRocket (mock) per ogni player")
    void visitOpenSpaceCard() throws CardEffectException {
        when(mockCtrl.getPowerEngine(p1)).thenReturn(2);
        when(mockCtrl.getPowerEngine(p2)).thenReturn(3);
        when(mockCtrl.getPowerEngine(p3)).thenReturn(1);
        when(mockCtrl.getPowerEngine(p4)).thenReturn(2);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);

        OpenSpaceCard card = new OpenSpaceCard();
        visitor.visit(card);

        InOrder inOrder = inOrder(mockBoard);
        inOrder.verify(mockBoard).moveRocket(2, p1);
        inOrder.verify(mockBoard).moveRocket(3, p2);
        inOrder.verify(mockBoard).moveRocket(1, p3);
        inOrder.verify(mockBoard).moveRocket(2, p4);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("visit(StardustCard): chiama moveRocket (mock) per ogni player, dall'ultimo al primo")
    void visitStardustCard() throws CardEffectException {
        when(p1.countExposedConnectors()).thenReturn(2);
        when(p2.countExposedConnectors()).thenReturn(5);
        when(p3.countExposedConnectors()).thenReturn(8);
        when(p4.countExposedConnectors()).thenReturn(7);

        StardustCard card = new StardustCard();
        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        InOrder inOrder = inOrder(mockBoard);
        inOrder.verify(mockBoard).moveRocket(-7, p4);
        inOrder.verify(mockBoard).moveRocket(-8, p3);
        inOrder.verify(mockBoard).moveRocket(-5, p2);
        inOrder.verify(mockBoard).moveRocket(-2, p1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("visit(SlaversCard)")
    void visitSlaversCard() throws CardEffectException {
        SlaversCard card = new SlaversCard(6, 10, 4, 5);
        when(mockCtrl.getFirePower(p1)).thenReturn(4.0);
        when(mockCtrl.getFirePower(p2)).thenReturn(5.0);
        when(mockCtrl.getFirePower(p3)).thenReturn(6.0);
        when(mockCtrl.getFirePower(p4)).thenReturn(7.0);

        when(mockCtrl.askPlayerDecision(anyString(), eq(p3))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockCtrl).removeCrewmate(p1, card.getNumCrewmates());
        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p2));
        verify(mockBoard, never()).moveRocket(-6, p2);
        verify(mockCtrl, never()).removeCrewmate(p2, card.getNumCrewmates());
        verify(mockBoard).moveRocket(-6, p3);
        verify(p3).addCredits(10);
        verify(mockBoard, never()).moveRocket(-6, p4);
        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p4));
        verify(mockCtrl, never()).removeCrewmate(p4, card.getNumCrewmates());
    }

    @Test
    @DisplayName("visit(PlanetsCard)")
    void visitPlanetsCard() throws CardEffectException {
        //PlanetsCard card = new PlanetsCard();
    }



}

