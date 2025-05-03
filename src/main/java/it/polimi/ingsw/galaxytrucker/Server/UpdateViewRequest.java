package it.polimi.ingsw.galaxytrucker.Server;
import java.io.Serializable;

public class UpdateViewRequest implements Serializable {
    private final String nickname;
    private final Float firePower;
    private final int powerEngine;
    private final int credits;
    private final int position;
    private final boolean purpleAlien;
    private final boolean brownAlien;
    private final int numberOfHuman;
    private final int numberOfEnergy;

    public UpdateViewRequest(String nickname, Float firePower, int powerEngine, int credits, int position,
                             boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        this.nickname = nickname;
        this.firePower = firePower;
        this.powerEngine = powerEngine;
        this.credits = credits;
        this.position = position;
        this.purpleAlien = purpleAlien;
        this.brownAlien = brownAlien;
        this.numberOfHuman = numberOfHuman;
        this.numberOfEnergy = numberOfEnergy;
    }

    // Getters
    public String getNickname() {
        return nickname;
    }

    public Float getFirePower() {
        return firePower;
    }

    public int getPowerEngine() {
        return powerEngine;
    }

    public int getCredits() {
        return credits;
    }

    public int getPosition() {
        return position;
    }

    public boolean hasPurpleAlien() {
        return purpleAlien;
    }

    public boolean hasBrownAlien() {
        return brownAlien;
    }

    public int getNumberOfHuman() {
        return numberOfHuman;
    }

    public int getNumberOfEnergy() {
        return numberOfEnergy;
    }
}