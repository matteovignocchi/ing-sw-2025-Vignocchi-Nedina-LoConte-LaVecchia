package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum;
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
    @FXML private GridPane coordinateGridPane;
    @FXML private GridPane graphicGridPane;
    @FXML private HBox nicknameBox;
    @FXML private Label nicknameLabel;
    @FXML private VBox tilePreviewPane;

    private ClientTile currentTile;
    private ClientTile[][] playerGrid = new ClientTile[5][7];
    private int currentRotation = 0; // 0, 90, 180, 270

    @FXML
    public void initialize() {
        // Pulsanti per rotazione tile
        rotateLeftBtn.setOnAction(e -> rotateTile(-90));
        rotateRightBtn.setOnAction(e -> rotateTile(90));

        // Azioni passive
        getCoveredBtn.setOnAction(e -> completeCommand("deck covered"));
        getShownBtn.setOnAction(e -> completeCommand("deck shown"));
        returnTileBtn.setOnAction(e -> completeCommand("return"));
        setReadyBtn.setOnAction(e -> completeCommand("ready"));
        hourGlassBtn.setOnAction(e -> completeCommand("hourglass"));

        playerShip1Btn.setOnAction(e -> completeCommand("look player1"));
        playerShip2Btn.setOnAction(e -> completeCommand("look player2"));
        playerShip3Btn.setOnAction(e -> completeCommand("look player3"));

        deck1Btn.setOnAction(e -> completeIndex(0));
        deck2Btn.setOnAction(e -> completeIndex(1));
        deck3Btn.setOnAction(e -> completeIndex(2));

        // Coordinate: click sulla griglia
        setupCoordinateGridClickHandler();
    }

    private void completeCommand(String command) {
        if (!inputManager.commandFuture.isDone()) {
            inputManager.commandFuture.complete(command);
        }
    }

    private void completeIndex(int index) {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(index);
        }
    }

    private void rotateTile(int angle) {
        currentRotation = (currentRotation + angle + 360) % 360;
        if (!inputManager.rotationFuture.isDone()) {
            inputManager.rotationFuture.complete(currentRotation);
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
        Platform.runLater(() -> nicknameLabel.setText(nickname));
    }
    public void placeTileAt(ClientTile tile, int row, int col) {
        ImageView tileImage = new ImageView();
        tileImage.setFitWidth(70);
        tileImage.setFitHeight(70);
        tileImage.setImage(tile.getImage());

        // Aggiungi nella griglia grafica (la nave "renderizzata")
        graphicGridPane.add(tileImage, col, row);

        // Salva localmente se serve aggiornare lo stato
        playerGrid[row][col] = tile;
    }
    public void showCurrentTile(ClientTile tile) {
        currentTile = tile;

        // Mostra graficamente la tile corrente (es. in un ImageView dedicato)
        ImageView currentTileView = new ImageView(tile.getImage());
        currentTileView.setFitWidth(100);
        currentTileView.setFitHeight(100);

        // Inserisci nel nodo UI previsto per anteprima (es. Rectangle o VBox)
        // Supponiamo che tu abbia un nodo come: `tilePreviewPane.getChildren().setAll(currentTileView);`
        tilePreviewPane.getChildren().setAll(currentTileView);
    }



    @Override
    public void postInitialize(){}

    @FXML
    private void onButtonClick(ActionEvent event) {
        // Recupera il bottone premuto
        Button sourceButton = (Button) event.getSource();
        String buttonId = sourceButton.getId();

        // Esegui la logica passiva che comunica con GUIView
        // (ad esempio, completa una future oppure chiama un metodo helper passivo)
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

}

