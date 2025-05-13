package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.fxml.FXML;

import java.io.IOException;

public class CreateGameMenuController extends GUIController {
    @FXML
    public void back() throws IOException {
        ((GUIView) ClientController.getInstance().getViewInterface()).setMainScene(SceneEnum.MAIN_MENU);
    }

}
