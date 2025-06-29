package it.polimi.ingsw.galaxytrucker.Model.Card;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implements the CardVisitor interface to apply the effects of adventure cards
 * during the flight phase of the game.
 * This visitor uses the game Controller and FlightCardBoard to execute
 * card-specific logic—such as drawing rewards, applying penalties,
 * updating player state, and advancing game phases.
 * It also holds a reference to the list of players involved in the current flight.
 *
 * @author Gabriele La Vecchia
 * @author Francesco Lo Conte
 * @see CardVisitor
 */
public class CardEffectVisitor implements CardVisitor, Serializable {
    private final Controller controller;
    private final FlightCardBoard f;
    private List<Player> players;

    /**
     * CardEffectVisitor constructs that apply adventure card effects using the specified Controller.
     * This constructor retrieves the current FlightCardBoard and the list of players in flight order from the controller.
     *
     * @param controller the controller through which the visitor interacts with the game state;
     * @throws NullPointerException if the controller, flight board, player list, or an individual player is null.
     */

    public CardEffectVisitor(Controller controller) {

        if (controller == null) throw new NullPointerException("controller is null");
        FlightCardBoard f = controller.getFlightCardBoard();
        List<Player> players = f.getOrderedPlayers();

        if (players.isEmpty()) throw new NullPointerException("players is null");
        for (Player p : players) if (p == null) throw new NullPointerException("player is null");

        this.controller = controller;
        this.f = f;
        this.players = players;
    }

    /**
     * Applies the “Open Space” card effect: for each player in turn order, moves their rocket forward by an amount
     * equal to their engine power. If a player has an engine power equals to 0, he is eliminated.
     *
     * @param card the OpenSpaceCard to apply
     * @throws InvalidCardException if card is null
     * @throws BusinessLogicException  in case of any error during effect execution
     */

