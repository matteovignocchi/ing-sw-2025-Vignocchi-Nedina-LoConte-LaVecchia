package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
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

//TODO: discorso gestione disconnessioni nei metodi di oleg. disconnetto e poi continuo con esecuzione, o devo returnare. (mandare mail).
//TODO: sostituire ove possibile v.inform con chiamate a inform metodo controller
//TODO: capire le eccezione nei metodi che usano i parser!! Gestite male, soprattutto per il discorso markdisconnected

//TODO: pulire il codice

public class Controller implements Serializable {
    private final int gameId;
    public transient Map<String, VirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> playersByNickname = new ConcurrentHashMap<>();
    private Map<String , int[] > playersPosition = new ConcurrentHashMap<>();

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
    private transient CardSerializer cardSerializer;
    private transient TileSerializer tileSerializer;
    private transient EnumSerializer enumSerializer;
    private transient ScheduledExecutorService pingScheduler;



    public Controller(boolean isDemo, int gameId, int MaxPlayers, Consumer<Integer> onGameEnd, Set<String> loggedInUsers) throws CardEffectException, IOException {
        if(isDemo) {
            fBoard = new FlightCardBoard();
            DeckManager deckCreator = new DeckManager();
            //TODO: commentato per debugging. ripristinare una volta finito
            deck = deckCreator.CreateDemoDeck();
//            deck = deckCreator.CreateMixedDemoDeck();
        }else{
            fBoard = new FlightCardBoard2();
            DeckManager deckCreator = new DeckManager();
            //TODO: commentato per debugging. ripristinare una volta finito
            //decks = deckCreator.CreateSecondLevelDeck();
            decks = deckCreator.CreateMeteoritesDeck();
            deck = new Deck();
        }
        this.cardSerializer = new CardSerializer();
        this.tileSerializer = new TileSerializer();
        this.enumSerializer = new EnumSerializer();
        this.gameId = gameId;
        this.onGameEnd = onGameEnd;
        initPingScheduler();
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

    private void initPingScheduler() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PingScheduler-" + gameId);
            t.setDaemon(true);
            return t;
        });
        // ping ogni 10 secondi
        pingScheduler.scheduleAtFixedRate(this::pingAllClients, 10, 10, TimeUnit.SECONDS);
    }

    private void pingAllClients() {
        String phase;
        try {
            phase = enumSerializer.serializeGamePhase(getGamePhaseForAll());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("[PING] errore serializzazione fase: " + e.getMessage());
            phase = "PING";
        }

        for (var entry : viewsByNickname.entrySet()) {
            String nick = entry.getKey();
            VirtualView v = entry.getValue();
            try {
                v.updateGameState("PING");
            } catch (Exception e) {
                markDisconnected(nick);
            }
        }
    }

    public void shutdownPing() {
        if (pingScheduler != null) pingScheduler.shutdownNow();
    }

    private GamePhase getGamePhaseForAll() {
        return playersByNickname.values().iterator().next().getGamePhase();
    }

    public void notifyView(String nickname) {
        VirtualView v = viewsByNickname.get(nickname);
        Player p      = playersByNickname.get(nickname);
        try {
            v.updateGameState(enumSerializer.serializeGamePhase(p.getGamePhase()));
            Map<String,int[]> fullMap = buildPlayersPositionMap();
            v.updateMapPosition(fullMap);
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
            if(getPlayerCheck(nickname).getGamePhase()==GamePhase.TILE_MANAGEMENT) {

                String json = tileSerializer.toJson(p.getLastTile());
                v.setTile(json);}
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
        view.setIsDemo(isDemo);
        view.updateGameState(enumSerializer.serializeGamePhase(GamePhase.WAITING_IN_LOBBY));
        view.setTile(tileSerializer.toJson(p.getTile(2,3)));
        playersByNickname.put(nickname, p);
        viewsByNickname.put(nickname, view);
        playersPosition=buildPlayersPositionMap();

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

    public void inform(String msg, String nick){
        VirtualView v = viewsByNickname.get(nick);
        try{
            v.inform(msg);
        } catch (IOException e){
            markDisconnected(msg);
        } catch(Exception e){
            markDisconnected(msg);
            System.err.println("[ERROR] in inform: " + e);
        }
    }

    public void informAndNotify(String msg, String nick){
        VirtualView v = viewsByNickname.get(nick);
        try{
            v.inform(msg);
            notifyView(nick);
        } catch (IOException e){
            markDisconnected(msg);
        } catch(Exception e){
            markDisconnected(msg);
            System.err.println("[ERROR] in inform: " + e);
        }
    }

    public void broadcastInform(String msg) {
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            Player p = getPlayerByNickname(nickname);
            if(p.isConnected()) inform(msg, nickname);
        }
    }

    public void printPlayerDashboard (VirtualView v, Player p, String nick) {
        try{
            v.printPlayerDashboard(tileSerializer.toJsonMatrix(p.getDashMatrix()));
        } catch (IOException e) {
            markDisconnected(nick);
        } catch (Exception e){
            markDisconnected(nick);
            System.err.println("[ERROR] in addGoods: " + e.getMessage());
        }
    }

    public void printListOfGoods (List<Colour> list, String nick) throws BusinessLogicException {
        VirtualView v = getViewCheck(nick);
        try{
            v.printListOfGoods(enumSerializer.serializeColoursList(list));
        } catch (IOException e) {
            markDisconnected(nick);
        } catch (Exception e) {
            markDisconnected(nick);
            System.err.println("[ERROR] in printListOfGoods : " + e.getMessage());
        }
    }

    public void updateGamePhase(String nick, VirtualView v, GamePhase phase){
        try {
            v.updateGameState(enumSerializer.serializeGamePhase(phase));
        } catch (IOException ex) {
            markDisconnected(nick);
        } catch (Exception e) {
            markDisconnected(nick);
            System.err.println("[ERROR] in updateGamePhase: " + e.getMessage());
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
            broadcastInform("SERVER: "+ nickname + " is reconnected");
        }
        cancelLastPlayerTimeout();
        notifyView(nickname);
    }

    public void handleElimination(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);

        //TODO: capire la fase di eliminazione
        p.setGamePhase(GamePhase.WAITING_FOR_TURN);

        String msg = "\nSERVER: You have been eliminated!";
        informAndNotify(msg, nick);
        String msg1 = "\nSERVER: Player "+nick+" has been eliminated!";
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            if(nickname.equals(nick)) continue;
            Player player = getPlayerByNickname(nickname);
            if(player.isConnected()) inform(msg1, nickname);
        }
    }

    public void reinitializeAfterLoad(Consumer<Hourglass> hourglassListener) {
        this.viewsByNickname = new ConcurrentHashMap<>();
        this.hourglass = new Hourglass(hourglassListener);
        this.cardSerializer = new CardSerializer();
        this.tileSerializer = new TileSerializer();
        this.enumSerializer = new EnumSerializer();
        initPingScheduler();
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
                    String msg = "SERVER: " + "You win by timeout!";
                    inform(msg, winner);
                }
                onGameEnd.accept(gameId);
            }, 1, TimeUnit.MINUTES);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 1
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void startGame() {
        playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.BOARD_SETUP));
        Map<String,int[]> fullMap = buildPlayersPositionMap();

        viewsByNickname.forEach((nick, v) -> {
            try {
                v.updateMapPosition(fullMap);
                v.setIsDemo(isDemo);
                v.updateGameState(enumSerializer.serializeGamePhase(GamePhase.BOARD_SETUP));
                v.setTile(tileSerializer.toJson(getPlayerCheck(nick).getTile(2,3)));
                v.printPlayerDashboard(tileSerializer.toJsonMatrix(getPlayerCheck(nick).getDashMatrix()));
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println(" in startGame: " + e.getMessage());
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

    public String getCoveredTile(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        int size = getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        Tile t = getTile(1);
        p.setLastTile(t);

        p.setGamePhase(GamePhase.TILE_MANAGEMENT);
        try {
            viewsByNickname.get(nickname).updateGameState(enumSerializer.serializeGamePhase(GamePhase.TILE_MANAGEMENT));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        notifyView(nickname);

        try {
            return tileSerializer.toJson(t);
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
    }

    public String chooseUncoveredTile(String nickname, int idTile) throws BusinessLogicException {
        List<Tile> uncoveredTiles = getShownTiles();
        Optional<Tile> opt = uncoveredTiles.stream().filter(t -> t.getIdTile() == idTile).findFirst();
        if(opt.isEmpty()) throw new BusinessLogicException("Tile already taken");
        if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("No tiles found");

        Player p = getPlayerCheck(nickname);
        Tile t = getShownTile(uncoveredTiles.indexOf(opt.get()));
        p.setLastTile(t);

        p.setGamePhase(GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);
        try {
            return tileSerializer.toJson(t);
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
    }

    public void dropTile (String nickname, String tile) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        try {
            addToShownTile(tileSerializer.fromJson(tile));
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
        p.setGamePhase(GamePhase.BOARD_SETUP);
        try {
            viewsByNickname.get(nickname).updateGameState(enumSerializer.serializeGamePhase(GamePhase.BOARD_SETUP));
        } catch (Exception e) {
            System.err.println("dropTile: " + e.getMessage());
        }
        notifyView(nickname);
    }

    public void placeTile(String nickname, String tile, int[] cord) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        try {
            p.addTile(cord[0], cord[1], tileSerializer.fromJson(tile));
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
        p.setGamePhase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    public String getReservedTile(String nickname , int id) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        List<Tile> discardPile = p.getTilesInDiscardPile();
        for(Tile t : discardPile) {
            if(t.getIdTile() == id) {
                discardPile.remove(t);
                p.resetValidity(t.getIdTile());
                p.setLastTile(t);
                p.setGamePhase(GamePhase.TILE_MANAGEMENT_AFTER_RESERVED);
                notifyView(nickname);
                try {
                    return tileSerializer.toJson(t);
                } catch (JsonProcessingException e) {
                    throw new BusinessLogicException(e.getMessage());
                }
            }
        }
        throw new BusinessLogicException("Tile not found");
    }

    public void setReady(String nickname) throws BusinessLogicException, RemoteException {
        Player p = getPlayerCheck(nickname);

        getFlightCardBoard().setPlayerReadyToFly(p, isDemo);

        p.setGamePhase(GamePhase.WAITING_FOR_PLAYERS);

        playersPosition = buildPlayersPositionMap();

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
            playersPosition = buildPlayersPositionMap();
        }

        broadcastInform("SERVER: " + "Flight started!");

        //TODO: queste stampe da eliminare per debug
        /**/for(Player p: playersByNickname.values()) System.out.println("1 VOLTA: Player "+getNickByPlayer(p)+" num di discard tiles: "+
                p.checkDiscardPile());

        for (String nick : viewsByNickname.keySet()) checkPlayerAssembly(nick, 2, 3);

        /**/for(Player p: playersByNickname.values()) System.out.println("2 VOLTA: Player "+getNickByPlayer(p)+" num di discard tiles: "+
                p.checkDiscardPile());

        addHuman();

        playersByNickname.forEach( (s, p) -> p.setGamePhase(GamePhase.WAITING_FOR_TURN));

        activateDrawPhase();
    }

    public void activateDrawPhase() throws BusinessLogicException {
        List<Player> candidates = fBoard.getOrderedPlayers().stream()
                .filter(Player::isConnected)
                .toList();

        if(candidates.isEmpty()) throw new BusinessLogicException("No player connected");

        String leaderNick = null;
        for(Player leader : candidates) {
            leaderNick = getNickByPlayer(leader);
            VirtualView v = viewsByNickname.get(leaderNick);
            leader.setGamePhase(GamePhase.DRAW_PHASE);
            try {
                v.inform("\nSERVER: " + "You're the leader! Draw a card");
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
            if(nickname.equals(leaderNick)) continue;
            Player p = getPlayerCheck(nickname);
            if(p.isEliminated()) continue;
            p.setGamePhase(GamePhase.WAITING_FOR_TURN);
            if(p.isConnected()) notifyView(nickname);
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
                    String msg = "\nSERVER: " + "You cannot flip the hourglass: It's still running";
                    inform(msg, nickname);
                }
                break;
            case 2:
                if(state == HourglassState.ONGOING){
                    String msg = "\nSERVER: " + "You cannot flip the hourglass: It's still running";
                    inform(msg, nickname);
                } else if (state == HourglassState.EXPIRED && p.getGamePhase() == GamePhase.WAITING_FOR_PLAYERS) {
                    hourglass.flip();
                    broadcastInform("\nSERVER: " + "Hourglass flipped the last time!");
                } else {
                    String msg = "\nSERVER: You cannot flip the hourglass for the last time: " +
                            "You are not ready";
                    inform(msg, nickname);
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

    public void drawCardManagement(String nickname) throws BusinessLogicException {
        Card card = deck.draw();
        
        Player drawer = getPlayerCheck(nickname);
        drawer.setGamePhase(GamePhase.WAITING_FOR_TURN);
        updateGamePhase(nickname, getViewCheck(nickname), GamePhase.WAITING_FOR_TURN); //omettibile

        broadcastInform("\nSERVER: " + "Card drawn!");

        for(Map.Entry<String, VirtualView> entry :viewsByNickname.entrySet()){
            String nick = entry.getKey();
            VirtualView v = entry.getValue();
            Player p = getPlayerCheck(nick);
            if(!p.isEliminated()) p.setGamePhase(GamePhase.CARD_EFFECT);

            if(p.isConnected()){
                if(!p.isEliminated()) updateGamePhase(nick, v, GamePhase.CARD_EFFECT);
                try {
                    v.printCard(cardSerializer.toJSON(card));
                } catch (IOException e) {
                    markDisconnected(nick);
                } catch (Exception e){
                    markDisconnected(nick);
                    System.err.println("[ERROR] in drawCardManagement: "+e.getMessage());
                }
            }
        }

        activateCard(card);
        broadcastInform("SERVER: end of card's effect");

        if(deck.isEmpty()){
            broadcastInform("SERVER: All cards drawn");
            startAwardsPhase();
        } else if (fBoard.getOrderedPlayers().isEmpty()){
            broadcastInform("SERVER: All players eliminated");
            startAwardsPhase();
        }else {
            List<String> inFlight = playersByNickname.entrySet().stream()
                    .filter(e -> !e.getValue().isEliminated())
                    .map(Map.Entry::getKey)
                    .toList();


            ExecutorService exec = Executors.newFixedThreadPool(inFlight.size());
            try {
                Map<String,Future<Boolean>> futures = new HashMap<>();
                for(String nick : inFlight){
                    Player p = playersByNickname.get(nick);
                    if(p.isConnected() && !p.isEliminated()){
                        futures.put(nick, exec.submit(() ->
                                askPlayerDecision("\nSERVER: Do you want to abandon the flight? ", p)
                        ));
                    }
                }

                Map<String, Boolean> decisions = new HashMap<>();
                for(var e : futures.entrySet()){
                    String nick = e.getKey();
                    Future<Boolean> f = e.getValue();
                    try {
                        decisions.put(nick, f.get());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        decisions.put(nick, false);
                    } catch (ExecutionException ee) {
                        markDisconnected(nick);
                        decisions.put(nick, false);
                    }
                }

                for(var e : decisions.entrySet()){
                    if(e.getValue()){
                        Player p = playersByNickname.get(e.getKey());
                        p.setEliminated();
                        handleElimination(p);
                    }
                }
            } finally {
                exec.shutdownNow();
            }

            fBoard.eliminatePlayers();
            fBoard.orderPlayersInFlightList();

            if(fBoard.getOrderedPlayers().isEmpty()){
                broadcastInform("SERVER: All players eliminated");
                startAwardsPhase();
            } else {
                inFlight =  playersByNickname.entrySet().stream()
                        .filter(e -> !e.getValue().isEliminated())
                        .map(Map.Entry::getKey)
                        .toList();

                for (String nick : inFlight) {
                    Player p = playersByNickname.get(nick);
                    p.setGamePhase(GamePhase.WAITING_FOR_TURN);
                    if(p.isConnected()){
                        VirtualView v = viewsByNickname.get(nick);
                        updateGamePhase(nick, v, GamePhase.WAITING_FOR_TURN);
                    }
                }

                activateDrawPhase();
            }
        }
    }


    public void startAwardsPhase() throws BusinessLogicException {

        broadcastInform("\nSERVER: Flight ended! Time to collect rewards!");

        int malusBrokenTile = fBoard.getBrokenMalus();
        int bonusBestShip = fBoard.getBonusBestShip();
        int redGoodBonus = fBoard.getBonusRedCargo();
        int yellowGoodBonus = fBoard.getBonusYellowCargo();
        int greenGoodBonus = fBoard.getBonusGreenCargo();
        int blueGoodBonus = fBoard.getBonusBlueCargo();
        int[] arrivalBonus = {fBoard.getBonusFirstPosition(), fBoard.getBonusSecondPosition(),
                fBoard.getBonusThirdPosition(), fBoard.getBonusFourthPosition()};
        List<Player> orderedPlayers = fBoard.getOrderedPlayers();

        /**/System.out.printf("%d %d %d %d %d %d %s%n", malusBrokenTile, bonusBestShip, redGoodBonus, yellowGoodBonus, greenGoodBonus, blueGoodBonus, Arrays.toString(arrivalBonus));

        int minExpConnectors = playersByNickname.values().stream()
                .mapToInt(Player::countExposedConnectors)
                .min()
                .orElseThrow( () -> new IllegalArgumentException("No Player in Game"));
        List<Player> bestShipPlayers = playersByNickname.values().stream()
                .filter(p -> p.countExposedConnectors() == minExpConnectors)
                .toList();

        /**/System.out.printf("Connettori minimi esposti %s%n", minExpConnectors);
        /**/for(Player p: bestShipPlayers) System.out.println("Best ship player: "+getNickByPlayer(p));

        /**/for (Player p : playersByNickname.values()) System.out.println("Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

        for (int i = 0; i < orderedPlayers.size(); i++) {
            orderedPlayers.get(i).addCredits(arrivalBonus[i]);
        }

        /**/for (Player p : playersByNickname.values()) System.out.println("AFTER ARRIVAL BONUS: Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

        for (Player p : bestShipPlayers) {
            p.addCredits(bonusBestShip);
        }

        /**/for (Player p : playersByNickname.values()) System.out.println("AFTER BESTSHIP BONUS: Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

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

            /**/System.out.println("Crediti bonus per merce: "+totalDouble);

            /**/ System.out.println("BEFORE GOODS CREDTIS: Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

            p.addCredits((int) Math.ceil(totalDouble));

            /**/ System.out.println("AFTER GOODS CREDTIS: Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

            int numBrokenTiles = p.checkDiscardPile();

            /**/ System.out.println("Numero di tessere distrutte: "+numBrokenTiles);

            p.addCredits(numBrokenTiles * malusBrokenTile);

            /**/ System.out.println("AFTER BROKEN TILES: Player "+getNickByPlayer(p)+" credits: "+p.getCredits());

            String nick = getNickByPlayer(p);
            VirtualView v = viewsByNickname.get(nick);
            int totalCredits = p.getCredits();
            p.setGamePhase(GamePhase.EXIT);

            if(p.isConnected()){
                if(totalCredits>0) inform("SERVER: " + "Your total credits are: " + totalCredits + " You won!", nick);
                else inform("SERVER: " + "Your total credits are: " + totalCredits + " You lost!", nick);

                informAndNotify("SERVER: " + "Game over. Thank you for playing!", nick);
            }
        }

        onGameEnd.accept(this.gameId);
    }

    public String showDeck (int idxDeck){
        try {
            return cardSerializer.toJsonList(new ArrayList<>(decks.get(idxDeck).getCards()));
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in showDeck: " + e.getMessage());
        }
        return null;
    }

    public String[][] lookAtDashBoard(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        try {
            return tileSerializer.toJsonMatrix(p.getDashMatrix());
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GESTIONE MODEL 2
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean askPlayerDecision(String question, Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if (!p.isConnected()) return false;

        try {
            return v.askWithTimeout(question);
        } catch (IOException e) {
            System.err.println("[WARN] IOException in askPlayerDecision for " + nick + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            markDisconnected(nick);
            System.err.println("Error in askPlayerDecision: " + e);
            return false;
        }

    }

    public int[] askPlayerCoordinates (Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if(!p.isConnected()) return null;

        try {
            return v.askCoordsWithTimeout();
        } catch (IOException e) {
            markDisconnected(nick);
        } catch(Exception e){
            markDisconnected(nick);
            System.err.println("Error in askPlayerDecision");
        }
        return null;
    }

    public Integer askPlayerIndex(Player p, int len) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if (!p.isConnected()) return null;

        while (true) {
            try {
                Integer idx = v.askIndexWithTimeout();

                if (idx == null) return null;

                if (idx < 0 || idx >= len) {
                    inform("SERVER: Index out of range. Please try again", nick);
                    continue;
                }
                return idx;
            } catch (IOException e) {
                markDisconnected(nick);
                return null;
            } catch (Exception e) {
                markDisconnected(nick);
                System.err.println("Error in askPlayerIndex");
                return null;
            }
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
    public String jsongetShownTiles(){
        try {
            if(shownTile.isEmpty()) return "PIEDONIPRADELLA";
            return tileSerializer.toJsonList(shownTile);
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in jsongetShownTiles: " + e);
        }
        return null;
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
                            String mex = "To activate a double engine";
                            boolean activate = manageEnergyCell(nick, mex);
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
                    case Cannon c -> {
                        var = c.isDouble();
                        if (var) {
                            String mex = "To activate a double cannon";
                            boolean activate = manageEnergyCell(nick, mex);
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
                    case Cannon c -> {
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


    public boolean manageIfPlayerEliminated(Player p) {
        int tmp= p.getTotalHuman();
        if(tmp == 0){
            p.setEliminated();
            return true;
        }else{
            return false;
        }
    }

    //TODO questi tre sono i metodi per la autoCommand che mi permettono di eseguire i controlli automatici , vanno inserii nei casi di disconnessione


    public void autoCommandForRemoveGoods(Player p, int numOfGoods) throws BusinessLogicException {
        int flag = numOfGoods;
        if (flag<= 0) return;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile tmpTile = p.getTile(i, j);
                switch (tmpTile){
                    case StorageUnit c -> {
                        if(c.getListOfGoods().isEmpty()) continue;
                        else {
                            while(flag > 0 && !c.getListOfGoods().isEmpty()) {
                                c.removeGood(0);
                                flag--;
                            }
                        }
                    }
                    default -> {}
                }
                if(flag<=0) return;
            }
        }
    }

    public void autoCommandForRemoveSingleGood(Player p, Colour col) throws BusinessLogicException {
        for(int i=0; i<5; i++){
            for(int j=0; j<7; j++){
                Tile tmp = p.getTile(i, j);
                switch (tmp){
                    case StorageUnit c -> {
                        List<Colour> list = c.getListOfGoods();
                        if(list.isEmpty()) continue;
                        else {
                            for(int z=0; z<list.size(); z++){
                                if(list.get(z).equals(col)){
                                    c.removeGood(z);
                                    return;
                                }
                            }
                        }
                    }
                    default ->{}
                }
            }
        }
    }

    public void autoCommandForRemovePlayers(Player p, int num) throws BusinessLogicException {
        int flag = num;
        if (flag<= 0) return;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile tmpTile = p.getTile(i, j);
                switch (tmpTile){
                    case HousingUnit h -> {
                        if(h.getListOfToken().isEmpty()) continue;
                        else {
                            for(int z=0 ; z<h.getListOfToken().size() ; z++) {
                                h.removeHumans(0);
                                flag--;
                            }
                        }
                    }
                    default -> {}
                }
                if(flag<=0) return;
            }
        }
    }
    public void autoCommandForBattery(Player p, int num) throws BusinessLogicException {
        int flag = num;
        if (flag<= 0) return;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile tmpTile = p.getTile(i, j);
                switch (tmpTile){
                    case EnergyCell e -> {
                        if(e.getCapacity() == 0) continue;
                        else {
                            int size = e.getCapacity();
                            int z = 0;
                            while(flag > 0 && z<size){
                                e.useBattery();
                                flag--;
                                z++;
                            }
                        }
                    }
                    default -> {}
                }
                if(flag<=0) return;
            }
        }
    }


    //TODO: gestire caso null e vedere per il caso delle disconnessioni
    public void removeGoods(Player p, int num) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);
        int totalEnergy = getTotalEnergy(p);
        int totalGood = getTotalGood(p);
        boolean flag = false;

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

        //1° caso
        if(num == totalGood){
            autoCommandForRemoveGoods(p, totalGood);
            if(!p.isConnected()) return;
            inform("SERVER: You have lost all your goods", nick);

        //2° caso
        } else if(num < totalGood){
            if(!p.isConnected()) {
                autoCommandForRemoveGoods(p, num);
                return;
            }

            while(num != 0){
                if(r != 0){
                    if(p.isConnected()){
                        inform("SERVER: Select a storage unit to remove a red good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.RED);
                            r--;
                            num--;
                            inform("SERVER: Timeout! Automatic choice", nick);
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case StorageUnit c -> {
                                for(Colour co : c.getListOfGoods()) {
                                    if (co == Colour.RED) {
                                        r--;
                                        num--;
                                        c.removeGood(c.getListOfGoods().indexOf(co));
                                        flag=true;
                                        break;
                                    }
                                }
                                if(!flag) inform("SERVER: There are not red goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> inform("SERVER: Not valid cell. Try again", nick);
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
                } else if(g != 0){
                    if(p.isConnected()){
                        inform("SERVER: Select a storage unit to remove a yellow good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.YELLOW);
                            g--;
                            num--;
                            inform("SERVER: Timeout! Automatic choice", nick);
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case StorageUnit c -> {
                                for(Colour co : c.getListOfGoods()) {
                                    if (co == Colour.YELLOW) {
                                        g--;
                                        num--;
                                        c.removeGood(c.getListOfGoods().indexOf(co));
                                        flag=true;
                                        break;
                                    }
                                }
                                if(!flag) inform("SERVER: There are not yellow goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> inform("SERVER: Not valid cell. Try again", nick);
                        }
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
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
                        }

                    }
                }else if(v != 0){
                    if(p.isConnected()){
                        inform("SERVER: Select a storage unit to remove a green good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.GREEN);
                            v--;
                            num--;
                            inform("SERVER: Timeout! Automatic choice", nick);
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case StorageUnit c -> {
                                for(Colour co : c.getListOfGoods()) {
                                    if (co == Colour.GREEN) {
                                        v--;
                                        num--;
                                        c.removeGood(c.getListOfGoods().indexOf(co));
                                        flag=true;
                                        break;
                                    }
                                }
                                if(!flag) inform("SERVER: There are not green goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> inform("SERVER: Not valid cell. Try again", nick);
                        }
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
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
                        }
                    }
                } else if(b != 0){
                    if(p.isConnected()){
                        inform("SERVER: Select a storage unit to remove a blue good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.BLUE);
                            b--;
                            num--;
                            inform("SERVER: Timeout! Automatic choice", nick);
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case StorageUnit c -> {
                                for(Colour co : c.getListOfGoods()) {
                                    if (co == Colour.BLUE) {
                                        b--;
                                        num--;
                                        c.removeGood(c.getListOfGoods().indexOf(co));
                                        flag=true;
                                        break;
                                    }
                                }
                                if(!flag) inform("SERVER: There are not blue goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> inform("SERVER: Not valid cell. Try again", nick);
                        }
                    }else{
                        for(int index =0 ; index <5 ; index++){
                            for(int index2 =0 ; index2 <7 ; index2++){
                                Tile tmpTile = p.getTile(index, index2);
                                switch (tmpTile){
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
                }
            }
        }else { //3° caso: if(num > totalGood)
            autoCommandForRemoveGoods(p, totalGood);
            int finish = num-totalGood;

            if(!p.isConnected()) {
                autoCommandForBattery(p, finish);
                return;
            }

            if(totalGood > 0) inform("SERVER: You have lost all your goods", nick);
            else inform("SERVER: You don't have any good to remove", nick);

            if(finish < totalEnergy){
                inform("SERVER: You will lose "+finish+" battery/ies", nick);
                while(finish > 0){
                    if(p.isConnected()){
                        inform("SERVER: Select an energy cell to remove a battery from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForBattery(p, 1);
                            finish--;
                            inform("SERVER: Timeout! Automatic choice", nick);
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case EnergyCell c -> {
                                if(c.getCapacity() != 0){
                                    c.useBattery();
                                    finish--;
                                } else inform("SERVER: Empty energy cell. Try again", nick);
                            }
                            default -> inform("SERVER: Not valid cell. Try again", nick);
                        }
                    }else{
                        autoCommandForBattery(p, 1);
                    }
                }
            }else{
                autoCommandForBattery(p, totalEnergy);
                if(!p.isConnected()) return;

                if(totalEnergy>0){
                    inform("SERVER: You have lost all your batteries", nick);
                }
                else inform("SERVER: You don't have any batteries to remove", nick);
            }
        }
        printPlayerDashboard(x, p, nick);
    }

    //TODO: Gestione risposte predefinite per timeout,
    // gestione indici richiesti-coordinate fuori bunds
    public void manageGoods(Player p, List<Colour> list) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);
        boolean flag = true;

        while(flag){
            inform("SELECT:\n 1. Add good\n 2. Rearranges the goods\n 3. Trash some goods", nick);
            int tmp = askPlayerIndex(p, 3);

            //TODO: gestione timeout/disconnessione
            //TODO: gestire default

            switch (tmp){
                case 0 -> addGoods(p, x, list, nick);
                case 1 -> caseRedistribution(p, x, list, nick);
                case 2 -> caseRemove(p,x,nick);
                default -> {
                    inform("SERVER: Invalid choice. Please try again", nick);
                    continue;
                }
            }

            if(!askPlayerDecision("SERVER: Do you want to continue to manage your goods?", p)) flag = false;
        }
    }


    public void addGoods(Player p, VirtualView x, List<Colour> list, String nick) throws BusinessLogicException {
        boolean flag = true;
        Colour tempGood = null;

        if(list.isEmpty()){
            inform("SERVER: Empty list of goods", nick);
            return;
        }

        while (!list.isEmpty() && flag) {
            inform("SERVER: Select a storage unit", nick);
            printListOfGoods(list, nick);
            printPlayerDashboard(x, p, nick);

            int[] vari = askPlayerCoordinates(p);

            Tile t = p.getTile(vari[0], vari[1]);
            switch (t){
                case StorageUnit c -> {
                    if(c.isFull()){
                        inform("SERVER: Full Storage Unit\n SERVER: Select the index of the good in the storage unit to remove", nick);
                        List<Colour> listGoods = c.getListOfGoods();
                        printListOfGoods(listGoods, nick);
                        int tmpint = askPlayerIndex(p, listGoods.size());

                        Colour tmp = c.getListOfGoods().get(tmpint);
                        tempGood = c.removeGood(tmpint);
                        list.add(tmp);
                    }

                    inform("SERVER: Select the index of the good to place", nick);
                    printListOfGoods(list, nick);
                    int tmpint = askPlayerIndex(p, list.size());

                    if(list.get(tmpint) == Colour.RED){
                        if(c.isAdvanced()) {
                            c.addGood(list.get(tmpint));
                            list.remove(tmpint);
                        }
                        else {
                            inform("SERVER: You can't place a dangerous good in a not advanced storage unit", nick);
                            if(tempGood!=null){
                                c.addGood(tempGood);
                                list.remove(tempGood);
                                tempGood = null;
                            }
                        }
                    }else {
                        c.addGood(list.get(tmpint));
                        list.remove(tmpint);
                    }
                }
                default -> inform("SERVER: Not valid cell", nick);
            }

            if(!askPlayerDecision("SERVER: Do you want to continue to add goods?", p)) flag = false;
        }

        if(flag) inform("SERVER: Empty list of goods", nick);
    }

    //TODO: gestione default, timeout,
    // gestione indici/coordinate out of bunds
    public void caseRedistribution(Player p , VirtualView v , List<Colour> list , String nick) throws BusinessLogicException {
        printPlayerDashboard(v, p, nick);

        int[] coordinates;
        boolean exit = true;

        while (exit) {
            inform("SERVER: Select a storage unit to take the good from", nick);
            coordinates = askPlayerCoordinates(p);

            Tile tmp2 = p.getTile(coordinates[0], coordinates[1]);
            switch (tmp2) {
                case StorageUnit c -> {
                    List<Colour> tmplist = c.getListOfGoods();

                    if(tmplist.isEmpty()){
                        inform("SERVER: Empty storage unit", nick);
                        break;
                    }

                    printListOfGoods(tmplist, nick);
                    inform("SERVER: Select the index of the good you want to rearrange", nick);
                    int tmpint = askPlayerIndex(p, tmplist.size());

                    Colour tmpColor = tmplist.get(tmpint);
                    c.removeGood(tmpint);
                    selectStorageUnitForAdd(v, p, tmpColor, nick);
                    printPlayerDashboard(v, p, nick);
                }
                default -> inform("SERVER: Not valid cell", nick);
            }
            if(!askPlayerDecision("SERVER: Do you want to select another storage unit for the rearranging?", p)) exit = false;
        }
    }

    //TODO: gestione default, timeout,
    // gestione indici/coordinate out of bunds
    public void selectStorageUnitForAdd(VirtualView v, Player p , Colour color , String nick) throws BusinessLogicException {
        int[] coordinates;
        boolean exit = false;

        while (!exit) {
            inform("SERVER: Select a storage unit to place the good in", nick);
            coordinates = askPlayerCoordinates(p);
            Tile tmp2 = p.getTile(coordinates[0], coordinates[1]);
            switch (tmp2) {
                case StorageUnit c -> {
                    if(c.isFull()) {
                        inform("SERVER: This storage unit is full. Try again", nick);
                        continue;
                    }
                    if(color == Colour.RED) {
                        if(c.isAdvanced()) {
                            c.addGood(color);
                            exit = true;
                        } else inform("SERVER: You can't place a dangerous good in a not advanced storage unit", nick);
                    }else {
                        c.addGood(color);
                        exit = true;
                    }
                }
                default -> {
                    inform("SERVER: Not valid cell", nick);
                    //if(!askPlayerDecision("SERVER: " + "Do you want to select another Storage Unit? , if not you will loose the goods", p)) exit = true;
                }
            }
        }
    }

    //TODO: gestione default, timeout,
    // gestione indici/coordinate out of bunds
    public void caseRemove(Player p , VirtualView v , String nick) throws BusinessLogicException {
        int[] coordinates;
        boolean exit = true;
        printPlayerDashboard(v, p, nick);

        while (exit) {
            inform("SERVER: Select a storage unit", nick);
            coordinates = askPlayerCoordinates(p);
            Tile tmp2 = p.getTile(coordinates[0], coordinates[1]);
            switch (tmp2) {
                case StorageUnit c -> {
                    if(!c.getListOfGoods().isEmpty()) {
                        List<Colour> tmplist = c.getListOfGoods();
                        printListOfGoods(tmplist, nick);
                        inform("SERVER: Select the index of the good you want to trash", nick);
                        int tmpint = askPlayerIndex(p, tmplist.size());
                        c.removeGood(tmpint);
                        printPlayerDashboard(v, p, nick);
                    }else{
                        inform("SERVER: Empty list of goods", nick);
                    }

                }
                default -> inform("SERVER: Not valid cell", nick);
            }
            if(!askPlayerDecision("SERVER: " + "Do you want to select another storage unit for trashing?", p)) exit = false;
        }
    }

    public void addHuman() throws BusinessLogicException {
        for (Player p : playersByNickname.values()) {
            String tmpNick = getNickByPlayer(p);
            VirtualView x = viewsByNickname.get(tmpNick);
            p.setGamePhase(GamePhase.CARD_EFFECT);
            try {
                x.updateGameState(enumSerializer.serializeGamePhase(GamePhase.CARD_EFFECT));
            } catch (IOException e) {
                markDisconnected(tmpNick);
            } catch (Exception e) {
                markDisconnected(tmpNick);
                System.err.println("[ERROR] in notifyView: " + e.getMessage());
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++) {
                    Tile t = p.getTile(i, j);
                    switch (t) {
                        case HousingUnit h -> {
                            Human tmp = h.getTypeOfConnections();
                            if (h.getType() == Human.HUMAN){
                                switch (tmp) {
                                    case PRADELLA -> {
                                        Human tmp2 = Human.HUMAN;
                                        for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                    }
                                    case PURPLE_ALIEN -> {
                                        if(p.presencePurpleAlien() || (i == 2 && j == 3)) {
                                            Human tmp2 = Human.HUMAN;
                                            for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                            continue;
                                        }
                                        try {
                                            String msg = "SERVER: Do you want to place a purple alien in the housing unit " +
                                                    "next to the purple alien module?";
                                            if (askPlayerDecision(msg, p)) {
                                                Human tmp2 = Human.PURPLE_ALIEN;
                                                h.addHuman(tmp2);
                                                p.setPurpleAlien();
                                            } else {
                                                Human tmp2 = Human.HUMAN;
                                                for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                            }
                                        } catch (BusinessLogicException e) {
                                            throw new RuntimeException(e);
                                        }

                                    }
                                    case BROWN_ALIEN -> {
                                        if(p.presenceBrownAlien() || (i == 2 && j == 3)){
                                            Human tmp2 = Human.HUMAN;
                                            for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                            continue;
                                        }
                                        try {
                                            String msg = "SERVER: Do you want to place a brown alien in the housing unit " +
                                                    "next to the brown alien module?";
                                            if (askPlayerDecision(msg, p)) {
                                                Human tmp2 = Human.BROWN_ALIEN;
                                                h.addHuman(tmp2);
                                                p.setBrownAlien();
                                            } else {
                                                Human tmp2 = Human.HUMAN;
                                                for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                            }
                                        } catch (BusinessLogicException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                        }
                    }
                        default -> {}
                    }
                }
            }
            //in tutte le abitazioni normali metto 2 human
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
            inform("SERVER: You lost all your crewmates", nick);
        } else {
            if(!p.isConnected()){
                autoCommandForRemovePlayers(p, num);
                return;
            }
            while (num > 0) {
                if(p.isConnected()){
                    try {
                        x.inform("SERVER: Select an Housing unit");
                    } catch (IOException e) {
                        markDisconnected(nick);
                    } catch (Exception e){
                        markDisconnected(nick);
                        System.err.println("[ERROR] in removeCrewmates: " + e.getMessage());
                    }
                    int[] vari = askPlayerCoordinates(p);
                    //TODO: gestire caso scadenza timeout (il client non risponde in tempo, askPlayerCoordinates ritorna null)
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
                                    x.inform("SERVER: Select a valid housing unit");
                                    //x.printPlayerDashboard(tileSerializer.toJsonMatrix(p.getDashMatrix()));
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
                                x.inform("SERVER:  Select a valid housing unit");
                                //x.printPlayerDashboard(tileSerializer.toJsonMatrix(p.getDashMatrix()));
                            } catch (IOException e) {
                                markDisconnected(nick);
                            } catch (Exception e){
                                markDisconnected(nick);
                                System.err.println("[ERROR] in removeCrewmates: " + e.getMessage());
                            }
                        }
                    }

                    printPlayerDashboard(x, p, nick);
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

    public void startPlauge(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        inform("SERVER: Starting plague", nick);
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                Human tmpHuman;
                switch (y){
                    case HousingUnit h -> {
                        if(h.isConnected() && !h.getListOfToken().isEmpty()){
                            tmpHuman = h.getListOfToken().getFirst();
                            h.removeHumans(0);
                            inform("SERVER: Connected Housing Unit detected. You lose 1 crewmate", nick);
                            switch (tmpHuman){
                                case BROWN_ALIEN -> p.setBrownAlien();
                                case PURPLE_ALIEN ->  p.setPurpleAlien();
                                default -> {}
                            }
                        }
                    }
                    default ->{}
                }
            }
            tmp = getNumCrew(p);
            if(tmp ==  0) p.setEliminated();

        }
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected, and they want to use a battery
     *
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */

    //TODO: caso default null timeout
    public boolean isProtected(String nick, int d) throws BusinessLogicException {
        boolean flag = false;
        VirtualView x = viewsByNickname.get(nick);
        Player p = getPlayerByNickname(nick);
        boolean directionProtected = false;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile tile = p.getTile(i, j);
                switch (tile) {
                    case Shield sh -> {
                        if (sh.getProtectedCorner(d) == 8) directionProtected = true;
                    }
                    default -> {
                    }
                }
            }
        }
        if (directionProtected) {
            inform("SERVER: You can activate the shield by consuming a battery ", nick);
            String mex = "To activate a shield";
            return manageEnergyCell(nick, mex);
        }
//            while (!flag) {
//                boolean ans = askPlayerDecision("SERVER: Do you want to use a shield?", p);
//
//                if (ans) {
//                    int[] coordinate = askPlayerCoordinates(playersByNickname.get(nick));
//
//                    //TODO: gestire caso default null
//
//                    Tile y = playersByNickname.get(nick).getTile(coordinate[0], coordinate[1]);
//                    switch (y) {
//                        case Shield shield -> {
//                            if (!(shield.getProtectedCorner(d) == 8))
//                                inform("SERVER: Select another shield", nick);
//                            else
//                                return manageEnergyCell(nick);
//                        }
//                        default -> inform("SERVER: Select a valid shield", nick);
//                    }
//                } else flag = true;
//            }
//        }
        return false;
    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     *
     * @param dir  cardinal direction of the attack
     * @param type dimension of the attack, true if it is big
     */
//    public void defenceFromCannon(int dir, boolean type, int dir2, Player p) throws BusinessLogicException {
//
//
//        String direction = "";
//        int direction2 = dir2;
//        switch (dir) {
//            case 0 -> {
//                direction = "Nord";
//                direction2 = dir2+4;
//            }
//            case 1 -> {
//                direction = "East";
//                direction2 = dir2+5;
//            }
//            case 2 -> {
//                direction = "South";
//                direction2 = dir2+4;
//            }
//            case 3 -> {
//                direction = "West";
//                direction2 = dir2+5;
//            }
//        }
//        String nick = getNickByPlayer(p);
//        Tile[][] tmpDash = p.getDashMatrix();
//        try {
//            viewsByNickname.get(nick).inform("the attack is coming from "+direction+" on the section "+direction2);
//            viewsByNickname.get(nick).inform(" SHIP BEFORE THE ATTACK ");
//            viewsByNickname.get(nick).printPlayerDashboard(tileSerializer.toJsonMatrix(tmpDash));
//        } catch (Exception e) {
//            markDisconnected(nick);
//        }
//        if (dir == 0) {
//            if (dir2 > 3 && dir2 < 11) {
//                if (type || (!isProtected(nick, dir) && !type)) {
//                    scriptOfDefence(nick , tmpDash , dir2);
//                } else {
//                    try {
//                        viewsByNickname.get(nick).inform("you are safe");
//                    } catch (Exception e) {
//                        markDisconnected(nick);
//                    }
//                }
//            }
//        } else if (dir == 2) {
//            if (dir2 > 3 && dir2 < 11) {
//                if (type || (!isProtected(nick, dir) && !type)) {
//                    scriptOfDefence(nick , tmpDash , dir2);
//                } else {
//                    try {
//                        viewsByNickname.get(nick).inform("you are safe");
//                    } catch (Exception e) {
//                        markDisconnected(nick);
//                    }
//                }
//            }
//        } else if (dir == 1) {
//            if (dir2 > 4 && dir2 < 10) {
//                if (type || (!isProtected(nick, dir) && !type)) {
//                    scriptOfDefence(nick , tmpDash , dir2);
//                } else {
//                    try {
//                        viewsByNickname.get(nick).inform("you are safe");
//                    } catch (Exception e) {
//                        markDisconnected(nick);
//                    }
//                }
//            }
//        } else if (dir == 3) {
//            if (dir2 > 4 && dir2 < 10) {
//                if (type || (!isProtected(nick, dir) && !type)) {
//                    scriptOfDefence(nick , tmpDash , dir2);
//                } else {
//                    try {
//                        viewsByNickname.get(nick).inform("you are safe");
//                    } catch (Exception e) {
//                        markDisconnected(nick);
//                    }
//                }
//            }
//        }
//
//    }
    public boolean defenceFromCannon(int dir, boolean type, int dir2, Player p) throws BusinessLogicException {
        String[] directions = {"Nord", "East", "South", "West"};
        String direction = directions[dir];
        String size = type ? "big" : "small";
        String nick = getNickByPlayer(p);
        VirtualView v = getViewCheck(nick);

        String msg = "\nSERVER: A "+size+" attack is coming from "+direction+" on section "+dir2+"\nSERVER: Ship before attack";
        inform(msg, nick);
        printPlayerDashboard(v, p, nick);

        if (isHitZone(dir, dir2)) {
            if (type || !isProtected(nick, dir)){
                return scriptOfDefence(nick, p, v, dir2 , dir);
            } else {
                inform("SERVER: You are protected", nick);
            }
        } else {
            inform("SERVER: Attack out of range. You are safe", nick);
        }

        return false;
    }

    private boolean isHitZone(int dir, int dir2) {
        return switch (dir) {
            case 0, 2 -> dir2 > 3 && dir2 < 11;
            case 1, 3 -> dir2 > 4 && dir2 < 10;
            default -> false;
        };
    }




    private boolean scriptOfDefence(String Nickname , Player p, VirtualView v, int dir2 , int dir) throws BusinessLogicException {
        Boolean tmpBoolean = false;
        switch (dir){
            case 0 ->  tmpBoolean = p.removeFrom0(dir2);
            case 1 ->  tmpBoolean = p.removeFrom1(dir2);
            case 2 ->  tmpBoolean = p.removeFrom2(dir2);
            case 3 ->  tmpBoolean = p.removeFrom3(dir2);
        }

        if(manageIfPlayerEliminated(p)){
            inform("SERVER: You have lost all your humans", Nickname);
            return true;
        }

        if(!tmpBoolean){
            inform("SERVER: You are safe", Nickname);
        }else{
            inform("SERVER: You've been hit", Nickname);
            askStartHousingForControl(Nickname);
        }

        return false;
    }

    /**
     * this method evaluates the protection of the ship making use of the other methods
     *
     * @param dir  cardinal direction of the attack
     * @param isBig dimension of the attack, true if it is big
     */
//    public void defenceFromMeteorite(int dir, boolean type, int dir2) throws BusinessLogicException {
//
//        String direction = "";
//        int direction2 = dir2;
//        switch (dir) {
//            case 0 -> {
//                direction = "Nord";
//                direction2 = dir2+4;
//            }
//            case 1 -> {
//                direction = "East";
//                direction2 = dir2+5;
//            }
//            case 2 -> {
//                direction = "South";
//                direction2 = dir2+4;
//            }
//            case 3 -> {
//                direction = "West";
//                direction2 = dir2+5;
//            }
//        }
//
//        for (String nick : playersByNickname.keySet()) {
//
//            Player p = getPlayerCheck(nick);
//            VirtualView v = getViewCheck(nick);
//            Tile[][] tmpDash = playersByNickname.get(nick).getDashMatrix();
//            try {
//                viewsByNickname.get(nick).inform("the attack is coming from "+direction+" on the section "+direction2);
//                viewsByNickname.get(nick).inform("ship before the attack");
//                viewsByNickname.get(nick).printPlayerDashboard(tileSerializer.toJsonMatrix(tmpDash));
//            } catch (Exception e) {
//                markDisconnected(nick);
//            }
//            if (dir == 0) {
//                if (dir2 > 3 && dir2 < 11) {
//                    if (type && !checkProtection(dir, dir2, nick)) {
//                            scriptOfDefence(nick, p, v, dir2 , dir);
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                    if (!type && playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
//                        if (!isProtected(nick,dir)) {
//                            scriptOfDefence(nick, p, v, dir2 , dir);
//                        }
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                }
//            } else if (dir == 2) {
//                if (dir2 > 3 && dir2 < 11) {
//                    if (type && checkProtection(dir, dir2, nick)) {
//                        scriptOfDefence(nick, p, v, dir2 , dir);
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                    if (!type && !playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
//                        if (!isProtected(nick, dir)) {
//                            scriptOfDefence(nick, p, v , dir2 ,dir);
//                        }
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                }
//            } else if (dir == 1 || dir == 3) {
//                if (dir2 > 4 && dir2 < 10) {
//                    if (type && !checkProtection(dir, dir2, nick)) {
//                        scriptOfDefence(nick, p, v , dir2 , dir);
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                    if (!type && !playersByNickname.get(nick).checkNoConnector(dir, dir2)) {
//                        if (!isProtected(nick, dir)) {
//                            scriptOfDefence(nick, p, v, dir2 , dir);
//                        }
//                    }else {
//                        try {
//                            viewsByNickname.get(nick).inform("you are safe");
//                        } catch (Exception e) {
//                            markDisconnected(nick);
//                        }
//                    }
//                }
//            }
//        }
//    }

    public void defenceFromMeteorite(int dir, boolean isBig, int dir2, List<Player> players, int numMeteorite) throws BusinessLogicException {
        String[] directions = {"Nord", "East", "South", "West"};
        String direction = directions[dir];
        String size = isBig ? "big" : "small";

        for (Player p : players) {
            String nick = getNickByPlayer(p);
            if(players.indexOf(p)!=0) inform("\nSERVER: Waiting for your turn...", nick);
        }

        for (Player p : players) {
            if(p.isConnected() && !p.isEliminated()){
                String nick = getNickByPlayer(p);
                VirtualView v = getViewCheck(nick);
                inform("SERVER: A " + size + " meteorite is coming from " + direction + " on section " + dir2, nick);
                inform("SERVER: Ship before the attack", nick);
                printPlayerDashboard(v, p, nick);

                if (!isHitZone(dir, dir2)) {
                    inform("SERVER: Meteorite out of range. You are safe", nick);
                    continue;
                }
                if (isBig) {
                    if (!checkProtection(dir, dir2, nick)) {
                        scriptOfDefence(nick, p, v, dir2, dir);
                    } else {
                        inform("SERVER: Cannon protected you!", nick);
                    }
                } else {
                    boolean noConnector = p.checkNoConnector(dir, dir2);
                    boolean shielded = isProtected(nick, dir);

                    if (!noConnector && !shielded) {
                        scriptOfDefence(nick, p, v, dir2, dir);
                    } else {
                        inform("SERVER: You are safe", nick);
                    }
                }

                if(players.indexOf(p) != players.size()-1) inform("SERVER: Checking other players...", nick);
                else broadcastInform("SERVER: "+numMeteorite+"° meteorite processed for all players");
            }
        }
    }



    public void addCreditToPlayer(int credits, Player player) {
        player.addCredits(credits);
    }

    private boolean manageEnergyCell(String nick, String mex) throws BusinessLogicException {
        VirtualView x = getViewCheck(nick);
        //Caso disconnesso WorstCase scenario: non attivo i doppi motori
        Player player = getPlayerCheck(nick);
        if(!player.isConnected()) return false;
        int[] coordinates;
        boolean exit = false;

        if (!askPlayerDecision("SERVER: " + "Do you want to use a battery? "+mex, player)) {
            return false;
        } else {
            while (!exit) {
                coordinates = askPlayerCoordinates(player);
                if(coordinates == null) return false;

                Tile p = playersByNickname.get(nick).getTile(coordinates[0], coordinates[1]);
                switch (p) {
                    case EnergyCell c -> {
                        int capacity = c.getCapacity();
                        if (capacity == 0) {
                            inform("SERVER: You have already used all the batteries for this cell", nick);
                            if(!askPlayerDecision("SERVER: " + "Do you want to select another EnergyCell?", player))
                                return false;
                        } else {
                            c.useBattery();
                            return true;
                        }
                    }
                    default -> {
                        inform("SERVER: Not valid cell", nick);
                        if(!askPlayerDecision("SERVER: " + "Do you want to select another EnergyCell?", player))
                            exit = true;
                    }
                }
            }
            return false;
        }
    }


    public boolean checkProtection(int dir, int dir2, String player) throws BusinessLogicException {
        return switch (dir) {
            case 0 -> checkColumnProtection(player, dir2 - 4);
            case 1 -> checkRowProtectionFromSide(player, dir2 - 5 , 1);
            case 2 -> checkColumnProtectionFromSouth(player, dir2-4, 2);
            case 3 -> checkRowProtectionFromSide(player, dir2 - 5 , 3);
            default -> false;
        };
    }

    private boolean checkColumnProtection(String player, int col) throws BusinessLogicException {
        for (int row = 0; row < 5; row++) {
            if (playersByNickname.get(player).validityCheck(row, col) == Status.USED) {
                Tile tile = playersByNickname.get(player).getTile(row, col);
                if(isCannonProtected(tile, player, 0)) return true;
            }
        }
        return false;
    }

    private boolean checkRowProtectionFromSide(String player, int row, int direction) throws BusinessLogicException {
        for (int r = row - 1; r <= row + 1; r++) {
            if (r < 0 || r >= 5) continue;
            if (checkTileInRow(player, r, direction)) return true;
        }
        return false;
    }


    private boolean checkTileInRow(String player, int row, int direction) throws BusinessLogicException {
        if (direction == 1) {
            for (int col = 6; col >= 0; col--) {
                if (playersByNickname.get(player).validityCheck(row, col) == Status.USED) {
                    Tile tile = playersByNickname.get(player).getTile(row, col);
                    if(isCannonProtected(tile, player, direction)) return true;
                }
            }
        } else if (direction == 3) {
            for (int col = 0; col <= 6; col++) {
                if (playersByNickname.get(player).validityCheck(row, col) == Status.USED) {
                    Tile tile = playersByNickname.get(player).getTile(row, col);
                    if(isCannonProtected(tile, player, direction)) return true;
                }
            }
        }
        return false;
    }

    private boolean checkColumnProtectionFromSouth(String player, int column, int direction) throws BusinessLogicException {
        for (int c = column - 1; c <= column + 1; c++) {
            if(c < 0 || c>= 7) continue;
            if(checkTileInColumn(player, c, direction)) return true;
        }
        return false;
    }

    private boolean checkTileInColumn(String player, int column, int direction) throws BusinessLogicException {
        for(int row = 4; row >= 0; row--){
            if (playersByNickname.get(player).validityCheck(row, column) == Status.USED){
                Tile tile = playersByNickname.get(player).getTile(row, column);
                if(isCannonProtected(tile, player, direction)) return true;
            }
        }
        return false;
    }

    private boolean isCannonProtected(Tile tile, String player , int orientation) throws BusinessLogicException {
        switch (tile){
            case Cannon c -> {
                if(c.isDouble() && c.controlCorners(orientation) == 5) return manageEnergyCell(player, "To activate double cannon");
                else if (c.isDouble() && c.controlCorners(orientation) != 5) {return false;}
                else if(c.controlCorners(orientation) != 4) {return false;}
                else return true;
            }
            default -> {return false;}
        }
    }


    //    public boolean checkProtection(int dir, int dir2, String player) throws BusinessLogicException {
//        boolean result = false;
//        if (dir == 0) {
//            boolean flag = true;
//            int i = 0;
//            while (flag && i < 5) {
//                if (playersByNickname.get(player).validityCheck(i, dir2 - 4) == Status.USED) {
//                    Tile y = playersByNickname.get(player).getTile(i, dir2 - 4);
//                    switch (y) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                return manageEnergyCell(player);
//                            }
//                        }
//                        default -> {
//                            return false;
//                        }
//                    }
//                }
//                i++;
//
//            }
//            return result;
//        } else if (dir == 1) {
//            boolean flag = true;
//            int i = 5;
//            while (flag && i >= 1) {
//                if (playersByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
//                    Tile y1 = playersByNickname.get(player).getTile(dir2 - 5, i);
//                    Tile y2 = playersByNickname.get(player).getTile(dir2 - 5, i + 1);
//                    Tile y3 = playersByNickname.get(player).getTile(dir2 - 5, i - 1);
//                    switch (y1) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    switch (y2) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    switch (y3) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    flag = false;
//
//                }
//                i--;
//            }
//            return result;
//        }else if (dir == 3) {
//            boolean flag = true;
//            int i = 1;
//            while (flag && i<6) {
//                if (playersByNickname.get(player).validityCheck(dir2 - 5, i) == Status.USED) {
//                    Tile y1 = playersByNickname.get(player).getTile(dir2 - 5, i);
//                    Tile y2 = playersByNickname.get(player).getTile(dir2 - 5, i + 1);
//                    Tile y3 = playersByNickname.get(player).getTile(dir2 - 5, i - 1);
//                    switch (y1) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    switch (y2) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    switch (y3) {
//                        case Cannon c -> {
//                            if (!c.isDouble()) {
//                                return true;
//                            } else {
//                                if (manageEnergyCell(player)) {
//                                    return true;
//                                }
//                            }
//                        }
//                        default -> {
//                        }
//                    }
//                    flag = false;
//
//                }
//                i++;
//            }
//            return result;
//
//        }
//        return false;
//    }

    public void askStartHousingForControl(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        if(p.isConnected()){
            int[] xy;
            boolean flag = true;
            do{
                inform("SERVER: Choose your starting housing unit:", nickname);
                xy = askPlayerCoordinates(p);

                if(xy == null) {
                    xy = new int[] {2,3};
                    break;
                }

               Tile tmp = p.getTile(xy[0], xy[1]);
                switch (tmp) {
                    case HousingUnit h -> {
                        if(h.getType() == Human.HUMAN) flag = false;
                        else inform("SERVER: Not valid position, try again", nickname);
                    }
                    default -> inform("SERVER: Not valid position, try again", nickname);
                }
            } while(flag);

            /**/System.out.println("PRIMA DI CHECKPLAYER ASSEMBLY: Player "+getNickByPlayer(p)+" num di discard tiles: "+ p.checkDiscardPile());
            checkPlayerAssembly(nickname,  xy[0], xy[1]);
            /**/System.out.println("DOPO DI CHECKPLAYER ASSEMBLY: Player "+getNickByPlayer(p)+" num di discard tiles: "+ p.checkDiscardPile());

        }else{
            /**/System.out.println("PRIMA DI CHECKPLAYER ASSEMBLY: Player "+getNickByPlayer(p)+" num di discard tiles: "+ p.checkDiscardPile());
            checkPlayerAssembly(nickname,  2,3);
            /**/System.out.println("PRIMA DI CHECKPLAYER ASSEMBLY: Player "+getNickByPlayer(p)+" num di discard tiles: "+ p.checkDiscardPile());
        }
    }

    private void checkPlayerAssembly(String nick , int x , int y) throws BusinessLogicException {
        Player p = getPlayerCheck(nick);
        VirtualView v = getViewCheck(nick);
        p.controlAssembly(x,y);
        printPlayerDashboard(v, p, nick);
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

    /**
    public void changePhaseFromCard(String nick, Player p, GamePhase tmp){
        if(p.isConnected()){
            try {
                viewsByNickname.get(nick).updateGameState(enumSerializer.serializeGamePhase(tmp));
                //notifyView(nick);
            } catch (IOException e) {
                markDisconnected(nick);
            } catch (Exception e){
                markDisconnected(nick);
                System.err.println("[ERROR] in changePhaseFromCard: " + e.getMessage());
            }
        }
    }
     */

    public void changeMapPosition(String nick, Player p) throws BusinessLogicException {
        playersPosition = buildPlayersPositionMap();
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

    public void setExit() throws BusinessLogicException {
        playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.EXIT));

        viewsByNickname.forEach((nick, view) -> {
            try {
                view.updateGameState(enumSerializer.serializeGamePhase(GamePhase.EXIT));
            } catch (Exception e) {
                markDisconnected(nick);
            }
        });
        onGameEnd.accept(gameId);
    }

    public Map<String,int[] > getPlayersPosition(){
        return new HashMap<>(playersPosition);
    }

    public String getGamePhase(String nick){
        try {
            return enumSerializer.serializeGamePhase(playersByNickname.get(nick).getGamePhase());
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in serializeGamePhase: " + e.getMessage());
        }
        return "EXIT";
    }

    public String[][] getDashJson(String nick){
        try {
            return tileSerializer.toJsonMatrix( playersByNickname.get(nick).getDashMatrix());
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in serializeDashMatrix: " + e.getMessage());
        }
        return null;
    }

    private Map<String,int[]> buildPlayersPositionMap() {
        Map<String,int[]> m = new HashMap<>();
        for (Map.Entry<String, Player> e : playersByNickname.entrySet()) {
            String nick = e.getKey();
            Player p    = e.getValue();

            m.put(nick, new int[]{
                    p.getPos(),                 // posizione
                    p.getLap(),                 // lap
                    p.isEliminated() ? 1 : 0  ,  // 1=eliminato, 0=ingame
                    p.getIdPhoto()

            });
        }
        return m;
    }

}

