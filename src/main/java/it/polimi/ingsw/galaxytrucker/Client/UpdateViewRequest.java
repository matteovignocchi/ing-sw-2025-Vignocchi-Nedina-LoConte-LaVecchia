package it.polimi.ingsw.galaxytrucker.Client;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) representing an update to the player's view state.
 * Contains essential information about the player’s current ship and resources
 * to be sent over the network (e.g., via sockets) to update the client UI.
 * This class is serializable for network transmission.
 * @author Oleg Nedina
 */
public class UpdateViewRequest implements Serializable {
    private final String nickname;
    private final double firePower;
    private final int powerEngine;
    private final int credits;
    private final boolean purpleAlien;
    private final boolean brownAlien;
    private final int numberOfHuman;
    private final int numberOfEnergy;

    /**
     * Constructs an UpdateViewRequest with all necessary player state fields.
     * @param nickname the player's nickname
     * @param firePower the total firepower of the player's ship
     * @param powerEngine the total engine power of the player's ship
     * @param credits the current credits held by the player
     * @param purpleAlien true if a purple alien is present on the ship
     * @param brownAlien true if a brown alien is present on the ship
     * @param numberOfHuman total number of humans on the ship
     * @param numberOfEnergy total energy available on the ship
     */

    public UpdateViewRequest(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        this.nickname = nickname;
        this.firePower = firePower;
        this.powerEngine = powerEngine;
        this.credits = credits;
        this.purpleAlien = purpleAlien;
        this.brownAlien = brownAlien;
        this.numberOfHuman = numberOfHuman;
        this.numberOfEnergy = numberOfEnergy;
    }

    /**
     * Returns the player's nickname.
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the player's total firepower.
     * @return the firepower
     */
    public double getFirePower() {
        return firePower;
    }

    /**
     * Returns the player's total engine power.
     * @return the engine power
     */
    public int getPowerEngine() {
        return powerEngine;
    }

    /**
     * Returns the player's current credits.
     * @return the credits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Indicates if a purple alien token is present on the ship.
     * @return true if present, false otherwise
     */
    public boolean hasPurpleAlien() {
        return purpleAlien;
    }

    /**
     * Indicates if a brown alien token is present on the ship.
     * @return true if present, false otherwise
     */
    public boolean hasBrownAlien() {
        return brownAlien;
    }

    /**
     * Returns the total number of human tokens on the ship.
     * @return the number of humans
     */
    public int getNumberOfHuman() {
        return numberOfHuman;
    }

    /**
     * Returns the total energy units available on the ship.
     * @return the energy count
     */
    public int getNumberOfEnergy() {
        return numberOfEnergy;
    }
}