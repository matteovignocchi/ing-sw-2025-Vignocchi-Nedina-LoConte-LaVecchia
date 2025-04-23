package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Player;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
    private final Map<Integer, Controller> games;
    private final AtomicInteger idCounter;

    //Map<nickname, virtualview>;

    //nickname.getplayer
    //player.reconnect(){isconnected=true}
    //nickname x -->modifico vitrualview

    public GameManager() {
        this.games = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        loadSavedGames(); // Caricamento automatico all'avvio
    }

    public synchronized int createGame(boolean isDemo, VirtualView v) throws CardEffectException, IOException {
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo);
        games.put(gameId, controller);
        controller.addPlayer(v);
        saveGameState(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v) throws CardEffectException, IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");
        controller.addPlayer(v);
        saveGameState(gameId, controller);
    }

    public synchronized void quitGame(int gameId, VirtualView v) throws CardEffectException, IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByView(v);
        if (player == null) throw new IOException("Player not found in this game");

        controller.removePlayer(player);
        if (controller.checkNumberOfPlayers() == 0) {
            removeGame(gameId);
            saveGameState(gameId, controller);
        } else {
            saveGameState(gameId, controller);
        }
    }

    public synchronized void stopGame(int gameId, VirtualView v) throws CardEffectException, IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByView(v);
        if (player == null) throw new IOException("Player not found");

        controller.markDisconnected(player);
        if (controller.countConnectedPlayers() <= 1) {
            controller.pauseGame(); // ferma gioco temporaneamente fino a scadenza timeout
        }
        saveGameState(gameId, controller);
    }

    public synchronized void reconnectGame(String username, VirtualView newView) throws IOException {
        for (Map.Entry<Integer, Controller> entry : games.entrySet()) {
            Controller controller = entry.getValue();
            Player player = controller.getPlayerByUsername(username);
            if (player != null) {
                controller.remapView(player, newView);
                player.setConnected(true);

                if (controller.countConnectedPlayers() >= 2) {
                    controller.resumeGame(); // riattiva partita se era in pausa
                }

                saveGameState(entry.getKey(), controller);
                return;
            }
        }
        throw new IOException("No active game found for user: " + username);
    }

    public Controller getController(int gameId) {
        return games.get(gameId);
    }

    public synchronized void removeGame(int gameId) {
        games.remove(gameId);
        deleteSavedGame(gameId);
    }

    public Set<Integer> listActiveGames() {
        return games.keySet();
    }

    private void saveGameState(int gameId, Controller controller) {
        try {
            File dir = new File("saves");
            if (!dir.exists()) dir.mkdirs();

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream("saves/game_" + gameId + ".sav"))) {
                out.writeObject(controller);
            }
        } catch (IOException e) {
            System.err.println("Errore salvataggio partita " + gameId + ": " + e.getMessage());
        }
    }

    //non ho ben capito
    private void deleteSavedGame(int gameId) {
        File file = new File("saves/game_" + gameId + ".sav");
        if (file.exists() && file.delete()) {
            System.out.println("🗑️ Salvataggio game_" + gameId + ".sav rimosso.");
        } else {
            System.err.println("⚠ Nessun file da eliminare per game " + gameId);
        }
    }

    private void loadSavedGames() {
        File dir = new File("saves");
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles((d, name) -> name.startsWith("game_") && name.endsWith(".sav"));
        if (files == null) return;

        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Controller controller = (Controller) in.readObject();
                int gameId = Integer.parseInt(file.getName().replace("game_", "").replace(".sav", ""));
                games.put(gameId, controller);
                System.out.println("🔁 Partita " + gameId + " ripristinata.");
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
