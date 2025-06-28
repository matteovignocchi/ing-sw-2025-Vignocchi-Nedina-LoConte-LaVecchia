
package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

/**
 * Controller for the nickname input dialog GUI scene.
 * Handles user input for entering a nickname and confirms it to the input manager.
 * Validates that the nickname is not empty before submitting.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class NicknameDialogController extends GUIController {

    @FXML private TextField nicknameField;
    @FXML private Button confirmButton;

    /**
     * Initializes the nickname dialog scene.
     * Sets up the confirm button to trigger nickname confirmation when clicked.
     */
    @FXML
    public void initialize() {
        confirmButton.setOnAction(event -> confirmNickname());
    }

    /**
     * Confirms the nickname entered by the user.
     * Trims whitespace and validates that the nickname is not empty.
     * If valid, sets the nickname in the model and completes the nickname future
     * in the input manager.
     * If invalid, reports an error via the GUI view.
     */
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

    /**
     * Empty override of postInitialize; no additional setup required after scene load.
     */
    @Override
    public void postInitialize() {
    }

}
