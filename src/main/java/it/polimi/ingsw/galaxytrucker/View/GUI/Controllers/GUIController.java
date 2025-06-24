package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.*;
import javafx.fxml.FXML;

import java.awt.*;
import java.awt.event.ActionEvent;
import javafx.scene.control.Button;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

    public abstract void postInitialize2();

    public void updateDashboard(ClientTile[][] ship) {
    }

}

