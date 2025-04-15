package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

/**
 * "Stardust" adventure card description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class StardustCard implements Card {
    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }
}
