package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * GameManager class. Its methods are invoked by Server classes, with the purpose of
 * forwarding calls to the right games, in order to manage multiple games at the same time.
 * It also takes care of creating, managing, saving and deleting the game.
 *
 * @author Gabriele La Vecchia
 * @author Francesco Lo Conte
 */
public class GameManager {
    private final Map<Integer, Controller> games = new ConcurrentHashMap<>();
    private final Map<String,Integer> nicknameToGameId = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final File savesDir;
    private final Set<String> loggedInUsers = ConcurrentHashMap.newKeySet();

    /**
     * Constructor for GameManager
     */
    public GameManager() {
        String dirProp = System.getProperty("game.saves.dir", "saves");
        this.savesDir = new File(dirProp);
        loadSavedGames();// Caricamento automatico all'avvio
        schedulePeriodicSaves();
    }

    /**
     * Creates a new game session, registers it, and adds the first player.
     *
     * @param isDemo    whether the game should run in demo mode
     * @param v         the VirtualView associated with the creating player
     * @param nickname  the nickname of the player creating the game
     * @param maxPlayers the maximum number of players allowed in this game
     * @return the unique identifier assigned to the new game
     * @throws Exception if an error occurs during game creation or saving
     */
    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws Exception {
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, gameId, maxPlayers, this::removeGame, loggedInUsers);

