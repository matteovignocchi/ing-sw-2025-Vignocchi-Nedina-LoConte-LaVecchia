
package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class NicknameDialogController extends GUIController {

    @FXML
    private TextField nicknameField;

    @FXML
    private Button confirmButton;

    @FXML
    public void initialize() {
        confirmButton.setOnAction(event -> confirmNickname());
    }

    public void confirmNickname() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty()) {
            model.setNickname(nickname);
            if (!inputManager.nicknameFuture.isDone())
                inputManager.nicknameFuture.complete(nickname);
                inputManager.resetAll();
        } else {
            guiView.reportError("Nickname cannot be empty.");
        }

    }

    @Override
    public void postInitialize() {
    }




}
