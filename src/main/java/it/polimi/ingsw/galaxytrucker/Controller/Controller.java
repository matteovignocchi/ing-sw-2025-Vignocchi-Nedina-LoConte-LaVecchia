package it.polimi.ingsw.galaxytrucker.Controller;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard2;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.InvalidPlayerException;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Model.TileParserLoader;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Controller implements Serializable {

    private List<Player> players_in_Game = new ArrayList<>(); //forse può essere eliminato
    private final transient Map<String, VirtualView> ViewByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> PlayerByNickname = new ConcurrentHashMap<>();
    private final AtomicInteger player_id_counter;
    private final int Max_Players;
    private GameFase principalGameFase; //iniazilizzato a fase 0
    private GameFase preGameFase;
    private final boolean isDemo;

    public List<Tile> pileOfTile;
    public List<Tile> shownTile = new ArrayList<>();
    private final FlightCardBoard f_board;
    private Deck deck;
    private List<Deck> decks;
    private TileParserLoader pileMaker = new TileParserLoader();

    public Controller(boolean isDemo, int Max_Players) throws CardEffectException, IOException {
        if(isDemo) {
            f_board = new FlightCardBoard();
            DeckManager deckCreator = new DeckManager();
            deck = deckCreator.CreateDemoDeck();
        }else{
            f_board = new FlightCardBoard2();
            DeckManager deckCreator = new DeckManager();
            decks = deckCreator.CreateSecondLevelDeck();
        }
        this.isDemo = isDemo;
        this.Max_Players = Max_Players;
        this.player_id_counter = new AtomicInteger(1); //verificare che matcha con la logica
        pileOfTile = pileMaker.loadTiles();
        Collections.shuffle(pileOfTile);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                                    //GESTIONE PARTITA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //faccaimo una v.inform("player aggiunto")????
    public synchronized void addPlayer(String nickname, VirtualView view, boolean isDemo) throws BusinessLogicException {
        if (PlayerByNickname.containsKey(nickname)) throw new BusinessLogicException("Nickname already used");
        if (PlayerByNickname.size() >= Max_Players) throw new BusinessLogicException("Game is full");

        Player player = new Player(player_id_counter.getAndIncrement(), isDemo);
        PlayerByNickname.put(nickname, player);
        ViewByNickname.put(nickname, view);
        if (PlayerByNickname.size() == Max_Players)
            startGame();
    }

    //il motivo per cui inserisco un try catch è legato alla robustezza del codice:

    //Devo gestire l'eccezione (Exception) a livello di Controller?
    //
    //In teoria no! Il Controller non dovrebbe occuparsi di gestire problemi di rete o errori di comunicazione.
    //Il Controller gestisce solo la logica di gioco, non l'infrastruttura di comunicazione.
    //
    //Se c'è un problema di comunicazione (RemoteException, Exception dalla VirtualView),
    //dovrebbe essere gestito nei livelli superiori, tipo ServerRMI o il GameManager.
    //
    //Quindi hai ragione.
    //Nel Controller noi possiamo:
    //
    //propagare l'eccezione (throws Exception)
    //
    //oppure lanciarla come unchecked (RuntimeException) se proprio vogliamo essere estremi.
    //
    //MA visto che i tuoi metodi tipo addPlayer e startGame sono private e interni,
    //è molto meglio avvolgere tutto nel try-catch per sicurezza (così eviti di far esplodere tutto se solo un client ha problemi).
    //E loggare l'errore senza mandare tutto in crash.
    //
    //Quindi: la gestione con try-catch nel Controller è accettabile solo per robustezza,
    //NON perché "è sua responsabilità" gestire i problemi di rete.

    private void startGame() {
        principalGameFase = GameFase.BOARD_SETUP; //capire se serve fase nel controller
        PlayerByNickname.values().forEach(p -> p.setGameFase(GameFase.BOARD_SETUP));
        ViewByNickname.forEach( (nickname, v) -> {
            try{
                Player player = PlayerByNickname.get(nickname);

                double fire_power = getFirePower(nickname);
                int power_engine = getPowerEngine(nickname);
                int credits = player.getCredit();
                int position = f_board.getPositionOfPlayer(player);//implementato in flight... vai a vedere
                boolean hasPurpleAlien = player.presencePurpleAlien();
                boolean hasBrownAlien = player.presenceBrownAlien();
                int Human = player.countTotalCrew(); //deve implementare oleg, che cazzo ne so io
                int Energy = player.getTotalEnergy();


                v.inform("Game started. It's time to build your ship!"); //inutile logicamente, ma utile a far capire lo stato del gioco al client
                v.updateGameState(GameFase.BOARD_SETUP);
                v.showUpdate(nickname, fire_power, power_engine, credits, position, hasPurpleAlien, hasBrownAlien, Human, Energy);
            }catch (Exception e){
                System.err.println("Communication error with a client: " e.getMessage());
            }
        });
    }

    public Player getPlayerByNickname(String nickname) {
        return PlayerByNickname.get(nickname);
    }

    public void removePlayer(String nickname){
        PlayerByNickname.remove(nickname);
        ViewByNickname.remove(nickname);
    } //va rimosso anche dalla lista players_in_game??

    public void remapView(String nickname, VirtualView view){
        ViewByNickname.put(nickname, view);
    }

    public int countConnectedPlayers() {
        return (int) PlayerByNickname.values().stream().filter(Player::isConnected).count();
    }

    public GameFase getPrincipalGameFase() {
        return principalGameFase;
    }

    public void markDisconnected(String nickname) {
        Player player = PlayerByNickname.get(nickname);
        if (player != null) player.setConnected(false);
    }

    public void pauseGame() {
        principalGameFase = GameFase.WAITING_FOR_PLAYERS;
    }//non so se la fase è corretta, cioè il gioco deve continuare se crasha, a meno che non resta un solo player e attivo un timeout
    //Metodo da rivedere

    public void resumeGame() {
        principalGameFase = GameFase.DRAW_PHASE;
    }//metodo da rivedere

    //essendoci già condizione su if non penso servi
    public int checkNumberOfPlayers() {
        return PlayerByNickname.size();
    }

    public boolean controlPresenceOfPlayer(int id) {
        for (Player p : players_in_Game) {
            if (p.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public int getMax_Players(){ return Max_Players; }

    //da verificare se la fase è giusta
    public void startGameIfReady() {
        if (players_in_Game.size() == Max_Players) {
            principalGameFase = GameFase.TILE_MANAGEMENT;
            setGameFaseForEachPlayer(GameFase.TILE_MANAGEMENT);
            // TODO: notifica view se serve
            // TODO: capire bene come gestire le fasi
        }
    }

    //MEDOTI PER PRENDERE LE GAMEFASE
    public List<GameFase> getGameFasesForEachPlayer() {
        List<GameFase> gameFases = new ArrayList<>();
        for(Player x : players_in_Game ) {
            gameFases.add(x.getGameFase());
        }
        return gameFases;
    }

    public GameFase getGameFase(int id) {
        for(Player p : players_in_Game ) {
            if(id == p.getId()) {
                return p.getGameFase();
            }
        }
        return null;
    }

    public void setGameFaseForEachPlayer(GameFase gameFase) {
        for(Player p : players_in_Game ) {
            p.setGameFase(gameFase);
        }
    }
    public void setGameFase(GameFase gameFase, int id) {
        for(Player p : players_in_Game ) {
            if(id == p.getId()) {
                p.setGameFase(gameFase);
            }
        }
    }

    public void setNextPrincipalGameFase() {
        switch (principalGameFase) {
            case BOARD_SETUP -> {
                preGameFase = principalGameFase;
                principalGameFase = GameFase.WAITING_FOR_TURN;
            }
            case WAITING_FOR_TURN -> {
                preGameFase = principalGameFase;
                principalGameFase = GameFase.SCORING;
            }
            default -> principalGameFase = preGameFase;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                                //GESTIONE MODEL
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean askPlayerDecision(String condition, Player id) throws Exception {
        VirtualView x = ViewByNickname.get(id);
        return x.ask(condition);
    }

    public void addToShownTile(Tile tile) {
        shownTile.add(tile);
    }

    public Tile getShownTile(int index) {
        Tile tmp = shownTile.get(index);
        shownTile.remove(index);
        return tmp;

    }

    public List<Tile> getPileOfTile() {
        return pileOfTile;
    }

    public Tile getTile(int index) {
        Tile tmp = pileOfTile.get(index);
        pileOfTile.remove(index);
        return tmp;
    }

    // metodo che restituisce il numero di crewMate nella nave
    public int getNumCrew(Player p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                switch (y) {
                    case HousingUnit c -> tmp = tmp + c.returnLenght();
                    default -> {}
                }
            }
        }
        return tmp;
    }

    public boolean getIsDemo(){
        return isDemo;
    }

    /**
     * this method return the engine power, checking every tile
     * this method checks even if there is a double engine and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the brown alien, with the flag on the player and adds the bonus
     *
     * @return the total amount of engine power
     */
    public int getPowerEngine(String p) throws Exception {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Tile y = PlayerByNickname.get(p).getTile(i, j);
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
        if (PlayerByNickname.get(p).presenceBrownAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    public double getFirePower(String p) throws Exception {
        double tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = PlayerByNickname.get(p).getTile(i, j);
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
        if (PlayerByNickname.get(p).presencePurpleAlien() && tmp != 0) {
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

    public void removeGoods(String p, int num) throws Exception {
        int totalEnergy = getTotalEnergy(PlayerByNickname.get(p));
        int totalGood = getTotalGood(PlayerByNickname.get(p));

        List<Colour> TotalGood = PlayerByNickname.get(p).getTotalListOfGood();
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
                  Tile y = PlayerByNickname.get(p).getTile(i, j);
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
                    ViewByNickname.get(p).inform("selezionare cella ed eliminare rosso");
                    int[] vari = ViewByNickname.get(p).askCoordinate();
                    Tile y = PlayerByNickname.get(p).getTile(vari[0], vari[1]);
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
                    ViewByNickname.get(p).inform("selezionare cella ed eliminare giallo");
                    int[] vari = ViewByNickname.get(p).askCoordinate();
                    Tile y = PlayerByNickname.get(p).getTile(vari[0], vari[1]);
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
                    ViewByNickname.get(p).inform("selezionare cella ed eliminare verde");
                    int[] vari = ViewByNickname.get(p).askCoordinate();
                    Tile y = PlayerByNickname.get(p).getTile(vari[0], vari[1]);
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
                    ViewByNickname.get(p).inform("selezionare cella ed eliminare blu");
                    int[] vari = ViewByNickname.get(p).askCoordinate();
                    Tile y = PlayerByNickname.get(p).getTile(vari[0], vari[1]);
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
                    Tile y = PlayerByNickname.get(p).getTile(i, j);
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
                    ViewByNickname.get(p).inform("selezionare cella ed eliminare una batteria");
                    int[] vari = ViewByNickname.get(p).askCoordinate();
                    Tile y = PlayerByNickname.get(p).getTile(vari[0], vari[1]);
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
                        Tile y = PlayerByNickname.get(p).getTile(i, j);
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

    public void addGoods(String player, List<Colour> list) throws Exception {
        boolean flag = true;
        VirtualView x = ViewByNickname.get(player);
        if(!x.ask("vuoi aggiungere un goods?")) flag=false;
        while (list.size() != 0 && flag == true) {
            x.inform("seleziona una HOusing unit");
            int[] vari = x.askCoordinate();
            Tile t = PlayerByNickname.get(player).getTile(vari[0], vari[1]);
            switch (t){
                case StorageUnit c -> {
                    if(c.isFull()){
                        x.printListOfGoods(c.getListOfGoods());
                        x.ask("seleziona indice da rimuovere");
                        int tmpint = x.askIndex();
                        Colour tmp = c.getListOfGoods().get(tmpint - 1);
                        c.removeGood(tmpint-1);
                        list.add(tmp);
                    }
                    x.inform("seleziona la merce da inserire");
                    int tmpint = x.askIndex();
                    c.addGood(list.get(tmpint-1));
                }
                default -> {
                    x.inform("cella non valida");
                }


            }
            if(!x.ask("Vuoi continurare")) flag = false;

            //select storage Unit
            //selecton indice lista che sto passando dentro
            //t.addGood
            // p.askPlayerDecision
            // se no diventa false
            // remove.(index)
        }
    }

    public void addHuman() throws Exception {
        for (Player p : players_in_Game) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++) {
                    Tile t = p.getTile(i, j);
                    switch (t){
                        case HousingUnit h -> {
                            Human tmp = h.getType();
                            switch (tmp){
                                case HUMAN -> {
                                    Human tmp2 = Human.HUMAN;
                                    for(int z = 0; z<2 ; z++) h.addHuman(tmp2);}
                                case PURPLE_ALIEN -> {
                                    if(askPlayerDecision("alien",p)){
                                        Human tmp2 = Human.PURPLE_ALIEN;
                                        h.addHuman(tmp2);
                                    }else{
                                        Human tmp2 = Human.HUMAN;
                                        for(int z = 0 ; z<2 ; z++) h.addHuman(tmp2);
                                    }

                                }
                                case BROWN_ALIEN -> {
                                    if(askPlayerDecision("alien",p)){
                                        Human tmp2 = Human.BROWN_ALIEN;
                                        h.addHuman(tmp2);
                                    }else{
                                        Human tmp2 = Human.HUMAN;
                                        for(int z = 0 ; z<2 ; z++) h.addHuman(tmp2);
                                    }
                                }
                            }
                        }
                        default -> {}
                    }
                }
            }            //in tutte le abitazioni normali metto 2 human
            //in tutte le altre chiedo se vuole un alieno -> aggiorno flag quindi smette
            //se è connessa -> mettere umani
        }
    }

    public void removeCrewmate(String player, int num) throws Exception {
        int totalCrew = getNumCrew(PlayerByNickname.get(player));
        VirtualView x = ViewByNickname.get(player);
        if (num >= totalCrew) {
            PlayerByNickname.get(player).isEliminated();
        } else {
            while (num > 0) {
                x.inform("seleziona un HOusing unit");
                int[] vari = x.askCoordinate();
                Tile y = PlayerByNickname.get(player).getTile(vari[0], vari[1]);
                switch (y){
                    case HousingUnit h -> {
                        if(h.returnLenght()>0){
                            int tmp = h.removeHumans(1);
                            if(tmp == 2) PlayerByNickname.get(player).setBrownAlien();
                            if(tmp == 3) PlayerByNickname.get(player).setPurpleAlien();
                            num--;
                        }else{
                            x.inform("seleziona una housing unit valida");
                        }
                    }
                    default -> {
                        x.inform("seleziona una abitazione valida");
                    }
                }

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

    public void startPlauge(String player) throws Exception {
        int firstNumber = getNumCrew(PlayerByNickname.get(player));
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = PlayerByNickname.get(player).getTile(i, j);
                switch (y) {
                    case HousingUnit c -> {
                        if (c.isConnected()) {
                            ViewByNickname.get(player).askIndex();
                            int x = c.removeHumans(1);
                            tmp++;
                            if (x == 2) PlayerByNickname.get(player).setBrownAlien();
                            if (x == 3) PlayerByNickname.get(player).setPurpleAlien();
                        }

                    }
                    default -> {
                    }
                }
            }
            if (tmp == firstNumber) {
                PlayerByNickname.get(player).setEliminated();
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
    public boolean isProtected(String p1, int d) throws Exception {
        boolean flag = false;
        VirtualView x = ViewByNickname.get(p1);
        while (!flag) {
            if (x.ask("vuoi usare uno scudo?")) {
                int[] coordinate = x.askCoordinate();
                Tile y = PlayerByNickname.get(p1).getTile(coordinate[0], coordinate[1]);
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
    public void defenceFromCannon(int dir, boolean type, int dir2, String p) throws Exception {
        if (dir == 0) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(p, dir) && !type)) {
                    PlayerByNickname.get(p).removeFrom0(dir2);
                }
            }
        } else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(p, dir) && !type)) {
                    PlayerByNickname.get(p).removeFrom2(dir2);
                }
            }
        } else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    PlayerByNickname.get(p).removeFrom1(dir2);
                }
            }
        } else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    PlayerByNickname.get(p).removeFrom3(dir2);
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
    public void defenceFromMeteorite(int dir, boolean type, int dir2) throws Exception {
        for (String p : PlayerByNickname.keySet()) {
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        PlayerByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && PlayerByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            PlayerByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && checkProtection(dir, dir2, p)) {
                        PlayerByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !PlayerByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            PlayerByNickname.get(p).removeFrom2(dir2);
                        }
                    }

                }
            } else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        PlayerByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !PlayerByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            PlayerByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }

            } else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        PlayerByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !PlayerByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            PlayerByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }
            }
        }
    }

    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(String player) throws Exception {

        VirtualView x = ViewByNickname.get(player);
        int[] coordinate = new int[2];
        boolean exits = false;

        //ricordarsi di mettere la catch per gestione null;
        boolean use = x.ask("Vuoi usare una batteria?");
        if (!use) {
            return false;
        } else {
            while (!exits) {
                coordinate = x.askCoordinate();
                Tile p = PlayerByNickname.get(player).getTile(coordinate[0], coordinate[1]);
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

    public boolean checkProtection(int dir, int dir2, String player) throws Exception {
        boolean result = false;
        if (dir == 0) {
            boolean flag = true;
            int i = 0;
            while (flag && i < 5) {
                if (PlayerByNickname.get(player).validityCheck(i, dir2 - 4) == Status.USED) {
                    Tile y = PlayerByNickname.get(player).getTile(i, dir2 - 4);
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
                if (PlayerByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = PlayerByNickname.get(player).getTile(dir2 - 5, i);
                    Tile y2 = PlayerByNickname.get(player).getTile(dir2 - 5, i + 1);
                    Tile y3 = PlayerByNickname.get(player).getTile(dir2 - 5, i - 1);
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
                if (PlayerByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = PlayerByNickname.get(player).getTile(dir2 - 5, i);
                    Tile y2 = PlayerByNickname.get(player).getTile(dir2 - 5, i + 1);
                    Tile y3 = PlayerByNickname.get(player).getTile(dir2 - 5, i - 1);
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


    public FlightCardBoard getFlightCardBoard() {
        return f_board;
    }

    // TODO: capire se gestire le eccezioni col try catch qui o nel virtual client (al confine con la
    //  view)

    /**
     * The following method activates the effect of a card. Then, it eliminates any possible overlapped players
     * after the application of the effect, and reorders the list of players in order of lap and position
     * on the flight board.
     *
     * @param card card
     */
    public void activateCard(Card card){
        try{
            CardEffectVisitor visitor = new CardEffectVisitor(this);
            card.accept(visitor);
            f_board.eliminateOverlappedPlayers();
            f_board.orderPlayersInFlightList();
        } catch (CardEffectException e) {
            System.err.println("Error: " + e.getMessage());
            // TODO: poi si dovrebbe notificare il problema al player, ad esmepio con view.notifyPlayer
        } catch (InvalidPlayerException e){
            System.err.println("Error: " + e.getMessage());
            // TODO: notificare la view con view.showerror?
        }
    }

    /**
     * The following method merges all four small decks for lvl 2 flight into a single one.
     */
    public void mergeDecks (){
        try{
            for(Deck d : decks){
                deck.addAll(d.getCards());
            }
            deck.shuffle();
        } catch (RuntimeException e){
            System.err.println("Error during decks' merging: " + e.getMessage());
            //TODO: notificare la view
        }
    }


    //metodo per far vedere a schermo al player il deck (solo 3 dei 4)

    /*metodo per pescare una carta e attivarla:
    1. pesco con il metodo draw (che rimuove dal deck)
    2. chiamo activate card
    3. check se deck vuoto (attivata ultima carta) si passa alla fase di premizione
     */


}


