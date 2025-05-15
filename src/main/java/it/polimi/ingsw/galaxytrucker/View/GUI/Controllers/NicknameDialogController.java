package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NicknameDialogController extends GUIController {


    @FXML
    private TextField nicknameField;


    @FXML
    public void confirmNickname() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty()) {
            guiView.resolveNickname(nickname);

            Stage stage = (Stage) nicknameField.getScene().getWindow();
            stage.close();
        }
    }

}