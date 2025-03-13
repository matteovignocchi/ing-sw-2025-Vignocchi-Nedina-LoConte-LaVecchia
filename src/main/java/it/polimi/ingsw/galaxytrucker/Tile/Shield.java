package it.polimi.ingsw.galaxytrucker.Tile;

/**
 * class for the shields
 * the orientation of the protected corners is standard and it is given by the new array protectedCorners
 * it is always on north and east
 * @author Matteo Vignocchi
 *
 */


public class Shield extends Tile {
    final int[] protectedCorners = new int[4];
    /**
     * the values are standard, and they are given when the game starts from the application
     * it also initializes the standard values for the shield
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public Shield(int a, int b, int c, int d) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        protectedCorners[0] = 8;
        protectedCorners[1] = 8;
        protectedCorners[2] = 0;
        protectedCorners[3] = 0;
    }

    /**
     * method to shift right the vector of corners by one position and rotates
     * the orientation of the shield
     * works directly with the array corners of the class
     *
     * @author Oleg Nedina & Matteo Vignocchi
     */
    @Override
    public void RotateRight() {
        int tmp, tmp2;
        tmp = corners[3];
        corners[3] = corners[2];
        corners[2] = corners[1];
        corners[1] = corners[0];
        corners[0] = tmp;
        tmp2 = protectedCorners[3];
        protectedCorners[3] = protectedCorners[2];
        protectedCorners[2] = protectedCorners[1];
        protectedCorners[1] = protectedCorners[0];
        protectedCorners[0] = tmp2;
    }

    /**
     * method to shift left the vector of corners by one position and rotates
     * the orientation of the shield
     * works directly with the array corners of the class
     *
     * @author Oleg Nedina & Matteo Vignocchi
     */
    @Override
    public void RotateLeft() {
        int tmp, tmp2;
        tmp = corners[0];
        corners[0] = corners[1];
        corners[1] = corners[2];
        corners[2] = corners[3];
        corners[3] = tmp;
        tmp2 = protectedCorners[0];
        protectedCorners[0] = protectedCorners[1];
        protectedCorners[1] = protectedCorners[2];
        protectedCorners[2] = protectedCorners[3];
        protectedCorners[3] = tmp2;
    }
}
