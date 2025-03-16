package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Good;
import java.util.ArrayList;
import java.util.List;

public interface Storage {
    public void AddGood(Good good);
    public void RemoveGood(int i);
    public List<Good> getListOfGoods();
}
