package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
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
        mockCtrl = mock(Controller.class);
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
        List<List<Colour>> reward = new ArrayList<>(List.of(List.of(Colour.RED, Colour.RED, Colour.GREEN, Colour.BLUE),
                List.of(Colour.BLUE, Colour.YELLOW, Colour.YELLOW)));
        PlanetsCard card = new PlanetsCard(reward, 3);

        when(mockCtrl.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);
        when(mockCtrl.askPlayerDecision(anyString(), eq(p2))).thenReturn(false);
        when(mockCtrl.askPlayerDecision(anyString(), eq(p3))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockBoard).moveRocket(eq(-3), eq(p1));
        verify(mockCtrl).addGoods(eq(p1), eq(reward.getFirst()));
        verify(mockBoard, never()).moveRocket(eq(-3), eq(p2));
        verify(mockCtrl, never()).addGoods(eq(p2), anyList());
        verify(mockBoard).moveRocket(eq(-3), eq(p3));
        verify(mockCtrl).addGoods(eq(p3), eq(reward.get(1)));
        verify(mockBoard, never()).moveRocket(eq(-3), eq(p4));
        verify(mockCtrl, never()).addGoods(eq(p4), anyList());
    }

    @Test
    @DisplayName("visit(FirstWarzoneCard): sposta, rimuove crew e spara in ordine")
    void visitFirstWarzoneCard() throws CardEffectException {
        FirstWarzoneCard card = new FirstWarzoneCard(3, 2, List.of(0, 2), List.of(false, true));

        when(mockCtrl.getNumCrew(p1)).thenReturn(5);
        when(mockCtrl.getNumCrew(p2)).thenReturn(3);
        when(mockCtrl.getNumCrew(p3)).thenReturn(6);
        when(mockCtrl.getNumCrew(p4)).thenReturn(7);

        when(mockCtrl.getFirePower(p1)).thenReturn(8.0);
        when(mockCtrl.getFirePower(p2)).thenReturn(4.0);
        when(mockCtrl.getFirePower(p3)).thenReturn(9.0);
        when(mockCtrl.getFirePower(p4)).thenReturn(7.0);

        when(mockCtrl.getPowerEngine(p1)).thenReturn(5);
        when(mockCtrl.getPowerEngine(p2)).thenReturn(6);
        when(mockCtrl.getPowerEngine(p3)).thenReturn(2);
        when(mockCtrl.getPowerEngine(p4)).thenReturn(4);

        when(p3.throwDice()).thenReturn(2, 3, 6, 5);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        InOrder inOrder = inOrder(mockBoard, mockCtrl);
        inOrder.verify(mockBoard).moveRocket(-3, p2);
        inOrder.verify(mockCtrl).removeCrewmate(p2, 2);
        inOrder.verify(mockCtrl).defenceFromCannon(0, false, 5, p3);
        inOrder.verify(mockCtrl).defenceFromCannon(2, true, 11, p3);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("visit(SecondWarzoneCard): sposta, rimuove goods e spara in ordine")
    void visitSecondWarzoneCard() throws CardEffectException {
        SecondWarzoneCard card = new SecondWarzoneCard(4, 3, List.of(1, 3, 0), List.of(true, false, true));

        when(mockCtrl.getNumCrew(p1)).thenReturn(8);
        when(mockCtrl.getNumCrew(p2)).thenReturn(2);
        when(mockCtrl.getNumCrew(p3)).thenReturn(6);
        when(mockCtrl.getNumCrew(p4)).thenReturn(3);

        when(mockCtrl.getFirePower(p1)).thenReturn(7.0);
        when(mockCtrl.getFirePower(p2)).thenReturn(5.0);
        when(mockCtrl.getFirePower(p3)).thenReturn(9.0);
        when(mockCtrl.getFirePower(p4)).thenReturn(1.0);

        when(mockCtrl.getPowerEngine(p1)).thenReturn(1);
        when(mockCtrl.getPowerEngine(p2)).thenReturn(3);
        when(mockCtrl.getPowerEngine(p3)).thenReturn(8);
        when(mockCtrl.getPowerEngine(p4)).thenReturn(5);

        when(p2.throwDice()).thenReturn(2, 3, 2, 6, 4, 3);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        InOrder inOrder = inOrder(mockBoard, mockCtrl);
        inOrder.verify(mockBoard).moveRocket(-4, p4);
        inOrder.verify(mockCtrl).removeGoods(p1, 3);
        inOrder.verify(mockCtrl).defenceFromCannon(1, true, 5, p2);
        inOrder.verify(mockCtrl).defenceFromCannon(3, false, 8, p2);
        inOrder.verify(mockCtrl).defenceFromCannon(0, true, 7, p2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("visit(AbandonedShipCard): solo primo player risponde true")
    void visitAbandonedShipCard() throws CardEffectException {
        AbandonedShipCard card = new AbandonedShipCard(1, 6, 2);

        when(mockCtrl.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockBoard).moveRocket(-1, p1);
        verify(p1).addCredits(6);
        verify(mockCtrl).removeCrewmate(p1, 2);

        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p2));
        verify(mockCtrl, never()).removeCrewmate(eq(p2), anyInt());
    }

    @Test
    @DisplayName("visit(AbandonedStationCard): primo player ha abbastanza crew e accetta")
    void visitAbandonedStationCard() throws CardEffectException {
        AbandonedStationCard card = new AbandonedStationCard(3, 1, List.of(Colour.BLUE, Colour.RED));

        when(mockCtrl.getNumCrew(p1)).thenReturn(5);
        when(mockCtrl.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockBoard).moveRocket(-1, p1);
        verify(mockCtrl).addGoods(p1, card.getStationGoods());

        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p2));
    }

    @Test
    @DisplayName("visit(SmugglersCard): primo player ha firepower maggiore e accetta")
    void visitSmugglersCard() throws CardEffectException {
        SmugglersCard card = new SmugglersCard(4, 2, 1, List.of(Colour.RED, Colour.GREEN));

        when(mockCtrl.getFirePower(p1)).thenReturn(6.0);
        when(mockCtrl.askPlayerDecision(anyString(), eq(p1))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockBoard).moveRocket(-4, p1);
        verify(mockCtrl).addGoods(p1, card.getRewardGoods());
        verify(mockCtrl, never()).removeGoods(eq(p2), anyInt());
    }

    @Test
    @DisplayName("visit(SmugglersCard): p1 e p2 sconfitti, p3 accetta ricompensa")
    void visitSmugglersCard_MultiplePlayers() throws CardEffectException {
        SmugglersCard card = new SmugglersCard(5, 5, 2, List.of(Colour.RED, Colour.YELLOW));

        when(mockCtrl.getFirePower(p1)).thenReturn(2.0); // sconfitto
        when(mockCtrl.getFirePower(p2)).thenReturn(4.5); // sconfitto
        when(mockCtrl.getFirePower(p3)).thenReturn(6.0); // vince
        when(mockCtrl.getFirePower(p4)).thenReturn(7.0); // ignorato

        when(mockCtrl.askPlayerDecision(anyString(), eq(p2))).thenReturn(false); // nuovo!
        when(mockCtrl.askPlayerDecision(anyString(), eq(p3))).thenReturn(true);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockCtrl).removeGoods(p1, 2);
        verify(mockCtrl).removeGoods(p2, 2);
        verify(mockCtrl, never()).removeGoods(p3, 2);
        verify(mockCtrl, never()).removeGoods(p4, 2);

        verify(mockBoard).moveRocket(-5, p3);
        verify(mockCtrl).addGoods(p3, card.getRewardGoods());

        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p1));
        verify(mockCtrl, never()).askPlayerDecision(anyString(), eq(p4));
    }


    @Test
    @DisplayName("visit(MeteoritesRainCard): spara su tutti i player")
    void visitMeteoritesRainCard() throws CardEffectException {
        MeteoritesRainCard card = new MeteoritesRainCard(List.of(0, 1), List.of(true, false));

        when(p1.throwDice()).thenReturn(3, 3);
        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(mockCtrl, times(4)).defenceFromMeteorite(0, true, 6);
        verify(mockCtrl, times(4)).defenceFromMeteorite(1, false, 6);
    }

    @Test
    @DisplayName("visit(PlaugeCard): chiama startPlague su ogni player")
    void visitPlagueCard() throws CardEffectException {
        PlaugeCard card = new PlaugeCard();
        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        for (Player p : players) {
            verify(mockCtrl).startPlauge(p);
        }
    }

    @Test
    @DisplayName("visit(PiratesCard): tutti vengono colpiti")
    void visitPiratesCardAllLose() throws CardEffectException {
        PiratesCard card = new PiratesCard(10, 2, 3, List.of(0, 1), List.of(false, true));

        when(mockCtrl.getFirePower(p1)).thenReturn(3.0);
        when(mockCtrl.getFirePower(p2)).thenReturn(4.0);
        when(mockCtrl.getFirePower(p3)).thenReturn(5.0);
        when(mockCtrl.getFirePower(p4)).thenReturn(6.0);

        when(p1.throwDice()).thenReturn(2, 4);

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        for (Player p : players) {
            verify(mockCtrl, times(1)).defenceFromCannon(0, false, 6, p);
            verify(mockCtrl, times(1)).defenceFromCannon(1, true, 6, p);
        }
    }

    @Test
    @DisplayName("visit(PiratesCard): p3 sconfigge i pirati, p1 e p2 subiscono danni")
    void visitPiratesCard_Cornercase() throws CardEffectException {
        PiratesCard card = new PiratesCard(10, 2, 3, List.of(0, 1), List.of(false, true));

        when(mockCtrl.getFirePower(p1)).thenReturn(5.0);
        when(mockCtrl.getFirePower(p2)).thenReturn(8.0);
        when(mockCtrl.getFirePower(p3)).thenReturn(11.0);
        when(mockCtrl.getFirePower(p4)).thenReturn(12.0);

        when(mockCtrl.askPlayerDecision(anyString(), eq(p3))).thenReturn(false);

        when(p1.throwDice()).thenReturn(2, 4);

        when(mockBoard.getOrderedPlayers()).thenReturn(List.of(p1, p2, p3, p4));

        CardEffectVisitor visitor = new CardEffectVisitor(mockCtrl);
        visitor.visit(card);

        verify(p1, times(2)).throwDice();

        for (Player p : List.of(p1, p2)) {
            verify(mockCtrl).defenceFromCannon(0, false, 6, p);
            verify(mockCtrl).defenceFromCannon(1, true, 6, p);
        }

        verify(mockCtrl).askPlayerDecision(anyString(), eq(p3));
        verify(mockBoard, never()).moveRocket(anyInt(), eq(p3));
        verify(p3, never()).addCredits(anyInt());

        verify(mockCtrl, never()).defenceFromCannon(anyInt(), anyBoolean(), anyInt(), eq(p4));
        verify(p4, never()).throwDice();
    }
}


