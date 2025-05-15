package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController extends GUIController {
    @FXML
    private Button joinButton;
    @FXML
    private Button createButton;

    private ClientController clientController;

    @FXML
    public void initialize() {
        clientController = ClientController.getInstance();
        joinButton.setOnAction(event -> {
            try {
                //TODO fare join con le scelte
                clientController.joinExistingGame();
                joinGame();
            } catch (Exception e) {
                e.printStackTrace();
                guiView.reportError("Failed to load join game scene");
            }
        });

        createButton.setOnAction(event -> {
            try {
                clientController.createNewGame();
                createGame();
            } catch (Exception e) {
                e.printStackTrace();
                guiView.reportError("Failed to load create game scene");
            }
        });
    }



    @FXML
    private void joinGame() throws IOException {
        ((GUIView) ClientController.getInstance().getViewInterface()).setMainScene(SceneEnum.JOIN_GAME_MENU);
    }

    @FXML
    private void createGame() throws IOException {
        ((GUIView) ClientController.getInstance().getViewInterface()).setMainScene(SceneEnum.CREATE_GAME_MENU);
    }


    @FXML
    public void logout(){
        try {
            clientController.logOutGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
