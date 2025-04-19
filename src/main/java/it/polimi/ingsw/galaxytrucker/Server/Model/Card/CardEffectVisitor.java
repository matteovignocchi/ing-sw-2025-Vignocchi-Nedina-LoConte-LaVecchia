package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import it.polimi.ingsw.galaxytrucker.Server.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Server.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Server.Model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardEffectVisitor implements CardVisitor {
    private final Controller controller;
    private final FlightCardBoard f;
    private final List<Player> players;

    //GESTIRE ECCEZIONI SU PLAYERS, PLAYER SINGOLO E F != NULL (NO EMPTY) A MONTE, AL MOMENTO DELLA CREAZIONE
    //O QUI NEL COSTRUTTORE ?????
    //VEDERE SE ECCEZIONI CHECKED O UNCHECKED IN STO CASO
    public CardEffectVisitor(Controller controller) throws CardEffectException {

        //NullPointerException (?) o eccezione custom (?)
        if (controller == null) throw new NullPointerException("controller is null");
        FlightCardBoard f = controller.getFlightCardBoard();
        List<Player> players = f.getOrderedPlayers();
        //Questi controlli necessari qui (?)
        if (f == null) throw new NullPointerException("flightCardBoard is null");
        if (players == null || players.isEmpty()) throw new NullPointerException("players is null");
        for (Player p : players) if (p == null) throw new NullPointerException("player is null");

        this.controller = controller;
        this.f = f;
        this.players = players;
    }

    @Override
    public void visit(OpenSpaceCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

        for (Player p : players) {
            int x = controller.getPowerEngine(p);
            f.moveRocket(x, p);
        }
    }

    @Override
    public void visit (StardustCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

        for (int i = players.size() - 1; i >= 0; i--) {
            Player p = players.get(i);
            int x = p.countExposedConnectors();
            f.moveRocket(-x, p);
        }
    }

    @Override
    public void visit(SlaversCard card) throws CardEffectException {
        if(card == null) throw new InvalidCardException("Card cannot be null");

        double slavers_fire_power = card.getFirePower();
        for (Player p : players) {
            double player_fire_power = controller.getFirePower(p);
            if(player_fire_power > slavers_fire_power) {
                int credits = card.getCredits();
                int days = card.getDays();
                String string = String.format("Do you want to redeem %d credits and lose %d flight days?",
                        credits, days);
                if(controller.askPlayerDecision(string, p)){
                    f.moveRocket(-days, p);
                    p.addCredits(credits);
                }
                break;
            } else if (player_fire_power < slavers_fire_power) {
                controller.removeCrewmate(p, card.getNumCrewmates());
            }
        }
    }

    @Override
    public void visit(FirstWarzoneCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

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

        //f.moveRocket(-card.getDays(), players.get(i_less_crewmates), players);
        f.moveRocket(-card.getDays(), players.get(i_less_crewmates));
        controller.removeCrewmate(players.get(i_less_firepower), card.getNumCrewmates());
        Player p = players.get(i_less_powerengine);
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < card.getShotsDirections().size(); i++) {
            int res = p.throwDice() + p.throwDice();
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }
    }

    @Override
    public void visit(SecondWarzoneCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

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
        //f.moveRocket(-card.getDays(), players.get(i_less_firepower), players);
        f.moveRocket(-card.getDays(), players.get(i_less_firepower));
        controller.removeGoods(players.get(i_less_powerengine), card.getNumGoods());
        Player p = players.get(i_less_crewmates);
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < card.getShotsDirections().size(); i++) {
            int res = p.throwDice() + p.throwDice();
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }
    }

    @Override
    public void visit(SmugglersCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

        double smugglers_fire_power = card.getFirePower();
        for(Player p : players) {
            double player_fire_power = controller.getFirePower(p);
            if(player_fire_power > smugglers_fire_power){
                //Prima variante di askPlayerDecision (gli passo le info della carta)
                int days = card.getDays();
                List<Colour> reward_goods = card.getRewardGoods();
                String reward_goods_string = reward_goods.stream().map(Colour::name).collect(Collectors.joining(", "));
                String string = String.format("Do you want to redeem %s goods and lose %d flight days?",
                        reward_goods_string, days);
                if(controller.askPlayerDecision(string, p)){
                    //f.moveRocket(-days, p, players);
                    f.moveRocket(-days, p);
                    controller.addGoods(p, card.getRewardGoods());
                }
                break;
            } else if (player_fire_power < smugglers_fire_power) {
                controller.removeGoods(p, card.getNumRemovedGoods());
            }
        }
    }

    @Override
    public void visit(AbandonedShipCard card) throws CardEffectException{
        if(card == null) throw new InvalidCardException("Card cannot be null");

        for(Player p : players) {
            //Seconda versione di askPlayerDecision (non gli passo le info della carta, le ha già)
            String string = "Do you want to redeem the card's reward and lose the indicated flight days?";
            if (controller.askPlayerDecision(string, p)) {
                int days = card.getDays();
                f.moveRocket(-days, p);
                int credits = card.getCredits();
                p.addCredits(credits);
                int num_crewmates = card.getNumCrewmates();
                controller.removeCrewmate(p, num_crewmates);
                break;
            }
        }
    }

    @Override
    public void visit(AbandonedStationCard card) throws CardEffectException {
        if(card == null) throw new InvalidCardException("Card cannot be null");

        for(Player p: players) {
            int num_crewmates = card.getNumCrewmates();
            if(controller.getNumCrew(p)>=num_crewmates){
                String string = "Do you want to redeem the card's reward and lose the indicated flight days?";
                if(controller.askPlayerDecision(string, p)){
                    int days = card.getDays();
                    //f.moveRocket(-days, p, players);
                    f.moveRocket(-days, p);
                    controller.addGoods(p, card.getStationGoods());
                }
                break;
            }
        }
    }

    @Override
    public void visit(MeteoritesRainCard card) throws CardEffectException {
        if(card == null) throw new InvalidCardException("Card cannot be null");

        for (int i = 0; i < card.getMeteorites_directions().size(); i++) {
            int res = players.getFirst().throwDice() + players.getFirst().throwDice();
            for(Player p : players){
                controller.defenceFromMeteorite(card.getMeteorites_directions().get(i), card.getMeteorites_size().get(i), res);
            }
        }
    }

    @Override
    public void visit(PiratesCard card) throws CardEffectException {
        if(card == null) throw new InvalidCardException("Card cannot be null");

        List<Player> losers = new ArrayList<>();
        for(Player p : players) {
            if(controller.getFirePower(p) > card.getFirePower()){
                String string = "Do you want to redeem the card's reward and lose the indicated flight days?";
                if(controller.askPlayerDecision(string, p)){
                    int days = card.getDays();
                    f.moveRocket(-days, p);
                    int credits = card.getCredits();
                    p.addCredits(credits);
                }
                break;
            } else if (controller.getFirePower(p) < card.getFirePower())
                losers.add(p);
        }
        if(!losers.isEmpty()){
            int res = losers.getFirst().throwDice() + losers.getFirst().throwDice();
            for(Player p : losers){
                for(int i = 0; i < card.getShots_directions().size(); i++){
                    controller.defenceFromCannon(card.getShots_directions().get(i), card.getShots_size().get(i), res, p);
                }
            }
        }
    }

    @Override
    public void visit(PlanetsCard card) throws CardEffectException {
        if(card == null) throw new InvalidCardException("Card cannot be null");

        int z = 0;
        for(Player p : players){
            String string ="Do you want to redeem the card's reward and lose the indicated flight days?";
            if(controller.askPlayerDecision(string, p)){
                int days = card.getDays();
                f.moveRocket(-days, p);
                controller.addGoods(p, card.getRewardGoods().get(z));
                z++;
                if(z >= card.getRewardGoods().size()) break;
            }
        }
    }

    @Override
    public void visit(PlaugeCard card) throws CardEffectException {

        for(Player p : players){
            controller.startPlauge(p);
        }
    }
}
