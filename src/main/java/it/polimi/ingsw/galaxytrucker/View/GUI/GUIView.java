package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.GUI.Controllers.GUIController;
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

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class GUIView extends Application implements View {
    private Stage mainStage;
    private GUIController controller;
    private CompletableFuture<int[]> coordinateFuture;
    private Tile currentTile;

    public GUIView(){
        Platform.startup(()->{});
    }


    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        this.mainStage = stage;
    }


    @Override
    public void inform(String message) {

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
            return coordinateFuture.get(); // blocca solo il thread secondario, non l'interfaccia
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{-1, -1}; // valore d'errore
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
        return "";
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

    public VirtualView setMainScene(SceneEnum sceneName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName.value()));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        this.mainStage.setScene(scene);
        return loader.getController();
    }
    public void resolveCoordinates(int row, int col) {
        if (coordinateFuture != null && !coordinateFuture.isDone()) {
            coordinateFuture.complete(new int[] {row, col});
        }
    }
}
