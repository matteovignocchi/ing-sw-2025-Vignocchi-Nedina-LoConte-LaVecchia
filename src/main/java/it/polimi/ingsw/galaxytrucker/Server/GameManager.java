package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Player;
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
        controller.notifyView(nickname);
        controller.updatePositionForEveryBody();
        safeSave(gameId, controller);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws BusinessLogicException, IOException, Exception {
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

    //TODO: deve mandare businesslogicException, ma inform e setGameId mandano exception generica
    public synchronized int login(String nickname, VirtualView v) throws Exception {
        Integer gameId = nicknameToGameId.get(nickname);
        if (gameId != null && games.containsKey(gameId)) {
            Controller controller = getControllerCheck(gameId);
            Player player = controller.getPlayerCheck(nickname);
            if(player.isConnected()){
                throw new BusinessLogicException("Nickname already in use!");
            }
            loggedInUsers.add(nickname);//todo:ci va?
            v.updateMapPosition(controller.getPlayersPosition());
            controller.markReconnected(nickname, v);
            Tile[][] dash = controller.getPlayerCheck(nickname).getDashMatrix();
            v.setIsDemo(controller.getIsDemo());
            v.setGameId(gameId);
            v.updateGameState(controller.getGamePhase(nickname));
            v.updateDashMatrix(controller.getDashJson(nickname));
            v.printPlayerDashboard(controller.getDashJson(nickname));

            try {
                v.updateDashMatrix(controller.getDashJson(nickname));
            } catch (Exception e) {

            }
            return gameId;
        }

        if (loggedInUsers.add(nickname)) {
            return 0;
        }

        throw new BusinessLogicException("Nickname already used: " + nickname);
    }

    public synchronized void logout(String nickname) {
        loggedInUsers.remove(nickname);
        nicknameToGameId.remove(nickname);
    }

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

    ////////////////////////////////////////////////GESTIONE CONTROLLER/////////////////////////////////////////////////

    public String getCoveredTile(int gameId, String nickname) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String t = controller.getCoveredTile(nickname);
            safeSave(gameId, controller);
            return t;
        }
    }

    public String getUncoveredTilesList(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String uncoveredTiles = controller.jsongetShownTiles();
            if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
            return uncoveredTiles;
        }
    }

    public String chooseUncoveredTile(int gameId, String nickname, int idTile) throws BusinessLogicException{
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            String t = controller.chooseUncoveredTile(nickname, idTile);
            safeSave(gameId, controller);
            return t;
        }
    }

    public void dropTile (int gameId, String nickname, String tile) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.dropTile(nickname, tile);
            safeSave(gameId, controller);
        }
    }

    public void placeTile(int gameId, String nickname, String tile, int[] cord) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.placeTile(nickname, tile, cord);
            safeSave(gameId, controller);
        }
    }

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

    public void setReady(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.setReady(nickname);
            safeSave(gameId, controller);
        }
    }

    public void flipHourglass(int gameId, String nickname) throws BusinessLogicException, RemoteException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            safeSave(gameId, controller);
            controller.flipHourglass(nickname);
            safeSave(gameId, controller);
        }
    }

    public String showDeck(int gameId, int idxDeck) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        synchronized (controller) {
            return controller.showDeck(idxDeck);
        }
    }

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

    public String[][] lookAtDashBoard(String nickname, int gameId) throws BusinessLogicException {
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
        }, 0, 1, TimeUnit.MINUTES);
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
        File f = new File(savesDir, "game_" + gameId + ".sav");
        System.out.println("⏏ Cancello save in: " + f.getAbsolutePath());
        if (f.exists() && !f.delete()) {
            System.err.println("I cannot delete the game " + gameId);
        }
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

    private void safeSave(int gameId, Controller ctrl) {
        try {
            saveGameState(gameId, ctrl);
        } catch(IOException e) {
            System.err.println("Save failed for game " + gameId + ": " + e.getMessage());
        }
    }

    ////////////////////////////////////////////////GESTIONE UTILITA'///////////////////////////////////////////////////

    public Controller getControllerCheck(int gameId) throws BusinessLogicException {
        Controller controller = games.get(gameId);
        if (controller == null) throw new BusinessLogicException("Game not found");
        return controller;
    }

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

    public void handleDisconnectRmi(int gameId, String nickname) {
        try {
            Controller ctrl = getControllerCheck(gameId);
            ctrl.markDisconnected(nickname);
            System.out.println("Marked "+nickname+" as DISCONNECTED in game "+gameId);
        } catch (BusinessLogicException e) {
        }
    }
}
