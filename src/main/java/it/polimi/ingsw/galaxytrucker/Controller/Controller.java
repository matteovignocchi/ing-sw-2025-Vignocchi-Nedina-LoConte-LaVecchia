package it.polimi.ingsw.galaxytrucker.Controller;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.*;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Model.FlightCardBoard.FlightCardBoard2;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//TODO: corner-case: crash in startGame
//TODO: corner-case: crash del leader quando deve pescare

//TODO: verificare che tutti i metodi invocati sulla view vanno e vengono invocati correttamente anche per socket
//TODO: inserire i timeout in tutti i metodi che mettono in attesa il server. Creare nuovi metodi inform, ecc.. nel controller? se si, come integrare
// nei metodi sostituendoli alla perfezione ai precedenti?
//TODO: discorso gestione disconnessioni nei metodi di oleg. disconnetto e poi continuo con esecuzione, o devo returnare. (mandare mail).
//TODO: gestione degli askPlayerDecision, askPlayerIndex ecc.. nei metodi di oleg
//TODO: parser per non passare oggetti del model
//TODO: inserire mappa virtualview-player? e usare quella al posto di getPlayer.. ogni volta? vedere alla fine.
//TODO: creare metodi inform ecc.. che chiamano i metodi corrispondenti sulla view, inglobando i try-catch, per snellire il codice negli altri metodi


public class Controller implements Serializable {
    private final int gameId;
    public transient Map<String, VirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> playersByNickname = new ConcurrentHashMap<>();
    private final Map<String , Integer> playersPosition = new ConcurrentHashMap<>();

    //Capire se tenere e come gestire
    private final Set<String> loggedInUsers;

    private final AtomicInteger playerIdCounter;
    private final int MaxPlayers;
    private final boolean isDemo;
    private transient final Consumer<Integer> onGameEnd;
    private GamePhase principalGamePhase;
    private int numberOfEnter =0;
    private final int TIME_OUT = 30;


    private transient Hourglass hourglass;
    public List<Tile> pileOfTile;
    public List<Tile> shownTile = new ArrayList<>();
    private final FlightCardBoard fBoard;
    private Deck deck;
    private List<Deck> decks;
    private TileParserLoader pileMaker = new TileParserLoader();
    private transient static final  ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private transient ScheduledFuture<?> lastPlayerTask;

