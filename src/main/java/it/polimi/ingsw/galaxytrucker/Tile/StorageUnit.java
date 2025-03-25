package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Colour;
import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.InvalidIndex;
import it.polimi.ingsw.galaxytrucker.TooDangerous;

import java.util.ArrayList;
import java.util.List;

/**
 * class for the storage unit with 2 slots
 * the methods check if the slots are full, there is the "max" parameter for the slots
 * and check if the good can be put in the storage
 * @author Matteo Vignocchi
 */
public class StorageUnit extends Tile {
    private List<Colour> listOfGoods = new ArrayList<>();
    private int max;
    private boolean advanced;

    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public StorageUnit(int a, int b, int c, int d, int max, boolean advanced) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        this.max = max;
        this.advanced = advanced;
    }

    /**
     * the method adds only if there is space
     * @param good it's the good that they add, it can be all the type of Good
     * @throws FullGoodsList if the storage is full it says to the player
     * @throws TooDangerous if the good is dangerous
     */
    public void addGood(Colour good) throws FullGoodsList, TooDangerous {
        if (listOfGoods.size() == max) {
            throw new FullGoodsList("Storage is full, choose another one"); //ricordati di aggiungere gestione eccezione chiamata remove
        } else if ( good == Colour.RED && !advanced) {
            throw new TooDangerous("Too Dangerous! Do not even try!\nYou risk blowing up the ship");
        }
        listOfGoods.add(good);
    }
    /**
     * @return the list of goods that the storage contains
     */
    public List<Colour> getListOfGoods(){
        return listOfGoods;
    }
    /**
     * the method remove a good choose by the player from the storage
     * @param index it's the index of the cell from the player want to remove
     * @throws InvalidIndex if there is no good or there is empty
     */
    public void removeGood(int index) throws InvalidIndex {
        if (listOfGoods.get(index) == null || listOfGoods.isEmpty()) {
            throw new InvalidIndex("The cell of the storage doesn't contains a Good");
        }
        listOfGoods.remove(listOfGoods.get(index));
    }
    public int getListSize() {
        return listOfGoods.size();
    }
}