package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.Cannon;
import it.polimi.ingsw.galaxytrucker.Tile.Engine;
import it.polimi.ingsw.galaxytrucker.Tile.HousingUnit;
import it.polimi.ingsw.galaxytrucker.Tile.Tile;

public class ViewEngine implements Visitor {
    @Override
    public int visit(Cannon cannon) {
        return 0;
    }

    @Override
    public int visit(Tile tile) {
        return 0;
    }

    @Override
    public int visit(Engine e) {
        if(e.isDouble()){
            return 2;
        }else{
            return 1;
        }
    }

    @Override
    public int visit(HousingUnit H) {
        return 0;
    }
}

