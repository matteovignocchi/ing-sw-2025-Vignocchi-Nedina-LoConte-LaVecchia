package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIModel;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BuildingPhaseController extends GUIController {

    @FXML private Button rotateLeftBtn;
    @FXML private Button rotateRightBtn;
    @FXML private Button getCoveredBtn;
    @FXML private Button getShownBtn;
    @FXML private Button returnTileBtn;
    @FXML private Button setReadyBtn;
    @FXML private Button playerShip1Btn, playerShip2Btn, playerShip3Btn;
    @FXML private Button deck1Btn, deck2Btn, deck3Btn;
    @FXML private Button hourGlassBtn;
    @FXML private Button logOutBtn;
    @FXML private GridPane coordinateGridPane;
    @FXML private GridPane graphicGridPane;
    @FXML private ImageView dash;
    @FXML private ImageView dashDemo;
    @FXML private ImageView card0;
    @FXML private ImageView card1;
    @FXML private ImageView card2;
    @FXML private ImageView card3;
    @FXML private ImageView card4;
    @FXML private ImageView card5;
    @FXML private HBox nicknameBox;
    @FXML private Label nicknameLabel;
    @FXML private VBox tilePreviewPane;
    @FXML private Button leftArrowButton;
    @FXML private Button rightArrowButton;
    @FXML private ImageView currentTileView;


    private ClientTile currentTile;
    private ClientTile[][] playerGrid = new ClientTile[5][7];
    private int currentRotation = 0; // 0, 90, 180, 270

    @FXML
    public void initialize() {
        rotateLeftBtn.setOnAction(e -> rotateTile(-90));
        rotateRightBtn.setOnAction(e -> rotateTile(90));

        getCoveredBtn.setOnAction(e -> {
                completeCommand("GET_COVERED");
                getShownBtn.setVisible(false);
                getCoveredBtn.setVisible(false);
                returnTileBtn.setVisible(true);
                rotateLeftBtn.setVisible(true);
                rotateRightBtn.setVisible(true);
                setReadyBtn.setVisible(false);
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

        deck1Btn.setOnAction(e -> completeIndex(0));
        deck2Btn.setOnAction(e -> completeIndex(1));
        deck3Btn.setOnAction(e -> completeIndex(2));

        setupCoordinateGridClickHandler();
    }



    @Override
    public void postInitialize() {

        ClientTile[][] ship = model.getDashboard();
        for (int row = 0; row < ship.length; row++) {
            for (int col = 0; col < ship[row].length; col++) {
                ClientTile tile = ship[row][col];
                if (tile != null && !tile.type.equals("EMPTYSPACE")) {
                    placeTileAt(tile, row, col);
                }
            }
        }


        ClientTile current = model.getCurrentTile();
        if (current != null) {
            showCurrentTile(current);
        }
        String nickname = model.getNickname();
        if (nickname != null && !nickname.isBlank()) {
            setNickname(nickname);

        }
        setCommandVisibility(model.isDemo());
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);
        rightArrowButton.setVisible(false);
        leftArrowButton.setVisible(false);
    }


    @FXML
    private void onButtonClick(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String buttonId = sourceButton.getId();

        switch (buttonId) {
            case "getCoveredBtn" -> guiView.resolveGenericCommand("GET_COVERED");
            case "getShownBtn" -> guiView.resolveGenericCommand("GET_SHOWN");
            case "rotateLeftBtn" -> guiView.resolveGenericCommand("ROTATE_LEFT");
            case "rotateRightBtn" -> guiView.resolveGenericCommand("ROTATE_RIGHT");
            case "returnTileBtn" -> guiView.resolveGenericCommand("RETURN_TILE");
            case "logOutBtn" -> guiView.resolveGenericCommand("LOGOUT");
            case "setReadyBtn" -> guiView.resolveGenericCommand("READY");
            case "hourGlassBtn" -> guiView.resolveGenericCommand("SPIN_HOURGLASS");
            case "deck1Btn" -> guiView.resolveIndex(1);
            case "deck2Btn" -> guiView.resolveIndex(2);
            case "deck3Btn" -> guiView.resolveIndex(3);
            default -> guiView.reportError("Unrecognized button: " + buttonId);
        }
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
        }
        setPlayersButton();
    }
    private void setPlayersButton() {
        Map<String, int[]> mapPosition = model.getPlayerPositions();
        int size = mapPosition.size();
        List<String> tmpString = new ArrayList<>();
        for (String s : mapPosition.keySet()) {
            tmpString.add(s);
        }
        List<String> tmpString2 = new ArrayList<>();
        for(int i = 0; i < size; i++){
            if(!tmpString.get(i).equals(model.getNickname())){
                tmpString2.add(tmpString.get(i));
            }
        }
        switch(size){
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

    private void completeCommand(String command) {
        guiView.resolveGenericCommand(command);
    }


    private void rotateTile(int angle) {
        int oldRotation = currentRotation;
        currentRotation += angle;

        if (currentTileView != null) {
            Platform.runLater(() -> {
                RotateTransition rotate = new RotateTransition(Duration.millis(200), currentTileView);
                rotate.setFromAngle(oldRotation);
                rotate.setToAngle(currentRotation);
                rotate.setInterpolator(Interpolator.EASE_BOTH);
                rotate.play();
            });
        }

        if (!inputManager.rotationFuture.isDone()) {
            inputManager.rotationFuture.complete(currentRotation % 360); // se ti serve normalizzato
        }
    }



    private void completeIndex(int index) {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(index);
        }
    }

    private void setupCoordinateGridClickHandler() {
        for (int row = 0; row < coordinateGridPane.getRowCount(); row++) {
            for (int col = 0; col < coordinateGridPane.getColumnCount(); col++) {
                final int r = row;
                final int c = col;
                GridPane.setRowIndex(coordinateGridPane, r);
                GridPane.setColumnIndex(coordinateGridPane, c);

                // Associazione evento click
                Button tileButton = new Button();
                tileButton.setStyle("-fx-background-color: transparent;");
                tileButton.setOnMouseClicked(event -> onGridTileClicked(r, c));
                coordinateGridPane.add(tileButton, c, r);
            }
        }
    }

    private void onGridTileClicked(int row, int col) {
        if (!inputManager.coordinateFuture.isDone()) {
            inputManager.coordinateFuture.complete(new int[]{row, col});
        }
    }

    public void updateTileOnGrid(ClientTile tile, int row, int col) {
        ImageView tileImage = new ImageView(tile.getImage());
        tileImage.setFitWidth(100);
        tileImage.setFitHeight(100);
        Platform.runLater(() -> graphicGridPane.add(tileImage, col, row));
    }

    public void setNickname(String nickname) {

        Platform.runLater(() -> {
            nicknameLabel.setText(nickname);
        });
    }

    public void placeTileAt(ClientTile tile, int row, int col) {
        try {
            ImageView tileImage = new ImageView();
            tileImage.setFitWidth(70);
            tileImage.setFitHeight(70);
            tileImage.setImage(tile.getImage()); // <-- può lanciare eccezione

            graphicGridPane.add(tileImage, col, row);
            playerGrid[row][col] = tile;

        } catch (RuntimeException e) {
            System.err.println("[ERROR] Failed to place tile at (" + row + "," + col + "): " + e.getMessage());
            // Puoi anche mostrare una tile "vuota" o placeholder se vuoi
        }
    }

    public void showCurrentTile(ClientTile tile) {
        currentTile = tile;

        if (tile == null || tile.getImage() == null) {
            tilePreviewPane.getChildren().clear();
            currentTileView = null;
            return;
        }

        Platform.runLater(() -> {
            currentTileView = new ImageView(tile.getImage());
            currentTileView.setFitWidth(100);
            currentTileView.setFitHeight(100);
            currentTileView.setRotate(currentRotation);  // imposta angolo iniziale

            tilePreviewPane.getChildren().setAll(currentTileView);
        });
    }



    public void clearCurrentTile() {
        Platform.runLater(() -> {
            tilePreviewPane.getChildren().clear();
            currentTile = null;
        });
    }


    @Override
    public void updateDashboard(ClientTile[][] ship) {
        graphicGridPane.getChildren().clear();
        for (int row = 0; row < ship.length; row++) {
            for (int col = 0; col < ship[row].length; col++) {
                ClientTile tile = ship[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    placeTileAt(tile, row, col);
                }
            }
        }
    }


}

