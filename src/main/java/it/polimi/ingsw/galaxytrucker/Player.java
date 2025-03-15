package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Player {
    //parte iniziale
    protected int id;

    //parte costruzione nave
    private boolean inReady;
    private boolean isComplete;
    private Tile[][] Dash_Matrix; //hashmap?
    private boolean[][] validPosition;
    private boolean purpleAlien;
    private boolean brownAlien;

    //discsrdPIle
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

        //controlla la colonna 4 della matrice
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
            if(!((Dash_Matrix[0][i] instanceof EmptySpace))){


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
     *
     * @param d the direction of a small meteorite
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

    public double getFirePower(){
        double tmp = 0;
        for(Tile[] row : Dash_Matrix){
            for(Tile tile : row){
                if(tile instanceof Cannon){
                    tmp= tmp + ((Cannon) tile).getPower();
                } else if (tile instanceof DoubleCannon) {
                    Scanner scanner = new Scanner(System.in);
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    scanner.close();
                    boolean boh = selectEnergyCell(x,y);
                    tmp = tmp + ((DoubleCannon) tile).getPower(boh);
                }
            }
        }
        return tmp;
    }

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
        return tmp;
    }
    // i metodi prima chiamano selectEnergyCell,e se poi restituisce un true chiamano la activate dei doppio.
    //poi chiamano la getpowet e poi chiamano il turnof

    //mancano metodi per contare i connettori scoperti
    //mancono metodi per gestire se larrivo di un meteorite piccolo colpisce un connettore scoperto o no
    //manca quindi metodo per ritorare una tile da una posizione x
    //per gli altri metodi ci pensiamo insieme, bisogna capire meglio le interazioni con le carte

}
