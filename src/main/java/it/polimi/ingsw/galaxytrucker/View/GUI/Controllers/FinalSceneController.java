package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Map;


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
    @FXML private Label playerCargoLabel;
    @FXML private Label opponent1Label;
    @FXML private Label opponent2Label;
    @FXML private Label opponent3Label;
    @FXML private Button logoutButton;

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
    private void updateFinalScreen() {
        String me = model.getNickname();
        playerNameLabel.setText(me);
        playerPointsLabel.setText("Credits: " + model.credits);
        playerCargoLabel.setText("Position: " + model.getMyPosition(me));

        int index = 0;
        for (Map.Entry<String, int[]> entry : model.getPlayerPositions().entrySet()) {
            String name = entry.getKey();
            if (name.equals(me)) continue;

            int[] pos = entry.getValue();
            String line = name + " → Position: " + pos[0];
            switch (index++) {
                case 0 -> opponent1Label.setText(line);
                case 1 -> opponent2Label.setText(line);
                case 2 -> opponent3Label.setText(line);
            }
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

