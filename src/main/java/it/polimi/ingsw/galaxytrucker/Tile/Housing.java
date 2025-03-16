package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Humans;

import java.util.List;

/**
 * interface used for all the type of housing
 * in this way, all the methods common to all types of housing are available
 * all the methods are override in the classes and explained
 */
public interface Housing {
    public void AddHuman(Humans human);
    public int RemoveHumans(Humans human);
    public int ReturnLenght();
    public List<Humans> ReturnHumans();
    public boolean getStatus();

}
