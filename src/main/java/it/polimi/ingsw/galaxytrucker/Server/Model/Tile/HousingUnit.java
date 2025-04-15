package it.polimi.ingsw.galaxytrucker.Server.Model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * class for the housing unit
 * max is the parameter for the slots
 * Human is a flag that gives the information of which token can be placed in the unit
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
     * @param isAlien is the type of housing unit
     */
    public HousingUnit(int a,int b,int c,int d, Human isAlien) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        this.isAlien=isAlien;
    }

    /**
     * add to the tile one token till it reaches the max capacity
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

    /**
     * @param size when a alien is placed on the tile, it sets a different max size
     */
    public void setSize(int size){
        max=size;
    }

    /**
     * @return the type of token on the tile
     */
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
        Human temp = listOfToken.get(index);
        listOfToken.remove(index);
        if(temp == Human.BROWN_ALIEN){
            return 2;
        }
        if(temp == Human.PURPLE_ALIEN){
            return 3;
        }
        return 1;
    }

    /**
     * @return the number of human in the housing unit
     */
    public int returnLenght(){
        return listOfToken.size();
    }

    /**
     * @param connected set true if is connected to another housing unit
     */
    public void setConnected(boolean connected){
        isConnected = connected;
    }

    /**
     * @return true if it is connected
     */
    public boolean isConnected(){
        return isConnected;
    }
}
