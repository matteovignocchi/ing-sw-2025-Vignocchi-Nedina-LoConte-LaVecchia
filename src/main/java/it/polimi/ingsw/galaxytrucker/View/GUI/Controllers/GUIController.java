package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;

public abstract class GUIController {
    protected ClientTile currentTile;
    protected ClientTile[][] dashBoard;
    protected GUIView guiView;

    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    public void setDashBoard(ClientTile[][] dashBoard) {
        this.dashBoard = dashBoard;
    }

    public void setCurrentTile(ClientTile tile) {

    }
}
