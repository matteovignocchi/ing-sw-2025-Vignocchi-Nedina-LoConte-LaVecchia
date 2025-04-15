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

    private List<Player> Players_in_Game = new ArrayList<>();
    public Pile pileOfTile = new Pile();
    public List<Tile> shownTile = new ArrayList<>();
    private FlightCardBoard f_board;
    private List<PlayerView> Players_views = new ArrayList<>();

     // da cambiare
    public Controller(boolean isDemo) {
        //if(isDemo) {
        //    f_board = new FlightCardBoard();
        //}else{
        //    f_board = new FlightCardBoard2();
        //}
    }

    public void addPlayer(int id, boolean isDemo) {
        Player p = new Player(id, isDemo);
        Players_in_Game.add(p);
        PlayerView p2 = new PlayerView(id);
        Players_views.add(p2);
    }

    public int checkNumberOfPlayers() {
        return Players_in_Game.size();
    }


    //switch (t) {
    //   case Cannone t  -> this.potenza+= t.potenzaDiFuoco();
    //  case CannoneDoppio  t -> this.potenza+= 2*t.potenzaDiFuoco();
    // default        -> ;

    // }

    //matrici da i=4 j=6
    // metodo che restituisce il numero di crewMate nella nave
    public int getNumCrew(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                switch (y) {
                    case HousingUnit c -> tmp = tmp + c.returnLenght();
                    default -> tmp = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * this method return the engine power, checking every tile
     * this method checks even if there is a double engine and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the brown alien, with the flag on the player and adds the bonus
     *
     * @return the total amount of engine power
     */
    public int getPowerEngine(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Tile y = p.getTile(i, j);
                Boolean var = false;
                switch (y) {
                    case Engine c -> {
                        var = c.isDouble();
                        if (var) {
                            boolean activate = manageEnergyCell(p);
                            if (activate) {
                                tmp = tmp + 2;
                            }
                        } else {
                            tmp = tmp + 1;
                        }
                    }
                    default -> tmp = tmp;
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
                Tile y = p.getTile(i, j);
                boolean var;
                switch (y) {
                    case Engine c -> {
                        var = c.isDouble();
                        if (var) {
                            boolean activate = manageEnergyCell(p);
                            if (activate) {
                                if (c.controlCorners(0) != 5) tmp = tmp + 1;
                                else tmp = tmp + 2;
                            }
                        } else {
                            if (c.controlCorners(0) != 4) tmp = tmp + 0.5;
                            else tmp = tmp + 1;
                        }
                    }
                    default -> tmp = tmp;
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
        return p.getTotalEnergy();
    }

    public int getTotalGood(Player p) {
        return p.getTotalGood();
    }

    public void removeGoods(Player p, int num) {
        int totalEnergy = getTotalEnergy(p);
        int totalGood = getTotalGood(p);
        PlayerView pview = getPlayerView(p.getId());
        List<Colour> TotalGood = p.getTotalListOfGood();
        int r = 0;
        int g = 0;
        int b = 0;
        int v = 0;
        for(Colour co : TotalGood) {
            switch (co) {
                case RED -> r++;
                case BLUE -> b++;
                case GREEN -> v++;
                case YELLOW -> g++;
            }
        }
        if(num == totalGood){
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++){
                  Tile y = p.getTile(i, j);
                  switch (y){
                      case StorageUnit c -> {
                          for(int i2=0 ; i2<c.getListSize() ;i2++) c.removeGood(i2);
                      }
                      default -> {}
                  }
                }
            }
        }
        if(num < totalGood){
            while(num != 0){
                if(r != 0){
                    pview.inform("selezionare cella ed eliminare rosso");
                    int[] vari = pview.askCoordinate();
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case StorageUnit c -> {
                            for(Colour co : c.getListOfGoods()) {
                                if (co == Colour.RED) {
                                    r--;
                                    num--;
                                    c.removeGood(c.getListOfGoods().indexOf(co));
                                }
                            }
                        }
                        default -> {}
                    }

                }
                if(r == 0 && num!=0 && g != 0){
                    pview.inform("selezionare cella ed eliminare giallo");
                    int[] vari = pview.askCoordinate();
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case StorageUnit c -> {
                            for(Colour co : c.getListOfGoods()) {
                                if (co == Colour.YELLOW) {
                                    g--;
                                    num--;
                                    c.removeGood(c.getListOfGoods().indexOf(co));
                                }
                            }
                        }
                        default -> {}
                    }

                }
                if(r == 0 && g == 0 && v != 0 && num!=0){
                    pview.inform("selezionare cella ed eliminare verde");
                    int[] vari = pview.askCoordinate();
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case StorageUnit c -> {
                            for(Colour co : c.getListOfGoods()) {
                                if (co == Colour.GREEN) {
                                    v--;
                                    num--;
                                    c.removeGood(c.getListOfGoods().indexOf(co));
                                }
                            }
                        }
                        default -> {}
                    }

                }
                if(r == 0 && g == 0 && v == 0 && b != 0 && num!=0){
                    pview.inform("selezionare cella ed eliminare blu");
                    int[] vari = pview.askCoordinate();
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case StorageUnit c -> {
                            for(Colour co : c.getListOfGoods()) {
                                if (co == Colour.BLUE) {
                                    b--;
                                    num--;
                                    c.removeGood(c.getListOfGoods().indexOf(co));
                                }
                            }
                        }
                        default -> {}
                    }

                }
            }
        }
        if(num > totalGood){
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++){
                    Tile y = p.getTile(i, j);
                    switch (y){
                        case StorageUnit c -> {
                            for(int i2=0 ; i2<c.getListSize() ;i2++) c.removeGood(i2);
                        }
                        default -> {}
                    }
                }
            }
            int finish = num-totalGood;
            if(finish < totalEnergy){
                while(finish > 0){
                    pview.inform("selezionare cella ed eliminare una batteria");
                    int[] vari = pview.askCoordinate();
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case EnergyCell c -> {
                            if(c.getCapacity() != 0) c.useBattery();
                            }
                        default -> {}

                    }
                 }

                }else{
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 7; j++){
                        Tile y = p.getTile(i, j);
                        switch (y){
                            case EnergyCell c -> {
                                for(int bb = 0 ; bb < c.getCapacity() ; i++) c.useBattery();
                            }
                            default -> {}
                        }
                    }
                }


            }
            }
        }

    public void addGoods(Player player, List<Colour> list) {
        boolean flag = true;
        while (list.size() != 0 && flag == true) {
            //select storage Unit
            //selecton indice lista che sto passando dentro
            //t.addGood
            // p.askPlayerDecision
            // se no diventa false
            // remove.(index)
        }
    }

    public void addHuman() {
        for (Player p : Players_in_Game) {
            //in tutte le abitazioni normali metto 2 human
            //in tutte le altre chiedo se vuole un alieno -> aggiorno flag quindi smette
            //se è connessa -> mettere umani
        }
    }

    public void removeCrewmate(Player player, int num) {
        int totalCrew = getNumCrew(player);
        if (num > totalCrew) {
            player.isEliminated();
        } else {
            while (num != 0) {
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
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = player.getTile(i, j);
                boolean var = false;
                switch (y) {
                    case HousingUnit c -> {
                        if (c.isConnected()) {
                            //il player seleziona l'indice
                            int x = c.removeHumans(1);
                            tmp++;
                            if (x == 2) player.setBrownAlien();
                            if (x == 3) player.setPurpleAlien();
                        }

                    }
                    default -> {
                    }
                }
            }
            if (tmp == firstNumber) {
                player.setEliminated();
            }
        }
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected and they want to use a battery
     *
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(Player p1, int d) {
        boolean flag = false;
        PlayerView x = getPlayerView(p1.getId());
        while (!flag) {
            if (x.ask("vuoi usare uno scudo?")) {
                int[] coordinate = x.askCoordinate();
                Tile y = p1.getTile(coordinate[0], coordinate[1]);
                switch (y) {
                    case Shield shield -> {
                        if (!(shield.getProtectedCorner(d) == 8)) {
                            x.inform("seleziona un'altro scudo");
                        } else {
                            return manageEnergyCell(p1);
                        }
                    }
                    default -> x.inform("cella non valida");

                }
            } else {
                flag = true;
            }
        }
        return false;
        //il controller chiede al player se vuole usare uno scudo
        //il player se vuole usare uno scudo fa partire unn ciclo in cui
        //deve selezionare una tile, se il controller tramite il visitor osserva che
        //è uno scudo,controlla che protegga il lato richiesto e passo al punto 2
        //2: fa in modo di uscire dal ciclo e chiedere al player se vuole quindi usare una batteria
        //se la vuole usare fa selezionare una energy cell
        //il controller a questo punto se osserva come prima che è una energy cell fa in modo che si possa eliminare una batteria
        //se si puo eliminare modifica il flag protection
        //altrimenti chiede unaltra energy cell
    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     *
     * @param dir  cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromCannon(int dir, boolean type, int dir2, Player p) {
        if (dir == 0) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(p, dir) && !type)) {
                    p.removeFrom0(dir2);
                }
            }
        } else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(p, dir) && !type)) {
                    p.removeFrom2(dir2);
                }
            }
        } else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    p.removeFrom1(dir2);
                }
            }
        } else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    p.removeFrom3(dir2);
                }
            }
        }

    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     *
     * @param dir  cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromMeteorite(int dir, boolean type, int dir2) {
        for (Player p : Players_in_Game) {
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && checkProtection(dir, dir2, p)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            p.removeFrom2(dir2);
                        }
                    }

                }
            } else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }

            } else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        p.removeFrom0(dir2);
                    }
                    if (!type && !p.checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            p.removeFrom2(dir2);
                        }
                    }
                }
            }
        }
    }

    public FlightCardBoard getFlightCardBoard() {
        return f_board;
    }

    public void activateCard(Card card){
        try{
            CardEffectVisitor visitor = new CardEffectVisitor(this);
            card.accept(visitor);
        } catch (CardEffectException e) {
            System.err.println("Error: " + e.getMessage());
            //poi si dovrebbe notificare il problema al player, ad esmepio con view.notifyPlayer
        }
    }

    public boolean askPlayerDecision(String condition, Player id) {
        PlayerView x = getPlayerView(id.getId());
        return x.ask(condition);
    }

    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(Player player) {

        PlayerView x = getPlayerView(player.getId());
        int[] coordinate = new int[2];
        boolean exits = false;

        //ricordarsi di mettere la catch per gestione null;
        boolean use = x.ask("Vuoi usare una batteria?");
        if (!use) {
            return false;
        } else {
            while (!exits) {
                coordinate = x.askCoordinate();
                Tile p = player.getTile(coordinate[0], coordinate[1]);
                switch (p) {
                    case EnergyCell c -> {
                        int capacity = c.getCapacity();
                        if (capacity == 0) {
                            if (!x.ask("Vuoi selezionare un'altra cella?")) {
                                return false;
                            }
                        } else {
                            c.useBattery();
                            return true;
                        }
                    }
                    default -> {
                        System.out.println("cella non valida");
                        if (!x.ask("vuoi selezionare un'altra cella?")) {
                            exits = true;
                        }
                    }
                }
            }

            return false;
        }
    }

    private boolean manageHousingUnit(Player player) {
        PlayerView x = getPlayerView(player.getId());
        x.inform("selezionare una HOusingUnit");
        int[] var = x.askCoordinate();
  return true;
    }

    private PlayerView getPlayerView(int id) {
        PlayerView x = null;
        for (PlayerView p : Players_views) {
            if (id == p.getId()) {
                x = p;
            }
        }
        return x;
    }

    //switch (t) {
    //   case Cannone t  -> this.potenza+= t.potenzaDiFuoco();
    //  case CannoneDoppio  t -> this.potenza+= 2*t.potenzaDiFuoco();
    // default        -> ;

    // }

    public boolean checkProtection(int dir, int dir2, Player player) {
        boolean result = false;
        if (dir == 0) {
            boolean flag = true;
            int i = 0;
            while (flag && i < 5) {
                if (player.validityCheck(i, dir2 - 4) == Status.USED) {
                    Tile y = player.getTile(i, dir2 - 4);
                    switch (y) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                return manageEnergyCell(player);
                            }
                        }
                        default -> {
                            return false;
                        }
                    }
                }
                i++;

            }
            return result;
        } else if (dir == 1) {
            boolean flag = true;
            int i = 5;
            while (flag && i >= 1) {
                if (player.validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = player.getTile(dir2 - 5, i);
                    Tile y2 = player.getTile(dir2 - 5, i + 1);
                    Tile y3 = player.getTile(dir2 - 5, i - 1);
                    switch (y1) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    switch (y2) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    switch (y3) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    flag = false;

                }
                i--;
            }
            return result;
        }else if (dir == 3) {
            boolean flag = true;
            int i = 1;
            while (flag && i<6) {
                if (player.validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = player.getTile(dir2 - 5, i);
                    Tile y2 = player.getTile(dir2 - 5, i + 1);
                    Tile y3 = player.getTile(dir2 - 5, i - 1);
                    switch (y1) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    switch (y2) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    switch (y3) {
                        case Cannon c -> {
                            if (!c.isDouble()) {
                                return true;
                            } else {
                                if (manageEnergyCell(player)) {
                                    return true;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    flag = false;

                }
                i++;
            }
            return result;

        }
        return false;
    }







}


