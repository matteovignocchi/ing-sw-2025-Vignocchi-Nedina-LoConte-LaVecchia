package it.polimi.ingsw.galaxytrucker.Model;

public class NoEnergyLeft extends RuntimeException {
    public NoEnergyLeft(String message) {
        super(message);
    }
}
