package it.polimi.ingsw.galaxytrucker.Server.Model;

import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * class for the ship dashboard of the player
 * it has all the information for the game
 * @author Matteo Vignocchi & Oleg Nedina
 */
public class Momentaneo {
    //Beginning
    protected int id;
    private int credit;
    //Ship building
    private boolean isReady;
    private boolean isComplete;
    private Tile[][] Dash_Matrix;
    private final Status[][] validStatus;
    private boolean purpleAlien;
    private boolean brownAlien;
    //discard Pile
    private List<Tile> discardPile;
    //In game values
    protected int lap;
    protected int position;
    private boolean isEliminated; //eliminabile

    /**
     * constructor that initialize all the variables
     * it initializes the mask for the dashboard
     * @param id of the player
     * @param isDemo define the type of dashboard
     */
    public Momentaneo(int id, boolean isDemo) {
        this.id = id;
        this.lap = 0;
        this.position = 0;
        this.isEliminated = false;
        this.discardPile = new ArrayList<Tile>();
        credit = 0;
        purpleAlien = false;
        brownAlien = false;
        isComplete = false;
        isReady = false;
        //initialize the matrix
        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
        //place the central unit
        Dash_Matrix[3][2] = new HousingUnit(3,3,3,3,Human.HUMAN);

        //initialized a matrix with the valid position of the ship
        validStatus = new Status[5][7];
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
            validStatus[2][3]  = Status.FREE;
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
            validStatus[0][5]  = Status.BLOCK;
            validStatus[0][6]  = Status.BLOCK;
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
            validStatus[2][3]  = Status.FREE;
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
     * set true when the player is ready to play
     */
    public void setIsReady(){
        isReady = true;
    }

    /**
     * @return the status
     */
    public boolean isReady(){
        return isReady;
    }

    /**
     * set if the player has completed the ship
     */
    public void setComplete(){
        isComplete = true;
    }

    /**
     * @return the status
     */
    public boolean isComplete(){
        return isComplete;
    }

    /**
     * @return id of the player
     */
    public int getId() {
        return id;
    }

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
    public int getCredit(){
        return credit;
    }

    /**
     * this method changes the play order
     * @param pos turn order placement
     */
    public void setPos(int pos) {
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
     * @return number of tile in the discard pile
     */
    public int checkDiscardPile(){
        return discardPile.size();
    }

    /**
     * the method add the tile in the discard pile
     * @param tile
     */
    public void addToDiscardPile(Tile tile){
        discardPile.add(tile);
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
        this.credit = this.credit + credits;
    }

    //Tile insertion and placement methods

    /**
     * remove and replace with empty space the tile
     * @param a raw of the matrix
     * @param b column of the matrix
     */
    public void removeTile(int a, int b) {
        Tile tmp = Dash_Matrix[a][b];
        Dash_Matrix[a][b] = new EmptySpace();
        validStatus[a][b] = Status.FREE;
        if (a == 2 && b == 3) {
            this.setEliminated();
        }
        addToDiscardPile(tmp);
    }

    /**
     * @return a random number from 1 to 6
     */
    public int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;

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
    public void addTile(int x, int y, Tile t) throws IllegalArgumentException {

        if (validStatus[x][y] == Status.FREE ) {
            Dash_Matrix[x][y] = t;
            validStatus[x][y] = Status.USED;

        } else {
            throw new IllegalArgumentException("Position not valid");
        }
    }

    //tile placement validation methods

    /**
     * check if al the engine are display in the correct way
     * every engine's value 6/7 it is not face to the index 2 of the orientation array, will be removed
     * if there is a tile under the index 2 of the orientation array, it will be removed
     */
    public void controlEngine() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = Dash_Matrix[i][j];
                switch (y){
                    case Engine e ->{
                        if(e.controlCorners(2) != 7 && e.controlCorners(2) != 6){
                            removeTile(i, j);
                        }else {
                            for (int x = 4; x > i; x--) {
                                removeTile(i, j);
                            }
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
    public void controlCannon(){
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile c = Dash_Matrix[i][j];
                switch (c){
                    case Cannon e ->{
                        for (int x = 0; x < 4; x++) {
                            if (Dash_Matrix[i][j].controlCorners(x) == 4 || Dash_Matrix[i][j].controlCorners(x) == 5) {
                                if (x == 0) {
                                    for (int y = i-1; y != 0; y--) {
                                        removeTile(y, j);
                                    }
                                }
                                if (x == 1) {
                                    for (int y = j+1; y < 7; y++) {
                                        removeTile(i, y);
                                    }
                                }
                                if (x == 2) {
                                    for (int y = i+1; y < 5; y++) {
                                        removeTile(y, j);
                                    }
                                }
                                if (x == 3) {
                                    for (int y = j-1; y != 0; y--) {
                                        removeTile(i, y);
                                    }
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
     * this method controls if the ship that the player built is legal, checking every tile
     */
    /**
     * this method checks every exposed connectors on the ship
     * first it checks the exposed connectors in the inner matrix,
     * Not considering the first row, the first column, the last row, and the last column
     * @return the amount of exposed connectors
     */
    public int countExposedConnectors() {
        int tmp = 0;
        //inner matrix check
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < 7; j++) {
                int a, b;
                if (validStatus[i][j] == Status.USED) {
                    a = Dash_Matrix[i][j].controlCorners(0);
                    b = Dash_Matrix[i - 1][j].controlCorners(2);
                    if ((a < 4 && a != 0) && (a + b == a)) {
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(1);
                    b = Dash_Matrix[i][j + 1].controlCorners(3);
                    if ((a < 4 && a != 0) && (a + b == a)) {
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(2);
                    b = Dash_Matrix[i + 1][j].controlCorners(0);
                    if ((a < 4 && a != 0) && (a + b == a)) {
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(3);
                    b = Dash_Matrix[i][j - 1].controlCorners(1);
                    if ((a < 4 && a != 0) && (a + b == a)) {
                        tmp++;
                    }
                }
            }
        }
        //first row check
        for (int i = 1; i < 6; i++) {
            int a, b;
            if (validStatus[0][i] == Status.USED) {
                a = Dash_Matrix[0][i].controlCorners(3);
                b = Dash_Matrix[0][i - 1].controlCorners(1);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(2);
                b = Dash_Matrix[1][i].controlCorners(0);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(1);
                b = Dash_Matrix[0][i + 1].controlCorners(3);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(0);
                if ((a < 4 && a != 0)) {
                    tmp++;
                }
            }
        }
        //last row check
        for (int i = 1; i < 6; i++) {
            int a, b;
            if (validStatus[4][i] == Status.USED) {
                a = Dash_Matrix[4][i].controlCorners(3);
                b = Dash_Matrix[4][i - 1].controlCorners(1);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(0);
                b = Dash_Matrix[3][i].controlCorners(2);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(1);
                b = Dash_Matrix[4][i + 1].controlCorners(3);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(2);
                if ((a < 4 && a != 0)) {
                    tmp++;
                }
            }
        }
        //first column check
        for (int i = 1; i < 4; i++) {
            int a, b;
            if (validStatus[i][0] == Status.USED) {
                a = Dash_Matrix[i][0].controlCorners(0);
                b = Dash_Matrix[i - 1][0].controlCorners(2);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[i][0].controlCorners(1);
                b = Dash_Matrix[i][1].controlCorners(3);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[i][0].controlCorners(2);
                b = Dash_Matrix[i + 1][0].controlCorners(0);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }

                a = Dash_Matrix[i][0].controlCorners(3);
                if (a < 4 && a != 0) {
                    tmp++;
                }
            }
        }
        //last column check
        for (int i = 1; i < 4; i++) {
            int a, b;
            if (validStatus[i][6] == Status.USED) {
                a = Dash_Matrix[i][6].controlCorners(0);
                b = Dash_Matrix[i - 1][6].controlCorners(2);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[i][6].controlCorners(3);
                b = Dash_Matrix[i][5].controlCorners(1);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }
                a = Dash_Matrix[i][6].controlCorners(2);
                b = Dash_Matrix[i + 1][6].controlCorners(0);
                if ((a < 4 && a != 0) && (a + b == a)) {
                    tmp++;
                }

                a = Dash_Matrix[i][6].controlCorners(1);
                if (a < 4 && a != 0) {
                    tmp++;
                }
            }
        }
        int a, b;
        //this checks the connectors of the tiles facing outward from the ship, not connected to any other tile
        if (validStatus[4][0] == Status.USED ) {
            a = Dash_Matrix[4][0].controlCorners(0);
            b = Dash_Matrix[3][0].controlCorners(2);
            if ((a < 4 && a != 0) && (a + b == a)) {
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(1);
            b = Dash_Matrix[4][1].controlCorners(3);
            if ((a < 4 && a != 0) && (a + b == a)) {
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(2);
            if (a < 4 && a != 0) {
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(3);
            if (a < 4 && a != 0) {
                tmp++;
            }
        }
        if (validStatus[4][6] == Status.USED) {
            a = Dash_Matrix[4][6].controlCorners(0);
            b = Dash_Matrix[3][6].controlCorners(2);
            if ((a < 4 && a != 0) && (a + b == a)) {
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(3);
            b = Dash_Matrix[4][5].controlCorners(1);
            if ((a < 4 && a != 0) && (a + b == a)) {
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(2);
            if (a < 4 && a != 0) {
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(1);
            if (a < 4 && a != 0) {
                tmp++;
            }
        }
        //the result
        return tmp;
    }

    //directional removal methods
    /**
     * Support method for verifying if the ship is being attacked and hit from north
     * the method remove the first tile hit
     * @param dir2 column index
     */
    public void removeFrom0(int dir2) {
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
    public void removeFrom1(int dir2) {
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
    public void removeFrom2(int dir2) {
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
    public void removeFrom3(int dir2) {
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


    public boolean checkFirstColumn() {
        boolean result = false;
        int a, b;
        for (int i = 1; i < 4; i++) {
            boolean flag = true;
            if (validStatus[i][0] == Status.USED) {

                if (validStatus[i + 1][0] == Status.FREE && validStatus[i - 1][0] == Status.FREE && validStatus[i][1] == Status.FREE)
                    flag = false;

                if (flag) {
                    a = Dash_Matrix[i][0].controlCorners(0);
                    b = Dash_Matrix[i - 1][0].controlCorners(2);
                    if (a * b != 0) {
                        if ((a-b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[i][0].controlCorners(2);
                    b = Dash_Matrix[i + 1][0].controlCorners(0);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[i][0].controlCorners(1);
                    b = Dash_Matrix[i][1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                }

            }
            if (!flag) {
                result = true;
                removeTile(i, 0);
            }
        }
        return result;
    }

    public boolean checkLastColumn() {
        boolean result = false;
        int a, b;
        for (int i = 1; i < 4; i++) {
            boolean flag = true;
            if (validStatus[i][6] == Status.USED) {

                if (validStatus[i + 1][6] == Status.FREE && validStatus[i - 1][6] == Status.FREE && validStatus[i][5] == Status.FREE)
                    flag = false;

                if (flag) {
                    a = Dash_Matrix[i][6].controlCorners(0);
                    b = Dash_Matrix[i - 1][6].controlCorners(2);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[i][6].controlCorners(2);
                    b = Dash_Matrix[i + 1][6].controlCorners(0);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[i][6].controlCorners(3);
                    b = Dash_Matrix[i][5].controlCorners(1);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                }

            }
            if (!flag) {
                result = true;
                removeTile(i, 6);
            }
        }
        return result;
    }

    public boolean checkInnerMatrix() {
        boolean result = false;
        int a, b;
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 6; j++) {

                boolean flag = true;

                if (validStatus[i][j] == Status.USED) {
                    if (validStatus[i - 1][j] == Status.FREE && validStatus[i+1][j] == Status.FREE && validStatus[i][j+1] == Status.FREE && validStatus[i][j-1] == Status.FREE) flag = false;

                    if(flag){
                        a = Dash_Matrix[i][j].controlCorners(0);
                        b = Dash_Matrix[i - 1][j].controlCorners(2);
                        if (a * b != 0) {
                            if ((a - b) != 0) {
                                if (a != 3 && b != 3) {
                                    flag = false;
                                }
                            }
                        }
                    }
                    else if (flag) {
                        a = Dash_Matrix[i][j].controlCorners(2);
                        b = Dash_Matrix[i + 1][j].controlCorners(0);
                        if (a * b != 0) {
                            if ((a - b) != 0) {
                                if(a != 3 && b != 3){
                                    flag = false;
                                }
                            }
                        }

                    }
                    else if (flag) {
                        a = Dash_Matrix[i][j].controlCorners(3);
                        b = Dash_Matrix[i][j-1].controlCorners(1);
                        if (a * b != 0) {
                            if ((a - b) != 0) {
                                if (a != 3 && b != 3) {
                                    flag = false;
                                }
                            }
                        }

                    }
                    else if (flag) {
                        a = Dash_Matrix[i][j].controlCorners(1);
                        b = Dash_Matrix[i][j+1].controlCorners(3);
                        if (a * b != 0) {
                            if ((a - b) != 0) {
                                if (a != 3 && b != 3) {
                                    flag = false;
                                }
                            }
                        }

                    }
                }

                if (!flag) {
                    result = true;
                    removeTile(i, j);
                }
            }
        }
        return result;
    }

    public boolean checkFirstRow(){
        boolean result = false;
        int a, b;
        for (int i = 1; i < 6; i++) {
            boolean flag = true;
            if(validStatus[0][i] == Status.USED){
                if(validStatus[0][i-1] == Status.FREE && validStatus[0][i+1] == Status.FREE && validStatus[1][i] == Status.FREE) flag = false;

                if(flag){
                    a = Dash_Matrix[0][i].controlCorners(3);
                    b = Dash_Matrix[0][i-1].controlCorners(1);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[0][i].controlCorners(1);
                    b = Dash_Matrix[0][i+1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }

                } else if (flag) {
                    a = Dash_Matrix[0][i].controlCorners(2);
                    b = Dash_Matrix[1][i].controlCorners(0);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }

                }
            }
            if (!flag) {
                result = true;
                removeTile(0, i);
            }

        }
        return result;
    }

    public boolean checkLastRow(){
        boolean result = false;
        int a, b;
        for (int i = 1; i < 6; i++) {
            boolean flag = true;
            if(validStatus[4][i] == Status.USED){
                if(validStatus[4][i-1] == Status.FREE && validStatus[4][i+1] == Status.FREE && validStatus[3][i] == Status.FREE) flag = false;
                if(flag){
                    a = Dash_Matrix[4][i].controlCorners(1);
                    b = Dash_Matrix[4][i+1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                } else if (flag) {
                    a = Dash_Matrix[4][i].controlCorners(3);
                    b = Dash_Matrix[4][i-1].controlCorners(1);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }

                }else if (flag) {
                    a = Dash_Matrix[4][i].controlCorners(2);
                    b = Dash_Matrix[3][i].controlCorners(0);
                    if (a * b != 0) {
                        if ((a - b) != 0) {
                            if (a != 3 && b != 3) {
                                flag = false;
                            }
                        }
                    }
                }
            }
            if (!flag) {
                result = true;
                removeTile(4, i);
            }
        }
        return result;
    }

    public boolean checkSx(){
        boolean result = false;
        int a, b;
        boolean flag = true;
        if(validStatus[4][0] == Status.USED){
            if(validStatus[4][1] == Status.FREE && validStatus[3][0] == Status.FREE) flag = false;
            if(flag){
                a = Dash_Matrix[4][0].controlCorners(1);
                b = Dash_Matrix[4][1].controlCorners(3);
                if (a * b != 0) {
                    if ((a - b) != 0) {
                        if (a != 3 && b != 3) {
                            flag = false;
                        }
                    }
                }
            }
            else if (flag) {
                a = Dash_Matrix[4][0].controlCorners(0);
                b = Dash_Matrix[3][0].controlCorners(2);
                if (a * b != 0) {
                    if ((a - b) != 0) {
                        if (a != 3 && b != 3) {
                            flag = false;
                        }
                    }
                }
            }
        }
        if (!flag) {
            result = true;
            removeTile(4,0);
        }
        return result;
    }

    public boolean checkDx(){
        boolean result = false;
        int a, b;
        boolean flag = true;
        if(validStatus[4][6] == Status.USED){
            if(validStatus[4][5] == Status.FREE && validStatus[3][6] == Status.FREE) flag = false;
            if(flag){
                a = Dash_Matrix[4][6].controlCorners(3);
                b = Dash_Matrix[4][5].controlCorners(1);
                if (a * b != 0) {
                    if ((a - b) != 0) {
                        if (a != 3 && b != 3) {
                            flag = false;
                        }
                    }
                }
            }else if (flag) {
                a = Dash_Matrix[4][6].controlCorners(0);
                b = Dash_Matrix[3][6].controlCorners(2);
                if (a * b != 0) {
                    if ((a - b) != 0) {
                        if (a != 3 && b != 3) {
                            flag = false;
                        }
                    }
                }
            }
        }
        if (!flag) {
            result = true;
            removeTile(4,6);
        }
        return result;
    }

    public void controlAssembly() {
        boolean flag = true;
        controlCannon();
        controlEngine();
        while (flag) {
            if(checkFirstRow() && checkLastRow() && checkSx() && checkDx() && checkFirstColumn() && checkLastColumn() && checkInnerMatrix()){
                flag = false;
            }

        }
    }



}
























