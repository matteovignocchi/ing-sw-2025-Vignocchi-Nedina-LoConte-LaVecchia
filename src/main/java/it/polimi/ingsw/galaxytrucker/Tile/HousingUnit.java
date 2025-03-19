package it.polimi.ingsw.galaxytrucker.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * class for the housing unit designate to host humans
 * max is the parameter for the slots
 * @author Matteo Vignocchi
 */

public class HousingUnit extends Tile{
    private List<Human> listOfToken = new ArrayList<>();
    private int max;
    private Human isAlien;
    private boolean isConnected;


    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public HousingUnit(int a,int b,int c,int d, Human isAlien) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        this.isAlien=isAlien;
    }

    /**
     * add to the tile one human till it reaches the max capacity
     * @param token new human or alien to the house
     * @throws FullHousingList if the party is full
     */
    public void addHuman(Human token) throws FullHousingList, IllegalArgumentException {
        if (listOfToken.size() == max) {
            throw new FullHousingList("HousingList is full");
        }else if (isAlien == Human.HUMAN && token != Human.HUMAN){
            throw new IllegalArgumentException("Aliens can not stay in the human housing");
        } else if (isAlien == Human.BROWN_ALIEN && !isConnected && token != Human.BROWN_ALIEN) {
            throw new IllegalArgumentException("Wrong place!");
        }else if (isAlien == Human.PURPLE_ALIEN && !isConnected && token != Human.PURPLE_ALIEN) {
            throw new IllegalArgumentException("Wrong place!");
        }else{
            listOfToken.add(token);
        }
    }

    public void setSize(int size){
        max=size;
    }

    public Human getType(){
        return isAlien;
    }

    /**
     * method for giving out a human
     * @param index human that will be kick out the party
     * @throws EmptyHousingList if the housing unit is empty
     * @return it always returns 1 because there are only humans
     */
    public int removeHumans(int index) throws EmptyHousingList {
        if(listOfToken.isEmpty()) throw new EmptyHousingList("HousingList is empty");
        listOfToken.remove(index);
        return 1;
    }

    /**
     * @return the number of human in the housing unit
     */
    public int returnLenght(){
        return listOfToken.size();
    }
    public void setConnected(boolean connected){
        isConnected = connected;
    }
    public boolean isConnected(){
        return isConnected;
    }
}
