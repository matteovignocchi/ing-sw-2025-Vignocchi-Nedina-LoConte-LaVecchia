package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NicknameDialogController {

    @FXML
    private TextField nicknameField;

    private GUIView guiView;

    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    @FXML
    public void confirmNickname() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty()) {
            guiView.resolveNickname(nickname);
            Stage dialogStage = (Stage) nicknameField.getScene().getWindow();
            dialogStage.close();
        } else {
            showError("Nickname cannot be empty.");
        }
    }




    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Nickname");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
