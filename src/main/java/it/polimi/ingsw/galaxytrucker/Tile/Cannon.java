package it.polimi.ingsw.galaxytrucker.Tile;


/**
 * class for single cannon
 * the methods for changing the orientation also change the firepower
 * @author Matteo Vignocchi
 */
public class Cannon extends Tile {
    private boolean isDouble;

    public Cannon(int a, int b, int c, int d, boolean isDouble) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        this.isDouble = isDouble;
    }

    public boolean isDouble() {
        return isDouble;
    }

}
