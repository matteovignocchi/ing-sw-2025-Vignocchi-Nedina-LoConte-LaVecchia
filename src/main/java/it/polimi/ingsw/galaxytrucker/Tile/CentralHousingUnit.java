package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.Token.BrownAlien;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
import it.polimi.ingsw.galaxytrucker.Token.PurpleAlien;

import java.util.ArrayList;
import java.util.List;

/**
 * class the central housing unit
 * it is unique and has properties different from the others
 * similar to the housing unit, they can not add aliens
 * when they remove all the humans on the unit, the player becomes eliminated
 * @author Matteo Vignocchi
 */
public class CentralHousingUnit extends Tile {
    private boolean isDestroyed;
    private List<Humans> TotHumans = new ArrayList<>();
    final int max = 2;

    /**
     * standard values
     */
    public CentralHousingUnit() {
        isDestroyed = false;
        corners[0]=3;
        corners[1]=3;
        corners[2]=3;
        corners[3]=3;

    }

    //ricordarsi di chiedere ad oleg del fatto che questo metodo possiamo fare del tipo che non riceve niente in input e
    // si riempe in automatico di umani
    public void AddHuman(Humans u) throws FullHousingList, IllegalArgumentException {
        if (TotHumans.size() == max) {
            throw new FullHousingList("HousingList is full");
        }else if (u instanceof PurpleAlien || u instanceof BrownAlien) {
            throw new IllegalArgumentException("Aliens can not stay in the human housing");
        }
        TotHumans.add(u);
    }

    /**
     * this method remove a human token from the unit, when the player tries to remove the last human
     * the player loses the game
     * @param u
     * @return it always returns 1 because there are only humans
     */
    public int RemoveHumans(Humans u){
        if(TotHumans.size() == 1) this.destroy();
        TotHumans.remove(u);
        return 1;
    }

    /**
     * @return if the central unit is destroyed
     */
    public boolean checkStatus() {
        return isDestroyed;
    }

    /**
     * changes the status of the unit to destroyed
     */
    public void destroy() {
        isDestroyed = true;
    }

}
