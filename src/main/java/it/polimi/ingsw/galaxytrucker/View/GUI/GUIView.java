package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.*;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.BuildingPhaseController;
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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum.BUILDING_PHASE;
import static it.polimi.ingsw.galaxytrucker.View.GUI.SceneEnum.WAITING_QUEUE;

public class GUIView extends Application implements View {

    private static ClientController clientController;
    private SceneEnum sceneEnum;
    private Stage mainStage;
    private SceneRouter sceneRouter;
    private UserInputManager inputManager;
    private GUIModel model;
    private final BlockingQueue<String> menuChoiceQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();




    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        this.model = new GUIModel();
        this.inputManager = new UserInputManager();

        this.sceneRouter = new SceneRouter(primaryStage, model, inputManager, this);
        sceneRouter.initializeAllScenes();

        primaryStage.setTitle("Galaxy Trucker");
        primaryStage.show();

        new Thread(() -> {
            try {
                System.out.println("Launching ClientController in GUI thread...");

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
        menuChoiceQueue.add(choice);  // ✅ correta
    }

    @Override
    public void inform(String message) {
        if (filterDisplayNotification(message, sceneEnum)) {
            showNotification(message);
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
    public int[] askCoordinate() throws IOException, InterruptedException {
        try {
            return inputManager.coordinateFuture.get();
        } catch (Exception e) {
            reportError("Failed to get coordinates: " + e.getMessage());
            return new int[]{-1, -1};
        }
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
        Platform.runLater(() -> {
            model.setDashboard(ship);
            sceneRouter.getController(SceneEnum.BUILDING_PHASE).updateDashboard(ship);
        });
    }


    @Override
    public void updateMap(Map<String, int[]> playerMaps) {
        model.setPlayerPositions(playerMaps);
    }

    public void updateState(ClientGamePhase gamePhase) {
        Platform.runLater(() -> {
            switch (gamePhase) {
                case BOARD_SETUP -> {
                    setSceneEnum(BUILDING_PHASE);
                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
                }
                case WAITING_IN_LOBBY -> setSceneEnum(WAITING_QUEUE);
                default -> {}
            }
        });
    }


    @Override
    public void printTile(ClientTile tile) {
        Platform.runLater(() -> model.setCurrentTile(tile));
    }

    @Override
    public void printListOfCommand() {
        Platform.runLater(() -> {
            // TODO: display command options in GUI
        });
    }

//    public String sendAvailableChoices() {
//        try {
//            return inputManager.commandFuture.get();
//        } catch (Exception e) {
//            reportError("Failed to get user command: " + e.getMessage());
//            return "";
//        }
//    }

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
            ctrl.showCurrentTile(tile);  // Questo è il tile che l'utente deve piazzare
        });
    }


    @Override
    public void setNickName(String cognomePradella) {

    }

    @Override
    public Integer askIndex() {
        try {
            return inputManager.indexFuture.get();  // blocca finché un controller non completa
        } catch (Exception e) {
            reportError("Failed to get index: " + e.getMessage());
            return -1;
        }
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

    }

    @Override
    public void resetValidity(int a, int b) {

    }

    @Override
    public ClientGamePhase getGamePhase() {
        return null;
    }

    @Override public void printListOfGoods(List<String> goods) {}
    @Override public void printMapPosition() {}
    @Override public void printNewFase(String gamePhase) {}
    @Override public void printPileCovered() {}
    @Override public void printPileShown(List<ClientTile> tiles) {}
    @Override public void printCard(ClientCard card) {}
    @Override public void printDeck(List<ClientCard> deck) {}

    public void resolveDataGame(List<Object> data) {
        inputManager.createGameDataFuture.complete(data);
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
            return commandQueue.take();  // blocca finché non riceve un comando
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
        commandQueue.offer(command);
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


    private void showNotification(String message) {
        Platform.runLater(() -> {
            Scene scene = sceneRouter.getCurrentScene();
            if (scene == null || scene.getRoot() == null) {
                System.err.println("[GUIView] inform: scena o root null");
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
                -fx-background-color: rgba(255, 223, 0, 0.85); /* giallo oro semi-trasparente */
                -fx-padding: 10px 18px;
                -fx-background-radius: 0; /* rettangolare */
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
                fadeOut.setOnFinished(e -> pane.getChildren().remove(toast));

                new SequentialTransition(fadeIn, pause, fadeOut).play();

            } catch (ClassCastException e) {
                System.err.println("[GUIView] inform: root della scena non è un Pane. Non posso mostrare il toast.");
            }
        });
    }




    private boolean filterDisplayNotification(String message, SceneEnum sceneEnum) {
        if (sceneEnum == null) {
            return message.toLowerCase().contains("login successful");
        }
        return switch (sceneEnum) {
            case BUILDING_PHASE -> true;
            case WAITING_QUEUE -> !message.contains("Waiting");
            case MAIN_MENU -> !message.contains("Connected") || !message.contains("Insert") || !message.contains("Creating New Game...");
            case NICKNAME_DIALOG -> message.contains("Login");
            default -> false;
        };
    }




}