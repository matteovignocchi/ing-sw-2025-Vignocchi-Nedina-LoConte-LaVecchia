package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.InvalidIndex;
import it.polimi.ingsw.galaxytrucker.Token.DangerousGood;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import it.polimi.ingsw.galaxytrucker.TooDangerous;

import java.util.ArrayList;
import java.util.List;

/**
 * class for the storage unit with 3 slots
 * the methods check if the slots are full, there is the "max" parameter for the slots
 * and check if the good can be put in the storage
 * @author Matteo Vignocchi
 */
public class LargeStorageUnit extends Tile implements Storage {
    private List<Good> listOfGoods = new ArrayList<>();
    final int max = 3;

    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public LargeStorageUnit(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }
    /**
     * the method adds only if there is space
     * @param g it's the good that they add, it can be all the type of Good
     * @throws FullGoodsList if the storage is full it says to the player
     * @throws TooDangerous if the good is dangerous
     */
    @Override
    public void AddGood(Good g) throws FullGoodsList, TooDangerous {
        if (listOfGoods.size() == max) {
            throw new FullGoodsList("Storage is full, choose another one"); //ricordati di aggiungere gestione eccezione chiamata remove
        } else if ( g instanceof DangerousGood) {
            throw new TooDangerous("Too Dangerous! Do not even try!\nYou risk blowing up the ship");
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
