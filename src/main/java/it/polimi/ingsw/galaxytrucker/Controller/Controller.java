package it.polimi.ingsw.galaxytrucker.Controller;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.*;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard2;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.InvalidPlayerException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//TODO: capire gestione dei players in gioco (mappa, lista playersInGame, lista players in volo in flightcardboar)
//      un po tante liste ahahah
//TODO: gestire fase del game (?) per riconnessioni dei players.
//TODO: gestire e applicare i metodi che applicano gli effetti delle tiles (ex. addHuman per le celle)
// alla fine della fase di assemblaggio (sta parte rivederla)
//TODO: rivedere bene le inform e inserirle dove mancano
public class Controller implements Serializable {

    private List<Player> playersInGame = new ArrayList<>(); //giocatori effettivamente in gioco
    private transient Map<String, VirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> playersByNickname = new ConcurrentHashMap<>(); //in gioco + disconnessi
    private final AtomicInteger playerIdCounter;
    private final int MaxPlayers;
    private final boolean isDemo;

    private GameFase principalGameFase; //inutile

    private transient Hourglass hourglass;
    public List<Tile> pileOfTile;
    public List<Tile> shownTile = new ArrayList<>();
    private final FlightCardBoard fBoard;
    private Deck deck;
    private List<Deck> decks;
    private TileParserLoader pileMaker = new TileParserLoader();

    public Controller(boolean isDemo, int MaxPlayers) throws CardEffectException, IOException {
        if(isDemo) {
            fBoard = new FlightCardBoard();
            DeckManager deckCreator = new DeckManager();
            deck = deckCreator.CreateDemoDeck();
        }else{
            fBoard = new FlightCardBoard2();
            DeckManager deckCreator = new DeckManager();
            decks = deckCreator.CreateSecondLevelDeck();
        }
        this.hourglass = new Hourglass(this::onHourglassStateChange);
        this.isDemo = isDemo;
        this.MaxPlayers = MaxPlayers;
        this.playerIdCounter = new AtomicInteger(1); //verificare che matcha con la logica
        pileOfTile = pileMaker.loadTiles();
        Collections.shuffle(pileOfTile);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE PARTITA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: Capire discorso playersInGame vs PlayersByNick.values(). iterare su playersInGame, non sui valori della mappa
    //TODO: sostituire gli inform multipli con broadcastInform (oppure eliminarlo e dove è usato mettere serie di inform singoli) sia qui nel gamemanager



    public synchronized void updatePlayer(String nickname) throws RemoteException {
        VirtualView v = getViewByNickname(nickname);
        Player p = getPlayerByNickname(nickname);

        double firePower = getFirePower(nickname);
        int enginePower = getPowerEngine(nickname);
        int credits = p.getCredit();
        int position = fBoard.getPositionOfPlayer(p);
        boolean purpleAlien = p.presencePurpleAlien();
        boolean brownAlien = p.presenceBrownAlien();
        int humans = p.getTotalHuman();
        int energyCells = p.getTotalEnergy();

        v.updateGameState(p.getGameFase());
        v.showUpdate(nickname, firePower, enginePower, credits, position, purpleAlien, brownAlien, humans, energyCells);
    }



    public synchronized void addPlayer(String nickname, VirtualView view) throws BusinessLogicException, RemoteException {
        if (playersByNickname.containsKey(nickname)) throw new BusinessLogicException("Nickname already used");
        if (playersByNickname.size() >= MaxPlayers) throw new BusinessLogicException("Game is full");

        Player player = new Player(playerIdCounter.getAndIncrement(), isDemo);
        playersByNickname.put(nickname, player);
        viewsByNickname.put(nickname, view);
        playersInGame.add(player); //capire se va bene
        view.inform(String.format("Player %s added to game", nickname));
        if (playersByNickname.size() == MaxPlayers)
            startGame();
    }

    public Player getPlayerByNickname(String nickname) {
        return playersByNickname.get(nickname);
    }

    public VirtualView getViewByNickname(String nickname){
        return viewsByNickname.get(nickname);
    }

    public List<Player> getPlayersInGame(){
        return playersInGame;
    }

    public void broadcastInform(String msg) {
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            VirtualView v = viewsByNickname.get(nickname);
            try {
                v.inform(msg);
            } catch (IOException e) {
                markDisconnected(nickname); //il client non risponde: disconnesso (il metodo informa tutti)
            }
        }
    }

    public int countConnectedPlayers() {
        return (int) playersByNickname.values().stream().filter(Player::isConnected).count();
    }

    public GameFase getPrincipalGameFase() {
        return principalGameFase;
    }

