package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import it.polimi.ingsw.galaxytrucker.View.GUI.*;

import java.util.*;

public class BuildingPhaseController extends GUIController {

    @FXML private Button rotateLeftBtn, rotateRightBtn, getCoveredBtn, getShownBtn, returnTileBtn, setReadyBtn;
    @FXML private Button playerShip1Btn, playerShip2Btn, playerShip3Btn;
    @FXML private Button deck1Btn, deck2Btn, deck3Btn;
    @FXML private Button hourGlassBtn, logOutBtn;
    @FXML private GridPane coordinateGridPane, graphicGridPane;
    @FXML private ImageView dash, dashDemo, card0, card1, card2, card3, card4, card5;
    @FXML private HBox nicknameBox;
    @FXML private Label nicknameLabel;
    @FXML private VBox tilePreviewPane;
    @FXML private Button leftArrowButton, rightArrowButton, reserveBtn1, reserveBtn2;


    private ClientTile currentTile;
    private ImageView currentTileView;
    private int currentRotation = 0;
    private ClientTile[][] playerGrid = new ClientTile[5][7];
    private ClientTile emptySpace = new ClientTile();


    @FXML
    public void initialize() {
        emptySpace.id = 0;
        rotateLeftBtn.setOnAction(e -> {
            completeCommand("ROTATE_LEFT");
            rotateTile(-90);
        });
        rotateRightBtn.setOnAction(e -> {
            completeCommand("ROTATE_RIGHT");
            rotateTile(90);
        });
        getCoveredBtn.setOnAction(e -> {
            completeCommand("GET_COVERED");
            getShownBtn.setVisible(false);
            getCoveredBtn.setVisible(false);
            returnTileBtn.setVisible(true);
            rotateLeftBtn.setVisible(true);
            rotateRightBtn.setVisible(true);
            setReadyBtn.setVisible(false);
            reserveBtn1.setVisible(false);
            reserveBtn2.setVisible(false);
            reserveBtn1.setDisable(true);
            reserveBtn2.setDisable(true);
        });
        getShownBtn.setOnAction(e -> completeCommand("GET_SHOWN"));
        returnTileBtn.setOnAction(e -> {
            completeCommand("RETURN_TILE");
            rotateLeftBtn.setVisible(false);
            rotateRightBtn.setVisible(false);
            returnTileBtn.setVisible(false);
            setReadyBtn.setVisible(true);
            getShownBtn.setVisible(true);
            getCoveredBtn.setVisible(true);
            reserveBtn1.setVisible(true);
            reserveBtn2.setVisible(true);
            reserveBtn1.setDisable(false);
            reserveBtn2.setDisable(false);
        });
        setReadyBtn.setOnAction(e -> {
            completeCommand("READY");
            getCoveredBtn.setVisible(false);
            getShownBtn.setVisible(false);
            setReadyBtn.setVisible(false);
        });
        hourGlassBtn.setOnAction(e -> completeCommand("SPIN_HOURGLASS"));
        logOutBtn.setOnAction(e -> completeCommand("LOGOUT"));

        playerShip1Btn.setOnAction(e -> completeCommand("LOOK_PLAYER1"));
        playerShip2Btn.setOnAction(e -> completeCommand("LOOK_PLAYER2"));
        playerShip3Btn.setOnAction(e -> completeCommand("LOOK_PLAYER3"));
        reserveBtn1.setOnAction(e -> takeReserved(1));
        reserveBtn2.setOnAction(e -> takeReserved(2));
        deck1Btn.setOnAction(e -> completeIndex(0));
        deck2Btn.setOnAction(e -> completeIndex(1));
        deck3Btn.setOnAction(e -> completeIndex(2));

        setupCoordinateGridClickHandler();
    }

