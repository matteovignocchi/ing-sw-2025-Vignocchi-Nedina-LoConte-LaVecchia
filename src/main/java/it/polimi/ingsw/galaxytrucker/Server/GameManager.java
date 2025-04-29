package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: cambiare synchronized -> lock

////////////////////////////////////////////////GESTIONE GAME///////////////////////////////////////////////////////////

public class GameManager {
    private final Map<Integer, Controller> games;
    private final AtomicInteger idCounter;

    public GameManager() {
        this.games = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        loadSavedGames(); // Caricamento automatico all'avvio
    }

    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws BusinessLogicException, IOException {
        checkNickname(nickname);
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, maxPlayers);
        controller.addPlayer(nickname, v);
        games.put(gameId, controller);
        saveGameState(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws IOException, BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.addPlayer(nickname, v);
        saveGameState(gameId, controller);
    }

    public synchronized void quitGame(int gameId, String nickname) throws BusinessLogicException, IOException {
        Controller controller = getControllerCheck(gameId);
        Player player = getPlayerCheck(controller, nickname);
        controller.removePlayer(nickname);
        if (controller.checkNumberOfPlayers() == 0)
            removeGame(gameId);
        else
            saveGameState(gameId, controller);
    }


    public synchronized void stopGame(int gameId, String nickname) throws BusinessLogicException, IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new BusinessLogicException("Game not found");

        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new BusinessLogicException("Player not found");

