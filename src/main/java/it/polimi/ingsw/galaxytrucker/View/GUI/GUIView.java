package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.*;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.*;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import static it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum.*;

/**
 * JavaFX-based graphical user interface implementation of the View interface.
 * Manages scenes, user inputs, and interactions for the Galaxy Trucker client.
 * Coordinates with the ClientController and GUIModel, handles asynchronous input,
 * and controls scene transitions using SceneRouter.
 * @author Matteo VIgnochhi
 * @author Oleg Nedina
 */
public class GUIView extends Application implements View {

    private static ClientController clientController;
    private SceneEnum sceneEnum;
    private ClientGamePhase gamePhase;
    private Stage mainStage;
    private SceneRouter sceneRouter;
    private UserInputManager inputManager;
    private GUIModel model;
    private final BlockingQueue<String> menuChoiceQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private boolean isShowingNotification = false;
    private int[] bufferedCoordinate = null;
    private Integer bufferedIndex = null;
    private boolean previewingEnemyDashboard = false;
    private String bufferedPlayerName = null;
    private volatile Boolean bufferedBoolean;
    private boolean showGoodActionPrompt = false;
    private Queue<String> notificationQueue = new LinkedList<>();
    private boolean isNotificationPlaying = false;
    private StackPane currentToast = null;
    private StackPane previousToast = null;
    private List<String> bufferedGoods = List.of();
    private volatile long lastAskCoordinateTimestamp = 0;
    private volatile long lastAskIndexTimestamp = 0;

    /**
     * Entry point for the JavaFX application.
     * Initializes the primary stage, model, input manager, and scene router.
     * Starts the ClientController in a background thread.
     * @param primaryStage the main stage of the JavaFX application
     */
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        this.model = new GUIModel();
        this.inputManager = new UserInputManager();

        this.sceneRouter = new SceneRouter(primaryStage, model, inputManager, this);
        sceneRouter.initializeAllScenes();

        primaryStage.setTitle("Galaxy Trucker");
        primaryStage.show();

