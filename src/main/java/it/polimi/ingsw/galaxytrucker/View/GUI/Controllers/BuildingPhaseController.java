package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.SecondaryPhase;
import javafx.application.Platform;
import javafx.event.Event;
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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


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
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);
        dashBoard = new ClientTile[5][7];
    }

    @FXML
    public void onButtonClick(javafx.event.ActionEvent event) {
        super.onButtonClick(event);
    }

    public void postInitialize() {
        Boolean isDemo = getIsDemo();
        playerShip1Btn.setVisible(false);
        playerShip2Btn.setVisible(false);
        playerShip3Btn.setVisible(false);

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
        setVisibleBottom();

    }

    @Override
    public void setTileInDash(ClientTile tile, int row, int col) {
        dashBoard[row][col] = tile;
        if(numeberOfTile == 0){
            numeberOfTile ++;
            placeStartingTile(tile);
        }
        ImageView tileView = new ImageView(ClientTile.loadImageById(tile.id));
        tileView.setFitWidth(graphicGridPane.getWidth() / 7);
        tileView.setFitHeight(graphicGridPane.getHeight() / 5);
        tileView.setRotate(tile.getRotation());

        graphicGridPane.add(tileView, row, col);

    }

    public void placeStartingTile(ClientTile startingTile) {
        dashBoard[2][3] = startingTile;

        ImageView tileView = new ImageView(ClientTile.loadImageById(startingTile.id));
        tileView.setFitWidth(graphicGridPane.getWidth() / 7);
        tileView.setFitHeight(graphicGridPane.getHeight() / 5);
        tileView.setRotate(startingTile.getRotation());

        graphicGridPane.add(tileView, 3, 2); // col = 3, row = 2
    }

    public void setVisibleBottom(){
        int size = mapPosition.size();
        List<String> tmpString = new ArrayList<>();
        for (String s : mapPosition.keySet()) {
            tmpString.add(s);
        }
        List<String> tmpString2 = new ArrayList<>();
        for(int i = 0; i < size; i++){
            if(!tmpString.get(i).equals(getNickname())){
                tmpString2.add(tmpString.get(i));
            }
        }
        switch(size){
            case 0 -> guiView.inform("piedino pradella");
            case 1 -> guiView.inform("piedino pradella");
            case 2 -> {
                playerShip1Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of "+ tmpString2.getFirst());
            }
            case 3 -> {
                playerShip2Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of "+ tmpString2.getFirst());
                playerShip3Btn.setVisible(true);
                playerShip3Btn.setText("Player Ship of "+ tmpString2.getLast());
            }
            case 4 -> {
                playerShip1Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of "+ tmpString2.getFirst());
                playerShip2Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of "+ tmpString2.get(1));
                playerShip3Btn.setVisible(true);
                playerShip3Btn.setText("Player Ship of "+ tmpString2.getLast());
            }
        }

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

    private void onTileReleased(MouseEvent event) {    draggedTileView.setMouseTransparent(false);
        draggedTileView.setOpacity(1.0);

        Point2D sceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D gridCoords = coordinateGridPane.sceneToLocal(sceneCoords);

        if (coordinateGridPane.getBoundsInLocal().contains(gridCoords)) {
            int col = (int)(gridCoords.getX() / (coordinateGridPane.getWidth() / coordinateGridPane.getColumnConstraints().size()));
            int row = (int)(gridCoords.getY() / (coordinateGridPane.getHeight() / coordinateGridPane.getRowConstraints().size()));

            if (col >= 0 && col < 7 && row >= 0 && row < 5) {
                // Salva tile + rotazione + coordinate
                super.row = row;
                super.col = col;
                super.currentRotation = rotationAngle;

                // Manda solo la tile al server
                guiView.setSecondaryPhase(SecondaryPhase.COORDINATE_FOR_PLACING);
                guiView.sendTileToServer(currentTile, currentRotation);
            }
        }

        event.consume();
    }

    public void redrawDashboard() {
        coordinateGridPane.getChildren().clear();

        for (int i = 0; i < dashBoard.length; i++) {
            for (int j = 0; j < dashBoard[0].length; j++) {
                ClientTile tile = dashBoard[i][j];
                if (tile != null && tile.id != 0) {
                    ImageView tileView = new ImageView(ClientTile.loadImageById(tile.id));
                    tileView.setFitWidth(coordinateGridPane.getWidth() / 7);
                    tileView.setFitHeight(coordinateGridPane.getHeight() / 5);
                    tileView.setRotate(tile.getRotation());
                    coordinateGridPane.add(tileView, j, i);
                }
            }
        }
    }

    @Override
    public void placeTileOnGrid(int row, int col) {
        if (getNodeAt(coordinateGridPane, col, row) != null) return;
        if (dashBoard[row][col] != null && dashBoard[row][col].id != 0) return;
        if(maschera[row][col] != true) return;
        super.row = row;
        super.col = col;
        super.currentRotation = rotationAngle;
        if (guiView != null) {
            guiView.sendTileToServer(currentTile, rotationAngle);
        }
        resetTileSelection();
    }
    public void drawTileAt(ClientTile tile, int row, int col) {
        ImageView tileView = new ImageView(ClientTile.loadImageById(tile.id));
        tileView.setFitWidth(coordinateGridPane.getWidth() / 7);
        tileView.setFitHeight(coordinateGridPane.getHeight() / 5);
        coordinateGridPane.add(tileView, col, row);
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
