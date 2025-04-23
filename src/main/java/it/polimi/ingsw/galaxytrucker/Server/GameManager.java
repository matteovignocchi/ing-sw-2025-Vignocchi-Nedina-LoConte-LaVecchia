package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Player;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Tiene traccia di tutte le partite in corso, mappando un gameId univoco al relativo Controller.

public class GameManager {
    private final Map< Integer, Controller > sessions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // Crea una nuova partita e restituisce il suo gameId. Il caller chiamerà poi session.join(player) per aggiungere altri giocatori.

    public String createGame(String hostNickname) {
        String gameId = String.valueOf(idCounter.getAndIncrement());
        Controller controller = new Controller(gameId, hostNickname);
        sessions.put(gameId, controller);
        return gameId;
    }

    /**
     * Ritorna true se il giocatore è stato aggiunto con successo alla partita
     */
    public boolean joinGame(String gameId, Player player) {
        Controller controller = sessions.get(gameId);
        if (controller == null) return false;
        return controller.addPlayer(player);
    }

    /**
     * Restituisce il controller per una data partita, o null se non esiste.
     */
    public Controller getController(String gameId) {
        return sessions.get(gameId);
    }

    /**
     * Chiude (rimuove) una partita dal manager.
     */
    public void removeGame(String gameId) {
        sessions.remove(gameId);
    }

    /**
     * Restituisce l’insieme di tutti i gameId attivi
     */
    public Set<String> listActiveGames() {
        return sessions.keySet();
    }

    /**
     * Verifica se esiste una partita con quel gameId
     */
    public boolean exists(String gameId) {
        return sessions.containsKey(gameId);
    }
}