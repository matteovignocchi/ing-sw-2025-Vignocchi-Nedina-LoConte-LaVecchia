package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

/**
 * Card interface
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

public interface Card {
    void accept (CardVisitor visitor) throws CardEffectException;
}
