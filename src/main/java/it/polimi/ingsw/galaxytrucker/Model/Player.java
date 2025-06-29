package it.polimi.ingsw.galaxytrucker.Model;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a player in the game and manages all aspects of their ship dashboard.
 *
 * This class handles:
 * - The structure and layout of the ship (tiles and their statuses)
 * - Game-related values such as credits, lap count, and position
 * - Whether the dashboard is in demo mode or not
 * - Alien presence (purple and brown aliens)
 * - Discard pile and tile validation
 * - Core gameplay mechanics: placing and removing tiles, handling meteor hits,
 *   validating ship connections, and tracking resources (humans, energy, goods)
 *
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */
public class Player implements Serializable {
    private final int id;
    private boolean connected;
    private int credits;
    private int idPhoto;
    private Tile[][] Dash_Matrix;
    private final Status[][] validStatus;
    private boolean purpleAlien;
    private boolean brownAlien;
    private boolean isdemo;
    private List<Tile> discardPile;
    protected int lap;
    protected Integer position;
    private boolean isEliminated;
    private GamePhase gamePhase;
    private Tile lastTile;
    private boolean semaphore;

    /**
     * Creates a new player and initializes their ship dashboard.
     *
     * The constructor sets the player's ID, photo ID, and initial game values
     * such as credits, position, lap count, and alien presence flags. It also
     * initializes the ship matrix with empty tiles and places a central housing unit.
     *
     * Based on the isDemo flag, it sets up the allowed tile positions (validStatus matrix)
     * with a specific pattern for demo or standard mode.
     *
     * @param id the unique identifier of the player
     * @param isDemo true if the player is using a demo dashboard layout
     * @param idPhoto the identifier for the player's ship image
     *
     */
    public Player(int id, boolean isDemo ,int idPhoto) {
        this.id = id;
        this.lap = 0;
        this.position = 0;
        this.isEliminated = false;
        this.connected = true;
        this.discardPile = new ArrayList<>();
        this.isdemo = isDemo;
        credits = 0;
        purpleAlien = false;
        brownAlien = false;
        this.idPhoto = idPhoto;
        //initialize the matrix
        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
        //place the central unit
        Dash_Matrix[2][3] = new HousingUnit(3,3,3,3, Human.HUMAN ,idPhoto);

        //initialized a matrix with the valid position of the ship
        validStatus = new Status[5][7];
        validStatus[2][3] = Status.USED;
        if (isDemo) {

            //first row
            validStatus[0][0]  = Status.BLOCK;
            validStatus[0][1]  = Status.BLOCK;
            validStatus[0][2]  = Status.BLOCK;
            validStatus[0][3]  = Status.FREE;
            validStatus[0][4]  = Status.BLOCK;
            validStatus[0][5]  = Status.BLOCK;
            validStatus[0][6]  = Status.BLOCK;
            //second row
            validStatus[1][0]  = Status.BLOCK;
            validStatus[1][1]  = Status.BLOCK;
            validStatus[1][2]  = Status.FREE;
            validStatus[1][3]  = Status.FREE;
            validStatus[1][4]  = Status.FREE;
            validStatus[1][5]  = Status.BLOCK;
            validStatus[1][6]  = Status.BLOCK;
            //third row
            validStatus[2][0]  = Status.BLOCK;
            validStatus[2][1]  = Status.FREE;
            validStatus[2][2]  = Status.FREE;
            validStatus[2][4]  = Status.FREE;
            validStatus[2][5]  = Status.FREE;
            validStatus[2][6]  = Status.BLOCK;
            //fourth row
            validStatus[3][0]  = Status.BLOCK;
            validStatus[3][1]  = Status.FREE;
            validStatus[3][2]  = Status.FREE;
            validStatus[3][3]  = Status.FREE;
            validStatus[3][4]  = Status.FREE;
            validStatus[3][5]  = Status.FREE;
            validStatus[3][6]  = Status.BLOCK;
            //fifth row
            validStatus[4][0]  = Status.BLOCK;
            validStatus[4][1]  = Status.FREE;
            validStatus[4][2]  = Status.FREE;
            validStatus[4][3]  = Status.BLOCK;
            validStatus[4][4]  = Status.FREE;
            validStatus[4][5]  = Status.FREE;
            validStatus[4][6]  = Status.BLOCK;
        } else {
            //first row
            validStatus[0][0]  = Status.BLOCK;
            validStatus[0][1]  = Status.BLOCK;
            validStatus[0][2]  = Status.FREE;
            validStatus[0][3]  = Status.BLOCK;
            validStatus[0][4]  = Status.FREE;
            validStatus[0][5]  = Status.FREE;
            validStatus[0][6]  = Status.FREE;
            //second row
            validStatus[1][0]  = Status.BLOCK;
            validStatus[1][1]  = Status.FREE;
            validStatus[1][2]  = Status.FREE;
            validStatus[1][3]  = Status.FREE;
            validStatus[1][4]  = Status.FREE;
            validStatus[1][5]  = Status.FREE;
            validStatus[1][6]  = Status.BLOCK;
            //third row
            validStatus[2][0]  = Status.FREE;
            validStatus[2][1]  = Status.FREE;
            validStatus[2][2]  = Status.FREE;
            validStatus[2][4]  = Status.FREE;
            validStatus[2][5]  = Status.FREE;
            validStatus[2][6]  = Status.FREE;
            //fourth row
            validStatus[3][0]  = Status.FREE;
            validStatus[3][1]  = Status.FREE;
            validStatus[3][2]  = Status.FREE;
            validStatus[3][3]  = Status.FREE;
            validStatus[3][4]  = Status.FREE;
            validStatus[3][5]  = Status.FREE;
            validStatus[3][6]  = Status.FREE;
            //fifth row
            validStatus[4][0]  = Status.FREE;
            validStatus[4][1]  = Status.FREE;
            validStatus[4][2]  = Status.FREE;
            validStatus[4][3]  = Status.BLOCK;
            validStatus[4][4]  = Status.FREE;
            validStatus[4][5]  = Status.FREE;
            validStatus[4][6]  = Status.FREE;
        }

    }


