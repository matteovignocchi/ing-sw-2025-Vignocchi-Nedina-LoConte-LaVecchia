package it.polimi.ingsw.galaxytrucker.Server.Model.FlightCardBoard;

import it.polimi.ingsw.galaxytrucker.Server.Model.Player;

import java.util.ArrayList;

/**
 * Class that extends FlightCardBoard, used for lvl. 2 Flights
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */
public class FlightCardBoard2 extends FlightCardBoard {

    /**
     * Sets bonuses for placements, for the best ship and the number of spaces on the board for
     * a lvl.2 flight
     */
    public FlightCardBoard2() {
        this.position_number = 24;
        this.bonus_first_position = 8;
        this.bonus_second_position = 6;
        this.bonus_third_position = 4;
        this.bonus_fourth_position = 2;
        this.bonus_MostBeautifulShip = 4;
        this.orderedPlayersInFlight = new ArrayList<Player>();
    }
}