    public synchronized void markDisconnected(String nickname) {
        Player p = playersByNickname.get(nickname);
        if (p != null && playersInGame.remove(p)) {
            broadcastInform(nickname + " is disconnected");
        }
    }

    public synchronized void markReconnected(String nickname, VirtualView view) throws BusinessLogicException, RemoteException {
        viewsByNickname.put(nickname, view); //Aaggiorno la view nella mappa
        Player p = playersByNickname.get(nickname);
        if (p != null && !playersInGame.contains(p)) {
            playersInGame.add(p);
            broadcastInform(nickname + "is riconnected");
            updatePlayer(nickname);
        }
    }

    public void reinitializeAfterLoad(Consumer<Hourglass> hourglassListener) {
        this.viewsByNickname = new ConcurrentHashMap<>();
        this.hourglass       = new Hourglass(hourglassListener);
    }

    public String getNickname(Player player) throws BusinessLogicException {
        return playersByNickname.entrySet().stream()
                .filter(e -> e.getValue().equals(player))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Player not found"));
    }

    //essendoci già condizione su if non penso servi
    public int checkNumberOfPlayers() {
        return playersByNickname.size();
    }

    public int getMaxPlayers(){ return MaxPlayers; }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 1
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void startGame() {
        playersInGame.forEach(p -> p.setGameFase(GameFase.BOARD_SETUP));
        broadcastInform("Game is starting! Place your tiles on the board.");
        startHourglass();
    }

    public void setPlayerReady(Player p){
        getFlightCardBoard().setPlayerReadyToFly(p, isDemo);
    }

