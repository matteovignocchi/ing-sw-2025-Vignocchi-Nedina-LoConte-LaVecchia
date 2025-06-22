package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class SceneRouter {
    private final Stage stage;
    private final Map<SceneEnum, Scene> scenes = new HashMap<>();
    private final Map<SceneEnum, GUIController> controllers = new HashMap<>();
    private final GUIModel model;
    private final UserInputManager inputManager;
    private final GUIView guiView;

    public SceneRouter(Stage stage, GUIModel model, UserInputManager inputManager, GUIView guiView) {
        this.stage = stage;
        this.model = model;
        this.inputManager = inputManager;
        this.guiView = guiView;
    }

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

                controller.setGuiView(guiView); // <-- QUI lo passi
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

    public void setScene(SceneEnum sceneEnum) {
        Scene scene = scenes.get(sceneEnum);
        if (scene != null) {
            stage.setScene(scene);
        } else {
            System.err.println("Scene not found: " + sceneEnum);
        }
    }

    public GUIController getController(SceneEnum sceneEnum) {
        return controllers.get(sceneEnum);
    }
}
