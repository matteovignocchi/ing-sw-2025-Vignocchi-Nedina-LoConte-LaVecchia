package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;


public class BuildingPhaseController extends GUIController {

    @FXML
    private GridPane gameGrid;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button rotateLeftBtn;
    @FXML
    private Button rotateRightBtn;

    private ImageView draggedTileView;
    private int rotationAngle = 0;

    @FXML
    public void initialize() {
        setupGridStructure();

//        rotateLeftBtn.setVisible(false);
//        rotateRightBtn.setVisible(false);
//        rotateLeftBtn.setDisable(true);
//        rotateRightBtn.setDisable(true);

        dashBoard = new ClientTile[5][7];
    }

    @FXML
    private void rotateRight() {
        if (currentTile != null) {
            rotationAngle = (rotationAngle + 90) % 360;
            refreshTileView();
        }
    }

    @FXML
    private void rotateLeft() {
        if (currentTile != null) {
            rotationAngle = (rotationAngle - 90 + 360) % 360;
            refreshTileView();
        }
    }

    public void setCurrentTile(ClientTile tile) {
        this.currentTile = tile;
        this.rotationAngle = 0;

        Platform.runLater(() -> {
            createDraggableTileView();
            updateRotationButtons();
        });
    }

    private void updateRotationButtons() {
        boolean tileSelected = (currentTile != null);

        rotateLeftBtn.setVisible(tileSelected);
        rotateRightBtn.setVisible(tileSelected);
        rotateLeftBtn.setDisable(!tileSelected);
        rotateRightBtn.setDisable(!tileSelected);

        if (tileSelected) {
            rotateLeftBtn.requestFocus();
        }
    }

    private void setupGridStructure() {
        if (!gameGrid.getColumnConstraints().isEmpty()) return;

        for (int i = 0; i < 5; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5);
            gameGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            gameGrid.getColumnConstraints().add(cc);
        }
    }

    private void createDraggableTileView() {
        if (currentTile == null) return;

        Image tileImage = ClientTile.loadImageById(currentTile.id);
        draggedTileView = new ImageView(tileImage);
        draggedTileView.setFitWidth(50);
        draggedTileView.setFitHeight(50);
        draggedTileView.setLayoutX(rootPane.getWidth() / 2);
        draggedTileView.setLayoutY(rootPane.getHeight() / 2);

        draggedTileView.setRotate(rotationAngle);

        draggedTileView.setOnMousePressed(this::onTilePressed);
        draggedTileView.setOnMouseDragged(this::onTileDragged);
        draggedTileView.setOnMouseReleased(this::onTileReleased);

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

        Point2D sceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D gridCoords = gameGrid.sceneToLocal(sceneCoords);

        if (gameGrid.getBoundsInLocal().contains(gridCoords)) {
            int col = (int)(gridCoords.getX() / (gameGrid.getWidth() / gameGrid.getColumnConstraints().size()));
            int row = (int)(gridCoords.getY() / (gameGrid.getHeight() / gameGrid.getRowConstraints().size()));

            if (col >= 0 && col < gameGrid.getColumnConstraints().size() &&
                    row >= 0 && row < gameGrid.getRowConstraints().size()) {
                placeTileOnGrid(row, col);
            }
        }

        event.consume();
    }

    private void placeTileOnGrid(int row, int col) {
        if (getNodeAt(gameGrid, col, row) != null) return;

        ImageView tileView = new ImageView(ClientTile.loadImageById(currentTile.id));
        tileView.setFitWidth(gameGrid.getWidth() / gameGrid.getColumnConstraints().size());
        tileView.setFitHeight(gameGrid.getHeight() / gameGrid.getRowConstraints().size());
        tileView.setRotate(rotationAngle);
        gameGrid.add(tileView, col, row);

        if (guiView != null) {
            guiView.resolveCoordinates(row, col);
        }

        resetTileSelection();
    }

    private Node getNodeAt(GridPane grid, int col, int row) {
        for (Node node : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            if ((nodeRow != null && nodeRow == row) && (nodeCol != null && nodeCol == col)) {
                return node;
            }
        }
        return null;
    }

    private void resetTileSelection() {
        if (draggedTileView != null) {
            rootPane.getChildren().remove(draggedTileView);
            draggedTileView = null;
        }
        currentTile = null;
        rotationAngle = 0;
        updateRotationButtons();
    }

    private void refreshTileView() {
        if (draggedTileView == null || currentTile == null) return;

        rootPane.getChildren().remove(draggedTileView);
        createDraggableTileView();
    }
}
