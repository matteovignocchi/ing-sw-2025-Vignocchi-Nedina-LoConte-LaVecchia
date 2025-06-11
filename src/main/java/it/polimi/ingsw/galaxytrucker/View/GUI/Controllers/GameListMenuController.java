package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class GameListMenuController extends GUIController {

    @FXML private Button backButton;
    @FXML private Button joinButton;
    @FXML private ListView<String> gameListView;
    @FXML private Label infoLabel;

    private GUIView guiView;

    @FXML
    public void initialize() {
        gameListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (gameListView.getSelectionModel().getSelectedItem() != null) {
                    joinSelectedGame();
                }
            }
        });

        joinButton.disableProperty().bind(
                gameListView.getSelectionModel().selectedItemProperty().isNull()
        );
    }
    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    public void displayGames(ObservableList<String> games) {
        Platform.runLater(() -> {
            gameListView.setItems(games);
            infoLabel.setText("");
        });
    }


    public void showInfo(String message) {
        Platform.runLater(() -> infoLabel.setText(message));
    }

    @FXML
    public void joinSelectedGame() {
        String selectedItem = gameListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.isBlank()) {
            showInfo("Please select a game.");
            return;
        }

        try {
            int gameId = Integer.parseInt(selectedItem.split("\\.")[0].trim());
            guiView.setSelectedGameId(gameId);
        } catch (NumberFormatException e) {
            showInfo("Invalid game selection.");
        }
    }

    @FXML
    public void back() {
        try {
            guiView.setSelectedGameId(0);
            guiView.setMainScene(SceneEnum.MAIN_MENU);
        } catch (IOException e) {
            showInfo("Error returning to main menu.");
            e.printStackTrace();
        }
    }
}
//TODO fare un modo tale che quando vado indietro, mando la choice a "0" al virtual client