    public Controller(boolean isDemo, int gameId, int MaxPlayers, Consumer<Integer> onGameEnd, Set<String> loggedInUsers) throws CardEffectException, IOException {
        if(isDemo) {
            fBoard = new FlightCardBoard();
            DeckManager deckCreator = new DeckManager();
            deck = deckCreator.CreateDemoDeck();
        }else{
            fBoard = new FlightCardBoard2();
            DeckManager deckCreator = new DeckManager();
            decks = deckCreator.CreateSecondLevelDeck();
            deck = new Deck();
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

        this.loggedInUsers = loggedInUsers;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE PARTITA
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void notifyView(String nickname) {
        VirtualView v = viewsByNickname.get(nickname);
        Player p      = playersByNickname.get(nickname);
        try {
            v.updateGameState(p.getGamePhase());
            v.showUpdate(
                    nickname,
                    getFirePower(p),
                    getPowerEngine(p),
                    p.getCredits(),
                    //fBoard.getPositionOfPlayer(p),
                    p.presencePurpleAlien(),
                    p.presenceBrownAlien(),
                    p.getTotalHuman(),
                    p.getTotalEnergy()
            );
            if(getPlayerCheck(nickname).getGamePhase()==GamePhase.TILE_MANAGEMENT) v.setTile(p.getLastTile());
        } catch (IOException e) {
            markDisconnected(nickname);
        } catch (Exception e) {
            markDisconnected(nickname);
            System.err.println("[ERROR] in notifyView: " + e.getMessage());
        }
    }

    public void notifyAllViews() throws BusinessLogicException {
        for (String nickname : new ArrayList<>(viewsByNickname.keySet())) {
            Player p = getPlayerCheck(nickname);
            if(p.isConnected()) notifyView(nickname);
        }
    }

    public void addPlayer(String nickname, VirtualView view) throws BusinessLogicException, Exception {
        numberOfEnter ++;
        if (playersByNickname.containsKey(nickname)) throw new BusinessLogicException("Nickname already used");
        if (playersByNickname.size() >= MaxPlayers) throw new BusinessLogicException("Game is full");
        int tmp = 0;
        switch (numberOfEnter) {
            case 1 -> tmp = 33;
            case 2-> tmp = 34;
            case 3 -> tmp = 52;
            case 4 -> tmp = 61;
        }


        Player p = new Player(playerIdCounter.getAndIncrement(), isDemo , tmp);
        p.setConnected(true);

        p.setGamePhase(GamePhase.WAITING_IN_LOBBY);
        view.updateGameState(GamePhase.WAITING_IN_LOBBY);
//        view.setTile(p.getTile(2,3));

        playersByNickname.put(nickname, p);
        viewsByNickname.put(nickname, view);
        playersPosition.put(nickname, p.getId());

        broadcastInform( nickname + "  joined");
    }

    //Per testing
    public Map<String, Player> getPlayersByNickname(){
        return playersByNickname;
    }

    //Se tutto va, eliminabile
    public Player getPlayerByNickname(String nickname) {
        return playersByNickname.get(nickname);
    }

    public Player getPlayerCheck(String nickname) throws BusinessLogicException {
        Player player = playersByNickname.get(nickname);
        if (player == null) throw new BusinessLogicException("Player not found");
        return player;
    }

    private VirtualView getViewCheck(String nickname) throws BusinessLogicException {
        VirtualView view = viewsByNickname.get(nickname);
        if (view == null) throw new BusinessLogicException("Player not found");
        return view;
    }

    public String getNickByPlayer(Player player) throws BusinessLogicException {
        return playersByNickname.entrySet().stream()
                .filter(e -> e.getValue().equals(player))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Player Not Found"));
    }


    public void broadcastInform(String msg) {
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            Player p = getPlayerByNickname(nickname);
            if(p.isConnected()) {
                VirtualView v = viewsByNickname.get(nickname);
                try {
                    v.inform(msg);
                } catch (IOException e) {
                    markDisconnected(nickname);
                } catch (Exception e) {
                    markDisconnected(nickname);
                    System.err.println("[ERROR] in broadcastInform: " + e.getMessage());
                }
            }
        }
    }

    public int countConnectedPlayers() {
        return (int) playersByNickname.values().stream().filter(Player::isConnected).count();
    }

    public GamePhase getPrincipalGameFase() {
        return principalGamePhase;
    }

    public void markDisconnected(String nickname) {
        Player p = playersByNickname.get(nickname);
        if (p != null && p.isConnected()) {
            p.setConnected(false);
            broadcastInform("SERVER: " + nickname + " is disconnected");
            setTimeout();

            //Capire se tenere e come gestire
            loggedInUsers.remove(nickname);
        }
    }

    public void markReconnected(String nickname, VirtualView view) throws BusinessLogicException {
        viewsByNickname.put(nickname, view);
        Player p = playersByNickname.get(nickname);
        if (p == null)
            throw new BusinessLogicException("Player not found: " + nickname);

        if (!p.isConnected()) {
            p.setConnected(true);
            broadcastInform("SERVER: " + nickname + " is reconnected");
        }
        cancelLastPlayerTimeout();
        notifyView(nickname);
    }

    public void handleElimination(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = getViewCheck(nick);

        //TODO: capire la fase di eliminazione
        p.setGamePhase(GamePhase.WAITING_FOR_TURN);

        try{
            v.inform("SERVER: You have been eliminated!");
            notifyView(nick);
        } catch (IOException e){
            markDisconnected(nick);
        } catch (Exception e){
            markDisconnected(nick);
            System.err.println("[ERROR] in handleElimination " + e.getMessage());
        }
    }

    public void reinitializeAfterLoad(Consumer<Hourglass> hourglassListener) {
        this.viewsByNickname = new ConcurrentHashMap<>();
        this.hourglass       = new Hourglass(hourglassListener);
    }

    public boolean isGameStarted() {
        return playersByNickname.values().stream()
                .anyMatch(p -> p.getGamePhase() != GamePhase.WAITING_FOR_PLAYERS);
    }

    public int getMaxPlayers(){ return MaxPlayers; }

    private void cancelLastPlayerTimeout() {
        if (lastPlayerTask != null) {
            lastPlayerTask.cancel(false);
            lastPlayerTask = null;
        }
    }

    private void setTimeout() {
        cancelLastPlayerTimeout();
        if (countConnectedPlayers() == 1) {
            lastPlayerTask = TIMEOUT_EXECUTOR.schedule(() -> {
                String winner = playersByNickname.entrySet().stream()
                        .filter(e -> e.getValue().isConnected())
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);

                if (winner != null) {
                    try {
                        viewsByNickname.get(winner).inform("SERVER: " + "You win by timeout!");
                    } catch (Exception ignored) {}
                }
                onGameEnd.accept(gameId);
            }, 1, TimeUnit.MINUTES);
        }
    }

    public void sendInformTo(String nickname, String msg) {
        try {
            VirtualView v = getViewCheck(nickname);
            v.inform(msg);
        } catch (Exception e) {
            markDisconnected(nickname);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 1
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void startGame() {
        playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.BOARD_SETUP));

        viewsByNickname.forEach((nick, v) -> {
            try {
                v.updateMapPosition(playersPosition);
                v.setIsDemo(isDemo);
                v.updateGameState(GamePhase.BOARD_SETUP);
                v.setTile(getPlayerCheck(nick).getTile(2,3));
                v.printPlayerDashboard(getPlayerCheck(nick).getDashMatrix());
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in startGame: " + e.getMessage());
            }
        });

        viewsByNickname.forEach((nick, v) -> {
            try {
                notifyView(nick);
                v.setStart();
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in startGame: " + e.getMessage());
            }
        });

        if (!isDemo) startHourglass();
    }

    public Tile getCoveredTile(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        int size = getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        Tile t = getTile(1);
        p.setLastTile(t);

        p.setGamePhase(GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);

        return t;
    }

    public Tile chooseUncoveredTile(String nickname, int idTile) throws BusinessLogicException {
        List<Tile> uncoveredTiles = getShownTiles();
        Optional<Tile> opt = uncoveredTiles.stream().filter(t -> t.getIdTile() == idTile).findFirst();
        if(opt.isEmpty()) throw new BusinessLogicException("Tile already taken");
        if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("No tiles found");

        Player p = getPlayerCheck(nickname);
        Tile t = getShownTile(uncoveredTiles.indexOf(opt.get()));
        p.setLastTile(t);

        p.setGamePhase(GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);
        return t;
    }