    /**
     * * Returns the current ship dashboard matrix.*
     * The matrix contains all the tiles currently placed on the player's ship.*
     * @return a 2D array of Tile representing the player's ship layout
     * */
    public Tile[][] getDashMatrix() {
        return Dash_Matrix;
    }

    /**
     * Returns the unique ID of the player.
     * @return the player's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ID of the player's ship image.
     * @return the photo ID associated with the player
     */
    public int getIdPhoto(){
        return idPhoto;
    }

    /**
     * Checks whether the player is currently connected.
     * @return true if the player is connected, false otherwise
     */
    public boolean isConnected() { return connected; }

    /**
     * Returns the last tile selected or received by the client.
     * This tile typically represents the most recently drawn or interacted tile
     * during the building phase.*
     * @return the last tile handled by the player
     */
    public Tile getLastTile(){return lastTile;}

    /**
     * Sets the last tile selected or received by the client.
     * This method is usually called when the player draws a new tile from the deck.
     * @param t the tile to set as the last one handled
     */
    public void setLastTile (Tile t) {this.lastTile = t;}

    /**
     * Sets the connection status of the player.
     * @param connected true if the player is connected, false if disconnected
     */
    public void setConnected(boolean connected) { this.connected = connected; }

    /**
     * @return how many laps the player did
     */
    public int getLap() {
        return lap;
    }

    /**
     * @return the position where the player is
     */
    public Integer getPos() {
        return position;
    }

    /**
     * @return how many credits has the player
     */
    public int getCredits(){
        return credits;
    }

    /**
     * this method changes the play order
     * @param pos turn order placement
     */
    public void setPos(Integer pos) {
        position = pos;
    }

    /**
     * this method changes if the player has done a new lap
     * @param newLap number of lap
     */
    public void setLap(int newLap) {
        lap = newLap;
    }

    /**
     * Change the flag that indicates the presence of a purple alien
     */
    public void setPurpleAlien() {
        purpleAlien = !purpleAlien;
    }

