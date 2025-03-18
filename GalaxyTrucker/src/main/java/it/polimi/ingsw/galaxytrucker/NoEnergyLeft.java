package it.polimi.ingsw.galaxytrucker;

public class NoEnergyLeft extends RuntimeException {
    public NoEnergyLeft(String message) {
        super(message);
    }
}
