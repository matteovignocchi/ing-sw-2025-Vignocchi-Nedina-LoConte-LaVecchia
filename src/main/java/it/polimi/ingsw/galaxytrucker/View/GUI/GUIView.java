package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.*;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.BuildingPhaseController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GameListMenuController;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum.*;

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
    private final Queue<String> notificationQueue = new LinkedList<>();
    private boolean isShowingNotification = false;
    private int[] bufferedCoordinate = null;
    private Integer bufferedIndex = null;








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
                    reportError("VirtualClient non inizializzato.");
                    return;
                }

                clientController = new ClientController(this, virtualClient);
                clientController.start();

            } catch (Exception e) {
                reportError("Errore durante l'avvio del ClientController: " + e.getMessage());
                e.printStackTrace();
            }


        }).start();
    }

    public void setSceneEnum(SceneEnum sceneEnum) {
        this.sceneEnum = sceneEnum;
        sceneRouter.setScene(sceneEnum);
    }

    public void resolveNickname(String nickname) {
        inputManager.nicknameFuture.complete(nickname);
    }

    public void resolveMenuChoice(String choice) {
        menuChoiceQueue.add(choice);
    }

    @Override
    public void inform(String message) {
        if (filterDisplayNotification(message, sceneEnum)) {
            synchronized (notificationQueue) {
                notificationQueue.offer(message);
            }
            showNextNotificationIfIdle();
        } else {
        }
    }


    @Override
    public void reportError(String message) {
        System.err.println("[GUIView] reportError: " + message);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message != null && !message.isBlank() ? message : "Unknown error");
            alert.showAndWait();
        });
    }

    @Override
    public String askString() {
        try {
            return inputManager.nicknameFuture.get();
        } catch (Exception e) {
            reportError("Failed to get string: " + e.getMessage());
            return "";
        }
    }

    @Override
    public int[] askCoordinate() {
        while (bufferedCoordinate == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new int[]{-1, -1};
            }
        }

        int[] result = bufferedCoordinate;
        bufferedCoordinate = null;
        System.out.println("[DEBUG] Coordinate lette: " + Arrays.toString(result));
        return result;
    }


    public void setBufferedCoordinate(int[] coordinate) {
        this.bufferedCoordinate = coordinate;
    }

    @Override
    public void updateView(String nickname,
                           double firePower,
                           int powerEngine,
                           int credits,
                           boolean purpleAlien,
                           boolean brownAlien,
                           int numberOfHuman,
                           int numberOfEnergy) {
        Platform.runLater(() -> {
            model.setNickname(nickname);
            // TODO: bind additional stats to UI
        });
    }

    public String askMenuChoice() {
        try {
            return inputManager.menuChoiceFuture.get();
        } catch (Exception e) {
            reportError("Failed to get menu choice: " + e.getMessage());
            return "";
        }
    }


    @Override
    public void printDashShip(ClientTile[][] ship) {
        model.setDashboard(ship);

        Platform.runLater(() -> {
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
            if (ctrl != null) {
                ctrl.updateDashboard(ship); // <-- aggiorna la view!
            }
        });
    }



    @Override
    public void updateMap(Map<String, int[]> playerMaps) {
        model.setPlayerPositions(playerMaps);
    }

    public void updateState(ClientGamePhase gamePhase) {
        this.gamePhase= gamePhase;
        Platform.runLater(() -> {
            switch (gamePhase) {
                case BOARD_SETUP -> {
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                }
                case WAITING_IN_LOBBY -> setSceneEnum(WAITING_QUEUE);
                case MAIN_MENU -> setSceneEnum(MAIN_MENU);
                case TILE_MANAGEMENT -> {
                    setSceneEnum(BUILDING_PHASE);
//                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                    sceneRouter.getController(SceneEnum.BUILDING_PHASE).postInitialize2();
                }
                case TILE_MANAGEMENT_AFTER_RESERVED->{
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                    sceneRouter.getController(SceneEnum.BUILDING_PHASE).postInitialize3();

                }
                case EXIT -> {
                    setSceneEnum(BUILDING_PHASE);
                    GUIController controller = sceneRouter.getController(BUILDING_PHASE);
                    controller.postInitializeLogOut();
                }

                default -> {}
            }
        });
    }


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



    @Override
    public void printListOfCommand() {

    }

    @Override public Boolean ask(String message) { return false; }
    @Override public boolean askWithTimeout(String message) { return false; }
    @Override public int[] askCoordinatesWithTimeout() { return new int[0]; }

    @Override
    public void displayAvailableGames(Map<Integer, int[]> availableGames) {
        System.out.println("GUI - Loading game list into ListView...");

        Platform.runLater(() -> {
            GameListMenuController ctrl = (GameListMenuController) sceneRouter.getController(SceneEnum.JOIN_GAME_MENU);
            ObservableList<String> games = FXCollections.observableArrayList();
            for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
                int gameId = entry.getKey();
                int[] data = entry.getValue();
                games.add((gameId + 1) + ". Players: " + data[0] + ", Demo: " + (data[1] == 1 ? "Yes" : "No"));
            }

            ctrl.displayGames(games);
            setSceneEnum(SceneEnum.JOIN_GAME_MENU);

        });

    }

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


    @Override
    public void setCurrentTile(ClientTile tile) {
        Platform.runLater(() -> {
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(BUILDING_PHASE);
            ctrl.showCurrentTile(tile);
        });
    }


    @Override
    public void setNickName(String cognomePradella) {

    }
    public void setBufferedIndex(Integer index) {
        this.bufferedIndex = index;
    }
    @Override
    public Integer askIndex() {
        while (bufferedIndex == null) {
            try {
                Thread.sleep(100); // attende polling leggero
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }

        int result = bufferedIndex;
        bufferedIndex = null;
        return result;
    }

    @Override public Integer askIndexWithTimeout() { return -1; }
    @Override public String choosePlayer() { return null; }
    @Override public void setInt() {}

    @Override
    public void start() {

    }

    @Override public void setIsDemo(Boolean demo) { model.setDemo(demo); }

    @Override
    public boolean returnValidity(int a, int b) {
        return model.returnValidity(a,b);
    }

    @Override
    public void setValidity(int a, int b) {
        model.setValidity(a, b);
    }

    @Override
    public void resetValidity(int a, int b) {
        model.resetValidity(a,b);
    }

    @Override
    public ClientGamePhase getGamePhase() {
        return gamePhase;
    }

    @Override public void printListOfGoods(List<String> goods) {}
    @Override public void printMapPosition() {}
    @Override public void printNewFase(String gamePhase) {}
    @Override public void printPileCovered() {}
    @Override
    public void printPileShown(List<ClientTile> tiles) {
        Platform.runLater(() -> {
            model.setCurrentTile(null); // non serve tile singola
            setBufferedIndex(null);    // reset importante per evitare valori vecchi
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
            ctrl.displayTileSelection(tiles);
        });
    }
    @Override public void printCard(ClientCard card) {}
    @Override public void printDeck(List<ClientCard> deck) {}

    public void resolveDataGame(List<Object> data) {
        inputManager.createGameDataFuture.complete(data);
        inputManager.resetAll();
    }

    public void askCoordinateAsync(Consumer<int[]> callback) {
        inputManager.coordinateFuture = new CompletableFuture<>();
        inputManager.coordinateFuture.thenAccept(callback);
    }
    public void prepareCoordinateInput() {
        Platform.runLater(() -> {
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(BUILDING_PHASE);
           // ctrl.enableGridSelection(); // questo metodo lo definisci tu se vuoi, ad esempio per abilitare highlight, ecc.
        });
    }
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

    public void resolveCommand(String command) {
        commandQueue.offer(command);
    }



    public boolean hasResolvedMenuChoice() {
        return !menuChoiceQueue.isEmpty();
    }

    public String consumeMenuChoice() {
        return menuChoiceQueue.poll();
    }

    public void prepareIndexInput(Runnable enableUI) {
        inputManager.indexFuture = new CompletableFuture<>();
        Platform.runLater(enableUI);
    }
    public void askIndexAsync(Consumer<Integer> callback) {
        inputManager.indexFuture = new CompletableFuture<>();
        inputManager.indexFuture.thenAccept(callback);
    }

    public List<Object> askCreateGameData() {
        try {
            return inputManager.createGameDataFuture.get();
        } catch (Exception e) {
            reportError("Failed to get create game data: " + e.getMessage());
            return List.of(false, 0); // fallback
        }
    }

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
            case "LOGOUT" -> "logout";
            default -> null;
        };
        System.out.println("[DEBUG] Comando ricevuto: " + command + " → " + translated);

        if (translated != null) {
            commandQueue.offer(translated);
            System.out.println("[DEBUG] Comando accodato");

        } else {
            reportError("Unrecognized command: " + command);
        }
    }


    public void resolveIndex(int index) {
        if (!inputManager.indexFuture.isDone()) {
            inputManager.indexFuture.complete(index);
        }
    }

    @Override
    public int askGameToJoin(Map<Integer, int[]> availableGames) {
        ObservableList<String> gameStrings = FXCollections.observableArrayList();
        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
            int id = entry.getKey();
            int[] info = entry.getValue();
            boolean isDemo = info[2] == 1;
            String suffix = isDemo ? " DEMO" : "";
            String desc = (id + 1) + ". Players in game: " + info[0] + "/" + info[1] + suffix;
            gameStrings.add(desc);
        }

        inputManager.indexFuture = new CompletableFuture<>();

        Platform.runLater(() -> {
            GameListMenuController ctrl = (GameListMenuController) sceneRouter.getController(SceneEnum.JOIN_GAME_MENU);
            ctrl.displayGames(gameStrings);
            setSceneEnum(SceneEnum.JOIN_GAME_MENU);
        });

        try {
            Integer result = inputManager.indexFuture.get();
            return result != null && result == -1 ? 0 : result;
        } catch (Exception e) {
            reportError("Failed to get index: " + e.getMessage());
            return 0;
        }
    }

    private void showNextNotificationIfIdle() {
        if (isShowingNotification) return;

        String nextMessage;
        synchronized (notificationQueue) {
            nextMessage = notificationQueue.poll();
        }

        if (nextMessage != null) {
            isShowingNotification = true;
            showNotification(nextMessage);
        }
    }


    private void showNotification(String message) {
        Platform.runLater(() -> {
            Scene scene = sceneRouter.getCurrentScene();
            if (scene == null || scene.getRoot() == null) {
                return;
            }

            Parent root = scene.getRoot();

            try {
                Pane pane = (Pane) root;

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
                label.setMaxWidth(300);

                StackPane toast = new StackPane(label);
                toast.setMouseTransparent(true);
                toast.setOpacity(0);

                pane.getChildren().add(toast);

                double xOffset = scene.getWidth() - 300 - 10;
                double yOffset = scene.getHeight() - 80;

                toast.setTranslateX(xOffset);
                toast.setTranslateY(yOffset);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                PauseTransition pause = new PauseTransition(Duration.seconds(3));

                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    pane.getChildren().remove(toast);
                    isShowingNotification = false;
                    showNextNotificationIfIdle();
                });


                new SequentialTransition(fadeIn, pause, fadeOut).play();

            } catch (ClassCastException e) {
                reportError("ClassCastException: " + e.getMessage());
            }
        });
    }




    private boolean filterDisplayNotification(String message, SceneEnum sceneEnum) {
        if (sceneEnum == null) {
            return message.toLowerCase().contains("login successful");
        }
        if(message.toLowerCase().contains("waiting for other players...")) {
            return false;
        }
        if(gamePhase == ClientGamePhase.TILE_MANAGEMENT){
            return false;
        }
        return switch (sceneEnum) {
            case BUILDING_PHASE -> !message.toLowerCase().contains("rotate");
            case WAITING_QUEUE -> message.toLowerCase().contains("joined");
            case MAIN_MENU -> !message.contains("Connected") || !message.contains("Insert") || !message.contains("Creating New Game...");
            case NICKNAME_DIALOG -> message.contains("Login");
            default -> false;
        };
    }

}