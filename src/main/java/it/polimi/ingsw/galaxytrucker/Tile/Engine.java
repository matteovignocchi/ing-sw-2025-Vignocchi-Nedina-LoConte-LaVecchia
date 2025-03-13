package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the tile of the single engine
 * the power is always 1
 * @author Matteo Vignocchi
 */

public class Engine extends Tile {
    final int power = 1;
    public Engine(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }
}
