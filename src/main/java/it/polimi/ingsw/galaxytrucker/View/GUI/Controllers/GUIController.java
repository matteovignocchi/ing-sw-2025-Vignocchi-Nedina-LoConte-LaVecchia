package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.GUI.SecondaryPhase;
import javafx.fxml.FXML;

import java.awt.*;
import java.awt.event.ActionEvent;
import javafx.scene.control.Button;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GUIController {
    protected static ClientTile currentTile;
    protected static ClientTile[][] dashBoard;
    protected static GUIView guiView;
    protected static String nickname;
    protected static boolean isDemo;
    protected static Boolean[][] maschera;
    protected static Map<String, int[]> mapPosition = new ConcurrentHashMap<String, int[]>();
    private CompletableFuture<String> pendingCommandFuture;
    protected int col = -1;
    protected int row = -1;
    protected int index = -1;
    protected int currentRotation = -1;
    protected int numeberOfTile = 0;


    public Boolean getIsDemo() {
        return isDemo;
    }

    public int[] getCordinate() {
        return new int[]{row, col};
    }

    public int[] askForCoordinate() {
        return new int[]{row, col};
    }


    public void setGuiView(GUIView guiView) {
        this.guiView = guiView;
    }

    public void redrawDashboard() {
    }


    public void setDashBoard(ClientTile[][] dashBoard) {
        this.dashBoard = dashBoard;
    }

    public void setTileInDash(ClientTile tile, int row, int col) {
        dashBoard[row][col] = tile;
    }

    public void setCurrentTile(ClientTile tile) {
        currentTile = tile;
    }

    public void updateMapPosition(Map<String, int[]> map) {
        mapPosition = map;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setValidity(int a, int b) {
        maschera[a][b] = false;
    }

    public void resetValidity(int a, int b) {
        maschera[a][b] = true;
    }

    public void setIsDemo(Boolean demo) {
        Boolean[][] validStatus = new Boolean[5][7];
        isDemo = demo;
        if (isDemo) {
            //first row
            validStatus[0][0] = null;
            validStatus[0][1] = null;
            validStatus[0][2] = null;
            validStatus[0][3] = true;
            validStatus[0][4] = null;
            validStatus[0][5] = null;
            validStatus[0][6] = null;
            //second row
            validStatus[1][0] = null;
            validStatus[1][1] = null;
            validStatus[1][2] = true;
            validStatus[1][3] = true;
            validStatus[1][4] = true;
            validStatus[1][5] = null;
            validStatus[1][6] = null;
            //third row
            validStatus[2][0] = null;
            validStatus[2][1] = true;
            validStatus[2][2] = true;
            validStatus[2][3] = true;
            validStatus[2][4] = true;
            validStatus[2][5] = true;
            validStatus[2][6] = null;
            //fourth row
            validStatus[3][0] = null;
            validStatus[3][1] = true;
            validStatus[3][2] = true;
            validStatus[3][3] = true;
            validStatus[3][4] = true;
            validStatus[3][5] = true;
            validStatus[3][6] = null;
            //fifth row
            validStatus[4][0] = null;
            validStatus[4][1] = true;
            validStatus[4][2] = true;
            validStatus[4][3] = null;
            validStatus[4][4] = true;
            validStatus[4][5] = true;
            validStatus[4][6] = null;
        } else {
            //first row
            validStatus[0][0] = null;
            validStatus[0][1] = null;
            validStatus[0][2] = true;
            validStatus[0][3] = null;
            validStatus[0][4] = true;
            validStatus[0][5] = true;
            validStatus[0][6] = true;
            //second row
            validStatus[1][0] = null;
            validStatus[1][1] = true;
            validStatus[1][2] = true;
            validStatus[1][3] = true;
            validStatus[1][4] = true;
            validStatus[1][5] = true;
            validStatus[1][6] = null;
            //third row
            validStatus[2][0] = true;
            validStatus[2][1] = true;
            validStatus[2][2] = true;
            validStatus[2][3] = true;
            validStatus[2][4] = true;
            validStatus[2][5] = true;
            validStatus[2][6] = true;
            //fourth row
            validStatus[3][0] = true;
            validStatus[3][1] = true;
            validStatus[3][2] = true;
            validStatus[3][3] = true;
            validStatus[3][4] = true;
            validStatus[3][5] = true;
            validStatus[3][6] = true;
            //fifth row
            validStatus[4][0] = true;
            validStatus[4][1] = true;
            validStatus[4][2] = true;
            validStatus[4][3] = null;
            validStatus[4][4] = true;
            validStatus[4][5] = true;
            validStatus[4][6] = true;
        }
        this.maschera = validStatus;
    }


    @FXML
    protected void onButtonClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String command = mapButtonIdToCommand(buttonId);
        if (command != null) {
            guiView.onUserCommand(command);
        }
    }


    private String mapButtonIdToCommand(String buttonId) {
        return switch (buttonId) {
            case "getCoveredBtn" -> "getacoveredtile";
            case "getShownBtn" -> "getashowntile";
            case "returnTileBtn" -> "returnthetile";
            default -> null;
        };
    }

    public void placeTileOnGrid(int row, int col) {}
}
