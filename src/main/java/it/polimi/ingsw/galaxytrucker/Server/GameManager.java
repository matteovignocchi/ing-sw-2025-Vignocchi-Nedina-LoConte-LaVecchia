package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//TODO:
// 1)eccezioni, capire bene quando e dove + remoteException per disconnessione
// 2)Salvataggi in memoria, valutando se creare gia la risorsa "saves"
// 3)verificare correttezza metodi scritti qui
// 4)Le fasi sono corrette ovunque? C'è da capire bene la questione associata ad esse
// 5)Capire bene come gestire i corner case, leggi su discord appena rispondono


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

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws CardEffectException, IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");
        controller.addPlayer(nickname, v);
        saveGameState(gameId, controller);
    }


    public synchronized void quitGame(int gameId, String nickname) throws IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new IOException("Player not found in this game");

        controller.removePlayer(nickname); // rimuove player e VirtualView
        if (controller.checkNumberOfPlayers() == 0) {
            removeGame(gameId); // rimuove anche il salvataggio
        } else {
            saveGameState(gameId, controller);
        }
    }

    public synchronized void stopGame(int gameId, String nickname) throws IOException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new IOException("Game not found");

        Player player = controller.getPlayerByNickname(nickname);
        if (player == null) throw new IOException("Player not found in this game");

        controller.markDisconnected(nickname);
        if (controller.countConnectedPlayers() <= 1) {
            controller.pauseGame(); // ferma il gioco in attesa di riconnessioni
        }
        saveGameState(gameId, controller);
    }


    public void reconnectGame(String nickname, VirtualView view) throws IOException {
        for (var entry : games.entrySet()) {
            Controller controller = entry.getValue();
            if (controller.getPlayerByNickname(nickname) != null) {
                controller.remapView(nickname, view);
                controller.getPlayerByNickname(nickname).setConnected(true);
                if (controller.countConnectedPlayers() >= 2) controller.resumeGame();
                saveGameState(entry.getKey(), controller);
                return;
            }
        }
        throw new IOException("Player not found in any game");
    }

    public Controller getController(int gameId) {
        return games.get(gameId);
    }

    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) deleteSavedGame(gameId);
    }

    public Set<Integer> listActiveGames() {
        return games.keySet();
    }

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
        for (File file : dir.listFiles((d, name) -> name.startsWith("game_") && name.endsWith(".sav"))) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Controller controller = (Controller) in.readObject();
                int gameId = Integer.parseInt(file.getName().replace("game_", "").replace(".sav", ""));
                games.put(gameId, controller);
                if (gameId > maxId) maxId = gameId;
            } catch (Exception e) {
                System.err.println("Errore nel caricamento " + file.getName() + ": " + e.getMessage());
            }
        }
        idCounter.set(maxId + 1);
    }
}
