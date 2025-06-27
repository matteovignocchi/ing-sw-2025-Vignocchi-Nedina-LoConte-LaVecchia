package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.*;


public abstract class GUIController {
    protected GUIModel model;
    protected UserInputManager inputManager;
    protected SceneRouter sceneRouter;
    protected GUIView guiView;

    public void setModel(GUIModel model) {
        this.model = model;
    }

    public void setInputManager(UserInputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setSceneRouter(SceneRouter router) {
        this.sceneRouter = router;
    }

    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    public abstract void postInitialize();

    public void updateDashboard(ClientTile[][] ship) {
    }

    public void postInitialize2() {
    }

    public void postInitialize3() {

    }
}

