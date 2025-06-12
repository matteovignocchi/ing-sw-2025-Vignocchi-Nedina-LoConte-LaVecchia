package it.polimi.ingsw.galaxytrucker.Model;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
//import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.*;

import java.io.Serializable;
import java.util.*;


/**
 * class for the ship dashboard of the player
 * it has all the information for the game
 * @author Matteo Vignocchi & Oleg Nedina
 */
public class Player implements Serializable {
    //Beginning
    private final int id;
    private boolean connected;
    private int credits;
    //Ship building
    private int idPhoto;
    private Tile[][] Dash_Matrix;
    private final Status[][] validStatus;
    private boolean purpleAlien;
    private boolean brownAlien;
    private boolean isdemo;
    //discard Pile
    private List<Tile> discardPile;
    //In game values
    //Si inizia a contare da 1 per le posizioni
    protected int lap;
    protected int position;
    private boolean isEliminated;
    private GamePhase gamePhase;
    private Tile lastTile;

    /**
     * constructor that initialize all the variables
     * it initializes the mask for the dashboard
     * @param id of the player
     * @param isDemo define the type of dashboard
     */
    public Player(int id, boolean isDemo ,int idPhoto) {
        this.id = id;
        this.lap = 0;
        this.position = 0;
        this.isEliminated = false;
        this.connected = true;
        this.discardPile = new ArrayList<Tile>();
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
        Dash_Matrix[2][3] = new HousingUnit(3,3,3,3, Human.HUMAN ,0);

        //initialized a matrix with the valid position of the ship
        validStatus = new Status[5][7];
        validStatus[2][3] = Status.BLOCK;
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

    public Tile[][] getDashMatrix() {
        return Dash_Matrix;
    }

    /**
     * @return id of the player
     */
    public int getId() {
        return id;
    }

    public boolean isConnected() { return connected; }

    public void setConnected(boolean connected) { this.connected = connected; }


    public Tile getLastTile(){return lastTile;}
    /**
     * @return how many laps the player did
     */
    public int getLap() {
        return lap;
    }

    /**
     * @return the position where the player is
     */
    public int getPos() {
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
    public void setPos(int pos) {
        position = pos;
    }

    public void setLastTile (Tile t) {this.lastTile = t;}
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

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    /**
     * @return number of tile in the discard pile
     */
    public int checkDiscardPile(){
        return discardPile.size();
    }

    /**
     * the method add the tile in the discard pile
     * @param tile
     */
    public void addToDiscardPile(Tile tile) throws BusinessLogicException{
        if(gamePhase != GamePhase.BOARD_SETUP) discardPile.add(tile);
        else {
            if(discardPile.size()>=2) throw new BusinessLogicException("too many Reserved Tiles");
            discardPile.add(tile);
        }
    }

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
     * this method changes the status of the player, so when it is eliminated
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

    //Tile insertion and placement methods

    /**
     * remove and replace with empty space the tile
     * @param a raw of the matrix
     * @param b column of the matrix
     */
    public void removeTile(int a, int b) throws BusinessLogicException {
        addToDiscardPile(Dash_Matrix[a][b]);
        Dash_Matrix[a][b] = new EmptySpace();
        validStatus[a][b] = Status.FREE;
//        if (a == 2 && b == 3) {
//            this.setEliminated();
//        }
    }

    /**
     * @return a random number from 1 to 6
     */
    public int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;

    }

    //metodo che serve per i test
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

    public List<Tile> getTilesInDiscardPile() {
        return discardPile;
    }

    public void removeTileInDiscardPile(Tile tile) {
        discardPile.remove(tile);
    }


    //tile placement validation methods

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
     * check if all the cannon is display in the correct way
     * it checks whether any cannons are facing inward toward the ship, if so, they are removed
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
    //funzioni di supporto ai metodi

    public void controlOfConnection(){
        int [] dx = {-1, 0, 1, 0};
        int [] dy = {0, 1, 0, -1};
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 7; y++) {
                Tile housing = Dash_Matrix[x][y];
                switch (housing){
                    case HousingUnit e->{
                        for (int i = 0; i < 4; i++) {
                            int nx = x + dx[i];
                            int ny = y + dy[i];
                            if(isOutOfBounds(nx,ny)) continue;
                            Tile nearTmp = Dash_Matrix[nx][ny];
                            switch (nearTmp){
                                case HousingUnit e1-> {
                                   if(e1.getType() != Human.HUMAN){
                                       e.setConnected(true);
                                       e.setTypeOfConnections(e1.getType());
                                   }
                                }
                                default -> {}
                            }
                        }
                    }
                    default -> {}
                }
            }
        }
    }

