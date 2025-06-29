package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controller for the GUI scene that displays a list of available games to join.
 * Manages user interactions for selecting and joining a game,
 * and allows navigating back to the main menu.
 * @author Matteo Vignocchi
 */
public class GameListMenuController extends GUIController {

    @FXML private Button backButton;
    @FXML private Button joinButton;
    @FXML private ListView<String> gameListView;
    @FXML private Label infoLabel;

    /**
     * Initializes the game list scene.
     * Sets up double-click event on the list view to join a selected game,
     * and binds the join button's disabled state to whether a game is selected.
     */
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

    /**
     * Empty override for post-initialization, no additional setup required.
     */
    @Override
    public void postInitialize() {
    }

    /**
     * Updates the list view with the given list of game descriptions.
     * Clears any informational label text.
     * @param games the observable list of game descriptions to display
     */
    public void displayGames(ObservableList<String> games) {
        Platform.runLater(() -> {
            gameListView.setItems(games);
            infoLabel.setText("");
        });
    }

    /**
     * Attempts to join the currently selected game.
     * Parses the selected game's ID from the string,
     * completes the input manager's index future with the game ID,
     * or reports an error if selection is invalid.
     */
    @FXML
    public void joinSelectedGame() {
        String selectedItem = gameListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.isBlank()) {
            guiView.reportError("Please select a game.");
            return;
        }

        try {
            int gameId = Integer.parseInt(selectedItem.split("\\.")[0].trim())+1;

            if (!inputManager.indexFuture.isDone()) {
                inputManager.indexFuture.complete(gameId);
            }

        } catch (NumberFormatException e) {
            guiView.reportError("Invalid game selection.");
        }
    }

    /**
     * Cancels the game join selection by completing the input manager's index future with -1.
     * Returns to the main menu scene.
     */
    @FXML
    public void back() {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(-1);
        }
        Platform.runLater(() -> guiView.setSceneEnum(SceneEnum.MAIN_MENU));
    }
}
