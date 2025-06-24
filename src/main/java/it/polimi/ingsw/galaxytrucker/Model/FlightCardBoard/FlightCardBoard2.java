package it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Player;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that extends FlightCardBoard, used for lvl. 2 flights
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */
public class FlightCardBoard2 extends FlightCardBoard implements Serializable {

    public FlightCardBoard2(Controller controller) {
        super(controller);
        this.spacesNumber = 24;
        this.bonusFirstPosition = 8;
        this.bonusSecondPosition = 6;
        this.bonusThirdPosition = 4;
        this.bonusFourthPosition = 2;
        this.bonusBestShip = 4;
    }
}

