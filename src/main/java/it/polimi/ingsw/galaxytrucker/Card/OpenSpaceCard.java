package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;

import javax.smartcardio.CardException;
import java.util.List;

/**
 * This class describes the behavior of the "open space" adventure card.
 * @author Gabriele La Vecchia & Francesco Lo Conte
 */

public class OpenSpaceCard implements Card {

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }
}
