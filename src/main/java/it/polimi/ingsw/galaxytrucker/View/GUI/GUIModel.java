package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;

import java.util.HashMap;
import java.util.Map;

public class GUIModel {
    private ClientTile[][] dashboard = new ClientTile[5][7];
    private Boolean[][] mask = new Boolean[5][7];
    private ClientTile currentTile;
    private boolean isDemo;
    private String nickname;
    private Map<String, int[]> playerPositions = new HashMap<>();
    private ClientCard card;
    private double firePower;
    private int enginePower;
    public int credits;
    private int numberOfHumans;
    private int numberOfEnergy;
    private boolean purpleAlien;
    private boolean brownAlien;



    // Getters/Setters
    public ClientTile[][] getDashboard() { return dashboard; }

    public void setDashboard(ClientTile[][] dashboard) { this.dashboard = dashboard; }

    public ClientTile getCurrentTile() { return currentTile; }

    public void setCurrentTile(ClientTile tile) { this.currentTile = tile; }

    public void setCurrentCard(ClientCard card) { this.card = card; }

    public ClientCard getCurrentCard() { return card; }

    public boolean isDemo() { return isDemo; }

    public void setDemo(Boolean demo) {
        Boolean[][] validStatus = new Boolean[5][7];
        this.isDemo = demo;
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
        mask = validStatus;

    }

    public Boolean returnValidity(int a, int b) {
        return mask[a][b];
    }

    public Boolean[][] getMask() {
        return mask;
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFirePower(double firePower) { this.firePower = firePower; }

    public void setEnginePower(int enginePower) { this.enginePower = enginePower; }

    public void setCredits(int credits) { this.credits = credits; }

    public void setNumberOfHumans(int numberOfHumans) { this.numberOfHumans = numberOfHumans; }

    public void setNumberOfEnergy(int numberOfEnergy) { this.numberOfEnergy = numberOfEnergy; }

    public void setPurpleAlien(boolean purple) { this.purpleAlien = purple; }

    public void setBrownAlien(boolean brown) { this.brownAlien = brown; }


    public void setPlayerPositions(Map<String, int[]> map) {
        this.playerPositions.clear();
        this.playerPositions.putAll(map);
    }

    public void setValidity(int a , int b){
        mask[a][b] = false;
    }

    public void resetValidity(int a , int b){
        mask[a][b] = true;
    }

    public Map<String, int[]> getPlayerPositions() {
        return new HashMap<>(playerPositions);
    }

    public int getMyPosition(String playerName){
        return playerPositions.get(playerName)[0];
    }
    public void reset() {
        this.dashboard = new ClientTile[5][7];
        this.mask = new Boolean[5][7];
        this.currentTile = null;
        this.card = null;
        this.firePower = 0;
        this.enginePower = 0;
        this.credits = 0;
        this.numberOfHumans = 0;
        this.numberOfEnergy = 0;
        this.purpleAlien = false;
        this.brownAlien = false;
        this.nickname = null;
        this.playerPositions.clear();
    }

}
