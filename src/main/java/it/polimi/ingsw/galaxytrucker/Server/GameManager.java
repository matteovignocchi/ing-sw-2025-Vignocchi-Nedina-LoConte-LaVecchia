package it.polimi.ingsw.galaxytrucker.Server;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//TODO:
// 1)cambiare synchronized -> lock
// 2)eliminare metodi di utility che non vengono utilizzati, ma solo dopo

////////////////////////////////////////////////GESTIONE GAME///////////////////////////////////////////////////////////

public class GameManager {
    private final Map<Integer, Controller> games;
    private final AtomicInteger idCounter;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<Integer, ScheduledFuture<?>> timeout = new ConcurrentHashMap<>();

    public GameManager() {
        this.games = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
        loadSavedGames(); // Caricamento automatico all'avvio
    }

    public synchronized int createGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws Exception {
        int gameId = idCounter.getAndIncrement();
        Controller controller = new Controller(isDemo, maxPlayers);

        controller.addPlayer(nickname, v);
        saveGameState(gameId, controller);
        sendUpdate(gameId, nickname);
        return gameId;
    }

    public synchronized void joinGame(int gameId, VirtualView v, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);

        if(controller.getPlayerByNickname(nickname) != null){
            cancelTimeout(gameId);
            controller.markReconnected(nickname, v);
            controller.broadcastInform(nickname + "is reconnected");
        } else {
            controller.addPlayer(nickname, v);
            if(controller.countConnectedPlayers() == controller.getMaxPlayers()){
                beginGame(gameId);
            }
        }
        saveGameState(gameId, controller);
        sendUpdate(gameId, nickname);
    }

    public synchronized void quitGame(int gameId, String nickname) throws BusinessLogicException, IOException {
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

    //TODO: dire al fra/floris se spostare parte di questi metodi nel controller (creando gli appositi)

    public synchronized Tile getCoveredTile(int gameId, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);
        Player p = getPlayerCheck(controller, nickname);

        int size = controller.getPileOfTile().size();
        if(size == 0) throw new BusinessLogicException("Pile of tiles is empty");

        int randomIdx = ThreadLocalRandom.current().nextInt(size);
        p.setGameFase(GameFase.TILE_MANAGEMENT);
        sendUpdate(gameId, nickname);
        return controller.getTile(randomIdx);
    }

    public synchronized List<Tile> getUncoveredTilesList(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);

        List<Tile> uncoveredTiles = controller.getShownTiles();
        if(uncoveredTiles.isEmpty()) throw new BusinessLogicException("Pile of uncovered tiles is empty");
        return uncoveredTiles;
    }

    public synchronized Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws Exception {
        Controller controller = getControllerCheck(gameId);
        VirtualView v = getViewCheck(controller, nickname);

        List<Tile> uncoveredTiles = controller.getShownTiles();
        Optional<Tile> opt = uncoveredTiles.stream().filter(t -> t.getIdTile() == idTile).findFirst();
        if(opt.isEmpty()) throw new BusinessLogicException("Tile already taken");

        Player p = getPlayerCheck(controller, nickname);
        p.setGameFase(GameFase.TILE_MANAGEMENT);
        sendUpdate(gameId, nickname);
        return controller.getShownTile(uncoveredTiles.indexOf(opt.get()));
    }

    public synchronized void dropTile (int gameId, String nickname, Tile tile) throws Exception {
        Controller controller = getControllerCheck(gameId);
        Player p = getPlayerCheck(controller, nickname);

        controller.addToShownTile(tile);
        p.setGameFase(GameFase.BOARD_SETUP);
        sendUpdate(gameId, nickname);
    }

    public synchronized void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws Exception {
        Controller controller = getControllerCheck(gameId);
        Player p = getPlayerCheck(controller, nickname);

        p.addTile(cord[0], cord[1], tile);
        p.setGameFase(GameFase.BOARD_SETUP);
        sendUpdate(gameId, nickname);
    }

    public synchronized void setReady(int gameId, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);
        Player p = getPlayerCheck(controller, nickname);

        controller.setPlayerReady(p);

        List<Player> playersInGame = controller.getPlayersInGame();
        if(playersInGame.stream().allMatch( e -> e.getGameFase() == GameFase.WAITING_FOR_PLAYERS)) {
            controller.startFlight();
        } else{
            p.setGameFase(GameFase.WAITING_FOR_PLAYERS);
            sendUpdate(gameId, nickname);
        }
    }

    public synchronized void flipHourglass(int gameId, String nickname) throws Exception {
        Controller controller = getControllerCheck(gameId);
        controller.flipHourglass(nickname);
    }

    public List<Card> showDeck(int gameId, int idxDeck) throws IOException, BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        return controller.showDeck(idxDeck);
    }


    //da finire
    public synchronized void drawCard(int gameId, Card card) throws IOException, BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.activateCard(card);
        //gestire le fasi (ricalcolare il leader (sua fase DrawCard)), informare e updatare le views
        //update(?)
    }

    //da finire
    public void lookDashBoard(String nickname, int gameId) throws Exception {
        Controller controller = findControllerByPlayer(nickname);
        controller.lookDashBoard(nickname, gameId);
        sendUpdate(gameId, nickname);
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

    private Controller findControllerByPlayer(String nickname) throws IOException {
        for (Controller controller : games.values()) {
            if (controller.getPlayerByNickname(nickname) != null) {
                return controller;
            }
        }
        throw new IOException("Player not found in any game");
    }

    public Set<Integer> listActiveGames() {
        return games.keySet();
    }

    private void setTimeout(int gameId) throws BusinessLogicException {
        ScheduledFuture<?> old = timeout.remove(gameId); // se esiste già un timeout lo annullo
        if (old != null) old.cancel(false);

        Controller controller = getControllerCheck(gameId);
        int connected = controller.countConnectedPlayers();

        long time = (connected == 1 ? 5 : connected == 0 ? 10 : -1);

        //programmo il timeout e al termine di time chiamo onTimout
        ScheduledFuture<?> task = scheduler.schedule(() -> {
                    try {
                        onTimeout(gameId);
                    } catch (Exception ignore) {}
                    },
                time, TimeUnit.MINUTES
        );
        timeout.put(gameId, task); //tengo il riferimento per poterlo eventualmente cancellare
    }

    private void cancelTimeout(int gameId) {
        ScheduledFuture<?> old = timeout.remove(gameId);
        if (old != null) {
            old.cancel(false);
        }
    }

    private void onTimeout(int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        int connected = controller.countConnectedPlayers();

        if (connected == 1) {
            Player player_winner = controller.getPlayersInGame().get(0);
            String winner = controller.getNickname(player_winner);
            try {
                controller.getViewByNickname(winner).inform("You won the game!");
            } catch (IOException ignored) {}
        }
        // se 0 o >1: nessun vincitore
        removeGame(gameId);
    }

    //Manda l’aggiornamento allo user; se il client non risponde, gestisce la disconnessione
    private void sendUpdate(int gameId, String nickname) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        try {
            controller.updatePlayer(nickname);
            cancelTimeout(gameId);// se era partito un timeout per questo game, lo cancelliamo
        } catch (RemoteException e) {
            controller.markDisconnected(nickname);// client non risponde → lo marchiamo disconnesso
            controller.broadcastInform(nickname + "is disconnected");

            int connected = controller.countConnectedPlayers();
            if (connected <= 1) {
                try {
                    setTimeout(gameId);
                } catch (BusinessLogicException ignore) {
                    // non dovrebbe succedere, ma in caso lo ignoriamo
                }
            }
        }
    }

    private void beginGame(int gameId) throws BusinessLogicException {
        Controller controller = getControllerCheck(gameId);
        controller.startGame();

        for (Player p : controller.getPlayersInGame()) {
            String nick = controller.getNickname(p);
            sendUpdate(gameId, nick);
        }
    }
}
