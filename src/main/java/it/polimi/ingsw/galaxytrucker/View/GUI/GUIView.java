package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.Client.*;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.*;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GUIView extends Application implements View {
    private Stage mainStage;
    private GUIController controller;
    private CompletableFuture<int[]> coordinateFuture;
    private CompletableFuture<String> nicknameFuture;
    private ClientController clientController;
    public ClientTile currentTile;
    public ClientTile[][] dashBoard;
    private CompletableFuture<List<Object>> dataForGame;
    private CompletableFuture<String> menuChoiceFuture;
    private SceneEnum sceneEnum;
    private ClientGamePhase gamePhase;
    private boolean demo;
    private static int protocolChoice;
    private static String host;
    private static int port;
    private int selectedGameId = -1;
    private final Object lock = new Object();

    public static void setStartupConfig(int protocol, String h, int p) {
        protocolChoice = protocol;
        host = h;
        port = p;
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        stage.setTitle("Galaxy Trucker");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneEnum.MAIN_MENU.value()));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setGuiView(this);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        new Thread(() -> {
            try {
                VirtualView virtualClient;
                if (protocolChoice == 1) {
                    Registry registry = LocateRegistry.getRegistry(host, 1099);
                    VirtualServer server = (VirtualServer) registry.lookup("RmiServer");
                    virtualClient = new VirtualClientRmi(server);
                } else {
                    virtualClient = new VirtualClientSocket(host, port);
                }

                ClientController controller = new ClientController(this, virtualClient);
                this.setClientController(controller);

                controller.start();

                Platform.runLater(() -> {
                    try {
                        setMainScene(SceneEnum.MAIN_MENU);
                    } catch (IOException e) {
                        reportError("Failed to load main menu: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> reportError("Connection failed: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    public void inform(String message) {
        if (message.equals("Login successful")) {
            showNotification("You have successfully connected!");
        } else if (message.contains("connected")) {
            showNotification(message);
        } else if (sceneEnum == SceneEnum.WAITING_QUEUE) {
            // TODO: fare metodo che mette messaggi sullo schermo di chi si è connesso
        } else if (sceneEnum == SceneEnum.MAIN_MENU) {
            // Gestione altri messaggi nel menu principale
        }
    }

    //TODO: da scrivere
    @Override
    public Boolean ask(String message) {
        return false;
    }

    //TODO: da scrivere
    @Override
    public boolean askWithTimeout(String message) {return false;}

    //TODO: da scrivere
    @Override
    public int[] askCoordinatesWithTimeout() {
        return new int[0];
    }

    @Override
    public int[] askCoordinate() throws IOException, InterruptedException {
        coordinateFuture = new CompletableFuture<>();

        Platform.runLater(() -> {
            controller.setGuiView(this);
            controller.setCurrentTile(currentTile);
        });

        try {
            return coordinateFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{-1, -1};
        }
    }

    @Override
    public void printMapPosition() {}

    @Override
    public Integer askIndex() {
        if(sceneEnum == SceneEnum.JOIN_GAME_MENU) {
            return waitForGameChoice();
        }
        return 0;
    }

    @Override
    public void setInt() {

    }

    @Override
    public void start() {
    }

    @Override
    public void printListOfGoods(List<String> Goods) {

    }

    @Override
    public void printDashShip(ClientTile[][] ship) {
        controller.setDashBoard(ship);
    }

    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {

    }

    @Override
    public void printNewFase(String gamePhase) {

    }

    @Override
    public void printDeck(List<ClientCard> deck) {

    }

    @Override
    public void printPileCovered() {

    }

    @Override
    public void printPileShown(List<ClientTile> tiles) {

    }

    @Override
    public String askString() {
        while (sceneEnum != SceneEnum.MAIN_MENU && sceneEnum != SceneEnum.NICKNAME_DIALOG) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "";
            }
        }

        if (sceneEnum == SceneEnum.NICKNAME_DIALOG) {
            nicknameFuture = new CompletableFuture<>();
            Platform.runLater(this::showNicknameDialog);
            try {
                String nickname = nicknameFuture.get();
                sceneEnum = null;
                return nickname;
            } catch (Exception e) {
                reportError("Can not load the nickname : " + e.getMessage());
                return "";
            } finally {
                nicknameFuture = null;
            }
        }

        if (sceneEnum == SceneEnum.MAIN_MENU) {
            menuChoiceFuture = new CompletableFuture<>();
            try {
                return menuChoiceFuture.get();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            } finally {
                sceneEnum = null;
                menuChoiceFuture = null;
            }
        }

        return "";
    }

    @Override
    public void reportError(String message) {
        System.err.println("[GUIView] reportError: " + message);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message != null && !message.isBlank() ? message : "Error.");

            if (mainStage != null) {
                alert.initOwner(mainStage);
            }

            alert.showAndWait();
        });
    }

    @Override
    public void updateState(ClientGamePhase gamePhase) {
        this.gamePhase = gamePhase;
        switch (gamePhase){
            case WAITING_IN_LOBBY -> sceneEnum = SceneEnum.WAITING_QUEUE;
            case BOARD_SETUP -> {
                if (demo) {
                    sceneEnum = SceneEnum.BUILDING_PHASE_DEMO;
                } else {
                    sceneEnum = SceneEnum.BUILDING_PHASE;
                }
            }
            case WAITING_FOR_PLAYERS -> {}//Aspetto che gli altri finiscano di completare la nave
            case WAITING_FOR_TURN -> {} //aspetto il mio turno di scelta
            case DRAW_PHASE -> {} //Metto possibilità di pescare
            case SCORING -> {} //lo metterò nella scena dello scoring
            case EXIT -> {} //boh ci ragioniamo
        }
    }

    @Override
    public void printTile(ClientTile tile) {
        this.currentTile = tile;
    }

    @Override
    public void printCard(ClientCard card) {

    }

    @Override
    public String sendAvailableChoices() throws Exception {
        return "";
    }

    @Override
    public void updateMap(Map<String, Integer> map) {

    }

    @Override
    public String choosePlayer() {
        return "";
    }

    @Override
    public void printListOfCommand() {

    }

    @Override
    public void setIsDemo(Boolean demo) {
        this.demo = demo;
    }

    @Override
    public boolean ReturnValidity(int a, int b) {
        return false;
    }

    @Override
    public void setValidity(int a, int b) {

    }

    public void showNotification(String message) {
        Platform.runLater(() -> {
            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.UTILITY);
            notificationStage.initModality(Modality.NONE);
            notificationStage.initOwner(mainStage);
            notificationStage.setAlwaysOnTop(true);
            notificationStage.setResizable(false);
            notificationStage.setTitle("Notification");

            Label label = new Label(message);
            label.setStyle("-fx-padding: 10; -fx-font-size: 14;");
            Scene scene = new Scene(new StackPane(label));
            notificationStage.setScene(scene);

            double notificationWidth = 250;
            double notificationHeight = 80;
            double mainX = mainStage.getX();
            double mainY = mainStage.getY();
            double mainWidth = mainStage.getWidth();
            double mainHeight = mainStage.getHeight();

            notificationStage.setX(mainX + mainWidth - notificationWidth - 10);
            notificationStage.setY(mainY + mainHeight - notificationHeight - 10);

            notificationStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> notificationStage.close());
            delay.play();
        });
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    public ClientController getClientController() {
        return clientController;
    }
    public void setSceneEnum(SceneEnum sceneEnum) {
        this.sceneEnum = sceneEnum;
    }
    public void setMainScene(SceneEnum sceneName) throws IOException {
        setSceneEnum(sceneName);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName.value()));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        this.mainStage.setScene(scene);
        this.mainStage.centerOnScreen();
        Object controller = loader.getController();
        //TODO ricordarsi di mettere tutti i controller quando si scrive
        switch (controller) {
            case MainMenuController c -> {
                c.setGuiView(this);
                this.controller = c;
            }
            case CreateGameMenuController c -> {
                c.setGuiView(this);
                this.controller = c;
            }
            case GameListMenuController c -> {
                c.setGuiView(this);
                this.controller = c;
            }
            case NicknameDialogController c -> {
                c.setGuiView(this);
            }
            case BuildingPhaseController c -> {
                c.setGuiView(this);
                this.controller = c;
            }
            case WaitingQueueController c ->{
                c.setGuiView(this);
                this.controller = c;
            }
            default -> {}
        }
    }

    public void resolveCoordinates(int row, int col) {
        if (coordinateFuture != null && !coordinateFuture.isDone()) {
            coordinateFuture.complete(new int[] {row, col});
        }
    }

    public void resolveMenuChoice(String choice) {
        if (menuChoiceFuture != null && !menuChoiceFuture.isDone()) {
            sceneEnum = SceneEnum.MAIN_MENU;
            menuChoiceFuture.complete(choice);
        }
    }


    public void showNicknameDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneEnum.NICKNAME_DIALOG.value()));
            Parent root = loader.load();
            NicknameDialogController controller = loader.getController();
            controller.setGuiView(this);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainStage);
            dialogStage.setResizable(false);
            dialogStage.setTitle("Insert your nickname");

            dialogStage.setOnCloseRequest(event -> {
                if (nicknameFuture != null && !nicknameFuture.isDone()) {
                    nicknameFuture.complete("");
                }
            });

            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            if (nicknameFuture != null && !nicknameFuture.isDone()) {
                nicknameFuture.completeExceptionally(e);
            }
        }
    }

    public void resolveNickname(String nickname) {
        if (nicknameFuture != null && !nicknameFuture.isDone()) {
            nicknameFuture.complete(nickname);
            nicknameFuture = null;
        }
    }


    public void resolveDataGame(List<Object> dataGame) {
        if (dataForGame != null && !dataForGame.isDone()) {
            dataForGame.complete(dataGame);
        }
    }

    public List<Object> getDataForGame() {
        if (dataForGame == null) {
            dataForGame = new CompletableFuture<>();
            try {
                Platform.runLater(() -> {
                    try {
                        setMainScene(SceneEnum.CREATE_GAME_MENU);
                    } catch (IOException e) {
                        reportError("Cannot load game creation screen: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return dataForGame.get();
        } catch (Exception e) {
            reportError("Error waiting for game data: " + e.getMessage());
            return List.of(false, 2);
        }
    }

    public ClientGamePhase getGamePhase() { return gamePhase; }

    @Override
    public void displayAvailableGames(Map<Integer, int[]> availableGames) {
        ObservableList<String> gameDescriptions = FXCollections.observableArrayList();

        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
            int id = entry.getKey();
            int[] info = entry.getValue();
            boolean isDemo = info[2] == 1;
            String suffix = isDemo ? " DEMO" : "";
            String desc = id + ". Players in game: " + info[0] + "/" + info[1] + suffix;
            gameDescriptions.add(desc);
        }

        Platform.runLater(() -> {
            if (controller != null) {
                try {
                    ((GameListMenuController) controller).displayGames(gameDescriptions);
                } catch (ClassCastException e) {
                    reportError("Controller is not GameListMenuController");
                }
            } else {
                reportError("Controller is null");
            }
        });
    }



    public void setSelectedGameId(int gameId) {
        synchronized (lock) {
            this.selectedGameId = gameId;
            lock.notifyAll();
        }
    }

    public int waitForGameChoice() {
        synchronized (lock) {
            while (selectedGameId == -1) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }
            int choice = selectedGameId;
            selectedGameId = -1;
            return choice;
        }
    }


    @Override
    public void resetValidity(int a , int b) {}
}
