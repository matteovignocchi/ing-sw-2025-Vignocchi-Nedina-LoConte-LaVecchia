package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for double cannon
 * the methods for changing the orientation also change the firepower
 * @author Matteo Vignocchi
 */
public class DoubleCannon extends Tile {

    public DoubleCannon(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }

    public double getPower(boolean active) {

        if(active){
            if(this.controlCorners(0) != 5){
            return 1;
            }else {return 2;}
        }
        else {
            return 0;
        }
    }


    }


