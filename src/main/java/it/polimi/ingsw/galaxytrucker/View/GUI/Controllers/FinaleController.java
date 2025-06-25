package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Map;

public class FinaleController extends GUIController {

    @FXML private Label playerNameLabel;
    @FXML private Label playerPointsLabel;
    @FXML private Label playerCargoLabel;

    @FXML private Label opponent1Label;
    @FXML private Label opponent2Label;
    @FXML private Label opponent3Label;

    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        logoutButton.setText("Logout");
        logoutButton.setOnAction(e -> guiView.resolveGenericCommand("logout"));
    }

    private void updateFinalScreen() {
        String me = model.getNickname();
        playerNameLabel.setText(me);
        playerPointsLabel.setText("Crediti: " + model.credits);
        playerCargoLabel.setText("Position: " + model.getMyPosition(me));

        // Avversari
        int index = 0;
        for (Map.Entry<String, int[]> entry : model.getPlayerPositions().entrySet()) {
            String name = entry.getKey();
            if (name.equals(me)) continue;

            int[] pos = entry.getValue();
            String line = name + " → Position: " + pos[0];
            switch (index++) {
                case 0 -> opponent1Label.setText(line);
                case 1 -> opponent2Label.setText(line);
                case 2 -> opponent3Label.setText(line);
            }
        }
    }


    @Override
    public void postInitialize() {
        updateFinalScreen();

    }
}

