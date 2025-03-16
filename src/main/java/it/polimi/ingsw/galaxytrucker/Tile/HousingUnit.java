package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.BrownAlien;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
import it.polimi.ingsw.galaxytrucker.Token.PurpleAlien;

import java.util.ArrayList;
import java.util.List;

/**
 * class for the housing unit designate to host humans
 * max is the parameter for the slots
 * @author Matteo Vignocchi
 */

public class HousingUnit extends Tile implements Housing{

    private List<Humans> TotHumans = new ArrayList<>();
    final int max = 2;

    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public HousingUnit(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
    }

    /**
     * add to the tile one human till it reaches the max capacity
     * @param u new human to the house
     * @throws FullHousingList if the party is full
     */
    @Override
    public void AddHuman(Humans u) throws FullHousingList, IllegalArgumentException {
        if (TotHumans.size() == max) {
            throw new FullHousingList("HousingList is full");
        }else if (u instanceof PurpleAlien || u instanceof BrownAlien) {
            throw new IllegalArgumentException("Aliens can not stay in the human housing");
        }
        TotHumans.add(u);
    }

    /**
     * method for giving out a human
     * @param u human that will be kick out the party
     * @throws EmptyHousingList if the housing unit is empty
     */
    @Override
    public void RemoveHumans(Humans u) throws EmptyHousingList {
        if(TotHumans.isEmpty()) throw new EmptyHousingList("HousingList is empty");
        TotHumans.remove(u);
    }

    /**
     * @return the number of human in the housing unit
     */
    @Override
    public int ReturnLenght(){
        return TotHumans.size();
    }

    /**
     * maybe we need the list of human
     * @return the list of token
     */
    @Override
    public List<Humans> ReturnHumans(){
        return TotHumans;
    }

    @Override
    public boolean getStatus(){
        return false;
    }

}
