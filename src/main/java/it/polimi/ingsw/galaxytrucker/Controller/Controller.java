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


/**
 * Server-side game controller in the MVC architecture.
 * Manages the entire game logic, player state, tile deck, card effects, and phase transitions.
 * Responsibilities:
 * - Initializing the game board, decks, and players
 * - Managing turn-based interactions and enforcing rules
 * - Handling card effects during the flight phase
 * - Serializing and broadcasting game state updates to clients via VirtualView
 * - Orchestrating game flow based on the current GamePhase
 * - Handling disconnections, timeouts, and demo mode behaviors
 * It holds core game structures such as:
 * - `pileOfTile`, `deck`, `shownTile`: game resources
 * - `playersByNickname`: player data and dashboards
 * - `viewsByNickname`: client connection views
 * - `fBoard`: the active flight board instance
 * Designed to operate concurrently and safely via thread-safe collections (e.g., ConcurrentHashMap),
 * and uses scheduled tasks for timeout and ping monitoring.
 * @author Gabriele La Vecchia
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 * @author Francesco Lo Conte
 */
public class Controller implements Serializable {
    private final int gameId;
    public transient Map<String, VirtualView> viewsByNickname = new ConcurrentHashMap<>();
    private final Map<String, Player> playersByNickname = new ConcurrentHashMap<>();
    private Map<String , int[] > playersPosition = new ConcurrentHashMap<>();
    private final Set<String> loggedInUsers;
    private final AtomicInteger playerIdCounter;
    private final int MaxPlayers;
    private final boolean isDemo;
    private transient Consumer<Integer> onGameEnd;
    private int numberOfEnter =0;
    private final int TIME_OUT = 30;

    private transient Hourglass hourglass;
    public List<Tile> pileOfTile;
    public List<Tile> shownTile = new ArrayList<>();
    protected final FlightCardBoard fBoard;
    public Deck deck;
    public List<Deck> decks;
    private TileParserLoader pileMaker = new TileParserLoader();
    private transient static final  ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private transient ScheduledFuture<?> lastPlayerTask;
    private transient CardSerializer cardSerializer;
    public transient TileSerializer tileSerializer;
    private transient EnumSerializer enumSerializer;
    public transient ScheduledExecutorService pingScheduler;

