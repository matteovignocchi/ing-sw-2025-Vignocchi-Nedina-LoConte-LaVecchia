package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class GameListMenuController extends GUIController {

    @FXML private Button backButton;
    @FXML private Button joinButton;
    @FXML private ListView<String> gameListView;
    @FXML private Label infoLabel;

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

    @Override
    public void postInitialize() {
        // Potresti voler ricaricare la lista qui
        ObservableList<String> testItems = FXCollections.observableArrayList();
        testItems.add("1. Players: 2 | Demo: Yes");
        testItems.add("2. Players: 3 | Demo: No");
        displayGames(testItems);
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
            guiView.reportError("Please select a game.");
            return;
        }

        try {
            int gameId = Integer.parseInt(selectedItem.split("\\.")[0].trim()) - 1;

            if (!inputManager.indexFuture.isDone()) {
                inputManager.indexFuture.complete(gameId);  // ✅ sblocca askIndex()
            }

        } catch (NumberFormatException e) {
            guiView.reportError("Invalid game selection.");
        }
    }

    @FXML
    public void back() {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(-1);  // annullamento
        }
        Platform.runLater(() -> guiView.setSceneEnum(SceneEnum.MAIN_MENU));
    }
}
