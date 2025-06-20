package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateGameMenuController extends GUIController {
    @FXML
    private Button backButton;
    @FXML
    private Button demoButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button level2Button;
    @FXML
    private Button twoPlayerButton;
    @FXML
    private Button threePlayerButton;
    @FXML
    private Button fourPlayerButton;
    @FXML
    private Text demoQuestionText;
    @FXML
    private Text playersQuestionText;

    private boolean isDemo;
    private int player;

    @FXML
    public void initialize() {
        showDemoQuestion();
    }

    private void showDemoQuestion() {
        demoQuestionText.setVisible(true);
        level2Button.setVisible(true);
        demoButton.setVisible(true);
        exitButton.setVisible(true);

        backButton.setVisible(false);
        twoPlayerButton.setVisible(false);
        threePlayerButton.setVisible(false);
        fourPlayerButton.setVisible(false);
        playersQuestionText.setVisible(false);

        demoButton.setOnAction(e -> {
            isDemo = true;
            showPlayerSelection();
        });

        level2Button.setOnAction(e -> {
            isDemo = false;
            showPlayerSelection();
        });
    }

    private void showPlayerSelection() {
        demoQuestionText.setVisible(false);
        level2Button.setVisible(false);
        demoButton.setVisible(false);
        exitButton.setVisible(false);

        backButton.setVisible(true);
        twoPlayerButton.setVisible(true);
        threePlayerButton.setVisible(true);
        fourPlayerButton.setVisible(true);
        playersQuestionText.setVisible(true);

        twoPlayerButton.setOnAction(e -> {
            player = 2;
            List<Object> dataForGame = new ArrayList<>();
            dataForGame.add(isDemo);
            dataForGame.add(player);
            guiView.resolveDataGame(dataForGame);
        });

        threePlayerButton.setOnAction(e -> {
            player = 3;
            List<Object> dataForGame = new ArrayList<>();
            dataForGame.add(isDemo);
            dataForGame.add(player);
            guiView.resolveDataGame(dataForGame);
        });

        fourPlayerButton.setOnAction(e -> {
            player = 4;
            List<Object> dataForGame = new ArrayList<>();
            dataForGame.add(isDemo);
            dataForGame.add(player);
            guiView.resolveDataGame(dataForGame);
        });

    }


    @FXML
    private void back() {
        showDemoQuestion();
    }


    @FXML
    public void exit() throws IOException {
        Platform.runLater(() -> guiView.setSceneEnum(SceneEnum.MAIN_MENU));
    }

}
