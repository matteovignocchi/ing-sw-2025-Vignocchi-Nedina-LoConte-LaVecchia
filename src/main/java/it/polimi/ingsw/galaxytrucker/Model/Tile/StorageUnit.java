package it.polimi.ingsw.galaxytrucker.Model.Tile;

import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.InvalidIndex;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a storage unit tile on the player's ship.
 * This tile holds a list of goods (Colour), up to a maximum capacity.
 * It supports basic and advanced variants: only advanced units can store red (dangerous) goods.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class StorageUnit extends Tile implements Serializable {
    private List<Colour> listOfGoods = new ArrayList<>();
    private int max;
    private boolean advanced;

    /**
     * the values are standard, and they are given when the game starts from the application
     * Creates a new storage unit tile with given connectors, capacity and type.
     * @param a connector for the north side
     * @param b connector for the east side
     * @param c connector for the south side
     * @param d connector for the west side
     * @param max maximum number of goods the unit can store
     * @param advanced true if the storage can accept dangerous (red) goods
     * @param id the unique identifier of the tile
     */
    public StorageUnit(int a, int b, int c, int d, int max, boolean advanced, int id) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        this.max = max;
        this.advanced = advanced;
        idTile = id;
    }

    /**
     * Adds a good to the storage unit if there is available space and it is allowed.
     * Red (dangerous) goods can only be added to advanced storage units.
     * @param good the good to be added
     * @throws FullGoodsList if the storage has reached its maximum capacity
     * @throws TooDangerous if the good is red and the unit is not advanced
     */
    public void addGood(Colour good) throws FullGoodsList, TooDangerous {
        if (listOfGoods.size() == max) {
            throw new FullGoodsList("Storage is full, choose another one");
        } else if ( good == Colour.RED && !advanced) {
            throw new TooDangerous("Too Dangerous! Do not even try!\nYou risk blowing up the ship");
        }
        listOfGoods.add(good);
    }

    /**
     * Returns the list of goods currently stored in the unit.
     * @return a list of goods (Colour)
     */
    public List<Colour> getListOfGoods(){
        return listOfGoods;
    }

    /**
     * Removes and returns the good at the specified index.
     * @param index the index of the good to remove
     * @return the removed good
     * @throws InvalidIndex if the index is invalid or the list is empty
     */
    public Colour removeGood(int index) throws InvalidIndex {
        if (listOfGoods.get(index) == null || listOfGoods.isEmpty()) {
            throw new InvalidIndex("The cell of the storage doesn't contains a Good");
        }
        Colour good = listOfGoods.get(index);
        listOfGoods.remove(good);
        return good;
    }

    /**
     * Returns the number of goods currently stored in the unit.
     * @return the number of goods
     */
    public int getListSize() {
        return listOfGoods.size();
    }

    /**
     * Checks whether the storage unit has reached its maximum capacity.
     * @return true if full, false otherwise
     */
    public boolean isFull() {
        if(listOfGoods.size() >= max) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Returns whether the storage unit is advanced (able to hold red goods).
     * @return true if advanced, false otherwise
     */
    public boolean isAdvanced() {
        return advanced;
    }

    /**
     * Returns the maximum number of goods the storage unit can hold.
     * @return the capacity of the storage
     */
    public int getMax() {
        return max;
    }
}