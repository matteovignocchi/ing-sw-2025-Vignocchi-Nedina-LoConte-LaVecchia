package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Humans;

import java.util.ArrayList;
import java.util.List;

public class HousingUnit extends Tile {

    private List<Humans> TotHumans = new ArrayList<>();
    int max;


    public HousingUnit(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        max=2;
    }

    /**
     * add to the tile one uman till it reach the max capacity
     * @param u new human to the house
     * @throws FullHousingList if the party is full
     */
    public void AddHumans(Humans u) throws FullHousingList {
        if (this.ReturnLenght() == max) throw new  FullHousingList("HousingList is full");
            TotHumans.add(u);
    }

    /**
     * method for giving out a human
     * @param u human that will be kick out the party
     * @throws EmptyHousingList
     */
    public void RemoveHumans(Humans u) throws EmptyHousingList {
        if(this.ReturnLenght()==0) throw new EmptyHousingList("HousingList is empty");
        TotHumans.remove(u);
    }

    /**
     *
     * @return the number of human in the housingUnit
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

}
