package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.*;

/**
 * Abstract base class for all GUI controllers in the application.
 * Provides common fields and setter methods for the GUI model, input manager,
 * scene router, and GUI view references.
 * Defines lifecycle hooks for initialization that subclasses should implement or override.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */
public abstract class GUIController {
    protected GUIModel model;
    protected UserInputManager inputManager;
    protected SceneRouter sceneRouter;
    protected GUIView guiView;

    /**
     * Sets the GUI model instance for this controller.
     * @param model the GUIModel representing the current application state
     */
    public void setModel(GUIModel model) {
        this.model = model;
    }

    /**
     * Sets the input manager responsible for handling user input futures.
     * @param inputManager the UserInputManager instance
     */
    public void setInputManager(UserInputManager inputManager) {
        this.inputManager = inputManager;
    }

    /**
     * Sets the scene router used to switch and manage GUI scenes.
     * @param router the SceneRouter instance
     */
    public void setSceneRouter(SceneRouter router) {
        this.sceneRouter = router;
    }

    /**
     * Sets the GUI view interface reference.
     * @param guiView the GUIView instance
     */
    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    /**
     * Abstract method called after the FXML scene has been loaded and initialized.
     * Subclasses must implement this method to perform post-load initialization tasks.
     */
    public abstract void postInitialize();

    /**
     * Optional method for updating the ship dashboard in the GUI.
     * Subclasses may override this to update their dashboard UI components.
     * @param ship the 2D array of ClientTiles representing the player's ship
     */
    public ClientTile[][] getDashboard(){ return model.getDashboard(); }

    public void updateDashboard() {
    }

    /**
     * Optional lifecycle hooks for additional post-initialization steps.
     * Subclasses may override these methods as needed.
     */
    public void postInitialize2() {
    }

    /**
     * Optional lifecycle hooks for additional post-initialization steps.
     * Subclasses may override these methods as needed.
     */
    public void postInitialize3() {
    }
}