    /**
     * @return true if the alien is present
     */
    public boolean presencePurpleAlien() { return this.purpleAlien; }

    /**
     * Change the flag that indicates the presence of a brown alien
     */
    public void setBrownAlien() {
        brownAlien = !brownAlien;
    }

    /**
     * @return true if the alien is present
     */
    public boolean presenceBrownAlien() { return this.brownAlien; }


    /**
     * Sets the current phase of the game for this player.
     * This is used to track which stage of the game the player is in
     * (e.g., setup, building, flight, etc.).
     * @param gamePhase the current game phase to set
     */
    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }


    /**
     * Returns the current phase of the game for this player.
     * @return the current game phase
     */
    public GamePhase getGamePhase() {
        return gamePhase;
    }

    /**
     * Returns the number of tiles in the discard pile that are not EmptySpace.
     * This represents how many actual components (destroyed or discarded) the player
     * has lost, excluding placeholder tiles.*
     * @return the number of non-empty tiles in the discard pile
     */
    public int checkDiscardPile(){
        int tmp = 0;
        for(Tile boh : discardPile){
            switch (boh){
                case EmptySpace emptySpace->{}
                default -> tmp++;
            }
        }
        return tmp;
    }

    /**
     * Adds a tile to the player's discard pile.
     * If the game is in the BOARD_SETUP phase, a player can only have up to 2 reserved tiles.
     * If this limit is exceeded, an exception is thrown.
     * @param tile the tile to add to the discard pile
     * @throws BusinessLogicException if too many tiles are reserved during setup
     */
    public void addToDiscardPile(Tile tile) throws BusinessLogicException{
        if(gamePhase != GamePhase.BOARD_SETUP) discardPile.add(tile);
        else {
            if(discardPile.size()>=2) throw new BusinessLogicException("too many Reserved Tiles");
            discardPile.add(tile);
        }
    }


    /**
     * Returns the total number of standard human tokens on the player's ship.
     * This method scans all tiles on the dashboard and counts the HUMAN tokens
     * in housing units. If the player is eliminated, the result is 0.
     * @return the number of HUMAN tokens on the ship
     */
    public int getTotalHuman(){
        if(isEliminated) return 0;

        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = getTile(i, j);
                switch (y) {
                    case HousingUnit h  -> {
                        for(Human x : h.getListOfToken()){
                            if(x==Human.HUMAN) tmp++;
                        }
                    }
                    default -> tmp = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * this method checks every tile in the dashboard
     * if the tile is an energy cell, it gets the capacity left
     * @return the total amount of energy cell left of the player
     */
    public int getTotalEnergy(){
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = getTile(i, j);
                switch (y) {
                    case EnergyCell c  -> tmp = tmp + c.getCapacity();
                    default -> tmp = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * this method checks every tile in the dashboard
     * if the tile is a storage unit, it gets how many goods are in the unit
     * @return the total amount of goods held by the player
     */
    public int getTotalGood() {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = getTile(i, j);
                switch (y) {
                    case StorageUnit c  -> tmp = tmp + c.getListSize();
                    default -> tmp = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * this method changes the status of the player, when it is eliminated
     */
    public void setEliminated() {
        isEliminated = true;
    }

    /**
     * @return if the player is eliminated or in the game
     */
    public boolean isEliminated() {
        return isEliminated;
    }

    /**
     * the method adds the credits passed as input to the function.
     * @param credits amount of credit
     */
    public void addCredits(int credits) {
        this.credits = this.credits + credits;
    }

    /**
     * Removes the tile at the specified position on the ship and replaces it with an EmptySpace.
     * If the semaphore flag is active, the removed tile is added to the discard pile.
     * If the tile is a HousingUnit containing a single alien, the alien is re-enabled on the player.
     * It also checks nearby tiles to potentially restore alien presence if needed.
     * @param a the row index of the tile
     * @param b the column index of the tile
     * @throws BusinessLogicException if adding the tile to the discard pile violates game rules
     */
    public void removeTile(int a, int b) throws BusinessLogicException {
        if(semaphore) addToDiscardPile(Dash_Matrix[a][b]);
        Tile tmp = Dash_Matrix[a][b];
        Dash_Matrix[a][b] = new EmptySpace();
        validStatus[a][b] = Status.FREE;
        switch(tmp){
            case HousingUnit housingUnit->{
                if(housingUnit.getListOfToken().size()==1){
                    switch(housingUnit.getTypeOfConnections()){
                        case BROWN_ALIEN -> setBrownAlien();
                        case PURPLE_ALIEN ->  setPurpleAlien();
                        default -> {}
                    }
                }
                if(!housingUnit.getType().equals(Human.HUMAN)){
                    checkNearAlien(a, b);
                }
            }
            default ->{}
        }
    }


    /**
     * Checks adjacent tiles after removing an alien housing unit to update alien presence.
     * This method is used when an alien cabin is destroyed. It scans the four neighboring
     * tiles to see if any housing units contain a single alien connected to the removed one.
     * If found, the alien is removed from that housing unit and the corresponding alien flag
     * is re-enabled for the player.*
     * @param a the row index of the removed alien tile
     * @param b the column index of the removed alien tile
     * @throws BusinessLogicException if an error occurs while modifying the housing unit
     */
    public void checkNearAlien(int a, int b) throws BusinessLogicException {
        int[] tmp = {1, 0, -1, 0};
        int[] tmp2 = {0, 1, 0, -1};
        for (int i = 0; i < 4; i++) {
            if(isOutOfBounds(a + tmp[i], b + tmp2[i])) continue;
            Tile tmp3 = Dash_Matrix[a+tmp[i]][b+tmp2[i]];
            switch (tmp3){
                case HousingUnit housingUnit->{
                    if(housingUnit.getListOfToken().size()==1){
                        switch(housingUnit.getTypeOfConnections()){
                            case BROWN_ALIEN -> {
                                setBrownAlien();
                                housingUnit.removeHumans(0);
                            }
                            case PURPLE_ALIEN ->  {
                                setPurpleAlien();
                                housingUnit.removeHumans(0);
                            }
                            default -> {}
                        }
                    }
                }
                default ->{}
            }
        }
    }

    /**
     * @return a random number from 1 to 6
     */
    public int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    /**
     * Utility method used for local testing to replace the entire ship dashboard.
     * This method directly sets the dashboard matrix and updates the validity status
     * of each tile based on whether it is an EmptySpace or not.
     * @param tile a custom 5x7 matrix of tiles to assign to the player's dashboard
     */
    public void modifyDASH(Tile[][] tile) {
        this.Dash_Matrix = tile;
        for(int i=0;i<5;i++){
            for(int j=0;j<7;j++){
                switch ((tile[i][j])){
                    case EmptySpace e-> validStatus[i][j]=Status.FREE;
                    default -> validStatus[i][j]=Status.USED;
                }
            }
        }
    }

    /**
     * the method return the tile in the position (x,y)
     * @param x row index
     * @param y column index
     * @return the tile in the position (x,y)
     */
    public Tile getTile(int x, int y) {
        return Dash_Matrix[x][y];
    }

    /**
     * the method returns the validity flag
     * @param x row index
     * @param y column index
     * @return the status flag of the tile
     */
    public Status validityCheck(int x, int y) {
        return validStatus[x][y];
    }

    /**
     * this method adds the tile on the player's shipboard
     * @param x row index
     * @param y column index
     * @param t Tile to be added
     * @throws IllegalArgumentException if the player tries to place the tile in an illegal position
     */
    public void addTile(int x, int y, Tile t) throws BusinessLogicException {
        if (validStatus[x][y] == Status.FREE ) {
            Dash_Matrix[x][y] = t;
            validStatus[x][y] = Status.USED;
            if(x == 0 && (y == 5 || y ==6)){
                addToDiscardPile(t);
            }
        } else {
            throw new BusinessLogicException("Position not valid");
        }
    }

    /**
     * Returns the list of tiles currently in the player's discard pile.
     * This includes all destroyed or discarded components collected during the game.
     * @return the list of discarded tiles
     */
    public List<Tile> getTilesInDiscardPile() {
        return discardPile;
    }


    /**
     * check if al the engine are display in the correct way
     * every engine's value 6/7 it is not face to the index 2 of the orientation array, will be removed
     * if there is a tile under the index 2 of the orientation array, it will be removed
     */
    public void controlEngine() throws BusinessLogicException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = Dash_Matrix[i][j];
                switch (y){
                    case Engine e ->{
                        if(e.controlCorners(2) != 7 && e.controlCorners(2) != 6){
                            removeTile(i, j);
                        }else {
                            if(i<4)removeTile(i+1, j);
                        }
                    }
                    default -> {}
                }
            }
        }
    }

    /**
     * Validates the orientation of all cannons on the player's ship.
     * If a cannon is facing inward toward another tile (using connector type 4 or 5),
     * the tile in that direction is removed. This simulates the penalty of having
     * improperly oriented weapons during ship assembly.
     * @throws BusinessLogicException if tile removal encounters a rule violation
     */
    public void controlCannon() throws BusinessLogicException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile c = Dash_Matrix[i][j];
                switch (c){
                    case Cannon e ->{
                        for (int x = 0; x < 4; x++) {
                            if (Dash_Matrix[i][j].controlCorners(x) == 4 || Dash_Matrix[i][j].controlCorners(x) == 5) {
                                if (x == 0) {
                                    if(i>0) removeTile(i-1, j);
                                }
                                if (x == 1) {
                                    if(j<6) removeTile(i, j+1);
                                }
                                if (x == 2) {
                                    if(i<4) removeTile(i+1, j);
                                }
                                if (x == 3) {
                                    if(j>0) removeTile(i, j-1);
                                }
                            }
                        }
                    }
                    default -> {}
                }
            }
        }
    }

    /**
     * Updates the connection status between housing units on the ship.
     * Called after the ship assembly validation, this method checks all housing units
     * and marks them as connected if they are adjacent to another housing unit.
     * If a housing unit is adjacent to an alien cabin (non-HUMAN), it also inherits
     * the alien type for connection purposes.
     */
    public void controlOfConnection() {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        int[] opposite = {2, 3, 0, 1};

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 7; y++) {
                Tile housing = Dash_Matrix[x][y];

                switch (housing) {
                    case HousingUnit e -> {
                        if(e.getType() != Human.HUMAN) continue;
                        boolean connectedToHuman = false;
                        for (int i = 0; i < 4; i++) {
                            int nx = x + dx[i];
                            int ny = y + dy[i];
                            if (isOutOfBounds(nx, ny)) continue;
                            Tile neighbor = Dash_Matrix[nx][ny];
                            switch (neighbor) {
                                case HousingUnit e1 -> {
                                    int a = e.controlCorners(i);
                                    int b = e1.controlCorners(opposite[i]);

                                    if (connected(a, b)) {
                                        if (e1.getType() == Human.HUMAN) {
                                            e.setConnected(true);
                                            e1.setConnected(true);
                                            connectedToHuman = true;
                                        } else {
                                            e.setTypeOfConnections(e1.getType());
                                        }
                                    }
                                }
                                default -> {}
                            }
                        }
                        if (!connectedToHuman) {
                            e.setConnected(false);
                        }
                    }
                    default -> {}
                }
            }
        }
    }
    /**
     * Checks whether two sides of adjacent tiles are connected.
     * Two sides are considered connected if:
     * - They have the same connector type
     * - One of them has a universal connector (value 3)
     * A connector value of 0 is treated as absent and not connectable.
     * @param a the connector type of the first tile side
     * @param b the connector type of the second tile side
     * @return true if the two connectors are compatible, false otherwise
     */
    private boolean connected(int a, int b) {
        if (a == 0 || b == 0) return false;
        if (a == b) return true;
        if (a == 3 || b == 3) return true;
        return false;
    }

    /**
     * Checks whether the given coordinates are outside the ship dashboard boundaries.
     * The dashboard is a fixed 5x7 grid. This method returns true if the given (x, y)
     * position falls outside that range.
     * @param x the row index to check
     * @param y the column index to check
     * @return true if the coordinates are out of bounds, false otherwise
     */
    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || y < 0 || x >= 5 || y >= 7;
    }

    /**
     * Checks whether the tile at the given position is isolated from all others.
     * A tile is considered isolated if none of its sides are connected to
     * any adjacent tiles. The method verifies each of the four directions and
     * returns true only if no valid connection is found.
     * @param x the row index of the tile
     * @param y the column index of the tile
     * @return true if the tile is not connected to any neighbor, false otherwise
     */
    public boolean isIsolated(int x, int y) {
        Tile tmp = Dash_Matrix[x][y];
        if(tmp == null) return true;
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        int[] opt = {2,3,0,1};
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if(isOutOfBounds(nx,ny)) continue;
            Tile nearTmp = Dash_Matrix[nx][ny];
            int sideCurr = tmp.controlCorners(i);
            int sideNear = nearTmp.controlCorners(opt[i]);
            if ((sideCurr*sideNear) != 0) return false;
        }
        return true;
    }


    /**
     * Performs a full validation of the ship's structure after the building phase.
     * This method checks engine and cannon orientation, removes any invalid reserved tiles
     * (only in non-demo mode), and verifies tile connectivity using a BFS traversal.
     * It also ensures that housing units remain connected, and applies final corrections
     * to remove isolated or misconnected components.
     * @param x the row index of the central housing unit
     * @param y the column index of the central housing unit
     * @throws BusinessLogicException if invalid tile removals or assignments occur
     */
    public void controlAssembly(int x , int y) throws BusinessLogicException {
        controlEngine();
        controlCannon();
        if(!isdemo){
            removeTile(0,5);
            removeTile(0,6);
        }
        HousingUnit tmp = (HousingUnit) getTile(x,y);

        boolean flag = true;
        while(flag){
            flag = false;
            boolean tmpBo = controlAssembly2(x,y);
            if(tmpBo) flag = true;
        }
        controlOfConnection();
        semaphore = false;
        removeTile(x, y);
        addTile(x,y,tmp);
        semaphore = true;
    }

    /**
     * Traverses the ship using BFS to identify disconnected or improperly connected tiles.
     * Starting from the central housing unit, it marks reachable tiles. All other tiles,
     * including those with invalid connections or isolated components, are removed.
     * @param xx the starting row index
     * @param yy the starting column index
     * @return true if any tile was removed, false otherwise
     * @throws BusinessLogicException if an error occurs while removing tiles
     */
    public boolean controlAssembly2(int xx, int yy) throws BusinessLogicException {
        int count = 0;
        boolean[][] visited = new boolean[5][7];
        boolean[][] wrongConnection = new boolean[5][7];
        Queue<int[]> queue = new LinkedList<>();
        int [] dx = {-1, 0, 1, 0};
        int [] dy = {0, 1, 0, -1};
        int [] opp = {2,3,0,1};
        queue.add(new int[] {xx, yy});
        visited[xx][yy] = true;
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0];
            int y = curr[1];

            for(int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                if(isOutOfBounds(nx, ny) || visited[nx][ny]) continue;
                if(validStatus[nx][ny] != Status.USED) continue;
                if (wrongConnection[x][y] || wrongConnection[nx][ny]) continue;
                int currentSide = Dash_Matrix[x][y].controlCorners(i);
                int nearSide = Dash_Matrix[nx][ny].controlCorners(opp[i]);
                if ((currentSide > 0 && nearSide == 0) || (currentSide == 0 && nearSide > 0)) {
                    wrongConnection[nx][ny] = true;
                    continue;
                }
                if (connected(currentSide, nearSide)) {
                    queue.add(new int[] {nx, ny});
                    visited[nx][ny] = true;
                } else {
                    wrongConnection[nx][ny] = true;
                }
            }

        }
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 7; j++) {
                if(!visited[i][j] || wrongConnection[i][j]) {
                    if(!(validStatus[i][j] == Status.FREE)){
                        count++;
                    }
                    this.removeTile(i, j);
                }
            }
        }
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 7; j++) {
                if(isIsolated(i,j) && validStatus[i][j] == Status.USED){
                    this.removeTile(i,j);
                    count ++;
                }
            }
        }
        return count != 0;
    }


    /**
     * Counts the number of exposed connectors on the player's ship.
     * For each tile on the ship, this method checks its four sides. A connector is
     * considered exposed if:
     * - It does not connect to another valid tile
     * - The adjacent tile has an incompatible or missing connector
     * Only connectors with values 1, 2, or 3 (not 0 or special values >= 4) are considered.*
     * @return the total number of exposed connectors on the ship
     */
    public int countExposedConnectors() {
        int count = 0;
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        int[] opp = {2, 3, 0, 1};
        validStatus[2][3] = Status.USED;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 7; y++) {
                Status tmp = validStatus[x][y];
                if (tmp != Status.USED) continue;
                for (int d = 0; d < 4; d++) {
                    int a = Dash_Matrix[x][y].controlCorners(d);

                    if (a == 0 || a >= 4) continue;

                    int nx = x + dx[d];
                    int ny = y + dy[d];

                    if (isOutOfBounds(nx, ny) || validStatus[nx][ny] != Status.USED) {
                        count++;
                    } else {
                        int b = Dash_Matrix[nx][ny].controlCorners(opp[d]);
                        if (!connected(a, b)) count++;
                    }
                }
            }
        }
        validStatus[2][3] = Status.BLOCK;

        return count;
    }

    /**
     * Handles a ship attack from the north direction.
     * Scans the specified column from top to bottom and removes the first
     * tile hit (i.e., with USED or BLOCK status).
     * @param dir2 the column index relative to the meteor's direction
     * @return true if a tile was hit and removed, false otherwise
     * @throws BusinessLogicException if removing the tile causes a rule violation
     */
    public Boolean removeFrom0(int dir2) throws BusinessLogicException {
        int i = 0;
        int tmp = dir2 -4;
        if(tmp < 0) return false;
        while ( i < 5) {
            if (validStatus[i][tmp] == Status.USED || validStatus[i][tmp]==Status.BLOCK ) {
                this.removeTile(i, tmp);
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     * Handles a ship attack from the east direction.
     * Scans the specified row from right to left and removes the first tile hit.
     * @param dir2 the row index relative to the meteor's direction
     * @return true if a tile was hit and removed, false otherwise
     * @throws BusinessLogicException if removing the tile causes a rule violation
     */
    public Boolean removeFrom1(int dir2) throws BusinessLogicException {
        int i = 6;
        int tmp = dir2 -5;
        if(tmp < 0) return false;
        while (i >= 0) {
            if (validStatus[tmp][i] == Status.USED) {
                this.removeTile(tmp, i);
                return true;

            }
            i--;
        }
        return false;
    }

    /**
     * Handles a ship attack from the south direction.
     * Scans the specified column from bottom to top and removes the first tile hit.
     * @param dir2 the column index relative to the meteor's direction
     * @return true if a tile was hit and removed, false otherwise
     * @throws BusinessLogicException if removing the tile causes a rule violation
     */
    public Boolean removeFrom2(int dir2) throws BusinessLogicException {
        int i = 4;
        int tmp = dir2 -4;
        if(tmp < 0) return false;
        while (i >= 0) {
            if (validStatus[i][tmp] == Status.USED) {
                this.removeTile(i, tmp);
                return true;
            }
            i--;
        }
        return false;

    }

    /**
     * Handles a ship attack from the west direction.
     * Scans the specified row from left to right and removes the first tile hit.
     * @param dir2 the row index relative to the meteor's direction
     * @return true if a tile was hit and removed, false otherwise
     * @throws BusinessLogicException if removing the tile causes a rule violation
     */
    public Boolean removeFrom3(int dir2) throws BusinessLogicException {
        int i = 0;
        int tmp = dir2 -5;
        if(tmp < 0) return false;
        while (i < 7) {
            if (validStatus[tmp][i] == Status.USED) {
                this.removeTile(tmp, i);
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     * Checks whether the first tile hit by a small meteorite has no exposed connector
     * on the impacted side.
     * This method scans in the direction of the meteor impact (north, east, south, west)
     * to find the first non-empty tile. If the side of the tile facing the impact
     * has no connector (value 0), the method returns true.
     * @param x the cardinal direction of the impact (0 = north, 1 = east, 2 = south, 3 = west)
     * @param y the row or column index where the meteorite strikes
     * @return true if the impacted tile has no connector on the exposed side, false otherwise
     */
    public boolean checkNoConnector(int x, int y) {
        boolean result = false;
        if(x==0){
            boolean flag = true;
            int i = 0;
            while (flag && i < 5) {
                if (validStatus[i][y - 4] == Status.USED) {
                    flag = false;
                    if(Dash_Matrix[i][y - 4].controlCorners(0)==0) {
                        result = true;
                    }
                }
                i++;
            }
        }
        if(x==1){
            boolean flag = true;
            int i = 6;
            while (flag && i >= 0) {
                if (validStatus[y - 5][i] == Status.USED) {
                    flag = false;
                    if(Dash_Matrix[y - 5][i].controlCorners(1)==0) {
                        result = true;
                    }
                }
                i--;
            }
        }
        if(x==2){
            boolean flag = true;
            int i = 4;
            while (flag && i >= 0) {
                if (validStatus[i][y - 4] == Status.USED) {
                    flag = false;
                    if(Dash_Matrix[i][y - 4].controlCorners(2)==0) {
                        result = true;
                    }
                }
                i--;
            }
        }
        if(x==3){
            boolean flag = true;
            int i = 0;
            while (flag && i < 7) {
                if (validStatus[y - 5][i] == Status.USED) {
                    flag = false;
                    if(Dash_Matrix[y - 5][i].controlCorners(3)==0) {
                        result = true;
                    }
                }
                i++;
            }
        }
        return result;
    }

    /**
     * Checks whether the specified value is present among the tile's connectors.
     * This method iterates through the four sides of the tile and returns true
     * if any of them matches the given value.
     * @param t the tile to check
     * @param x the connector value to look for
     * @return true if the value is found in one of the tile's corners, false otherwise
     */
    public boolean checkPresentValue(Tile t, int x){
        boolean result = false;
        for(int i = 0;i<4;i++){
            if(t.controlCorners(i)==x) result = true;
        }
        return result;
    }

    /**
     * Returns a sorted list of all goods currently stored on the player's ship.
     * This method scans all storage units on the dashboard and aggregates their
     * contents into a single list, sorted by the value of each good.
     * @return a sorted list of goods (Colour) present on the ship
     */
    public List<Colour> getTotalListOfGood(){
        List<Colour> tmp = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = getTile(i, j);
                switch (y) {
                    case StorageUnit c  -> {
                        List<Colour> colours = c.getListOfGoods();
                        tmp.addAll(colours);
                    }
                    default -> {}
                }
            }
        }
        Collections.<Colour>sort(tmp);
        return tmp;
    }

    /**
     * Clears one of the two reserved tile positions if it matches the given ID.
     * This is typically used during the setup phase to remove a previously reserved tile.
     * @param id the ID of the tile to remove from position (0,5) or (0,6)
     */
    public void resetValidity(int id){
        Tile tmp =  getTile(0, 5);
        Tile tmp2 = getTile(0, 6);
        if(tmp.getIdTile() == id){
            Dash_Matrix[0][5] = new EmptySpace();
            validStatus[0][5] = Status.FREE;
            return;
        }
        if(tmp2.getIdTile() == id){
                Dash_Matrix[0][6] = new EmptySpace();
                validStatus[0][6] = Status.FREE;
            }
    }

    /**
     * Destroys all tiles currently placed on the player's ship.
     * This method iterates over the entire dashboard and removes every tile
     * except empty spaces. Used when resetting the ship completely.
     * @throws BusinessLogicException if tile removal fails due to game constraints
     */
    public void destroyAll() throws BusinessLogicException {
        for(int j = 0;j<5;j++){
            for(int k = 0;k<7;k++){
                Tile t = Dash_Matrix[j][k];
                switch (t) {
                    case EmptySpace c  -> {}
                    default -> {
                        removeTile(j , k);
                    }
                }
            }
        }
    }
}

