package it.polimi.ingsw.galaxytrucker.Model.Tile;
import java.io.Serializable;

/**
 * class for MultiJoint
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class MultiJoint extends Tile implements Serializable {
    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public MultiJoint(int a,int b,int c,int d, int id) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        idTile = id;
    }

}