    @Override
    public void visit(OpenSpaceCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            controller.inform("SERVER: Checking your engine power...", nick);
            controller.printPlayerDashboard(controller.getViewCheck(nick), p, nick);
            int x = controller.getPowerEngineForCard(p);

            if (x == 0) {
                p.setEliminated();
                controller.inform("SERVER: Your engine power is 0", nick);
            } else {
                String msg = "SERVER: Your engine power is " + x + ". You move forward by those spaces.";
                controller.inform(msg, nick);
                f.moveRocket(x, p);
            }

            controller.changeMapPosition();
            controller.updatePositionForEveryBody();
        }
    }

    /**
     * Applies the “Stardust” card effect: in reverse turn order,
     * each player moves backward by the number of exposed connectors on their ship.
     *
     * @param card the StardustCard to apply
     * @throws InvalidCardException if card is null
     * @throws BusinessLogicException in case of any error during effect execution
     */

    @Override
    public void visit(StardustCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        for (int i = players.size() - 1; i >= 0; i--) {
            Player p = players.get(i);
            String nick = controller.getNickByPlayer(p);

            int x = p.countExposedConnectors();

            String msg = "SERVER: You have " + x + " exposed connectors. You move back by those spaces";
            controller.inform(msg, nick);
            f.moveRocket(-x, p);

            controller.changeMapPosition();
            controller.updatePositionForEveryBody();
        }
    }

    /**
     * Applies the “Slavers” card effect:
     * In order from the leader, for each player:
     * - If a player has less firepower than the Slavers' value, they lose the specified number of crewmates.
     * - If a player has the same firepower as the Slavers' value, nothing happens
     * - If a player has higher firepower than the Slavers' value, they can decide whether to get the specified number
     * of credits and lose the specified number of days flight or not. In any case, Slavers are defeated and don't
     * attack the following players in the list.
     *
     * @param card the SlaversCard to apply
     * @throws InvalidCardException if card is null
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(SlaversCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");
        boolean exit = false;

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        double slavers_fire_power = card.getFirePower();
        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            controller.inform("SERVER: Checking your fire power...", nick);
            double player_fire_power = controller.getFirePowerForCard(p);

            if (player_fire_power > slavers_fire_power) {
                int credits = card.getCredits();
                int days = card.getDays();
                String string = String.format("SERVER: You have defeated the slavers. Do you want to redeem %d credits and lose %d flight days?",
                        credits, days);

                if (controller.askPlayerDecision(string, p)) {
                    f.moveRocket(-days, p);
                    p.addCredits(credits);
                }

                controller.changeMapPosition();
                controller.updatePositionForEveryBody();
                controller.broadcastInform("SERVER: Slavers defeated by " + nick + "!");
                exit = true;

            } else if (player_fire_power < slavers_fire_power) {
                int lostCrewmates = card.getNumCrewmates();
                String msg = "SERVER: You have been defeated by Slavers. You'll lose " + lostCrewmates + " crewmates";
                controller.inform(msg, nick);

                controller.removeCrewmates(p, card.getNumCrewmates());

                msg = "SERVER: Checking other players...";
                controller.inform(msg, nick);

            } else {
                String msg = "SERVER: You have the same firepower as the slavers. Draw, nothing happens\n" +
                        "SERVER: Checking other players";
                controller.inform(msg, nick);
            }

            if (exit) break;
        }
    }

    /**
     * Applies the "First Warzone" card effect:
     * - It finds the player with the fewest crewmates and moves his rocket backward by the card's days
     * - It finds the player with the lowest firepower and removes the specified crewmates from him
     * - It finds the player with the lowest engine power, and he will be hit by the specified shots. The player
     * rolls two dices per shot, to determinate the raw/column which will be hit.
     * If two or more player tie for the lowest interested attribute, the first one in order on the board will be
     * chosen.
     *
     * @param card card
     * @throws CardEffectException if card is null
     * @throws BusinessLogicException in case of any logic error during effect execution
     */

    @Override
    public void visit(FirstWarzoneCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        if (players.size() == 1) {
            String nick = controller.getNickByPlayer(players.getFirst());
            controller.inform("SERVER: You are flying alone. warzone card effect not activated ", nick);
            return;
        }


        int idx_crew = 0;
        int days = card.getDays();
        controller.broadcastInform("\nSERVER: Checking the player with the least number of crewmates...\n");

        controller.broadcastInform("SERVER: "+controller.getNickByPlayer(players.get(idx_crew))+" has "+
                controller.getNumCrew(players.get(idx_crew))+" crewmates");
        for (int i = 1; i < players.size(); i++) {
            Player p = players.get(i);
            int numcrew = controller.getNumCrew(p);
            controller.broadcastInform("SERVER: "+controller.getNickByPlayer(p)+" has "+numcrew+" crewmates");
            if (numcrew < controller.getNumCrew(players.get(idx_crew)))
                idx_crew = i;
        }

        String nickCrew = controller.getNickByPlayer(players.get(idx_crew));
        controller.broadcastInform("SERVER: "+nickCrew+" is the player with the least number of crewmates on board!" +
                "He loses "+days+" flight days");
        f.moveRocket(-days, players.get(idx_crew));


        int idx_enginepower = 0;
        int numCrew = card.getNumCrewmates();
        controller.broadcastInform("\nSERVER: Checking the player with the lowest engine power...\n");

        List<Integer> enginePowers = new ArrayList<>();
        for(Player p : players) {
            int x = controller.getPowerEngineForCard(p);
            enginePowers.add(x);
            String nick = controller.getNickByPlayer(p);
            controller.broadcastInform("SERVER: "+nick+" has an engine power of "+x);
        }
        for (int i = 1; i < enginePowers.size(); i++) {
            if(enginePowers.get(i) < enginePowers.get(idx_enginepower))
                idx_enginepower = i;
        }

        String nickEngine = controller.getNickByPlayer(players.get(idx_enginepower));
        controller.broadcastInform("SERVER: "+nickEngine+" is the player with the lowest engine power! He loses "+numCrew+" crewmates");
        controller.removeCrewmates(players.get(idx_enginepower), numCrew);


        int idx_firepower = 0;
        controller.broadcastInform("\nSERVER: Checking the player with the lowest fire power...\n");

        List<Double> firePowers = new ArrayList<>();
        for(Player p : players) {
            double x = controller.getFirePowerForCard(p);
            firePowers.add(x);
            String nick = controller.getNickByPlayer(p);
            controller.broadcastInform("SERVER: "+nick+" has a fire power of "+x);
        }
        for (int i = 1; i < firePowers.size(); i++) {
            if(firePowers.get(i) < firePowers.get(idx_firepower))
                idx_firepower = i;
        }

        Player p = players.get(idx_firepower);
        String nickFire = controller.getNickByPlayer(p);
        controller.broadcastInform("SERVER: "+nickFire+" is the player with the lowest fire power! He will be hit by cannon fire");
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < shots_directions.size(); i++) {
            if(p.isEliminated()) continue;
            int res = p.throwDice() + p.throwDice();
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }

    }

    /**
     * Applies the "Second Warzone" card effect:
     * - It finds the player with the lowest firepower and moves his rocket backward by the card's days
     * - It finds the player with the lowest engine power, and removes from him the specified number of goods
     * - It finds the player with the fewest number of crewmates, and e will be hit by the specified shots. The player
     * rolls two dices per shot, to determinate the raw/column which will be hit.
     * If two or more player tie for the lowest interested attribute, the first one in order on the board will be
     * chosen.
     *
     * @param card card
     * @throws InvalidCardException if card is null
     * @throws BusinessLogicException in case of any logic error during effect execution
     */

    @Override
    public void visit(SecondWarzoneCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        if (players.size() == 1) {
            String nick = controller.getNickByPlayer(players.getFirst());
            controller.inform("SERVER: You are flying alone. warzone card effect not activated ", nick);
            return;
        }


        int idx_firepower = 0;
        int days = card.getDays();
        controller.broadcastInform("\nSERVER: Checking the player with the lowest fire power...\n");

        List<Double> firePowers = new ArrayList<>();
        for(Player p : players) {
            double x = controller.getFirePowerForCard(p);
            firePowers.add(x);
            String nick = controller.getNickByPlayer(p);
            controller.broadcastInform("SERVER: "+nick+" has a fire power of "+x);
        }
        for (int i = 1; i < firePowers.size(); i++) {
            if(firePowers.get(i) < firePowers.get(idx_firepower))
                idx_firepower = i;
        }

        String nickFire = controller.getNickByPlayer(players.get(idx_firepower));
        controller.broadcastInform("SERVER: "+nickFire+" is the player with the lowest fire power!" +
                " He loses "+days+" flight days");
        f.moveRocket(-days, players.get(idx_firepower));


        int idx_engine = 0;
        int numGoods = card.getNumGoods();
        controller.broadcastInform("\nSERVER: Checking the player with the lowest engine power...\n");

        List<Integer> enginePowers = new ArrayList<>();
        for(Player p : players) {
            int x = controller.getPowerEngineForCard(p);
            enginePowers.add(x);
            String nick = controller.getNickByPlayer(p);
            controller.broadcastInform("SERVER: "+nick+" has an engine power of "+x);
        }
        for (int i = 1; i < enginePowers.size(); i++) {
            if(enginePowers.get(i) < enginePowers.get(idx_engine))
                idx_engine = i;
        }

        String nickEngine = controller.getNickByPlayer(players.get(idx_engine));
        controller.broadcastInform("SERVER: "+nickEngine+" is the player with the lowest engine power! He loses "+numGoods+" goods");
        controller.removeGoods(players.get(idx_engine), numGoods);



        int idx_crew = 0;

        controller.broadcastInform("\nSERVER: Checking the player with the least number of crewmates...\n");
        controller.broadcastInform("SERVER: "+controller.getNickByPlayer(players.get(idx_crew))+" has "+
                controller.getNumCrew(players.get(idx_crew))+" crewmates");
        for (int i = 1; i < players.size(); i++) {
            Player p = players.get(i);
            int numcrew = controller.getNumCrew(p);
            controller.broadcastInform("SERVER: "+controller.getNickByPlayer(p)+" has "+numcrew+" crewmates");
            if (numcrew < controller.getNumCrew(players.get(idx_crew)))
                idx_crew = i;
        }

        String nickCrew = controller.getNickByPlayer(players.get(idx_crew));
        controller.broadcastInform("SERVER: "+nickCrew+" is the player with the least number of crewmates on board!" +
                " He will be hit by cannon fire!");

        Player p = players.get(idx_crew);
        List<Integer> shots_directions = card.getShotsDirections();
        List<Boolean> shots_size = card.getShotsSize();
        for (int i = 0; i < card.getShotsDirections().size(); i++) {
            if(p.isEliminated()) continue;
            int res = p.throwDice() + p.throwDice();
            controller.defenceFromCannon(shots_directions.get(i), shots_size.get(i), res, p);
        }
    }

    /**
     * Applies the “Smugglers” card effect:
     * In order from the leader, for each player:
     * - If a player has less firepower than the Smuggler's value, he loses the specified number of goods
     * - If a player has the same firepower as Smuggler's value, nothing happens
     * - If a player has higher firepower than the Smuggler's value, he can decide whether to get the specified goods as
     * reward and lose the specified flight days or not.
     * In any case, Smugglers are defeated and don't attack any other player
     *
     * @param card the SmugglersCard to apply
     * @throws InvalidCardException if card is null
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(SmugglersCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");
        boolean exit = false;
        double smugglers_fire_power = card.getFirePower();

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            controller.inform("\nSERVER: Checking your fire power...", nick);
            double player_fire_power = controller.getFirePowerForCard(p);

            if (player_fire_power > smugglers_fire_power) {
                int days = card.getDays();
                String string = String.format("SERVER: You defeated the smugglers!\nDo you want to redeem the card's reward goods" +
                                " and lose %d flight days?", days);

                if (controller.askPlayerDecision(string, p)) {
                    f.moveRocket(-days, p);
                    controller.manageGoods(p, card.getRewardGoods());
                    controller.changeMapPosition();
                    controller.updatePositionForEveryBody();
                }

                controller.broadcastInform("\nSERVER: Smugglers defeated by " + nick + "!");
                exit = true;
            } else if (player_fire_power < smugglers_fire_power) {

                String msg = "SERVER: You have been defeated by Smugglers. You'll lose the indicated goods";
                controller.inform(msg, nick);
                controller.removeGoods(p, card.getNumRemovedGoods());
                controller.inform("SERVER: Checking other players", nick);

            } else {
                controller.inform("SERVER: You have the same firepower as the slavers. Draw, nothing happens\n" +
                        "SERVER: Checking other players", nick);
            }

            if (exit) break;
        }
    }

    /**
     * Applies the "Abandoned Ship" card effect:
     * In order, from the leader onwards, each player is asked if he is willing to lose "num_crewmates"
     * crewmates and "days" flight days. The first player to accept "destroys" the card, exiting the cycle
     * and no other player can take advantage of this.
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(AbandonedShipCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");
        boolean exit = false;

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            int credits = card.getCredits();
            int days = card.getDays();
            int num_crewmates = card.getNumCrewmates();
            String string = "SERVER: Do you want to redeem " + credits + " credits and lose " + num_crewmates + " crewmates and "
                    + days + " flight days?";

            if (controller.askPlayerDecision(string, p)) {
                f.moveRocket(-days, p);
                p.addCredits(credits);
                controller.removeCrewmates(p, num_crewmates);

                controller.changeMapPosition();
                controller.updatePositionForEveryBody();
                exit = true;
            } else {
                if (players.indexOf(p) != players.size() - 1) controller.inform("SERVER: Asking other players...", nick);
            }

            if (exit) break;
        }
    }

    /**
     * Applies the "Abandoned Station" card effect:
     * In order, from the leader onwards, check that the player in question has a number of crewmates
     * greater than or equal to the number of crewmates indicated on the card. If this is true,
     * then ask the player if he is willing to lose "days" flight days in exchange for some goods.
     * The first player to accept "destroys" the card, preventing all other players from using it.
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(AbandonedStationCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");
        boolean exit = false;

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);
            int num_crewmates = card.getNumCrewmates();
            int days = card.getDays();

            if (controller.getNumCrew(p) >= num_crewmates) {
                String string = "SERVER: Do you want to redeem the card's reward goods and lose "+days+" flight days?";

                if (controller.askPlayerDecision(string, p)) {
                    f.moveRocket(-days, p);

                    controller.manageGoods(p, card.getStationGoods());

                    controller.changeMapPosition();
                    controller.updatePositionForEveryBody();
                    exit = true;
                } else {
                    if (players.indexOf(p) != players.size() - 1) controller.inform("SERVER: Asking other players...", nick);
                }
            } else {
                controller.inform("SERVER: You don't have enough crewmates to be able to redeem the card's reward", nick);
                if (players.indexOf(p) != players.size() - 1) controller.inform("SERVER: Asking other players...", nick);
            }

            if (exit) break;
        }
    }

    /**
     * Applies the "Meteorites Rain" card effect:
     * For each meteorite, the leader rolls the dice twice, determining for each player the column
     * or row on which the meteorite will be thrown. Meteorites are managed with two lists,
     * one indicating the size and one indicating the direction.
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(MeteoritesRainCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        Player p;
        try{
            p = players.stream().filter(pl -> pl.isConnected() && !pl.isEliminated()).toList().getFirst();
        } catch (NoSuchElementException e){
            return;
        }

        List<Player> meteoritesPlayers = players.stream().filter(pl -> !pl.isEliminated()).toList();

        for (int i = 0; i < card.getMeteorites_directions().size(); i++) {
            int res = p.throwDice() + p.throwDice();

            controller.defenceFromMeteorite(card.getMeteorites_directions().get(i), card.getMeteorites_size().get(i), res, meteoritesPlayers, i+1);
        }

    }

    /**
     * Applies the "Pirates" card effect:
     * For each player, in order of route, verify that the firepower of each of them is greater than
     * that indicated on the card. If this is not true, the player in question is added to a list of losers,
     * otherwise he is asked if he is willing to lose "days" days of flight in exchange for "credits" credits.
     * Regardless of whether he accepts or not, the card is "destroyed" and no other player can become a loser
     * or can exploit it.
     * All losers will receive cannon shots that are managed with two lists, one indicating the direction
     * and one indicating the size.
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(PiratesCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");

        List<Player> losers = new ArrayList<>();
        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            controller.inform("SERVER: Checking your fire power...", nick);
            double playerFirePower = controller.getFirePowerForCard(p);

            if (playerFirePower > card.getFirePower()) {
                int days = card.getDays();
                int credits = card.getCredits();
                String string = "SERVER: You defeated the Pirates!\n SERVER: Do you want to redeem "+credits+
                        " and lose "+days+ " flight days?";

                if (controller.askPlayerDecision(string, p)) {
                    f.moveRocket(-days, p);
                    p.addCredits(credits);
                    controller.changeMapPosition();
                    controller.updatePositionForEveryBody();
                }

                controller.broadcastInform("SERVER: Pirates defeated by "+nick+"!");
                break;

            } else if (playerFirePower < card.getFirePower()) {
                losers.add(p);
                controller.inform("SERVER: You have been defeated by slavers. You'll be hit by cannon fire", nick);
            } else {
                controller.inform("SERVER: You have the same fire power as Pirates. Draw, nothing happens", nick);
            }
        }

        if (!losers.isEmpty()) {
            Player first;
            try{
                first = losers.stream().filter(Player::isConnected).toList().getFirst();
            } catch(NoSuchElementException e){
                return;
            }

            for (int i = 0; i < card.getShots_directions().size(); i++){
                int res = first.throwDice() + first.throwDice();

                for (Player p : losers){
                    if(p.isEliminated()) continue;
                    controller.defenceFromCannon(card.getShots_directions().get(i), card.getShots_size().get(i), res, p);
                }
            }
        }
    }

    /**
     * Applies the "Planets" card effect:
     * For each player, in order of route, it is asked if he wants to land on a planet receiving goods and
     * losing "days" days of flight. The first player can only land on the first planet, the second can land
     * on the first planet (if the leader has not landed there previously) or on the second planet
     * (if the leader has landed on the first planet) and so on.
     * If there are more players than available planets, there is a risk that the last player
     * will not even have the possibility to decide to land.
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(PlanetsCard card) throws BusinessLogicException {
        if (card == null) throw new InvalidCardException("Card cannot be null");
        int z = 0;
        int days = card.getDays();
        List<Player> landedPlayers = new ArrayList<>();

        controller.broadcastInformExcept("\nSERVER: Waiting for your turn...", players.getFirst());

        for (Player p : players) {
            String nick = controller.getNickByPlayer(p);

            String string = "SERVER: Do you want to land on "+(z+1)+"° planet, take its goods and lose "+days+" flight days?";

            if (controller.askPlayerDecision(string, p)) {
                landedPlayers.add(p);
                controller.broadcastInform("SERVER: "+nick+" landed on planet "+(z+1));
                controller.manageGoods(p, card.getRewardGoods().get(z));
                z++;

                if (z >= card.getRewardGoods().size()) break;
            }

            if(players.indexOf(p) < players.size()-1) controller.inform("SERVER: Asking other players...", nick);
        }

        if (!landedPlayers.isEmpty()) {
            for(int i = landedPlayers.size()-1; i >= 0; i--){
                Player p = landedPlayers.get(i);
                String nick = controller.getNickByPlayer(p);
                f.moveRocket(-days, p);
                controller.changeMapPosition();
            }
            controller.updatePositionForEveryBody();
        }
    }

    /**
     * Applies the "Plague" card effect:
     * For each player, for each couple of housing units directly connected, a crewmate is removed from both of them
     *
     * @param card: card object on which the method is activated.
     * @throws InvalidCardException if card is null.
     * @throws BusinessLogicException  in case of any logic error during effect execution
     */

    @Override
    public void visit(PlagueCard card) throws BusinessLogicException {
        for (Player p : players) {
            controller.startPlague(p);
        }
    }

}
