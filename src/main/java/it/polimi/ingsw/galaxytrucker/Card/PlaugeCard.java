package it.polimi.ingsw.galaxytrucker.Card;
/**
 * This class describes the behavior of the "Plauge Card" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class PlaugeCard implements Card {

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }
}
