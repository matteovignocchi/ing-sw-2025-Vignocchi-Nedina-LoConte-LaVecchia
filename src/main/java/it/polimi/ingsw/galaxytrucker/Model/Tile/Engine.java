package it.polimi.ingsw.galaxytrucker.Model.Tile;
import java.io.Serializable;

/**
 * class for double and single engine
 * the flag isDouble give the information if is double or single
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class Engine extends Tile implements Serializable {
    private boolean isDouble;
    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     * @param isDouble
     */
    public Engine(int a,int b,int c,int d, boolean isDouble, int id) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        this.isDouble = isDouble;
        idTile = id;
    }

    /**
     * @return true the engine is double
     */
    public boolean isDouble() {
        return isDouble;
    }
}