    public void dropTile (String nickname, Tile tile) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        addToShownTile(tile);
        p.setGamePhase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    public void placeTile(String nickname, Tile tile, int[] cord) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        p.addTile(cord[0], cord[1], tile);
        p.setGamePhase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    public Tile getReservedTile(String nickname , int id) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        List<Tile> discardPile = p.getTilesInDiscardPile();
        for(Tile t : discardPile) {
            if(t.getIdTile() == id) {
                discardPile.remove(t);
                p.setLastTile(t);
                p.setGamePhase(GamePhase.TILE_MANAGEMENT);
                notifyView(nickname);
                return t;
            }
        }
        throw new BusinessLogicException("Tile not found");
    }

    public void setReady(String nickname) throws BusinessLogicException, RemoteException {
        Player p = getPlayerCheck(nickname);

        getFlightCardBoard().setPlayerReadyToFly(p, isDemo);

        p.setGamePhase(GamePhase.WAITING_FOR_PLAYERS);

        if(playersByNickname.values().stream().filter(Player::isConnected).allMatch(e -> e.getGamePhase() == GamePhase.WAITING_FOR_PLAYERS)) {
            startFlight();
            return;
        }

        notifyView(nickname);
    }

    public void startFlight() throws BusinessLogicException {

        if(!isDemo){
            hourglass.cancel();
            mergeDecks();
        }

        //metto in lista gli eventuali players disconnesi che non hanno chiamato il metodo setReady
        List<Player> playersInFlight = fBoard.getOrderedPlayers();
        for(Player p : playersByNickname.values()) if(!playersInFlight.contains(p)) fBoard.setPlayerReadyToFly(p, isDemo);

        //aggiorno la mappa di oleg
        for(Map.Entry<String, Player> entry : playersByNickname.entrySet()) {
            Player p = entry.getValue();
            String nickname = entry.getKey();
            playersPosition.put(nickname, p.getPos());
        }

        broadcastInform("SERVER: " + "Flight started!");
        playersByNickname.forEach( (s, p) -> p.setGamePhase(GamePhase.WAITING_FOR_TURN));

        addHuman();

        for (String nick : viewsByNickname.keySet()) checkPlayerAssembly(nick, 2, 3);

        //notifyAllViews();

        activateDrawPhase();
    }

    public void activateDrawPhase() throws BusinessLogicException {
        List<Player> candidates = fBoard.getOrderedPlayers().stream()
                .filter(Player::isConnected)
                .toList();

        if(candidates.isEmpty()) throw new BusinessLogicException("No player connected");

        String leaderNick = null;
        for(Player leader : candidates) {
            leaderNick = playersByNickname.entrySet().stream()
                    .filter(e -> e.getValue().equals(leader))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new BusinessLogicException("Impossible to find first player's nickname"));
            VirtualView v = viewsByNickname.get(leaderNick);
            leader.setGamePhase(GamePhase.DRAW_PHASE);

            try {
                v.inform("SERVER: " + "You're the leader! Draw a card");
                notifyView(leaderNick);
                break;
            } catch (IOException e) {
                markDisconnected(leaderNick);
                leader.setGamePhase(GamePhase.WAITING_FOR_TURN);
            } catch (Exception e){
                markDisconnected(leaderNick);
                leader.setGamePhase(GamePhase.WAITING_FOR_TURN);
                System.err.println("[ERROR] in activateDrawPhase:" + e.getMessage());
            }
        }

        for (String nickname : viewsByNickname.keySet()) {
            Player p = getPlayerCheck(nickname);
            if(p.isConnected() && !p.isEliminated() && !nickname.equals(leaderNick)) notifyView(nickname);
        }
    }

    public void startHourglass(){
        hourglass.flip();
        broadcastInform("SERVER: " + "Hourglass started!");
    }

    public void flipHourglass (String nickname) throws RemoteException, BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        int flips = hourglass.getFlips();
        HourglassState state = hourglass.getState();

        switch(flips){
            case 1:
                if(state == HourglassState.EXPIRED){
                    hourglass.flip();
                    broadcastInform("\nSERVER: " + "Hourglass flipped a second time!");
                } else {
                    try {
                        getViewCheck(nickname).inform("\nSERVER: " + "You cannot flip the hourglass: It's still running");
                    } catch (IOException e) {
                        markDisconnected(nickname);
                    } catch (Exception e){
                        markDisconnected(nickname);
                        System.err.println("[ERROR] in activateDrawPhase: " + e.getMessage());
                    }
                }
                break;
            case 2:
                if(state == HourglassState.ONGOING){
                    try {
                        getViewCheck(nickname).inform("\nSERVER: " + "You cannot flip the hourglass: It's still running");
                    } catch (IOException e) {
                        markDisconnected(nickname);
                    } catch (Exception e){
                        markDisconnected(nickname);
                        System.err.println("[ERROR] in activateDrawPhase: " + e.getMessage());
                    }
                } else if (state == HourglassState.EXPIRED && p.getGamePhase() == GamePhase.WAITING_FOR_PLAYERS) {
                    hourglass.flip();
                    broadcastInform("\nSERVER: " + "Hourglass flipped the last time!");
                } else {
                    try {
                        getViewCheck(nickname).inform("\nSERVER: " + "You cannot flip the hourglass for the last time: " +
                                "You are not ready");
                    } catch (IOException e) {
                        markDisconnected(nickname);
                    } catch (Exception e){
                        markDisconnected(nickname);
                        System.err.println("[ERROR] in activateDrawPhase: " + e.getMessage());
                    }
                }
                break;
            default: throw new BusinessLogicException("\nImpossible to flip the hourglass another time!");
        }
    }

    public void onHourglassStateChange(Hourglass h) throws BusinessLogicException {
        int flips = h.getFlips();

        switch (flips) {
            case 1:
                broadcastInform("\nSERVER: " + "First Hourglass expired");
                break;
            case 2:
                broadcastInform("\nSERVER: " + "Second Hourglass expired");
                break;
            case 3:
                broadcastInform("\nSERVER: " + "Time’s up! Building phase ended.");
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
    public void drawCardManagement(String nickname) throws BusinessLogicException {
        Card card = deck.draw();
        
        Player drawer = getPlayerCheck(nickname);
        drawer.setGamePhase(GamePhase.WAITING_FOR_TURN);
        try{
            getViewCheck(nickname).updateGameState(GamePhase.WAITING_FOR_TURN);
        } catch (IOException e){
            markDisconnected(nickname);
        } catch (Exception e){
            markDisconnected(nickname);
            System.err.println("[ERROR] in drawCardManagement: " + e.getMessage());
        }

        broadcastInform("SERVER: " + "Card drawn!");

        for(Map.Entry<String, VirtualView> entry :viewsByNickname.entrySet()){
            String nick = entry.getKey();
            VirtualView v = entry.getValue();
            Player p = getPlayerCheck(nick);

            if(p.isConnected()){
                try {
                    v.printCard(card);
                } catch (IOException e) {
                    markDisconnected(nick);
                } catch (Exception e){
                    markDisconnected(nick);
                    System.err.println("[ERROR] in drawCardManagement: " + e.getMessage());
                }
            }
        }

        activateCard(card);

        if(deck.isEmpty() || fBoard.getOrderedPlayers().isEmpty()){
            startAwardsPhase();
        } else {

            for(Player p : playersByNickname.values()){
                if(!p.isEliminated()){
                    p.setGamePhase(GamePhase.WAITING_FOR_TURN);
                    String msg = "SERVER: Do you want to abandon the flight? ";
                    if(askPlayerDecision(msg, p)){
                        p.setEliminated();
                        handleElimination(p);
                    }
                }
            }
            fBoard.eliminatePlayers();

            activateDrawPhase();
        }
    }

    //TODO: testare e capire se updatare e mettere in exit prima o dopo gli awards
    public void startAwardsPhase() throws BusinessLogicException {

        //UPDATE FATTO DOPO, vedere se va bene
        //playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.EXIT));
        //notifyAllViews();

        broadcastInform("SERVER: Flight ended! Time to collect rewards!");

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
            double factor = p.isEliminated() ? 0.5 : 1.0;
            double totalDouble = 0.0;

            for (Colour c : goods) {
                switch (c) {
                    case RED    -> totalDouble += redGoodBonus    * factor;
                    case YELLOW -> totalDouble += yellowGoodBonus * factor;
                    case GREEN  -> totalDouble += greenGoodBonus  * factor;
                    case BLUE   -> totalDouble += blueGoodBonus   * factor;
                }
            }

            p.addCredits((int) Math.ceil(totalDouble));

            int numBrokenTiles = p.checkDiscardPile();
            p.addCredits(numBrokenTiles * malusBrokenTile);

            String nick = playersByNickname.entrySet().stream()
                    .filter(e -> e.getValue().equals(p))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Impossible to find first player nickname"));
            VirtualView v = viewsByNickname.get(nick);
            int totalCredits = p.getCredits();
            p.setGamePhase(GamePhase.EXIT);  //se fase di exit messa prima, levare

            if(p.isConnected()){
                try{
                    if(totalCredits>0) v.inform("SERVER: " + "Your total credits are: " + totalCredits + " You won!");
                    else v.inform("SERVER: " + "Your total credits are: " + totalCredits + " You lost!");
                    v.inform("SERVER: " + "Game over. Thank you for playing!");
                    notifyView(nick);  //se fase di exit messa prima, levare
                } catch (IOException e) {
                    markDisconnected(nick);
                } catch (Exception e){
                    markDisconnected(nick);
                    System.err.println("[ERROR] in startAwardPhase: " + e.getMessage());
                }
            }
        }

        onGameEnd.accept(this.gameId);
    }

    public List<Card> showDeck (int idxDeck){
        return new ArrayList<>(decks.get(idxDeck).getCards());
    }

    public Tile[][] lookAtDashBoard(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        return p.getDashMatrix();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 2
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //TODO: aspettare risposta del floris per gestione eccezioni più robusta, così comunque ok
    public boolean askPlayerDecision(String question, Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if (!p.isConnected()) return false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> v.ask(question));

        try {
            return future.get(TIME_OUT, TimeUnit.SECONDS);

        } catch (TimeoutException te) {
            future.cancel(true);
            return false;

        } catch (Exception e) {
            future.cancel(true);
            markDisconnected(nick);
            return false;

        } finally {
            executor.shutdownNow();
        }
    }

    public int[] askPlayerCoordinates (Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if(!p.isConnected()) return null;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<int[]> future = executor.submit(v::askCoordinate);

        try{
            int[] coords = future.get(TIME_OUT, TimeUnit.SECONDS);
            if(coords == null) throw new BusinessLogicException("Coordinates null");
            if(coords.length != 2) throw new BusinessLogicException("Coordinates length should be 2");
            return coords;

        } catch (TimeoutException te){
            future.cancel(true);
            return null;

        } catch (Exception e) {
            future.cancel(true);
            markDisconnected(nick);
            return null;

        } finally {
            executor.shutdownNow();
        }
    }

    public int askPlayerIndex (Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if(!p.isConnected()) return 0;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(v::askIndex);

        try{
            return future.get(TIME_OUT, TimeUnit.SECONDS);

        } catch (TimeoutException te){
            future.cancel(true);
            return 0;

        } catch (Exception e) {
            future.cancel(true);
            markDisconnected(nick);
            return 0;

        } finally {
            executor.shutdownNow();
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
    public int getPowerEngineForCard(Player p) throws BusinessLogicException {
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
                            tmp = tmp + 2;

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


    public double getFirePowerForCard(Player p) throws BusinessLogicException {
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
                            if (c.controlCorners(0) != 5) tmp = tmp + 1;
                            else tmp = tmp + 2;
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
            try {
                viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
            } catch (Exception e) {
                markDisconnected(nick);
            }
        }
        if(num < totalGood){
            while(num != 0){
                if(r != 0){
                    if(p.isConnected()){
                        try {
                            x.inform("SERVER: " + "selezionare cella ed eliminare rosso");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in removeGoods: " + e.getMessage());
                        }
                        int[] vari = null;
                        try {
                            vari = askPlayerCoordinates(p);
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

                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
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
                        }
                    }

                }
                if(r == 0 && num!=0 && g != 0){
                    if(p.isConnected()){
                        try {
                            x.inform("SERVER: " + "selezionare cella ed eliminare giallo");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in removeGoods: " + e.getMessage());
                        }
                        int[] vari = null;
                        try {
                            vari = askPlayerCoordinates(p);
                        } catch (Exception e) {
                            markDisconnected(nick);
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
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
                                    case StorageUnit c -> {
                                        for(Colour co : c.getListOfGoods()) {
                                            if (co == Colour.YELLOW) {
                                                r--;
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
                }
                if(r == 0 && g == 0 && v != 0 && num!=0){
                    if(p.isConnected()){
                        try {
                            x.inform("SERVER: " + "selezionare cella ed eliminare verde");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in removeGoods: " + e.getMessage());
                        }
                        int[] vari = askPlayerCoordinates(p);

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
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
                                    case StorageUnit c -> {
                                        for(Colour co : c.getListOfGoods()) {
                                            if (co == Colour.GREEN) {
                                                r--;
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


                }
                if(r == 0 && g == 0 && v == 0 && b != 0 && num!=0){
                  if(p.isConnected()){
                      try {
                          x.inform("SERVER: " + "selezionare cella ed eliminare blu");
                      } catch (IOException e) {
                          markDisconnected(nick);
                      } catch (Exception e){
                          markDisconnected(nick);
                          System.err.println("[ERROR] in removeGoods: " + e.getMessage());
                      }
                      int[] vari = null;
                      try {
                          vari = askPlayerCoordinates(p);
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
                  }else{
                      for(int index =0 ; index <5 ; index++){
                          for(int index2 =0 ; index2 <7 ; index2++){
                              Tile tmpTile = p.getTile(index, index2);
                              switch (tmpTile){
                                  case StorageUnit c -> {
                                      for(Colour co : c.getListOfGoods()) {
                                          if (co == Colour.BLUE) {
                                              r--;
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

                }
                try {
                    viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
                } catch (Exception e) {
                    markDisconnected(nick);
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
                    if(p.isConnected()){
                        try {
                            x.inform("SERVER: " + "selezionare cella ed eliminare una batteria");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in removeGoods: " + e.getMessage());
                        }
                        int[] vari = null;
                        try {
                            vari = askPlayerCoordinates(p);
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
                        }
                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case EnergyCell c -> {
                                if(c.getCapacity() != 0) c.useBattery();
                                finish--;
                            }
                            default -> {}

                        }
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
                                    case EnergyCell c -> {
                                        if(c.getCapacity() != 0) c.useBattery();
                                        finish--;
                                    }
                                    default -> {}
                                }
                            }
                        }

                    }
                    try {
                        viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
                    } catch (Exception e) {
                        markDisconnected(nick);
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
        try {
            viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
        } catch (Exception e) {
            markDisconnected(nick);
        }
    }

    public void addGoods(Player p, List<Colour> list) throws BusinessLogicException {
        boolean flag = true;
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        try {
            if(!x.ask("SERVER: " + "vuoi aggiungere un goods?")) flag=false;
        } catch (IOException e) {
            markDisconnected(nick);
        } catch (Exception e){
            markDisconnected(nick);
            System.err.println("[ERROR] in addGoods: " + e.getMessage());
        }
        while (list.size() != 0 && flag == true) {
            try {
                x.inform("SERVER: " + "seleziona una Housing unit");
                x.printPlayerDashboard(p.getDashMatrix());
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in addGoods: " + e.getMessage());
            }
            int[] vari = null;
            try {
                vari = askPlayerCoordinates(p);
            } catch (Exception e) {
                markDisconnected(nick);
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
                            x.ask("SERVER: " + "seleziona indice da rimuovere");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in addGoods: " + e.getMessage());
                        }
                        int tmpint = 0;
                        try {
                            tmpint = askPlayerIndex(p);
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
                        }
                        Colour tmp = c.getListOfGoods().get(tmpint - 1);
                        c.removeGood(tmpint-1);
                        list.add(tmp);
                    }
                    try {
                        x.inform("SERVER: " + "seleziona la merce da inserire");
                    } catch (IOException e) {
                        markDisconnected(nick);
                    } catch (Exception e){
                        markDisconnected(nick);
                        System.err.println("[ERROR] in addGoods: " + e.getMessage());
                    }
                    int tmpint = 0;
                    try {
                        tmpint = askPlayerIndex(p);
                    } catch (Exception e) {
                        markDisconnected(nick);
                        throw new RuntimeException(e);
                    }
                    c.addGood(list.get(tmpint-1));
                }
                default -> {
                    try {
                        x.inform("SERVER: " + "cella non valida");
                    } catch (IOException e) {
                        markDisconnected(nick);
                    } catch (Exception e){
                        markDisconnected(nick);
                        System.err.println("[ERROR] in addGoods: " + e.getMessage());
                    }
                }


            }
            try {
                if(!x.ask("SERVER: " + "Vuoi continurare")) flag = false;
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in addGoods: " + e.getMessage());
            }
        }
    }

    public void addHuman() throws BusinessLogicException {
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
                                    try {
                                        if(askPlayerDecision("alien",p)){
                                            Human tmp2 = Human.PURPLE_ALIEN;
                                            h.addHuman(tmp2);
                                        }else{
                                            Human tmp2 = Human.HUMAN;
                                            for(int z = 0 ; z<2 ; z++) h.addHuman(tmp2);
                                        }
                                    } catch (BusinessLogicException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                                case BROWN_ALIEN -> {
                                    try {
                                        if(askPlayerDecision("alien",p)){
                                            Human tmp2 = Human.BROWN_ALIEN;
                                            h.addHuman(tmp2);
                                        }else{
                                            Human tmp2 = Human.HUMAN;
                                            for(int z = 0 ; z<2 ; z++) h.addHuman(tmp2);
                                        }
                                    } catch (BusinessLogicException e) {
                                        throw new RuntimeException(e);
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

    public void removeCrewmates(Player p, int num) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        int totalCrew = getNumCrew(p);
        if (num >= totalCrew) {
            p.setEliminated();
        } else {
            while (num > 0) {
                if(p.isConnected()){
                    try {
                        x.inform("SERVER: " + "seleziona un Housing unit");
                    } catch (IOException e) {
                        markDisconnected(nick);
                    } catch (Exception e){
                        markDisconnected(nick);
                        System.err.println("[ERROR] in removeCrewmates: " + e.getMessage());
                    }
                    int[] vari = askPlayerCoordinates(p);
                    Tile y = p.getTile(vari[0], vari[1]);
                    switch (y){
                        case HousingUnit h -> {
                            if(h.returnLenght()>0){
                                int tmp = h.removeHumans(0);
                                if(tmp == 2) p.setBrownAlien();
                                if(tmp == 3) p.setPurpleAlien();
                                num--;
                            }else{
                                try{
                                    x.inform("SERVER: " + "seleziona una housing unit valida");
                                    x.printPlayerDashboard(p.getDashMatrix());
                                } catch (IOException e) {
                                    markDisconnected(nick);
                                } catch (Exception e){
                                    markDisconnected(nick);
                                    System.err.println("[ERROR] in removeCrewmates " + e.getMessage());
                                }
                            }
                        }
                        default -> {
                            try {
                                x.inform("SERVER: " + "seleziona una abitazione valida");
                                x.printPlayerDashboard(p.getDashMatrix());
                            } catch (IOException e) {
                                markDisconnected(nick);
                            } catch (Exception e){
                                markDisconnected(nick);
                                System.err.println("[ERROR] in removeCrewmates: " + e.getMessage());
                            }
                        }
                    }
                    try {
                        viewsByNickname.get(nick).printPlayerDashboard(playersByNickname.get(nick).getDashMatrix());
                    } catch (Exception e) {
                        markDisconnected(nick);
                    }
                }else{
                    Tile[][] tmpDash = p.getDashMatrix();
                    for(int i = 0 ; i<5; i++){
                        for(int j = 0 ; j<7; j++){
                            switch (tmpDash[i][j]){
                                case HousingUnit h -> {
                                    if(h.returnLenght()>0){
                                        int tmp = h.removeHumans(1);
                                        if(tmp == 2) p.setBrownAlien();
                                        if(tmp == 3) p.setPurpleAlien();
                                        num--;
                                    }
                                }
                                default ->{}
                            }
                        }
                    }
                }
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
                            int index;
                            try {
                                if(p.isConnected()){
                                    index = askPlayerIndex(p);
                                }else{
                                    index = 0;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            int x = c.removeHumans(index);
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
    public boolean isProtected(String nick, int d) throws BusinessLogicException {
        boolean flag = false;
        VirtualView x = viewsByNickname.get(nick);

        while (!flag) {
            boolean ans = false;
            try {
                ans = x.ask("SERVER: " + "vuoi usare uno scudo?");
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in isProtected: " + e.getMessage());
            }
            if (ans) {
                int[] coordinate = askPlayerCoordinates(playersByNickname.get(nick));
                Tile y = playersByNickname.get(nick).getTile(coordinate[0], coordinate[1]);
                switch (y) {
                    case Shield shield -> {
                        if (!(shield.getProtectedCorner(d) == 8)) {
                            try {
                                x.inform("SERVER: " + "seleziona un'altro scudo");
                            } catch (IOException e) {
                                markDisconnected(nick);
                            } catch (Exception e){
                                markDisconnected(nick);
                                System.err.println("[ERROR] in isProtected: " + e.getMessage());
                            }
                        } else {
                            return manageEnergyCell(nick);
                        }
                    }
                    default ->{
                        try{
                            x.inform("SERVER: " + "cella non valida");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in isProtected: " + e.getMessage());
                        }
                    }
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
    public void defenceFromCannon(int dir, boolean type, int dir2, Player p) throws BusinessLogicException {


        String direction = "";
        int direction2 = dir2;
        switch (dir) {
            case 0 -> {
                direction = "Nord";
                direction2 = dir2+4;
            }
            case 1 -> {
                direction = "East";
                direction2 = dir2+5;
            }
            case 2 -> {
                direction = "South";
                direction2 = dir2+4;
            }
            case 3 -> {
                direction = "West";
                direction2 = dir2+5;
            }
        }
        String nick = getNickByPlayer(p);
        Tile[][] tmpDash = p.getDashMatrix();
        try {
            viewsByNickname.get(nick).inform("the attack is coming from "+direction+"  on the section  "+direction2);
            viewsByNickname.get(nick).inform(" SHIP BEFORE THE ATTACK ");
            viewsByNickname.get(nick).printPlayerDashboard(tmpDash);
        } catch (Exception e) {
            markDisconnected(nick);
        }
        if (dir == 0) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    scriptOfDefence(nick , tmpDash , dir2);
                } else {
                    try {
                        viewsByNickname.get(nick).inform("you are safe");
                    } catch (Exception e) {
                        markDisconnected(nick);
                    }
                }
            }
        } else if (dir == 2) {
            if (dir2 > 3 && dir2 < 11) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    scriptOfDefence(nick , tmpDash , dir2);
                } else {
                    try {
                        viewsByNickname.get(nick).inform("you are safe");
                    } catch (Exception e) {
                        markDisconnected(nick);
                    }
                }
            }
        } else if (dir == 1) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    scriptOfDefence(nick , tmpDash , dir2);
                } else {
                    try {
                        viewsByNickname.get(nick).inform("you are safe");
                    } catch (Exception e) {
                        markDisconnected(nick);
                    }
                }
            }
        } else if (dir == 3) {
            if (dir2 > 4 && dir2 < 10) {
                if (type || (!isProtected(nick, dir) && !type)) {
                    scriptOfDefence(nick , tmpDash , dir2);
                } else {
                    try {
                        viewsByNickname.get(nick).inform("you are safe");
                    } catch (Exception e) {
                        markDisconnected(nick);
                    }
                }
            }
        }

    }


    private void scriptOfDefence(String Nickname , Tile[][] tmpDash , int dir2) throws BusinessLogicException {
        Player p = getPlayerByNickname(Nickname);
        if(Arrays.deepEquals(tmpDash, p.getDashMatrix())){
            try {
                viewsByNickname.get(Nickname).printPlayerDashboard(p.getDashMatrix());
                viewsByNickname.get(Nickname).inform("safe");
            } catch (Exception e) {
                markDisconnected(Nickname);
            }
        }else{
            p.removeFrom0(dir2);
            askStartHousingForControl(Nickname);
            try {
                viewsByNickname.get(Nickname).printPlayerDashboard(p.getDashMatrix());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     *
     * @param dir  cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
    public void defenceFromMeteorite(int dir, boolean type, int dir2) throws BusinessLogicException {

        String direction = "";
        int direction2 = dir2;
        switch (dir) {
            case 0 -> {
                direction = "Nord";
                direction2 = dir2+4;
            }
            case 1 -> {
                direction = "East";
                direction2 = dir2+5;
            }
            case 2 -> {
                direction = "South";
                direction2 = dir2+4;
            }
            case 3 -> {
                direction = "West";
                direction2 = dir2+5;
            }
        }

        for (String nick : playersByNickname.keySet()) {

            Tile[][] tmpDash = playersByNickname.get(nick).getDashMatrix();
            try {
                viewsByNickname.get(nick).inform("the attack is coming from "+direction+"on the section"+direction2);
                viewsByNickname.get(nick).inform("ship before the attack");
                viewsByNickname.get(nick).printPlayerDashboard(tmpDash);
            } catch (Exception e) {
                markDisconnected(nick);
            }
            if (dir == 0) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && !checkProtection(dir, dir2, nick)) {
                            scriptOfDefence(nick , tmpDash , dir2);
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                    if (!type && playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
                        if (!isProtected(nick,dir)) {
                            scriptOfDefence(nick , tmpDash , dir2);
                        }
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                }
            } else if (dir == 2) {
                if (dir2 > 3 && dir2 < 11) {
                    if (type && checkProtection(dir, dir2, nick)) {
                        scriptOfDefence(nick , tmpDash , dir2);
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                    if (!type && !playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
                        if (!isProtected(nick, dir)) {
                            scriptOfDefence(nick , tmpDash , dir2);
                        }
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                }
            } else if (dir == 1 || dir == 3) {
                if (dir2 > 4 && dir2 < 10) {
                    if (type && !checkProtection(dir, dir2, nick)) {
                        scriptOfDefence(nick , tmpDash , dir2);
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                    if (!type && !playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
                        if (!isProtected(nick, dir)) {
                            scriptOfDefence(nick , tmpDash , dir2);
                        }
                    }else {
                        try {
                            viewsByNickname.get(nick).inform("you are safe");
                        } catch (Exception e) {
                            markDisconnected(nick);
                        }
                    }
                }
            }
        }
    }

    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(String nick) throws BusinessLogicException {
        VirtualView x = getViewCheck(nick);
        //Caso disconnesso WorstCase scenario: non attivo i doppi motori
        Player player = getPlayerCheck(nick);
        if(!player.isConnected()) return false;
        int[] coordinates = new int[2];
        boolean exits = false;

//        boolean use = false;
//        try {
//            use = askPlayerDecision("vuoi usare una batteria?",player);
//        } catch (Exception e) {
//            markDisconnected(nick);
//            throw new RuntimeException(e);
//        }
        if (!askPlayerDecision("SERVER: " + "Do you want to use a battery?", player)) {
            return false;
        } else {
            while (!exits) {
                /*
                try {
                    coordinate = x.askCoordinate();
                } catch (Exception e) {
                    markDisconnected(nick);
                    throw new RuntimeException(e);
                }
                 */
                coordinates = askPlayerCoordinates(player);
                if(coordinates == null) return false;

                Tile p = playersByNickname.get(nick).getTile(coordinates[0], coordinates[1]);
                switch (p) {
                    case EnergyCell c -> {
                        int capacity = c.getCapacity();
                        if (capacity == 0) {
                            /*
                            try {
                                if (!x.ask("Vuoi selezionare un'altra cella?")) {
                                    return false;
                                }
                            } catch (Exception e) {
                                markDisconnected(nick);
                                throw new RuntimeException(e);
                            }
                             */
                            if(!askPlayerDecision("SERVER: " + "Do you want to select another EnergyCell?", player))
                                return false;
                        } else {
                            c.useBattery();
                            return true;
                        }
                    }
                    default -> {
                        try{
                            x.inform("Cella non valida");
                        } catch (IOException e) {
                            markDisconnected(nick);
                        } catch (Exception e){
                            markDisconnected(nick);
                            System.err.println("[ERROR] in menageEnegryCell: " + e.getMessage());
                        }
                        /*
                        try {
                            if (!x.ask("vuoi selezionare un'altra cella?")) {
                                exits = true;
                            }
                        } catch (Exception e) {
                            markDisconnected(nick);
                            throw new RuntimeException(e);
                        }
                        */
                        if(!askPlayerDecision("SERVER: " + "Do you want to select another EnergyCell?", player))
                            exits = true;
                    }
                }
            }

            return false;
        }
    }

    public boolean checkProtection(int dir, int dir2, String player) throws BusinessLogicException {
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
        if(p.isConnected()){
            int[] xy;
            try {
                v.inform("SERVER: " + "choose your starting housing unit");
                xy = askPlayerCoordinates(p);
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
                            v.inform("SERVER: " + "Position non valid , choose another tile");
                            xy = askPlayerCoordinates(p);
                        } catch (IOException e) {
                            markDisconnected(nickname);
                        } catch (Exception e){
                            markDisconnected(nickname);
                            System.err.println("[ERROR] in askStartHousingForControl: " + e.getMessage());
                        }
                    }
                }
            }
            checkPlayerAssembly(nickname,  xy[0], xy[1]);
        }else{
            checkPlayerAssembly(nickname,  2,3);
        }


    }

    private void checkPlayerAssembly(String id , int x , int y) throws BusinessLogicException {
        playersByNickname.get(id).controlAssembly(x,y);
        try {
            //notifyView(id);
            viewsByNickname.get(id).printPlayerDashboard(playersByNickname.get(id).getDashMatrix());
        } catch (IOException e) {
            markDisconnected(id);
        } catch (Exception e){
            markDisconnected(id);
            System.err.println("[ERROR] in checkPlayerAssembly: " + e.getMessage());
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public FlightCardBoard getFlightCardBoard() {
        return fBoard;
    }

    /**
     * The following method activates the effect of a card. Then, it eliminates any possible overlapped players
     * after the application of the effect, and reorders the list of players in order of lap and position
     * on the flight board.
     *
     * @param card card
     */

    public void activateCard(Card card) throws BusinessLogicException {
        CardEffectVisitor visitor = new CardEffectVisitor(this);
        card.accept(visitor);

        fBoard.setOverlappedPlayersEliminated();
        List<Player> eliminated = fBoard.eliminatePlayers();
        for (Player player : eliminated) handleElimination(player);
        fBoard.orderPlayersInFlightList();
    }

    /**
     * The following method merges all four small decks for lvl 2 flight into a single one.
     */
    public void mergeDecks (){
        for(Deck d : decks){
            deck.addAll(d.getCards());
        }
        deck.shuffle();
    }


    //metodi da inserire nel card visitor per gestione tui

    public void changePhaseFromCard(String nick, Player p, GamePhase tmp){
        if(p.isConnected()){
            try {
                viewsByNickname.get(nick).updateGameState(tmp);
                //notifyView(nick);
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in changePhaseFromCard: " + e.getMessage());
            }
        }
    }

    public void changeMapPosition(String nick, Player p) throws BusinessLogicException {
        playersPosition.put(nick, p.getPos());
    }

    public void updatePositionForEveryBody() throws BusinessLogicException {
        for(String nick : playersPosition.keySet()){
            Player p = getPlayerCheck(nick);
            if(p.isConnected()){
                try {
                    viewsByNickname.get(nick).updateMapPosition(playersPosition);
                } catch (IOException e) {
                    markDisconnected(nick);
                } catch (Exception e){
                    markDisconnected(nick);
                    System.err.println("[ERROR] in updatePositionForEverybody: " + e.getMessage());
                }
            }
        }
        //notifyAllViews();
    }

    public void setExit() {
        // metti in EXIT tutti i player
        playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.EXIT));

        // notifica tutte le view dell’EXIT
        viewsByNickname.forEach((nick, view) -> {
            try {
                view.updateGameState(GamePhase.EXIT);
            } catch (Exception e) {
                // se qualche client è già caduto, marcallo disconnected
                markDisconnected(nick);
            }
        });

        // infine avvisa il GameManager che la partita è finita
        onGameEnd.accept(gameId);
    }

    public Map<String,Integer> getPlayersPosition(){
        return playersPosition;
    }
}

