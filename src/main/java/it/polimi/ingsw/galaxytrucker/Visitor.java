package it.polimi.ingsw.galaxytrucker;

import com.sun.javafx.geom.Edge;
import it.polimi.ingsw.galaxytrucker.Tile.Cannon;
import it.polimi.ingsw.galaxytrucker.Tile.Engine;
import it.polimi.ingsw.galaxytrucker.Tile.HousingUnit;
import it.polimi.ingsw.galaxytrucker.Tile.Tile;

public interface Visitor {
    public int visit(Cannon cannon);
    public int visit(Tile tile);
    public int visit(Engine engine);
    public int visit(HousingUnit H);
}
