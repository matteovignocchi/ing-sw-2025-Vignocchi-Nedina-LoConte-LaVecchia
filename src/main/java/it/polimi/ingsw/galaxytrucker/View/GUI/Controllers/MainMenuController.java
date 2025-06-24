package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainMenuController extends GUIController {

    @FXML
    private Button joinButton;

    @FXML
    private Button createButton;

    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        joinButton.setOnAction(event -> {
            guiView.resolveMenuChoice("2");
        });

        createButton.setOnAction(event -> {
            guiView.resolveMenuChoice("1");
        });

        logoutButton.setOnAction(event -> {
            guiView.resolveMenuChoice("3");
            System.exit(0);
        });
    }


    @Override
    public void postInitialize() {
    }

}
