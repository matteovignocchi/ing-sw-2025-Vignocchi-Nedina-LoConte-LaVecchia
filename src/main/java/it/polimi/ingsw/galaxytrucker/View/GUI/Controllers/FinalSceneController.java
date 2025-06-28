package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientGamePhase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

import java.util.List;
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
    @FXML private Label opponent1Label;
    @FXML private Label opponent2Label;
    @FXML private Label opponent3Label;
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
    private void updateFinalScreen() {
        String me = model.getNickname();
        int[] myData = model.getPlayerPositions().get(me);
        int myCredits = (myData != null && myData.length > 1) ? myData[1] : 0;
        boolean iAmEliminated = (myData != null && myData.length > 2 && myData[2] == 1);

        playerNameLabel.setText(me);
        playerPointsLabel.setText("Credits: " + myCredits);

        List<Map.Entry<String, int[]>> sortedPlayers = model.getPlayerPositions().entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(
                        e2.getValue().length > 1 ? e2.getValue()[1] : 0,
                        e1.getValue().length > 1 ? e1.getValue()[1] : 0))
                .toList();

        boolean iAmFirst = !sortedPlayers.isEmpty() && sortedPlayers.get(0).getKey().equals(me);

        if (iAmFirst && !iAmEliminated) {
            titleLabel.setText("You won!");
            titleLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 28px;");
        } else {
            titleLabel.setText("Final Results");
            titleLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 24px;");
        }

        int index = 0;
        int labelIndex = 0;

        for (Map.Entry<String, int[]> entry : sortedPlayers) {
            String name = entry.getKey();
            if (name.equals(me)) continue;

            String line = (index + 1) + "° - " + name;

            switch (labelIndex++) {
                case 0 -> opponent1Label.setText(line);
                case 1 -> opponent2Label.setText(line);
                case 2 -> opponent3Label.setText(line);
            }

            index++;

            if (labelIndex >= 3) break;
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

