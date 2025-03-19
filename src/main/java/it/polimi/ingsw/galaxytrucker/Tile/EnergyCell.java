package it.polimi.ingsw.galaxytrucker.Tile;

public class EnergyCell extends Tile{
    private int capacity;

    public EnergyCell(int a, int b, int c, int d, int capacity) {
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean useBattery(){
        if(capacity == 0){
            return false;
        }else{
            capacity--;
            return true;
        }
    }
}
