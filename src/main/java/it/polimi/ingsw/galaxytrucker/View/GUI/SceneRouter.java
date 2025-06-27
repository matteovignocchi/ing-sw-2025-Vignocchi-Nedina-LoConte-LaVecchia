package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the JavaFX scenes and their controllers for the application.
 * Responsible for loading all scenes defined in the `SceneEnum`, associating
 * each with its controller, and managing scene transitions on the primary stage.
 * Provides access to controllers and scenes for external manipulation.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */

public class SceneRouter {
    private final Stage stage;
    private final Map<SceneEnum, Scene> scenes = new HashMap<>();
    private final Map<SceneEnum, GUIController> controllers = new HashMap<>();
    private final GUIModel model;
    private final UserInputManager inputManager;
    private final GUIView guiView;

    /**
     * Constructs a SceneRouter to manage GUI scenes and controllers.
     * @param stage the primary JavaFX stage where scenes are displayed
     * @param model the GUI model representing application state
     * @param inputManager the input manager for user interactions
     * @param guiView the GUI view interface
     */
    public SceneRouter(Stage stage, GUIModel model, UserInputManager inputManager, GUIView guiView) {
        this.stage = stage;
        this.model = model;
        this.inputManager = inputManager;
        this.guiView = guiView;
    }

    /**
     * Loads and initializes all scenes and their controllers as defined in `SceneEnum`.
     * Loads the FXML resources, sets up controller references, and stores scenes and controllers in maps.
     * Logs errors if resources or controllers are missing or if loading fails.
     */
    public void initializeAllScenes() {
        for (SceneEnum scene : SceneEnum.values()) {
            String path = scene.value();
            URL resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("ERROR: Resource not found for scene: " + scene.name() + " at path: " + path);
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                GUIController controller = loader.getController();

                if (controller == null) {
                    System.err.println("ERROR: Controller is null for scene: " + scene.name());
                    continue;
                }

                controller.setGuiView(guiView);
                controller.setModel(model);
                controller.setInputManager(inputManager);
                controller.setSceneRouter(this);

                controllers.put(scene, controller);
                scenes.put(scene, new Scene(root));
            } catch (IOException | RuntimeException e) {
                System.err.println("ERROR while loading scene: " + scene.name() + " at path: " + path);
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the current scene on the primary stage to the specified scene.
     * @param sceneEnum the enum value identifying the scene to display
     */
    public void setScene(SceneEnum sceneEnum) {
        Scene scene = scenes.get(sceneEnum);
        if (scene != null) {
            stage.setScene(scene);
        } else {
            System.err.println("Scene not found: " + sceneEnum);
        }
        stage.centerOnScreen();
    }

    /**
     * Returns the currently displayed JavaFX scene on the primary stage.
     * @return the current Scene instance
     */
    public Scene getCurrentScene() {
        return stage.getScene();
    }

    /**
     * Retrieves the controller associated with the specified scene.
     * @param sceneEnum the enum value identifying the scene
     * @return the controller for that scene, or null if not found
     */
    public GUIController getController(SceneEnum sceneEnum) {
        return controllers.get(sceneEnum);
    }

    /**
     * Clears all loaded scenes and controllers and reloads them from the FXML files.
     * Useful for refreshing the GUI after dynamic changes.
     */
    public void reinitializeAllScenes() {
        scenes.clear();
        controllers.clear();
        initializeAllScenes();
    }

    /**
     * Retrieves the JavaFX scene associated with the specified scene enum.
     * @param sceneEnum the enum value identifying the scene
     * @return the JavaFX Scene, or null if not loaded
     */
    public Scene getScene(SceneEnum sceneEnum) {
        return scenes.get(sceneEnum);
    }


}
