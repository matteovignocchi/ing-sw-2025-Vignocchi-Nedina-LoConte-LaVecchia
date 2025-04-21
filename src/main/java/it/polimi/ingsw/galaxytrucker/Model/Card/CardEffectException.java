package it.polimi.ingsw.galaxytrucker.Model.Card;

/**
 *Custom exception that handles anomalous behaviors in cards.
 * It is extended by other exceptions and returns a message describing the type of error that led to its generation.
 */

public abstract class CardEffectException extends Exception{
  public CardEffectException(String message) {
       super(message);
   }
}
