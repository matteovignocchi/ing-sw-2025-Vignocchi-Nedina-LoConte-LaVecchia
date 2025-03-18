package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.*;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
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
    private Status[][] validStatus;
    private boolean purpleAlien;
    private boolean brownAlien;
    //discard Pile
    private List<Tile> discardPile;
    //In game values
    protected int lap;
    protected int position;
    private boolean isEliminated;
    private int totalGoods;

    //creare metodi che ridanno al controllore liste su cui applicare il modello visitor

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
        Dash_Matrix[3][2] = new CentralHousingUnit();

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
     * @return if the player is eliminated or in the game
     */
    public boolean isEliminated() {
        return isEliminated;
    }

    /**
     * this method changes the status of the player, so when it is eliminated
     */
    public void setEliminated() {
        isEliminated = true;
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
     * method used for choosing if they want to use the energy, after choosing the energy cell type tile
     * @param a row index
     * @param b colum index
     * @return true if the player spent a battery
     */
    //devo chiamare solo make decision
    //chiamato make decision chiamo removeEnergy(Dash_Matrix[a][b])
    //quindi sistemo il mio energy cell a cui chiamo questo metodo
    public boolean selectEnergyCell(int a, int b) {
        if (Dash_Matrix[a][b] instanceof TripleEnergyCell) {
            return ((TripleEnergyCell) Dash_Matrix[a][b]).energyManagement();
        } else if (Dash_Matrix[a][b] instanceof DoubleEnergyCell) {
            return ((DoubleEnergyCell) Dash_Matrix[a][b]).energyManagement();
        } else {
            return false;
        }
    }
    public void removeEnergy(DoubleEnergyCell e){
        e.energyManagement();

    }

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
        discardPile.add(tmp);
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected and they want to use a battery
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(int d) {
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof Shield) {
                    if (((Shield) tile).getProtectedCorner(d) == 8) {
                        Scanner scanner = new Scanner(System.in);
                        int x = scanner.nextInt();
                        int y = scanner.nextInt();
                        scanner.close();
                        return selectEnergyCell(x, y);
                    }
                }
            }
        }
        return false;
    }

    /**
     * this method return the firepower, checking every tile
     * this method checks even if there is a double cannon and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the purple alien, with the flag on the player and adds the bonus
     * @return the total amount of firepower
     */
    public double getFirePower() {
        double tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                int a = tile.controlCorners(0);
                int b = tile.controlCorners(1);
                int c = tile.controlCorners(2);
                int d = tile.controlCorners(3);
                if (a == 4 || b==4 || c==4 || d==4) {
                    tmp = tmp + ((Cannon) tile).getPower();
                } else if (a == 5 || b==5 || c==5 || d==5) {
                    Scanner scanner = new Scanner(System.in);
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.close();
                    boolean boh = selectEnergyCell(x, y);
                    tmp = tmp + ((DoubleCannon) tile).getPower(boh);
                }
            }
        }
        if (purpleAlien) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    /**
     * this method return the engine power, checking every tile
     * this method checks even if there is a double engine and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the brown alien, with the flag on the player and adds the bonus
     * @return the total amount of engine power
     */
    public int getPowerEngine() {
        int tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                int a = tile.controlCorners(0);
                int b = tile.controlCorners(1);
                int c = tile.controlCorners(2);
                int d = tile.controlCorners(3);
                if (a == 6 || b == 6 || c == 6 || d == 6) {
                    tmp++;
                } else if (a == 7 || b == 7 || c == 7 || d == 7) {
                    Scanner scanner = new Scanner(System.in);
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.close();
                    boolean activate = selectEnergyCell(x, y);
                    tmp = tmp + ((DoubleEngine) tile).getPower(activate);
                }
            }
        }
        if (brownAlien) {
            return tmp + 2;
        } else {
            return tmp;
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

    /**
     * method used for counting human token the player has left
     * it checks every tile instance of Housing and get the amount of humans left
     * @return the total amount of human and alien tokens on the player's ship
     */
    public int getCrewmates() {
        int tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof Housing) tmp = tmp + ((Housing) tile).ReturnLenght();
            }
        }
        return tmp;
    }

    /**
     * method used for counting every energy the player has left
     * it checks every tile instance of DoubleEnergyCell or TripleEnergyCell and get the amount of energy left
     * @return the total of energy
     */
    public int getEnergy() {
        int tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof DoubleEnergyCell) {
                    tmp = tmp + ((DoubleEnergyCell) tile).getEnergy();
                } else if (tile instanceof TripleEnergyCell) {
                    tmp = tmp + ((TripleEnergyCell) tile).getEnergy();
                }
            }
        }
        return tmp;
    }

    /**
     * this method remove the humans or the aliens from the tile choose by the player
     * every time they remove the token, it checks the output of the method removeHumans
     * if the flag is 1, it means they removed a human, when it is 2, they removed a purple alien,
     * when it is 3 it means they removed a brown alien
     * when the flag is 2 or 3, the method changes the status of presence status of brown or purple aliens
     * @param i amount of token will be removed
     */
    public void removeCrewmates(int i) {
        Scanner scanner = new Scanner(System.in);
        int x, y;
        //every time a token is remove, we decrease "i" by one until it is 0
        while (i != 0) {
            do {
                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.close();
            } while (!(Dash_Matrix[x][y] instanceof Housing) || (Dash_Matrix[x][y] instanceof CentralHousingUnit));
            if (Dash_Matrix[x][y] instanceof CentralHousingUnit) {
                Humans tmp = new Humans();
                ((CentralHousingUnit) Dash_Matrix[x][y]).RemoveHumans(tmp);
            } else if (Dash_Matrix[x][y] instanceof Housing) {
                while (((Housing) Dash_Matrix[x][y]).ReturnLenght() > 0) {
                    Humans tmp = new Humans();
                    int flag = ((Housing) Dash_Matrix[x][y]).RemoveHumans(tmp);
                    if (flag == 2) {
                        purpleAlien = false;
                    } else if (flag == 3) {
                        brownAlien = false;
                    }
                    i--;
                }
            }
        }
    }

    /**
     * @return amount of credits
     */
    public int getCredit() {
        return credit;
    }

    /**
     * this method changes the amount of credits of the player
     * @param credit the amount earned by the player
     */
    public void addCredits(int credit) {
        this.credit += credit;
    }

    /**
     * the method take a list of good and give the player the decision if they intend to place them all on the ship
     * @param good the list of goods
     * @throws FullGoodsList
     * @throws TooDangerous
     */
    public void addGoods(List<Good> good) throws FullGoodsList, TooDangerous{
        Scanner scanner = new Scanner(System.in);
        int x, y, index;
        while (!good.isEmpty()) { //manca opzione di stop quando il player non vuole aggiungere più roba
            do {
                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.close();
            } while (!(Dash_Matrix[x][y] instanceof Storage));
            index = scanner.nextInt();
            scanner.close();
            Good tmp = good.get(index);
            try {
                ((Storage) Dash_Matrix[x][y]).AddGood(tmp);
            } catch (FullGoodsList | TooDangerous e) {
                //parte di gestione della eccezione
            }
            good.remove(index);
            totalGoods ++;
        }
    }

    /**
     * Change the flag that indicates the presence of a purple alien
     */
    public void setPurpleAlien() {
        purpleAlien = true;
    }

    /**
     * Change the flag that indicates the presence of a brown alien
     */
    public void setBrownAlien() {
        brownAlien = true;
    }

    /**
     * this method adds the tile on the player's shipboard
     * @param x row index
     * @param y column index
     * @param t Tile to be added
     * @throws IllegalArgumentException if the player tries to place the tile in a illegal position
     */
    public void addTile(int x, int y, Tile t) throws IllegalArgumentException {

        if (validStatus[x][y] == Status.FREE ) {
            Dash_Matrix[x][y] = t;
            validStatus[x][y] = Status.USED;

        } else {
            throw new IllegalArgumentException("Position not valid");
        }
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
     * the method return the tile in the position (x,y)
     * @param x row index
     * @param y column index
     * @return the tile in the position (x,y)
     */
    public Tile getTile(int x, int y) {
        return Dash_Matrix[x][y];
    }

    /**
     * @return a random number from 1 to 6
     */
    public int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;

    }

    /**
     * Support method for verifying if the ship is being attacked and hit from north
     * the method remove the first tile hit
     * @param dir2 column index
     */
    public void removeFrom0(int dir2) {
        boolean flag = true;
        int i = 0;
        while (flag && i < 5) {
            if (!(Dash_Matrix[i][dir2 - 4] instanceof EmptySpace)) {
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
            if (!(Dash_Matrix[dir2 - 5][i] instanceof EmptySpace)) {
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
            if (!(Dash_Matrix[i][dir2 - 4] instanceof EmptySpace)) {
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
            if (!(Dash_Matrix[dir2 - 5][i] instanceof EmptySpace)) {
                flag = false;
                this.removeTile(dir2-5, i);
            }
            i++;
        }
    }

    /**
     * the method controls if a small meteorite is going to hit or an empty space, or a small cannon
     * and if it is a double cannon, if the player want to activate it
     * @param x cardinal direction of the attack
     * @param y the row or column of the attack
     * @return true if the tile is protected
     */
    public boolean checkProtection(int x, int y) {
        boolean result = false;
        //check the north side
        if (x == 0) {
            //flag for exit from the while if they are protected
            boolean flag = true;
            int i = 0;
            //it iterates, searching for the first non-empty tile, evaluating whether it is a cannon to determine if they are protected
            while (flag && i < 5) {
                if (!(Dash_Matrix[i][y] instanceof EmptySpace)) {
                    if (Dash_Matrix[i][y - 4] instanceof Cannon) {
                        flag = false;
                        result = true;
                    } else if (Dash_Matrix[i][y - 4] instanceof DoubleCannon) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        result = activate;
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
                if(!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    if ((Dash_Matrix[y - 5][i] instanceof Cannon) || (Dash_Matrix[y - 5][i + 1] instanceof Cannon) || (Dash_Matrix[y - 5][i - 1] instanceof Cannon)) {
                        flag = false;
                        result = true;
                    } else if ((Dash_Matrix[y - 5][i] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i + 1] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i - 1] instanceof DoubleCannon)) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        result = activate;
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
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    if ((Dash_Matrix[y - 5][i] instanceof Cannon) || (Dash_Matrix[y - 5][i + 1] instanceof Cannon) || (Dash_Matrix[y - 5][i - 1] instanceof Cannon)) {
                        flag = false;
                        result = true;
                    } else if ((Dash_Matrix[y - 5][i] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i + 1] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i - 1] instanceof DoubleCannon)) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        result = activate;
                    }
                }
                i++;
            }
        }
        return result;
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
                if (!(Dash_Matrix[i][y - 4] instanceof EmptySpace)) {
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
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
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
                if (!(Dash_Matrix[i][y - 4] instanceof EmptySpace)) {
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
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
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
     * this method evaluates the protection of the ship making use of the other methods
     * @param dir cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromCannon (int dir, boolean type){
            int tmp1, tmp2;
            int dir2;
            tmp1 = throwDice();
            tmp2 = throwDice();
            dir2 = tmp1 + tmp2;
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type || (!isProtected(dir) && !type)) {
                        this.removeFrom0(dir2);
                    }
                }
            }else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type || (!isProtected(dir) && !type)) {
                        this.removeFrom2(dir2);
                    }
                }
            }else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type || (!isProtected(dir) && !type)) {
                        this.removeFrom1(dir2);
                    }
                }
            }else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type || (!isProtected(dir) && !type)) {
                        this.removeFrom3(dir2);
                    }
                }
            }
    }
    /**
     * this method evaluates the protection of the ship making use of the other methods
     * @param dir cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromMeteorite( int dir, boolean type){
            int tmp1, tmp2;
            int dir2;
            tmp1 = throwDice();
            tmp2 = throwDice();
            dir2 = tmp1 + tmp2;
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir,dir2) ) {
                        this.removeFrom0(dir2);
                    }
                    if(!type && checkNoConnector(dir,dir2)) {
                        if(!isProtected(dir)){
                            this.removeFrom2(dir2);
                        }
                   }
                }
            }else if (dir == 2) {
                if(dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir,dir2) ) {
                        this.removeFrom0(dir2);
                    }
                    if(!type && !checkNoConnector(dir,dir2)) {
                        if(!isProtected(dir)){
                            this.removeFrom2(dir2);
                        }
                    }

                }
            }else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir,dir2) ) {
                        this.removeFrom0(dir2);
                    }
                    if(!type && !checkNoConnector(dir,dir2)) {
                        if(!isProtected(dir)){
                            this.removeFrom2(dir2);
                        }
                    }
                }

            }else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir,dir2) ) {
                        this.removeFrom0(dir2);
                    }
                    if(!type && !checkNoConnector(dir,dir2)) {
                        if(!isProtected(dir)){
                            this.removeFrom2(dir2);
                        }
                    }
                }
            }
    }

    /**
     * the method removes the number of goods
     * if the number of goods to eliminates it is bigger then the total goods of the player
     * the number of remaining goods that have not been removed is deducted
     * if the number of battery is smaller than the number left, it stops
     * @param num number of good to be remove
     */
    public void removeGoods(int num) {
        Scanner scanner = new Scanner(System.in);
        int x, y, index;
        int battery = this.getEnergy();
        if (num > totalGoods) {
            int tmp = num - totalGoods;
            while (totalGoods != 0) {
                do {
                    x = scanner.nextInt();
                    y = scanner.nextInt();
                    scanner.close();
                } while (!(Dash_Matrix[x][y] instanceof Storage));
                while (!((Storage) Dash_Matrix[x][y]).getListOfGoods().isEmpty()) {
                    index = scanner.nextInt();
                    ((Storage) Dash_Matrix[x][y]).RemoveGood(index);
                }
                totalGoods--;
            }
            if(battery>tmp) {
                while (tmp != 0) {
                    do {
                        x = scanner.nextInt();
                        y = scanner.nextInt();
                        scanner.close();
                    } while (!(Dash_Matrix[x][y] instanceof DoubleEnergyCell || Dash_Matrix[x][y] instanceof TripleEnergyCell));
                    if (Dash_Matrix[x][y] instanceof DoubleEnergyCell) {
                        ((DoubleEnergyCell) Dash_Matrix[x][y]).removeEnergy();
                    } else {
                        ((TripleEnergyCell) Dash_Matrix[x][y]).removeEnergy();
                    }
                }
            }else{
                while (battery != 0) {
                    do {
                        x = scanner.nextInt();
                        y = scanner.nextInt();
                        scanner.close();
                    } while (!(Dash_Matrix[x][y] instanceof DoubleEnergyCell || Dash_Matrix[x][y] instanceof TripleEnergyCell));
                    if (Dash_Matrix[x][y] instanceof DoubleEnergyCell) {
                        ((DoubleEnergyCell) Dash_Matrix[x][y]).removeEnergy();
                    } else {
                        ((TripleEnergyCell) Dash_Matrix[x][y]).removeEnergy();
                    }
                }
            }
        } else {
            while(num != 0){
                do {
                    x = scanner.nextInt();
                    y = scanner.nextInt();
                    scanner.close();
                } while (!(Dash_Matrix[x][y] instanceof Storage));
                while (!((Storage) Dash_Matrix[x][y]).getListOfGoods().isEmpty()) {
                    index = scanner.nextInt();
                    ((Storage) Dash_Matrix[x][y]).RemoveGood(index);
                }
                num--;
            }
        }
    }

    /**
     * the method check and saves in a set all the housing unit connected at least to one housing unit
     * thereafter it removes one human/alien from each of them
     */
    public void startPlauge() {
            Set<Tile> tempList = new HashSet<>();
            //Inner matrix
            for (int i = 1; i < 4; i++) {
                for (int j = 1; j < 6; j++) {
                    if (Dash_Matrix[i][j] instanceof Housing && Dash_Matrix[i + 1][j] instanceof Housing) {
                        int tmp = Dash_Matrix[i][j].controlCorners(2) * Dash_Matrix[i + 1][j].controlCorners(0);
                        if (tmp != 0) {
                            tempList.add(Dash_Matrix[i][j]);
                        }
                    }
                    if (Dash_Matrix[i][j] instanceof Housing && Dash_Matrix[i - 1][j] instanceof Housing) {
                        int tmp = Dash_Matrix[i][j].controlCorners(0) * Dash_Matrix[i - 1][j].controlCorners(2);
                        if (tmp != 0) {
                            tempList.add(Dash_Matrix[i][j]);
                        }
                    }
                    if (Dash_Matrix[i - 1][j] instanceof Housing && Dash_Matrix[i][j + 1] instanceof Housing) {
                        int tmp = Dash_Matrix[i][j].controlCorners(1) * Dash_Matrix[i][j + 1].controlCorners(3);
                        if (tmp != 0) {
                            tempList.add(Dash_Matrix[i][j]);
                        }
                    }
                    if (Dash_Matrix[i][j] instanceof Housing && Dash_Matrix[i][j - 1] instanceof Housing) {
                        int tmp = Dash_Matrix[i][j].controlCorners(3) * Dash_Matrix[i][j - 1].controlCorners(1);
                        if (tmp != 0) {
                            tempList.add(Dash_Matrix[i][j]);
                        }
                    }
                }
            }
            // check first column
            for (int i = 1; i < 4; i++) {
                if (Dash_Matrix[i][0] instanceof Housing && Dash_Matrix[i + 1][0] instanceof Housing){
                    int tmp = Dash_Matrix[i][0].controlCorners(2) * Dash_Matrix[i + 1][0].controlCorners(0);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][0]);
                    }
                }
                if (Dash_Matrix[i][0] instanceof Housing && Dash_Matrix[i - 1][0] instanceof Housing){
                    int tmp = Dash_Matrix[i][0].controlCorners(0) * Dash_Matrix[i - 1][0].controlCorners(2);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][0]);
                    }
                }
                if (Dash_Matrix[i][0] instanceof Housing && Dash_Matrix[i][1] instanceof Housing){
                    int tmp = Dash_Matrix[i][0].controlCorners(1) * Dash_Matrix[i][0].controlCorners(3);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][0]);
                    }
                }
            }
            // check last column
            for (int i = 1; i < 4; i++) {
                if (Dash_Matrix[i][6] instanceof Housing && Dash_Matrix[i + 1][6] instanceof Housing){
                    int tmp = Dash_Matrix[i][6].controlCorners(2) * Dash_Matrix[i + 1][6].controlCorners(0);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][6]);
                    }
                }
                if (Dash_Matrix[i][6] instanceof Housing && Dash_Matrix[i - 1][6] instanceof Housing){
                    int tmp = Dash_Matrix[i][6].controlCorners(0) * Dash_Matrix[i - 1][6].controlCorners(2);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][0]);
                    }
                }
                if (Dash_Matrix[i][6] instanceof Housing && Dash_Matrix[i][5] instanceof Housing){
                    int tmp = Dash_Matrix[i][6].controlCorners(1) * Dash_Matrix[i][5].controlCorners(3);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[i][6]);
                    }
                }
            }
            // check first row
            for (int i = 1; i < 6; i++){
                if (Dash_Matrix[0][i] instanceof Housing && Dash_Matrix[1][i] instanceof Housing){
                    int tmp = Dash_Matrix[0][i].controlCorners(2) * Dash_Matrix[1][i].controlCorners(0);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[0][i]);
                    }
                }
                if (Dash_Matrix[0][i] instanceof Housing && Dash_Matrix[0][i-1] instanceof Housing){
                    int tmp = Dash_Matrix[0][i].controlCorners(3) * Dash_Matrix[0][i-1].controlCorners(1);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[0][i]);
                    }
                }
                if (Dash_Matrix[0][i] instanceof Housing && Dash_Matrix[0][i+1] instanceof Housing){
                    int tmp = Dash_Matrix[0][i].controlCorners(1) * Dash_Matrix[0][i+1].controlCorners(3);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[0][i]);
                    }
                }
            }
            // check last row
            for (int i = 1; i < 6; i++){
                if (Dash_Matrix[4][i] instanceof Housing && Dash_Matrix[3][i] instanceof Housing){
                    int tmp = Dash_Matrix[4][i].controlCorners(0) * Dash_Matrix[3][i].controlCorners(2);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[4][i]);
                    }
                }
                if (Dash_Matrix[4][i] instanceof Housing && Dash_Matrix[3][i-1] instanceof Housing){
                    int tmp = Dash_Matrix[4][i].controlCorners(3) * Dash_Matrix[3][i-1].controlCorners(1);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[4][i]);
                    }
                }
                if (Dash_Matrix[4][i] instanceof Housing && Dash_Matrix[3][i+1] instanceof Housing){
                    int tmp = Dash_Matrix[4][i].controlCorners(1) * Dash_Matrix[3][i+1].controlCorners(3);
                    if (tmp != 0) {
                        tempList.add(Dash_Matrix[4][i]);
                    }
                }
            }
            //check bottom-left corner
            if(Dash_Matrix[4][0] instanceof Housing){
                if(Dash_Matrix[3][0] instanceof Housing){
                    int tmp = Dash_Matrix[4][0].controlCorners(0) * Dash_Matrix[3][0].controlCorners(2);
                    if(tmp != 0){
                        tempList.add(Dash_Matrix[4][0]);
                    }
                }
            }
            if(Dash_Matrix[4][0] instanceof Housing){
                if(Dash_Matrix[4][1] instanceof Housing){
                    int tmp = Dash_Matrix[4][0].controlCorners(1) * Dash_Matrix[4][1].controlCorners(3);
                    if(tmp != 0){
                        tempList.add(Dash_Matrix[4][0]);
                    }
                }
            }
            //check bottom-right corner
            if(Dash_Matrix[4][6] instanceof Housing){
                if(Dash_Matrix[3][6] instanceof Housing){
                    int tmp = Dash_Matrix[4][6].controlCorners(0) * Dash_Matrix[3][6].controlCorners(2);
                    if(tmp != 0){
                        tempList.add(Dash_Matrix[4][6]);
                    }
                }
            }
            if(Dash_Matrix[4][6] instanceof Housing){
                if(Dash_Matrix[4][5] instanceof Housing){
                    int tmp = Dash_Matrix[4][6].controlCorners(3) * Dash_Matrix[4][5].controlCorners(1);
                    if(tmp != 0){
                        tempList.add(Dash_Matrix[4][6]);
                    }
                }
            }
            //for each housing unit in the set, we remove one human
            for( Tile tile : tempList){
                Humans u = new Humans();
                ((Housing)tile).RemoveHumans(u);
            }
        }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     * @param dir cardinal direction of the attack
     * @param dir2 row or column where the pirate will go to hit
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromPirateCannon (int dir,int dir2, boolean type){
        if (dir == 0){
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(dir) && !type)) {
                    this.removeFrom0(dir2);
                }
            }
        }else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(dir) && !type)) {
                    this.removeFrom2(dir2);
                }
            }
        }else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(dir) && !type)) {
                    this.removeFrom1(dir2);
                }
            }
        }else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(dir) && !type)) {
                    this.removeFrom3(dir2);
                }
            }
        }
    }

    //mancono metodi per gestire se larrivo di un meteorite piccolo colpisce un connettore scoperto o no
    }

