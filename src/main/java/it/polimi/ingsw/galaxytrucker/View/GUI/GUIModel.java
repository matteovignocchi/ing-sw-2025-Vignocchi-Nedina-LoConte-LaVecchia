package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.Client.ClientCard;
import it.polimi.ingsw.galaxytrucker.Client.ClientTile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for the GUI representing the client’s local game state.
 * Maintains the player’s dashboard (5x7 tile matrix), a mask of valid positions,
 * current tile and card being manipulated, and player-specific info such as nickname,
 * firepower, engine power, credits, crew counts, alien presence, and positions of all players.
 * Provides getters and setters to update and retrieve the state, and supports resetting the model.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
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

    /**
     * Returns the current dashboard tile matrix.
     * @return 2D array of ClientTile representing the player’s ship layout.
     */
    public ClientTile[][] getDashboard() { return dashboard; }

    /**
     * Sets the player’s dashboard matrix.
     * @param dashboard the new 2D array of ClientTile.
     */
    public void setDashboard(ClientTile[][] dashboard) { this.dashboard = dashboard; }

    /**
     * Sets the current tile being manipulated by the player.
     * @param tile the ClientTile to set as current.
     */
    public void setCurrentTile(ClientTile tile) { this.currentTile = tile; }

    /**
     * Sets the current card that the player is interacting with in the GUI model.
     * @param card the ClientCard to set as current
     */
    public void setCurrentCard(ClientCard card) { this.card = card; }

    /**
     * Returns whether the model is in demo mode.
     * @return true if demo mode, false otherwise.
     */
    public boolean isDemo() { return isDemo; }

    /**
     * Enables or disables demo mode, and sets the mask of valid tile positions accordingly.
     * @param demo true to enable demo mode, false otherwise.
     */
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

    /**
     * Returns whether the position (a, b) on the dashboard is currently valid.
     * @param a row index
     * @param b column index
     * @return true if valid, false or null otherwise
     */
    public Boolean returnValidity(int a, int b) {
        return mask[a][b];
    }

    /**
     * Returns the current mask of valid positions on the dashboard.
     * The mask is a 2D Boolean array indicating valid (true) or invalid (false/null) tile positions.
     * @return the mask matrix
     */
    public Boolean[][] getMask() {
        return mask;
    }

    /**
     * Returns the nickname of the current player.
     * @return the player's nickname
     */
    public String getNickname() { return nickname; }

    /**
     * Sets the nickname of the current player.
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Sets the current total firepower of the player's ship.
     * @param firePower the firepower value to set
     */
    public void setFirePower(double firePower) { this.firePower = firePower; }

    /**
     * Sets the current total engine power of the player's ship.
     * @param enginePower the engine power value to set
     */
    public void setEnginePower(int enginePower) { this.enginePower = enginePower; }

    /**
     * Sets the current credits owned by the player.
     * @param credits the number of credits to set
     */
    public void setCredits(int credits) { this.credits = credits; }

    /**
     * Sets the number of human crew members currently on the player's ship.
     * @param numberOfHumans the number of humans to set
     */
    public void setNumberOfHumans(int numberOfHumans) { this.numberOfHumans = numberOfHumans; }

    /**
     * Sets the total energy units available on the player's ship.
     * @param numberOfEnergy the amount of energy to set
     */
    public void setNumberOfEnergy(int numberOfEnergy) { this.numberOfEnergy = numberOfEnergy; }

    /**
     * Sets the presence status of the purple alien on the player's ship.
     * @param purple true if the purple alien is present, false otherwise
     */
    public void setPurpleAlien(boolean purple) { this.purpleAlien = purple; }

    /**
     * Sets the presence status of the brown alien on the player's ship.
     * @param brown true if the brown alien is present, false otherwise
     */
    public void setBrownAlien(boolean brown) { this.brownAlien = brown; }

    /**
     * Updates the map of player positions with the given data.
     * Clears the existing positions and replaces them with the new map.
     * @param map a mapping from player nicknames to their position arrays
     */
    public void setPlayerPositions(Map<String, int[]> map) {
        this.playerPositions.clear();
        this.playerPositions.putAll(map);
    }

    /**
     * Marks a dashboard position as invalid (used).
     * @param a row index
     * @param b column index
     */
    public void setValidity(int a , int b){
        mask[a][b] = false;
    }

    /**
     * Resets a dashboard position as valid (free).
     * @param a row index
     * @param b column index
     */
    public void resetValidity(int a , int b){
        mask[a][b] = true;
    }

    /**
     * Gets a copy of the map containing all player positions.
     * @return a map of player nicknames to their position arrays.
     */
    public Map<String, int[]> getPlayerPositions() {
        return new HashMap<>(playerPositions);
    }

    /**
     * Gets the position index of a given player.
     * @param playerName the nickname of the player
     * @return the position index
     */
    public int getMyPosition(String playerName){
        return playerPositions.get(playerName)[0];
    }

    /**
     * Resets all fields of the model to their initial state.
     */
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
