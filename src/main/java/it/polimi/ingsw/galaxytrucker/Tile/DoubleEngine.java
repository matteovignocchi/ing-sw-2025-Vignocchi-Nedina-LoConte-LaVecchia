package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the tile of the double engine
 * it has the flag for turning on the engine when needed
 * @author Matteo Vignocchi
 *
 */

public class DoubleEngine extends Tile {
    private boolean isOn;
    private int power;

    public DoubleEngine(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        isOn= false;
        power = 0;
    }

    /**
     * it changes the status of the engine and increase the power
     */
    public void Start(){
        isOn = true;
        power = 2;
    }
    public void TurnOf(){
        isOn = false;
        power = 0;
    }
    public int getPower() {
        if(isOn){
            return power;
        }
        else {
            return 0;
        }
    }

    /**
     * @return return the status of the engine
     */
    public boolean getStatus(){
        return isOn;
    }
}
