package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import javax.annotation.processing.Generated;


public class BuildingPhaseController extends GUIController {

    @FXML
    private GridPane coordinateGridPane;
    @FXML
    private GridPane graphicGridPane;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button rotateLeftBtn;
    @FXML
    private Button rotateRightBtn;
    @FXML
    private Button hourGlassBtn;
    @FXML
    private Button returnTileBtn;
    @FXML
    private Button getCoveredBtn;
    @FXML
    private Button getShownBtn;
    @FXML
    private Button setReadyBtn;
    @FXML
    private Button logOutBtn;
    @FXML
    private Button deck1Btn;
    @FXML
    private Button deck2Btn;
    @FXML
    private Button deck3Btn;
    @FXML
    private Button playerShip1Btn;
    @FXML
    private Button playerShip2Btn;
    @FXML
    private Button playerShip3Btn;
    @FXML
    private ImageView dashDemo;
    @FXML
    private ImageView dash;
    @FXML
    private ImageView card0;
    @FXML
    private ImageView card1;
    @FXML
    private ImageView card2;
    @FXML
    private ImageView card3;
    @FXML
    private ImageView card4;
    @FXML
    private ImageView card5;
    @FXML
    private Label nicknameLabel;



    private ImageView draggedTileView;

    private int rotationAngle = 0;

    @FXML
    public void initialize() {
        setupGridStructure();


        dashBoard = new ClientTile[5][7];
    }

    public void postInitialize() {
        Boolean isDemo = getIsDemo();

        if (isDemo) {
            dash.setVisible(false);
            dashDemo.setVisible(true);
            hourGlassBtn.setVisible(false);
            deck1Btn.setVisible(false);
            deck2Btn.setVisible(false);
            deck3Btn.setVisible(false);
            card0.setVisible(false);
            card1.setVisible(false);
            card2.setVisible(false);
            card3.setVisible(false);
            card4.setVisible(false);
            card5.setVisible(false);
        } else {
            dash.setVisible(true);
            dashDemo.setVisible(false);
            hourGlassBtn.setVisible(true);
            deck1Btn.setVisible(true);
            deck2Btn.setVisible(true);
            deck3Btn.setVisible(true);
        }
        setNickName(getNickname());

    }

    public void setNickName(String nickName) {
        nicknameLabel.setText(nickName);
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
        if (!coordinateGridPane.getColumnConstraints().isEmpty()) return;

        for (int i = 0; i < 5; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5);
            coordinateGridPane.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            coordinateGridPane.getColumnConstraints().add(cc);
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
        if (event.getTarget() != draggedTileView) {
            event.consume();
            return;
        }
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
        Point2D gridCoords = coordinateGridPane.sceneToLocal(sceneCoords);

        if (coordinateGridPane.getBoundsInLocal().contains(gridCoords)) {
            int col = (int)(gridCoords.getX() / (coordinateGridPane.getWidth() / coordinateGridPane.getColumnConstraints().size()));
            int row = (int)(gridCoords.getY() / (coordinateGridPane.getHeight() / coordinateGridPane.getRowConstraints().size()));

            if (col >= 0 && col < coordinateGridPane.getColumnConstraints().size() &&
                    row >= 0 && row < coordinateGridPane.getRowConstraints().size()) {
                placeTileOnGrid(row, col);
            }
        }

        event.consume();
    }

    private void placeTileOnGrid(int row, int col) {
        if (getNodeAt(coordinateGridPane, col, row) != null) return;

        ImageView tileView = new ImageView(ClientTile.loadImageById(currentTile.id));
        tileView.setFitWidth(coordinateGridPane.getWidth() / coordinateGridPane.getColumnConstraints().size());
        tileView.setFitHeight(coordinateGridPane.getHeight() / coordinateGridPane.getRowConstraints().size());
        tileView.setRotate(rotationAngle);
        coordinateGridPane.add(tileView, col, row);

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
