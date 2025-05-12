package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.List;

public class BuildingPhaseController extends GUIController{

    @FXML
    private GridPane gameGrid;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button rotateLeftBtn;
    @FXML
    private Button rotateRightBtn;

    private Tile currentTile;
    private Player model;
    private VirtualView client;
    private ImageView draggedTileView;
    private GridPane gridPane;
    private int rotationAngle = 0;


    @FXML
    private void rotateRight() {
        if (currentTile != null) {
            currentTile.rotateRight();
            refreshTileView();
        }
    }

    @FXML
    private void rotateLeft() {
        if (currentTile != null) {
            currentTile.rotateLeft();
            refreshTileView();
        }
    }



    public void setCurrentTile(Tile tile) {
        this.currentTile = tile;
        createDraggableTileView();
        setupSingleGridPane();
    }

    private void setupSingleGridPane() {
        gameGrid.getRowConstraints().clear();
        gameGrid.getColumnConstraints().clear();

        // Configurazione per griglia 5x7 (come da tuo requisito iniziale)
        for (int i = 0; i < 5; i++) { // 5 righe
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5);
            gameGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 7; i++) { // 7 colonne
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            gameGrid.getColumnConstraints().add(cc);
        }
    }

    public void setModel(Player model) {
        this.model = model;
    }

    public void setClient(VirtualView client) {
        this.client = client;
    }
    private void createDraggableTileView() {
        if (currentTile == null) return;
        Image tileImage = Tile.loadImageById(currentTile.getIdTile());


        draggedTileView = new ImageView(tileImage);
        draggedTileView.setFitWidth(50);
        draggedTileView.setFitHeight(50);
        draggedTileView.setLayoutX(rootPane.getWidth() / 2);
        draggedTileView.setLayoutY(rootPane.getHeight() / 2);

        // Imposta il comportamento di trascinamento
        draggedTileView.setOnMousePressed(this::onTilePressed);
        draggedTileView.setOnMouseDragged(this::onTileDragged);
        draggedTileView.setOnMouseReleased(this::onTileReleased);

        // Aggiungi al root pane
        rootPane.getChildren().add(draggedTileView);
    }

    private void onTilePressed(MouseEvent event) {
        draggedTileView.setMouseTransparent(true);
        draggedTileView.setOpacity(0.7);
        event.consume();
    }
    private void onTileDragged(MouseEvent event) {
        draggedTileView.setX(event.getSceneX() - draggedTileView.getFitWidth() / 2);
        draggedTileView.setY(event.getSceneY() - draggedTileView.getFitHeight() / 2);
        event.consume();
    }
    private void onTileReleased(MouseEvent event) {
        draggedTileView.setMouseTransparent(false);
        draggedTileView.setOpacity(1.0);

        // Trova la griglia sotto il cursore
        Point2D sceneCoords = new Point2D(event.getSceneX(), event.getSceneY());


        Point2D gridCoords = gridPane.sceneToLocal(sceneCoords);
        if (gridPane.boundsInLocalProperty().get().contains(gridCoords)) {
                // Calcola la cella
                int column = (int)(gridCoords.getX() / (gridPane.getWidth() / gridPane.getColumnConstraints().size()));
                int row = (int)(gridCoords.getY() / (gridPane.getHeight() / gridPane.getRowConstraints().size()));

                // Verifica che la posizione sia valida
                if (column >= 0 && column < gridPane.getColumnConstraints().size() &&
                        row >= 0 && row < gridPane.getRowConstraints().size()) {

                    placeTileOnGrid(gridPane, row, column);

                }
            }

        event.consume();
    }

    private void placeTileOnGrid(GridPane gridPane, int row, int column) {

        // Crea una nuova ImageView per la tile posizionata
        ImageView tileView = new ImageView(Tile.loadImageById(currentTile.getIdTile()));
        tileView.setFitWidth(gridPane.getWidth() / gridPane.getColumnConstraints().size());
        tileView.setFitHeight(gridPane.getHeight() / gridPane.getRowConstraints().size());
        tileView.setRotate(rotationAngle);

        gridPane.add(tileView, column, row);

        // Notifica al model il posizionamento
        if (model != null) {
                model.addTile(row,column,currentTile);
        }
        resetTileSelection();
    }


    private void resetTileSelection() {
        if (draggedTileView != null) {
            rootPane.getChildren().remove(draggedTileView);
            draggedTileView = null;
        }
        currentTile = null;
        rotationAngle = 0;
    }


    private void refreshTileView() {
        if (draggedTileView == null || currentTile == null) return;


        rootPane.getChildren().remove(draggedTileView);
        createDraggableTileView();
    }

}
