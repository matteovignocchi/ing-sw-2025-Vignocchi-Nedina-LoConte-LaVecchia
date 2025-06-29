package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the chat window in the GUI.
 * Manages message display and chat window closing.
 * @author Matteo Vignocchi
 */
public class ChatController extends GUIController{

    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScroll;

    /**
     * Add a message to the chat box.
     *
     * @param message the message to be displayed
     */
    public void addMessage(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(190);

        chatBox.getChildren().add(label);

        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    /**
     * Close the chat window.
     */
    @FXML
    private void closeChat() {
        Stage stage = (Stage) chatBox.getScene().getWindow();
        stage.close();
    }

    /**
     * Perform any post-initialization logic for the controller.
     * This implementation does nothing.
     */
    @Override
    public void postInitialize() {
    }
}
