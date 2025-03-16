package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Humans;

import java.util.List;

public interface Housing {
    public void AddHuman(Humans human);
    public int RemoveHumans(Humans human);
    public int ReturnLenght();
    public List<Humans> ReturnHumans();
    public boolean getStatus();

}
