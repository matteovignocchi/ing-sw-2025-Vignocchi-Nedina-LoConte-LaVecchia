package it.polimi.ingsw.galaxytrucker;
import it.polimi.ingsw.galaxytrucker.Tile.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pile {
     private List<Tile> pile_of_tile = new ArrayList<>();

    /**
     * constructor that initialize all the tile and create the list pile_of_tile randomized with all the different tile
     * @author Oleg Nedina
     */
    public Pile() {

        Collections.shuffle(pile_of_tile);
     }

     /**
      * method for getting a tile from the pile
      * @param i the index of the list were the tile wanted stays
      * @throws InvalidIndex if the index is not present in the tile
      */
    public void TakeFromPile (int i) throws InvalidIndex{

         if(i<=0 || i>=pile_of_tile.size()){
            throw new InvalidIndex("Invalid index, chose another tile");
         }
         Tile tmp;
         tmp = pile_of_tile.get(i);
         takeFrom(tmp);
    }

    //qua manca la exception
    /**
     * this method change the flag isTaken of tile the player chose
     * @param t object given again to the pile
     */
    public void AddToPile (Tile t) {
         t.giveTile();
    }

    /**
     * this method change the flag isShown and isTaken of tile the player chose
     * @param t object taken from the pile
     */
    public void takeFrom ( Tile t) {
        t.takeTile();
        t.ShowTile();

    }
    /**
     * @return the list with all the different combination of tile
     */
    public List<Tile> getPile_of_tile() {
        return pile_of_tile;
    }
}