    /**
     * Initializes the game controller with all core components.
     * This constructor sets up the game board, tile and card decks, serializers,
     * player data structures, and timing mechanisms. It differentiates setup logic
     * based on whether the game is running in demo mode or standard mode.
     * @param isDemo true if the game is in demo mode (simplified deck and board)
     * @param gameId the unique ID of the game instance
     * @param MaxPlayers the maximum number of players allowed
     * @param onGameEnd callback function to execute when the game ends
     * @param loggedInUsers the set of users currently logged into the system
     * @throws CardEffectException if there is an error initializing a card effect
     * @throws IOException if loading tiles or decks fails
     */
    public Controller(boolean isDemo, int gameId, int MaxPlayers, Consumer<Integer> onGameEnd, Set<String> loggedInUsers) throws BusinessLogicException, IOException {
        if(isDemo) {
            fBoard = new FlightCardBoard(this);
            DeckManager deckCreator = new DeckManager();
            deck = deckCreator.CreateDemoDeck();
        }else{
            fBoard = new FlightCardBoard2(this);
            DeckManager deckCreator = new DeckManager();
            decks = deckCreator.CreateSecondLevelDeck();
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
        this.playerIdCounter = new AtomicInteger(1);
        pileOfTile = pileMaker.loadTiles();
        Collections.shuffle(pileOfTile);
        this.loggedInUsers = loggedInUsers;
    }

    /**
     * Initializes a single-threaded, daemon ScheduledExecutorService named
     * "PingScheduler-<gameId>" and schedules a recurring ping task.
     * <p>
     * After an initial delay of 10 seconds, {@link #pingAllClients()} will be
     * invoked every 3 seconds to check client connectivity.
     * </p>
     */
    private void initPingScheduler() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PingScheduler-" + gameId);
            t.setDaemon(true);
            return t;
        });
        pingScheduler.scheduleAtFixedRate(this::pingAllClients, 10, 3, TimeUnit.SECONDS);
    }

    /**
     * Sends a heartbeat ("PING") to every registered client by calling
     * {@code updateGameState("PING")} on each {@link VirtualView}. If a client
     * fails to respond (throws), it is marked disconnected.
     */
    private void pingAllClients() {

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

    /**
     * Immediately shuts down the ping scheduler, cancelling any scheduled tasks.
     * Safe to call multiple times.
     */
    public void shutdownPing() {
        if (pingScheduler != null) pingScheduler.shutdownNow();
    }

    /**
     * Sends a full game state update to the specified player's view.
     * This includes:
     * - The current game phase
     * - The full map of all player positions
     * - The player's status (firepower, engine power, credits, alien presence, crew, energy)
     * - The last drawn tile, if in TILE_MANAGEMENT phase
     * If any error occurs during communication, the player is marked as disconnected.
     * @param nickname the nickname of the player to update
     */
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

    /**
     * Adds a new player to the game and initializes their state.
     * Performs the following actions:
     * - Validates that the nickname is unique and the game is not full
     * - Assigns a ship image ID based on join order
     * - Creates a new Player object with default settings and dashboard
     * - Sets the initial game phase to WAITING_IN_LOBBY
     * - Sends initial tile and phase to the player's VirtualView
     * - Updates player and view registries
     * - Broadcasts the join event to all players
     * @param nickname the nickname of the joining player
     * @param view the VirtualView associated with the player
     * @throws BusinessLogicException if the nickname is already used or the game is full
     * @throws Exception if an error occurs during serialization or communication
     */
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

    /**
     * Returns the map of all players indexed by their nickname.
     *
     * @return an unmodifiable view of the map from nickname to Player
     */
    public Map<String, Player> getPlayersByNickname(){
        return playersByNickname;
    }

    /**
     * Retrieves a Player by their nickname, or null if not found.
     *
     * @param nickname the player's nickname
     * @return the Player object, or null if no such player
     */
    public Player getPlayerByNickname(String nickname) {
        return playersByNickname.get(nickname);
    }

    /**
     * Retrieves a Player by nickname, throwing if absent.
     *
     * @param nickname the player's nickname
     * @return the Player object
     * @throws BusinessLogicException if no player with the given nickname exists
     */
    public Player getPlayerCheck(String nickname) throws BusinessLogicException {
        Player player = playersByNickname.get(nickname);
        if (player == null) throw new BusinessLogicException("Player not found");
        return player;
    }


    /**
     * Retrieves a VirtualView by nickname, throwing if absent.
     *
     * @param nickname the player's nickname
     * @return the VirtualView associated with the player
     * @throws BusinessLogicException if no view for the given nickname exists
     */
    public VirtualView getViewCheck(String nickname) throws BusinessLogicException {
        VirtualView view = viewsByNickname.get(nickname);
        if (view == null) throw new BusinessLogicException("Player not found");
        return view;
    }

    /**
     * Finds the nickname corresponding to a Player instance.
     *
     * @param player the Player object
     * @return the nickname key under which this player is stored
     * @throws BusinessLogicException if the player is not present in the map
     */
    public String getNickByPlayer(Player player) throws BusinessLogicException {
        return playersByNickname.entrySet().stream()
                .filter(e -> e.getValue().equals(player))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Player Not Found"));
    }

    /**
     * Sends an informational message to a specific player.
     * Marks the player disconnected on any I/O or other failure.
     *
     * @param msg  the message to send
     * @param nick the target player's nickname
     */
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


    /**
     * Sends an informational message and then notifies the client of state update.
     * Marks the player disconnected on any failure.
     *
     * @param msg  the message to send
     * @param nick the target player's nickname
     */
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

    /**
     * Reports an error message to a specific player.
     * Marks the player disconnected on any failure.
     *
     * @param msg  the error message
     * @param nick the target player's nickname
     */
    public void reportError(String msg, String nick){
        VirtualView v = viewsByNickname.get(nick);
        try{
            v.reportError(msg);
        } catch (IOException e){
            markDisconnected(msg);
        } catch(Exception e){
            markDisconnected(msg);
            System.err.println("[ERROR] in reportError: " + e);
        }
    }

    /**
     * Broadcasts an informational message to all connected players.
     *
     * @param msg the message to broadcast
     */
    public void broadcastInform(String msg) {
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            Player p = getPlayerByNickname(nickname);
            if(p.isConnected()) inform(msg, nickname);
        }
    }


    /**
     * Broadcasts an informational message to all connected players except one.
     *
     * @param msg    the message to broadcast
     * @param caller the player to exclude from the broadcast
     */
    public void broadcastInformExcept(String msg, Player caller){
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            Player p = getPlayerByNickname(nickname);
            if(p.isConnected() && !p.equals(caller)) inform(msg, nickname);
        }
    }

    /**
     * Sends the player's dashboard matrix to their view.
     * Marks the player disconnected on any failure.
     *
     * @param v    the player's VirtualView
     * @param p    the Player whose dashboard to print
     * @param nick the player's nickname (for disconnection handling)
     */
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

    /**
     * Prints the list of goods for a player.
     * Marks the player disconnected on any failure.
     *
     * @param list the list of goods to display
     * @param nick the target player's nickname
     * @throws BusinessLogicException if the player's view cannot be retrieved
     */
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

    /**
     * Updates the player's view with a new game phase.
     * Marks the player disconnected on any failure.
     *
     * @param nick  the player's nickname
     * @param v     the player's VirtualView
     * @param phase the new GamePhase to send
     */
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

    /**
     * Counts how many players are currently connected.
     *
     * @return the number of players with isConnected == true
     */
    public int countConnectedPlayers() {
        return (int) playersByNickname.values().stream().filter(Player::isConnected).count();
    }

    /**
     * Marks the specified player as disconnected and informs the other players.
     * If the player is currently connected, this method:
     * - Sets their status to disconnected
     * - Broadcasts a message to all players
     * - Triggers a timeout check for game continuation
     * - Removes the player from the logged-in user list
     * @param nickname the nickname of the player to mark as disconnected
     */

    public void markDisconnected(String nickname) {
        Player p = playersByNickname.get(nickname);
        if (p != null && p.isConnected()) {
            p.setConnected(false);
            broadcastInform(nickname + " is disconnected");
            setTimeout();
            loggedInUsers.remove(nickname);
        }
    }

    /**
     * Marks the given player as reconnected by updating their view and
     * restoring their connected status. Broadcasts a reconnection message
     * if the player was previously disconnected, cancels any pending
     * single-player timeout, and notifies the client of the current state.
     *
     * @param nickname the nickname of the player who reconnected
     * @param view     the new VirtualView instance for the player
     * @throws BusinessLogicException if no player with the given nickname exists
     */
    public void markReconnected(String nickname, VirtualView view) throws BusinessLogicException {
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

    /**
     * Handles the elimination of a player by setting their phase to WAITING_FOR_TURN,
     * informing them of their elimination, and broadcasting the elimination to all
     * other connected players.
     *
     * @param p the Player instance to eliminate
     * @throws BusinessLogicException if the player's nickname cannot be resolved
     */

    public void handleElimination(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);

        p.setGamePhase(GamePhase.WAITING_FOR_TURN);

        String msg = "\nYou have been eliminated!";
        informAndNotify(msg, nick);
        String msg1 = "\nPlayer "+nick+" has been eliminated!";
        List<String> nicknames = new ArrayList<>(viewsByNickname.keySet());
        for(String nickname : nicknames) {
            if(nickname.equals(nick)) continue;
            Player player = getPlayerByNickname(nickname);
            if(player.isConnected()) inform(msg1, nickname);
        }
    }

    /**
     * Reinitializes transient state after deserialization of the Controller:
     * clears existing views, creates a new Hourglass with the provided listener,
     * re-creates serializers, sets the end-of-game listener, and restarts pinging.
     *
     * @param hourglassListener   callback for hourglass state changes
     * @param onGameEndListener   callback to invoke when the game ends
     */
    public void reinitializeAfterLoad(Consumer<Hourglass> hourglassListener, Consumer<Integer> onGameEndListener) {
        this.viewsByNickname = new ConcurrentHashMap<>();
        this.hourglass = new Hourglass(hourglassListener);
        this.cardSerializer = new CardSerializer();
        this.tileSerializer = new TileSerializer();
        this.enumSerializer = new EnumSerializer();
        this.onGameEnd = onGameEndListener;
        initPingScheduler();
    }


    /**
     * Indicates whether the game has started. Returns true if any player
     * is in a phase other than WAITING_FOR_PLAYERS.
     *
     * @return true if at least one player’s phase is not WAITING_FOR_PLAYERS
     */
    public boolean isGameStarted() {
        return playersByNickname.values().stream()
                .anyMatch(p -> p.getGamePhase() != GamePhase.WAITING_FOR_PLAYERS);
    }

    /**
     * Returns the maximum number of players allowed in this game.
     *
     * @return the configured maximum player count
     */
    public int getMaxPlayers(){ return MaxPlayers; }

    /**
     * Cancels the scheduled timeout task for a single remaining player, if any.
     */
    private void cancelLastPlayerTimeout() {
        if (lastPlayerTask != null) {
            lastPlayerTask.cancel(false);
            lastPlayerTask = null;
        }
    }


    /**
     * Schedules a timeout to automatically end the game and declare a winner
     * if only one player remains connected for one minute.
     * Cancels any previous single-player timeout before scheduling.
     */
    private void setTimeout() {
        cancelLastPlayerTimeout();
        if (countConnectedPlayers() == 1) {
            lastPlayerTask = TIMEOUT_EXECUTOR.schedule(() -> {
                try {
                    String winner = playersByNickname.entrySet().stream()
                            .filter(e -> e.getValue().isConnected())
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);

                    if (winner != null) {
                        inform("\nYou win by timeout!", winner);
                        Player p = getPlayerCheck(winner);
                        p.setGamePhase(GamePhase.EXIT);
                        updateGamePhase(winner, getViewCheck(winner), GamePhase.EXIT);
                    }
                    if(onGameEnd != null){
                        onGameEnd.accept(gameId);
                    }
                } catch (BusinessLogicException e) {
                    e.printStackTrace();
                }
            }, 1, TimeUnit.MINUTES);
        }
    }


    /**
     * Starts the game by transitioning all players to the BOARD_SETUP phase.
     * For each player:
     * - Updates their game phase to BOARD_SETUP
     * - Sends the full map with all player positions
     * - Sends the initial tile (central housing unit) and full dashboard matrix
     * - Notifies the player to initialize their view and enables input
     * If the game is not in demo mode, it also starts the hourglass timer
     * for turn management or player inactivity.
     * Any communication failure with a player marks them as disconnected.
     */
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

    /**
     * Draws a covered tile from the pile and assigns it to the specified player.
     * The drawn tile is stored as the player's last tile and their game phase is
     * updated to TILE_MANAGEMENT. The view is also updated accordingly.
     * @param nickname the nickname of the player requesting a tile
     * @return the serialized JSON representation of the drawn tile
     * @throws BusinessLogicException if the pile is empty or serialization fails
     */
    public String getCoveredTile(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        int size = getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        Tile t = getTile(0);
        p.setLastTile(t);

        p.setGamePhase(GamePhase.TILE_MANAGEMENT);
        updateGamePhase(nickname, viewsByNickname.get(nickname), GamePhase.TILE_MANAGEMENT);
        notifyView(nickname);

        try {
            return tileSerializer.toJson(t);
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException("Error serializing tile to JSON", e);
        }
    }

    /**
     * Allows a player to select a tile from the list of uncovered (shown) tiles.
     * If the tile is available, it is removed from the shown list and set as the player's
     * last selected tile. The player's phase is set to TILE_MANAGEMENT and the view is updated.
     * @param nickname the player making the selection
     * @param idTile the ID of the tile to select
     * @return the selected tile serialized as JSON
     * @throws BusinessLogicException if the tile is not found or has already been taken
     */
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
            throw new BusinessLogicException("Error serializing tile to JSON", e);
        }
    }

    /**
     * Allows a player to drop (return) a tile, making it available again to others.
     * The dropped tile is deserialized and added back to the shown tile list.
     * The player's phase is reverted to BOARD_SETUP and the view is updated.
     * @param nickname the player dropping the tile
     * @param tile the tile to drop, serialized as JSON
     * @throws BusinessLogicException if deserialization fails
     */
    public void dropTile (String nickname, String tile) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        try {
            addToShownTile(tileSerializer.fromJson(tile));
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException("Error serializing tile to JSON", e);
        }

        p.setGamePhase(GamePhase.BOARD_SETUP);
        updateGamePhase(nickname, viewsByNickname.get(nickname), GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    /**
     * Places a tile on the player's dashboard at the specified coordinates.
     * The tile is deserialized and placed at the given (row, column) if valid.
     * The player's phase is set back to BOARD_SETUP and the view is updated.
     * @param nickname the player placing the tile
     * @param tile the tile to place, serialized as JSON
     * @param cord an array containing [row, column] coordinates
     * @throws BusinessLogicException if placement is invalid or deserialization fails
     */
    public void placeTile(String nickname, String tile, int[] cord) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        try {
            p.addTile(cord[0], cord[1], tileSerializer.fromJson(tile));
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException("Error serializing tile to JSON", e);
        }
        p.setGamePhase(GamePhase.BOARD_SETUP);
        notifyView(nickname);
    }

    /**
     * Retrieves a reserved tile from the player's discard pile by ID.
     * If found, the tile is removed from the discard pile and set as the
     * player's last tile. The game phase is set to TILE_MANAGEMENT_AFTER_RESERVED.
     * @param nickname the player retrieving the tile
     * @param id the ID of the reserved tile
     * @return the tile serialized as JSON
     * @throws BusinessLogicException if the tile is not found or serialization fails
     */
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
                    throw new BusinessLogicException("Error serializing tile to JSON", e);
                }
            }
        }
        throw new BusinessLogicException("Tile not found");
    }

    /**
     * Marks the player as ready to start the flight phase.
     * Updates the player's phase to WAITING_FOR_PLAYERS and prints their dashboard.
     * If all connected players are ready, starts the flight phase automatically.
     * @param nickname the player declaring readiness
     * @throws BusinessLogicException if the player is invalid or flight prep fails
     * @throws RemoteException if communication with the client fails
     */
    public void setReady(String nickname) throws BusinessLogicException, RemoteException {
        Player p = getPlayerCheck(nickname);

        getFlightCardBoard().setPlayerReadyToFly(p, isDemo);

        p.setGamePhase(GamePhase.WAITING_FOR_PLAYERS);
        printPlayerDashboard(viewsByNickname.get(nickname),playersByNickname.get(nickname),nickname);
        playersPosition = buildPlayersPositionMap();

        if(playersByNickname.values().stream().filter(Player::isConnected).allMatch(e -> e.getGamePhase() == GamePhase.WAITING_FOR_PLAYERS)) {
            startFlight();
            return;
        }

        notifyView(nickname);
    }

    /**
     * Begins the flight phase of the game.
     * <p>
     * If not in demo mode, cancels the hourglass timer and merges the remaining card decks.
     * Marks any players not in the flight order as ready to fly, rebuilds player positions,
     * and broadcasts a “Flight started!” notification.
     * Then checks each player’s ship assembly, adds crewmates where needed,
     * sets all players to WAITING_FOR_TURN, and initiates the draw phase.
     * </p>
     *
     * @throws BusinessLogicException if an error occurs in game logic during setup of the flight phase
     */
    public void startFlight() throws BusinessLogicException {

        if(!isDemo){
            hourglass.cancel();
            mergeDecks();
        }

        List<Player> playersInFlight = fBoard.getOrderedPlayers();
        for(Player p : playersByNickname.values()) if(!playersInFlight.contains(p)) fBoard.setPlayerReadyToFly(p, isDemo);

        playersPosition = buildPlayersPositionMap();
        broadcastInform("Flight started!");

        for (String nick : viewsByNickname.keySet()) checkPlayerAssembly(nick, 2, 3);
        addHuman();

        playersByNickname.forEach( (s, p) -> p.setGamePhase(GamePhase.WAITING_FOR_TURN));

        activateDrawPhase();
    }

    /**
     * Selects the next leader among connected players to draw a card,
     * notifies that player to draw, and sets all others to WAITING_FOR_TURN.
     *
     * @throws BusinessLogicException if no players are currently connected
     */
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
                v.inform("\nYou're the leader! Draw a card");
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


    /**
     * Starts the hourglass timer and broadcasts a start message to all players.
     */
    public void startHourglass(){
        hourglass.flip();
        broadcastInform("Hourglass started!");
    }

    /**
     * Attempts to flip the hourglass for the given player, enforcing flip limits
     * and game-phase requirements, and broadcasts appropriate messages or errors.
     *
     * @param nickname the nickname of the player requesting the flip
     * @throws BusinessLogicException if the flip is not allowed by game rules
     */
    public void flipHourglass (String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        int flips = hourglass.getFlips();
        HourglassState state = hourglass.getState();

        switch(flips){
            case 1:
                if(state == HourglassState.EXPIRED){
                    hourglass.flip();
                    broadcastInform("\nHourglass flipped a second time!");
                } else {
                    String msg = "You cannot flip the hourglass: It's still running";
                    reportError(msg, nickname);
                }
                break;
            case 2:
                if(state == HourglassState.ONGOING){
                    String msg = "You cannot flip the hourglass: It's still running";
                    reportError(msg, nickname);
                } else if (state == HourglassState.EXPIRED && p.getGamePhase() == GamePhase.WAITING_FOR_PLAYERS) {
                    hourglass.flip();
                    broadcastInform("\nHourglass flipped the last time!");
                } else {
                    String msg = "You cannot flip the hourglass for the last time: " +
                            "You are not ready";
                    reportError(msg, nickname);
                }
                break;
            default: throw new BusinessLogicException("\nImpossible to flip the hourglass another time!");
        }
    }

    /**
     * Called when the hourglass state expires. Broadcasts messages
     * based on the number of flips and starts the flight phase on the third expiration.
     *
     * @param h the Hourglass instance whose state changed
     * @throws BusinessLogicException if an error occurs while starting the flight
     */
    public void onHourglassStateChange(Hourglass h) throws BusinessLogicException {
        int flips = h.getFlips();

        switch (flips) {
            case 1:
                broadcastInform("\nFirst Hourglass expired");
                break;
            case 2:
                broadcastInform("\nSecond Hourglass expired");
                break;
            case 3:
                broadcastInform("\nTime’s up! Building phase ended.");
                startFlight();
                break;
        }
    }

    /**
     * Draws the next card from the deck and manages the sequence of
     * phases and notifications for all players.
     * <p>
     * - Draws a card and sets the drawer’s phase to WAITING_FOR_TURN.<br>
     * - Broadcasts a “Card drawn!” message.<br>
     * - For each connected, non-eliminated player: sets phase to CARD_EFFECT,
     *   updates their view, and sends them the drawn card.<br>
     * - Activates the card’s effect, then broadcasts “end of card's effect”.<br>
     * - If the deck is empty or all players eliminated, moves to awards phase;<br>
     *   otherwise, asks each remaining player (in parallel) whether they
     *   wish to abandon the flight, handles eliminations, re-orders players,
     *   and either starts the awards phase or continues with the next draw.
     * </p>
     *
     * @param nickname the nickname of the player who initiated the draw
     * @throws BusinessLogicException if drawing the card or applying game logic fails
     */
    public void drawCardManagement(String nickname) throws BusinessLogicException {
        Card card = deck.draw();
        
        Player drawer = getPlayerCheck(nickname);
        drawer.setGamePhase(GamePhase.WAITING_FOR_TURN);
        updateGamePhase(nickname, getViewCheck(nickname), GamePhase.WAITING_FOR_TURN);

        broadcastInform("\nCard drawn!");

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
        broadcastInform("\nEnd of card's effect");

        if(deck.isEmpty()){
            broadcastInform("All cards drawn");
            startAwardsPhase();
        } else if (fBoard.getOrderedPlayers().isEmpty()){
            broadcastInform("All players eliminated");
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
                                askPlayerDecision("\nDo you want to abandon the flight? ", p)
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
                broadcastInform("All players eliminated");
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

    /**
     * Calculates and distributes end-of-flight rewards and maluses,
     * updates player credits and phases, and notifies each player of their final score.
     * <p>
     * - Computes arrival bonuses, best-ship bonus, cargo bonuses (with half factor
     *   for eliminated players), and broken-tile maluses.<br>
     * - Applies each bonus/malus to players’ credit totals.<br>
     * - Sets all players’ phase to EXIT, informs them of win/loss and game end,
     *   and triggers the onGameEnd callback if present.<br>
     * - Finally, notifies all clients of the updated END-phase state.
     * </p>
     *
     * @throws BusinessLogicException if an error occurs during credit calculation or notification
     */
    public void startAwardsPhase() throws BusinessLogicException {

        broadcastInform("\nFlight ended! Time to collect rewards!");

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
            int j = i+1;
            inform("You received "+ arrivalBonus[i]+ " because you arrived "+ j, getNickByPlayer(orderedPlayers.get(i)));
        }

        for (Player p : bestShipPlayers) {
            p.addCredits(bonusBestShip);
            inform("You have received "+bonusBestShip+" credits because of the Best Ship", getNickByPlayer(p));
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

            String nick = getNickByPlayer(p);
            int totalCredits = p.getCredits();
            notifyView(getNickByPlayer(p));
            p.setGamePhase(GamePhase.EXIT);
            inform("You received "+totalDouble+" credits from selling goods", getNickByPlayer(p));
            inform("You lost "+ numBrokenTiles * malusBrokenTile + " credits because of your broken tiles", getNickByPlayer(p));
            if(p.isConnected()){
                if(totalCredits>0) inform("\nYour total credits are: " + totalCredits + "\nYOU WON!", nick);
                else inform("Your total credits are: " + totalCredits + "\nYOU LOST!", nick);

                informAndNotify("Game over. Thank you for playing!", nick);
            }
        }
        if (onGameEnd != null) {
            onGameEnd.accept(gameId);
        }
        for (Player p : playersByNickname.values()) {
            String nick = getNickByPlayer(p);
            notifyView(nick);
        }
    }

    /**
     * Serializes and returns the full list of cards in the specified deck.
     * This is typically used for debug or spectator features to inspect deck contents.
     * @param idxDeck the index of the deck to inspect
     * @return a JSON array string representing the list of cards, or null if serialization fails
     */
    public String showDeck (int idxDeck){
        try {
            return cardSerializer.toJsonList(new ArrayList<>(decks.get(idxDeck).getCards()));
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in showDeck: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the player's ship dashboard as a serialized 2D matrix of tiles.
     * Used to allow one player to inspect another player's ship state.
     * @param nickname the name of the player whose dashboard is requested
     * @return a 2D array of JSON strings representing the tiles
     * @throws BusinessLogicException if the player is invalid or serialization fails
     */
    public String[][] lookAtDashBoard(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);
        try {
            return tileSerializer.toJsonMatrix(p.getDashMatrix());
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(e.getMessage());
        }
    }

    /**
     * Prompts a connected player with a yes/no question and waits for their response.
     * If the player is disconnected or an error occurs (including timeout), returns false.
     *
     * @param question the question to present to the player
     * @param p        the Player instance to query
     * @return true if the player answered “yes”; false on “no”, timeout, disconnection, or error
     * @throws BusinessLogicException if the nickname-to-player lookup fails
     */
    public boolean askPlayerDecision(String question, Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if (!p.isConnected()) return false;

        try {
            return v.askWithTimeout(question);
        } catch (IOException e) {
            System.err.println("Error in IOException in askPlayerDecision for " + nick + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            markDisconnected(nick);
            System.err.println("Error in askPlayerDecision: " + e);
            return false;
        }

    }

    /**
     * Prompts a connected player to supply coordinate input within a timeout.
     * Returns null if the player is disconnected, times out, or an error occurs.
     *
     * @param p the Player instance to query
     * @return an int array [x, y] of the coordinates, or null on timeout, disconnection, or error
     * @throws BusinessLogicException if the nickname-to-player lookup fails
     */
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

    /**
     * Prompts a connected player to choose an index within a valid range.
     * Repeats on invalid input, returns null on timeout, disconnection, or error.
     *
     * @param p   the Player instance to query
     * @param len the exclusive upper bound on the valid index (0 ≤ index < len)
     * @return the chosen index if valid; null on timeout, disconnection, or error
     * @throws BusinessLogicException if the nickname-to-player lookup fails
     */
    public Integer askPlayerIndex(Player p, int len) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView v = viewsByNickname.get(nick);

        if (!p.isConnected()) return null;

        while (true) {
            try {
                Integer idx = v.askIndexWithTimeout();

                if (idx == null) return null;

                if (idx < 0 || idx >= len) {
                    reportError("Index out of range. Please try again", nick);
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

    /**
     * Adds a tile to the list of currently visible (uncovered) tiles.
     * This list is used during the tile selection phase when players choose
     * tiles from the shared revealed pool.
     * @param tile the tile to add to the shownTile list
     */
    public void addToShownTile(Tile tile) {
        shownTile.add(tile);
    }

    /**
     * Retrieves and removes the tile at the specified index from the shown tile list.
     * @param index the index of the tile to retrieve
     * @return the selected tile
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Tile getShownTile(int index) {
        Tile tmp = shownTile.get(index);
        shownTile.remove(index);
        return tmp;
    }

    /**
     * Returns the list of covered tiles remaining in the main tile pile.
     * @return the list of covered tiles
     */
    public List<Tile> getPileOfTile() {
        return pileOfTile;
    }

    /**
     * Returns the current list of uncovered (visible) tiles available for selection.
     * @return the list of shown tiles
     */
    public List<Tile> getShownTiles(){
        return shownTile;
    }

    /**
     * Returns the list of shown tiles serialized as a JSON array string.
     * If the list is empty, returns the string "CODE404".
     * @return a JSON string of shown tiles, or "CODE404" if the list is empty
     */
    public String jsongetShownTiles(){
        try {
            if(shownTile.isEmpty()) return "CODE404";
            return tileSerializer.toJsonList(shownTile);
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in jsongetShownTiles: " + e);
        }
        return null;
    }

    /**
     * Retrieves and removes a tile from the main tile pile at the given index.
     * Used when a player draws a covered tile.
     * @param index the index of the tile to retrieve
     * @return the selected tile
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Tile getTile(int index) {
        Tile tmp = pileOfTile.get(index);
        pileOfTile.remove(index);
        return tmp;
    }

    /**
     * Counts the total number of crew slots (including aliens) on the player's ship.
     * Iterates through all housing units on the ship and sums their capacity.
     * @param p the player whose crew count is requested
     * @return the total number of crew slots
     */
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

    /**
     * Returns whether the current game is in demo mode.
     * @return true if demo mode is active, false otherwise
     */
    public boolean getIsDemo(){
        return isDemo;
    }

    /**
     * this method return the engine power, checking every tile
     * this method checks even if there is a double engine and ask the player if they want to activate it
     * the method calls selectedEnergyCell, and when they return true, it activates it
     * also it checks if there is the brown alien, with the flag on the player and adds the bonus
     * @param p the player whose engine power is being calculated
     * @return the total amount of engine power
     * @throws BusinessLogicException if the player reference is invalid
     */
    public int getPowerEngineForCard(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                boolean var;
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
                    default -> {}
                }
            }
        }
        if (p.presenceBrownAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    /**
     * Calculates the total engine power of a player's ship.
     * The method sums:
     * - +1 for each single engine
     * - +2 for each double engine
     * - +2 bonus if the player has a brown alien and at least one engine
     * @param p the player whose engine power is being calculated
     * @return the total engine power
     * @throws BusinessLogicException if the player reference is invalid
     */
    public int getPowerEngine(Player p) throws BusinessLogicException {
        int tmp = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                boolean var;
                switch (y) {
                    case Engine c -> {
                        var = c.isDouble();
                        if (var) {
                            tmp = tmp + 2;

                        } else {
                            tmp = tmp + 1;
                        }
                    }
                    default -> {}
                }
            }
        }
        if (p.presenceBrownAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    /**
     * Calculates the total firepower of a player's ship during card resolution.
     * Firepower is computed as follows:
     * - +1 or +2 for each activated double cannon (requires energy)
     * - +0.5 or +1 for each single cannon depending on its orientation
     * - +2 bonus if the player has a purple alien and at least one cannon
     * The method prompts the player to choose whether to activate double cannons,
     * and takes orientation into account for both single and double cannons.
     * @param p the player whose firepower is being calculated
     * @return the total firepower (maybe fractional)
     * @throws BusinessLogicException if player lookup or energy management fails
     */
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
                    default -> {}
                }

            }
        }
        if (p.presencePurpleAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    /**
     * Calculates the passive total firepower of a player's ship.
     * Firepower is computed based on cannon tiles:
     * - +2 for a double cannon facing outward (type 5), otherwise +1
     * - +1 for a single cannon facing outward (type 4), otherwise +0.5
     * This version does not prompt for energy activation (unlike getFirePowerForCard).
     * A +2 bonus is added if the player has a purple alien and at least one cannon.
     * @param p the player whose firepower is being calculated
     * @return the total firepower (maybe fractional)
     * @throws BusinessLogicException if player lookup fails
     */
    public double getFirePower(Player p) throws BusinessLogicException {
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
                    default -> {}
                }

            }
        }
        if (p.presencePurpleAlien() && tmp != 0) {
            return tmp + 2;
        } else {
            return tmp;
        }
    }

    /**
     * Returns the total amount of stored energy on the player's ship.
     * Delegates to the player's own method for computing the sum of all energy cell capacities.
     * @param p the player whose energy count is requested
     * @return the total number of energy units available
     */
    public int getTotalEnergy(Player p) {
        return p.getTotalEnergy();
    }

    /**
     * Returns the total amount of stored goods on the player's ship.
     * Delegates to the player's own method for computing the sum of all goods.
     * @param p the player whose goods count is requested
     * @return the total number of goods units present
     */
    public int getTotalGood(Player p) {
        return p.getTotalGood();
    }

    /**
     * Checks whether the player has any remaining crew members and eliminates them if not.
     * If the player's total number of human tokens is zero, they are marked as eliminated.
     * @param p the player to check
     * @return true if the player was eliminated, false otherwise
     */
    public boolean manageIfPlayerEliminated(Player p) {
        int tmp= p.getTotalHuman();
        if(tmp == 0){
            p.setEliminated();
            return true;
        }else{
            return false;
        }
    }

    /**
     * Automatically removes a specified number of goods from the player's storage units.
     * The method scans the dashboard from top-left to bottom-right and removes goods
     * in order of appearance until the requested number is removed or none remain.
     * @param p the player from whom to remove goods
     * @param numOfGoods the total number of goods to remove
     */
    public void autoCommandForRemoveGoods(Player p, int numOfGoods) {
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

    /**
     * Automatically removes the first instance of a good of the specified colour from the player's storage units.
     * The method searches all storage units in row-major order and removes the first match.
     * @param p the player from whom to remove the good
     * @param col the colour of the good to remove
     */
    public void autoCommandForRemoveSingleGood(Player p, Colour col) {
        for(int i=0; i<5; i++){
            for(int j=0; j<7; j++){
                Tile tmp = p.getTile(i, j);
                switch (tmp){
                    case StorageUnit c -> {
                        List<Colour> list = c.getListOfGoods();
                        if(list.isEmpty()) {}
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

    /**
     * Automatically removes a specified number of crew tokens from the player's housing units.
     * The method scans the dashboard from top-left to bottom-right and removes crew tokens
     * in order of appearance until the requested number is removed or none remain.
     * @param p the player from whom to remove crew tokens
     * @param num the total number of crew to remove
     * @throws BusinessLogicException if removal from a housing unit fails
     */
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
                            while (flag > 0 && !h.getListOfToken().isEmpty()) {
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
        printPlayerDashboard(getViewCheck(getNickByPlayer(p)),p,getNickByPlayer(p));
    }

    /**
     * Automatically removes the specified number of batteries from the player's energy cells.
     * Batteries are removed in row-major order until the required amount is deducted
     * or no capacity remains.
     * @param p the player from whom to remove batteries
     * @param num the number of batteries to remove
     * @throws BusinessLogicException if removal fails due to invalid state
     */
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

    /**
     * Removes a specified number of goods (and possibly batteries) from the player's ship.
     * The method handles three scenarios:
     * 1. All goods are lost — removes everything automatically.
     * 2. Fewer goods lost — prompts the player (if connected) to choose which goods to discard.
     * 3. More goods lost than owned — removes all goods and deducts remaining loss from energy.
     * If the player is disconnected, removal is fully automatic.
     * @param p the player whose goods are being removed
     * @param num the total number of goods (or equivalent value) to remove
     * @throws BusinessLogicException if a game rule is violated
     */
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

        if(num == totalGood){
            autoCommandForRemoveGoods(p, totalGood);
            if(!p.isConnected()) return;
            inform("You have lost all your goods", nick);

        } else if(num < totalGood){
            if(!p.isConnected()) {
                autoCommandForRemoveGoods(p, num);
                return;
            }

            while(num != 0){
                if(r != 0){
                    if(p.isConnected()){
                        inform("Select a storage unit to remove a red good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.RED);
                            r--;
                            num--;
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
                                if(!flag) reportError("There are not red goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> reportError("Not valid cell. Try again", nick);
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
                        inform("Select a storage unit to remove a yellow good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.YELLOW);
                            g--;
                            num--;
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
                                if(!flag) reportError("There are not yellow goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> reportError("Not valid cell. Try again", nick);
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
                        inform("Select a storage unit to remove a green good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.GREEN);
                            v--;
                            num--;
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
                                if(!flag) reportError("There are not green goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> reportError("Not valid cell. Try again", nick);
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
                        inform("Select a storage unit to remove a blue good from", nick);
                        printPlayerDashboard(x, p, nick);
                        int[] vari = askPlayerCoordinates(p);

                        if(vari==null){
                            autoCommandForRemoveSingleGood(p, Colour.BLUE);
                            b--;
                            num--;
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
                                if(!flag) reportError("There are not blue goods in this storage unit. Try again", nick);
                                else flag=false;
                            }
                            default -> reportError("Not valid cell. Try again", nick);
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
        }else { 
            autoCommandForRemoveGoods(p, totalGood);
            int finish = num-totalGood;

            if(!p.isConnected()) {
                autoCommandForBattery(p, finish);
                return;
            }

            if(totalGood > 0) inform("You have lost all your goods", nick);
            else inform("You don't have any good to remove", nick);

            if(finish < totalEnergy){
                inform("You will lose "+finish+" battery/ies", nick);
                while(finish > 0){
                    if(p.isConnected()){
                        inform("Select an energy cell to remove a battery from", nick);
                        int[] vari = askPlayerCoordinates(p);
                        printPlayerDashboard(x, p, nick);

                        if(vari==null){
                            autoCommandForBattery(p, 1);
                            finish--;
                            continue;
                        }

                        Tile y = p.getTile(vari[0], vari[1]);
                        switch (y){
                            case EnergyCell c -> {
                                if(c.getCapacity() != 0){
                                    c.useBattery();
                                    finish--;
                                } else reportError("Empty energy cell. Try again", nick);
                            }
                            default -> reportError("Not valid cell. Try again", nick);
                        }
                    }else{
                        autoCommandForBattery(p, 1);
                    }
                }
            }else{
                autoCommandForBattery(p, totalEnergy);
                if(!p.isConnected()) return;

                if(totalEnergy>0){
                    inform("You have lost all your batteries", nick);
                }
                else inform("You don't have any batteries to remove", nick);
            }
        }
        printPlayerDashboard(x, p, nick);
    }

    /**
     * Provides an interactive loop for the player to manage their collected goods.
     * The player can:
     * - Add goods to storage units
     * - Rearrange goods across units
     * - Trash unwanted goods
     * Prompts continue until the player chooses to exit.
     * @param p the player managing goods
     * @param list the list of goods currently available to place
     * @throws BusinessLogicException if a rule is violated during interaction
     */
    public void manageGoods(Player p, List<Colour> list) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);
        boolean flag = true;

        while(flag){
            inform("SELECT:\n 1. Add good\n 2. Rearranges the goods\n 3. Trash some goods", nick);
            Integer tmp = askPlayerIndex(p, 3);

            if(tmp!=null){
                switch (tmp){
                    case 0 -> addGoods(p, x, list, nick);
                    case 1 -> caseRedistribution(p, x, list, nick);
                    case 2 -> caseRemove(p,x,nick);
                    default -> {
                        reportError("Invalid choice. Please try again", nick);
                        continue;
                    }
                }
            }

            if(!askPlayerDecision("Do you want to continue to manage your goods?", p)) flag = false;
        }
    }

    /**
     * Allows the player to add goods from the provided list to available storage units.
     * If a storage unit is full, the player is prompted to remove a stored good.
     * Dangerous (red) goods can only be placed in advanced storage units.
     * The loop continues until the player chooses to stop or the list is empty.
     * @param p the player placing the goods
     * @param x the VirtualView associated with the player
     * @param list the list of goods to place
     * @param nick the nickname of the player (for messaging)
     * @throws BusinessLogicException if adding/removing goods fails
     */
    public void addGoods(Player p, VirtualView x, List<Colour> list, String nick) throws BusinessLogicException {
        boolean flag = true;
        Colour tempGood = null;

        if(list.isEmpty()){
            reportError("Empty list of goods", nick);
            return;
        }

        while (!list.isEmpty() && flag) {
            inform("Select a storage unit", nick);
            printPlayerDashboard(x, p, nick);

            int[] vari = askPlayerCoordinates(p);

            Tile t;
            if(vari==null) t = p.getTile(2,3);
            else t = p.getTile(vari[0], vari[1]);

            switch (t){
                case StorageUnit c -> {
                    if(c.isFull()){
                        inform("Full Storage Unit\nSelect the index of the good in the storage unit to remove", nick);
                        List<Colour> listGoods = c.getListOfGoods();

                        printListOfGoods(listGoods, nick);
                        Integer tmpint = askPlayerIndex(p, listGoods.size());

                        if(tmpint==null) tmpint = 0;
                        int idx = tmpint;

                        Colour tmp = c.getListOfGoods().get(idx);
                        tempGood = c.removeGood(idx);
                        list.add(tmp);
                    }

                    inform("Select the index of the good to place", nick);

                    printListOfGoods(list, nick);
                    Integer tmpint = askPlayerIndex(p, list.size());

                    if(tmpint==null) tmpint = 0;
                    int idx = tmpint;

                    if(list.get(idx) == Colour.RED){
                        if(c.isAdvanced()) {
                            c.addGood(list.get(idx));
                            list.remove(idx);
                        }
                        else {
                            reportError("You can't place a dangerous good in a not advanced storage unit", nick);
                            if(tempGood!=null){
                                c.addGood(tempGood);
                                list.remove(tempGood);
                                tempGood = null;
                            }

                        }
                    }else {
                        c.addGood(list.get(idx));
                        list.remove(idx);
                    }
                }
                default -> reportError("Not valid cell", nick);
            }
            printPlayerDashboard(x, p, nick);

            if(!askPlayerDecision("Do you want to continue to add goods?", p)) flag = false;
        }

        if(flag) reportError("Empty list of goods", nick);
    }

    /**
     * Allows the player to rearrange goods between different storage units.
     * The player selects a source storage unit, chooses a good to remove,
     * and then selects a destination unit to place it in. This process
     * can be repeated until the player decides to stop.
     * Validity checks are performed to ensure the selected cell is a storage unit
     * and not empty. Red goods can only be placed in advanced units.
     * @param p the player performing the rearrangement
     * @param v the VirtualView used to display the dashboard
     * @param list unused (reserved for future logic or shared reference)
     * @param nick the player's nickname for messaging
     * @throws BusinessLogicException if a game rule is violated during the operation
     */
    public void caseRedistribution(Player p , VirtualView v , List<Colour> list , String nick) throws BusinessLogicException {
        printPlayerDashboard(v, p, nick);

        int[] coordinates;
        boolean exit = true;

        while (exit) {
            inform("Select a storage unit to take the good from", nick);
            coordinates = askPlayerCoordinates(p);

            Tile tmp2;
            if(coordinates==null) tmp2 = p.getTile(2,3);
            else tmp2 = p.getTile(coordinates[0], coordinates[1]);

            switch (tmp2) {
                case StorageUnit c -> {
                    List<Colour> tmplist = c.getListOfGoods();

                    if(tmplist.isEmpty()){
                        reportError("Empty storage unit", nick);
                        break;
                    }

                    printListOfGoods(tmplist, nick);
                    inform("Select the index of the good you want to rearrange", nick);
                    Integer tmpint = askPlayerIndex(p, tmplist.size());
                    if(tmpint==null) tmpint = 0;
                    int idx = tmpint;

                    Colour tmpColor = tmplist.get(idx);
                    c.removeGood(idx);
                    selectStorageUnitForAdd(v, p, tmpColor, nick);
                    printPlayerDashboard(v, p, nick);
                }
                default -> reportError("Not valid cell", nick);
            }
            printPlayerDashboard(v, p, nick);
            if(!askPlayerDecision("Do you want to select another storage unit for the rearranging?", p)) exit = false;
        }
    }

    /**
     * Prompts the player to select a valid storage unit to place a given good.
     * The method loops until the player selects a valid storage unit:
     * - If the unit is full, an error is shown and the prompt repeats
     * - If the good is red, it can only be added to an advanced unit
     * - For all other goods, placement is allowed if space is available
     * The operation ends once the good is successfully placed.
     * @param v the VirtualView used to display the updated dashboard
     * @param p the player placing the good
     * @param color the colour of the good to be placed
     * @param nick the player's nickname for communication
     * @throws BusinessLogicException if the placement fails due to game rules
     */
    public void selectStorageUnitForAdd(VirtualView v, Player p , Colour color , String nick) throws BusinessLogicException {
        int[] coordinates;
        boolean exit = false;

        while (!exit) {
            inform("Select a storage unit to place the good in", nick);
            coordinates = askPlayerCoordinates(p);

            Tile tmp2;
            if(coordinates==null) tmp2 = p.getTile(2,3);
            else tmp2 = p.getTile(coordinates[0], coordinates[1]);

            switch (tmp2) {
                case StorageUnit c -> {
                    if(c.isFull()) {
                        reportError("This storage unit is full. Try again", nick);
                        continue;
                    }
                    if(color == Colour.RED) {
                        if(c.isAdvanced()) {
                            c.addGood(color);
                            exit = true;
                        } else reportError("You can't place a dangerous good in a not advanced storage unit", nick);
                    }else {
                        c.addGood(color);
                        exit = true;
                    }
                }
                default -> {
                    reportError("Not valid cell", nick);
                }
            }
        }
    }


    /**
     * Allows the player to remove (trash) goods from their storage units.
     * The player is prompted to select a storage unit and choose a good to remove.
     * If no coordinates are selected, a good is removed automatically.
     * The process can be repeated until the player chooses to stop.
     * @param p the player removing the goods
     * @param v the VirtualView used to display the dashboard
     * @param nick the player's nickname for messages and prompts
     * @throws BusinessLogicException if a removal fails or a rule is violated
     */
    public void caseRemove(Player p , VirtualView v , String nick) throws BusinessLogicException {
        int[] coordinates;
        boolean exit = true;
        printPlayerDashboard(v, p, nick);

        while (exit) {
            inform("Select a storage unit", nick);
            coordinates = askPlayerCoordinates(p);

            if(coordinates==null) {
                autoCommandForRemoveGoods(p, 1);
            } else {
                Tile tmp2 = p.getTile(coordinates[0], coordinates[1]);
                switch (tmp2) {
                    case StorageUnit c -> {
                        if(!c.getListOfGoods().isEmpty()) {
                            List<Colour> tmplist = c.getListOfGoods();
                            printListOfGoods(tmplist, nick);
                            inform("Select the index of the good you want to trash", nick);
                            Integer tmpint = askPlayerIndex(p, tmplist.size());
                            if(tmpint==null) tmpint = 0;
                            int idx = tmpint;

                            c.removeGood(idx);
                            printPlayerDashboard(v, p, nick);
                        }else{
                            reportError("Empty list of goods", nick);
                        }

                    }
                    default -> reportError("Not valid cell", nick);
                }
            }
            if(!askPlayerDecision("Do you want to select another storage unit for trashing?", p)) exit = false;
        }
    }

    /**
     * Automatically assigns crew members or aliens to housing units for all players.
     * For each player's dashboard:
     * - If the housing unit supports HUMANS, adds two humans by default
     * - If the housing unit is connected to a special module (purple or brown alien):
     * - Asks the player whether to place the corresponding alien
     * - If confirmed, places the alien and updates the player state
     * Also updates the player dashboard view after assignment.
     * @throws BusinessLogicException if an error occurs during crew placement
     */
    public void addHuman() throws BusinessLogicException {
        broadcastInform("Checking all players' ships");
        for (Player p : playersByNickname.values()) {
            String tmpNick = getNickByPlayer(p);
            VirtualView x = viewsByNickname.get(tmpNick);
            p.setGamePhase(GamePhase.CARD_EFFECT);
            updateGamePhase(tmpNick, x, GamePhase.CARD_EFFECT);
            printPlayerDashboard(x,p ,tmpNick);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++) {
                    Tile t = p.getTile(i, j);
                    switch (t) {
                        case HousingUnit h -> {
                            Human tmp = h.getTypeOfConnections();
                            if (h.getType() == Human.HUMAN){
                                switch (tmp) {
                                    case HUMAN234 -> {
                                        Human tmp2 = Human.HUMAN;
                                        for (int z = 0; z < 2; z++) {
                                            h.addHuman(tmp2);}
                                    }
                                    case PURPLE_ALIEN -> {
                                        if(p.presencePurpleAlien() || (i == 2 && j == 3)) {
                                            Human tmp2 = Human.HUMAN;
                                            for (int z = 0; z < 2; z++) h.addHuman(tmp2);
                                            continue;
                                        }
                                        try {
                                            String msg = "Do you want to place a purple alien in the housing unit " +
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
                                            String msg = "Do you want to place a brown alien in the housing unit " +
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
            printPlayerDashboard(x,p ,tmpNick);
        }
    }

    /**
     * Removes a specified number of crewmates from the player's ship.
     * If the player is connected:
     * - Prompts them to select housing units manually
     * If disconnected:
     * - Automatically removes crewmates from available housing units
     * If the number to remove is greater than or equal to total crew,
     * all crewmates are removed.
     * Also updates the player state if an alien is removed.
     * @param p the player losing crew
     * @param num the number of crewmates to remove
     * @throws BusinessLogicException if the operation fails
     */
    public void removeCrewmates(Player p, int num) throws BusinessLogicException {
        String nick = getNickByPlayer(p);
        VirtualView x = viewsByNickname.get(nick);

        int totalCrew = getNumCrew(p);

        if (num >= totalCrew) {
            autoCommandForRemovePlayers(p, totalCrew);
        } else {
            if(!p.isConnected()){
                autoCommandForRemovePlayers(p, num);
                return;
            }
            while (num > 0) {
                if(p.isConnected()){
                    inform("Select an housing unit", nick);
                    int[] vari = askPlayerCoordinates(p);

                    Tile y;
                    if(vari==null) y = p.getTile(2,3);
                    else y = p.getTile(vari[0], vari[1]);

                    switch (y){
                        case HousingUnit h -> {
                            if(h.returnLenght()>0){
                                int tmp = h.removeHumans(0);
                                if(tmp == 2) p.setBrownAlien();
                                if(tmp == 3) p.setPurpleAlien();
                                num--;
                            }else{
                                reportError("Select a valid housing unit", nick);
                            }
                        }
                        default -> reportError("Select a valid housing unit", nick);
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
    /**
     * Simulates a plague event affecting all connected housing units on the player's ship.
     * Each connected housing unit with at least one crew member loses one crewmate.
     * If the removed crewmate is an alien, the corresponding alien flag on the player is cleared.
     * @param p the player affected by the plague
     * @throws BusinessLogicException if crew removal fails
     */
    public void startPlague(Player p) throws BusinessLogicException {
        String nick = getNickByPlayer(p);

        inform("Starting plague", nick);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Tile y = p.getTile(i, j);
                Human tmpHuman;
                switch (y){
                    case HousingUnit h -> {
                        if(h.isConnected() && !h.getListOfToken().isEmpty()){
                            tmpHuman = h.getListOfToken().getFirst();
                            h.removeHumans(0);
                            inform("Connected Housing Unit detected. You lose 1 crewmate", nick);
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
        }
        printPlayerDashboard(viewsByNickname.get(nick), p, nick);
    }

    /**
     * method used for checking the protection of a ship side by shield,
     * return true if it is protected, and they want to use a battery
     *
     * @param d the direction of a small meteorite of cannon_fire
     * @return if the ship is safe
     */
    public boolean isProtected(String nick, int d) throws BusinessLogicException {
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
            inform("You can activate the shield by consuming a battery ", nick);
            String mex = "To activate a shield";
            return manageEnergyCell(nick, mex);
        }
        return false;
    }

    /**
     * Evaluates whether a player's ship can defend against an incoming attack.
     * The method performs the following steps:
     * - Prints an attack warning to the player
     * - Checks if the attack is within the hit zone
     * - If the attack is a big one or the ship is unprotected, applies damage via scriptOfDefence
     * - Otherwise, informs the player that they are protected
     * @param dir the cardinal direction of the attack (0 = North, 1 = East, 2 = South, 3 = West)
     * @param type true if the attack is big, false if small
     * @param dir2 the position index of the attack along the edge
     * @param p the player being attacked
     * @return true if the ship is destroyed, false otherwise
     * @throws BusinessLogicException if tile removal or crew checks fail
     */
    public boolean defenceFromCannon(int dir, boolean type, int dir2, Player p) throws BusinessLogicException {
        String[] directions = {"Nord", "East", "South", "West"};
        String direction = directions[dir];
        String size = type ? "big" : "small";
        String nick = getNickByPlayer(p);
        VirtualView v = getViewCheck(nick);

        String msg = "\nA "+size+" attack is coming from "+direction+" on section "+dir2+"\nShip before attack";
        inform(msg, nick);
        printPlayerDashboard(v, p, nick);

        if (isHitZone(dir, dir2)) {
            if (type || !isProtected(nick, dir)){
                return scriptOfDefence(nick, p, v, dir2 , dir);
            } else {
                inform("You are protected", nick);
            }
        } else {
            inform("Attack out of range. You are safe", nick);
        }

        return false;
    }

    /**
     * Determines whether the given attack is within the valid hit zone.
     * @param dir the cardinal direction of the attack
     * @param dir2 the index of the impact along the ship's border
     * @return true if the attack lands in a vulnerable zone, false otherwise
     */
    public boolean isHitZone(int dir, int dir2) {
        return switch (dir) {
            case 0, 2 -> dir2 > 3 && dir2 < 11;
            case 1, 3 -> dir2 > 4 && dir2 < 10;
            default -> false;
        };
    }

    /**
     * Executes the ship’s defence logic after an attack lands.
     * The method:
     * - Attempts to remove the first tile hit in the attack direction
     * - Checks if the player has lost all crew (in which case the ship is destroyed)
     * - Notifies the player whether the attack was blocked, missed, or successful
     * @param Nickname the nickname of the player
     * @param p the player object
     * @param v the associated virtual view
     * @param dir2 the position index of the impact
     * @param dir the cardinal direction of the attack
     * @return true if the ship was destroyed, false otherwise
     * @throws BusinessLogicException if tile removal or alien status updates fail
     */
    public boolean scriptOfDefence(String Nickname, Player p, VirtualView v, int dir2, int dir) throws BusinessLogicException {
        Boolean tmpBoolean = false;
        switch (dir){
            case 0 ->  tmpBoolean = p.removeFrom0(dir2);
            case 1 ->  tmpBoolean = p.removeFrom1(dir2);
            case 2 ->  tmpBoolean = p.removeFrom2(dir2);
            case 3 ->  tmpBoolean = p.removeFrom3(dir2);
        }

        if(manageIfPlayerEliminated(p)){
            inform("You have lost all your humans. Your ship is destroyed", Nickname);
            p.destroyAll();
            printPlayerDashboard(v, p, Nickname);
            return true;
        }


        if(!tmpBoolean){
            inform("You are safe", Nickname);
        }else{
            inform("You've been hit", Nickname);
            askStartHousingForControl(Nickname);

        }

        return false;
    }

    /**
     * Handles the resolution of a meteorite attack on all connected, non-eliminated players.
     * For each player:
     * - Prints a warning and the ship's current dashboard
     * - Checks whether the meteorite is in a valid hit zone
     * - If the meteorite is big:
     * - It hits unless the tile is protected by a shield
     * - If the meteorite is small:
     * - It hits unless the tile has no exposed connector or is shielded
     * - If a hit occurs, the standard defence script is executed
     * Players who are first in the list, eliminated, or disconnected are skipped.
     * @param dir the direction the meteorite comes from (0 = North, 1 = East, etc.)
     * @param isBig true if the meteorite is big, false if small
     * @param dir2 the index on the border where the meteorite hits
     * @param players the list of players involved in the game
     * @param numMeteorite the meteorite's sequence number (used for logging)
     * @throws BusinessLogicException if ship modification or player status update fails
     */
    public void defenceFromMeteorite(int dir, boolean isBig, int dir2, List<Player> players, int numMeteorite) throws BusinessLogicException {
        String[] directions = {"Nord", "East", "South", "West"};
        String direction = directions[dir];
        String size = isBig ? "big" : "small";

        for(Player p : players){
            if(players.getFirst().equals(p) || !p.isConnected() || p.isEliminated()) continue;
            String nick = getNickByPlayer(p);
            inform("\nWaiting for your turn...", nick);
        }

        for (Player p : players) {
            if(p.isConnected() && !p.isEliminated()){
                String nick = getNickByPlayer(p);
                VirtualView v = getViewCheck(nick);
                inform("A " + size + " meteorite is coming from " + direction + " on section " + dir2, nick);
                inform("Ship before the attack", nick);
                printPlayerDashboard(v, p, nick);

                if (!isHitZone(dir, dir2)) {
                    inform("Meteorite out of range. You are safe", nick);
                    continue;
                }
                if (isBig) {
                    if (!checkProtection(dir, dir2, nick)) {
                        scriptOfDefence(nick, p, v, dir2, dir);
                    } else {
                        inform("Cannon protected you!", nick);
                    }
                } else {
                    boolean noConnector = p.checkNoConnector(dir, dir2);
                    boolean shielded = isProtected(nick, dir);

                    if (!noConnector && !shielded) {
                        scriptOfDefence(nick, p, v, dir2, dir);
                    } else {
                        inform("You are safe", nick);
                    }
                }

                if(players.indexOf(p) != players.size()-1) inform("Checking other players...", nick);
                else broadcastInform(numMeteorite+"° meteorite processed for all players");
            }
        }
    }

    /**
     * Handles user interaction to determine whether to use a battery from an energy cell.
     * The method:
     * - Asks the player if they want to use a battery (with a custom message)
     * - If confirmed, prompts the player to select an energy cell
     * - If the selected cell has available batteries, one is consumed
     * - If not, the player can choose to retry or abort
     * If the player is disconnected, the action is skipped automatically.
     * @param nick the nickname of the player
     * @param mex a custom message displayed to the player (contextual hint)
     * @return true if a battery was successfully used, false otherwise
     * @throws BusinessLogicException if tile access or battery use fails
     */
    public boolean manageEnergyCell(String nick, String mex) throws BusinessLogicException {
        Player player = getPlayerCheck(nick);
        if(!player.isConnected()) return false;
        int[] coordinates;
        boolean exit = false;

        if (!askPlayerDecision("Do you want to use a battery? "+mex, player)) {
            return false;
        } else {
            while (!exit) {
                coordinates = askPlayerCoordinates(player);

                Tile p;
                if(coordinates == null) p = playersByNickname.get(nick).getTile(2,3);
                else p = playersByNickname.get(nick).getTile(coordinates[0], coordinates[1]);

                switch (p) {
                    case EnergyCell c -> {
                        int capacity = c.getCapacity();
                        if (capacity == 0) {
                            reportError("You have already used all the batteries for this cell", nick);
                            if(!askPlayerDecision("Do you want to select another EnergyCell?", player))
                                return false;
                        } else {
                            c.useBattery();
                            return true;
                        }
                    }
                    default -> {
                        reportError("Not valid cell", nick);
                        if(!askPlayerDecision("Do you want to select another EnergyCell?", player))
                            exit = true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Checks whether the player's ship is protected from a big meteorite impact
     * based on the shield configuration and the meteorite direction.
     * The method evaluates if a shield tile protects the specified incoming direction
     * and section of the ship.
     * @param dir the cardinal direction of the meteorite (0 = North, 1 = East, 2 = South, 3 = West)
     * @param dir2 the section index where the meteorite is expected to hit
     * @param player the nickname of the player being evaluated
     * @return true if the ship is protected, false otherwise
     * @throws BusinessLogicException if the player or their tiles cannot be retrieved
     */
    public boolean checkProtection(int dir, int dir2, String player) throws BusinessLogicException {
        return switch (dir) {
            case 0 -> checkColumnProtection(player, dir2 - 4);
            case 1 -> checkRowProtectionFromSide(player, dir2 - 5 , 1);
            case 2 -> checkColumnProtectionFromSouth(player, dir2-4, 2);
            case 3 -> checkRowProtectionFromSide(player, dir2 - 5 , 3);
            default -> false;
        };
    }

    /**
     * Checks if any tile in the specified column of the player's ship provides protection
     * from a North-directed big meteorite via a cannon.
     * Iterates through each row in the column, and for each tile that is actively used (`Status.USED`),
     * checks whether it offers cannon protection in direction 0 (North).
     * @param player the nickname of the player
     * @param col the column index to check (0–6)
     * @return true if at least one tile provides protection in that column, false otherwise
     * @throws BusinessLogicException if tile access fails
     */
    private boolean checkColumnProtection(String player, int col) throws BusinessLogicException {
        for (int row = 0; row < 5; row++) {
            if (playersByNickname.get(player).validityCheck(row, col) == Status.USED) {
                Tile tile = playersByNickname.get(player).getTile(row, col);
                if(isCannonProtected(tile, player, 0)) return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the specified row or its adjacent rows provide protection
     * from a meteorite coming from the East or West side.
     * Evaluates the current row, the row above, and the row below.
     * @param player the nickname of the player
     * @param row the central row to check (0–4)
     * @param direction the attack direction (1 = East, 3 = West)
     * @return true if any tile in the checked rows offers cannon protection in the given direction
     * @throws BusinessLogicException if player data is unavailable
     */
    private boolean checkRowProtectionFromSide(String player, int row, int direction) throws BusinessLogicException {
        for (int r = row - 1; r <= row + 1; r++) {
            if (r < 0 || r >= 5) continue;
            if (checkTileInRow(player, r, direction)) return true;
        }
        return false;
    }

    /**
     * Checks whether any tile in the specified row offers cannon protection
     * in the given direction.
     * Iterates from right to left if direction is East (1),
     * or left to right if direction is West (3).
     * @param player the nickname of the player
     * @param row the row to check
     * @param direction the side to evaluate (1 = East, 3 = West)
     * @return true if a cannon protects the ship in that direction from that row
     * @throws BusinessLogicException if access to the player's tile fails
     */
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

    /**
     * Checks whether the given column or its adjacent columns contain any tile
     * that provides protection from a meteorite coming from the South.
     * Evaluates the column and its immediate neighbors (left and right).
     * @param player the nickname of the player
     * @param column the central column to check (0–6)
     * @param direction the attack direction (expected 2 = South)
     * @return true if protection is found in any of the columns
     * @throws BusinessLogicException if the player's ship state is inaccessible
     */
    private boolean checkColumnProtectionFromSouth(String player, int column, int direction) throws BusinessLogicException {
        for (int c = column - 1; c <= column + 1; c++) {
            if(c < 0 || c>= 7) continue;
            if(checkTileInColumn(player, c, direction)) return true;
        }
        return false;
    }

    /**
     * Checks whether any tile in the specified column offers cannon protection
     * in the given direction.
     * Iterates from bottom to top.
     * @param player the nickname of the player
     * @param column the column to check
     * @param direction the direction to check protection from (typically South)
     * @return true if a tile offers protection, false otherwise
     * @throws BusinessLogicException if the player's data cannot be accessed
     */
    private boolean checkTileInColumn(String player, int column, int direction) throws BusinessLogicException {
        for(int row = 4; row >= 0; row--){
            if (playersByNickname.get(player).validityCheck(row, column) == Status.USED){
                Tile tile = playersByNickname.get(player).getTile(row, column);
                if(isCannonProtected(tile, player, direction)) return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the given tile is a cannon that provides protection
     * in the specified orientation.
     * Handles single and double cannons:
     * - For double cannons, energy is required to activate
     * - For single cannons, protection is granted if the connector is facing outward
     * @param tile the tile to evaluate
     * @param player the nickname of the player (used to interactively request energy use)
     * @param orientation the direction to evaluate (0–3, depending on attack origin)
     * @return true if the cannon provides protection, false otherwise
     * @throws BusinessLogicException if battery activation fails
     */
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

    /**
     * Asks the player to select a starting housing unit to revalidate the ship structure after damage.
     * If the player is connected, prompts them to select a tile until a valid housing unit is chosen.
     * If the player is not connected, the central tile (2,3) is used by default.
     * Once a valid housing unit is selected, the method triggers a ship structure check
     * using the `controlAssembly` method on the player's dashboard.
     * @param nickname the player's nickname
     * @throws BusinessLogicException if tile access or structural validation fails
     */
    public void askStartHousingForControl(String nickname) throws BusinessLogicException {
        Player p = getPlayerCheck(nickname);

        if(p.isConnected()){
            int[] xy;
            boolean flag = true;
            do{
                inform("Choose your starting housing unit:", nickname);
                xy = askPlayerCoordinates(p);

                Tile tmp;
                if(xy == null) {
                    tmp = p.getTile(2,3);
                } else {
                    tmp = p.getTile(xy[0], xy[1]);
                }

                switch (tmp) {
                    case HousingUnit h -> {
                        if(h.getType() == Human.HUMAN) flag = false;
                        else reportError("Not valid position, try again", nickname);
                    }
                    default -> reportError("Not valid position, try again", nickname);
                }
            } while(flag);

            checkPlayerAssembly(nickname, xy != null ? xy[0] : 2, xy != null ? xy[1] : 3);

        }else{
            checkPlayerAssembly(nickname,  2,3);
        }
    }

    /**
     * Triggers a structure validation of the player's ship starting from the specified tile.
     * This method calls `controlAssembly` on the player's dashboard to verify tile connectivity.
     * It also updates the dashboard view to reflect any changes caused by the validation process.
     * @param nick the nickname of the player
     * @param x the row index of the starting tile
     * @param y the column index of the starting tile
     * @throws BusinessLogicException if structural checks fail or player/view access fails
     */
    public void checkPlayerAssembly(String nick, int x, int y) throws BusinessLogicException {
        Player p = getPlayerCheck(nick);
        VirtualView v = getViewCheck(nick);
        p.controlAssembly(x,y);
        printPlayerDashboard(v, p, nick);
    }

    /**
     * Returns the current game phase of the specified player as a string.
     * If serialization fails, returns "EXIT" as fallback.
     * @param nick the player's nickname
     * @return the current game phase as a string, or "EXIT" on error
     */
    public String getGamePhase(String nick){
        try {
            return enumSerializer.serializeGamePhase(playersByNickname.get(nick).getGamePhase());
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in serializeGamePhase: " + e.getMessage());
        }
        return "EXIT";
    }

    /**
     * Returns the player's dashboard matrix serialized as a 2D JSON array of tiles.
     * If serialization fails, returns null.
     * @param nick the player's nickname
     * @return a 2D JSON representation of the player's dashboard, or null on error
     */
    public String[][] getDashJson(String nick){
        try {
            return tileSerializer.toJsonMatrix( playersByNickname.get(nick).getDashMatrix());
        } catch (JsonProcessingException e) {
            System.err.println("[ERROR] in serializeDashMatrix: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the flightCardBoard associated to the game
     * @return flightCardBoard of the game
     */
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

        fBoard.checkIfPlayerOverlapped();
        fBoard.checkIfPlayerNoHumansLeft();
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

    /**
     * Refreshes the internal playersPosition map by rebuilding it from
     * the current state of all Player objects.
     */
    public void changeMapPosition() {
        playersPosition = buildPlayersPositionMap();
    }

    /**
     * Sends the latest playersPosition map to every connected client view.
     * <p>
     * Iterates over all nicknames in playersPosition and invokes
     * {@code updateMapPosition(playersPosition)} on each registered
     * {@link VirtualView} if the corresponding player is connected.
     * Any I/O or runtime failure will mark that player as disconnected.
     * </p>
     *
     * @throws BusinessLogicException if a nickname in playersPosition does not correspond to a known Player
     */
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
    }

    /**
     * Transitions all players into the EXIT phase, notifies their views of
     * the EXIT state, and invokes the game-end callback if present.
     * <p>
     * Sets each Player’s phase to {@link GamePhase#EXIT}, calls
     * {@code updateGameState(EXIT)} on every VirtualView, and finally
     * fires {@code onGameEnd.accept(gameId)}.
     * </p>
     */
    public void setExit()  {
        playersByNickname.values().forEach(p -> p.setGamePhase(GamePhase.EXIT));

        viewsByNickname.forEach((nick, view) -> {
            try {
                view.updateGameState(enumSerializer.serializeGamePhase(GamePhase.EXIT));
            } catch (Exception e) {
                markDisconnected(nick);
            }
        });
        if (onGameEnd != null) {
            onGameEnd.accept(gameId);
        }
    }

    /**
     * Returns a copy of the current playersPosition map, mapping each
     * player nickname to their position data array.
     *
     * @return a new {@code Map<String,int[]>} where each value is
     *         {@code [pos, lap, eliminatedFlag, idPhoto]}
     */
    public Map<String,int[] > getPlayersPosition(){
        return new HashMap<>(playersPosition);
    }

    /**
     * Builds a fresh map of player positions from the internal Player state.
     * <p>
     * For each entry in playersByNickname, extracts:
     * <ul>
     *   <li>current track position ({@code p.getPos()})</li>
     *   <li>current lap ({@code p.getLap()})</li>
     *   <li>elimination flag (1 if eliminated, 0 otherwise)</li>
     *   <li>photo ID ({@code p.getIdPhoto()})</li>
     * </ul>
     * </p>
     *
     * @return a new {@code Map<String,int[]>} reflecting each player’s state
     */
    private Map<String,int[]> buildPlayersPositionMap() {
        Map<String,int[]> m = new HashMap<>();
        for (Map.Entry<String, Player> e : playersByNickname.entrySet()) {
            String nick = e.getKey();
            Player p    = e.getValue();

            m.put(nick, new int[]{
                    p.getPos(),
                    p.getLap(),
                    p.isEliminated() ? 1 : 0  ,
                    p.getIdPhoto()

            });
        }
        return m;
    }

    /**
     * Shuts down the hourglass timer if it exists, preventing any further
     * scheduled flips or expiration callbacks.
     */
    public void shutdownHourglass() {
        if (hourglass != null) {
            hourglass.shutdown();
            hourglass = null;
        }
    }
}

