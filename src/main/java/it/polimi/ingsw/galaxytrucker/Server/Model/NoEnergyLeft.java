package it.polimi.ingsw.galaxytrucker.Server.Model;

public class NoEnergyLeft extends RuntimeException {
    public NoEnergyLeft(String message) {
        super(message);
    }
}
