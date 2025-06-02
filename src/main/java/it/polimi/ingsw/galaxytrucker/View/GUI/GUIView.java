package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.Client.VirtualClientRmi;
import it.polimi.ingsw.galaxytrucker.Client.VirtualClientSocket;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.*;
import it.polimi.ingsw.galaxytrucker.View.View;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUIView extends Application implements View {
    private Stage mainStage;
    private GUIController controller;
    private CompletableFuture<int[]> coordinateFuture;
    private CompletableFuture<String> nicknameFuture;
    private ClientController clientController;
    public Tile currentTile;
    public Tile[][] dashBoard;
    private CompletableFuture<List<Object>> dataForGame;
    private CompletableFuture<String> menuChoiceFuture;
    public int gameChoice;
    private SceneEnum sceneEnum;
    private GamePhase gamePhase;
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
        if (sceneEnum == SceneEnum.WAITING_QUEUE) {
            // TODO: fare metodo che mette messaggi sullo schermo di chi si è connesso
        } else if (sceneEnum == SceneEnum.MAIN_MENU) {

        }
    }

    @Override
    public Boolean ask(String message) {
        return false;
    }

    @Override
    public boolean askWithTimeout(String message) {return false;}

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
        return 0;
    }

    @Override
    public void setInt() {

    }

    @Override
    public void start() {
    }

    @Override
    public void printListOfGoods(List<Colour> Goods) {

    }

    @Override
    public void printDashShip(Tile[][] ship) {
        controller.setDashBoard(ship);
    }

    @Override
    public void updateView(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {

    }

    @Override
    public void printNewFase(GamePhase gamePhase) {

    }

    @Override
    public void printDeck(List<Card> deck) {

    }

    @Override
    public void printPileCovered() {

    }

    @Override
    public void printPileShown(List<Tile> tiles) {

    }

    @Override
    public String askString() {
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
            alert.setContentText(message != null && !message.isBlank() ? message : "Errore sconosciuto.");

            if (mainStage != null) {
                alert.initOwner(mainStage);
            }

            alert.showAndWait();
        });
    }

    @Override
    public void updateState(GamePhase gamePhase) {
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
    public void printTile(Tile tile) {
        this.currentTile = tile;
    }

    @Override
    public void printCard(Card card) {

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


    public GamePhase getGamePhase() { return gamePhase; }

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

            if (controller instanceof GameListMenuController gameController) {
                gameController.displayGames(gameDescriptions);
            } else {
                reportError("Controller is not GameListMenuController");
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
}
