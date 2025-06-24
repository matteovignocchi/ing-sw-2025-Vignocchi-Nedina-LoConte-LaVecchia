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
    @FXML private GridPane griddashboard;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setIsDemo(boolean isDemo) {
        // Mostra solo l'immagine corretta
        dashboard1.setVisible(!isDemo);
        dashboard2.setVisible(isDemo);

        // Imposta immagini (puoi usare getClass().getResourceAsStream anche qui se preferisci)
        dashboard1.setImage(new Image(getClass().getResourceAsStream("/Polytechnic/cardboard/cardboard-1.jpg")));
        dashboard2.setImage(new Image(getClass().getResourceAsStream("/Polytechnic/cardboard/cardboard-1b.jpg")));
    }

    public void loadDashboard(ClientTile[][] ship) {
        griddashboard.getChildren().clear();

        for (int row = 0; row < ship.length; row++) {
            for (int col = 0; col < ship[0].length; col++) {
                ClientTile tile = ship[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    ImageView image = new ImageView(tile.getImage());
                    image.setFitWidth(70);
                    image.setFitHeight(70);
                    image.setRotate(tile.getRotation());
                    griddashboard.add(image, col, row);
                }
            }
        }
    }

    @Override
    public void postInitialize() {

    }
}
