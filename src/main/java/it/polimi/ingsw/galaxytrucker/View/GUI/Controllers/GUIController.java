package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;

public abstract class GUIController {
    protected Tile currentTile;
    protected Tile[][] dashBoard;
    protected GUIView guiView;
    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    public void setDashBoard(Tile[][] dashBoard) {
        this.dashBoard = dashBoard;
    }

    public void setCurrentTile(Tile tile) {

    }
}
