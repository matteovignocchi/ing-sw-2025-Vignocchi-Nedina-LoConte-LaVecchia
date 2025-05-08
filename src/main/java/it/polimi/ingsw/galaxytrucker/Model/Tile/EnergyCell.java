package it.polimi.ingsw.galaxytrucker.Model.Tile;

/**
 * class for double and triple energy cell
 * when the game initializes the tile, it will give a different capacity
 * this class store the information of how many battery the player has left
 * @author Matteo Vignocchi
 */
public class EnergyCell extends Tile{
    private int capacity;
    /**
     * the values are standard, and they are given when the game starts from the application
     * @param a
     * @param b
     * @param c
     * @param d
     * @param capacity
     */
    public EnergyCell(int a, int b, int c, int d, int capacity, int id) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        this.capacity = capacity;
        idTile = id;
    }

    /**
     * @return how many energy the player has
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * this method reduce by one the number of energy in the energy cell
     * @return true if there was at leats one energy cell left, false if the cell is empty
     */
    public boolean useBattery(){
        if(capacity == 0){
            return false;
        }else{
            capacity--;
            return true;
        }
    }
}