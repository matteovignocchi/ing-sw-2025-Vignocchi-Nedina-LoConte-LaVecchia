package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//TODO:
// 1)eccezioni, capire bene quando e dove + remoteException per disconnessione
// 5)Capire bene come gestire i corner case, leggi su discord appena rispondono, fondamentale per sistemare i metodi

////////////////////////////////////////////////GESTIONE GAME///////////////////////////////////////////////////////////

public class GameManager {
    private final Map<Integer, Controller> games;
    private final AtomicInteger idCounter;

    public GameManager() {
        this.games = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        loadSavedGames(); // Caricamento automatico all'avvio
    }

    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws CardEffectException, IOException {
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, maxPlayers);
        controller.addPlayer(nickname, v);
        games.put(gameId, controller);
        saveGameState(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");
        controller.addPlayer(nickname, v);
        saveGameState(gameId, controller);
    }

    public synchronized void quitGame(int gameId, String nickname) throws IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new IOException("Player not found");

        controller.removePlayer(nickname);
        if (controller.checkNumberOfPlayers() == 0) {
            removeGame(gameId);
        } else {
            saveGameState(gameId, controller);
        }
    }

    public synchronized void stopGame(int gameId, String nickname) throws IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new IOException("Player not found");

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

    public synchronized void endGame(int gameId) throws Exception {
        Controller controller = games.get(gameId);
        if (controller == null) return;

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

    ////////////////////////////////////////////////GESTIONE CONTROLLERE////////////////////////////////////////////////

    public synchronized Tile getTileServer(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        return controller.getTileFromPile(nickname);
    }

    public synchronized Tile getUncoveredTile(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        return controller.getUncoveredTile(nickname);
    }

    public synchronized void spinHourglass(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.spinHourglass(nickname);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void setReady(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.setPlayerReady(nickname);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void returnTile(String nickname, Tile tile) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.returnTile(nickname, tile);
        saveGameState(getGameId(controller), controller);
    }

    public synchronized void placeTile(String nickname, Tile tile, int x, int y) throws IOException {
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

    private Controller findControllerByPlayer(String nickname) throws IOException {
        for (Controller controller : games.values()) {
            if (controller.getPlayerByNickname(nickname) != null) {
                return controller;
            }
        }
        throw new IOException("Player not found in any game");
    }

    private int getGameId(Controller controller) {
        for (Map.Entry<Integer, Controller> entry : games.entrySet()) {
            if (entry.getValue().equals(controller)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public Controller getController(int gameId) {
        return games.get(gameId);
    }

    public Set<Integer> listActiveGames() {
        return games.keySet();
    }
}