    public void startFlight() throws RemoteException {
        if(!isDemo) mergeDecks();

        broadcastInform("Flight started!");
        playersInGame.forEach(p -> p.setGameFase(GameFase.WAITING_FOR_TURN));
        viewsByNickname.forEach((s, v) -> {
            try {
                v.updateGameState(GameFase.WAITING_FOR_TURN);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        Player firstPlayer = fBoard.getOrderedPlayers().getFirst();
        String firstPlayerNick = playersByNickname.entrySet().stream()
                        .filter(e -> e.getValue().equals(firstPlayer))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Impossible to find first player nickname"));
        firstPlayer.setGameFase(GameFase.DRAW_PHASE);
        VirtualView v = viewsByNickname.get(firstPlayerNick);
        v.inform("You're the leader! Draw a card");
        v.updateGameState(GameFase.DRAW_PHASE);
    }

    public  synchronized void startHourglass(){
        hourglass.flip();
        broadcastInform("Hourglass started!");
    }

    public void flipHourglass (String nickname) throws RemoteException, BusinessLogicException {
        Player p = getPlayerByNickname(nickname);
        int flips = hourglass.getFlips();
        HourglassState state = hourglass.getState();

        switch(flips){
            case 1:
                if(state == HourglassState.EXPIRED){
                    hourglass.flip();
                    broadcastInform("Hourglass flipped a second time!");
                } else {
                    getViewByNickname(nickname).inform("You cannot flip the hourglass: It's still running");
                }
                break;
            case 2:
                if(state == HourglassState.EXPIRED){
                    getViewByNickname(nickname).inform("You cannot flip the hourglass: It's still running");
                } else if (p.getGameFase() == GameFase.WAITING_FOR_PLAYERS) {
                    hourglass.flip();
                    broadcastInform("Hourglass flipped the last time!");
                } else {
                    getViewByNickname(nickname).inform("You cannot flip the hourglass for the last time: " +
                            "You are not ready");
                }
                break;
            default: throw new BusinessLogicException("Impossible to flip the hourglass another time!");
        }
    }

    public void onHourglassStateChange(Hourglass h){
        int flips = h.getFlips();

        switch (flips) {
            case 1:
                broadcastInform("First Hourglass expired");
                break;
            case 2:
                broadcastInform("Second Hourglass expired");
                break;
            case 3:
                broadcastInform("Time’s up! Building phase ended.");
                try {
                    startFlight();
                } catch (RemoteException e) {
                    //TODO:gabri riempi il catch qui, non so cosa deve catturare
                    // questo try-catch sistema quel problema che dava all'inizio nel costruttore
                }
                break;
        }
    }

    //TODO: da finire
    public void startAwardsPhase(){
        int malusBrokenTile = fBoard.getBrokenMalus();
        int bonusBestShip = fBoard.getBonusBestShip();
        int redGoodBonus = fBoard.getBonusRedCargo();
        int yellowGoodBonus = fBoard.getBonusYellowCargo();
        int greenGoodBonus = fBoard.getBonusGreenCargo();
        int blueGoodBonus = fBoard.getBonusBlueCargo();
        int[] arrivalBonus = {fBoard.getBonusFirstPosition(), fBoard.getBonusSecondPosition(),
                fBoard.getBonusThirdPosition(), fBoard.getBonusFourthPosition()};
        List<Player> orderedPlayers = fBoard.getOrderedPlayers();

        int minExpConnectors = playersInGame.stream()
                .mapToInt(Player::countExposedConnectors)
                .min()
                .orElseThrow( () -> new IllegalArgumentException("No Player in Game"));
        List<Player> bestShipPlayers = playersInGame.stream()
                .filter(p -> p.countExposedConnectors() == minExpConnectors)
                .toList();

        for (int i = 0; i < orderedPlayers.size(); i++) {
            orderedPlayers.get(i).addCredits(arrivalBonus[i]);
        }

        for (Player p : bestShipPlayers) {
            p.addCredits(bonusBestShip);
        }

        for (Player p : playersInGame) {
            List<Colour> goods = p.getTotalListOfGood();
            for(Colour c : goods){
                switch(c){
                    case Colour.RED:
                        p.addCredits(redGoodBonus);
                        break;
                    case Colour.YELLOW:
                        p.addCredits(yellowGoodBonus);
                        break;
                    case Colour.GREEN:
                        p.addCredits(greenGoodBonus);
                        break;
                    case Colour.BLUE:
                        p.addCredits(blueGoodBonus);
                        break;
                }
            }

            int numBrokenTiles = p.checkDiscardPile();
            p.addCredits(numBrokenTiles * malusBrokenTile);

            //TODO: trovare la view associata al player e informarla del numero x di crediti fatti:
            // se x>0, vinto, se no perso (farlo qui o in un for separato?)
        }
        //TODO: mettere tutti in fase di exit e proseguire con la fine del gioco
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 2
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean askPlayerDecision(String condition, Player id) {
        VirtualView x = viewsByNickname.get(id);
        return x.ask(condition);
    }

    public void addToShownTile(Tile tile) {
        shownTile.add(tile);
    }

    public Tile getShownTile(int index) {
        return shownTile.remove(index);
    }


    public List<Tile> getPileOfTile() {
        return pileOfTile;
    }

    public List<Tile> getShownTiles(){
        return shownTile;
    }

    public Tile getTile(int index) {
        return pileOfTile.remove(index);
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
    public int getPowerEngine(String p) {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Tile y = playersByNickname.get(p).getTile(i, j);
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
        if (playersByNickname.get(p).presenceBrownAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    public double getFirePower(String p){
        double tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = playersByNickname.get(p).getTile(i, j);
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
        if (playersByNickname.get(p).presencePurpleAlien() && tmp != 0) {
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

    public void removeGoods(String p, int num)  {
        int totalEnergy = getTotalEnergy(playersByNickname.get(p));
        int totalGood = getTotalGood(playersByNickname.get(p));

        List<Colour> TotalGood = playersByNickname.get(p).getTotalListOfGood();
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
                    Tile y = playersByNickname.get(p).getTile(i, j);
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
                    viewsByNickname.get(p).inform("selezionare cella ed eliminare rosso");
                    int[] vari = viewsByNickname.get(p).askCoordinate();
                    Tile y = playersByNickname.get(p).getTile(vari[0], vari[1]);
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
                    viewsByNickname.get(p).inform("selezionare cella ed eliminare giallo");
                    int[] vari = viewsByNickname.get(p).askCoordinate();
                    Tile y = playersByNickname.get(p).getTile(vari[0], vari[1]);
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
                    viewsByNickname.get(p).inform("selezionare cella ed eliminare verde");
                    int[] vari = viewsByNickname.get(p).askCoordinate();
                    Tile y = playersByNickname.get(p).getTile(vari[0], vari[1]);
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
                    viewsByNickname.get(p).inform("selezionare cella ed eliminare blu");
                    int[] vari = viewsByNickname.get(p).askCoordinate();
                    Tile y = playersByNickname.get(p).getTile(vari[0], vari[1]);
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
                    Tile y = playersByNickname.get(p).getTile(i, j);
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
                    viewsByNickname.get(p).inform("selezionare cella ed eliminare una batteria");
                    int[] vari = viewsByNickname.get(p).askCoordinate();
                    Tile y = playersByNickname.get(p).getTile(vari[0], vari[1]);
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
                        Tile y = playersByNickname.get(p).getTile(i, j);
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

    public void addGoods(String player, List<Colour> list) throws RemoteException {
        boolean flag = true;
        VirtualView x = viewsByNickname.get(player);
        if(!x.ask("vuoi aggiungere un goods?")) flag=false;
        while (list.size() != 0 && flag == true) {
            x.inform("seleziona una HOusing unit");
            int[] vari = x.askCoordinate();
            Tile t = playersByNickname.get(player).getTile(vari[0], vari[1]);
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
        for (Player p : playersInGame) {
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
        int totalCrew = getNumCrew(playersByNickname.get(player));
        VirtualView x = viewsByNickname.get(player);
        if (num >= totalCrew) {
            playersByNickname.get(player).isEliminated();
        } else {
            while (num > 0) {
                x.inform("seleziona un HOusing unit");
                int[] vari = x.askCoordinate();
                Tile y = playersByNickname.get(player).getTile(vari[0], vari[1]);
                switch (y){
                    case HousingUnit h -> {
                        if(h.returnLenght()>0){
                            int tmp = h.removeHumans(1);
                            if(tmp == 2) playersByNickname.get(player).setBrownAlien();
                            if(tmp == 3) playersByNickname.get(player).setPurpleAlien();
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
        int firstNumber = getNumCrew(playersByNickname.get(player));
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = playersByNickname.get(player).getTile(i, j);
                switch (y) {
                    case HousingUnit c -> {
                        if (c.isConnected()) {
                            viewsByNickname.get(player).askIndex();
                            int x = c.removeHumans(1);
                            tmp++;
                            if (x == 2) playersByNickname.get(player).setBrownAlien();
                            if (x == 3) playersByNickname.get(player).setPurpleAlien();
                        }

                    }
                    default -> {
                    }
                }
            }
            if (tmp == firstNumber) {
                playersByNickname.get(player).setEliminated();
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
        VirtualView x = viewsByNickname.get(p1);
        while (!flag) {
            if (x.ask("vuoi usare uno scudo?")) {
                int[] coordinate = x.askCoordinate();
                Tile y = playersByNickname.get(p1).getTile(coordinate[0], coordinate[1]);
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
                    playersByNickname.get(p).removeFrom0(dir2);
                }
            }
        } else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(p, dir) && !type)) {
                    playersByNickname.get(p).removeFrom2(dir2);
                }
            }
        } else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    playersByNickname.get(p).removeFrom1(dir2);
                }
            }
        } else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(p, dir) && !type)) {
                    playersByNickname.get(p).removeFrom3(dir2);
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
        for (String p : playersByNickname.keySet()) {
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                        }
                    }

                }
            } else if (dir == 1) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }

            } else if (dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                    }
                    if (!type && !playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                        }
                    }
                }
            }
        }
    }

    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(String player)  {

        VirtualView x = viewsByNickname.get(player);
        int[] coordinate = new int[2];
        boolean exits = false;

        //ricordarsi di mettere la catch per gestione null;
        boolean use = x.ask("Vuoi usare una batteria?");
        if (!use) {
            return false;
        } else {
            while (!exits) {
                coordinate = x.askCoordinate();
                Tile p = playersByNickname.get(player).getTile(coordinate[0], coordinate[1]);
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
                if (playersByNickname.get(player).validityCheck(i, dir2 - 4) == Status.USED) {
                    Tile y = playersByNickname.get(player).getTile(i, dir2 - 4);
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
                if (playersByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = playersByNickname.get(player).getTile(dir2 - 5, i);
                    Tile y2 = playersByNickname.get(player).getTile(dir2 - 5, i + 1);
                    Tile y3 = playersByNickname.get(player).getTile(dir2 - 5, i - 1);
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
                if (playersByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
                    Tile y1 = playersByNickname.get(player).getTile(dir2 - 5, i);
                    Tile y2 = playersByNickname.get(player).getTile(dir2 - 5, i + 1);
                    Tile y3 = playersByNickname.get(player).getTile(dir2 - 5, i - 1);
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


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public FlightCardBoard getFlightCardBoard() {
        return fBoard;
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
            fBoard.eliminateOverlappedPlayers();
            fBoard.orderPlayersInFlightList();
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

    public List<Card> showDeck (int idxDeck){
        return new ArrayList<>(decks.get(idxDeck).getCards());
    }

    /*metodo per pescare una carta e attivarla:
    1. pesco con il metodo draw (che rimuove dal deck) (metodo a parte view, la riceve e chiama activate card)
    2. chiamo activate card (gestione chiedere agli utenti, come?)
    3. check se deck vuoto (attivata ultima carta):
     si, si passa alla fase di premizione (cambio fase, inform e update (?))
     no, rimodifico le fasi per una nuova drawcard
     */

}


