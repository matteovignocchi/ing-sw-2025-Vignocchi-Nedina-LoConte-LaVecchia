package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

////////////////////////////////////////////////GESTIONE GAME///////////////////////////////////////////////////////////

public class GameManager {
    private final Map<Integer, Controller> games = new ConcurrentHashMap<>();
    private final Map<String,Integer> nicknameToGameId = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final File savesDir;
    private final Set<String> loggedInUsers = ConcurrentHashMap.newKeySet();

    public GameManager() {
        String dirProp = System.getProperty("game.saves.dir", "saves");
        this.savesDir = new File(dirProp);
        loadSavedGames();// Caricamento automatico all'avvio
        schedulePeriodicSaves();
    }

    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws Exception {
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, gameId, maxPlayers, this::removeGame, loggedInUsers);

        games.put(gameId, controller);
        controller.addPlayer(nickname, v);
        nicknameToGameId.put(nickname, gameId);
        safeSave(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws BusinessLogicException, IOException, Exception {
        Controller controller = getControllerCheck(gameId);

        //if (controller.getPlayerByNickname(nickname) == null)
            controller.addPlayer(nickname, v);
            nicknameToGameId.put(nickname, gameId);
            if (controller.countConnectedPlayers() == controller.getMaxPlayers())
                controller.startGame();


        safeSave(gameId, controller);
    }

    public synchronized void quitGame(int gameId, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);
        for (String other : controller.viewsByNickname.keySet()) {
            if (!other.equals(nickname)) {
                controller.sendInformTo(other,
                        "\n" + nickname + " abandoned: press any key to return to the main menù!");
            }
        }
        controller.setExit();
        removeGame(gameId);
    }

    //TODO: deve mandare businesslogicException, ma inform e setGameId mandano exception generica
    public synchronized int login(String nickname, VirtualView v) throws Exception {
        // caso nuovo utente
        if (loggedInUsers.add(nickname)) {
            return -2;
        }
        // caso reconnect
        Integer gameId = nicknameToGameId.get(nickname);
        if (gameId != null && games.containsKey(gameId)) {
            Controller controller = getControllerCheck(gameId);
            controller.markReconnected(nickname, v);
            Tile[][] dash = controller.getPlayerCheck(nickname).getDashMatrix();
            try {
                v.printPlayerDashboard(dash);
            } catch (Exception e) {

            }
            controller.broadcastInform("SERVER: " + nickname + " reconnected to game");
            v.setGameId(gameId);
            return gameId;
        }
        // nick in uso ma non in partita: rifiuto
        throw new BusinessLogicException("Nickname already used: " + nickname);
    }

