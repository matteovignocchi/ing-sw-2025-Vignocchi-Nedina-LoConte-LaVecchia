package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * Controller for the GUI scene that displays decks of cards.
 * Manages three panes (left, center, right) to show cards visually.
 * Connects GUI components with the underlying GUI model and input manager.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class PrintDeckController extends GUIController{

    @FXML public Pane leftPane;
    @FXML public Pane centerPane;
    @FXML public Pane rightPane;

    /**
     * Empty override of postInitialize; no additional setup required after scene load.
     */
    @Override
    public void postInitialize() {}
}
