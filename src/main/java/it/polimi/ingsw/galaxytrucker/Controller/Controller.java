package it.polimi.ingsw.galaxytrucker.Controller;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//corner case da discutere con Gabri: se un giocatore crasha nella fase di drawCard impostiamo delle risposte predefinite.
//ma se crasha nella fase di building della nave in una partita demo? Cioè qui non abbiamo una clessidra.
//Aspettiamo all'infinito che si riconnetta??????
//TODO: gestire fase del game (?) per riconnessioni dei players. (Oleg: ho un idea per questa cosa)
//TODO: gestire e applicare i metodi che applicano gli effetti delle tiles (ex. addHuman per le celle)
// alla fine della fase di assemblaggio (sta parte rivederla) (oleg: se volete questa cosa la facciamo insiem dato che vi avevamo già pensato io e teo)
//TODO: rivedere bene le inform e inserirle dove mancano (Oleg:gestire bene anche le try-catch)
//TODO: sistemare cardVisitor con le fasi e le chiamate ai metodi , verificare se meglio la mappa o il player (va gestita sta cosa gabri sa a cosa mi riferisco)

public class Controller implements Serializable {
    //private List<Player> playersInGame = new ArrayList<>();
    private final int gameId;
    private transient Map<String, VirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> playersByNickname = new ConcurrentHashMap<>();
    private final AtomicInteger playerIdCounter;
    private final int MaxPlayers;
    private final boolean isDemo;
    private final Consumer<Integer> onGameEnd;
    private final Map<String , Integer> playerPosition = new ConcurrentHashMap<>();
    private GamePhase principalGamePhase;


    private transient Hourglass hourglass;
    public List<Tile> pileOfTile;
    public List<Tile> shownTile = new ArrayList<>();
    private final FlightCardBoard fBoard;
    private Deck deck;
    private List<Deck> decks;
    private TileParserLoader pileMaker = new TileParserLoader();
    private static final ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private transient ScheduledFuture<?> lastPlayerTask;

