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
         Tile tmp ;
         tmp = new MultiJoint(3,3,0,1);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,1,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,2,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint( 2,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3,1,2,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3, 2, 0 ,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3,2,2,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(0,2,1,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(0,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(1,1,1,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(2,1,0,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(2,2,2,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(3,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new LargeAdvStorageUnit(0,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new LargeAdvStorageUnit(0,2,0,1);
         pile_of_tile.add(tmp);

         tmp = new LargeAdvStorageUnit(0,0,0,2);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,3,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(1,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(1,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(1,1,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(2,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(2,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(2,3,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(3,2,6,0);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,3,6,1);
         pile_of_tile.add(tmp);

         tmp = new Engine(2,1,6,1);
         pile_of_tile.add(tmp);

         tmp = new Engine(3,0,6,1);
         pile_of_tile.add(tmp);

         tmp = new Engine(3,2,6,1);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,1,6,2);
         pile_of_tile.add(tmp);

         tmp = new Engine(1,2,6,2);
         pile_of_tile.add(tmp);

         tmp = new Engine(2,0,6,2);
         pile_of_tile.add(tmp);

         tmp = new Engine(3,1,6,2);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,0,6,3);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,0,6,3);
         pile_of_tile.add(tmp);

         tmp = new Engine(0,2,6,3);
         pile_of_tile.add(tmp);

         tmp = new Engine(1,0,6,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(1,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(1,3,6,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(2,0,6,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(3,1,6,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(1,1,6,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(2,2,6,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(3,0,6,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(0,3,6,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(2,0,6,3);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,1,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,1,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,2,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,2,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,1,0,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,1,2,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,1,3,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,0,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,1,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,3,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,3,1,0);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,2,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,3,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,1,1,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,3,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,3,0,1);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,0,2);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,1,2);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,1,3,2);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,2,2);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,0,2,3);
         pile_of_tile.add(tmp);

         tmp = new Cannon(4,2,0,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,1,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,2,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,1, 3,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,3,0,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,3,2,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,1,2,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,1,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,2,0,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,3,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,2,1,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEngine(5,0,1,3);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(1,1,0,1);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(2,1,0,1);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(0,0,1,3);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(0,2,0,3);
         pile_of_tile.add(tmp);

         tmp = new BrownAlienUnit(1,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(1,2,0,2);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(2,2,0,2);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(0,0,2,3);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(0,1,0,3);
         pile_of_tile.add(tmp);

         tmp = new PurpleAlienUnit(2,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new Shield(0,1,3,1);
         pile_of_tile.add(tmp);

         tmp = new Shield(1,0,1,1);
         pile_of_tile.add(tmp);

         tmp = new Shield(2,1,2,1);
         pile_of_tile.add(tmp);

         tmp = new Shield(0,0,3,2);
         pile_of_tile.add(tmp);

         tmp = new Shield(0,2,2,2);
         pile_of_tile.add(tmp);

         tmp = new Shield(1,2,1,2);
         pile_of_tile.add(tmp);

         tmp = new Shield(0,0,1,3);
         pile_of_tile.add(tmp);

         tmp = new Shield(0,2,2,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,1,2,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,2,0,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,2,1,0);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(2,1,2,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,1,1,1);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,2,2,2);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(0,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new DoubleEnergyCell(3,0,2,3);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(1,0,2,0);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(1,1,2,0);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(2,0,0,0);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(2,1,0,0);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(2,2,1,0);
         pile_of_tile.add(tmp);

         tmp = new TripleEnergyCell(0,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(2,1,3,0);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(2,1,3,1);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(2,3,0,1);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(0,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(0,1,0,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(0,2,0,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(2,1,2,3);
         pile_of_tile.add(tmp);

         tmp = new StorageUnit(3,0,0,3);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(0,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(1,0,1,0);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(0,0,0,2);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(2,0,2,0);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(1,0,2,1);
         pile_of_tile.add(tmp);

         tmp = new AdvStorageUnit(2,0,1,2);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(0,0,3,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(1,2,1,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(1,2,2,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,0,0,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,1,2,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,1,0,1);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(0,1,3,2);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(1,2,0,2);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,1,2,2);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(3,0,0,2);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(0,1,0,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(0,1,1,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(0,2,0,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(1,0,1,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(1,0,2,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,0,2,3);
         pile_of_tile.add(tmp);

         tmp = new HousingUnit(2,2,0,3);
         pile_of_tile.add(tmp);

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



