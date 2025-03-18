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
    private boolean[][] validPosition;
    private boolean purpleAlien;
    private boolean brownAlien;
    //discard Pile
    private List<Tile> discardPile;
    //In game values
    protected int lap;
    protected int position;
    private boolean isEliminated;

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
        validPosition = new boolean[5][7];
        if (isDemo) {

            validPosition[0][3] = true;
            validPosition[1][2] = true;
            validPosition[1][3] = true;
            validPosition[1][4] = true;
            validPosition[2][1] = true;
            validPosition[2][2] = true;
            validPosition[2][3] = true;
            validPosition[2][4] = true;
            validPosition[2][5] = true;
            validPosition[3][1] = true;
            validPosition[3][2] = true;
            validPosition[3][3] = true;
            validPosition[3][4] = true;
            validPosition[3][5] = true;
            validPosition[4][1] = true;
            validPosition[4][2] = true;
            validPosition[4][4] = true;
            validPosition[4][5] = true;

        } else {
            validPosition[0][2] = true;
            validPosition[0][4] = true;
            validPosition[1][0] = true;
            validPosition[1][5] = true;
            validPosition[1][2] = true;
            validPosition[1][3] = true;
            validPosition[1][4] = true;
            validPosition[2][0] = true;
            validPosition[2][1] = true;
            validPosition[2][2] = true;
            validPosition[2][3] = true;
            validPosition[2][4] = true;
            validPosition[2][5] = true;
            validPosition[2][6] = true;
            validPosition[3][0] = true;
            validPosition[3][1] = true;
            validPosition[3][2] = true;
            validPosition[3][3] = true;
            validPosition[3][4] = true;
            validPosition[3][5] = true;
            validPosition[3][6] = true;
            validPosition[4][0] = true;
            validPosition[4][1] = true;
            validPosition[4][2] = true;
            validPosition[4][4] = true;
            validPosition[4][5] = true;
            validPosition[4][6] = true;
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
     *
     * @param pos turn order placement
     */
    public void setPos(int pos) {
        position = pos;
    }

    /**
     * this method changes if the player has done a new lap
     *
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
            if (!((Dash_Matrix[i][0] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[i][6] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[0][i] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[4][i] instanceof EmptySpace))) {
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
        if (!(Dash_Matrix[4][0] instanceof EmptySpace)) {
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
        if (!(Dash_Matrix[4][6] instanceof EmptySpace)) {
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
     */
    public void controlCannon() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                if (Dash_Matrix[i][j] instanceof Cannon || Dash_Matrix[i][j] instanceof DoubleCannon) {
                    for (int x = 0; x < 4; x++) {
                        if (Dash_Matrix[i][j].controlCorners(x) == 4 || Dash_Matrix[i][j].controlCorners(x) == 5) {
                            if (x == 0) {
                                for (int y = 0; y < i; y++) {
                                    if (!(Dash_Matrix[y][j] instanceof EmptySpace)) {
                                        removeTile(i, j);
                                    }
                                }
                            }
                            if (x == 1) {
                                for (int y = 6; y > j; y--) {
                                    if (!(Dash_Matrix[y][j] instanceof EmptySpace)) {
                                        removeTile(i, j);
                                    }
                                }
                            }
                            if (x == 2) {
                                for (int y = 4; y > i; y--) {
                                    if (!(Dash_Matrix[y][j] instanceof EmptySpace)) {
                                        removeTile(i, j);
                                    }
                                }
                            }
                            if (x == 3) {
                                for (int y = 0; y < j; y++) {
                                    if (!(Dash_Matrix[i][j] instanceof EmptySpace)) {
                                        removeTile(i, j);
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }
    }

    /**
     * chech if al the engine are display in the correct way
     */
    public void controlEngine() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile tile = Dash_Matrix[i][j];
                if (tile instanceof Engine || tile instanceof DoubleEngine) {
                    if (tile.controlCorners(2) != 6 || tile.controlCorners(2) != 7) {
                        removeTile(i, j);
                    } else {
                        for (int x = 4; x > i; x--) {
                            if (!(Dash_Matrix[x][j] instanceof EmptySpace)) {
                                removeTile(i, j);
                            }
                        }
                    }
                }
            }
        }
    }

    //metodo che prende in ingrsso le cordinate di una tile selezionata da utente e chiama i metodi per usare
    //le batterie , ci dovrebbe essere una exception che mi permette di richiamare il metodo nel caso non sia un contenitore di esergie
    public boolean selectEnergyCell(int a, int b) {
        if (Dash_Matrix[a][b] instanceof TripleEnergyCell) {
            return ((TripleEnergyCell) Dash_Matrix[a][b]).energyManagement();
        } else if (Dash_Matrix[a][b] instanceof DoubleEnergyCell) {
            return ((DoubleEnergyCell) Dash_Matrix[a][b]).energyManagement();
        } else {
            return false;
        }
    }

    /**
     * remove and replace with empty space the tile
     *
     * @param a raw of the matrix
     * @param b column of the matrix
     */
    public void removeTile(int a, int b) {
        Tile tmp = Dash_Matrix[a][b];
        Dash_Matrix[a][b] = new EmptySpace();
        if (Dash_Matrix[a][b] instanceof CentralHousingUnit) {
            this.setEliminated();
        }
        discardPile.add(tmp);
    }

    /**
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
     *
     * @return the total amount of firepower
     */
    public double getFirePower() {
        double tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof Cannon) {
                    tmp = tmp + ((Cannon) tile).getPower();
                } else if (tile instanceof DoubleCannon) {
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
     *
     * @return the total amount of engine power
     */
    public int getPowerEngine() {
        int tmp = 0;
        for (Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof Engine) {
                    tmp++;
                } else if (tile instanceof DoubleEngine) {
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
     *
     * @return the amount of exposed connectors
     */
    public int countExposedConnectors() {
        int tmp = 0;
        //inner matrix check
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < 7; j++) {
                int a, b;
                if (!(Dash_Matrix[i][j] instanceof EmptySpace)) {
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
            if (!((Dash_Matrix[0][i] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[4][i] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[i][0] instanceof EmptySpace))) {
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
            if (!((Dash_Matrix[i][6] instanceof EmptySpace))) {
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
        if (!((Dash_Matrix[4][0] instanceof EmptySpace))) {
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
        if (!((Dash_Matrix[4][6] instanceof EmptySpace))) {
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

    //scelta player molto generale
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
     * this method remove the humans or the aliens from the tile choose by the player
     * every time they remove the token, it checks the output of the method removeHumans
     * if the flag is 1, it means they removed a human, when it is 2, they removed a purple alien,
     * when it is 3 it means they removed a brown alien
     * when the flag is 2 or 3, the method changes the status of presence status of brown or purple aliens
     *
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
     *
     * @param credit the amount earned by the player
     */
    public void addCredits(int credit) {
        this.credit += credit;
    }

    public void addGoods(List<Good> good) throws FullGoodsList {
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

    public void addTile(int x, int y, Tile t) throws IllegalArgumentException {

        if (validityCheck(x, y)) {
            Dash_Matrix[x][y] = t;

        } else {
            throw new IllegalArgumentException("Position not valid");
        }
    }

    public boolean validityCheck(int x, int y) {
        return validPosition[x][y];
    }

    public Tile getTile(int x, int y) {
        return Dash_Matrix[x][y];
    }

    public int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;

    }

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

    public boolean checkProtection(int x, int y) {
        boolean risultato = false;
        if (x == 0) {
            boolean flag = true;
            int i = 0;
            while (flag && i < 5) {
                if (!(Dash_Matrix[i][y] instanceof EmptySpace)) {
                    if (Dash_Matrix[i][y - 4] instanceof Cannon) {
                        flag = false;
                        risultato = true;

                    } else if (Dash_Matrix[i][y - 4] instanceof DoubleCannon) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        risultato = activate;
                    }

                }
                i++;
            }
        } else if (x == 1) {
            boolean flag = true;
            int i = 5;
            while (flag && i >= 1) {
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    if ((Dash_Matrix[y - 5][i] instanceof Cannon) || (Dash_Matrix[y - 5][i + 1] instanceof Cannon) || (Dash_Matrix[y - 5][i - 1] instanceof Cannon)) {
                        flag = false;
                        risultato = true;

                    } else if ((Dash_Matrix[y - 5][i] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i + 1] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i - 1] instanceof DoubleCannon)) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        risultato = activate;
                    }
                }
                i--;
            }
        } else if (x == 3) {
            boolean flag = true;
            int i = 1;
            while (flag && i < 6) {
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    if ((Dash_Matrix[y - 5][i] instanceof Cannon) || (Dash_Matrix[y - 5][i + 1] instanceof Cannon) || (Dash_Matrix[y - 5][i - 1] instanceof Cannon)) {
                        flag = false;
                        risultato = true;

                    } else if ((Dash_Matrix[y - 5][i] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i + 1] instanceof DoubleCannon) || (Dash_Matrix[y - 5][i - 1] instanceof DoubleCannon)) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        scanner.close();
                        boolean activate = selectEnergyCell(a, b);
                        flag = false;
                        risultato = activate;
                    }
                }
                i++;
            }
        }
        return risultato;
    }

    public boolean checkNoConnector(int x, int y) {
        boolean risultato = false;
        if(x==0){
            boolean flag = true;
            int i = 0;
            while (flag && i < 5) {
                if (!(Dash_Matrix[i][y - 4] instanceof EmptySpace)) {
                    flag = false;
                    if(Dash_Matrix[i][y - 4].controlCorners(0)==0) {
                        risultato = true;
                    }
                }
                i++;
            }

        }
        if(x==1){
            boolean flag = true;
            int i = 6;
            while (flag && i >= 0) {
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    flag = false;
                    if(Dash_Matrix[y - 5][i].controlCorners(1)==0) {
                        risultato = true;
                    }
                }
                i--;
            }

        }
        if(x==2){
            boolean flag = true;
            int i = 4;
            while (flag && i >= 0) {
                if (!(Dash_Matrix[i][y - 4] instanceof EmptySpace)) {
                    flag = false;
                    if(Dash_Matrix[i][y - 4].controlCorners(0)==0) {
                        risultato = true;
                    }
                }
                i--;
            }
        }
        if(x==3){
            boolean flag = true;
            int i = 0;
            while (flag && i < 7) {
                if (!(Dash_Matrix[y - 5][i] instanceof EmptySpace)) {
                    flag = false;
                    if(Dash_Matrix[y - 5][i].controlCorners(3)==0) {
                        risultato = true;
                    }
                }
                i++;
            }

        }
        return risultato;
    }

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




    public void removeGoods(int rmv){}
    //metodo che elimina un human da ogni housing unit che sia collegata a un'altra
    //mi creo una lista che contiene un puntatore a tutte le housing unit già collegate tra loro, per oguna di
    //essa verifico che la presenza di umani, e se c'è chiamo un metodo remove uman che me ne toglie uno
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
    //mancono metodi per gestire se larrivo di un meteorite piccolo colpisce un connettore scoperto o no
    }

