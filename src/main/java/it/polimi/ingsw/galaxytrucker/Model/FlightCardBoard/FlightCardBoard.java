package it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Class that describes the FlightCardBoard (for demo flight) and its methods
 * @author Gabriele La Vecchia
 * @author Francesco Lo Conte
 */

public class FlightCardBoard implements Serializable {
    public int spacesNumber;
    protected int bonusBestShip;
    protected int bonusFirstPosition;
    protected int bonusSecondPosition;
    protected int bonusThirdPosition;
    protected int bonusFourthPosition;
    protected int redGoodBonus;
    protected int yellowGoodBonus;
    protected int greenGoodBonus;
    protected int blueGoodBonus;
    protected int malusBrokenTile;
    public List<Player> orderedPlayersInFlight;
    protected Controller controller;

    public FlightCardBoard(Controller controller) {
        this.spacesNumber = 18;
        this.bonusFirstPosition = 4;
        this.bonusSecondPosition = 3;
        this.bonusThirdPosition = 2;
        this.bonusFourthPosition = 1;
        this.bonusBestShip = 2;
        this.redGoodBonus = 4;
        this.yellowGoodBonus = 3;
        this.greenGoodBonus = 2;
        this.blueGoodBonus = 1;
        this.malusBrokenTile = -1;
        this.orderedPlayersInFlight = new ArrayList<>();
        this.controller = controller;
    }

    /**
     * @return credits for a red good
     */
    public int getBonusRedCargo() {
        return redGoodBonus;
    }

    /**
     * @return credits for a yellow good
     */
    public int getBonusYellowCargo() {
        return yellowGoodBonus;
    }

    /**
     * @return credits for a green good
     */
    public int getBonusGreenCargo() {
        return greenGoodBonus;
    }

    /**
     * @return credits for a blue good
     */
    public int getBonusBlueCargo() {
        return blueGoodBonus;
    }

    /**
     * @return malus for a broken tile
     */
    public int getBrokenMalus(){
        return malusBrokenTile;
    }

    /**
     * @return credits for the best ship award
     */
    public int getBonusBestShip() {
        return bonusBestShip;
    }

    /**
     * @return reward credits for the first position
     */
    public int getBonusFirstPosition(){
        return bonusFirstPosition;
    }

    /**
     * @return reward credits for the second position
     */
    public int getBonusSecondPosition(){
        return bonusSecondPosition;
    }

    /**
     * @return reward credits for the third position
     */
    public int getBonusThirdPosition(){
        return bonusThirdPosition;
    }

    /**
     * @return reward credits for the fourth position
     */
    public int getBonusFourthPosition(){
        return bonusFourthPosition;
    }

    /**
     * @return in-flight ordered players' list
     */
    public List<Player> getOrderedPlayers(){ return new ArrayList<>(orderedPlayersInFlight); }

    /**
     * Returns the player's position in flight
     *
     * @param p player you want to know the position of
     * @return player's position
     */
    public int getPositionOfPlayer(Player p) {
        if (!orderedPlayersInFlight.contains(p)) {
            throw new IllegalArgumentException("Player not found in flight board");
        }
        return p.getPos();
    }

    /**
     * Adds a player to the flight List
     *
     * @param p player to be added
     * @throws IllegalArgumentException if p==null or p is already in list
     * @throws RuntimeException if there are already 4 players (the maximum) in list
     */
    public void addPlayer(Player p) {
        if(p == null) throw new IllegalArgumentException("Player cannot be null");
        if(orderedPlayersInFlight.contains(p)) throw new IllegalArgumentException("Player is already in flight");
        if(orderedPlayersInFlight.size() >= 4) throw new RuntimeException("Too many players in flight");

        orderedPlayersInFlight.add(p);
    }

    /**
     * Marks the given player as ready to start the flight by initializing their lap to 1, assigning their starting
     * position based on the current flight mode (demo or normal) and the number of players already in flight, and then
     * adds them to the flight ordered players' list
     *
     * @param p       the player to mark as ready and add to flight
     * @param isDemo  {@code true} for demo games,
     *                {@code false} for level 2 games
     * @throws IllegalArgumentException if the player is already in flight
     * @throws RuntimeException         if there are already 4 players in flight
     */
    public void setPlayerReadyToFly(Player p, boolean isDemo) {
        if(orderedPlayersInFlight.contains(p)) throw new IllegalArgumentException("Player is already in flight");
        int size = orderedPlayersInFlight.size();
        if(size >= 4) throw new RuntimeException ("Too many players in flight");

        p.setLap(1);
        if(isDemo){
            switch(size){
                case 0:
                    p.setPos(5);
                    break;
                case 1:
                    p.setPos(3);
                    break;
                case 2:
                    p.setPos(2);
                    break;
                case 3:
                    p.setPos(1);
                    break;
            }
        } else {
            switch(size){
                case 0:
                    p.setPos(7);
                    break;
                case 1:
                    p.setPos(4);
                    break;
                case 2:
                    p.setPos(2);
                    break;
                case 3:
                    p.setPos(1);
                    break;
            }
        }
        orderedPlayersInFlight.add(p);
    }

