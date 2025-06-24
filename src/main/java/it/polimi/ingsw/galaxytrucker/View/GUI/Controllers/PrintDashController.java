package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PrintDashController extends GUIController {

    @FXML private ImageView dashboard1;
    @FXML private ImageView dashboard2;
    @FXML private GridPane gridDashboard;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setIsDemo(boolean isDemo) {
        dashboard1.setVisible(isDemo);
        dashboard2.setVisible(!isDemo);

    }

    public void loadDashboard(ClientTile[][] ship) {
        gridDashboard.getChildren().clear();

        for (int row = 0; row < ship.length; row++) {
            for (int col = 0; col < ship[0].length; col++) {
                ClientTile tile = ship[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    ImageView image = new ImageView(tile.getImage());
                    image.setPreserveRatio(true);
                    image.fitWidthProperty().bind(gridDashboard.widthProperty().divide(ship[0].length));
                    image.fitHeightProperty().bind(gridDashboard.heightProperty().divide(ship.length));
                    image.setRotate(tile.getRotation());
                    gridDashboard.add(image, col, row);
                }
            }
        }
    }

    @Override
    public void postInitialize() {

    }
}
