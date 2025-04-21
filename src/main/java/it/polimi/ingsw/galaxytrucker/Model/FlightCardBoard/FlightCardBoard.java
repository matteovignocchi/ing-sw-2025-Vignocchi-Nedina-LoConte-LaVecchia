package it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard;

import it.polimi.ingsw.galaxytrucker.Model.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Class that describes the FlightCardBoard (for demo flight) and its methods
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class FlightCardBoard {
    public int position_number;
    protected int bonus_MostBeautifulShip;
    protected int bonus_first_position;
    protected int bonus_second_position;
    protected int bonus_third_position;
    protected int bonus_fourth_position;
    protected List<Player> orderedPlayersInFlight;

    public FlightCardBoard() {
        //si inizia a contare da 1 le posizioni
        this.position_number = 18;
        this.bonus_first_position = 4;
        this.bonus_second_position = 3;
        this.bonus_third_position = 2;
        this.bonus_fourth_position = 1;
        this.bonus_MostBeautifulShip = 2;
        this.orderedPlayersInFlight = new ArrayList<Player>();
    }

    public int getBonusRedCargo() {
        return 4;
    }

    public int getBonusYellowCargo() {
        return 3;
    }

    public int getBonusGreenCargo() {
        return 2;
    }

    public int getBonusBlueCargo() {
        return 1;
    }

    public int getDamageMalus(){
        return -1;
    }

    public int getBonus_MostBeautifulShip() {
        return bonus_MostBeautifulShip;
    }

    public int getBonus_first_position(){
        return bonus_first_position;
    }

    public int getBonus_second_position(){
        return bonus_second_position;
    }

    public int getBonus_third_position(){
        return bonus_third_position;
    }

    public int getBonus_fourth_position(){
        return bonus_fourth_position;
    }

    public List<Player> getOrderedPlayers(){ return new ArrayList<>(orderedPlayersInFlight); }

    /**
     * The following method adds a player to the flight List, not in order.
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
        if (temp > position_number) {
            temp = temp - position_number;
            p.setLap(p.getLap()+1);
        } else if (temp < 1){
            temp = temp + position_number;
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
                        if((p_start < other_pos && other_pos <= position_number) || (0 < other_pos && other_pos <= p_final)){
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
                        if((0 < other_pos && other_pos < p_start) || (p_final <= other_pos && other_pos <= position_number)){
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
     * The following method eliminates the overlapped players, by checking for each of them if there's
     * another one with a higher number of laps and a higher position on the board
     */

    public void eliminateOverlappedPlayers() {
        Iterator<Player> iterator = orderedPlayersInFlight.iterator();
        while(iterator.hasNext()) {
            Player p = iterator.next();
            boolean overlapped = false;
            for(Player other : orderedPlayersInFlight) {
                if(other.getId() == p.getId()) continue;
                if(other.getLap() > p.getLap() && other.getPos() > p.getPos()){
                    overlapped = true;
                    break;
                }
            }
            if(overlapped) iterator.remove();
        }
    }

}