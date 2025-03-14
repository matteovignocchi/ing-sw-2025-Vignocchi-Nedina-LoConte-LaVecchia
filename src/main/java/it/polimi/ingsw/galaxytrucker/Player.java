package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Tile.*;

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

        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
        Dash_Matrix[3][2] = new CentralHousingUnit();
        //inizializzo per demo o II secondo livello
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


    /**
     *
     * @return the total engine power of the player
     */
    // da capire perchè tile.getPower non va bene nonostante ho messo che tile deve essere istanza di DoubleEngine
    public int getPowerEngine(){
        int power = 0;
        for(Tile[] row : Dash_Matrix){
            for(Tile tile : row){
                if(tile instanceof Engine){
                    power++;
                } else if (tile instanceof DoubleEngine) {
                    DoubleEngine tmp = (DoubleEngine) tile;
                    power += tmp.getPower();

                }
            }
        }
        return power;
    }

    /**
     *
     * @return the total cannon power of the player
     */

    public int getCannonEngine(){
        int power = 0;
        for(Tile[] row : Dash_Matrix){
            for(Tile tile : row){
                if(tile instanceof Cannon){
                    power++;
                } else if (tile instanceof DoubleCannon) {
                    DoubleCannon tmp = (DoubleCannon) tile;
                    power += tmp.getPower();

                }
            }
        }
        return power;
    }


//metodi autoesplicativi
    public int getId() {
        return id;
    }

    public int getLap() {
        return lap;
    }

    public int getPosition() {
        return position;
    }

    public boolean getStatus() {
        return isEliminated;
    }

    public void setStatus(boolean e) {
        isEliminated = e;
    }

    public void setPosition(int pos) {
        position = pos;
    }

    public void setLap(int newLap) {
        lap = newLap;
    }



}
