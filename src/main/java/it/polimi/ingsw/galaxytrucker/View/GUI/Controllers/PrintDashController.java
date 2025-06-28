package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Controller for the dashboard display scene in the GUI.
 * Manages visualization of the player's ship dashboard using a grid pane,
 * supports switching between demo and normal dashboard images,
 * and handles closing the dashboard window.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */
public class PrintDashController extends GUIController {

    @FXML private ImageView dashboard1;
    @FXML private ImageView dashboard2;
    @FXML private GridPane gridDashboard;
    @FXML private Button closeButton;

    /**
     * Initializes the dashboard scene.
     * Sets the action handler for the close button to close the current window.
     */
    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Sets the visibility of demo and normal dashboard images based on the demo flag.
     * @param isDemo true to show the demo dashboard, false to show the normal dashboard
     */
    public void setIsDemo(boolean isDemo) {
        dashboard1.setVisible(isDemo);
        dashboard2.setVisible(!isDemo);

    }

    /**
     * Loads and displays the given ship dashboard tiles in the grid pane.
     * Clears existing grid children and adds image views for each non-empty tile.
     * Binds the image size to the grid cell size and applies tile rotation.
     * @param ship 2D array of ClientTile representing the ship layout
     */
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

    /**
     * Empty override of postInitialize; no additional setup required after scene load.
     */
    @Override
    public void postInitialize() {}
}
