package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class GameListMenuController extends GUIController{
    @FXML
    private Button backButton;

    @FXML
    private ListView<String> gameListView;

    @FXML
    public void initialize() {
        gameListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedItem = gameListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    int gameId = Integer.parseInt(selectedItem.split("\\.")[0]);
                    handleGameSelection(gameId);
                }
            }
        });
    }

    public void displayGames(ObservableList<String> games) {
        Platform.runLater(() -> gameListView.setItems(games));
    }

    private void handleGameSelection(int index) {
        guiView.resolveGameChoice(index);
    }

    @FXML
    public void back() throws IOException {
        guiView.setMainScene(SceneEnum.MAIN_MENU);
    }

}

