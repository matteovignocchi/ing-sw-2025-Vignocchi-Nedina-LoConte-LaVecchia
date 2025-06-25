package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.Node;


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

    @Override
    public void postInitialize() {

    }
}
