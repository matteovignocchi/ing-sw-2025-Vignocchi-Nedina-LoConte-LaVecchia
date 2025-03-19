package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the tile of the single engine
 * the power is always 1
 * @author Matteo Vignocchi
 */
public class Engine extends Tile {
    private boolean isDouble;
    public Engine(int a,int b,int c,int d, boolean isDouble) {
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
