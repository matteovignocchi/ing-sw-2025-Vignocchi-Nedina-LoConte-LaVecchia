package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.Client.ClientController;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.NicknameDialogController;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
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
    public List<Object> dataForGame;
    private SceneEnum sceneEnum;
    public GUIView(){
        Platform.startup(()->{});

    }



    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        this.mainStage = stage;

    }


    @Override
    public void inform(String message) {
        if( sceneEnum == SceneEnum.WAITING_QUEUE){
            //TODO fare metodo che mette messaggi sullo schermo di chi si è connesso
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
        nicknameFuture = new CompletableFuture<>();

        Platform.runLater(this::showNicknameDialog);

        try {
            return nicknameFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void reportError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);

            if (mainStage != null) {
                alert.initOwner(mainStage);
            }

            alert.showAndWait();
        });
    }


    @Override
    public void updateState(GamePhase gamePhase) {

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

    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    public ClientController getClientController() {
        return clientController;
    }
    private void setSceneEnum(SceneEnum sceneEnum) {
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

    private void showNicknameDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/nicknameDialog.fxml"));
            Parent root = loader.load();

            NicknameDialogController dialogController = loader.getController();
            dialogController.setGuiView(this);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Insert your nickname");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resolveNickname(String nickname) {
        if (nicknameFuture != null && !nicknameFuture.isDone()) {
            nicknameFuture.complete(nickname);
        }
    }

    public void resolveDataGame(List<Object> dataGame) {
        this.dataForGame = dataGame;
    }

    public List<Object> getDataForGame() {
        return dataForGame;
    }

}
