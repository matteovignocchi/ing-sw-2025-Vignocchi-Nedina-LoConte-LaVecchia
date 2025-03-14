package it.polimi.ingsw.galaxytrucker.Tile;

public class CentralHousingUnit extends Tile {
    private boolean isDestroyed;

    public CentralHousingUnit() {
        isDestroyed = false;
        corners[0]=3;
        corners[1]=3;
        corners[2]=3;
        corners[3]=3;

    }

    public boolean checkStatus() {
        return isDestroyed;
    }

    public void destroy() {
        isDestroyed = true;
    }

}
