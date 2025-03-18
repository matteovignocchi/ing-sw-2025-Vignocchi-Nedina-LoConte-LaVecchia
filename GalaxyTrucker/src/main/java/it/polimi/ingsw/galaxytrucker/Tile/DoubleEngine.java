package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the tile of the double engine
 * it has the flag for turning on the engine when needed
 * @author Matteo Vignocchi
 *
 */

public class DoubleEngine extends Tile {

    public DoubleEngine(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }

    public int getPower(boolean activate) {
        if(activate){
            return 2;
        }
        else {
            return 0;
        }
    }

}