//    public void setConnectionOfHousing(){
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 7; j++) {
//                Tile c = Dash_Matrix[i][j];
//                switch (c){
//                    case HousingUnit e->{
//                        isDirectlyConnectedToAnotherHousingUnit(i , j , e);
//                    }
//                    default -> {}
//                }
//            }
//        }
//
//    }

//    public void isDirectlyConnectedToAnotherHousingUnit(int x, int y , HousingUnit housing) {
//        int[] dx = {-1, 0, 1, 0}; // Nord, Est, Sud, Ovest
//        int[] dy = {0, 1, 0, -1};
//        int[] opp = {2, 3, 0, 1}; // Lato opposto
//
//        for (int dir = 0; dir < 4; dir++) {
//            int nx = x + dx[dir];
//            int ny = y + dy[dir];
//
//            if (isOutOfBounds(nx, ny)) continue;
//
//            Tile neighbor = Dash_Matrix[nx][ny];
//            if (neighbor instanceof HousingUnit) {
//                int sideA = Dash_Matrix[x][y].controlCorners(dir);
//                int sideB = neighbor.controlCorners(opp[dir]);
//                if (connected(sideA, sideB)) {
//                    housing.setConnected(true);
//                    housing.setTypeOfConnections(((HousingUnit) neighbor).getType());
//                }
//            }
//        }
//    }





    private boolean connected(int i, int j) {

        if(i == 0 || j == 0) return false;
        if(i == 3 || j == 3 ) return true;
        return  i == j;

    }
    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || y < 0 || x >= 5 || y >= 7;
    }


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

    public void controlAssembly(int x , int y) throws BusinessLogicException {
        controlEngine();
        controlCannon();
        if(!isdemo){
            removeTile(0,5);
            removeTile(0,6);
        }
        boolean flag = true;
        while(flag){
            flag = false;
            boolean tmp = controlAssembly2(x,y);
            if(tmp) flag = true;
        }
        controlOfConnection();
    }

    public boolean controlAssembly2(int xx, int yy) throws BusinessLogicException {
        //istanzio le mie variabili
        int count = 0;
        boolean[][] visited = new boolean[5][7];
        boolean[][] wrongConnection = new boolean[5][7];
        Queue<int[]> queue = new LinkedList<>();
        //array di dir per suo nel codice:
        int [] dx = {-1, 0, 1, 0};
        int [] dy = {0, 1, 0, -1};
        int [] opp = {2,3,0,1};
        //addo
        queue.add(new int[] {2,3});
        visited[xx][yy] = true;

        //inizio algoritmo di ricerca per nodale:
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0];
            int y = curr[1];

            for(int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                if(isOutOfBounds(nx,ny) || visited[nx][ny]) continue;
                if(validStatus[nx][ny] == Status.FREE) continue;
                int currentSide = Dash_Matrix[x][y].controlCorners(i);
                int nearSide = Dash_Matrix[nx][ny].controlCorners(opp[i]);
                if(connected(currentSide,nearSide)){
                    queue.add(new int[] {nx,ny});
                    visited[nx][ny] = true;
                }else{
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
     * this method checks every exposed connectors on the ship
     * first it checks the exposed connectors in the inner matrix,
     * Not considering the first row, the first column, the last row, and the last column
     * @return the amount of exposed connectors
     */

//    public int countExposedConnectors() {
//        int tmp = 0;
//        //inner matrix check
//        for (int i = 1; i < 5; i++) {
//            for (int j = 1; j < 7; j++) {
//                int a, b;
//                if (validStatus[i][j] == Status.USED) {
//                    a = Dash_Matrix[i][j].controlCorners(0);
//                    b = Dash_Matrix[i - 1][j].controlCorners(2);
//                    if ((a < 4 && a != 0) && (a + b == a)) {
//                        tmp++;
//                    }
//                    a = Dash_Matrix[i][j].controlCorners(1);
//                    b = Dash_Matrix[i][j + 1].controlCorners(3);
//                    if ((a < 4 && a != 0) && (a + b == a)) {
//                        tmp++;
//                    }
//                    a = Dash_Matrix[i][j].controlCorners(2);
//                    b = Dash_Matrix[i + 1][j].controlCorners(0);
//                    if ((a < 4 && a != 0) && (a + b == a)) {
//                        tmp++;
//                    }
//                    a = Dash_Matrix[i][j].controlCorners(3);
//                    b = Dash_Matrix[i][j - 1].controlCorners(1);
//                    if ((a < 4 && a != 0) && (a + b == a)) {
//                        tmp++;
//                    }
//                }
//            }
//        }
//        //first row check
//        for (int i = 1; i < 6; i++) {
//            int a, b;
//            if (validStatus[0][i] == Status.USED) {
//                a = Dash_Matrix[0][i].controlCorners(3);
//                b = Dash_Matrix[0][i - 1].controlCorners(1);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[0][i].controlCorners(2);
//                b = Dash_Matrix[1][i].controlCorners(0);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[0][i].controlCorners(1);
//                b = Dash_Matrix[0][i + 1].controlCorners(3);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[0][i].controlCorners(0);
//                if ((a < 4 && a != 0)) {
//                    tmp++;
//                }
//            }
//        }
//        //last row check
//        for (int i = 1; i < 6; i++) {
//            int a, b;
//            if (validStatus[4][i] == Status.USED) {
//                a = Dash_Matrix[4][i].controlCorners(3);
//                b = Dash_Matrix[4][i - 1].controlCorners(1);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[4][i].controlCorners(0);
//                b = Dash_Matrix[3][i].controlCorners(2);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[4][i].controlCorners(1);
//                b = Dash_Matrix[4][i + 1].controlCorners(3);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[4][i].controlCorners(2);
//                if ((a < 4 && a != 0)) {
//                    tmp++;
//                }
//            }
//        }
//        //first column check
//        for (int i = 1; i < 4; i++) {
//            int a, b;
//            if (validStatus[i][0] == Status.USED) {
//                a = Dash_Matrix[i][0].controlCorners(0);
//                b = Dash_Matrix[i - 1][0].controlCorners(2);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[i][0].controlCorners(1);
//                b = Dash_Matrix[i][1].controlCorners(3);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[i][0].controlCorners(2);
//                b = Dash_Matrix[i + 1][0].controlCorners(0);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//
//                a = Dash_Matrix[i][0].controlCorners(3);
//                if (a < 4 && a != 0) {
//                    tmp++;
//                }
//            }
//        }
//        //last column check
//        for (int i = 1; i < 4; i++) {
//            int a, b;
//            if (validStatus[i][6] == Status.USED) {
//                a = Dash_Matrix[i][6].controlCorners(0);
//                b = Dash_Matrix[i - 1][6].controlCorners(2);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[i][6].controlCorners(3);
//                b = Dash_Matrix[i][5].controlCorners(1);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//                a = Dash_Matrix[i][6].controlCorners(2);
//                b = Dash_Matrix[i + 1][6].controlCorners(0);
//                if ((a < 4 && a != 0) && (a + b == a)) {
//                    tmp++;
//                }
//
//                a = Dash_Matrix[i][6].controlCorners(1);
//                if (a < 4 && a != 0) {
//                    tmp++;
//                }
//            }
//        }
//        int a, b;
//        //this checks the connectors of the tiles facing outward from the ship, not connected to any other tile
//        if (validStatus[4][0] == Status.USED ) {
//            a = Dash_Matrix[4][0].controlCorners(0);
//            b = Dash_Matrix[3][0].controlCorners(2);
//            if ((a < 4 && a != 0) && (a + b == a)) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][0].controlCorners(1);
//            b = Dash_Matrix[4][1].controlCorners(3);
//            if ((a < 4 && a != 0) && (a + b == a)) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][0].controlCorners(2);
//            if (a < 4 && a != 0) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][0].controlCorners(3);
//            if (a < 4 && a != 0) {
//                tmp++;
//            }
//        }
//        if (validStatus[4][6] == Status.USED) {
//            a = Dash_Matrix[4][6].controlCorners(0);
//            b = Dash_Matrix[3][6].controlCorners(2);
//            if ((a < 4 && a != 0) && (a + b == a)) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][6].controlCorners(3);
//            b = Dash_Matrix[4][5].controlCorners(1);
//            if ((a < 4 && a != 0) && (a + b == a)) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][6].controlCorners(2);
//            if (a < 4 && a != 0) {
//                tmp++;
//            }
//            a = Dash_Matrix[4][6].controlCorners(1);
//            if (a < 4 && a != 0) {
//                tmp++;
//            }
//        }
//        //the result
//        return tmp;
//    }

    //directional removal methods


    //metodo che si rifa q uello che controllAssembly
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
     *
     * Support method for verifying if the ship is being attacked and hit from north
     * the method remove the first tile hit
     * @param dir2 column index
     */
    public void removeFrom0(int dir2) throws BusinessLogicException {
        boolean flag = true;
        int i = 0;
        while (flag && i < 5) {
            if (validStatus[i][dir2 - 4] == Status.USED) {
                flag = false;
                this.removeTile(i, dir2 - 4);
            }
            i++;
        }

    }

    /**
     * Support method for verifying if the ship is being attacked and hit from east
     * the method remove the first tile hit
     * @param dir2 row index
     */
    public void removeFrom1(int dir2) throws BusinessLogicException {
        boolean flag = true;
        int i = 6;
        while (flag && i >= 0) {
            if (validStatus[dir2 - 5][i] == Status.USED) {
                flag = false;
                this.removeTile(dir2-5, i);
            }
            i--;
        }
    }

    /**
     * Support method for verifying if the ship is being attacked and hit from south
     * the method remove the first tile hit
     * @param dir2 column index
     */
    public void removeFrom2(int dir2) throws BusinessLogicException {
        boolean flag = true;
        int i = 4;
        while (flag && i >= 0) {
            if (validStatus[i][dir2 - 4] == Status.USED) {
                flag = false;
                this.removeTile(i, dir2 - 4);
            }
            i--;
        }
    }

    /**
     * Support method for verifying if the ship is being attacked and hit from west
     * the method remove the first tile hit
     * @param dir2 row index
     */
    public void removeFrom3(int dir2) throws BusinessLogicException {
        boolean flag = true;
        int i = 0;
        while (flag && i < 7) {
            if (validStatus[dir2 - 5][i] == Status.USED) {
                flag = false;
                this.removeTile(dir2-5, i);
            }
            i++;
        }
    }

    /**
     * method that checks whether the first non-empty tile hit by a small meteorite has no exposed connectors
     * @param x cardinal direction of the attack
     * @param y the row or column of the attack
     * @return true if there are no connectors exposed
     */
    public boolean checkNoConnector(int x, int y) {
        boolean result = false;
        //check the north side
        if(x==0){
            boolean flag = true;
            int i = 0;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are unexposed
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
        //check the east side
        if(x==1){
            boolean flag = true;
            int i = 6;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are unexposed
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
        //check the south side
        if(x==2){
            boolean flag = true;
            int i = 4;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are unexposed
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
        //check the west side
        if(x==3){
            boolean flag = true;
            int i = 0;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are unexposed
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
     * the method checks whether the value passed to the function is present at the specified index of the orientation array
     * @param t tile under consideration
     * @param x value we want to compare
     * @return true if the values matches
     */
    public boolean checkPresentValue(Tile t, int x){
        boolean result = false;
        for(int i = 0;i<4;i++){
            if(t.controlCorners(i)==x) result = true;
        }
        return result;
    }

    /**
     * this method collects all the goods lists from each storage unit on the player's ship
     * it merges them into a single list
     * @return the list of goods ordered by value
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
}

