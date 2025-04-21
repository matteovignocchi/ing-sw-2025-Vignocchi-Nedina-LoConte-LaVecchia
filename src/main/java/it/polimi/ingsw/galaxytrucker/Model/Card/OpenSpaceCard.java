package it.polimi.ingsw.galaxytrucker.Model.Card;

/**
 * This class handles the OpenSpaceCard, which is deserialized via Jackson.
 * It is parsed through a visitor pattern.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */
public class OpenSpaceCard implements Card {

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }
}
