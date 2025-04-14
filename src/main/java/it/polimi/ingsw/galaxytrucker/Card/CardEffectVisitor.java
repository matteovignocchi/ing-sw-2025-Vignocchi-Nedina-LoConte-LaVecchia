package it.polimi.ingsw.galaxytrucker.Card;

import it.polimi.ingsw.galaxytrucker.Controller;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
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
        */

        for (Player p : players) {
            double player_fire_power = controller.getFirePower(p);
            double slavers_fire_power = card.getFirePower();
            if(player_fire_power > slavers_fire_power) {
                if(controller.askPlayerDecision()){ //modificare, passare messaggio come parametro
                    f.moveRocket(-1 * card.getDays(), p, players);
                    p.addCredits(card.getCredits());
                }
                break;
            } else if (player_fire_power < slavers_fire_power) {
                controller.removeCrewmate(p, card.getNumCrewmates());
            }
        }
    }

    @Override
    public void visit(FirstWarzoneCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");
         */

        int i_less_firepower = 0;
        int i_less_crewmates = 0;
        int i_less_powerengine = 0;

        for (int i=1; i < players.size(); i++) {
            if(controller.getNumCrew(players.get(i)) < controller.getNumCrew(players.get(i_less_crewmates)))
                i_less_crewmates = i;
            if(controller.getFirePower(players.get(i)) < controller.getFirePower(players.get(i_less_firepower)))
                i_less_firepower = i;
            if(controller.getPowerEngine(players.get(i)) < controller.getPowerEngine(players.get(i_less_powerengine)))
                i_less_powerengine = i;
        }

        f.moveRocket(-card.getDays(), players.get(i_less_crewmates), players);
        controller.removeCrewmate(players.get(i_less_firepower), card.getNumCrewmates());
        Player p = players.get(i_less_powerengine);
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < card.getShotsDirections().size(); i++) {
            int res = p.throwDice() + p.throwDice();
            //MODIFICARE METODO ORIGINARIO IN CONTROLLER (No for each player, solo per p)
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }
    }

    @Override
    public void visit(SecondWarzoneCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");
         */

        int i_less_firepower = 0;
        int i_less_powerengine = 0;
        int i_less_crewmates = 0;

        for (int i=1; i < players.size(); i++) {
            if(controller.getNumCrew(players.get(i)) < controller.getNumCrew(players.get(i_less_crewmates)))
                i_less_crewmates = i;
            if(controller.getFirePower(players.get(i)) < controller.getFirePower(players.get(i_less_firepower)))
                i_less_firepower = i;
            if(controller.getPowerEngine(players.get(i)) < controller.getPowerEngine(players.get(i_less_powerengine)))
                i_less_powerengine = i;
        }
        f.moveRocket(-card.getDays(), players.get(i_less_firepower), players);
        controller.removeGoods(players.get(i_less_powerengine), card.getNumGoods());
        Player p = players.get(i_less_crewmates);
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < card.getShotsDirections().size(); i++) {
            int res = p.throwDice() + p.throwDice();
            //MODIFICARE METODO ORIGINARIO IN CONTROLLER (No for each player, solo per p)
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }
    }

    @Override
    public void visit(SmugglersCard card) {}

}