    /**
     * The following method reorders the list of players on the flight board so that the player with the highest lap
     * count (and, within the same lap, the furthest position) comes first, down to the one with the lowest.
     *
     * @throws RuntimeException if two players have the same lap and position (violation of game's rule)
     */
    public void orderPlayersInFlightList(){
        orderedPlayersInFlight.sort(new Comparator<Player>(){
            @Override
            public int compare(Player p1, Player p2){
                int p1_lap = p1.getLap();
                int p2_lap = p2.getLap();
                int p1_pos = p1.getPos();
                int p2_pos = p2.getPos();
                if(p1_lap == p2_lap && p1_pos == p2_pos)
                    throw new RuntimeException("Two players cannot have the same lap and the same position");
                if(p1_lap > p2_lap || (p1_lap == p2_lap && p1_pos > p2_pos)) return -1;
                return 1;
            }
        });
    }

    /**
     * The following method normalizes a proposed board position into the valid range [1, position_number],
     * updating the player's lap count if a wrap‑around occurs.
     *
     * @param p player
     * @param temp current final position with no wrap-around
     * @return final position with wrap-around
     */
    public int checkOverLap(Player p, int temp) {
        if (temp > spacesNumber) {
            temp = temp - spacesNumber;
            p.setLap(p.getLap()+1);
        } else if (temp < 1){
            temp = temp + spacesNumber;
            p.setLap(p.getLap()-1);
        }
        return temp;
    }

    /**
     * Moves the specified player's rocket forward or backward by a given number of spaces on the circular
     * flight board, automatically handling wrap‑around and overtaking rules.
     *
     * @param x number of free spaces the rocket must move
     * @param p player
     * @throws IllegalArgumentException if p == null
     * @throws InvalidPlayerException if p is not in players in flight's list
     */

    public void moveRocket(int x, Player p) {
        if (p == null) throw new IllegalArgumentException("Player null");
        if (orderedPlayersInFlight.isEmpty()) return;
        if (!orderedPlayersInFlight.contains(p)) throw new InvalidPlayerException("Player is not in flight");
        int p_final = p.getPos() + x;
        int p_start;
        boolean rocketsFound = true;

        while (rocketsFound) {
            rocketsFound = false;
            p_start= p.getPos();
            p_final = this.checkOverLap(p, p_final);

            int count = 0;
            for (Player other : orderedPlayersInFlight) {
                if (other.equals(p)) continue;
                int other_pos = other.getPos();
                if (x >= 0){
                    if (p_final > p_start) {
                        if(p_start < other_pos && other_pos <= p_final){
                            count++;
                            rocketsFound = true;
                        }
                    } else {
                        if((p_start < other_pos && other_pos <= spacesNumber) || (0 < other_pos && other_pos <= p_final)){
                            count++;
                            rocketsFound = true;
                        }
                    }
                } else {
                    if(p_final < p_start) {
                        if(p_final <= other_pos && other_pos < p_start){
                            count--;
                            rocketsFound = true;
                        }
                    } else {
                        if((0 < other_pos && other_pos < p_start) || (p_final <= other_pos && other_pos <= spacesNumber)){
                            count--;
                            rocketsFound = true;
                        }
                    }
                }
            }
            p.setPos(p_final);
            p_final = p_final + count;
        }
    }

    /**
     * Eliminates the overlapped players, by checking for each of them if there's
     * another one with a higher number of laps and a higher position on the board
     */

    public void checkIfPlayerOverlapped() {
        for(Player p : orderedPlayersInFlight) {
            if(p.isEliminated()) continue;
            for(Player other : orderedPlayersInFlight) {
                if(other.getId() == p.getId() || other.isEliminated()) continue;
                if((other.getLap() == p.getLap()+1 &&  other.getPos() > p.getPos()) || other.getLap() > p.getLap()+1 ){
                    p.setEliminated();
                    break;
                }
            }
        }
    }

    /**
     * Checks each non-eliminated player still in flight and eliminates
     * any who have no humans left aboard. Eliminated players are notified of their elimination.
     *
     * @throws BusinessLogicException if notifying a player fails
     */
    public void checkIfPlayerNoHumansLeft() throws BusinessLogicException {
        for(Player p : orderedPlayersInFlight) {
            if(p.isEliminated()) continue;
            if(p.getTotalHuman()==0){
                p.setEliminated();
                String nick = controller.getNickByPlayer(p);
                controller.inform("SERVER: You have lost all your crewmates", nick);
            }
        }
    }

    /**
     * Removes all players marked as eliminated from the in‐flight ordered players' list
     * and returns the list of those removed.
     *
     * @return the list of players eliminated
     */
    public List<Player> eliminatePlayers(){
        List<Player> eliminated = new ArrayList<>();
        Iterator<Player> iterator = orderedPlayersInFlight.iterator();
        while(iterator.hasNext()) {
            Player p = iterator.next();
            if(p.isEliminated()){
                iterator.remove();
                eliminated.add(p);
            }
        }

        return eliminated;
    }

}