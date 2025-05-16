package it.polimi.ingsw.galaxytrucker.Model.Tile;

import java.io.Serializable;

/**
 * class for empty space, where there is not a tile
 * @author Matteo Vignocchi
 */
public class EmptySpace extends Tile implements Serializable {
    public EmptySpace() {
        corners[0]=0;
        corners[3]=0;
        corners[2]=0;
        corners[1]=0;
        idTile = 157;

    }
}
