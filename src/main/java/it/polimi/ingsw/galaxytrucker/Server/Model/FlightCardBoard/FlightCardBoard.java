package it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard;

import it.polimi.ingsw.galaxytrucker.Server.Model.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class that describes the FlightCardBoard (for test flight ) and its methods
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class FlightCardBoard {
    protected int position_number;
    protected int bonus_MostBeautifulShip;
    protected int bonus_first_position;
    protected int bonus_second_position;
    protected int bonus_third_position;
    protected int bonus_fourth_position;
    protected List<Player> orderedPlayersInFlight;

    /**
     * Sets bonuses for placements, for the best ship and the number of spaces on the board for
     * a test flight
     */
    public FlightCardBoard() {
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

    public void addPlayer(Player p) {
        if(p == null) throw new IllegalArgumentException("Player cannot be null");
        if(orderedPlayersInFlight.contains(p)) throw new IllegalArgumentException("Player is already in flight");
        if(orderedPlayersInFlight.size() >= 4) throw new RuntimeException("Too many players in flight");

        orderedPlayersInFlight.add(p);
    }

    /**
     * riordina la lista in ordine dal nuovo leader in poi
     */
    public void orderPlayersInFlightList(){
        orderedPlayersInFlight.sort(new Comparator<Player>(){
            @Override
            public int compare(Player p1, Player p2){
                int p1_lap = p1.getLap();
                int p2_lap = p2.getLap();
                int p1_pos = p1.getPos();
                int p2_pos = p2.getPos();
                if(p1_lap > p2_lap || (p1_lap == p2_lap && p1_pos > p2_pos)) return -1;
                return 1;
            }
        });
    }

    /**
     * The method sees if the player has a  position on the board beyond the last space.
     * If so, it calculates the correct position (the board has a cyclic structure) and
     * increases the laps' number of the player.
     *
     * @param p player
     * @param temp current final position on the board
     * @return final position with no overlap
     */
    public int checkOverLap(Player p, int temp) {
        if (temp > position_number) {
            temp = temp - position_number;
            p.setLap(p.getLap()+1);
        }
        return temp;
    }

    /**
     * The method moves the specified player's rocket on the board by x spaces (x can be positive
     * or negative).
     * The method initially calculates the current final position "temp" of the rocket on the board.
     * Inside the while loop, it calls the OverLap method (see above), and checks whether there
     * are any more other's rockets in the interval between its initial and final positions. If so, it
     * increases a counter for each one. Finally, it saves the final position in Player object.
     * If it has encountered any rockets, it repeats the while loop, until it encounters no more ones
     * and the final position is valid.
     *
     * @param x number of free spaces the rocket must move
     * @param p player
     * @param players list of players in game
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     * @throws InvalidPlayerException exception thrown if (see conditions below)
     */
    //aggiornare la lista dei giocatori in ordine in base agli spostamenti fatti (?)
    /* VECCHIA VERSIONE
    public void moveRocket(int x, Player p, List<Player> players) throws InvalidPlayerException {
        if(p==null) throw new IllegalArgumentException("Player null");
        else if(players==null) throw new IllegalArgumentException("Players list null");
        else if(players.isEmpty()) throw new IllegalArgumentException("Players list empty");
        else if(!players.contains(p)) throw new InvalidPlayerException("Player is not in game");

        int temp = p.getPos() + x;
        int pIndex = players.indexOf(p);
        boolean rocketsFound = true;

        while(rocketsFound) {
            rocketsFound = false;
            temp = this.checkOverLap(p, temp);

            int count = 0;
            for(Player other : players) {
                if(other.getId() != pIndex && !other.isEliminated() && p.getPos() < other.getPos() && other.getPos() <= temp){
                    count++;
                    rocketsFound = true;
                }
            }
            p.setPos(temp);
            temp = temp + count;
        }
    }
     */
    public void moveRocket(int x, Player p) throws InvalidPlayerException {
        if(p==null) throw new IllegalArgumentException("Player null");
        if(orderedPlayersInFlight.isEmpty()) return;
        if(!orderedPlayersInFlight.contains(p)) throw new InvalidPlayerException("Player is not in flight");
        //Gestire con un try catch al chiamante ?

        int temp = p.getPos() + x;
        int pIndex = orderedPlayersInFlight.indexOf(p);
        boolean rocketsFound = true;

        while(rocketsFound) {
            rocketsFound = false;
            temp = this.checkOverLap(p, temp);

            int count = 0;
            for(Player other : orderedPlayersInFlight) {
                if(other.getId() != pIndex &&  p.getPos() < other.getPos() && other.getPos() <= temp){
                    count++;
                    rocketsFound = true;
                }
            }
            p.setPos(temp);
            temp = temp + count;
        }
    }




    /**
     * The method eliminates the overlapped players, by checking for each of them if there's
     * another one with a higher number of laps and a higher position on the board, and setting
     * their "eliminated" attribute to "true".
     *
     * @param players list of players in game
     * @throws IllegalArgumentException exception thrown if (see conditions below)
     */

    /* VECCHIA VERSIONE
    public void eliminateOverlappedPlayers(List<Player> players) throws IllegalArgumentException{
        if(players==null) throw new IllegalArgumentException("Players list null");
        else if (players.isEmpty()) throw new IllegalArgumentException("Players list empty");

        for(Player p : players) {
            if(p.isEliminated()) continue;
            boolean overlapped = false;
            for(Player other : players){
                if(other.isEliminated() || other.getId() == p.getId()) continue;
                if(other.getLap() > p.getLap() && other.getPos() > p.getPos()){
                    overlapped = true;
                    break;
                }
            }
            if(overlapped) p.setEliminated();
        }
    }
     */

    //Se da problemi in testing, modificare con metodo con iteratore
    public void eliminateOverlappedPlayers() {
        for(Player p : orderedPlayersInFlight) {
            boolean overlapped = false;
            for(Player other : orderedPlayersInFlight) {
                if(other.getId() == p.getId()) continue;
                if(other.getLap() > p.getLap() && other.getPos() > p.getPos()){
                    overlapped = true;
                    break;
                }
            }
            if(overlapped) orderedPlayersInFlight.remove(p);
        }
    }

}