    public synchronized void logout(String nickname) {
        loggedInUsers.remove(nickname);
        nicknameToGameId.remove(nickname);
    }

    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) {
            deleteSavedGame(gameId);
        }
    }

    ////////////////////////////////////////////////GESTIONE CONTROLLER/////////////////////////////////////////////////

    public Tile getCoveredTile(int gameId, String nickname) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            Tile t = controller.getCoveredTile(nickname);
            safeSave(gameId, controller);
            return t;
        }
    }

    public List<Tile> getUncoveredTilesList(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            List<Tile> uncoveredTiles = controller.getShownTiles();
            if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
            return uncoveredTiles;
        }
    }

    public Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            Tile t = controller.chooseUncoveredTile(nickname, idTile);
            safeSave(gameId, controller);
            return t;
        }
    }

    public void dropTile (int gameId, String nickname, Tile tile) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            controller.dropTile(nickname, tile);
            safeSave(gameId, controller);
        }
    }

    public void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            controller.placeTile(nickname, tile, cord);
            safeSave(gameId, controller);
        }
    }

    public Tile getReservedTile(int gameId, String nickname , int id) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        Tile t;
        synchronized (controller) {
            t = controller.getReservedTile(nickname, id);
            safeSave(gameId, controller);
        }
        return t;
    }

    public void setReady(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            controller.setReady(nickname);
            safeSave(gameId, controller);
        }
    }

    public void flipHourglass(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            controller.flipHourglass(nickname);
            safeSave(gameId, controller);
        }
    }

    public List<Card> showDeck(int gameId, int idxDeck) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            return controller.showDeck(idxDeck);
        }
    }

    public void drawCard(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            controller.drawCardManagement(nickname);
            safeSave(gameId, controller);
        }
    }

    public Tile[][] lookAtDashBoard(String nickname, int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            return controller.lookAtDashBoard(nickname);
        }
    }

    ////////////////////////////////////////////////GESTIONE SALVATAGGIO////////////////////////////////////////////////


    //Usiamo newSingleThreadScheduledExecutor(), cioè un singolo worker thread dedicato solo a questi salvataggi.
    //Ciò evita di bloccare il thread “principale” del server e serializza i salvataggi uno dietro l’altro.
    //scheduleAtFixedRate(...) garantisce che il task venga invocato a intervalli regolari, indipendentemente da quanto duri la singola esecuzione (se dura più del periodo, le invocazioni successive partono subito).
    //L’initialDelay di 1 minuto serve a dare un po’ di tempo al server di avviarsi e caricare eventuali partite prima del primo auto-save.
    //Ad ogni esecuzione itero su games.entrySet(). Per ciascuna entry chiamo saveGameState(gameId, controller).
    //Se la serializzazione di un singolo controller fallisce (es. disco pieno, permessi, file lock), l’eccezione viene catturata, loggata su System.err, ma il loop prosegue sugli altri game.
    //In questo modo un problema puntuale non blocca tutti i salvataggi.
    //Il metodo saveGameState usa un file .tmp + Files.move(… ATOMIC_MOVE) per garantire che non esistano mai versioni parziali visibili in saves/.
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
        }, 1, 1, TimeUnit.MINUTES);
    }

    //Assicura che esiste la cartella saves/
    //serializza il controller su file temporaneo
    //Chiude il flusso e lo rinomina in modo da garantire che non ci siano mai file visibili se il processo si interrompe a metà
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

    //Rimuove il file di salvataggio di quel gameId quando il gioco viene definitivamente cancellato (ad esempio perché è finito o abbandonato).
    //Non lancia eccezioni se il file non esiste.
    private void deleteSavedGame(int gameId) {
        new File(savesDir, "game_" + gameId + ".sav").delete();
    }

    //controlla se esiste saves/
    //Filtra tutti i file e per ciascuno deserializza il rispettivo controller
    //chiama reinitializedAfterLoad per riallacciare tutte le parti transient
    //Inserisce l'istanza nella mappa games usando l'ID estratto dal nome del file
    //Riallinea idCounter per non riutilizzare ID già caricati
    private void loadSavedGames() {
        File dir = savesDir;
        if (!dir.exists()) return;
        int maxId = 0;
        File[] files = dir.listFiles((d,n)->n.matches("game_\\d+\\.sav"));
        if(files != null) {
            for (File f : files) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                    Controller controller = (Controller) in.readObject();
                    controller.reinitializeAfterLoad(h -> {
                        try {
                            controller.onHourglassStateChange(h);
                        } catch (BusinessLogicException ex) {
                            System.err.println("Error in the callback hourglass: " + ex.getMessage());
                        }
                    });
                    int id = Integer.parseInt(f.getName().replaceAll("\\D+", ""));
                    games.put(id, controller);
                    maxId = Math.max(maxId, id);
                } catch (Exception e) {
                    System.err.println("Loading error " + f + ": " + e.getMessage());
                }
            }
        }
        idCounter.set(maxId + 1);
    }

    private void safeSave(int gameId, Controller ctrl) {
        try {
            saveGameState(gameId, ctrl);
        } catch(IOException e) {
            System.err.println("Save failed for game " + gameId + ": " + e.getMessage());
        }
    }

    ////////////////////////////////////////////////GESTIONE UTILITA'///////////////////////////////////////////////////

    private Controller getControllerCheck(int gameId) throws BusinessLogicException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new BusinessLogicException("Game not found");
        return controller;
    }

    public synchronized Map<Integer,int[]> listActiveGames() {
        return games.entrySet().stream()
                .filter(e -> {
                    Controller c = e.getValue();
                    return c.countConnectedPlayers() < c.getMaxPlayers();
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
}
