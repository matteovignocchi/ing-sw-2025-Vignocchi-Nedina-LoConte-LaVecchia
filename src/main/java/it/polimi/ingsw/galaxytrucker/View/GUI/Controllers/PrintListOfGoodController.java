package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;


public class PrintListOfGoodController extends GUIController{

    @FXML private Pane goodPane;
    @FXML private Button addGood;
    @FXML private Button rearranges;
    @FXML private Button trash;
    @FXML private Button leftBtn;
    @FXML private Button rightBtn;

    public void setupForActionSelection(GUIView gui) {
        addGood.setVisible(true);
        rearranges.setVisible(true);
        trash.setVisible(true);

        addGood.setOnAction(e -> gui.setBufferedIndex(0));
        rearranges.setOnAction(e -> gui.setBufferedIndex(1));
        trash.setOnAction(e -> gui.setBufferedIndex(2));
    }

    public void setupForGoodsIndexSelection(GUIView gui) {
        leftBtn.setVisible(true);
        rightBtn.setVisible(true);

        for (int i = 0; i < goodPane.getChildren().size(); i++) {
            Node node = goodPane.getChildren().get(i);
            int index = i;
            node.setOnMouseClicked(e -> gui.setBufferedIndex(index));
        }
    }

    public void loadGoods(List<String> goods) {
        goodPane.getChildren().clear();

        double paneWidth = goodPane.getPrefWidth();
        double paneHeight = goodPane.getPrefHeight();

        int max = goods.size();
        double imageSize = Math.min(paneWidth / max, paneHeight); // dimensione massima disponibile

        for (int i = 0; i < max; i++) {
            String color = goods.get(i).toUpperCase();

            ImageView goodImage = new ImageView(getImageForGood(color));
            goodImage.setPreserveRatio(true);
            goodImage.setFitWidth(imageSize);
            goodImage.setFitHeight(imageSize);

            StackPane wrapper = new StackPane(goodImage);
            wrapper.setPrefSize(imageSize, imageSize);
            wrapper.setLayoutX(i * imageSize);
            wrapper.setLayoutY((paneHeight - imageSize) / 2); // centrato verticalmente

            goodPane.getChildren().add(wrapper);
        }
    }

    private String getImageForGood(String color) {
        return switch (color) {
            case "BLUE" -> "/BluGood.png";
            case "RED" -> "/RedGood.png";
            case "GREEN" -> "/GreenGood.png";
            case "YELLOW" -> "/YellowGood.png";
            default -> "/UnknownGood.png";
        };
    }


    @Override
    public void postInitialize() {

    }
}
