package it.polimi.ingsw.galaxytrucker;


import it.polimi.ingsw.galaxytrucker.Card.*;
import it.polimi.ingsw.galaxytrucker.Tile.*;
import kotlin.Unit;
import org.w3c.dom.html.HTMLParagraphElement;

import java.util.ArrayList;
import java.util.List;//support for changes method in player
import java.util.Scanner;
//List<Player> players



//method for select the energy cell
public class Controller {

    private List<Player> Players_in_Game;
    private Visitor visitor = new Visitor();
    public Pile pileOfTile = new Pile();
    private FlightCardBoard f_board;


    public Controller(List<Player> Players_in_Game, FlightCardBoard f) {
        this.Players_in_Game = Players_in_Game;
        this.f_board = f;
    }

    public void usage(Cannon cannon) {
    }

    public void usage(Engine engine) {

    }

    public void usage(StorageUnit unit) {

    }

    public void usage(EnergyCell energyCell) {

    }

    public void usage(EmptySpace emptySpace) {

    }

    public void usage(Shield shield) {
    }

    public void usage(HousingUnit housingUnit) {

    }

    public void usage(MultiJoint joint) {

    }

    //matrici da i=4 j=6
    // metodo che restituisce il numero di crewMate nella nave
    public int getNumCrew(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                if(visitor.visit(p.getTile(i,j))>=100 && visitor.visit(p.getTile(i,j))<200)
                    tmp = tmp + visitor.visit(p.getTile(i, j)) - 100;
            }
        }
        return tmp;
    }


    /**
     * this method return the engine power, checking every tile
     * this method checks even if there is a double engine and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the brown alien, with the flag on the player and adds the bonus
     * @return the total amount of engine power
     */
    public int getPowerEngine(Player p){
        int tmp = 0;
        for(int i =0; i<5; i++){
            for(int j=0; j<5; j++){
                int type = visitor.visit(p.getTile(i, j));
                if(type == 11){
                    tmp = tmp +1;
                }else if(type == 22){
                    tmp = tmp+ 2;
                }

            }
        }
        if (p.presenceBrownAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    public double getFirePower(Player p) {
        double tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int type = visitor.visit(p.getTile(i, j));
                if (type == 1) {
                    if (p.getTile(i, j).controlCorners(0) == 4) {
                        tmp = tmp + 1;
                    } else {
                        tmp = tmp + 0.5;
                    }
                } else if (type == 2) {
                    //MANCA LOGICA DI RICHIESTA E RISPOSTA
                    if (p.getTile(i, j).controlCorners(0) == 5) {
                        tmp = tmp + 2;
                    } else {
                        tmp = tmp + 1;
                    }
                }
            }
        }
        if (p.presencePurpleAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    public int getTotalEnergy(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int type = visitor.visit(p.getTile(i, j));
                if (type >= 300) tmp = type - 300;
            }
        }
        return tmp;
    }

    public int getTotalGood(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                int type = visitor.visit(p.getTile(i, j));
                if (type >= 200 && type < 300) tmp = type - 200;
            }
        }
        return tmp;
    }


    public void removeGoods(Player player, int num) {
        int totalEnergy = getTotalEnergy(player);
        int totalGood = getTotalGood(player);
        if(num > totalGood) {
            int tmp1 = totalGood;
            while (tmp1!=0) {
                //select StorageUnit t = p.selectStorageUnit
                //se contiene almeno 1 merce
                //dentro un if
                //t.removeGood
                //tmp2--;
            }
            if( num - totalGood > totalEnergy ) {
                int tmp2 = totalEnergy;
                while (tmp2!=0) {
                    //select EnergyCell t = p.selectEnergyCell
                    //se contiene almeno 1 merce
                    //dentro un if
                    //t.removeGood
                    //tmp2--;
                }
            }else{
                int tmp3 = num-totalGood;
                while (tmp3!=0) {
                    //select StorageUnit t = p.selectStorageUnit
                    //se contiene almeno 1 merce
                    //dentro un if
                    //t.removeGood
                    //tmp4--;
                }
            }
        }else{
            while (num!=0) {
                //select StorageUnit t = p.selectStorageUnit
                //se contiene almeno 1 merce
                //dentro un if
                //t.removeGood
                //tmp2--;
            }
        }
    }

    public void addGoods(Player player, List<Colour> list) {
        boolean flag = true;
        while(list.size()!=0 && flag == true) {
            //select storage Unit
            //selecton indice lista che sto passando dentro
            //t.addGood
            // p.askPlayerDecision
            // se no diventa false
            // remove.(index)
        }
    }

    public void addHuman(){
        for( Player p : Players_in_Game ) {
            //in tutte le abitazioni normali metto 2 human
            //in tutte le altre chiedo se vuole un alieno -> aggiorno flag quindi smette
            //se è connessa -> mettere umani
        }
    }

    public void removeCrewmate(Player player, int num) {
        int totalCrew = getNumCrew(player);
        if(num > totalCrew) {
            player.isEliminated();
        }else{
            while (num!=0) {
                //select HousinUnit t = p.selectHousingUnit
                //se contiene almeno 1 persona
                //dentro un if
                //t.removeHuman
                //tmp = t.removeHuman
                //in base al valore di tmp, 1 non faccio nulla, 2 setto brown alien, 3 purple
                //num--;
            }
        }
    }

    public void startPlauge(Player player) {
        int firstNumber = getNumCrew(player);
        int tmp = 0;
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                if(visitor.visit(player.getTile(i,j)) > 100 && visitor.visit(player.getTile(i,j)) < 200 ) {
                   HousingUnit unit = (HousingUnit) player.getTile(i,j);
                   if(unit.isConnected()){
                       //il player seleziona l'indice
                       int x = unit.removeHumans(1);
                       tmp ++;
                       if( x == 2 ) player.setBrownAlien();
                       if(x == 3) player.setPurpleAlien();
                   }
                }
            }
        }
        if(tmp == firstNumber){
            player.setEliminated();
        }
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected and they want to use a battery
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(Player p1, int d) {
        boolean protection = false;
        //il controller chiede al player se vuole usare uno scudo
        //il player se vuole usare uno scudo fa partire unn ciclo in cui
        //deve selezionare una tile, se il controller tramite il visitor osserva che
        //è uno scudo,controlla che protegga il lato richiesto e passo al punto 2
        //2: fa in modo di uscire dal ciclo e chiedere al player se vuole quindi usare una batteria
        //se la vuole usare fa selezionare una energy cell
        //il controller a questo punto se osserva come prima che è una energy cell fa in modo che si possa eliminare una batteria
        //se si puo eliminare modifica il flag protection
        //altrimenti chiede unaltra energy cell
        Shield s = new Shield(1,2,3,4);
        boolean x = p1.dashProtected(s,d);
        if(x==true){
            protection = true;
        }
        //logica sopra per controllo dell tile
        return protection;
    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     * @param dir cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromCannon (int dir, boolean type ,int dir2) {
        for (Player p : Players_in_Game) {
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type || (!isProtected(p,dir) && !type)) {
                        p.removeFrom0(dir2);
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type || (!isProtected(p,dir) && !type)) {
                        p.removeFrom2(dir2);
                    }
                }
            } else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type || (!isProtected(p,dir) && !type)) {
                        p.removeFrom1(dir2);
                    }
                }
            } else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type || (!isProtected(p,dir) && !type)) {
                            p.removeFrom3(dir2);
                    }
                }
            }
        }
    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     * @param dir cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromMeteorite( int dir, boolean type ,int dir2){
        for(Player p : Players_in_Game) {
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !p.checkProtection(dir, dir2)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p,dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !p.checkProtection(dir, dir2)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p,dir)) {
                            p.removeFrom2(dir2);
                        }
                    }

                }
            } else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !p.checkProtection(dir, dir2)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p,dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }

            } else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !p.checkProtection(dir, dir2)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p,dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }
            }
        }
    }





    ///////////////////////////////////////////////////////////////////

    public FlightCardBoard getFlightCardBoard(){ return f_board;}

    public void activateCard(Card card){
        CardEffectVisitor visitor = new CardEffectVisitor(this);
        card.accept(visitor);
    }



}
