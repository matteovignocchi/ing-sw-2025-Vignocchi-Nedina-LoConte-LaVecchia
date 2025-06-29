package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import java.util.*;

/**
 * Controller for the building phase GUI scene in Galaxy Trucker.
 * Manages user interactions such as rotating tiles, selecting covered or shown tiles,
 * returning tiles, declaring readiness, and viewing other players' ships.
 * Connects UI components (buttons, grid panes, image views) with the underlying game logic
 * through commands sent to the GUI view.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
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

    private List<ClientTile> tileList = new ArrayList<>();
    private int tileListIndex = 0;
    private ClientTile currentTile;
    private ImageView currentTileView;
    private int currentRotation = 0;
    private ClientTile[][] playerGrid = new ClientTile[5][7];
    private ClientTile emptySpace = new ClientTile();
    private boolean isSelectingTileFromList = false;

    /**
     * Initializes the building phase scene.
     * Sets up initial states and cursor styles for buttons.
     * Attaches event handlers to buttons to handle user actions such as:
     * - Rotating the current tile left or right
     * - Selecting covered or shown tiles
     * - Returning a tile to the pile
     * - Declaring the player ready
     * - Spinning the hourglass
     * - Logging out
     * - Viewing other players' dashboards
     * - Taking reserved tiles
     * - Choosing a deck
     * Also sets up click handlers for the coordinate grid used for tile placement.
     */
    @FXML
    public void initialize() {
        emptySpace.id = 0;
        getCoveredBtn.setCursor(Cursor.HAND);
        getShownBtn.setCursor(Cursor.HAND);
        returnTileBtn.setCursor(Cursor.HAND);
        rotateLeftBtn.setCursor(Cursor.HAND);
        rotateRightBtn.setCursor(Cursor.HAND);


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
        getShownBtn.setOnAction(e -> {
            completeCommand("GET_SHOWN");
            deck1Btn.setDisable(true);
            deck2Btn.setDisable(true);
            deck3Btn.setDisable(true);
        });
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

        playerShip1Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip1Btn.getUserData());
            completeCommand("LOOK_PLAYER1");
        });

        playerShip2Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip2Btn.getUserData());
            completeCommand("LOOK_PLAYER2");
        });

        playerShip3Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip3Btn.getUserData());
            completeCommand("LOOK_PLAYER3");
        });
        reserveBtn1.setOnAction(e -> takeReserved(1));
        reserveBtn2.setOnAction(e -> takeReserved(2));
        deck1Btn.setOnAction(e -> chooseDeck(0));
        deck2Btn.setOnAction(e -> chooseDeck(1));
        deck3Btn.setOnAction(e -> chooseDeck(2));

        setupCoordinateGridClickHandler();
    }

    /**
     * Performs post-initialization setup for the building phase scene.
     * Resets button states, places all non-empty tiles from the model's dashboard into the GUI grid,
     * clears the currently selected tile, sets visibility of main action buttons,
     * updates the nickname display, and configures command visibility based on demo mode.
     */
    @Override
    public void postInitialize() {
        resetButtons();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                ClientTile tile = model.getDashboard()[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    placeTileAt(tile, row, col);
                }
            }
        }

        clearCurrentTile();

        getShownBtn.setVisible(true);
        getCoveredBtn.setVisible(true);
        setReadyBtn.setVisible(true);
        setNickname(model.getNickname());
        setCommandVisibility(model.isDemo());
    }

    /**
     * Alternative post-initialization for the building phase scene with
     * different button visibility configuration.
     * Resets buttons, shows tile rotation and return buttons,
     * disables reserved tile buttons if not in demo mode.
     */
    public void postInitialize2(){
        resetButtons();
        returnTileBtn.setVisible(true);
        rotateLeftBtn.setVisible(true);
        rotateRightBtn.setVisible(true);
        if(!model.isDemo()){
            reserveBtn1.setDisable(true);
            reserveBtn2.setDisable(true);
        }
    }

    /**
     * Another variation of post-initialization for the building phase scene.
     * Similar to `postInitialize2()` but excludes the return tile button visibility setup.
     * Resets buttons, shows rotation buttons,
     * disables reserved tile buttons if not in demo mode.
     */
    public void postInitialize3(){
        resetButtons();
        rotateLeftBtn.setVisible(true);
        rotateRightBtn.setVisible(true);
        if(!model.isDemo()){
            reserveBtn1.setDisable(true);
            reserveBtn2.setDisable(true);
        }
    }

    public void postInitialize4(){
        resetButtons();
        if(!model.isDemo()){
            hourGlassBtn.setVisible(true);
        }
        logOutBtn.setVisible(true);
        setPlayersButton();
    }

    /**
     * Initializes the coordinate grid with transparent buttons that handle drag-and-drop for tile placement.
     * Each button corresponds to a tile position and:
     * - Highlights on drag over if the dragged item is a tile.
     * - Validates the drop position against the model's mask.
     * - On valid drop, buffers the coordinate and signals the controller to place the tile.
     * - Updates reserved tile buttons visibility if not in demo mode.
     */
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
                        guiView.setBufferedCoordinate(new int[]{r, c});
                        guiView.resolveGenericCommand("PLACE_TILE");
                        if(!model.isDemo()){
                            reserveBtn1.setVisible(true);
                            reserveBtn2.setVisible(true);
                            reserveBtn1.setDisable(false);
                            reserveBtn2.setDisable(false);
                        }
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

    /**
     * Completes the rotation future with the current rotation modulo 360 degrees
     * if it has not been completed yet.
     * Used to signal the rotation angle to the input manager asynchronously.
     * @param angle the rotation angle (not directly used in this method)
     */
    private void rotateTile(int angle) {
        if (!inputManager.rotationFuture.isDone()) {
            inputManager.rotationFuture.complete(currentRotation % 360);
        }
    }

    /**
     * Displays the given tile in the tile preview pane with its current rotation.
     * Sets up drag detection on the tile image to enable drag-and-drop placement.
     * Does nothing if a tile selection from a list is currently active.
     * @param tile the ClientTile to display
     */
    public void showCurrentTile(ClientTile tile) {
        if (isSelectingTileFromList) return;

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
                db.setDragView(currentTileView.snapshot(null, null));
                event.consume();


            });

            tilePreviewPane.getChildren().setAll(currentTileView);
        });
    }

    /**
     * Sends a command string to the GUI view to be resolved by the controller logic.
     * @param command the command to complete and send
     */
    private void completeCommand(String command) {
        guiView.resolveGenericCommand(command);
    }

    /**
     * Places the given tile image at the specified position in the graphical grid.
     * Runs on the JavaFX Application Thread and replaces any existing tile node at that position.
     * The tile image is resized and rotated to reflect its current state.
     * @param tile the ClientTile to display
     * @param row the row index in the grid
     * @param col the column index in the grid
     */
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

    /**
     * Clears the tile preview pane and resets the currently selected tile to null.
     * Runs on the JavaFX Application Thread.
     */
    public void clearCurrentTile() {
        Platform.runLater(() -> {
            tilePreviewPane.getChildren().clear();
            currentTile = null;
        });
    }

    /**
     * Updates the nickname label in the GUI with the provided nickname.
     * Runs on the JavaFX Application Thread.
     * @param nickname the player nickname to display
     */
    public void setNickname(String nickname) {
        Platform.runLater(() -> nicknameLabel.setText(nickname));
    }

    /**
     * Adjusts the visibility and enabled status of GUI components based on demo mode.
     * Hides or shows ship dashboard, decks, hourglass, cards, and player buttons
     * according to whether the GUI is running in demo mode.
     * Also calls `setPlayersButton()` to update player buttons visibility.
     * @param demo true if running in demo mode, false otherwise
     */
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
            deck1Btn.setDisable(false);
            deck2Btn.setDisable(false);
            deck3Btn.setDisable(false);
            reserveBtn1.setVisible(true);
            reserveBtn2.setVisible(true);
            reserveBtn1.setDisable(false);
            reserveBtn2.setDisable(false);

        }
        setPlayersButton();
    }

    /**
     * Updates visibility, labels, and user data of player ship buttons
     * based on the number of other players in the game.
     * Shows up to three buttons, one per other player, with appropriate nicknames.
     */
    private void setPlayersButton() {
        Map<String, int[]> mapPosition = model.getPlayerPositions();
        List<String> others = mapPosition.keySet().stream()
                .filter(name -> !name.equals(model.getNickname())).toList();

        switch (others.size()) {
            case 1 -> {
                playerShip1Btn.setVisible(true);
                String name = others.getFirst();
                playerShip1Btn.setText("Player Ship of " + name);
                playerShip1Btn.setUserData(name);
            }
            case 2 -> {
                String name1 = others.getFirst();
                String name2 = others.getLast();
                playerShip2Btn.setVisible(true);
                playerShip3Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of " + name1);
                playerShip2Btn.setUserData(name1);
                playerShip3Btn.setText("Player Ship of " + name2);
                playerShip3Btn.setUserData(name2);
            }
            case 3 -> {
                String name1 = others.getFirst();
                String name2 = others.get(1);
                String name3 = others.getLast();
                playerShip1Btn.setVisible(true);
                playerShip2Btn.setVisible(true);
                playerShip3Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of " + name1);
                playerShip1Btn.setUserData(name1);
                playerShip2Btn.setText("Player Ship of " + name2);
                playerShip2Btn.setUserData(name2);
                playerShip3Btn.setText("Player Ship of " + name3);
                playerShip3Btn.setUserData(name3);
            }
        }
    }

    /**
     * Handles the action of taking a reserved tile based on the button index (1 or 2).
     * If the reserved tile position is valid, it sets the buffered coordinate,
     * sends the RESERVE_TILE command, and places an empty space in the GUI grid.
     * @param a the reserved tile slot index (1 or 2)
     */
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

    /**
     * Sets the buffered index for the chosen deck and sends the DECK command to the controller.
     * @param index the index of the deck selected by the player
     */
    private void chooseDeck(int index){
        guiView.setBufferedIndex(index);
        completeCommand("DECK");
    }

    /**
     * Displays a selection of tiles for the player to choose from.
     * Hides other buttons, shows navigation arrows to cycle through the tile list,
     * and sets up event handlers for selection and navigation.
     * @param tiles the list of tiles available for selection
     */
    public void displayTileSelection(List<ClientTile> tiles) {
        if (tiles == null || tiles.isEmpty()) {
            guiView.reportError("No tiles to display.");
            return;
        }

        isSelectingTileFromList = true;
        tileList = tiles;
        tileListIndex = 0;

        getCoveredBtn.setVisible(false);
        getShownBtn.setVisible(false);
        setReadyBtn.setVisible(false);
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);

        leftArrowButton.setVisible(true);
        rightArrowButton.setVisible(true);

        updateTilePreviewFromList();

        leftArrowButton.setOnAction(e -> {
            tileListIndex = (tileListIndex - 1 + tileList.size()) % tileList.size();
            updateTilePreviewFromList();
        });

        rightArrowButton.setOnAction(e -> {
            tileListIndex = (tileListIndex + 1) % tileList.size();
            updateTilePreviewFromList();
        });

        tilePreviewPane.setOnMouseClicked(e -> {
            guiView.setBufferedIndex(tileListIndex);
            isSelectingTileFromList = false;
            tileList = List.of();
            leftArrowButton.setVisible(false);
            rightArrowButton.setVisible(false);
        });
    }

    /**
     * Updates the tile preview pane to show the currently selected tile from the tile list.
     * Sets image size, rotation, and cursor style.
     */
    private void updateTilePreviewFromList() {
        if (tileList == null || tileList.isEmpty()) return;

        ClientTile current = tileList.get(tileListIndex);
        ImageView image = new ImageView(current.getImage());
        image.setFitWidth(100);
        image.setFitHeight(100);
        image.setRotate(current.getRotation());
        image.setCursor(Cursor.HAND);


        tilePreviewPane.getChildren().setAll(image);
    }

    /**
     * Resets visibility and disables all interactive buttons in the building phase UI.
     * Prepares the UI for a fresh state or transition.
     */
    private void resetButtons() {
        getCoveredBtn.setVisible(false);
        getShownBtn.setVisible(false);
        returnTileBtn.setVisible(false);
        rotateLeftBtn.setVisible(false);
        rotateRightBtn.setVisible(false);
        setReadyBtn.setVisible(false);
        leftArrowButton.setVisible(false);
        rightArrowButton.setVisible(false);
        reserveBtn1.setVisible(false);
        reserveBtn2.setVisible(false);
        reserveBtn1.setDisable(true);
        reserveBtn2.setDisable(true);
    }

}