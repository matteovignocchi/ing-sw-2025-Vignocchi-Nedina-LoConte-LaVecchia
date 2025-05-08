package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//TODO:
// 1)cambiare synchronized -> lock
// 2)eliminare metodi di utility che non vengono utilizzati, ma solo dopo

////////////////////////////////////////////////GESTIONE GAME///////////////////////////////////////////////////////////

public class GameManager {
    private final Map<Integer, Controller> games;
    private final AtomicInteger idCounter;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GameManager() {
        this.games = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        loadSavedGames();// Caricamento automatico all'avvio
        schedulePeriodicSaves();
    }

    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws BusinessLogicException, IOException, Exception{
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, gameId, maxPlayers, this::removeGame);

        games.put(gameId, controller);
        controller.addPlayer(nickname, v);
        controller.notifyAllViews();
        safeSave(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws BusinessLogicException, IOException, Exception {
        Controller controller = getControllerCheck(gameId);

        if (controller.isGameStarted() && controller.getPlayerByNickname(nickname)==null)
            throw new BusinessLogicException("Game already in progress");

        if (controller.getPlayerByNickname(nickname)!=null) {
            controller.markReconnected(nickname, v);
            controller.broadcastInform(nickname + " is reconnected");
        } else {
            controller.addPlayer(nickname, v);
            if (controller.countConnectedPlayers() == controller.getMaxPlayers()) {
                controller.startGame();
            }
        }
        controller.notifyAllViews();
        safeSave(gameId, controller);
    }

    public synchronized void quitGame(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);

        controller.broadcastInform("A player has abandoned: the game ends.");
        removeGame(gameId);
    }

    public synchronized void removeGame(int gameId) {
        Controller controller = games.remove(gameId);
        if (controller != null) {
            deleteSavedGame(gameId);
        }
    }

    ////////////////////////////////////////////////GESTIONE CONTROLLER/////////////////////////////////////////////////

    public synchronized Tile getCoveredTile(int gameId, String nickname) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        safeSave(gameId, controller);
        return controller.getCoveredTile(nickname);
    }

    public synchronized List<Tile> getUncoveredTilesList(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);

        List<Tile> uncoveredTiles = controller.getShownTiles();
        if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
        return uncoveredTiles;
    }

    public synchronized Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        safeSave(gameId, controller);
        return controller.chooseUncoveredTile(nickname, idTile);
    }

    public synchronized void dropTile (int gameId, String nickname, Tile tile) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.dropTile(nickname, tile);
        safeSave(gameId, controller);
    }

    public synchronized void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.placeTile(nickname, tile, cord);
        safeSave(gameId, controller);
    }

    public synchronized void setReady(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        controller.setReady(nickname);
        safeSave(gameId, controller);
    }

    public synchronized void flipHourglass(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        controller.flipHourglass(nickname);
        safeSave(gameId, controller);
    }

    public List<Card> showDeck(int gameId, int idxDeck) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        return controller.showDeck(idxDeck);
    }

    public synchronized void drawCard(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.drawCardManagement(nickname);
        safeSave(gameId, controller);
    }

    public Tile[][] lookAtDashBoard(String nickname, int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        return controller.lookAtDashBoard(nickname);
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
                try {
                    saveGameState(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    System.err.println("Auto-save failed for game "
                            + entry.getKey() + ": " + e.getMessage());
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    //Assicura che esiste la cartella saves/
    //serializza il controller su file temporaneo
    //Chiude il flusso e lo rinomina in modo da garantire che non ci siano mai file visibili se il processo si interrompe a metà
    private void saveGameState(int gameId, Controller controller) throws IOException {
        File dir = new File("saves");
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
        new File("saves/game_" + gameId + ".sav").delete();
    }

    //controlla se esiste saves/
    //Filtra tutti i file e per ciascuno deserializza il rispettivo controller
    //chiama reinitializedAfterLoad per riallacciare tutte le parti transient
    //Inserisce l'istanza nella mappa games usando l'ID estratto dal nome del file
    //Riallinea idCounter per non riutilizzare ID già caricati
    private void loadSavedGames() {
        File dir = new File("saves");
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

    private VirtualView getViewCheck(Controller controller, String nickname) throws BusinessLogicException {
        VirtualView view = controller.getViewByNickname(nickname);
        if (view == null) throw new BusinessLogicException("Player not found");
        return view;
    }

    public synchronized Map<Integer,int[]> listActiveGames() {
        return games.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    Controller controller = e.getValue();
                    return new int[]{ controller.countConnectedPlayers(), controller.getMaxPlayers() };
                }
        ));
    }
}
