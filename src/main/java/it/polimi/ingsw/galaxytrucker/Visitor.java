package it.polimi.ingsw.galaxytrucker;

import com.sun.javafx.geom.Edge;
import it.polimi.ingsw.galaxytrucker.Tile.*;
import javafx.concurrent.Worker;

public class Visitor {
    public int visit(Cannon cannon) {
        if (cannon.isDouble()) {
            return 2;
        } else {
            return 1;
        }
    }
    public int visit(Tile tile){
        return 0;
    }
    public int visit(Engine engine){
        if(engine.isDouble()){
            return 22;
        }else{
            return 11;
        }
    }
    public int visit(HousingUnit housingUnit){
        return 100+housingUnit.returnLenght();
    }

    public int visit(StorageUnit storageUnit){
        return 200+storageUnit.getListSize();
    }

    public int visit(EnergyCell energyCell){
        return 300+energyCell.getCapacity();
    }

    public boolean visit(Shield shield){
        return true;
    }
    public boolean returnProtection(Shield shield, int direction){
        if(shield.getProtectedCorner(direction) == 8){
            return true;
        }else{
            return false;
        }
    }
}
