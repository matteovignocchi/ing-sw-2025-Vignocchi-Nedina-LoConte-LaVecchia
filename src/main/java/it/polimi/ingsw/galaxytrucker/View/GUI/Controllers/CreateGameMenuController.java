package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

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

    @FXML
    public void initialize() {
        setupInitialState();
    }

    private void setupInitialState() {
        // Show demo selection, hide player selection
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

        exitButton.setOnAction(e -> Platform.runLater(() -> guiView.setSceneEnum(SceneEnum.MAIN_MENU)));
    }

    private void showPlayerSelection() {
        // Hide demo selection, show player selection
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

    private void confirmGameData(int playerCount) {
        List<Object> data = List.of(isDemo, playerCount);
        guiView.resolveDataGame(data);  // completa la future che aspetta il ClientController
        guiView.setSceneEnum(SceneEnum.WAITING_QUEUE);  // o qualunque scena venga dopo
    }

    @Override
    public void postInitialize() {
        // se serve setup dopo il caricamento completo
    }
}
