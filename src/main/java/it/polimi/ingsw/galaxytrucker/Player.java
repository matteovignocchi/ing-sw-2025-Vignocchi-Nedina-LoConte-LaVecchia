package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.*;

public class Player {
    //Beginning
    protected int id;
    private int credit;
    //Ship building
    private boolean inReady;
    private boolean isComplete;
    private Tile[][] Dash_Matrix; //hashmap?
    private final Status[][] validStatus;
    private boolean purpleAlien;
    private boolean brownAlien;
    //discard Pile
    private List<Tile> discardPile;
    //In game values
    protected int lap;
    protected int position;
    private boolean isEliminated;
    private int totalGoods;

    /**
     * constructor that initialize all the variables
     *
     * @param id
     * @param isDemo define the type of dashboard
     */
    public Player(int id, boolean isDemo) {
        this.id = id;
        this.lap = 0;
        this.position = 0;
        this.isEliminated = false;
        this.discardPile = new ArrayList<Tile>();
        credit = 0;
        totalGoods = 0;
        purpleAlien = false;
        brownAlien = false;
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

    public boolean presencePurpleAlien() { return this.purpleAlien; }

    /**
     * Change the flag that indicates the presence of a brown alien
     */
    public void setBrownAlien() {
        brownAlien = !brownAlien;
    }

    public boolean presenceBrownAlien() { return this.brownAlien; }

    public int checkDiscardPile(){
        return discardPile.size();
    }

    public void addToDiscardPile(Tile tile){
        discardPile.add(tile);
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

    //metodi di inserimento e posizionamento delle tessere

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
     * the method returns if the position is valid
     * @param x row index
     * @param y column index
     * @return true if the place is legal
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

    //metodi di controllo del posizionamento delle tessere

    /**
     * check if al the engine are display in the correct way
     * the value 4/5 involves that there is nothing behind the engine
     * every engine's value 6/7 it is not face to the index 2 of the orientation array, will be removed
     */
    public void controlEngine() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int a = Dash_Matrix[i][j].controlCorners(0);
                int b = Dash_Matrix[i][j].controlCorners(1);
                int c = Dash_Matrix[i][j].controlCorners(2);
                int d = Dash_Matrix[i][j].controlCorners(3);
                if (a == 4 || a == 5 || b == 4 || b == 5 || c == 4 || c == 5 || d == 4 || d == 5) {
                    if (Dash_Matrix[i][j].controlCorners(2) != 6 || Dash_Matrix[i][j].controlCorners(2) != 7) {
                        removeTile(i, j);
                    } else {
                        for (int x = 4; x > i; x--) {
                            removeTile(i, j);
                        }
                    }
                }
            }
        }
    }

    /**
     * check if all the cannon ar display in the correct way
     * the value 4/5 involves that there is nothing ahead the cannon
     */
    public void controlCannon() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int a = Dash_Matrix[i][j].controlCorners(0);
                int b = Dash_Matrix[i][j].controlCorners(1);
                int c = Dash_Matrix[i][j].controlCorners(2);
                int d = Dash_Matrix[i][j].controlCorners(3);
                if (a == 4 || a == 5 || b == 4 || b == 5 || c == 4 || c == 5 || d == 4 || d == 5) {
                    for (int x = 0; x < 4; x++) {
                        if (Dash_Matrix[i][j].controlCorners(x) == 4 || Dash_Matrix[i][j].controlCorners(x) == 5) {
                            if (x == 0) {
                                for (int y = 0; y < i; y++) {
                                    removeTile(i, j);
                                }
                            }
                            if (x == 1) {
                                for (int y = 6; y > j; y--) {
                                    removeTile(i, j);
                                }
                            }
                            if (x == 2) {
                                for (int y = 4; y > i; y--) {
                                    removeTile(i, j);
                                }
                            }
                            if (x == 3) {
                                for (int y = 0; y < j; y++) {
                                    removeTile(i, j);
                                }
                            }
                        }
                    }

                }

            }
        }
    }

    /**
     * this method controls if the ship that the player built is legal, checking every tile
     */
    public void controlAssembly() {
        int a, b;
        controlCannon();
        controlEngine();
        //check the first column
        for (int i = 1; i < 4; i++) {
            boolean tmp = false;
            if (validStatus[i][0]==Status.USED) {
                a = Dash_Matrix[i][0].controlCorners(0);
                b = Dash_Matrix[i - 1][1].controlCorners(2);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][0].controlCorners(2);
                    b = Dash_Matrix[i + 1][0].controlCorners(0);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][0].controlCorners(3);
                    b = Dash_Matrix[i][1].controlCorners(1);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                }
                if (tmp) {
                    removeTile(i, 0);
                }
            }
        }
        //check the last column
        for (int i = 1; i < 4; i++) {
            boolean tmp = false;
            if (validStatus[i][6] == Status.USED) {
                a = Dash_Matrix[i][6].controlCorners(0);
                b = Dash_Matrix[i - 1][6].controlCorners(2);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][6].controlCorners(2);
                    b = Dash_Matrix[i + 1][6].controlCorners(0);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][6].controlCorners(3);
                    b = Dash_Matrix[i][5].controlCorners(1);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                }
                if (tmp) {
                    removeTile(i, 6);
                }
            }
        }
        //check the first row
        for (int i = 1; i < 6; i++) {
            boolean tmp = false;
            if (validStatus[0][i] == Status.USED) {
                a = Dash_Matrix[0][i].controlCorners(3);
                b = Dash_Matrix[0][i - 1].controlCorners(1);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[0][i].controlCorners(1);
                    b = Dash_Matrix[0][i + 1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[0][i].controlCorners(2);
                    b = Dash_Matrix[1][i].controlCorners(0);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                }
                if (tmp) {
                    removeTile(i, 0);
                }
            }
        }
        //check the last row
        for (int i = 1; i < 6; i++) {
            boolean tmp = false;
            if (validStatus[4][i] == Status.USED) {
                a = Dash_Matrix[4][i].controlCorners(3);
                b = Dash_Matrix[4][i - 1].controlCorners(1);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[4][i].controlCorners(1);
                    b = Dash_Matrix[4][i + 1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[4][i].controlCorners(2);
                    b = Dash_Matrix[3][i].controlCorners(2);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                }
                if (tmp) {
                    removeTile(i, 0);
                }
            }
        }
        //inner matrix check
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 6; j++) {
                boolean tmp = false;

                if (!tmp) {
                    a = Dash_Matrix[i][j].controlCorners(0);
                    b = Dash_Matrix[i - 1][j].controlCorners(2);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][j].controlCorners(1);
                    b = Dash_Matrix[i][j + 1].controlCorners(3);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][j].controlCorners(2);
                    b = Dash_Matrix[i + 1][j].controlCorners(0);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][j].controlCorners(3);
                    b = Dash_Matrix[i][j - 1].controlCorners(1);
                    if (a * b != 0) {
                        if ((a + b) / 2 != a) {
                            if (a != 3 && b != 3) {
                                tmp = true;
                            }
                        }
                    }
                }
                if (tmp) removeTile(i, j);
            }
        }
        //check bottom left corner
        if (validStatus[4][0] == Status.USED) {
            boolean tmp = false;
            a = Dash_Matrix[4][0].controlCorners(0);
            b = Dash_Matrix[3][0].controlCorners(2);
            if (a * b != 0) {
                if ((a + b) / 2 != a) {
                    if (a != 3 && b != 3) {
                        tmp = true;
                    }
                }
            } else if (!tmp) {
                a = Dash_Matrix[4][0].controlCorners(1);
                b = Dash_Matrix[4][1].controlCorners(3);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                }
            }
            if (tmp) {
                removeTile(4, 0);
            }
        }
        //check bottom right corner
        if (validStatus[4][6] == Status.USED) {
            boolean tmp = false;
            a = Dash_Matrix[4][6].controlCorners(0);
            b = Dash_Matrix[3][6].controlCorners(2);
            if (a * b != 0) {
                if ((a + b) / 2 != a) {
                    if (a != 3 && b != 3) {
                        tmp = true;
                    }
                }
            } else if (!tmp) {
                a = Dash_Matrix[4][6].controlCorners(3);
                b = Dash_Matrix[4][5].controlCorners(1);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                }
            }
            if (tmp) {
                removeTile(4, 0);
            }
        }
    }

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

    //metodi per rimuovere da una direzione specifica
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
                    if(Dash_Matrix[i][y - 4].controlCorners(0)==0) {
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

    //parti di interazioni con il controller
    /**
     * method used for asking the player if they want to undertake an action
     * @return true if they want to do it
     */
    public boolean askPlayerDecision() {
        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        choice.setTitle("Choose your action");
        choice.setHeaderText(null);
        Optional<ButtonType> result = choice.showAndWait();
        return result.isPresent() && result.get().equals(buttonYes);

    }

    public void removeEnergy(EnergyCell e){
        e.useBattery();
    }

    public EnergyCell selectEnergyCell() {
        //momemnto bisogna gestire con un ciclo while la exception e capire come mi interfaccio con la view
        EnergyCell e = new EnergyCell(1,2,3,4,2);
        return e;
    }
    public StorageUnit seleStorageUnit() {
        //momemnto bisogna gestire con un ciclo while la exception e capire come mi interfaccio con la view
        StorageUnit e = new StorageUnit(1,2,3,4,2 , true);
        return e;
    }
    public StorageUnit selectHousingUnit() {
        //momemnto bisogna gestire con un ciclo while la exception e capire come mi interfaccio con la view
        StorageUnit e = new StorageUnit(1,2,3,4,2 , true);
        return e;
    }

    /**
     * this method return true if the shield protect a specific direction
     * @param s the tile shield to confront with the direction
     * @param dir the direction
     * @return true if the shield protect the direction dir
     */
    public boolean dashProtected(Shield s,int dir){
        if(s.controlCorners(dir)==8){
            return true;
        }
        return false;
    }

    public boolean checkPresentValue(Tile t, int x){
        boolean result = false;
        for(int i = 0;i<4;i++){
            if(t.controlCorners(i)==x) result = true;
        }
        return result;
    }

    public boolean checkProtection(int x, int y) {
        boolean result = false;
        //check the north side
        if (x == 0) {
            //flag for exit from the while if they are protected
            boolean flag = true;
            int i = 0;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are protected
            while (flag && i < 5) {
                if (validStatus[i][y] == Status.USED) {
                    if (Dash_Matrix[i][y - 4].controlCorners(0) == 4) {
                        flag = false;
                        result = true;
                    } else if (Dash_Matrix[i][y - 4].controlCorners(5)==5) {
                        //manca gestione di selezione di una tile e attivazione del double cannone
                        flag = false;
                        //result = activate;
                    }
                }
                i++;
            }
            //check the east side
        } else if (x == 1) {
            //flag for exit from the while if they are protected
            boolean flag = true;
            int i = 5;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are protected
            while (flag && i >= 1) {
                if(validStatus[y - 5][i] == Status.USED) {
                    if ((Dash_Matrix[y - 5][i].controlCorners(1)==4) || (Dash_Matrix[y - 5][i + 1].controlCorners(1)==4) || (Dash_Matrix[y - 5][i - 1].controlCorners(1)==4)) {
                        flag = false;
                        result = true;
                    } else if ((Dash_Matrix[y - 5][i].controlCorners(1)==5) || (Dash_Matrix[y - 5][i + 1].controlCorners(1)==5) || (Dash_Matrix[y - 5][i - 1].controlCorners(1)==5)) {
                        //metodo gestione selezione e attivazione
                        flag = false;
                        result = true;
                    }
                }
                i--;
            }
            //check the west side
        } else if (x == 3) {
            //flag for exit from the while if they are protected
            boolean flag = true;
            int i = 1;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are protected
            while (flag && i < 6) {
                if (validStatus[y - 5][i] == Status.USED) {
                    if ((Dash_Matrix[y - 5][i].controlCorners(3)==4) || (Dash_Matrix[y - 5][i + 1].controlCorners(3)==4) || (Dash_Matrix[y - 5][i - 1].controlCorners(3)==4)) {
                        flag = false;
                        result = true;
                    } else if ((Dash_Matrix[y - 5][i].controlCorners(3)==5) || (Dash_Matrix[y - 5][i + 1].controlCorners(3)==5) || (Dash_Matrix[y - 5][i - 1].controlCorners(3)==5)) {
                        //metodo selezione e attivaione
                        flag = false;
                        result = true;
                    }
                }
                i++;
            }
        }
        return result;
    }






}


