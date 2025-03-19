package it.polimi.ingsw.galaxytrucker;


import it.polimi.ingsw.galaxytrucker.Tile.*;
import kotlin.Unit;

import java.util.List;//support for changes method in player
import java.util.Scanner;
//List<Player> players



//method for select the energy cell
public class Controller {

    private List<Player> Players_in_Game;
    private Visitor visitor = new Visitor;
    public Pile pileOfTile = new Pile();


    public Controller(List<Player> Players_in_Game) {
        this.Players_in_Game = Players_in_Game;
    }

    public void usage(Cannon cannon){
    }

    public void usage(Engine engine){

    }

    public void usage(StorageUnit unit){

    }

    public void usage(EnergyCell energyCell){

    }

    public void usage(EmptySpace emptySpace){

    }

    public void usage(Shield shield){}

    public void usage(HousingUnit housingUnit){

    }

    public void usage(MultiJoint joint){

    }

    //matrici da i=4 j=6
    // metodo che restituisce il numero di crewMate nella nave
    public int getNumCrew(Player p){
        int tmp = 0;
        for(int i =0; i<5; i++){
            for(int j=0; j<5; j++){
                tmp = tmp + visitor.visit(p.getTile(i, j));

            }
        }
        return tmp;
    }

    public double getFirePower(Player p){
        double tmp = 0;
        for(int i =0; i<5; i++){
            for(int j=0; j<5; j++){
                int type = visitor.visit(p.getTile(i, j));
                if(type == 1){

                    if(p.getTile(i,j).controlCorners(0)==4){
                        tmp = tmp +1;
                    }else{
                        tmp = tmp + 0.5;
                    }
                }else{
                    //MANCA LOGICA DI RICHIESTA E RISPOSTA
                    if(p.getTile(i,j).controlCorners(0)==5){
                        tmp = tmp +2;
                    }else{
                        tmp = tmp + 1;
                    }
                }
            }
        }
        if (p.presencePurpleAlien()) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }


    public void removeGoods(Player player, int num) {
        int
    }

}