        controller.markDisconnected(nickname);
        if (controller.countConnectedPlayers() <= 1) {
            controller.pauseGame(); // ferma il gioco
        }
        saveGameState(gameId, controller);
    }

    public synchronized void reconnectPlayer(String nickname, VirtualView view) throws IOException {
        for (var entry : games.entrySet()) {
            Controller controller = entry.getValue();
            if (controller.getPlayerByNickname(nickname) != null) {
                controller.remapView(nickname, view);
                controller.getPlayerByNickname(nickname).setConnected(true);
                if (controller.countConnectedPlayers() >= 2)
                    controller.resumeGame();
                saveGameState(entry.getKey(), controller);
                return;
            }
        }
        throw new IOException("Player not found in any game");
    }

    public synchronized void endGame(int gameId) throws IOException, BusinessLogicException {
        Controller controller = games.get(gameId);
        if (controller == null) { throw new BusinessLogicException("Game not found"); }

        for (String nickname : controller.getNicknames()) {
            VirtualView view = controller.getView(nickname);
            if (view != null) {
                view.inform("La partita è terminata. Verrai disconnesso.");
            }
        }
        removeGame(gameId);
    }

    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) {
            deleteSavedGame(gameId);
        }
    }

    ////////////////////////////////////////////////GESTIONE CONTROLLER/////////////////////////////////////////////////

    //TODO: dire al fra/floris se spostare parte di questi metodi nel controller (creando gli appositi)

    public synchronized Tile getCoveredTile(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);

        int size = controller.getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        int randomIdx = ThreadLocalRandom.current().nextInt(size);
        Player p = getPlayerCheck(controller, nickname);
        p.setGameFase(GameFase.TILE_MANAGEMENT);
        updateGameState(v, GameFase.TILE_MANAGEMENT);
        //update (?)
        return controller.getTile(randomIdx);
    }

    public synchronized List<Tile> getUncoveredTilesList(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);

        List<Tile> uncoveredTiles = controller.getShownTiles();
        if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
        //update (?)
        return uncoveredTiles;
    }

    public synchronized Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);

        List<Tile> uncoveredTiles = controller.getShownTiles();
        Optional<Tile> opt = uncoveredTiles.stream().filter(t -> t.getIdTile() == idTile).findFirst();
        if(opt.isEmpty()) throw new BusinessLogicException("Tile already taken");

        Player p = getPlayerCheck(controller, nickname);
        p.setGameFase(GameFase.TILE_MANAGEMENT);
        updateGameState(v, GameFase.TILE_MANAGEMENT);
        //update (?)
        return controller.getShownTile(uncoveredTiles.indexOf(opt.get()));
    }

    public synchronized void dropTile (int gameId, String nickname, Tile tile) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);
        Player p = getPlayerCheck(controller, nickname);

        controller.addToShownTile(tile);
        p.setGameFase(GameFase.BOARD_SETUP);
        updateGameState(v, GameFase.BOARD_SETUP);
        //update(?)
    }

    public synchronized void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);
        Player p = getPlayerCheck(controller, nickname);

        p.addTile(cord[0], cord[1], tile);
        p.setGameFase(GameFase.BOARD_SETUP);
        updateGameState(v, GameFase.BOARD_SETUP);
        //update + update nave
    }

    public synchronized void setReady(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);
        Player p = getPlayerCheck(controller, nickname);

        controller.setPlayerReady(p);

        List<Player> playersInGame = controller.getPlayersInGame();
        if(playersInGame.stream().allMatch( e -> e.getGameFase() == GameFase.WAITING_FOR_PLAYERS)) {
            controller.startFlight();
        } else{
            p.setGameFase(GameFase.WAITING_FOR_PLAYERS);
            updateGameState(v, GameFase.WAITING_FOR_PLAYERS);
        }

        //update (?)
    }

    public synchronized void spinHourglass(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.spinHourglass(nickname);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void returnTile(String nickname, Tile tile) throws IOException, BusinessLogicException {
        checkTile(tile);
        Controller controller = findControllerByPlayer(nickname);
        controller.returnTile(nickname, tile);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void placeTile(String nickname, Tile tile, int x, int y) throws IOException, BusinessLogicException {
        checkTile(tile);
        Controller controller = findControllerByPlayer(nickname);
        controller.placeTile(nickname, tile, x, y);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void drawCard(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.drawCard(nickname);
        saveGameState(getGameId(controller), controller);
    }

    public void watchDeck(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.lookDeck(nickname);
    }

    public void lookDashBoard(String nickname, int targetId) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.lookDashBoard(nickname, targetId);
    }

    ////////////////////////////////////////////////GESTIONE SALVATAGGIO////////////////////////////////////////////////


    private void saveGameState(int gameId, Controller controller) throws IOException {
        File file = new File("saves/game_" + gameId + ".sav");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(controller);
        }
    }

    private void deleteSavedGame(int gameId) {
        File file = new File("saves/game_" + gameId + ".sav");
        if (file.exists()) file.delete();
    }

    private void loadSavedGames() {
        File dir = new File("saves");
        if (!dir.exists()) return;

        int maxId = 0;
        File[] files = dir.listFiles((d, name) -> name.startsWith("game_") && name.endsWith(".sav"));
        if (files == null) return;

        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Controller controller = (Controller) in.readObject();
                int gameId = Integer.parseInt(file.getName().replace("game_", "").replace(".sav", ""));
                games.put(gameId, controller);
                if (gameId > maxId) maxId = gameId;
            } catch (Exception e) {
                System.err.println("Loading error " + file.getName() + ": " + e.getMessage());
            }
        }
        idCounter.set(maxId + 1);
    }

    ////////////////////////////////////////////////GESTIONE UTILITA'///////////////////////////////////////////////////

    private Controller getControllerCheck(int gameId) throws BusinessLogicException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new BusinessLogicException("Game not found");
        return controller;
    }

    private Player getPlayerCheck(Controller controller, String nickname) throws BusinessLogicException {
        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new BusinessLogicException("Player not found");
        return player;
    }

    private VirtualView getViewCheck(Controller controller, String nickname) throws BusinessLogicException {
        VirtualView view = controller.getViewByNickname(nickname);
        if (view == null) throw new BusinessLogicException("Player not found");
        return view;
    }

    //"BEST EFFORT COMMUNICATION": provo a comunicare, ma se fallisce vado avanti lo stesso, perché l'affidabilità
    // del server è più importante della singola comunicazione. Se il client si è disconnesso non blocco tutto.
    private void updateGameState(VirtualView v, GameFase fase) {
        try {
            v.updateGameState(fase);
        } catch (Exception ignored) { //magari eccezione specifica(parla con Floris)
        }
    }

    private Controller findControllerByPlayer(String nickname) throws IOException {
        for (Controller controller : games.values()) {
            if (controller.getPlayerByNickname(nickname) != null) {
                return controller;
            }
        }
        throw new IOException("Player not found in any game");
    }

    private int getGameId(Controller controller) throws BusinessLogicException {
        return games.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(controller))
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Controller not associated with any game"))
                .getKey();
    }


    public Set<Integer> listActiveGames() {
        return games.keySet();
    }
}