        games.put(gameId, controller);
        controller.addPlayer(nickname, v);
        nicknameToGameId.put(nickname, gameId);
        controller.notifyView(nickname);
        controller.updatePositionForEveryBody();
        safeSave(gameId, controller);
        return gameId;
    }

    /**
     * Adds a player to an existing game, notifies everyone, and starts the game
     * if the maximum number of players is reached.
     *
     * @param gameId   the identifier of the game to join
     * @param v        the VirtualView associated with the joining player
     * @param nickname the nickname of the player joining the game
     * @throws BusinessLogicException if game rules prevent the player from joining
     * @throws Exception              for any other error during the join process
     */
    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws BusinessLogicException, Exception {
        Controller controller = getControllerCheck(gameId);

        safeSave(gameId, controller);
        controller.addPlayer(nickname, v);
        nicknameToGameId.put(nickname, gameId);
        controller.notifyView(nickname);
        controller.updatePositionForEveryBody();
        if (controller.countConnectedPlayers() == controller.getMaxPlayers())
            controller.startGame();
        safeSave(gameId, controller);
    }


    /**
     * Removes a player from the specified game, broadcasts a message that the
     * game is ending, and cleans up all related state.
     *
     * @param gameId   the identifier of the game to quit
     * @param nickname the nickname of the player quitting the game
     * @throws Exception if an error occurs while quitting or cleaning up the game
     */
    public synchronized void quitGame(int gameId, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);
        String Message = "\u001B[31m" + nickname + " has abandoned: the game ends for everyone!" + "\u001B[0m";
        controller.broadcastInform(Message);

        for (String otherNick : controller.viewsByNickname.keySet()) {
            nicknameToGameId.remove(otherNick);
            loggedInUsers.remove(otherNick);
        }

        controller.setExit();
        removeGame(gameId);
    }

    /**
     * Attempts to log in a player with the given nickname and view.
     *
     * @param nickname the nickname of the player logging in
     * @param v        the VirtualView associated with the player
     * @return the existing game ID if reconnecting to a game, or 0 for a new session
     * @throws BusinessLogicException if the nickname is already in use or reconnection is invalid
     * @throws Exception              for any other error during login
     */
    public synchronized int login(String nickname, VirtualView v) throws Exception {
        Integer gameId = nicknameToGameId.get(nickname);
        if (gameId != null && games.containsKey(gameId)) {
            Controller controller = getControllerCheck(gameId);
            Player player = controller.getPlayerCheck(nickname);
            if(player.isConnected()){
                throw new BusinessLogicException("Nickname already in use!");
            }
            loggedInUsers.add(nickname);
            v.updateMapPosition(controller.getPlayersPosition());
            controller.markReconnected(nickname, v);
            v.setIsDemo(controller.getIsDemo());
            v.setGameId(gameId);
            v.updateGameState(controller.getGamePhase(nickname));
            v.updateDashMatrix(controller.getDashJson(nickname));
            v.printPlayerDashboard(controller.getDashJson(nickname));

            try {
                v.updateDashMatrix(controller.getDashJson(nickname));
            } catch (Exception ignored) {}
            return gameId;
        }

        if (loggedInUsers.add(nickname)) {
            return 0;
        }

        throw new BusinessLogicException("Nickname already used: " + nickname);
    }


    /**
     * Logs out a player by removing their nickname from active sessions.
     *
     * @param nickname the nickname of the player logging out
     */
    public synchronized void logout(String nickname) {
        loggedInUsers.remove(nickname);
        nicknameToGameId.remove(nickname);
    }

    /**
     * Removes a game and cleans up all associated state, including controller tasks,
     * player mappings, and saved game files.
     *
     * @param gameId the identifier of the game to remove
     */
    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) {
            controller.shutdownPing();
            controller.shutdownHourglass();
            for (String nick : controller.getPlayersByNickname().keySet()) {
                nicknameToGameId.remove(nick);
                loggedInUsers.remove(nick);
            }
            deleteSavedGame(gameId);
        }
    }

    /**
     * Return the currently top covered tile to a player.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @return a JSON string representing the covered tile
     * @throws BusinessLogicException if the game or player is invalid or no tile is covered
     */
    public String getCoveredTile(int gameId, String nickname) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String t = controller.getCoveredTile(nickname);
            safeSave(gameId, controller);
            return t;
        }
    }

    /**
     * Return the list of uncovered tiles available for the player to choose from.
     *
     * @param gameId   the identifier of the game
     * @return a JSON string containing the list of uncovered tiles
     * @throws BusinessLogicException if the game is invalid or the uncovered tiles list is empty
     */
    public String getUncoveredTilesList(int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String uncoveredTiles = controller.jsongetShownTiles();
            if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
            return uncoveredTiles;
        }
    }

    /**
     * Allows a player to choose one of the uncovered tiles by its ID.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param idTile   the identifier of the uncovered tile to choose
     * @return a JSON string representing the chosen tile
     * @throws BusinessLogicException if the game, player, or tile ID is invalid
     */
    public String chooseUncoveredTile(int gameId, String nickname, int idTile) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String t = controller.chooseUncoveredTile(nickname, idTile);
            safeSave(gameId, controller);
            return t;
        }
    }

    /**
     * Drops a specified tile from the player hand
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param tile     the JSON string representing the tile to drop
     * @throws BusinessLogicException if the game, player, or tile is invalid
     */
    public void dropTile (int gameId, String nickname, String tile) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.dropTile(nickname, tile);
            safeSave(gameId, controller);
        }
    }


    /**
     * Places a specified tile on the player's ship at given coordinates.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param tile     the JSON string representing the tile to place
     * @param cord     the [x,y] coordinates on the ship grid where the tile should be placed
     * @throws BusinessLogicException if the game, player, tile, or coordinates are invalid
     */
    public void placeTile(int gameId, String nickname, String tile, int[] cord) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.placeTile(nickname, tile, cord);
            safeSave(gameId, controller);
        }
    }

    /**
     * Returns a reserved tile for a player by its index.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @param id       the index of the reserved tile to retrieve
     * @return a JSON string representing the reserved tile
     * @throws BusinessLogicException if the game, player, or reserved tile index is invalid
     */
    public String getReservedTile(int gameId, String nickname , int id) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        String t;
        synchronized (controller) {
            safeSave(gameId, controller);
            t = controller.getReservedTile(nickname, id);
            safeSave(gameId, controller);
        }
        return t;
    }

    /**
     * Marks a player as ready to start the flight.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @throws BusinessLogicException if the game or player state is invalid
     * @throws RemoteException        if an RMI communication error occurs
     */
    public void setReady(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.setReady(nickname);
            safeSave(gameId, controller);
        }
    }

    /**
     * Flips the hourglass timer for a player.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player
     * @throws BusinessLogicException if the game or player state is invalid
     */
    public void flipHourglass(int gameId, String nickname) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.flipHourglass(nickname);
            safeSave(gameId, controller);
        }
    }

    /**
     * Returns the JSON representation of a specific card deck.
     *
     * @param gameId  the identifier of the game
     * @param idxDeck the index of the deck to show
     * @return a JSON string representing the cards in the requested deck
     * @throws BusinessLogicException if the game does not exist or the deck index is invalid
     */
    public String showDeck(int gameId, int idxDeck) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            return controller.showDeck(idxDeck);
        }
    }

    /**
     * Draws a card for the specified player and saves the game state before and after.
     *
     * @param gameId   the identifier of the game
     * @param nickname the nickname of the player drawing the card
     * @throws BusinessLogicException if the game or player state is invalid
     */
    public void drawCard(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.drawCardManagement(nickname);
            if (games.containsKey(gameId)) {
                safeSave(gameId, controller);
            }
        }
    }

    /**
     * Returns the dashboard matrix for a given player.
     *
     * @param nickname the nickname of the player
     * @param gameId   the identifier of the game
     * @return a 2D array representing the player's dashboard
     * @throws BusinessLogicException if the game or player is invalid
     */
    public String[][] lookAtDashBoard(String nickname, int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            return controller.lookAtDashBoard(nickname);
        }
    }


    /**
     * Schedules periodic saving of all active games every minute.
     */
    private void schedulePeriodicSaves() {
        scheduler.scheduleAtFixedRate(() -> {
            for (var entry : games.entrySet()) {
                Controller controller = entry.getValue();
                synchronized (controller) {
                    try {
                        saveGameState(entry.getKey(), controller);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }


    /**
     * Atomically saves the state of a game to disk.
     *
     * @param gameId     the identifier of the game to save
     * @param controller the Controller instance whose state to serialize
     * @throws IOException if an I/O error occurs during saving
     */
    private void saveGameState(int gameId, Controller controller) throws IOException {
        File dir = savesDir;
        if (!dir.exists()) dir.mkdirs();

        File tmp = new File(dir, "game_" + gameId + ".sav.tmp");
        try (var out = new ObjectOutputStream(new FileOutputStream(tmp))) {
            out.writeObject(controller);
            out.flush();
        }
        File target = new File(dir, "game_" + gameId + ".sav");
        Files.move(
                tmp.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
        );
    }

    /**
     * Deletes the saved game file for the given game ID, if it exists.
     *
     * @param gameId the identifier of the game whose save file should be removed
     */
    private void deleteSavedGame(int gameId) {
        File f = new File(savesDir, "game_" + gameId + ".sav");
        System.out.println("⏏ Deleting save in: " + f.getAbsolutePath());
        if (f.exists() && !f.delete()) {
            System.err.println("I cannot delete the game " + gameId);
        }
    }

    /**
     * Loads all saved games from disk at startup, re-initializes transient state,
     * and repopulates the internal game maps.
     */
    private void loadSavedGames() {
        File dir = savesDir;
        if (!dir.exists()) return;
        int maxId = 0;

        File[] files = dir.listFiles((d,n) -> n.matches("game_\\d+\\.sav"));
        if (files != null) for (File f : files) {
            try (var in = new ObjectInputStream(new FileInputStream(f))) {
                Controller controller = (Controller) in.readObject();
                int gameId = Integer.parseInt(f.getName().replaceAll("\\D+", ""));
                controller.reinitializeAfterLoad(
                        h -> {
                            try { controller.onHourglassStateChange(h); }
                            catch (BusinessLogicException ex) {
                                System.err.println("Error in hourglass callback: " + ex.getMessage());
                            }
                        },
                        this::removeGame
                );
                controller.getPlayersByNickname()
                        .values()
                        .forEach(p -> p.setConnected(false));

                games.put(gameId, controller);
                controller.getPlayersByNickname().keySet()
                        .forEach(nick -> nicknameToGameId.put(nick, gameId));
                maxId = Math.max(maxId, gameId);

            } catch (Exception e) {
                System.err.println("Loading error " + f + ": " + e.getMessage());
            }
        }

        idCounter.set(maxId + 1);
    }

    /**
     * Saves the game state, logging any I/O errors without throwing.
     *
     * @param gameId the identifier of the game to save
     * @param ctrl   the Controller instance to serialize
     */
    private void safeSave(int gameId, Controller ctrl) {
        try {
            saveGameState(gameId, ctrl);
        } catch(IOException e) {
            System.err.println("Save failed for game " + gameId + ": " + e.getMessage());
        }
    }

    /**
     * Returns the Controller instance for the specified game.
     *
     * @param gameId the identifier of the game
     * @return the Controller associated with the given gameId
     * @throws BusinessLogicException if no game exists with the provided ID
     */

    public Controller getControllerCheck(int gameId) throws BusinessLogicException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new BusinessLogicException("Game not found");
        return controller;
    }

    /**
     * Lists all games that are currently active and waiting for additional players.
     * An active game is one where:
     * - At least one player is connected,
     * - All joined players are currently connected,
     * - The number of connected players is less than the maximum allowed.
     *
     * @return a map from game ID to a three-element array:
     *         <ul>
     *           <li>index 0: number of connected players</li>
     *           <li>index 1: maximum number of players for the game</li>
     *           <li>index 2: demo mode flag (1 if demo mode, 0 otherwise)</li>
     *         </ul>
     */
    public synchronized Map<Integer,int[]> listActiveGames() {
        return games.entrySet().stream()
                .filter(e -> {
                    Controller c = e.getValue();
                    int connected    = c.countConnectedPlayers();
                    int originalSize = c.getPlayersByNickname().size();
                    int maxPlayers   = c.getMaxPlayers();
                    return connected > 0
                            && connected == originalSize
                            && connected < maxPlayers;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            Controller c = e.getValue();
                            return new int[]{
                                    c.countConnectedPlayers(),
                                    c.getMaxPlayers(),
                                    c.getIsDemo() ? 1 : 0
                            };
                        }
                ));
    }

//    public void handleDisconnectRmi(int gameId, String nickname) {
//        try {
//            Controller ctrl = getControllerCheck(gameId);
//            ctrl.markDisconnected(nickname);
//            System.out.println("Marked "+nickname+" as DISCONNECTED in game "+gameId);
//        } catch (BusinessLogicException e) {
//        }
//    }

}