        new Thread(() -> {
            try {
                VirtualView virtualClient = GUIStartupConfig.virtualClient;
                if (virtualClient == null) {
                    reportError("VirtualClient not initialized.");
                    return;
                }

                clientController = new ClientController(this, virtualClient);
                clientController.start();

            } catch (Exception e) {
                reportError("Error from ClientController: " + e.getMessage());
                e.printStackTrace();
            }


        }).start();
    }

    /**
     * Changes the current displayed scene to the specified scene enum.
     * @param sceneEnum the target scene to display
     */
    public void setSceneEnum(SceneEnum sceneEnum) {
        this.sceneEnum = sceneEnum;
        sceneRouter.setScene(sceneEnum);
    }

    /**
     * Adds a menu choice string to the internal queue to be processed asynchronously.
     * @param choice the menu option chosen by the user
     */
    public void resolveMenuChoice(String choice) {
        menuChoiceQueue.add(choice);
    }

    /**
     * Displays an informational message to the user.
     * Filters and processes specific server messages to update the game controller or the final scene controller.
     * Shows a notification in chat for messages passing the filter.
     * @param message the message to display
     */
    @Override
    public void inform(String message) {
        if(message.toLowerCase().contains("eliminated")) model.setEliminated();
        if( message.contains("Choose your starting housing unit:") || message.contains("select") || message.toLowerCase().contains("eliminated") || message.contains("Select an housing unit") || message.contains("Select an energy cell to remove a battery from")) {
            Platform.runLater(() -> {
                GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
                if (ctrl != null) {
                    ctrl.messageSet(message);
                } else {
                    reportError("GameController not initialized");
                }
            });
        }
        if(message.contains("You have received the Best Ship Built bonus plus 14 credits")){
            Platform.runLater(() -> {
                FinalSceneController ctrl = (FinalSceneController) sceneRouter.getController(EXIT_PHASE);
                if (ctrl != null) {
                    ctrl.bestShipMessage(message);
                } else {
                    reportError("FinalSceneController not initialized");
                }
            });
        }
        if(message.contains("You earned") || message.contains("credits from selling goods")){
            Platform.runLater(() -> {
                FinalSceneController ctrl = (FinalSceneController) sceneRouter.getController(EXIT_PHASE);
                if (ctrl != null) {
                    ctrl.goodsMessage(message);
                } else {
                    reportError("FinalSceneController not initialized");
                }
            });
        }
        if(message.contains("because you arrived ")){
            Platform.runLater(() -> {
                FinalSceneController ctrl = (FinalSceneController) sceneRouter.getController(EXIT_PHASE);
                if (ctrl != null) {
                    ctrl.setArrivedMessage(message);
                } else {
                    reportError("FinalSceneController not initialized");
                }
            });
        }
        if(message.contains("credits from all the tile you lost")){
            Platform.runLater(() -> {
                FinalSceneController ctrl = (FinalSceneController) sceneRouter.getController(EXIT_PHASE);
                if (ctrl != null) {
                    ctrl.creditsLostMessage(message);
                } else {
                    reportError("FinalSceneController not initialized");
                }
            });
        }
        if(filterDisplayNotification(message)){
            appendToChat(message);
        }
    }

    /**
     * Displays an error message in the GUI as a temporary toast notification.
     * The toast fades in, stays visible for a short duration, then fades out and removes itself.
     * If the scene or root pane is unavailable, the error is logged to the console.
     * @param message the error message to display
     */
    @Override
    public void reportError(String message) {
        System.err.println("[GUIView] reportError: " + message);
        Platform.runLater(() -> {
            Scene scene = sceneRouter.getCurrentScene();
            if (scene == null || scene.getRoot() == null) return;

            Parent root = scene.getRoot();
            try {
                Pane pane = (Pane) root;

                Label label = new Label(message != null && !message.isBlank() ? message : "Unknown error");
                label.setStyle("""
                -fx-font-family: "Impact";
                -fx-font-size: 18px;
                -fx-text-fill: #ffffff;
                -fx-background-color: rgba(255, 0, 0, 0.9);
                -fx-padding: 10px 18px;
                -fx-background-radius: 0;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);
            """);
                label.setWrapText(true);
                label.setMaxWidth(300);

                StackPane toast = new StackPane(label);
                toast.setMouseTransparent(true);
                toast.setOpacity(0);

                pane.getChildren().add(toast);

                double xOffset = scene.getWidth() - 300 - 10;
                double yOffset = scene.getHeight() - 80;

                toast.setTranslateX(xOffset);
                toast.setTranslateY(yOffset);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), toast);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                PauseTransition pause = new PauseTransition(Duration.seconds(2));

                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), toast);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> pane.getChildren().remove(toast));

                new SequentialTransition(fadeIn, pause, fadeOut).play();

            } catch (ClassCastException e) {
                System.err.println("[GUIView] Error casting root pane: " + e.getMessage());
            }
        });
    }

    /**
     * Requests a string input from the user.
     * If a buffered player name is set, returns it immediately.
     * Otherwise, waits asynchronously for the nickname input future to complete.
     * @return the input string from the user or buffered name
     */
    @Override
    public String askString() {
        if (bufferedPlayerName != null) {
            String result = bufferedPlayerName;
            bufferedPlayerName = null;
            return result;
        }

        try {
            return inputManager.nicknameFuture.get();
        } catch (Exception e) {
            reportError("Failed to get string: " + e.getMessage());
            return "";
        }
    }

    /**
     * Requests tile coordinates input from the user.
     * Enables coordinate selection in the game controller asynchronously.
     * Waits for buffered coordinates to be set via user interaction.
     * Returns a default coordinate if interrupted.
     * @return the selected tile coordinates as an int array [row, column]
     */
    @Override
    public int[] askCoordinate() {
        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.enableDashboardCoordinateSelection(coords -> setBufferedCoordinate(coords));
            } else {
                reportError("GameController not enabled.");
            }
        });

        while (bufferedCoordinate == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new int[]{2,3};
            }
        }

        int[] result = bufferedCoordinate;
        bufferedCoordinate = null;
        return result;
    }

    /**
     * Buffers the coordinate input for later retrieval.
     * @param coordinate the selected coordinates as [row, column]
     */
    public void setBufferedCoordinate(int[] coordinate) {
        this.bufferedCoordinate = coordinate;
    }

    /**
     * Buffers a Boolean value for later retrieval.
     * @param value the Boolean value to buffer
     */
    public void setBufferedBoolean(Boolean value) {this.bufferedBoolean = value;}

    /**
     * Updates the GUI model and relevant controllers with the latest player statistics.
     * Updates fields like nickname, firepower, engine power, credits, alien presence,
     * number of humans and energy, and refreshes the game and final screens accordingly.
     * @param nickname the player's nickname
     * @param firePower total firepower
     * @param powerEngine total engine power
     * @param credits player's credits
     * @param purpleAlien whether purple alien is present
     * @param brownAlien whether brown alien is present
     * @param numberOfHuman number of human crew members
     * @param numberOfEnergy number of energy units
     */
    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {
        Platform.runLater(() -> {
            model.setNickname(nickname);
            model.setFirePower(firePower);
            model.setEnginePower(powerEngine);
            model.setCredits(credits);
            model.setPurpleAlien(purpleAlien);
            model.setBrownAlien(brownAlien);
            model.setNumberOfEnergy(numberOfEnergy);
            model.setNumberOfHumans(numberOfHuman);
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.updateStatsLabels(
                        nickname,
                        firePower,
                        powerEngine,
                        credits,
                        purpleAlien,
                        brownAlien,
                        numberOfHuman,
                        numberOfEnergy);}
        });
        FinalSceneController ctrl2 = (FinalSceneController) sceneRouter.getController(EXIT_PHASE);
        ctrl2.setCredits(credits);
    }

    /**
     * Displays the dashboard of a ship in the GUI.
     * If previewing an enemy dashboard, opens a popup window with that ship's tiles.
     * Otherwise, updates the player's own dashboard on the main scene.
     * @param ship 2D array of ClientTile representing the ship layout
     */
    @Override
    public void printDashShip(ClientTile[][] ship) {
        if (previewingEnemyDashboard) {
            previewingEnemyDashboard = false;
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PrintDash.fxml"));
                    AnchorPane root = loader.load();

                    PrintDashController ctrl = loader.getController();
                    ctrl.setIsDemo(model.isDemo());
                    ctrl.loadDashboard(ship);

                    Stage popup = new Stage();
                    popup.setTitle("Ship of " + bufferedPlayerName);
                    bufferedPlayerName = null;
                    Scene popupScene = new Scene(root);
                    popup.setScene(popupScene);
                    popup.centerOnScreen();

                    Button done = (Button) root.lookup("#closeButton");
                    if (done != null) {
                        done.setOnAction(e -> popup.close());
                    } else {
                        reportError("Done button not found.");
                    }

                    popup.show();

                } catch (IOException e) {
                    reportError("Error loading PrintDash.fxml: " + e.getMessage());
                }
            });
        } else {
            model.setDashboard(ship);
            Platform.runLater(() -> {
                GUIController controller = sceneRouter.getController(SceneEnum.GAME_PHASE);
                controller.updateDashboard();
            });
        }
    }

    /**
     * Updates the internal model with player positions and informs the game controller to update the map UI.
     * @param playerMaps map of player nicknames to position arrays
     */
    @Override
    public void updateMap(Map<String, int[]> playerMaps) {
        model.setPlayerPositions(playerMaps);

        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.updateMapPosition(playerMaps, model.isDemo());
            }
        });
    }

    /**
     * Changes the current game phase and updates the GUI scene accordingly.
     * Calls appropriate post-initialization hooks on the corresponding controllers.
     * @param newPhase the new game phase to set
     */
    public void updateState(ClientGamePhase newPhase) {

        if (newPhase == this.gamePhase) {
            return;
        }
        this.gamePhase = newPhase;

        Platform.runLater(() -> {
            switch (newPhase) {
                case BOARD_SETUP -> {
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                }
                case WAITING_IN_LOBBY -> setSceneEnum(WAITING_QUEUE);
                case MAIN_MENU ->{
                    setSceneEnum(MAIN_MENU);
                    resetGUIState();
                }
                case TILE_MANAGEMENT -> {
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                    sceneRouter.getController(SceneEnum.BUILDING_PHASE).postInitialize2();
                }
                case TILE_MANAGEMENT_AFTER_RESERVED->{
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                    sceneRouter.getController(SceneEnum.BUILDING_PHASE).postInitialize3();

                }
                case EXIT -> {
                    setSceneEnum(EXIT_PHASE);
                    GUIController controller = sceneRouter.getController(EXIT_PHASE);
                    if (controller != null) {
                        controller.postInitialize();
                    } else {
                        reportError("Controller EXIT_PHASE not found.");
                    }
                }
                case WAITING_FOR_PLAYERS -> {
                    setSceneEnum(BUILDING_PHASE);
                    GUIController controller = sceneRouter.getController(BUILDING_PHASE);
                    controller.postInitialize4();
                }
                case WAITING_FOR_TURN ->{
                    setSceneEnum(GAME_PHASE);
                    GUIController controller = sceneRouter.getController(GAME_PHASE);
                    controller.postInitialize();
                }
                case CARD_EFFECT -> {
                    setSceneEnum(GAME_PHASE);
                    GUIController controller = sceneRouter.getController(GAME_PHASE);
                    controller.postInitialize();
                    controller.postInitialize3();
                }
                case DRAW_PHASE ->{
                    setSceneEnum(GAME_PHASE);
                    sceneRouter.getController(GAME_PHASE).postInitialize();
                    sceneRouter.getController(GAME_PHASE).postInitialize2();
                }

                default -> {}
            }
        });
    }

    /**
     * Displays the specified tile in the building phase UI.
     * If tile is null, clears the current tile preview.
     * @param tile the tile to display
     */
    @Override
    public void printTile(ClientTile tile) {
        Platform.runLater(() -> {
            model.setCurrentTile(tile);

            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
            if (ctrl != null) {
                if (tile == null) {
                    ctrl.clearCurrentTile();
                } else {
                    ctrl.showCurrentTile(tile);
                }
            } else {
                System.err.println("[GUIView] WARNING: BUILDING_PHASE controller not initialized yet.");
            }
        });
    }

    /**
     * Empty override of printListOfCommand.
     * No commands are printed in this GUI implementation for now.
     */
    @Override
    public void printListOfCommand() {}

    /**
     * Stub method for asking a boolean question; always returns false.
     * @param message the message or question to display
     * @return false (stub implementation)
     */
    @Override public Boolean ask(String message) { return false; }

    /**
     * Displays the list of available games in the Join Game menu.
     * Converts the game data into a list of formatted strings, updates
     * the game list UI component asynchronously, and switches to the Join Game scene.
     * @param availableGames map of game IDs to their player count and demo status
     */
    @Override
    public void displayAvailableGames(Map<Integer, int[]> availableGames) {
    }

    /**
     * Updates the model dashboard at the specified position with the given tile.
     * If the current scene is the building phase, also updates the GUI grid visually.
     * @param tile the tile to set
     * @param row the row index
     * @param col the column index
     */
    @Override
    public void setTile(ClientTile tile, int row, int col) {
        model.getDashboard()[row][col] = tile;

        if (sceneEnum != SceneEnum.BUILDING_PHASE) {
            return;
        }

        Platform.runLater(() -> {
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(BUILDING_PHASE);
            if (ctrl != null) {
                ctrl.placeTileAt(tile, row, col);
            } else {
                System.err.println("[GUIView] WARNING: BUILDING_PHASE controller not initialized yet.");
            }
        });
    }

    /**
     * Displays the current tile preview in the building phase controller.
     * @param tile the ClientTile to display
     */
    @Override
    public void setCurrentTile(ClientTile tile) {
        Platform.runLater(() -> {
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(BUILDING_PHASE);
            ctrl.showCurrentTile(tile);
        });
    }

    /**
     * Empty override for setting nickname; no action performed.
     * @param name the nickname string (unused)
     */
    @Override
    public void setNickName(String name) {}

    /**
     * Buffers the selected index for asynchronous retrieval.
     * @param index the index to buffer
     */
    public void setBufferedIndex(Integer index) {
        this.bufferedIndex = index;
    }

    /**
     * Waits for a buffered index to be set and returns it.
     * Returns -1 if interrupted.
     * @return the buffered index or -1 if interrupted
     */
    @Override
    public Integer askIndex() {
        while (bufferedIndex == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }

        int result = bufferedIndex;
        bufferedIndex = null;
        return result;
    }

    /**
     * Requests an index from the user with a timeout of 5 minutes.
     * If called too frequently (within 300ms), returns null immediately.
     * Displays a goods selection popup if applicable, otherwise an action selection.
     * Waits for user input or until timeout expires.
     * @return the selected index, or null if timed out or no selection made
     */
    @Override
    public Integer askIndexWithTimeout() {
        long now = System.currentTimeMillis();
        if (now - lastAskIndexTimestamp < 300) {
            return null;
        }

        lastAskIndexTimestamp = now;
        long deadline = now + 300_000;

        long waitStart = System.currentTimeMillis();
        while (!showGoodActionPrompt && System.currentTimeMillis() - waitStart < 300) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PrintListOfGoods.fxml"));
                AnchorPane root = loader.load();
                PrintListOfGoodController ctrl = loader.getController();
                Stage stage = new Stage();
                ctrl.setStage(stage);
                if (showGoodActionPrompt) {
                    ctrl.loadGoods(bufferedGoods, this);
                    ctrl.setupForGoodsIndexSelection();
                    ctrl.configureNavigation(this);
                } else {
                    ctrl.setupForActionSelection(this);
                }

                showGoodActionPrompt = false;


                stage.setTitle("Goods");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();

            } catch (IOException ex) {
                reportError("Error load of PrintListOfGood.fxml: " + ex.getMessage());
            }
        });

        while (bufferedIndex == null && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return 0;
            }
        }

        if (bufferedIndex == null) {
            return null;
        }

        int res = bufferedIndex;
        bufferedIndex = null;


        return res;
    }

    /**
     * Asks the user a yes/no question with a timeout of 5 minutes.
     * Displays the message (stripped of "SERVER:" prefix) with Yes/No buttons.
     * Waits for user input or returns false if timeout or interruption occurs.
     * @param message the question to ask the user
     * @return true if user selects Yes, false otherwise or on timeout
     */
    @Override
    public boolean askWithTimeout(String message) {
        long timeout = 300_000;
        long deadline = System.currentTimeMillis() + timeout;
        if (message != null && message.startsWith("SERVER:")) {
            message = message.substring("SERVER:".length()).strip();
        }
        bufferedBoolean = null;

        String finalMessage = message;
        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(GAME_PHASE);
            if (ctrl != null) {
                ctrl.showYesNoButtons(finalMessage);
            } else {
                reportError("Controller not intialized yet.");
            }
        });

        while (bufferedBoolean == null && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (bufferedBoolean == null) {
            return false;
        }

        boolean result = bufferedBoolean;
        bufferedBoolean = null;
        return result;
    }

    /**
     * Requests coordinates input from the user with a timeout of 5 minutes.
     * Enables coordinate selection in the GUI and waits for input.
     * Returns default coordinates or null if timeout or interruption occurs.
     * @return selected coordinates as [row, col], or null on timeout
     */
    @Override
    public int[] askCoordinatesWithTimeout() {
        lastAskCoordinateTimestamp = System.currentTimeMillis();

        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.enableDashboardCoordinateSelection(coords -> setBufferedCoordinate(coords));
            } else {
                reportError("GameController not enabled yet.");
            }
        });

        long deadline = System.currentTimeMillis() + 300_000;
        while (bufferedCoordinate == null) {
            if (System.currentTimeMillis() > deadline) {
                return new int[]{2,3};
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new int[]{2,3};
            }
        }

        int[] result = bufferedCoordinate;
        bufferedCoordinate = null;


        return result;
    }

    /**
     * Returns the currently buffered player name if set.
     * Reports an error and returns null if no player name is buffered.
     * @return the selected player name or null if none selected
     */
    @Override
    public String choosePlayer() {
        if (bufferedPlayerName != null) {
            String result = bufferedPlayerName;
            return result;
        } else {
            reportError("Player not selected.");
            return null;
        }
    }

    /**
     * Stub implementation of start; no action performed.
     */
    @Override
    public void start() {}

    /**
     * Sets the demo mode flag in the model.
     * @param demo true to enable demo mode, false otherwise
     */
    @Override
    public void setIsDemo(Boolean demo) { model.setDemo(demo); }

    /**
     * Checks if the specified coordinate is currently valid for tile placement.
     * @param a row index
     * @param b column index
     * @return true if the position is valid, false otherwise
     */
    @Override
    public boolean returnValidity(int a, int b) {
        return model.returnValidity(a,b);
    }

    /**
     * Marks the specified coordinate as invalid for tile placement.
     * @param a row index
     * @param b column index
     */
    @Override
    public void setValidity(int a, int b) {
        model.setValidity(a, b);
    }

    /**
     * Resets the validity of the specified coordinate to valid for tile placement.
     * @param a row index
     * @param b column index
     */
    @Override
    public void resetValidity(int a, int b) {
        model.resetValidity(a,b);
    }

    /**
     * Returns the current game phase.
     * @return the current ClientGamePhase
     */
    @Override
    public ClientGamePhase getGamePhase() {
        return gamePhase;
    }

    /**
     * Buffers the list of goods to be displayed and sets a flag to prompt the user
     * for an action related to goods.
     * @param goods list of goods represented as strings (color names)
     */
    @Override
    public void printListOfGoods(List<String> goods) {
        this.bufferedGoods = goods;
        this.showGoodActionPrompt = true;
    }

    /**
     * Stub method for printing player map positions.
     */
    @Override
    public void printMapPosition() {}

    /**
     * Stub method for printing the covered pile of tiles.
     */
    @Override
    public void printPileCovered() {}

    /**
     * Displays the list of shown tiles in the building phase UI.
     * Resets any current tile selection before displaying.
     * @param tiles list of ClientTile objects to display
     */
    @Override
    public void printPileShown(List<ClientTile> tiles) {
        Platform.runLater(() -> {
            model.setCurrentTile(null);
            setBufferedIndex(null);
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
            ctrl.displayTileSelection(tiles);
        });
    }

    /**
     * Displays the specified card in the game phase UI.
     * Clears the card display if the card is null.
     * @param card the ClientCard to display
     */
    @Override
    public void printCard(ClientCard card) {
        Platform.runLater(() -> {
            model.setCurrentCard(card);

            GameController ctrl = (GameController) sceneRouter.getController(GAME_PHASE);
            if (ctrl != null) {
                if (card == null) {
                    ctrl.clearCurrentTile();
                } else {
                    ctrl.showCurrentCard(card);
                }
            } else {
                System.err.println("[GUIView] WARNING: BUILDING_PHASE controller not initialized yet.");
            }
        });
    }

    /**
     * Displays a deck of cards in a popup window.
     * Shows up to three cards side-by-side, with a close button.
     * Handles loading the FXML layout and populating card images.
     * @param deck list of ClientCard objects representing the deck
     */
    @Override
    public void printDeck(List<ClientCard> deck) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PrintDeck.fxml"));
                AnchorPane root = loader.load();

                Pane leftPane = (Pane) root.getChildren().get(1);
                Pane centerPane = (Pane) root.getChildren().get(2);
                Pane rightPane = (Pane) root.getChildren().get(3);
                List<Pane> panes = List.of(leftPane, centerPane, rightPane);

                for (int i = 0; i < Math.min(deck.size(), 3); i++) {
                    ClientCard card = deck.get(i);
                    ImageView imageView = new ImageView(card.getImage());
                    imageView.setFitWidth(280);
                    imageView.setFitHeight(430);
                    panes.get(i).getChildren().add(imageView);
                }

                Stage popupStage = new Stage();
                popupStage.setTitle("Deck");
                Scene popupScene = new Scene(root);
                popupStage.setScene(popupScene);
                popupStage.centerOnScreen();
                Button done = (Button) root.lookup("#done");
                if (done != null) {
                    done.setOnAction(e -> popupStage.close());
                } else {
                    reportError("Done button not fount");
                }

                popupStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                reportError("Error loading PrintDeck.fxml: " + e.getMessage());
            }
        });
    }

    /**
     * Completes the future related to game creation data input.
     * Resets all buffered inputs afterward.
     * @param data list of game configuration data to resolve
     */
    public void resolveDataGame(List<Object> data) {
        inputManager.createGameDataFuture.complete(data);
        inputManager.resetAll();
    }

    /**
     * Retrieves the next user command from the command queue, blocking until available.
     * @return the next command string input by the user, or null if interrupted
     */
    @Override
    public String sendAvailableChoices() {
        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            reportError("Interrupted while waiting for command");
            return null;
        }
    }

    /**
     * Adds a generic command string to the command queue for processing.
     * @param command the command to resolve
     */
    public void resolveCommand(String command) {
        commandQueue.offer(command);
    }

    /**
     * Checks if there is at least one menu choice available in the queue.
     * @return true if there is a pending menu choice, false otherwise
     */
    public boolean hasResolvedMenuChoice() {
        return !menuChoiceQueue.isEmpty();
    }

    /**
     * Retrieves and removes the next menu choice from the queue.
     * @return the next menu choice string, or null if none is available
     */
    public String consumeMenuChoice() {
        return menuChoiceQueue.poll();
    }

    /**
     * Retrieves the game creation data input by the user.
     * Blocks until the input future completes or returns a default if failed.
     * @return a list containing the game creation parameters
     */
    public List<Object> askCreateGameData() {
        try {
            return inputManager.createGameDataFuture.get();
        } catch (Exception e) {
            reportError("Failed to get create game data: " + e.getMessage());
            return List.of(false, 0);
        }
    }

    /**
     * Translates a generic command string to a known internal command and queues it.
     * If the command is unrecognized, reports an error.
     * @param command the user input command string
     */
    public void resolveGenericCommand(String command) {
        String translated = switch (command.toUpperCase()) {
            case "GET_COVERED" -> "getacoveredtile";
            case "GET_SHOWN" -> "getashowntile";
            case "RETURN_TILE" -> "returnthetile";
            case "READY" -> "declareready";
            case "SPIN_HOURGLASS" -> "spinthehourglass";
            case "LOOK_PLAYER1", "LOOK_PLAYER2", "LOOK_PLAYER3" -> "watchaplayersship";
            case "ROTATE_LEFT" -> "leftrotatethetile";
            case "ROTATE_RIGHT" -> "rightrotatethetile";
            case "PLACE_TILE" -> "placethetile";
            case "RESERVE_TILE" -> "takereservedtile";
            case "DECK" -> "watchadeck";
            case "LOGOUT" -> "logout";
            case "DRAW" -> "drawacard";
            default -> null;
        };

        if (translated != null) {
            commandQueue.offer(translated);
        } else {
            reportError("Unrecognized command: " + command);
        }
    }

    /**
     * Displays the list of available games and prompts the user to select one to join.
     * Converts the game map to a list of descriptive strings, updates the GUI,
     * then waits for the user selection asynchronously.
     * @param availableGames map of game IDs to player counts and demo status
     * @return the chosen game index or 0 if canceled or on error
     */
    @Override
    public int askGameToJoin(Map<Integer, int[]> availableGames) {
        ObservableList<String> gameStrings = FXCollections.observableArrayList();
        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
            int id = entry.getKey();
            int[] info = entry.getValue();
            boolean isDemo = info[2] == 1;
            String suffix = isDemo ? " DEMO" : "";
            String desc = (id) + ". Players in game: " + info[0] + "/" + info[1] + suffix;
            gameStrings.add(desc);
        }

        inputManager.indexFuture = new CompletableFuture<>();

        Platform.runLater(() -> {
            GameListMenuController ctrl = (GameListMenuController) sceneRouter.getController(SceneEnum.JOIN_GAME_MENU);
            ctrl.displayGames(gameStrings);
            setSceneEnum(SceneEnum.JOIN_GAME_MENU);
        });

        try {
            Integer result = inputManager.indexFuture.get()-1;
            return result != null && result == -1 ? 0 : result;
        } catch (Exception e) {
            reportError("Failed to get index: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Adds a notification message to the display queue and initiates showing it with animation.
     * @param message the notification text to display
     */
    public void showNotification(String message) {
        synchronized (notificationQueue) {
            notificationQueue.offer(message);
        }
        playNextNotification();
    }

    /**
     * Shows the next notification message from the queue if no notification is currently displayed.
     * Animates the current toast message fading out and moving up,
     * then displays the next message with a fade-in, pause, and fade-out sequence.
     */
    private void playNextNotification() {
        if (isNotificationPlaying) return;

        String message;
        synchronized (notificationQueue) {
            message = notificationQueue.poll();
        }

        if (message == null) return;

        isNotificationPlaying = true;

        Platform.runLater(() -> {
            Scene scene = sceneRouter.getCurrentScene();
            if (scene == null || scene.getRoot() == null) {
                isNotificationPlaying = false;
                return;
            }

            Pane pane;
            try {
                pane = (Pane) scene.getRoot();
            } catch (ClassCastException e) {
                reportError("ClassCastException: " + e.getMessage());
                isNotificationPlaying = false;
                return;
            }

            if (currentToast != null) {
                previousToast = currentToast;

                TranslateTransition moveUp = new TranslateTransition(Duration.millis(400), previousToast);
                moveUp.setByY(-70);

                FadeTransition fadeOutOld = new FadeTransition(Duration.millis(400), previousToast);
                fadeOutOld.setFromValue(1);
                fadeOutOld.setToValue(0);
                fadeOutOld.setOnFinished(e -> {
                    pane.getChildren().remove(previousToast);
                    previousToast = null;
                });

                new ParallelTransition(moveUp, fadeOutOld).play();
            }

            Label label = new Label(message);
            label.setStyle("""
            -fx-font-family: "Impact";
            -fx-font-size: 19px;
            -fx-text-fill: #000000;
            -fx-background-color: rgba(255, 223, 0, 0.85);
            -fx-padding: 10px 18px;
            -fx-background-radius: 0;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);
        """);
            label.setWrapText(true);
            label.setMaxWidth(400);

            StackPane toast = new StackPane(label);
            toast.setMouseTransparent(true);
            toast.setOpacity(0);
            pane.getChildren().add(toast);

            toast.setTranslateX(scene.getWidth() - 450);
            toast.setTranslateY(scene.getHeight() - 100);

            currentToast = toast;

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                pane.getChildren().remove(toast);
                if (currentToast == toast) currentToast = null;
                isNotificationPlaying = false;
                playNextNotification();
            });
            new SequentialTransition(fadeIn, pause, fadeOut).play();
        });
    }

    /**
     * Determines if a message should trigger a notification display based on its content and current scene.
     * Filters out messages related to specific game phases or repetitive messages to avoid notification spam.
     * @param message the message to evaluate
     * @return true if the message should be displayed as a notification, false otherwise
     */
    private boolean filterDisplayNotification(String message) {
        String lowerMsg = message.strip().toLowerCase();
        if (gamePhase == ClientGamePhase.EXIT) {
            resetQueue();
            return false;
        }
        if (message.strip().startsWith("SELECT:\n 1. Add good\n 2. Rearranges the goods\n 3. Trash some goods")) return false;
        if (lowerMsg.contains("choose deck") || lowerMsg.contains("choose coordinates")) return false;
        if (lowerMsg.contains("ship before the attack")) return false;
        if (lowerMsg.contains("select")) return false;
        if (lowerMsg.contains("flight started")) return false;
        if (lowerMsg.contains("checking")) return false;
        if (sceneEnum == null) {
            return lowerMsg.contains("login successful");
        }
        if (gamePhase == ClientGamePhase.TILE_MANAGEMENT || gamePhase == ClientGamePhase.TILE_MANAGEMENT_AFTER_RESERVED) {
            return lowerMsg.contains("hourglass");
        }

        return switch (sceneEnum) {
            case BUILDING_PHASE -> !lowerMsg.contains("rotate");
            case WAITING_QUEUE -> lowerMsg.contains("joined");
            case MAIN_MENU -> lowerMsg.contains("login successful") &&
                    !lowerMsg.contains("connected") &&
                    !lowerMsg.contains("insert") &&
                    !lowerMsg.contains("creating new game...");
            case NICKNAME_DIALOG -> lowerMsg.contains("login");
            default -> true;
        };
    }

    /**
     * Prepares the GUI to display an opponent's dashboard by setting flags and buffering the opponent's name.
     * @param enemyName the name of the opponent whose dashboard will be viewed
     */
    public void prepareToViewEnemyDashboard(String enemyName) {
        this.previewingEnemyDashboard = true;
        this.bufferedPlayerName = enemyName;
    }

    /**
     * Resets the GUI model and clears all buffered inputs and flags to initial state.
     * Clears dashboard, tiles, stats, player positions, demo flag, and input buffers.
     */
    private void clearModelAndBuffers() {
        if (model != null) {
            model.setDashboard(new ClientTile[5][7]);
            model.setCurrentTile(null);
            model.setCurrentCard(null);
            model.setFirePower(0);
            model.setEnginePower(0);
            model.setCredits(0);
            model.setNumberOfHumans(0);
            model.setNumberOfEnergy(0);
            model.setPurpleAlien(false);
            model.setBrownAlien(false);
            model.setPlayerPositions(new HashMap<>());
            model.setDemo(false);
        }

        bufferedCoordinate = null;
        bufferedIndex = null;
        bufferedPlayerName = null;
        bufferedBoolean = null;
        showGoodActionPrompt = false;
        bufferedGoods = List.of();

        model.reset();
        inputManager.resetAll();
    }

    /**
     * Resets the entire GUI state including the model, input buffers, scenes, and notification queues.
     * Prepares the GUI for a fresh start or after logout.
     */
    public void resetGUIState() {
        clearModelAndBuffers();
        sceneRouter.reinitializeAllScenes();
        menuChoiceQueue.clear();
        commandQueue.clear();
        notificationQueue.clear();
        isShowingNotification = false;
        gamePhase = null;
        sceneEnum = null;
    }

    /**
     * Clears the notification message queue.
     */
    private void resetQueue(){
        notificationQueue = new LinkedList<>();
    }

    public void appendToChat(String message) {
        ChatController ctrl = (ChatController) sceneRouter.getController(CHAT);
        if (ctrl != null) {
            Platform.runLater(() -> ctrl.addMessage(message));
        } else {
            System.err.println("Not initialized");
        }
    }

    /**
     * Show the chat scene in a new window.
     * The scene is loaded from the {@code SceneRouter} and displayed in a new stage.
     * If the scene is not found, an error is printed to standard error.
     */
    public void showChatScene() {
        Platform.runLater(() -> {
            Scene chatScene = sceneRouter.getScene(SceneEnum.CHAT);
            if (chatScene != null) {
                Stage chatStage = new Stage();
                chatStage.setTitle("Chat");
                chatStage.setScene(chatScene);
                chatStage.initOwner(mainStage);
                chatStage.setResizable(false);
                chatStage.initModality(Modality.NONE);
                chatStage.show();
            } else {
                System.err.println("Chat scene not found.");
            }
        });
    }



}