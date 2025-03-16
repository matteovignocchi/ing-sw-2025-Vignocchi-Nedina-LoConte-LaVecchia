package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for double cannon
 * the methods for changing the orientation also change the firepower
 * @author Matteo Vignocchi
 */
public class DoubleCannon extends Tile {
    private double power;

    public DoubleCannon(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        power = 0;
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
    }
    public double getPower(boolean active) {

        if(active){
            if(this.controlCorners(0) != 5){
            return power+1;
            }else {return power+2;}
        }
        else {
            return power;
        }
    }


    }


