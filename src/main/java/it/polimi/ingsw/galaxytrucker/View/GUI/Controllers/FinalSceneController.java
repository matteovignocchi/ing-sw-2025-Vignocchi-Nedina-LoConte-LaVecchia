package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the final scene GUI displayed at the end of the game.
 * Shows the player's final statistics including nickname, credits, and position,
 * as well as the positions of up to three opponents.
 * Provides a logout button that resets the GUI state and returns to the main menu.
 * @author Oleg Nedina
 * @author  Matteo Vignocchi
 */
public class FinalSceneController extends GUIController {

    @FXML private Label playerNameLabel;
    @FXML private Label playerPointsLabel;
    @FXML private Button logoutButton;
    @FXML private Label titleLabel;

    /**
     * Initializes the final scene, setting up the logout button text and action handler.
     * The logout button resets the GUI and signals the controller to process logout.
     */
    @FXML
    public void initialize() {
        logoutButton.setText("Logout");
        logoutButton.setOnAction(e -> {
            guiView.resetGUIState();
            guiView.updateState(ClientGamePhase.MAIN_MENU);
            guiView.resolveCommand("logout");
        });
    }

    /**
     * Updates all labels on the final screen with the player's and opponents' stats.
     * Displays the current player's nickname, credits, and position.
     * Lists the positions of up to three opponents, skipping the current player.
     */
    public void updateFinalScreen() {
        String me = model.getNickname();
        int[] myData = model.getPlayerPositions().get(me);
        int myCredits = (myData != null && myData.length > 1) ? myData[1] : 0;

        playerNameLabel.setText(me);
        playerPointsLabel.setText("Credits: " + myCredits);


        if (myCredits > 0) {
            titleLabel.setText("You won!");
            titleLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 28px;");
        } else {
            titleLabel.setText("You lost!");
            titleLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 28px;");
        }
    }

    /**
     * Called after the scene is fully loaded to populate the final screen data.
     */
    @Override
    public void postInitialize() {
        updateFinalScreen();

    }
}

