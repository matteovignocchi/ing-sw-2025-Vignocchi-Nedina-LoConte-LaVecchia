package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the tile of the double engine
 * it has the flag for turning on the engine when needed
 * @author Matteo Vignocchi
 *
 */


public class DoubleEngine extends Tile {
    private boolean isOn;

    public DoubleEngine(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        isOn= false;

    }

    /**
     * it changes the status of the engine
     */
    public void Start(){
        isOn = true;
    }

    /**
     * @return return the status of the engine
     */
    public boolean getStatus(){
        return isOn;
    }
}
