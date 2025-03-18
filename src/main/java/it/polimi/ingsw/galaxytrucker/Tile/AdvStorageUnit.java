package it.polimi.ingsw.galaxytrucker.Tile;


import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.InvalidIndex;

import it.polimi.ingsw.galaxytrucker.Token.Good;

/**
 * class for advance storage unit with one slot
 * the methods check if the slots are full, there is the "max" parameter for the slots
 * @author Matteo Vignocchi
 */
public class AdvStorageUnit extends Tile implements Storage{
    private List<Good> listOfGoods = new ArrayList<>();
    final int max = 1;

    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public AdvStorageUnit(int a, int b, int c, int d) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;

    }

    /**
     * the method adds a good only if there is space
     * @param g it's the good that they add, it can be all the type of Good
     * @throws FullGoodsList if the storage is full it says to the player
     */

    @Override
    public void AddGood(Good g) throws FullGoodsList {
        if (listOfGoods.size() == max) {
            throw new FullGoodsList("Storage is full, choose another one"); //ricordati di aggiungere gestione eccezione
        }
        listOfGoods.add(g);
    }

    /**
     * the method remove a good choose by the player from the storage
     * @param index it's the index of the cell from the player want to remove
     * @throws InvalidIndex if there is no good or there is empty
     */
    @Override
    public void RemoveGood(int index) throws InvalidIndex {
        if (listOfGoods.get(index) == null || listOfGoods.isEmpty()) {
            throw new InvalidIndex("The cell of the storage doesn't contains a Good");
        }
            listOfGoods.remove(listOfGoods.get(index));
    }

    /**
     * @return the list of goods that the storage contains
     */
    @Override
    public List<Good> getListOfGoods(){
        return listOfGoods;
    }
}