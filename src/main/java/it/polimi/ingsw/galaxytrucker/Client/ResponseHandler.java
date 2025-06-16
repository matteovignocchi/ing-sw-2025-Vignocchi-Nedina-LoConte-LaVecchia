package it.polimi.ingsw.galaxytrucker.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ResponseHandler {

    // Mappa delle risposte attese: requestId -> coda bloccante
    private final ConcurrentHashMap<String, BlockingQueue<Message>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * Registra l'attesa di una risposta per uno specifico requestId
     */
    public void expect(String requestId) {
        pendingResponses.putIfAbsent(requestId, new LinkedBlockingQueue<>());
    }

    /**
     * Controlla se stiamo ancora aspettando una response per questo requestId
     */
    public boolean hasPending(String requestId) {
        return pendingResponses.containsKey(requestId);
    }

    /**
     * Inserisce la risposta ricevuta nella coda corrispondente al requestId,
     * se qualcuno la stava aspettando.
     */
    public void handleResponse(String requestId, Message msg) {
        BlockingQueue<Message> queue = pendingResponses.get(requestId);
        if (queue != null) {
            queue.offer(msg);
        }
        // altrimenti: ignoro silenziosamente
    }

    /**
     * Attende e restituisce la risposta per un requestId specifico
     */
    public Message waitForResponse(String requestId) throws InterruptedException {
        BlockingQueue<Message> queue = pendingResponses.get(requestId);
        if (queue == null) {
            throw new IllegalStateException("No response expected for this requestId: " + requestId);
        }
        Message response = queue.poll(20, TimeUnit.MINUTES);
        if (response == null) {
            throw new InterruptedException("Timeout expired waiting for response to: " + requestId);
        }
        pendingResponses.remove(requestId);
        return response;
    }
}
