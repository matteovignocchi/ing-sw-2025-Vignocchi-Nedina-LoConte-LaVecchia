package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.*;
import it.polimi.ingsw.galaxytrucker.Token.BrownAlien;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Player {
    //parte iniziale
    protected int id;
    private int credit;

    //parte costruzione nave
    private boolean inReady;
    private boolean isComplete;
    private Tile[][] Dash_Matrix; //hashmap?
    private boolean[][] validPosition;
    private boolean purpleAlien;
    private boolean brownAlien;

    //discsrdPile
    private List<Tile> discardPile;

    protected int lap;
    protected int position;
    private boolean isEliminated;


    /**
     * constructor that initialize all the variable
     * @param id
     * @param isDemo define the type of dashboard
     */
    public Player(int id,boolean isDemo) {
        this.id = id;
        this.lap = 0;
        this.position = 0;
        this.isEliminated = false;
        this.discardPile = new ArrayList<Tile>();
        credit = 0;

        /**
         * inizialized the matrix
         */
        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
        /**
         * place the central unit
         */
        Dash_Matrix[3][2] = new CentralHousingUnit();
        /**
         * initialized a matrix with the valid position of the ship
         */
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

        }
        else{
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

    //metodi autoesplicativi
    public int getId() {
        return id;
    }

    public int getLap() {
        return lap;
    }

    public int getPos() {
        return position;
    }

    public boolean getStatus() {
        return isEliminated;
    }

    public void setStatus(boolean e) {
        isEliminated = e;
    }

    public void setPos(int pos) {
        position = pos;
    }

    public void setLap(int newLap) {
        lap = newLap;
    }

    public boolean isEliminated(){
        return isEliminated;
    }
    public void setEliminated() {
        isEliminated = true;
    }
    public void centralHousingUnit_isdestroyed() {
        isEliminated = true;
    }

    /**
     * control the placement of the tile in the matrix
     */
    public void controlAssembly(){
        int a,b;
        controlCannon();
        controlEngine();

        //controlla la colonna 0 della matrice
        for(int i=1;i<4;i++){
            boolean tmp = false;
            if(!((Dash_Matrix[i][0] instanceof EmptySpace))){
                a = Dash_Matrix[i][0].controlCorners(0);
                b = Dash_Matrix[i-1][1].controlCorners(2);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][0].controlCorners(2);
                    b = Dash_Matrix[i+1][0].controlCorners(0);
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
                    removeTile(i,0);
                }
            }
        }

        //controlla la colonna 6 della matrice
        for(int i=1;i<4;i++){
            boolean tmp = false;
            if(!((Dash_Matrix[i][6] instanceof EmptySpace))){
                a = Dash_Matrix[i][6].controlCorners(0);
                b = Dash_Matrix[i-1][6].controlCorners(2);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[i][6].controlCorners(2);
                    b = Dash_Matrix[i+1][6].controlCorners(0);
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
                    removeTile(i,6);
                }
            }
        }

        //controlla la riga 0 della matrice
        for(int i=1;i<6;i++){
            boolean tmp = false;
            if(!((Dash_Matrix[0][i] instanceof EmptySpace))){
                a = Dash_Matrix[0][i].controlCorners(3);
                b = Dash_Matrix[0][i-1].controlCorners(1);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[0][i].controlCorners(1);
                    b = Dash_Matrix[0][i+1].controlCorners(3);
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
                    removeTile(i,0);
                }
            }
        }

        //controlla la riga 4 della matrice
        for(int i=1;i<6;i++){
            boolean tmp = false;
            if(!((Dash_Matrix[4][i] instanceof EmptySpace))){
                a = Dash_Matrix[4][i].controlCorners(3);
                b = Dash_Matrix[4][i-1].controlCorners(1);
                if (a * b != 0) {
                    if ((a + b) / 2 != a) {
                        if (a != 3 && b != 3) {
                            tmp = true;
                        }
                    }
                } else if (!tmp) {
                    a = Dash_Matrix[4][i].controlCorners(1);
                    b = Dash_Matrix[4][i+1].controlCorners(3);
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
                    removeTile(i,0);
                }
            }
        }

        //controlla la matrice interna escludendo i lati
        for(int i = 1; i < 4; i++){
            for(int j = 1; j < 6; j++){
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
                } else if(!tmp) {
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

                if(tmp) removeTile(i,j);

            }
        }

        //controllo angolo in basso a sinistra
        if(!(Dash_Matrix[4][0] instanceof EmptySpace)) {
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
            if(tmp) {
                removeTile(4,0);
            }
        }

        //controllo angolo in basso a destra
        if(!(Dash_Matrix[4][6] instanceof EmptySpace)) {
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
            if(tmp) {
                removeTile(4,0);
            }
        }
    }
    /**
     * check if all the cannon ar display in the correct way
     */
    public void controlCannon(){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 7; j++){
                if(Dash_Matrix[i][j] instanceof Cannon || Dash_Matrix[i][j] instanceof DoubleCannon) {
                  for(int x=0; x < 4; x++){
                      if(Dash_Matrix[i][j].controlCorners(x) == 4 || Dash_Matrix[i][j].controlCorners(x) == 5) {
                          if(x==0){
                              for(int y = 0; y < i; y++){
                                  if(!(Dash_Matrix[y][j] instanceof EmptySpace)){
                                      removeTile(i,j);
                                  }
                              }
                          }
                          if(x==1){
                              for(int y = 6; y > j; y--){
                                  if(!(Dash_Matrix[y][j] instanceof EmptySpace)){
                                      removeTile(i,j);
                                  }
                              }
                          }
                          if(x==2){
                              for(int y = 4; y > i; y--){
                                  if(!(Dash_Matrix[y][j] instanceof EmptySpace)){
                                      removeTile(i,j);
                                  }
                              }
                          }
                          if(x==3){
                              for(int y = 0; y < j; y++){
                                  if(!(Dash_Matrix[i][j] instanceof EmptySpace)){
                                      removeTile(i,j);
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
    public void controlEngine(){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 7; j++){
                Tile tile = Dash_Matrix[i][j];
                if(tile instanceof Engine || tile instanceof DoubleEngine){
                    if(tile.controlCorners(2)!=6 || tile.controlCorners(2)!=7){
                        removeTile(i,j);
                    } else {
                        for(int x = 4; x > i; x--){
                            if(!(Dash_Matrix[x][j] instanceof EmptySpace)){
                                removeTile(i,j);
                            }
                        }
                    }
                }
            }
        }
    }

    //metodo che prende in ingrsso le cordinate di una tile selezionata da utente e chiama i metodi per usare
    //le batterie , ci dovrebbe essere una exception che mi permette di richiamare il metodo nel caso non sia un contenitore di esergie
    public boolean selectEnergyCell(int a, int b){
        if(Dash_Matrix[a][b] instanceof TripleEnergyCell){
            return ((TripleEnergyCell) Dash_Matrix[a][b]).energyManagment();
        } else if (Dash_Matrix[a][b] instanceof DoubleEnergyCell) {
            return ((DoubleEnergyCell) Dash_Matrix[a][b]).energyManagment();
        }else{
            return false;
        }
    }

    //metodo per controllare se ho un alieno viola
    public boolean controlPurpleAlien(){
        for(Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof PurpleAlienUnit && ((PurpleAlienUnit)tile).getStatus()) {
                    return true;
                }
            }
        }
        return false;
    }
    //metodo per controllare se ho un alieno viola
    public boolean controlBrownAlien(){
        for(Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof BrownAlienUnit && ((BrownAlienUnit)tile).getStatus()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * remove and sub. with empty space one tile
     * @param a raw of the matrix
     * @param b column of the matrix
     */
    public void removeTile(int a, int b){
        Tile tmp = Dash_Matrix[a][b];
        Dash_Matrix[a][b] = new EmptySpace();
        discardPile.add(tmp);
    }

    /**
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(int d){
        for(Tile[] row : Dash_Matrix){
            for(Tile tile : row){
                if(tile instanceof Shield){
                    if(((Shield) tile).getProtectedCorner(d)==8) {
                        Scanner scanner = new Scanner(System.in);
                        int x = scanner.nextInt();
                        int y = scanner.nextInt();
                        scanner.close();
                    return selectEnergyCell(x,y);
                    }
                 }
                }
            }
        return false;
    }

    //metodo controllo gestione delle batterie?
    //da capire come fare in modo che in energycell ci vadano le cordinate della matrice scelta
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
        if (controlPurpleAlien()) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    //aggiunta se presente un alieno
    public int getPowerEngine(){
        int tmp = 0;
        for(Tile[] row : Dash_Matrix){
            for(Tile tile : row){
                if(tile instanceof Engine){
                    tmp++;
                }else if(tile instanceof DoubleEngine){
                    Scanner scanner = new Scanner(System.in);
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.close();
                    boolean activate = selectEnergyCell(x,y);
                    tmp = tmp + ((DoubleEngine) tile).getPower(activate);
                }
            }
        }
        if (controlBrownAlien()) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }
    // i metodi prima chiamano selectEnergyCell,e se poi restituisce un true chiamano la activate dei doppio.
    //poi chiamano la getpowet e poi chiamano il turnof

    public int countExposedConnectors(){
        int tmp = 0;
        //controlla connettori esposti nella matrice interna
        for(int i=1; i<5; i++){
            for(int j=1; j<7; j++){

                int a,b;
                if(!(Dash_Matrix[i][j] instanceof EmptySpace)){
                    a = Dash_Matrix[i][j].controlCorners(0);
                    b = Dash_Matrix[i-1][j].controlCorners(2);
                    if((a<4 && a != 0) && (a+b==a)){
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(1);
                    b = Dash_Matrix[i][j+1].controlCorners(3);
                    if((a<4 && a != 0) && (a+b==a)){
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(2);
                    b = Dash_Matrix[i+1][j].controlCorners(0);
                    if((a<4 && a != 0) && (a+b==a)){
                        tmp++;
                    }
                    a = Dash_Matrix[i][j].controlCorners(3);
                    b = Dash_Matrix[i][j-1].controlCorners(1);
                    if((a<4 && a != 0) && (a+b==a)){
                        tmp++;
                    }
                }
            }
        }
        //controlla connettori esposti nella prima riga
        for(int i=1;i<6;i++){
            int a,b;
            if(!((Dash_Matrix[0][i] instanceof EmptySpace))){
                a = Dash_Matrix[0][i].controlCorners(3);
                b = Dash_Matrix[0][i-1].controlCorners(1);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(2);
                b = Dash_Matrix[1][i].controlCorners(0);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(1);
                b = Dash_Matrix[0][i+1].controlCorners(3);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[0][i].controlCorners(0);
                if((a<4 && a != 0)){
                    tmp++;
                }


            }
        }
        //controlla connettori esposti nell'ultima riga
        for(int i=1;i<6;i++){
            int a,b;
            if(!((Dash_Matrix[4][i] instanceof EmptySpace))){
                a = Dash_Matrix[4][i].controlCorners(3);
                b = Dash_Matrix[4][i-1].controlCorners(1);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(0);
                b = Dash_Matrix[3][i].controlCorners(2);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(1);
                b = Dash_Matrix[4][i+1].controlCorners(3);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[4][i].controlCorners(2);
                if((a<4 && a != 0)){
                    tmp++;
                }


            }
        }
        //controlla connettori esposti nella prima colonna
        for(int i=1;i<4;i++){
            int a,b;
            if(!((Dash_Matrix[i][0] instanceof EmptySpace))){
                a = Dash_Matrix[i][0].controlCorners(0);
                b = Dash_Matrix[i-1][0].controlCorners(2);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[i][0].controlCorners(1);
                b = Dash_Matrix[i][1].controlCorners(3);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[i][0].controlCorners(2);
                b = Dash_Matrix[i+1][0].controlCorners(0);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }

                a = Dash_Matrix[i][0].controlCorners(3);
                if(a<4 && a != 0){
                    tmp++;
                }



            }
        }
        //controlla connettori esposti ultima colonna
        for(int i=1;i<4;i++){
            int a,b;
            if(!((Dash_Matrix[i][6] instanceof EmptySpace))){
                a = Dash_Matrix[i][6].controlCorners(0);
                b = Dash_Matrix[i-1][6].controlCorners(2);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[i][6].controlCorners(3);
                b = Dash_Matrix[i][5].controlCorners(1);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }
                a = Dash_Matrix[i][6].controlCorners(2);
                b = Dash_Matrix[i+1][6].controlCorners(0);
                if((a<4 && a != 0) && (a+b==a)){
                    tmp++;
                }

                a = Dash_Matrix[i][6].controlCorners(1);
                if(a<4 && a != 0){
                    tmp++;
                }



            }
        }

        int a,b;
        //controllo le due pedine ai lati
        if(!((Dash_Matrix[4][0] instanceof EmptySpace))){
            a = Dash_Matrix[4][0].controlCorners(0);
            b = Dash_Matrix[3][0].controlCorners(2);
            if((a<4 && a != 0) && (a+b==a)){
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(1);
            b = Dash_Matrix[4][1].controlCorners(3);
            if((a<4 && a != 0) && (a+b==a)){
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(2);
            if(a<4 && a != 0){
                tmp++;
            }
            a = Dash_Matrix[4][0].controlCorners(3);
            if(a<4 && a != 0){
                tmp++;
            }


        }
        if(!((Dash_Matrix[4][6] instanceof EmptySpace))){
            a = Dash_Matrix[4][6].controlCorners(0);
            b = Dash_Matrix[3][6].controlCorners(2);
            if((a<4 && a != 0) && (a+b==a)){
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(3);
            b = Dash_Matrix[4][5].controlCorners(1);
            if((a<4 && a != 0) && (a+b==a)){
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(2);
            if(a<4 && a != 0){
                tmp++;
            }
            a = Dash_Matrix[4][6].controlCorners(1);
            if(a<4 && a != 0){
                tmp++;
            }


        }

        //restituisco il numero di connettori esposti
        return tmp;
    }

    //scelta player molto generale
    public boolean askPlayerDecision(){
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
    public int getCrewmates(){
        int tmp = 0;
        for(Tile[] row : Dash_Matrix) {
            for (Tile tile : row) {
                if (tile instanceof HousingUnit) {
                    tmp = tmp + ((HousingUnit)tile).ReturnLenght();
                } else if(tile instanceof BrownAlienUnit){
                    tmp = tmp + ((BrownAlienUnit)tile).ReturnLenght();
                } else if (tile instanceof PurpleAlienUnit) {
                    tmp = tmp + ((PurpleAlienUnit)tile).ReturnLenght();
                }
            }
        }
            return tmp;
    }

    //si possono solo togliere, a ogni rimozione diminuisco di 1 fino a quando non arrivo a 0
    public void removeCrewmates(int i){
        Scanner scanner = new Scanner(System.in);
        int x,y;

        while(i!=0){
            do{
                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.close();
            }while (!(Dash_Matrix[x][y] instanceof HousingUnit || Dash_Matrix[x][y] instanceof BrownAlienUnit || Dash_Matrix[x][y] instanceof PurpleAlienUnit));

            if(Dash_Matrix[x][y] instanceof HousingUnit){
                while(((HousingUnit)Dash_Matrix[x][y]).ReturnLenght()>0){
                    Humans tmp = new Humans();
                    ((HousingUnit)Dash_Matrix[x][y]).RemoveHumans(tmp);
                    i--;
                }
            }else if(Dash_Matrix[x][y] instanceof BrownAlienUnit){
                while(((BrownAlienUnit)Dash_Matrix[x][y]).ReturnLenght()>0){
                    Humans tmp = new Humans();
                    ((BrownAlienUnit)Dash_Matrix[x][y]).RemoveHumans(tmp);
                    i--;
                }
            }else if(Dash_Matrix[x][y] instanceof PurpleAlienUnit){
                while(((PurpleAlienUnit)Dash_Matrix[x][y]).ReturnLenght()>0) {
                    Humans tmp = new Humans();
                    ((PurpleAlienUnit) Dash_Matrix[x][y]).RemoveHumans(tmp);
                    i--;
                }
            }
        }

    }

    public int getCredit(){
        return credit;
    }

    public void addCredits(int credit){
        this.credit += credit;
    }

    public void removeCredits(int credit){
        this.credit -= credit;
    }

    public void addGoods(List<Good> good) throws FullGoodsList{
        Scanner scanner = new Scanner(System.in);
        int x,y,index;
        while(good.size()!=0){ //manca opzione di stop quando il player non vuole aggiungere più roba
            do {
                x = scanner.nextInt();
                y = scanner.nextInt();
                scanner.close();
            }while (!(Dash_Matrix[x][y] instanceof Storage));
            index = scanner.nextInt();
            scanner.close();
            Good tmp = good.get(index);
            try{
                ((Storage)Dash_Matrix[x][y]).AddGood(tmp);
            } catch (FullGoodsList | TooDangerous e){
                //parte di gestione della eccezione
            }
            good.remove(index);
        }
    }

    //mancono metodi per gestire se larrivo di un meteorite piccolo colpisce un connettore scoperto o no
    //manca quindi metodo per ritorare una tile da una posizione x
    //per gli altri metodi ci pensiamo insieme, bisogna capire meglio le interazioni con le carte
}