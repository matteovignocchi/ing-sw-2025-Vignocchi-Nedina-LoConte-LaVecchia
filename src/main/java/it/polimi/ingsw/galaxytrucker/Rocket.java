package it.polimi.ingsw.galaxytrucker;

/**
 * Class for the rocket
 * @author Gabriele La Vecchia & Francesco Lo Conte
 */

//PROVVISORIO
public class Rocket {
    private final Colour colour;
    /**
     * @deprecated useful attribute actually?
     * or is color enough?
     */
    private final int IDPlayer;

    /**
     * Class constructor
     * @param PlayerColour
     * @param IDPlayer
     */
    public Rocket(Colour PlayerColour, int IDPlayer) {
        this.colour = PlayerColour;
        this.IDPlayer = IDPlayer;
    }

    /**
     * Getter for rocket's colour
     * @return Colour
     */
    public Colour getColour() {
        return colour;
    }

    /**
     * Getter for Rocket's IDPlayer
     * @return int
     */
    public int getIDPlayer() {
        return IDPlayer;
    }
}

