package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import java.util.List;

/**
 * Controller for the "Create Game" menu GUI scene.
 * Manages UI components and user interactions to select game mode (demo or normal)
 * and the number of players (2 to 4).
 * Passes selected configuration data to the GUI view and transitions to the waiting queue scene.
 * @author Matteo Vignocchi
 */

public class CreateGameMenuController extends GUIController {

    @FXML private Button backButton;
    @FXML private Button demoButton;
    @FXML private Button exitButton;
    @FXML private Button level2Button;
    @FXML private Button twoPlayerButton;
    @FXML private Button threePlayerButton;
    @FXML private Button fourPlayerButton;
    @FXML private Text demoQuestionText;
    @FXML private Text playersQuestionText;

    private boolean isDemo = false;


    /**
     * Initializes the controller by setting up the initial UI state.
     * Shows demo selection buttons and hides player count options.
     */
    @FXML
    public void initialize() {
        setupInitialState();
    }

    /**
     * Configures the initial visibility and event handlers for demo and exit buttons.
     * Hides player count buttons and back button.
     * Sets up actions for demo selection, normal level 2 selection, back navigation, and exit.
     */
    private void setupInitialState() {

        demoQuestionText.setVisible(true);
        level2Button.setVisible(true);
        demoButton.setVisible(true);
        exitButton.setVisible(true);

        playersQuestionText.setVisible(false);
        twoPlayerButton.setVisible(false);
        threePlayerButton.setVisible(false);
        fourPlayerButton.setVisible(false);
        backButton.setVisible(false);

        demoButton.setOnAction(e -> {
            isDemo = true;
            showPlayerSelection();
        });

        level2Button.setOnAction(e -> {
            isDemo = false;
            showPlayerSelection();
        });

        backButton.setOnAction(e -> setupInitialState());

        exitButton.setOnAction(e -> {
            guiView.resolveDataGame(List.of());
        });
    }

    /**
     * Updates the UI to show player count selection buttons and back button.
     * Hides demo selection and exit buttons.
     * Sets up actions for selecting 2, 3, or 4 players.
     */
    private void showPlayerSelection() {

        demoQuestionText.setVisible(false);
        level2Button.setVisible(false);
        demoButton.setVisible(false);
        exitButton.setVisible(false);

        playersQuestionText.setVisible(true);
        twoPlayerButton.setVisible(true);
        threePlayerButton.setVisible(true);
        fourPlayerButton.setVisible(true);
        backButton.setVisible(true);

        twoPlayerButton.setOnAction(e -> confirmGameData(2));
        threePlayerButton.setOnAction(e -> confirmGameData(3));
        fourPlayerButton.setOnAction(e -> confirmGameData(4));
    }

    /**
     * Sends the selected game configuration (demo flag and player count) to the GUI view.
     * Changes the scene to the waiting queue and resets the menu UI to initial state.
     * @param playerCount the number of players selected
     */
    private void confirmGameData(int playerCount) {
        List<Object> data = List.of(isDemo, playerCount);
        guiView.resolveDataGame(data);
        guiView.setSceneEnum(SceneEnum.WAITING_QUEUE);
        setupInitialState();
    }

    /**
     * Empty override of postInitialize, no additional setup required after scene load.
     */
    @Override
    public void postInitialize() {
    }

}
