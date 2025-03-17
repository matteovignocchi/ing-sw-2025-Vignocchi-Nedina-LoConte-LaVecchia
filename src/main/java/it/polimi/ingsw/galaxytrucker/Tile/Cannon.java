package it.polimi.ingsw.galaxytrucker.Tile;


/**
 * class for single cannon
 * the methods for changing the orientation also change the firepower
 * @author Matteo Vignocchi
 */
public class Cannon extends Tile {

    public Cannon(int a, int b, int c, int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }

    /**
     * @return the actual firepower
     */
    public double getPower() {

        if(this.controlCorners(0)!= 4){
            return 0.5;

        }else{return 1;}
    }
}
