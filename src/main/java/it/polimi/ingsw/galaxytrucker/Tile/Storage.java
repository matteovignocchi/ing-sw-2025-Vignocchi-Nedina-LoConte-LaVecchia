package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Good;
import java.util.List;

/**
 * interface used for all the type of storages
 * in this way, all the methods common to all types of storages are available
 * all the methods are override in the classes and explained
 */
public interface Storage {
    public void AddGood(Good good);
    public void RemoveGood(int i);
    public List<Good> getListOfGoods();
}
