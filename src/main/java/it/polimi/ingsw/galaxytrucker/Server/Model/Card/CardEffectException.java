package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

public abstract class CardEffectException extends Exception{
  public CardEffectException(String message) {
       super(message);
   }
}

//E' una classe astratta perchè rappresenta una generica categoria di eccezioni legate agli effetti delle
//carte e non viene mai istanziata direttamente
