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
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GameListMenuController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.MainMenuController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.NicknameDialogController;
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
        if( sceneEnum == SceneEnum.WAITING_QUEUE){
            //TODO fare metodo che mette messaggi sullo schermo di chi si è connesso
        } else if (sceneEnum == SceneEnum.MAIN_MENU){

        }
    }

    @Override
    public boolean ask(String message) {
        return false;
    }

    @Override
    public int[] askCoordinate() {
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
    public int askIndex() {
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

                e.printStackTrace();
                reportError("Can not load the nickname : " + e.getMessage());
                return "";
            } finally {
                nicknameFuture = null;
            }
        } else if (sceneEnum == SceneEnum.MAIN_MENU) {
            menuChoiceFuture = new CompletableFuture<>();
            try {
                return menuChoiceFuture.get();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
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
        loader.getController();
    }

    public void resolveCoordinates(int row, int col) {
        if (coordinateFuture != null && !coordinateFuture.isDone()) {
            coordinateFuture.complete(new int[] {row, col});
        }
    }

    public void resolveMenuChoice(String choice) {
        if (menuChoiceFuture != null && !menuChoiceFuture.isDone()) {
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

    public void resolveGameChoice(int gameChoice){
        this.gameChoice = gameChoice;
    }

    public int getGameChoice() {
        return gameChoice;
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
            return dataForGame.get(); // <-- blocca finché il controller non chiama resolve
        } catch (Exception e) {
            reportError("Error waiting for game data: " + e.getMessage());
            return List.of(false, 2); // fallback
        }
    }


    public void updateAvailableGames(Map<Integer, int[]> availableGames) {
        Platform.runLater(() -> {
            Scene currentScene = mainStage.getScene();
            if (currentScene == null) return;

            Object controller = currentScene.getUserData();

            switch (controller) {
                case GameListMenuController gameListController -> {
                    ObservableList<String> gameDescriptions = FXCollections.observableArrayList();

                    if (availableGames == null || availableGames.isEmpty()) {
                        gameDescriptions.add("No available games");
                    } else {
                        for (Map.Entry<Integer, int[]> entry : availableGames.entrySet()) {
                            int id = entry.getKey();
                            int[] info = entry.getValue();
                            String description = id + ". Players in game: " + info[0] + "/" + info[1];
                            if (info.length > 2 && info[2] == 1) {
                                description += " DEMO";
                            }
                            gameDescriptions.add(description);
                        }
                    }

                    gameListController.displayGames(gameDescriptions);
                }
                default -> {
                }
            }
        });
    }

    public GamePhase getGamePhase() { return gamePhase; }

    @Override
    public String askStringNonBlocking() { return "";}

    @Override
    public int askIndexNonBlocking() {return 0;}

    @Override
    public int[] askCoordinateNonBlocking () { return null;}

    @Override
    public boolean askNonBlocking(String message) { return false;}

    @Override
    public String choosePlayerNonBlocking() throws IOException{return "";}

}
