package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * abstract class for the general structure of the tile and
 * define the common methods to move it and return some information about the tile
 * corners is an array of 4 position, the index indicate the cardinal orientation
 * 0 is for north, 1 is for est, 2 is for south , 3 is for west
 * the element in the index it depends on the instantiation of the tile
 * (all the child class will have a different instantiation)
 * 0 for no joint , 1 for 1 joint , 2 for 2 joint , 3 for universal , 4 for single cannon
 * 5 for double cannon , 6 for single engine , 7 for double engine and 8 for shield
 * isShown determinate if the tile is revealed
 * @author Oleg Nedina & Matteo Vignocchi
 */


public abstract class Tile {
    public int[] corners = new int[4];
    public boolean isShown = false;
    public boolean isTake= false;

    /**
     * method to shift right the vector of corners by one position
     * works directly with the array corners of the class
     */
    public void RotateRight(){
        int tmp ;
        tmp = corners[3];
        corners[3] = corners[2];
        corners[2] = corners[1];
        corners[1] = corners[0];
        corners[0] = tmp;
    }

    /**
     * method to shift left the vector of corners by one position
     * works directly with the array corners of the class
     */
    public void RotateLeft(){
        int tmp ;
        tmp = corners[0];
        corners[0] = corners[1];
        corners[1] = corners[2];
        corners[2] = corners[3];
        corners[3] = tmp;
    }
    /**
     * method to reveal the face of the card
     * works directly with the flag of the class
     */
    public void showTile()
    {
        isShown = true;
    }

    /**
     * the method changes the status of the tile
     */
    public void takeTile(){
        isTake = !isTake;
    }


    /**
     * method that return the array "corners"
     */
    public int[] getCorners() {
        return corners;
    }

    /**
     * @param i index of the orientation array
     * @return the value of the checked corner
     */
    public int controlCorners(int i){
        return corners[i];
    }

    /**
     * @return the flag isShown
     */
    public boolean isShown() {
        return isShown;
    }

    /**
     * @return the flag isTake
     */
    public boolean isTake() {
        return isTake;
    }
}
