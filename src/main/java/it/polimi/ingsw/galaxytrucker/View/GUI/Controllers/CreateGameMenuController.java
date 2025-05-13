package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;

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

    public void initialize() {

        demoQuestionText.setVisible(true);
        level2Button.setVisible(true);
        demoButton.setVisible(true);
        exitButton.setVisible(true);

        backButton.setVisible(false);
        twoPlayerButton.setVisible(false);
        threePlayerButton.setVisible(false);
        fourPlayerButton.setVisible(false);
        playersQuestionText.setVisible(false);
        backButton.setVisible(false);


        // Imposta gli handler per i bottoni del demo flight
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
        // Nascondi elementi demo choice
        demoQuestionText.setVisible(false);
        level2Button.setVisible(false);
        demoButton.setVisible(false);
        exitButton.setVisible(false);

        // Mostra elementi player selection
        backButton.setVisible(true);
        twoPlayerButton.setVisible(true);
        threePlayerButton.setVisible(true);
        fourPlayerButton.setVisible(true);
        playersQuestionText.setVisible(true);
        backButton.setVisible(true);

        // Personalizza il testo in base alla modalità
        playersQuestionText.setVisible(true);
        demoButton.setVisible(false);


    }

    @FXML
    private void back() {
        // Nascondi player selection
        backButton.setVisible(false);
        twoPlayerButton.setVisible(false);
        threePlayerButton.setVisible(false);
        fourPlayerButton.setVisible(false);
        playersQuestionText.setVisible(false);
        backButton.setVisible(false);

        // Mostra demo choice
        demoQuestionText.setVisible(true);
        demoButton.setVisible(true);
        level2Button.setVisible(true);
        exitButton.setVisible(true);
    }
    @FXML
    public void exit() throws IOException {
        ((GUIView) ClientController.getInstance().getViewInterface()).setMainScene(SceneEnum.MAIN_MENU);
    }

}
