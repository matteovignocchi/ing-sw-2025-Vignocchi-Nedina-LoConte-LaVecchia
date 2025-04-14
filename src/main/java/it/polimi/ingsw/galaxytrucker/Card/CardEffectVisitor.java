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
        double slavers_fire_power = card.getFirePower();
        for (Player p : players) {
            double player_fire_power = controller.getFirePower(p);
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
    public void visit(SmugglersCard card) {
        /*
        if(players == null) throw new NullPointerException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new NullPointerException("Null flight card board");
         */
        double smugglers_fire_power = card.getFirePower();
        for(Player p : players) {
            double player_fire_power = controller.getFirePower(p);
            if(player_fire_power > smugglers_fire_power){
                if(controller.askPlayerDecision()){
                    f.moveRocket(-card.getDays(), p, players);
                    controller.addGoods(p, card.getRewardGoods());
                }
                break;
            } else if (player_fire_power < smugglers_fire_power) {
                controller.removeGoods(p, card.getNumRemovedGoods());
            }
        }
    }

    @Override
    public void visit(AbandonedShipCard card) {
        //if(players == null) throw new NullPointerException("Null players list");
        //else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //else if(f == null) throw new NullPointerException("Null flight card board");
        for(Player p : players) {
            if (controller.askPlayerDecision(p)) {
                int days = card.getDays();
                f.moveRocket(-days, p, players);
                int credits = card.getCredits();
                p.addCredits(credits); //assicurarsi che il metodo vada in player o in controller
                int num_crewmates = card.getNumCrewmates();
                controller.removeCrewmate(p, num_crewmates);
                break;
            }
        }
    }

    @Override
    public void visit(AbandonedStationCard card) {
        //if(players == null) throw new NullPointerException("Null players list");
        //else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //else if(f == null) throw new NullPointerException("Null flight card board");
        for(Player p: players) {
            int num_crewmates = card.getNumCrewmates();
            if(controller.getNumCrew(p)>=num_crewmates){
                if(controller.askPlayerDecision(p)){
                    int days = card.getDays();
                    f.moveRocket(-days, p, players);
                    controller.addGoods(p, card.getStationGoods());
                }
                break;
            }
        }
    }

    @Override
    public void visit(MeteoritesRainCard card) {
        //if(players == null) throw new NullPointerException("Null players list");
        //else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //else if(f == null) throw new NullPointerException("Null flight card board");
        for (int i = 0; i < card.getMeteorites_directions().size(); i++) {
            int res = players.getFirst().throwDice() + players.getFirst().throwDice();
            for(Player p : players){
                controller.defenceFromMeteorite(card.getMeteorites_directions().get(i), card.getMeteorites_size().get(i), res);
            }
        }
    }

    @Override
    public void visit(PiratesCard card) {
        //if(players == null) throw new NullPointerException("Null players list");
        //else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //else if(f == null) throw new NullPointerException("Null flight card board");
        List<Player> losers = new ArrayList<>();
        for(Player p : players) {
            if(controller.getFirePower(p) > card.getFirePower()){
                if(controller.askPlayerDecision(p)){
                    int days = card.getDays();
                    f.moveRocket(-days, p, players);
                    int credits = card.getCredits();
                    p.addCredits(credits);
                }
                break;
            } else if (controller.getFirePower(p) < card.getFirePower())
                losers.add(p);
        }
        if(losers.getFirst() != null){
            int res = losers.getFirst().throwDice() + losers.getFirst().throwDice();
            for(Player p : losers){
                for(int i = 0; i < card.getShots_directions().size(); i++){
                    controller.defenceFromCannon(card.getShots_directions().get(i), card.getShots_size().get(i), res);
                }
            }
        }
    }

    @Override
    public void visit(PlanetsCard card) {
        //if(players == null) throw new NullPointerException("Null players list");
        //else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //else if(f == null) throw new NullPointerException("Null flight card board");
        int z = 0;
        for(Player p : players){
            if(controller.askPlayerDecision(p)){
                int days = card.getDays();
                f.moveRocket(-days, p, players);
                controller.addGoods(p, card.getRewardGoods().get(z));
                z++;
                if(z > card.getRewardGoods().size()) break;
            }
        }
    }

    @Override
    public void visit(PlaugeCard card){
        //if(players == null) throw new NullPointerException("Null players list");
        //        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        //        else if(f == null) throw new NullPointerException("Null flight card board");
        for(Player p : players){
            controller.startPlauge(p);
        }
    }
}
