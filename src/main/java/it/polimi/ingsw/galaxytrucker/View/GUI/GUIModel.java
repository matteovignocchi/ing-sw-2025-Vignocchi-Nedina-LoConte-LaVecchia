package it.polimi.ingsw.galaxytrucker.View.GUI;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;

public class GUIModel {
    private ClientTile[][] dashboard = new ClientTile[5][7];
    private ClientTile currentTile;
    private boolean isDemo;
    private String nickname;

    // Getters/Setters
    public ClientTile[][] getDashboard() { return dashboard; }
    public void setDashboard(ClientTile[][] dashboard) { this.dashboard = dashboard; }

    public ClientTile getCurrentTile() { return currentTile; }
    public void setCurrentTile(ClientTile tile) { this.currentTile = tile; }

    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { this.isDemo = demo; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
