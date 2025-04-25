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
        if (controller.checkNumberOfPlayers() == 0) { //e se ne rimane solo uno, assegniamo la vittoria al bro rimasto
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
            controller.pauseGame(); // ferma il gioco in attesa di riconnessioni o parte un timeout
        }
        saveGameState(gameId, controller);
    }


    public void reconnectPlayer(String nickname, VirtualView view) throws IOException {
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

    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) deleteSavedGame(gameId);
    }

    ////////////////////////////////////////////////GESTIONE CONTROLLERE////////////////////////////////////////////////

    //TODO: capire bene chi deve restituire cosa

    //Giocatore in DRAW_PHASE, pesca la carta e passa in CARD_EFFECT.
    public synchronized void drawCard_server(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.drawCard(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Prende una tile coperta e mette il player in TILE_MANAGEMENT.
    public synchronized void getTileServer(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.getTileFromPile(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Prende una tile scoperta e mette il player in TILE_MANAGEMENT.
    public synchronized void getUncoveredTile(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.getUncoveredTile(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Attiva un timeout, cambia clessidra, se count ≥ 3 → fase WAITING_FOR_TURN.
    public synchronized void spinTheHourglass(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.spinHourglass(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Imposta il player a "ready", passa a WAITING_FOR_PLAYERS.
    public synchronized void declearReady(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.setPlayerReady(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Rimette la tile nella pila, torna in BOARD_SETUP.
    public synchronized void returTile(String nickname, Tile tile) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.returnTile(nickname, tile);
        saveGameState(getGameId(controller), controller);
    }

    //Posiziona tile nella nave.
    public synchronized void placeTile(String nickname, Tile tile, int x, int y) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.placeTile(nickname, tile, x, y);
        saveGameState(getGameId(controller), controller);
    }

    //Ruota tile selezionata a destra.
    public synchronized void rightRotatedTile(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.rotateTileRight(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Ruota tile selezionata a sinistra.
    public synchronized void leftRotatedTile(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.rotateTileLeft(nickname);
        saveGameState(getGameId(controller), controller);
    }

    //Visualizza un mazzo, solo stampa/inform → niente salvataggio.
    public void watchDeck(String nickname) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.lookDeck(nickname);
    }

    //Visualizza plancia di un altro giocatore.
    public void lookDashBoard(String nickname, int targetPlayerId) throws IOException {
        Controller controller = findControllerByPlayer(nickname);
        controller.lookDashBoard(nickname, targetPlayerId);
    }

    //metodo logout non ha senso, al max possiamo fare un metodo endgame() quando i premi sono stati consegnati o
    //è scaduto il timeout (?)

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
