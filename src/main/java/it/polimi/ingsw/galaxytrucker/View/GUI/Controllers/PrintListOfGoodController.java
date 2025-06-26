
package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class PrintListOfGoodController extends GUIController {

    @FXML private Pane goodPane;
    @FXML private Button addGood, rearranges, trash, leftBtn, rightBtn;
    private int currentGoodIndex = 0;
    private List<String> loadedGoods;
    private Stage ownStage;


    public void setupForActionSelection(GUIView gui) {
        goodPane.setVisible(false);
        addGood.setVisible(true);
        rearranges.setVisible(true);
        trash.setVisible(true);
        leftBtn.setVisible(false);
        rightBtn.setVisible(false);

        addGood.setOnAction(e -> {
            gui.setBufferedIndex(0);
            closeWindow();
        });
        rearranges.setOnAction(e -> {
            gui.setBufferedIndex(1);
            closeWindow();
        });
        trash.setOnAction(e -> {
            gui.setBufferedIndex(2);
            closeWindow();
        });
    }

    public void setupForGoodsIndexSelection() {
        goodPane.setVisible(true);
        addGood.setVisible(false);
        rearranges.setVisible(false);
        trash.setVisible(false);
        leftBtn.setVisible(true);
        rightBtn.setVisible(true);
    }

    public void loadGoods(List<String> goods, GUIView guiView) {
        this.loadedGoods = goods;
        this.currentGoodIndex = 0;
        showGood(guiView);
    }

    public void showGood(GUIView gui) {
        goodPane.getChildren().clear();

        if (loadedGoods == null || loadedGoods.isEmpty()) return;

        String color = loadedGoods.get(currentGoodIndex).toUpperCase();
        String path = switch (color) {
            case "BLUE" -> "/images/BlueGood.png";
            case "RED" -> "/images/RedGood.png";
            case "GREEN" -> "/images/GreenGood.png";
            case "YELLOW" -> "/images/YellowGood.png";
            default -> "/images/placeholder.png";
        };

        ImageView iv = new ImageView(new Image(getClass().getResource(path).toExternalForm()));

        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setCursor(Cursor.HAND); // 👈 mostra la "manina"

        iv.fitWidthProperty().bind(goodPane.widthProperty());
        iv.fitHeightProperty().bind(goodPane.heightProperty());

        iv.setOnMouseClicked(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), iv);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(0.85);
            st.setToY(0.85);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.setOnFinished(ev -> {
                if (gui != null) gui.setBufferedIndex(currentGoodIndex);
                closeWindow();
            });
            st.play();
        });

        goodPane.getChildren().add(iv);
    }

    public void configureNavigation(GUIView gui) {
        leftBtn.setVisible(true);
        rightBtn.setVisible(true);

        leftBtn.setOnAction(e -> {
            currentGoodIndex = (currentGoodIndex - 1 + loadedGoods.size()) % loadedGoods.size();
            showGood(gui);
        });

        rightBtn.setOnAction(e -> {
            currentGoodIndex = (currentGoodIndex + 1) % loadedGoods.size();
            showGood(gui);
        });
    }

    private void closeWindow() {
        if (ownStage != null) {
            ownStage.close();
        } else {
            System.err.println("Stage non settato! Impossibile chiudere finestra.");
        }
    }
    public void reset() {
        addGood.setVisible(false);
        rearranges.setVisible(false);
        trash.setVisible(false);
        leftBtn.setVisible(false);
        rightBtn.setVisible(false);

        for (Node node : goodPane.getChildren()) {
            node.setOnMouseClicked(null);
        }
    }

    public void setStage(Stage stage) {
        this.ownStage = stage;
    }

    @Override
    public void postInitialize() {}
}