package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ChatController extends GUIController{

    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScroll;

    public void addMessage(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(190);

        chatBox.getChildren().add(label);

        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }
    @FXML
    private void closeChat() {
        Stage stage = (Stage) chatBox.getScene().getWindow();
        stage.close();
    }

    @Override
    public void postInitialize() {

    }
}
