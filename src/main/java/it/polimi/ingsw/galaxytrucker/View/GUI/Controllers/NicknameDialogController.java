package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NicknameDialogController extends GUIController {

    @FXML
    private TextField nicknameField;

    @FXML
    public void initialize() {

    }

    @FXML
    public void confirmNickname() {
        String nickname = nicknameField.getText();
        if (!nickname.isEmpty()) {
            guiView.resolveNickname(nickname);

        } else {
            guiView.reportError("Nickname cannot be empty.");
        }
    }
}