    @Override
    public void postInitialize() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                ClientTile tile = model.getDashboard()[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    placeTileAt(tile, row, col);
                }
            }
        }

        clearCurrentTile(); // <-- rimuove tile trascinabile se c'era

        // Mostra pulsanti per BOARD_SETUP
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);
        getShownBtn.setVisible(true);
        getCoveredBtn.setVisible(true);
        setReadyBtn.setVisible(true);
        rightArrowButton.setVisible(false);
        leftArrowButton.setVisible(false);

        setNickname(model.getNickname());
        setCommandVisibility(model.isDemo());
    }


    public void postInitialize2(){
        getShownBtn.setVisible(false);
        getCoveredBtn.setVisible(false);
        returnTileBtn.setVisible(true);
        rotateLeftBtn.setVisible(true);
        rotateRightBtn.setVisible(true);
        setReadyBtn.setVisible(false);
    }

    public void postInitialize3(){
        getShownBtn.setVisible(false);
        getCoveredBtn.setVisible(false);
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(true);
        rotateRightBtn.setVisible(true);
        setReadyBtn.setVisible(false);

    }



    private void setupCoordinateGridClickHandler() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                final int r = row;
                final int c = col;

                Button tileButton = new Button();
                tileButton.setStyle("-fx-background-color: transparent;");
                tileButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                tileButton.setOnDragOver(event -> {
                    if (event.getGestureSource() != tileButton && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        tileButton.setStyle("-fx-background-color: rgba(0,255,0,0.3);");
                    }
                    event.consume();
                });

                tileButton.setOnDragExited(e -> tileButton.setStyle("-fx-background-color: transparent;"));

                tileButton.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString() && db.getString().equals("tile")) {
                        Boolean[][] mask = model.getMask();

                        if (mask == null || !Boolean.TRUE.equals(mask[r][c])) {
                            guiView.reportError("Invalid position. Try again.");
                            tileButton.setStyle("-fx-background-color: transparent;");
                            event.setDropCompleted(false);
                            return;
                        }

                        // Salva coordinate per quando askCoordinate sarà chiamato
                        guiView.setBufferedCoordinate(new int[]{r, c});

                        guiView.resolveGenericCommand("PLACE_TILE");
                        if(!model.isDemo()){
                            reserveBtn1.setVisible(true);
                            reserveBtn2.setVisible(true);
                            reserveBtn1.setDisable(false);
                            reserveBtn2.setDisable(false);
                        }


                        // Pulisce preview e resetta
                        tilePreviewPane.getChildren().clear();
                        currentTileView = null;

                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(false);
                    }
                    event.consume();
                });

                coordinateGridPane.add(tileButton, c, r);
            }
        }
    }

    private void rotateTile(int angle) {
        if (!inputManager.rotationFuture.isDone()) {
            inputManager.rotationFuture.complete(currentRotation % 360);
        }
    }

    public void showCurrentTile(ClientTile tile) {
        currentTile = tile;
        currentRotation = tile.getRotation();

        Platform.runLater(() -> {
            currentTileView = new ImageView(tile.getImage());
            currentTileView.setFitWidth(100);
            currentTileView.setFitHeight(100);
            currentTileView.setRotate(currentRotation);

            currentTileView.setOnDragDetected(event -> {
                returnTileBtn.setVisible(false);
                Dragboard db = currentTileView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("tile");
                db.setContent(content);
                db.setDragView(currentTileView.snapshot(null, null));                System.out.println("ci siamo arrivati");
                System.out.println("ci siamo arrivati prade");


                event.consume();
                System.out.println("ci siamo arrivati");


            });

            tilePreviewPane.getChildren().setAll(currentTileView);
        });
    }

    private void completeCommand(String command) {
        guiView.resolveGenericCommand(command);
    }

    private void completeIndex(int index) {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(index);
        }
    }

    public void placeTileAt(ClientTile tile, int row, int col) {
        Platform.runLater(() -> {
            graphicGridPane.getChildren().removeIf(node -> {
                Integer nodeRow = GridPane.getRowIndex(node);
                Integer nodeCol = GridPane.getColumnIndex(node);
                return nodeRow != null && nodeCol != null && nodeRow == row && nodeCol == col;
            });

            ImageView tileImage = new ImageView(tile.getImage());
            tileImage.setFitWidth(70);
            tileImage.setFitHeight(70);
            tileImage.setRotate(tile.getRotation());
            graphicGridPane.add(tileImage, col, row);
        });
    }

    public void clearCurrentTile() {
        Platform.runLater(() -> {
            tilePreviewPane.getChildren().clear();
            currentTile = null;
        });
    }

    public void setNickname(String nickname) {
        Platform.runLater(() -> nicknameLabel.setText(nickname));
    }

    private void setCommandVisibility(Boolean demo) {
        playerShip1Btn.setVisible(false);
        playerShip2Btn.setVisible(false);
        playerShip3Btn.setVisible(false);
        if (demo) {
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
            reserveBtn1.setVisible(true);
            reserveBtn2.setVisible(true);

        }
        setPlayersButton();
    }

    private void setPlayersButton() {
        Map<String, int[]> mapPosition = model.getPlayerPositions();
        List<String> others = mapPosition.keySet().stream()
                .filter(name -> !name.equals(model.getNickname())).toList();

        switch (others.size()) {
            case 1 -> {
                playerShip1Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of " + others.getFirst());
            }
            case 2 -> {
                playerShip2Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of " + others.getFirst());
                playerShip3Btn.setVisible(true);
                playerShip3Btn.setText("Player Ship of " + others.getLast());
            }
            case 3 -> {
                playerShip1Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of " + others.getFirst());
                playerShip2Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of " + others.get(1));
                playerShip3Btn.setVisible(true);
                playerShip3Btn.setText("Player Ship of " + others.getLast());
            }
        }
    }
    public void postInitializeLogOut(){
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);
        getShownBtn.setVisible(false);
        getCoveredBtn.setVisible(false);
        setReadyBtn.setVisible(false);
        rightArrowButton.setVisible(false);
        leftArrowButton.setVisible(false);
        playerShip1Btn.setVisible(false);
        playerShip2Btn.setVisible(false);
        playerShip3Btn.setVisible(false);
        deck1Btn.setDisable(true);
        deck2Btn.setDisable(true);
        deck3Btn.setDisable(true);
        hourGlassBtn.setVisible(false);
    }
    private void takeReserved(int a){

        switch(a){
            case 1 ->{
                if(!guiView.returnValidity(0,5)){
                    guiView.setBufferedCoordinate(new int[]{0, 5});
                    completeCommand("RESERVE_TILE");
                    placeTileAt(emptySpace,0,5);
                }
            }
            case 2 ->{
                if(!guiView.returnValidity(0,6)){
                    guiView.setBufferedCoordinate(new int[]{0, 6});
                    completeCommand("RESERVE_TILE");
                    placeTileAt(emptySpace,0,6);
                }
            }
            default ->{}
        }
    }
}