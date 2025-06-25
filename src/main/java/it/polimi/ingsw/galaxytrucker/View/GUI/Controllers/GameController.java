package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameController extends GUIController {

    @FXML private GridPane imageShip;
    @FXML private ImageView ship1;
    @FXML private ImageView ship2;
    @FXML private ImageView dashboard1;
    @FXML private ImageView dashboard2;
    @FXML private Pane demo1;
    @FXML private Pane demo2;
    @FXML private Pane demo3;
    @FXML private Pane demo4;
    @FXML private Pane demo5;
    @FXML private Pane demo6;
    @FXML private Pane demo7;
    @FXML private Pane demo8;
    @FXML private Pane demo9;
    @FXML private Pane demo10;
    @FXML private Pane demo11;
    @FXML private Pane demo12;
    @FXML private Pane demo13;
    @FXML private Pane demo14;
    @FXML private Pane demo15;
    @FXML private Pane demo16;
    @FXML private Pane demo17;
    @FXML private Pane demo18;

    @FXML private Pane path1;
    @FXML private Pane path2;
    @FXML private Pane path3;
    @FXML private Pane path4;
    @FXML private Pane path5;
    @FXML private Pane path6;
    @FXML private Pane path7;
    @FXML private Pane path8;
    @FXML private Pane path9;
    @FXML private Pane path10;
    @FXML private Pane path11;
    @FXML private Pane path12;
    @FXML private Pane path13;
    @FXML private Pane path14;
    @FXML private Pane path15;
    @FXML private Pane path16;
    @FXML private Pane path17;
    @FXML private Pane path18;
    @FXML private Pane path19;
    @FXML private Pane path20;
    @FXML private Pane path21;
    @FXML private Pane path22;
    @FXML private Pane path23;
    @FXML private Pane path24;
    @FXML private Pane cardPane;
    @FXML private Label nicknametext;
    @FXML private Label credits;
    @FXML private Label enginepower;
    @FXML private Label firepower;
    @FXML private Label purplealien;
    @FXML private Label brownalien;
    @FXML private Label numofhumans;
    @FXML private Label energycell;



    @FXML private TextFlow messageTextFlow;
    @FXML private Text messageText;
    @FXML private Button yesButton;
    @FXML private Button noButton;

    @FXML private Button playerShip1Btn, playerShip2Btn, playerShip3Btn;
    @FXML private Button logout , DrawButton;
    private final Map<Integer, Pane> demoMap = new HashMap<>();
    private final Map<Integer, Pane> pathMap = new HashMap<>();
    private ClientCard currentCard;

    public void initialize() {
        playerShip1Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip1Btn.getUserData());
            completeCommand("LOOK_PLAYER1");
        });
        playerShip2Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip2Btn.getUserData());
            completeCommand("LOOK_PLAYER2");
        });
        playerShip3Btn.setOnAction(e -> {
            guiView.prepareToViewEnemyDashboard((String) playerShip3Btn.getUserData());
            completeCommand("LOOK_PLAYER3");
        });
        logout.setOnAction(e -> completeCommand("LOGOUT"));
        DrawButton.setOnAction(e -> completeCommand("DRAW"));

        demoMap.put(1, demo1);
        demoMap.put(2, demo2);
        demoMap.put(3, demo3);
        demoMap.put(4, demo4);
        demoMap.put(5, demo5);
        demoMap.put(6, demo6);
        demoMap.put(7, demo7);
        demoMap.put(8, demo8);
        demoMap.put(9, demo9);
        demoMap.put(10, demo10);
        demoMap.put(11, demo11);
        demoMap.put(12, demo12);
        demoMap.put(13, demo13);
        demoMap.put(14, demo14);
        demoMap.put(15, demo15);
        demoMap.put(16, demo16);
        demoMap.put(17, demo17);
        demoMap.put(18, demo18);
        pathMap.put(1, path1);
        pathMap.put(2, path2);
        pathMap.put(3, path3);
        pathMap.put(4, path4);
        pathMap.put(5, path5);
        pathMap.put(6, path6);
        pathMap.put(7, path7);
        pathMap.put(8, path8);
        pathMap.put(9, path9);
        pathMap.put(10, path10);
        pathMap.put(11, path11);
        pathMap.put(12, path12);
        pathMap.put(13, path13);
        pathMap.put(14, path14);
        pathMap.put(15, path15);
        pathMap.put(16, path16);
        pathMap.put(17, path17);
        pathMap.put(18, path18);
        pathMap.put(19, path19);
        pathMap.put(20, path20);
        pathMap.put(21, path21);
        pathMap.put(22, path22);
        pathMap.put(23, path23);
        pathMap.put(24, path24);
    }
    private void completeCommand(String command) {
        guiView.resolveGenericCommand(command);
    }

    private void setPlayersButton() {
        Map<String, int[]> mapPosition = model.getPlayerPositions();
        List<String> others = mapPosition.keySet().stream()
                .filter(name -> !name.equals(model.getNickname())).toList();

        switch (others.size()) {
            case 1 -> {
                playerShip1Btn.setVisible(true);
                String name = others.getFirst();
                playerShip1Btn.setText("Player Ship of " + name);
                playerShip1Btn.setUserData(name);
            }
            case 2 -> {
                String name1 = others.getFirst();
                String name2 = others.getLast();
                playerShip2Btn.setVisible(true);
                playerShip3Btn.setVisible(true);
                playerShip2Btn.setText("Player Ship of " + name1);
                playerShip2Btn.setUserData(name1);
                playerShip3Btn.setText("Player Ship of " + name2);
                playerShip3Btn.setUserData(name2);
            }
            case 3 -> {
                String name1 = others.getFirst();
                String name2 = others.get(1);
                String name3 = others.getLast();
                playerShip1Btn.setVisible(true);
                playerShip2Btn.setVisible(true);
                playerShip3Btn.setVisible(true);
                playerShip1Btn.setText("Player Ship of " + name1);
                playerShip1Btn.setUserData(name1);
                playerShip2Btn.setText("Player Ship of " + name2);
                playerShip2Btn.setUserData(name2);
                playerShip3Btn.setText("Player Ship of " + name3);
                playerShip3Btn.setUserData(name3);
            }
        }
    }


    public void updateMapPosition(Map<String, int[]> playerMaps, boolean isDemo) {
        Map<Integer, Pane> paneMap = isDemo ? demoMap : pathMap;

        // Pulisci tutto
        paneMap.values().forEach(p -> p.getChildren().clear());

        for (Map.Entry<String, int[]> entry : playerMaps.entrySet()) {
            int[] pos = entry.getValue();
            if (pos == null || pos.length < 2) continue;

            int position = pos[0]; // es. da 1 a 24
            int shipId = pos[3];   // id della nave associata

            Pane cell = paneMap.get(position);
            if (cell == null) continue;

            ImageView ship = new ImageView(getShipImage(shipId));
            ship.setFitWidth(40);
            ship.setFitHeight(40);

            cell.getChildren().add(ship);
        }
    }
    private Image getShipImage(int id) {
        String path = switch (id){
            case 33 ->  "/BlueRocket.png";
            case 34 ->  "/GreenRocket.png";
            case 52 ->  "/RedRocket.png";
            case 61 ->  "/YellowRockt.png";
            default -> "/placeholder.png";
        };
        return new Image(getClass().getResourceAsStream(path));
    }
    @Override
    public void postInitialize() {
        initializeGrid(); // inizializza le celle della dashboard
        updateDashboard(model.getDashboard());
        setCommandVisibility(model.isDemo());
    }
    @Override
    public void postInitialize2(){
        DrawButton.setVisible(true);
        DrawButton.setDisable(false);
    }
    public void postInitialize3(){
        playerShip1Btn.setVisible(false);
        playerShip2Btn.setVisible(false);
        playerShip3Btn.setVisible(false);

    }


    private void setCommandVisibility(boolean demo) {
        // Mostra/nasconde sfondi o bottoni demo in base al flag
        // esempio: demo1.setVisible(demo);
        // oppure disabilita click, bottoni, ecc.
        playerShip1Btn.setVisible(false);
        playerShip2Btn.setVisible(false);
        playerShip3Btn.setVisible(false);
        yesButton.setVisible(false);
        noButton.setVisible(false);


        DrawButton.setVisible(false);
        DrawButton.setDisable(true);
        setPlayersButton();

        if (demo) {
             ship1.setVisible(true);
             ship2.setVisible(false);
             dashboard1.setVisible(true);
             dashboard2.setVisible(false);
        }else{
            ship1.setVisible(false);
            ship2.setVisible(true);
            dashboard1.setVisible(false);
            dashboard2.setVisible(true);
        }
        setPlayersButton();
    }


    public void updateDashboard(ClientTile[][] dashboard) {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                ClientTile tile = dashboard[row][col];
                if (tile != null && !"EMPTYSPACE".equals(tile.type)) {
                    placeTileWithTokens(tile, row, col);
                } else {
                    cellStackPanes[row][col].getChildren().clear();
                }
            }
        }
    }




    private final StackPane[][] cellStackPanes = new StackPane[5][7]; // 5 righe, 7 colonne

    public void initializeGrid() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(70, 70);

                // Opzionale: stile base
                cell.setStyle("-fx-background-color: transparent;");

                imageShip.add(cell, col, row);
                cellStackPanes[row][col] = cell;
            }
        }
    }


    /**
     * Abilita il click su una cella della dashboard. Il callback riceve la coordinata.
     */
    public void enableDashboardCoordinateSelection(Consumer<int[]> callback) {
        for (Node node : imageShip.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);
            if (row == null || col == null) continue;

            node.setOnMouseClicked(e -> {
                callback.accept(new int[]{row, col});
                disableDashboardCoordinateSelection();
            });

            node.setStyle("-fx-border-color: yellow; -fx-border-width: 2px;");
        }
    }

    /**
     * Rimuove gli handler e lo stile dalla dashboard dopo la selezione.
     */
    public void disableDashboardCoordinateSelection() {
        for (Node node : imageShip.getChildren()) {
            node.setOnMouseClicked(null);
            node.setStyle(null);
        }
    }

    public void showCurrentCard(ClientCard card) {
        currentCard = card;
        cardPane.getChildren().clear();

        if (card == null || card.getImage() == null) return;

        ImageView imageView = new ImageView(card.getImage());
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageView.fitWidthProperty().bind(cardPane.widthProperty());
        imageView.fitHeightProperty().bind(cardPane.heightProperty());

        cardPane.getChildren().add(imageView);
    }


    public void clearCurrentTile() {
        currentCard = null;
        cardPane.getChildren().clear();
    }


    public void placeTileWithTokens(ClientTile tile, int row, int col) {
        StackPane cell = cellStackPanes[row][col];
        cell.getChildren().clear();

        // Immagine base della tile
        ImageView tileImage = new ImageView(tile.getImage());
        tileImage.setFitWidth(70);
        tileImage.setFitHeight(70);
        tileImage.setRotate(tile.getRotation());
        cell.getChildren().add(tileImage);
        // Umani
        for (int i = 0; i < tile.tokens.size(); i++) {

            String tokenType = tile.tokens.get(i);
            ImageView token = new ImageView(getTokenImage(tokenType));
            token.setFitWidth(26);
            token.setFitHeight(32);
            StackPane.setAlignment(token, Pos.CENTER);
            token.setTranslateX(i * 17); // offset orizzontale
            token.setTranslateY(2);
            cell.getChildren().add(token);
        }

        for (int i = 0; i < tile.capacity; i++) {
            ImageView token = new ImageView(getTokenImage("EnergyCell"));
            token.setFitWidth(22);
            token.setFitHeight(22);
            StackPane.setAlignment(token, Pos.CENTER);
            token.setTranslateX(-i * 8);
            token.setTranslateY(1);
            cell.getChildren().add(token);
        }

        // Merci
        List<String> goods = tile.goods;
        if (goods != null) {
            for (int i = 0; i < goods.size(); i++) {
                String goodType = goods.get(i);
                ImageView token = new ImageView(getTokenImage(goodType));
                token.setFitWidth(15);
                token.setFitHeight(15);
                StackPane.setAlignment(token, Pos.BOTTOM_LEFT);
                token.setTranslateX(i * 17);
                token.setTranslateY(-2);
                cell.getChildren().add(token);
            }
        }

    }

    public void showYesNoButtons(String message) {
        messageText.setText(message);
        messageTextFlow.setVisible(true);
        yesButton.setVisible(true);
        noButton.setVisible(true);

        yesButton.setOnAction(e -> {
            guiView.setBufferedBoolean(true);
            hidePrompt();
        });

        noButton.setOnAction(e -> {
            guiView.setBufferedBoolean(false);
            hidePrompt();
        });
    }


    private void hidePrompt() {
        messageTextFlow.setVisible(false);
        yesButton.setVisible(false);
        noButton.setVisible(false);
        messageText.setText("");
    }

    public void updateStatsLabels(String nickname, double firePower, int enginePower, int creditsVal, boolean purple, boolean brown, int humans, int energy) {
        nicknametext.setText(nickname);
        firepower.setText("Fire Power: " + firePower);
        enginepower.setText("Engine Power: " + enginePower);
        credits.setText("Credits: " + creditsVal);
        purplealien.setText("Purple Alien: " + (purple ? "Yes" : "No"));
        brownalien.setText("Brown Alien: " + (brown ? "Yes" : "No"));
        numofhumans.setText("Number of Humans: " + humans);
        energycell.setText("Number of energy Cell: " + energy);
    }

    private Image getTokenImage(String tokenType) {
        String path = switch (tokenType.toLowerCase()) {
            case "human" -> "/Human.png";
            case "purple_alien" -> "/PurpleAlien.png";
            case "brown_alien" -> "/BrownAlien.png";
            case "red" -> "/RedGood.png";
            case "yellow" -> "/YellowGood.png";
            case "green" -> "/GreenGood.png";
            case "blue" -> "/BlueGood.png";
            case "energycell" -> "/EnergyCell.png";
            default -> "/placeholder.png";
        };

        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return new Image("https://via.placeholder.com/16x16.png?text=?");
        }


        return new Image(stream);
    }


}
