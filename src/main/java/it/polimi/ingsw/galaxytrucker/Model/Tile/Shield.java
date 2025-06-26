package it.polimi.ingsw.galaxytrucker.Model.Tile;

import java.io.Serializable;

/**
 * Represents a shield tile placed on the ship.
 * The shield has a fixed initial orientation protecting the north and east sides
 * (represented by values in the protectedCorners array). It can be rotated left or right,
 * changing the sides it protects.
 * Each protected side is marked with the value 8. Non-protected sides have value 0.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class Shield extends Tile implements Serializable {
    final int[] protectedCorners = new int[4];
    /**
     * the values are standard, and they are given when the game starts from the application
     * it also initializes the standard values for the shield
     * @param a connector value for the north side
     * @param b connector value for the east side
     * @param c connector value for the south side
     * @param d connector value for the west side
     * @param id the unique identifier of the tile
     */
    public Shield(int a, int b, int c, int d, int id) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        protectedCorners[0] = 8;
        protectedCorners[1] = 8;
        protectedCorners[2] = 0;
        protectedCorners[3] = 0;
        idTile = id;
    }

    /**
     * Rotates the shield one position to the right.
     * Shifts the corner connector values and the protectedCorners array clockwise,
     * updating the orientation of the tile.
     */
    @Override
    public void rotateRight() {
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
     * Rotates the shield one position to the left.
     * Shifts the corner connector values and the protectedCorners array counter-clockwise,
     * updating the orientation of the tile.
     */
    @Override
    public void rotateLeft() {
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

    /**
     * Returns the value indicating whether the side at index x is protected.
     * A value of 8 means the side is protected by the shield; 0 means it's not.
     * @param x the index of the side (0 = north, 1 = east, 2 = south, 3 = west)
     * @return the protection value (8 if protected, 0 if not)
     */
    public int getProtectedCorner(int x) {
        return protectedCorners[x];
    }


    /**
     * Sets the protection value for the side at index x.
     * This method allows manually changing which sides are protected (e.g., for testing).
     * @param x the index of the side (0 = north, 1 = east, 2 = south, 3 = west)
     * @param y the protection value (8 for protected, 0 for not)
     */
    public void setProtectedCorner(int x, int y) {
        protectedCorners[x] = y;
    }
}