    public Controller(boolean isDemo, int gameId, int MaxPlayers, Consumer<Integer> onGameEnd) throws CardEffectException, IOException {
        if(isDemo) {
            fBoard = new FlightCardBoard();
            DeckManager deckCreator = new DeckManager();
            deck = deckCreator.CreateDemoDeck();
        }else{
            fBoard = new FlightCardBoard2();
            DeckManager deckCreator = new DeckManager();
            decks = deckCreator.CreateSecondLevelDeck();
        }
        this.gameId = gameId;
        this.onGameEnd = onGameEnd;
        this.hourglass = new Hourglass(h -> {
            try {
                onHourglassStateChange(h);
            } catch (BusinessLogicException e) {
                throw new RuntimeException(e);
            }
        });
        this.isDemo = isDemo;
        this.MaxPlayers = MaxPlayers;
        this.playerIdCounter = new AtomicInteger(1); //verificare che matcha con la logica
        pileOfTile = pileMaker.loadTiles();
        Collections.shuffle(pileOfTile);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE PARTITA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: sostituire gli inform multipli con broadcastInform (oppure eliminarlo e dove è usato mettere serie di inform singoli) sia qui nel gamemanager

    public synchronized void notifyView(String nickname) {
        VirtualView v = viewsByNickname.get(nickname);
        Player p      = playersByNickname.get(nickname);
        try {
            v.updateGameState(p.getGameFase());
            v.showUpdate(
                    nickname,
                    getFirePower(p),
                    getPowerEngine(p),
                    p.getCredit(),
                    fBoard.getPositionOfPlayer(p),
                    p.presencePurpleAlien(),
                    p.presenceBrownAlien(),
                    p.getTotalHuman(),
                    p.getTotalEnergy()
            );
        } catch (RemoteException e) {
            markDisconnected(nickname);
            broadcastInform(nickname + " is disconnected");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void notifyAllViews() {
        for (String nickname : new ArrayList<>(viewsByNickname.keySet())) {
            notifyView(nickname);
        }
    }

    public synchronized void addPlayer(String nickname, VirtualView view) throws BusinessLogicException, Exception {
        if (playersByNickname.containsKey(nickname)) throw new BusinessLogicException("Nickname already used");
        if (playersByNickname.size() >= MaxPlayers) throw new BusinessLogicException("Game is full");

        Player p = new Player(playerIdCounter.getAndIncrement(), isDemo);
        p.setConnected(true);
        playersByNickname.put(nickname, p);
        viewsByNickname.put(nickname, view);

        view.inform("Player " + nickname + " added to game");
        broadcastInform(nickname + "joined");
    }

    //TODO: discutere con fra per vedere se eliminabile (usare getPlayerCheck)
    public Player getPlayerByNickname(String nickname) {
        return playersByNickname.get(nickname);
    }

    private Player getPlayerCheck(String nickname) throws BusinessLogicException {
        Player player = playersByNickname.get(nickname);
        if (player == null) throw new BusinessLogicException("Player not found");
        return player;
    }

    private VirtualView getViewCheck(String nickname) throws BusinessLogicException {
        VirtualView view = viewsByNickname.get(nickname);
        if (view == null) throw new BusinessLogicException("Player not found");
        return view;
    }

    private String getNickByPlayer(Player player) throws BusinessLogicException {
        return playersByNickname.entrySet().stream()
                .filter(e -> e.getValue().equals(player))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Player Not Found"));
    }


    public void broadcastInform(String msg) {
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            VirtualView v = viewsByNickname.get(nickname);
            try {
                v.inform(msg);
            } catch (IOException e) {
                markDisconnected(nickname); //il client non risponde: disconnesso (il metodo informa tutti) //da cambiare, no markDisconnected2
            } catch (Exception e) {
                markDisconnected(nickname);
                throw new RuntimeException(e);
            }
        }
    }

    public int countConnectedPlayers() {
        return (int) playersByNickname.values().stream().filter(Player::isConnected).count();
    }

    public GamePhase getPrincipalGameFase() {
        return principalGamePhase;
    }

    public synchronized void markDisconnected(String nickname) {
        Player p = playersByNickname.get(nickname);
        if (p != null && p.isConnected()) {
            p.setConnected(false);
            broadcastInform(nickname + " is disconnected");
            setTimeout();
        }
    }

    public synchronized void markReconnected(String nickname, VirtualView view) throws BusinessLogicException {
        viewsByNickname.put(nickname, view);
        Player p = playersByNickname.get(nickname);
        if (p == null)
            throw new BusinessLogicException("Player not found: " + nickname);

        if (!p.isConnected()) {
            p.setConnected(true);
            broadcastInform(nickname + " is reconnected");
        }
        cancelLastPlayerTimeout();
        notifyView(nickname);
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

    public boolean isGameStarted() {
        return playersByNickname.values().stream()
                .anyMatch(p -> p.getGameFase() != GamePhase.WAITING_FOR_PLAYERS);
    }

    public int getMaxPlayers(){ return MaxPlayers; }

    private synchronized void cancelLastPlayerTimeout() {
        if (lastPlayerTask != null) {
            lastPlayerTask.cancel(false);
            lastPlayerTask = null;
        }
    }

    private synchronized void setTimeout() {
        cancelLastPlayerTimeout();
        if (countConnectedPlayers() == 1) {
            lastPlayerTask = TIMEOUT_EXECUTOR.schedule(() -> {
                String winner = playersByNickname.entrySet().stream()
                        .filter(e -> e.getValue().isConnected())
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);

                if (winner != null) {
                    try {
                        viewsByNickname.get(winner).inform("You win by timeout!");
                    } catch (Exception ignored) {}
                }
                onGameEnd.accept(gameId);
            }, 1, TimeUnit.MINUTES);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 1
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //TODO: discutere di sto updateMapPosition
    //TODO: vedere discorso Exception vs IOException: catcharli entrambi ? come gestirli?

    public synchronized void startGame() {
        playersByNickname.values().forEach(p -> p.setGameFase(GamePhase.BOARD_SETUP));

        viewsByNickname.forEach((nick, v) -> {
            try {
                v.setFlagStart(); //ne hai parlato con oleg a lezione , così non cambiate
                v.updateMapPosition(playerPosition);
                v.inform("Game is starting!");
            } catch (Exception e) {
                markDisconnected(nick);
            }
        });
        if (!isDemo) startHourglass();
        notifyAllViews();
    }

    public synchronized Tile getCoveredTile(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        int size = getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        int randomIdx = ThreadLocalRandom.current().nextInt(size);
        p.setGameFase(GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);

        return getTile(randomIdx);
    }

    public synchronized Tile chooseUncoveredTile(String nickname, int idTile) throws BusinessLogicException {
        List<Tile> uncoveredTiles = getShownTiles();
        Optional<Tile> opt = uncoveredTiles.stream().filter(t -> t.getIdTile() == idTile).findFirst();
        if(opt.isEmpty()) throw new BusinessLogicException("Tile already taken");

        Player p = getPlayerCheck(nickname);
        p.setGameFase(GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);
        return getShownTile(uncoveredTiles.indexOf(opt.get()));
    }

    public synchronized void dropTile (String nickname, Tile tile) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        addToShownTile(tile);
        p.setGameFase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    public synchronized void placeTile(String nickname, Tile tile, int[] cord) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        p.addTile(cord[0], cord[1], tile);
        p.setGameFase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    public synchronized void setReady(String nickname) throws BusinessLogicException, RemoteException {
        Player p = getPlayerCheck(nickname);

        getFlightCardBoard().setPlayerReadyToFly(p, isDemo);
        ;
        if(playersByNickname.values().stream().filter(Player::isConnected).allMatch(e -> e.getGameFase() == GamePhase.WAITING_FOR_PLAYERS)) {
            startFlight();
        } else{
            p.setGameFase(GamePhase.WAITING_FOR_PLAYERS);
            notifyView(nickname);
        }
    }

    //l'update non va gestito nel try-catch, metto notifyallviews alla fine prima di activateDrawPhase()
    //vedere bene questo metodo
    public synchronized void startFlight() throws BusinessLogicException {
        if(!isDemo) mergeDecks();
        //metto in lista gli eventuali players disconnesi che non hanno chiamato il metodo setReady
        List<Player> playersInFlight = fBoard.getOrderedPlayers();
        for(Player p : playersByNickname.values()) if(!playersInFlight.contains(p)) fBoard.setPlayerReadyToFly(p, isDemo);

        broadcastInform("Flight started!");
        playersByNickname.forEach( (s, p) -> p.setGameFase(GamePhase.CARD_EFFECT));

        viewsByNickname.forEach((nick, v) -> {
            checkPlayerAssembly(nick , 2 , 3);
            //TODO: controlli tiles e attivare l'effetto delle tessere (ex. aggiungere umani per celle ecc..)
            //TODO: questa chiamata va fatta per le view dei giocatori connessi. Non checkare prima se il giocatore
            // connesso o meno, e lasciare partire l'eccezione? (che lo mette disconnesso anche se già lo è)
            try {
                //viewsByNickname.get(nick).updateMapPosition(playerPosition);
                //updatePlayer(nick);
                //non mi convince
                //viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
                //v.updateGameState(GamePhase.CARD_EFFECT);
                //TODO: franci gestire bene l'update
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        activateDrawPhase();
    }

    public synchronized void activateDrawPhase() throws BusinessLogicException {
        List<Player> candidates = fBoard.getOrderedPlayers().stream()
                .filter(Player::isConnected)
                .toList();

        if(candidates.isEmpty()) throw new BusinessLogicException("No player connected");

        for(Player leader : candidates) {
            String leaderNick = playersByNickname.entrySet().stream()
                    .filter(e -> e.getValue().equals(leader))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new BusinessLogicException("Impossible to find first player's nickname"));
            VirtualView v = viewsByNickname.get(leaderNick);
            leader.setGameFase(GamePhase.DRAW_PHASE);

            try {
                v.inform("You're the leader! Draw a card");
                return;
            } catch (Exception e) {
                markDisconnected(leaderNick);
                leader.setGameFase(GamePhase.CARD_EFFECT);
                throw new RuntimeException(e);
            }
        }
        notifyAllViews();
    }

    public  synchronized void startHourglass(){
        hourglass.flip();
        broadcastInform("Hourglass started!");
    }

    public void flipHourglass (String nickname) throws RemoteException, BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        int flips = hourglass.getFlips();
        HourglassState state = hourglass.getState();

        switch(flips){
            case 1:
                if(state == HourglassState.EXPIRED){
                    hourglass.flip();
                    broadcastInform("Hourglass flipped a second time!");
                } else {
                    try {
                        getViewCheck(nickname).inform("You cannot flip the hourglass: It's still running");
                    } catch (Exception e) {
                        markDisconnected(nickname);
                        throw new RuntimeException(e);
                    }
                }
                break;
            case 2:
                if(state == HourglassState.EXPIRED){
                    try {
                        getViewCheck(nickname).inform("You cannot flip the hourglass: It's still running");
                    } catch (Exception e) {
                        markDisconnected(nickname);
                        throw new RuntimeException(e);
                    }
                } else if (p.getGameFase() == GamePhase.WAITING_FOR_PLAYERS) {
                    hourglass.flip();
                    broadcastInform("Hourglass flipped the last time!");
                } else {
                    try {
                        getViewCheck(nickname).inform("You cannot flip the hourglass for the last time: " +
                                "You are not ready");
                    } catch (Exception e) {
                        markDisconnected(nickname);
                        throw new RuntimeException(e);
                    }
                }
                break;
            default: throw new BusinessLogicException("Impossible to flip the hourglass another time!");
        }
    }

    public void onHourglassStateChange(Hourglass h) throws BusinessLogicException {
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
                startFlight();
                break;
        }
    }

    /*metodo per pescare una carta e attivarla:
      1. pesco con il metodo draw (che rimuove dal deck)
      2. inform + print card su tutte le view
      3. chiamo activate card
      3.1 activatecard chiama il metodo accept sulla carta che chiama il visit corretto sul visitor
      3.2 logica della carta + ricalcolo nuove posizione ecc..
      4. alla fine, check se deck vuoto:
        -si, si passa alla fase di premizione (cambio fase, inform..))
        -no, rimodifico le fasi per una nuova drawcard (assegno la fase di drawCard al leader e agli altri quella di attesa..)
      5. update per ogni player
   */
    public synchronized void drawCardManagement(String nickname) throws BusinessLogicException, CardEffectException{
        Card card = deck.draw();
        
        Player drawer = getPlayerCheck(nickname);
        drawer.setGameFase(GamePhase.CARD_EFFECT);
        //TODO: update del drawer (nuova fase)
        //      In questi casi, in cui si updata solo la fase, conviene chiamare tutto il metodo update?
        //      Basterebbe updtare solo la fase

        broadcastInform("Card drawn!");
        viewsByNickname.forEach( (s, v) -> {
            try {
                v.printCard(card);
            } catch (Exception e) {
                markDisconnected(s);
                throw new RuntimeException(e);
            }
        });

        activateCard(card);

        if(deck.isEmpty()){
            startAwardsPhase();
        } else {
            playersByNickname.values().forEach(p -> p.setGameFase(GamePhase.CARD_EFFECT));
            notifyAllViews();
            activateDrawPhase();
            //TODO: update per tutti (così facendo, il leader effettivo verrà updatato due volte, non penso sia un problema, capire)
            //io metterei l'update qui, prima di activate....
        }
    }

    public void startAwardsPhase(){

        playersByNickname.forEach( (s, p) -> {
            p.setGameFase(GamePhase.SCORING);
            notifyAllViews();
        });

        int malusBrokenTile = fBoard.getBrokenMalus();
        int bonusBestShip = fBoard.getBonusBestShip();
        int redGoodBonus = fBoard.getBonusRedCargo();
        int yellowGoodBonus = fBoard.getBonusYellowCargo();
        int greenGoodBonus = fBoard.getBonusGreenCargo();
        int blueGoodBonus = fBoard.getBonusBlueCargo();
        int[] arrivalBonus = {fBoard.getBonusFirstPosition(), fBoard.getBonusSecondPosition(),
                fBoard.getBonusThirdPosition(), fBoard.getBonusFourthPosition()};
        List<Player> orderedPlayers = fBoard.getOrderedPlayers();

        int minExpConnectors = playersByNickname.values().stream()
                .mapToInt(Player::countExposedConnectors)
                .min()
                .orElseThrow( () -> new IllegalArgumentException("No Player in Game"));
        List<Player> bestShipPlayers = playersByNickname.values().stream()
                .filter(p -> p.countExposedConnectors() == minExpConnectors)
                .toList();

        for (int i = 0; i < orderedPlayers.size(); i++) {
            orderedPlayers.get(i).addCredits(arrivalBonus[i]);
        }

        for (Player p : bestShipPlayers) {
            p.addCredits(bonusBestShip);
        }

        for (Player p : playersByNickname.values()) {
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

            String nick = playersByNickname.entrySet().stream()
                    .filter(e -> e.getValue().equals(p))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Impossible to find first player nickname"));
            VirtualView v = viewsByNickname.get(nick);
            int totalCredits = p.getCredit();
            p.setGameFase(GamePhase.EXIT);
            //TODO: anche qui, se una view è disconnessa, provo lo stesso ad updatarle -> finirò nel catch e
            // lo rimetterò disconnesso anche se già lo è
            try{
                if(totalCredits>0) v.inform("Your total credits are: " + totalCredits + " You won!");
                else v.inform("Your total credits are: " + totalCredits + " You lost!");
                v.inform("Game over. thank you for playing!");
                //TODO: update view
            } catch (Exception e) {
                markDisconnected(nick);
                throw new RuntimeException(e);
            }
        }

        onGameEnd.accept(this.gameId);
    }

    public synchronized List<Card> showDeck (int idxDeck){
        return new ArrayList<>(decks.get(idxDeck).getCards());
    }

    public synchronized Tile[][] lookAtDashBoard(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        return p.getDashMatrix();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 2
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean askPlayerDecision(String condition, Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);
        try {
            return x.ask(condition);
        } catch (Exception e) {
            markDisconnected(nick);
            throw new RuntimeException(e);
        }
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
    public List<Tile> getShownTiles(){
        return shownTile;
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
    public int getPowerEngine(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);

        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Tile y = p.getTile(i, j);
                Boolean var = false;
                switch (y) {
                    case Engine c -> {
                        var = c.isDouble();
                        if (var) {
                            boolean activate = manageEnergyCell(nick);
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

    public double getFirePower(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);

        double tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                boolean var;
                switch (y) {
                    case Engine c -> {
                        var = c.isDouble();
                        if (var) {
                            boolean activate = manageEnergyCell(nick);
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

    public void removeGoods(Player p, int num) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        int totalEnergy = getTotalEnergy(p);
        int totalGood = getTotalGood(p);

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
                    try {
                        x.inform("selezionare cella ed eliminare rosso");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int[] vari = null;
                    try {
                        vari = x.askCoordinate();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
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
                    try {
                        x.inform("selezionare cella ed eliminare giallo");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int[] vari = null;
                    try {
                        vari = x.askCoordinate();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
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
                    try {
                        x.inform("selezionare cella ed eliminare verde");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int[] vari = null;
                    try {
                        vari = x.askCoordinate();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
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
                    try {
                        x.inform("selezionare cella ed eliminare blu");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int[] vari = null;
                    try {
                        vari = x.askCoordinate();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
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
                    try {
                        x.inform("selezionare cella ed eliminare una batteria");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int[] vari = null;
                    try {
                        vari = x.askCoordinate();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
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

    public void addGoods(Player p, List<Colour> list) throws BusinessLogicException {
        boolean flag = true;
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        try {
            if(!x.ask("vuoi aggiungere un goods?")) flag=false;
        } catch (Exception e) {
            markDisconnected(nick);
            throw new RuntimeException(e);
        }
        while (list.size() != 0 && flag == true) {
            try {
                x.inform("seleziona una HOusing unit");
            } catch (Exception e) {
                markDisconnected(nick);
                throw new RuntimeException(e);
            }
            int[] vari = null;
            try {
                vari = x.askCoordinate();
            } catch (Exception e) {
                markDisconnected(nick);
                throw new RuntimeException(e);
            }
            Tile t = p.getTile(vari[0], vari[1]);
            switch (t){
                case StorageUnit c -> {
                    if(c.isFull()){
                        try {
                            x.printListOfGoods(c.getListOfGoods());
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
                        }
                        try {
                            x.ask("seleziona indice da rimuovere");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        int tmpint = 0;
                        try {
                            tmpint = x.askIndex();
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
                        }
                        Colour tmp = c.getListOfGoods().get(tmpint - 1);
                        c.removeGood(tmpint-1);
                        list.add(tmp);
                    }
                    try {
                        x.inform("seleziona la merce da inserire");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    int tmpint = 0;
                    try {
                        tmpint = x.askIndex();
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    c.addGood(list.get(tmpint-1));
                }
                default -> {
                    try {
                        x.inform("cella non valida");
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                }


            }
            try {
                if(!x.ask("Vuoi continurare")) flag = false;
            } catch (Exception e) {
                markDisconnected(nick);
                throw new RuntimeException(e);
            }

            //select storage Unit
            //selecton indice lista che sto passando dentro
            //t.addGood
            // p.askPlayerDecision
            // se no diventa false
            // remove.(index)
        }
    }

    public void addHuman() throws Exception {
        for (Player p : playersByNickname.values()) {
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

    public void removeCrewmate(Player p, int num) throws Exception {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        int totalCrew = getNumCrew(p);
        if (num >= totalCrew) {
            p.setEliminated();
        } else {
            while (num > 0) {
                x.inform("seleziona un Housing unit");
                int[] vari = x.askCoordinate();
                Tile y = p.getTile(vari[0], vari[1]);
                switch (y){
                    case HousingUnit h -> {
                        if(h.returnLenght()>0){
                            int tmp = h.removeHumans(1);
                            if(tmp == 2) p.setBrownAlien();
                            if(tmp == 3) p.setPurpleAlien();
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

    //TODO: il metodo non deve lanciare la exception generica
    public void startPlauge(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        int firstNumber = getNumCrew(p);
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                switch (y) {
                    case HousingUnit c -> {
                        if (c.isConnected()) {
                            v.askIndex();
                            int x = c.removeHumans(1);
                            tmp++;
                            if (x == 2) p.setBrownAlien();
                            if (x == 3) p.setPurpleAlien();
                        }

                    }
                    default -> {
                    }
                }
            }
            if (tmp == firstNumber) {
                p.setEliminated();
            }
        }
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected, and they want to use a battery
     *
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(String nick, int d) throws Exception {
        boolean flag = false;
        VirtualView x = viewsByNickname.get(nick);

        while (!flag) {
            if (x.ask("vuoi usare uno scudo?")) {
                int[] coordinate = x.askCoordinate();
                Tile y = playersByNickname.get(nick).getTile(coordinate[0], coordinate[1]);
                switch (y) {
                    case Shield shield -> {
                        if (!(shield.getProtectedCorner(d) == 8)) {
                            x.inform("seleziona un'altro scudo");
                        } else {
                            return manageEnergyCell(nick);
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
    public void defenceFromCannon(int dir, boolean type, int dir2, Player p) throws Exception {
        String nick = getNickByPlayer(p);

        if (dir == 0) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    p.removeFrom0(dir2);
                    askStartHousingForControl(nick);
                }
            }
        } else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    p.removeFrom2(dir2);
                    askStartHousingForControl(nick);
                }
            }
        } else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    p.removeFrom1(dir2);
                    askStartHousingForControl(nick);
                }
            }
        } else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    p.removeFrom3(dir2);
                    askStartHousingForControl(nick);
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
                        askStartHousingForControl(p);
                    }
                    if (!type && playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                            askStartHousingForControl(p);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                        askStartHousingForControl(p);
                    }
                    if (!type && !playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                            askStartHousingForControl(p);
                        }
                    }

                }
            } else if (dir == 1 || dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, p)) {
                        playersByNickname.get(p).removeFrom0(dir2);
                        askStartHousingForControl(p);
                    }
                    if (!type && !playersByNickname.get(p).checkNoConnector(dir, dir2)) {
                        if (!isProtected(p, dir)) {
                            playersByNickname.get(p).removeFrom2(dir2);
                            askStartHousingForControl(p);
                        }
                    }
                }
            }
        }
    }

    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(String nick)  {
        VirtualView x = viewsByNickname.get(nick);

        int[] coordinate = new int[2];
        boolean exits = false;

        //ricordarsi di mettere la catch per gestione null;
        boolean use = false;
        try {
            use = x.ask("Vuoi usare una batteria?");
        } catch (Exception e) {
            markDisconnected(nick);
            throw new RuntimeException(e);
        }
        if (!use) {
            return false;
        } else {
            while (!exits) {
                try {
                    coordinate = x.askCoordinate();
                } catch (Exception e) {
                    markDisconnected(nick);
                    throw new RuntimeException(e);
                }
                Tile p = playersByNickname.get(nick).getTile(coordinate[0], coordinate[1]);
                switch (p) {
                    case EnergyCell c -> {
                        int capacity = c.getCapacity();
                        if (capacity == 0) {
                            try {
                                if (!x.ask("Vuoi selezionare un'altra cella?")) {
                                    return false;
                                }
                            } catch (Exception e) {
                                markDisconnected(nick);
                                throw new RuntimeException(e);
                            }
                        } else {
                            c.useBattery();
                            return true;
                        }
                    }
                    default -> {
                        System.out.println("cella non valida");
                        try {
                            if (!x.ask("vuoi selezionare un'altra cella?")) {
                                exits = true;
                            }
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
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

    public void askStartHousingForControl(String nickname) throws BusinessLogicException {
        VirtualView v = getViewCheck(nickname);
        Player p = getPlayerCheck(nickname);

        int[] xy;
        try {
            v.inform("choose your starting hounsing unit");
            xy = v.askCoordinate();
        } catch (RemoteException e) {
            markDisconnected(nickname);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean flag = true;
        while (flag) {
            Tile tmp = p.getTile(xy[0], xy[1]);
            switch (tmp) {
                case HousingUnit h -> flag = false;
                default -> {
                    try {
                        v.inform("Position non valid , choose another tile");
                        xy = v.askCoordinate();
                    } catch (RemoteException e) {
                        markDisconnected(nickname);
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        checkPlayerAssembly(nickname,  xy[0], xy[1]);
    }

    private void checkPlayerAssembly(String id , int x , int y){
        playersByNickname.get(id).controlAssembly(x,y);
        try {
            notifyView(id);
            viewsByNickname.get(id).printPlayerDashboard(playersByNickname.get(id).getDashMatrix());
        } catch (Exception e) {
            markDisconnected(id);
            throw new RuntimeException(e);
        }
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
}

