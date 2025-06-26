package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.*;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.*;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.*;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
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
    private boolean previewingEnemyDashboard = false;
    private String bufferedPlayerName = null;
    private volatile Boolean bufferedBoolean;
    private boolean showGoodActionPrompt = false;
    private List<String> bufferedGoods = List.of();
    private volatile long lastAskCoordinateTimestamp = 0;
    private volatile long lastAskIndexTimestamp = 0;




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
        }
    }

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
                System.err.println("[GUIView] Errore nel cast del root pane: " + e.getMessage());
            }
        });
    }

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

    @Override
    public int[] askCoordinate() {
        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.enableDashboardCoordinateSelection(coords -> setBufferedCoordinate(coords));
            } else {
                reportError("GameController non disponibile.");
            }
        });
        long deadline = System.currentTimeMillis() + 20_000; // 20 secondi
        while (bufferedCoordinate == null) {
            if (System.currentTimeMillis() > deadline) {
                reportError("Timeout su askCoordinate.");
                return new int[]{-1, -1};
            }

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
    public void setBufferedBoolean(Boolean value) {this.bufferedBoolean = value;}

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
                        reportError("Done button non trovato nel file PrintDash.fxml.");
                    }

                    popup.show();

                } catch (IOException e) {
                    reportError("Errore caricando PrintDash.fxml: " + e.getMessage());
                }
            });
        } else {
            model.setDashboard(ship);
            Platform.runLater(() -> {
                BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
                GameController ctrl2 = (GameController) sceneRouter.getController(GAME_PHASE);
                Scene currentScene = sceneRouter.getCurrentScene();
                Scene gameScene = sceneRouter.getScene(GAME_PHASE);
                //TODO CAPIRE SE SERVE
                if (ctrl2 != null && currentScene == gameScene) {
                    ctrl2.updateDashboard(ship);
                } else {
                    System.out.println("[DEBUG] GameController non inizializzato o scena non attiva. Salto updateDashboard.");
                }
            });
        }
    }




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

    public Map<String, int[]> getPlayerPositions() {
       return model.getPlayerPositions();
    }

    public void updateState(ClientGamePhase newPhase) {

        //TODO CAPIRE SE SERVE
        System.out.println("[DEBUG] updateState ricevuto: " + newPhase);
        if (newPhase == this.gamePhase) {
            System.out.println("[DEBUG] Fase invariata, non aggiorno: " + newPhase);
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
//                    sceneRouter.getController(BUILDING_PHASE).postInitialize();
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
                        reportError("Controller EXIT_PHASE non disponibile.");
                    }
                }
                case WAITING_FOR_TURN  , WAITING_FOR_PLAYERS->{
                    setSceneEnum(GAME_PHASE);
                    GUIController controller = sceneRouter.getController(GAME_PHASE);

                    controller.postInitialize();
                }
                case CARD_EFFECT -> {
                    setSceneEnum(GAME_PHASE);
                    GUIController controller = sceneRouter.getController(GAME_PHASE);
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
            setSceneEnum(SceneEnum.JOIN_GAME_MENU); // ✅ Solo ora cambiamo scena
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

    @Override
    public Integer askIndexWithTimeout() {
        long now = System.currentTimeMillis();
        if (now - lastAskIndexTimestamp < 300) {
            System.out.println("[DEBUG] askIndexWithTimeout ignorato (chiamata duplicata)");
            return -1;
        }

        lastAskIndexTimestamp = now;
        long deadline = now + 20_000;

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
                reportError("Errore nel caricamento PrintListOfGood.fxml: " + ex.getMessage());
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
            reportError("Timeout su askIndex.");
            return 0;
        }

        int res = bufferedIndex;
        bufferedIndex = null;


        System.out.println("[DEBUG] Indice good confermato: " + res);
        return res;
    }


    @Override
    public boolean askWithTimeout(String message) {
        long timeout = 20_000; // 20 secondi
        long deadline = System.currentTimeMillis() + timeout;
        if (message != null && message.startsWith("SERVER:")) {
            message = message.substring("SERVER:".length()).strip(); // rimuove e pulisce spazi
        }
        bufferedBoolean = null; // reset

        String finalMessage = message;
        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(GAME_PHASE);
            if (ctrl != null) {
                ctrl.showYesNoButtons(finalMessage); // mostra pulsanti nella GUI
            } else {
                reportError("Controller non disponibile per askWithTimeout.");
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
            reportError("Timeout su askWithTimeout.");
            return false;
        }

        boolean result = bufferedBoolean;
        bufferedBoolean = null;
        return result;
    }


    @Override
    public int[] askCoordinatesWithTimeout() {
        lastAskCoordinateTimestamp = System.currentTimeMillis();

        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(SceneEnum.GAME_PHASE);
            if (ctrl != null) {
                ctrl.enableDashboardCoordinateSelection(coords -> setBufferedCoordinate(coords));
            } else {
                reportError("GameController non disponibile.");
            }
        });

        long deadline = System.currentTimeMillis() + 20_000; // 20 secondi
        while (bufferedCoordinate == null) {
            if (System.currentTimeMillis() > deadline) {
                reportError("Timeout su askCoordinate.");
                return new int[]{-1, -1};
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new int[]{-1, -1};
            }
        }

        int[] result = bufferedCoordinate;
        bufferedCoordinate = null;

        // Se entro 500ms askCoordinate non è richiamato di nuovo, consideriamo confermato
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException ignored) {}
//        if (System.currentTimeMillis() - lastAskCoordinateTimestamp > 400) {
//            System.out.println("[DEBUG] Coordinate confermate: " + Arrays.toString(result));
//        }

        return result;
    }



    @Override
    public String choosePlayer() {
        if (bufferedPlayerName != null) {
            String result = bufferedPlayerName;
            return result;
        } else {
            reportError("Nessun nome giocatore selezionato.");
            return null;
        }
    }


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

    @Override
    public void printListOfGoods(List<String> goods) {
        System.out.println("[LOG] printListOfGoods() chiamato con: " + goods);

        this.bufferedGoods = goods;
        this.showGoodActionPrompt = true;

        System.out.println("[LOG] showGoodActionPrompt = true");

    }
    @Override public void printMapPosition() {}

    @Override public void printPileCovered() {}
    @Override
    public void printPileShown(List<ClientTile> tiles) {
        Platform.runLater(() -> {
            model.setCurrentTile(null);
            setBufferedIndex(null);
            BuildingPhaseController ctrl = (BuildingPhaseController) sceneRouter.getController(SceneEnum.BUILDING_PHASE);
            ctrl.displayTileSelection(tiles);
        });
    }
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


    @Override
    public void printDeck(List<ClientCard> deck) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PrintDeck.fxml"));
                AnchorPane root = loader.load();

                // Ottieni i tre pannelli (meglio usare fx:id se possibile)
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

                // Crea una nuova finestra
                Stage popupStage = new Stage();
                popupStage.setTitle("Deck");

                // Imposta la scena
                Scene popupScene = new Scene(root);
                popupStage.setScene(popupScene);

                // Centra la finestra sullo schermo
                popupStage.centerOnScreen();

                // (Opzionale) Imposta come modale rispetto alla finestra principale
                // popupStage.initModality(Modality.WINDOW_MODAL);
                // popupStage.initOwner(mainStage);
                Button done = (Button) root.lookup("#done");
                if (done != null) {
                    done.setOnAction(e -> popupStage.close());
                } else {
                    reportError("Done button non trovato nel file FXML.");
                }

                popupStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                reportError("Errore nel caricamento del file PrintDeck.fxml: " + e.getMessage());
            }
        });
    }




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
            case "DECK" -> "watchadeck";
            case "LOGOUT" -> "logout";
            case "DRAW" -> "drawacard";
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


    public void showNotification(String message) {
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
                double yOffset = scene.getHeight() - 90;

                toast.setTranslateX(xOffset);
                toast.setTranslateY(yOffset);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(100), toast);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                PauseTransition pause = new PauseTransition(Duration.seconds(3));

                FadeTransition fadeOut = new FadeTransition(Duration.millis(100), toast);
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
        if (message.strip().startsWith("SELECT:\n 1. Add good\n 2. Rearranges the goods\n 3. Trash some goods")) {
            return false;
        }
        if (sceneEnum == null) {
            return message.toLowerCase().contains("login successful");
        }
        if(message.toLowerCase().contains("waiting for other players...")) {
            return false;
        }
        if(message.toLowerCase().contains("choose deck")) {
            return false;
        }
        if(gamePhase == ClientGamePhase.TILE_MANAGEMENT || gamePhase == ClientGamePhase.TILE_MANAGEMENT_AFTER_RESERVED){
            return message.toLowerCase().contains("hourglass");
        }

        return switch (sceneEnum) {
            case BUILDING_PHASE -> !message.toLowerCase().contains("rotate");
            case WAITING_QUEUE -> message.toLowerCase().contains("joined");
            case MAIN_MENU -> !message.contains("Connected") || !message.contains("Insert") || !message.contains("Creating New Game...") || message.toLowerCase().contains("login successful");
            case NICKNAME_DIALOG -> message.contains("Login");
            case GAME_PHASE -> true;
            default -> false;
        };
    }
    public void prepareToViewEnemyDashboard(String enemyName) {
        this.previewingEnemyDashboard = true;
        this.bufferedPlayerName = enemyName;
        System.out.println(""+ enemyName);
    }
    public void preparePlayerCoordinateInput() {
        Platform.runLater(() -> {
            GameController ctrl = (GameController) sceneRouter.getController(GAME_PHASE);
            ctrl.enableDashboardCoordinateSelection(coords -> {
                setBufferedCoordinate(coords);
                resolveGenericCommand("COORD_SELECTED");
            });
        });
    }

    public void ErPuzzo(){
    }





    public void triggerGoodActionPrompt() {
        this.showGoodActionPrompt = true;
    }

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

    public void resetGUIState() {
        clearModelAndBuffers();
        sceneRouter.reinitializeAllScenes();
        menuChoiceQueue.clear();
        commandQueue.clear();
        notificationQueue.clear();
        isShowingNotification = false;
        gamePhase = null;
        sceneEnum = null;
        System.out.println("[DEBUG] GUIView state resettato completamente.");
    }


}