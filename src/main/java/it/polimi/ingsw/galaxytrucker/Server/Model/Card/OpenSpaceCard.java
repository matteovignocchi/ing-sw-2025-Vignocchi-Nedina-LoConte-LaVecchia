package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

/**
 * This class describes the behavior of the "open space" adventure card.
 * @author Gabriele La Vecchia & Francesco Lo Conte
 */

public class OpenSpaceCard implements Card {

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    @Override
    public String toString() {return "OpenSpaceCard";}
}
