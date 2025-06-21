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
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GUIView extends Application implements View {
    private Stage mainStage;
    private GUIController controller;
    private final Map<SceneEnum, Scene> scenes = new HashMap<>();
    private final Map<SceneEnum, GUIController> controllers = new HashMap<>();

    private CompletableFuture<int[]> coordinateFuture;
    private CompletableFuture<String> nicknameFuture;
    private ClientController clientController;
    public ClientTile currentTile;
    public ClientTile[][] dashBoard;
    private static Map<String, int[]> mapPosition = new ConcurrentHashMap<String, int[]>();
    private Boolean[][] maschera;
    private CompletableFuture<List<Object>> dataForGame;
    private CompletableFuture<String> menuChoiceFuture;
    private SceneEnum sceneEnum;
    private ClientGamePhase gamePhase;
    private boolean isDemo;
    private static int protocolChoice;
    private static String host;
    private static int port;
    private int selectedGameId;
    private String nickname;
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
            initializeAllScenes();

        dashBoard = new ClientTile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                ClientTile tile = new ClientTile();
                tile.type = "EMPTYSPACE";
                dashBoard[i][j] = tile;
            }
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

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> reportError("Connection failed: " + e.getMessage()));
            }
        }).start();

        stage.show();
    }


    @Override
    public void inform(String message) {
        if (sceneEnum == SceneEnum.WAITING_QUEUE) {
            if(message.equals("Waiting for other players...")) return;
            showNotification(message);
        } else if (sceneEnum == SceneEnum.MAIN_MENU) {
            if (message.equals("Login successful")) {
                showNotification("You have successfully connected!");
            } else if (message.contains("connected")) {
                showNotification(message);
            }
        }
    }

    //TODO: da scrivere
    @Override
    public Boolean ask(String message) {
        return false;
    }

    //TODO: da scrivere
    @Override
    public Integer askIndexWithTimeout() {
        return 0;
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
        Integer choice = -1;
        switch (sceneEnum) {
            case JOIN_GAME_MENU -> {
                choice = waitForGameChoice();
            }
            default -> {}
        }
        return choice;
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
            try {
                String nickname = nicknameFuture.get();
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
            case WAITING_IN_LOBBY -> {
                    Platform.runLater(() -> {
                        setMainScene(sceneEnum.WAITING_QUEUE);
                    });
            }
            case BOARD_SETUP -> {
                Platform.runLater(() -> {
                    setMainScene(sceneEnum.BUILDING_PHASE);
                });
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
    public void updateMap(Map<String, int[]> map) {
        mapPosition = map;

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
        Boolean[][] validStatus = new Boolean[5][7];
        if (isDemo) {
            //first row
            validStatus[0][0]  = null;
            validStatus[0][1]  = null;
            validStatus[0][2]  = null;
            validStatus[0][3]  = true;
            validStatus[0][4]  = null;
            validStatus[0][5]  = null;
            validStatus[0][6]  = null;
            //second row
            validStatus[1][0]  = null;
            validStatus[1][1]  = null;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = null;
            validStatus[1][6]  = null;
            //third row
            validStatus[2][0]  = null;
            validStatus[2][1]  = true;
            validStatus[2][2]  = true;
            validStatus[2][3]  = true;
            validStatus[2][4]  = true;
            validStatus[2][5]  = true;
            validStatus[2][6]  =null;
            //fourth row
            validStatus[3][0]  = null;
            validStatus[3][1]  = true;
            validStatus[3][2]  = true;
            validStatus[3][3]  = true;
            validStatus[3][4]  = true;
            validStatus[3][5]  = true;
            validStatus[3][6]  = null;
            //fifth row
            validStatus[4][0]  = null;
            validStatus[4][1]  = true;
            validStatus[4][2]  = true;
            validStatus[4][3]  = null;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = null;
        } else {
            //first row
            validStatus[0][0]  = null;
            validStatus[0][1]  = null;
            validStatus[0][2]  = true;
            validStatus[0][3]  = null;
            validStatus[0][4]  = true;
            validStatus[0][5]  = true;
            validStatus[0][6]  = true;
            //second row
            validStatus[1][0]  = null;
            validStatus[1][1]  = true;
            validStatus[1][2]  = true;
            validStatus[1][3]  = true;
            validStatus[1][4]  = true;
            validStatus[1][5]  = true;
            validStatus[1][6]  = null;
            //third row
            validStatus[2][0]  = true;
            validStatus[2][1]  = true;
            validStatus[2][2]  = true;
            validStatus[2][3]  = true;
            validStatus[2][4]  = true;
            validStatus[2][5]  = true;
            validStatus[2][6]  = true;
            //fourth row
            validStatus[3][0]  = true;
            validStatus[3][1]  = true;
            validStatus[3][2]  = true;
            validStatus[3][3]  = true;
            validStatus[3][4]  = true;
            validStatus[3][5]  = true;
            validStatus[3][6]  = true;
            //fifth row
            validStatus[4][0]  = true;
            validStatus[4][1]  = true;
            validStatus[4][2]  = true;
            validStatus[4][3]  = null;
            validStatus[4][4]  = true;
            validStatus[4][5]  = true;
            validStatus[4][6]  = true;
        }
        this.maschera = validStatus;
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
            label.setStyle("-fx-padding: 12; -fx-font-size: 14px; -fx-alignment: top-left;");
            label.setWrapText(true);
            label.setMaxWidth(200);

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");

            Scene scene = new Scene(root);
            notificationStage.setScene(scene);

            double notificationWidth = 220;
            double notificationHeight = 160;

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenRight = screenBounds.getMaxX();
            double screenBottom = screenBounds.getMaxY();

            notificationStage.setX(screenRight - notificationWidth - 10);
            notificationStage.setY(screenBottom - notificationHeight - 10);

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
        setMainScene(sceneEnum);
    }

    public void initializeAllScenes(){
        for (SceneEnum scene : SceneEnum.values()) {
            String path = scene.value();
            URL resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("ERROR: Resource not found for scene: " + scene.name() + " at path: " + path);
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                GUIController controller = loader.getController();

                if (controller == null) {
                    System.err.println("ERROR: Controller is null for scene: " + scene.name());
                    continue;
                }

                controller.setGuiView(this);
                controllers.put(scene, controller);
                scenes.put(scene, new Scene(root));
            } catch (IOException | RuntimeException e) {
                System.err.println("ERROR while loading scene: " + scene.name() + " at path: " + path);
                e.printStackTrace();
            }
        }
    }


    public void setMainScene(SceneEnum sceneName) {
        Scene scene = scenes.get(sceneName);
        if (scene == null) {
            System.err.println("[ERROR] Scene not found for: " + sceneName);
            return;
        }

        GUIController controller = controllers.get(sceneName);
        if (controller == null) {
            System.err.println("[ERROR] Controller not found for: " + sceneName);
            return;
        }

        this.controller = controller;

        mainStage.setScene(scene);
        mainStage.centerOnScreen();
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
                    setMainScene(SceneEnum.CREATE_GAME_MENU);
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
        Platform.runLater(() -> {
            ObservableList<String> gameDescriptions = FXCollections.observableArrayList();

            for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
                int id = entry.getKey();
                int[] info = entry.getValue();
                boolean isDemo = info[2] == 1;
                String suffix = isDemo ? " DEMO" : "";
                String desc = id + ". Players in game: " + info[0] + "/" + info[1] + suffix;
                gameDescriptions.add(desc);
            }

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



    public void setSelectedGameId(Integer gameId) {
        synchronized (lock) {
            this.selectedGameId = gameId;
            lock.notifyAll();
        }
    }

    public Boolean getIsDemo(){
        return isDemo;
    }

    public Integer waitForGameChoice() {
        synchronized (lock) {
            selectedGameId = -10;

            try {
                while (selectedGameId == -10) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
            return selectedGameId;
        }
    }
    @Override
    public void setTile(ClientTile tile, int row, int col) {
        dashBoard[row][col] = tile;
    }
    @Override
    public void setCurrentTile(ClientTile tile) {

    }


    @Override
    public boolean returnValidity(int a , int b){
        return maschera[a][b];
    }
    @Override
    public void setValidity(int a , int b){
        maschera[a][b] = false;
    }
    @Override
    public void resetValidity(int a , int b){
        maschera[a][b] = true;
    }
    @Override
    public void setNickName(String nickname) {
        this.nickname = nickname;
        this.controller.setNickname(nickname);
    }
    public String getNickname() {
        return nickname;
    }
}
