package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for double cannon
 * the methods for changing the orientation also change the firepower
 * @author Matteo Vignocchi
 */
public class DoubleCannon extends Tile {
    private double power;
    private boolean isOn;

    public DoubleCannon(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        power = 0;
        isOn = false;
    }
    /**
     * the method besides changing the direction it changes the firepower
     */
    @Override
    public void RotateRight(){
        int tmp ;
        tmp = corners[3];
        corners[3] = corners[2];
        corners[2] = corners[1];
        corners[1] = corners[0];
        corners[0] = tmp;
        if( corners[0] != 4) power = 0.5;
    }
    /**
     * the method besides changing the direction it changes the firepower
     */
    @Override
    public void RotateLeft(){
        int tmp ;
        tmp = corners[0];
        corners[0] = corners[1];
        corners[1] = corners[2];
        corners[2] = corners[3];
        corners[3] = tmp;
        if( corners[0] != 4) power = 0.5;
    }

    /**
     * changes the status of the cannon
     */
    public void Start(){
        isOn = true;;
    }
    public void TurnOf(){
        isOn = false;
    }
    /**
     * @return the actual firepower
     */
    public double getPower(boolean active) {

        if(active){
            return power+2;
        }
        else {
            return power;
        }
    }
    public boolean getStatus(){
        return isOn;
    }

    }


