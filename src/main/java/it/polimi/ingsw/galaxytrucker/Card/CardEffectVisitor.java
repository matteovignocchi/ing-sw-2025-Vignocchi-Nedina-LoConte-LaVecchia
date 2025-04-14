package it.polimi.ingsw.galaxytrucker.Card;

import it.polimi.ingsw.galaxytrucker.Controller;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import java.util.ArrayList;
import java.util.List;

public class CardEffectVisitor implements CardVisitor {
    private Controller controller;
    private FlightCardBoard f;
    private List<Player> players;

    public CardEffectVisitor(Controller controller) {
        this.controller = controller;
        this.f = controller.getFlightCardBoard();
        this.players = f.getOrderedPlayers();
    }

    @Override
    public void visit(OpenSpaceCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");
        //gestire sempre le stesse eccezioni o creare quelle personalizzate
         */
        for (Player p : players) {
            int x = controller.getPowerEngine(p);
            f.moveRocket(x, p, players);
        }
    }

    @Override
    public void visit (StardustCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");
        */
        for (int i = players.size() - 1; i >= 0; i--) {
            Player p = players.get(i);
            int x = p.countExposedConnectors(); //perchè metodo nel player (?)
            f.moveRocket(-x, p, players);
        }
    }

    @Override
    public void visit(SlaversCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");

        for(Player p : players){
            if(p.getFirePower() > fire_power){
                if(p.askPlayerDecision()){
                    f.moveRocket(-days, p, players);
                    p.addCredits(credits);
                }
                break;
            } else if(p.getFirePower() < fire_power){
                p.removeCrewmates(num_crewmates);
            }
        }
         */
    }

    @Override
    public void visit(FirstWarzoneCard card) {}

    @Override
    public void visit(SecondWarzoneCard card) {}

    @Override
    public void visit(SmugglersCard card) {}

}
