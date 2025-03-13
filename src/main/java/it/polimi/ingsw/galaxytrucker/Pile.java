package it.polimi.ingsw.galaxytrucker;
import it.polimi.ingsw.galaxytrucker.Tile.Cannon;
import it.polimi.ingsw.galaxytrucker.Tile.MultiJoint;
import it.polimi.ingsw.galaxytrucker.Tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class Pile {
     public List<Tile> pile_of_tile = new ArrayList<>();
     public Pile() {

         Tile tmp ;
         tmp = new MultiJoint(3,3,0,1);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,1,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(1,3,2,3,);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint( 2,3,0,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3,1,2,3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3, 2, 0 , 3);
         pile_of_tile.add(tmp);

         tmp = new MultiJoint(3,2,2,3,);
         pile_of_tile.add(tmp);


















     }





























































































































































     public Tile TakeFromPile (int i) throws InvalidIndex{

         if(i<=0 || i>=pile_of_tile.size()){
            throw new InvalidIndex("Invalid index, chose another tile");
         }
         Tile tmp;
         tmp = pile_of_tile.get(i);
         pile_of_tile.remove(i);
         return tmp;
     }
    //qua manca la exception
    public void AddToPile ( Tile t) {
         pile_of_tile.add(t);
    }

}



