package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.Token.BrownAlien;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
import it.polimi.ingsw.galaxytrucker.Token.PurpleAlien;

import java.util.ArrayList;
import java.util.List;

/**
 *  class for the housing unit designate to host brown aliens or humans
 *  max is the parameter for the slots, it changes when an alien is present
 *  @author Matteo Vignocchi
 */
public class BrownAlienUnit extends Tile {


    private List<Humans> TotHumans = new ArrayList<>();
    int max;
    boolean isPresent;

    /**
     * the values are standard, and they are given when the game starts from the application
     * it initialized the tile with the max slots of two
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public BrownAlienUnit(int a, int b, int c, int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        max = 2;
        isPresent = false;
    }

    /**
     * the method add to the housing unit a human or a brown alien
     * it changes the parameter max if is an alien with 1, or it maintains the parameter 2 if is a human
     * @param u the human or the alien we want to add
     * @throws FullHousingList if the housing unit is full
     * @throws IllegalArgumentException if the human given is a purple alien
     */
    public void addHuman(Humans u) throws FullHousingList, IllegalArgumentException {
        if(TotHumans.size() == max){
            throw new FullGoodsList("The housing is full");
        } else if (u instanceof BrownAlien) {
            TotHumans.add(u);
            max = 1;
            isPresent = true;
        } else if (u instanceof PurpleAlien) {
            throw new IllegalArgumentException("Purple aliens can not be place here");
        }
        TotHumans.add(u);
    }

    /**
     * method used for removing humans or alien present in the unit
     * it changes the max slots of the unit if we remove the brown alien
     * @param u human or alien we want to remove
     * @throws EmptyHousingList if the unit is already empty
     */
    public void RemoveHumans(Humans u) throws EmptyHousingList {
        if(TotHumans.isEmpty()) {
            throw new EmptyHousingList("HousingList is empty");
        }else if(u instanceof BrownAlien){
            TotHumans.remove(u);
            max = 2;
        }
        TotHumans.remove(u);
    }

    /**
     * @return the number of human in the housing unit
     */
    public int ReturnLenght(){
        return TotHumans.size();
    }

    /**
     * maybe we need the list of human
     * @return the list of token
     */
    public List<Humans> ReturnHumans(){
        return TotHumans;
    }

    /**
     * @return if an alien is present
     */
    public boolean getStatus(){
        return isPresent;
    }




}
