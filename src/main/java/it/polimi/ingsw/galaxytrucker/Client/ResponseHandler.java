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
     * Inserisce la risposta ricevuta nella coda corrispondente al requestId
     */
    public void handleResponse(String requestId, Message msg) {
        BlockingQueue<Message> queue = pendingResponses.get(requestId);
        if (queue != null) {
            queue.offer(msg);
        } else {
            System.err.println("No response from this requestId: " + requestId);
        }
    }

    /**
     * Attende e restituisce la risposta per un requestId specifico
     */
    public Message waitForResponse(String requestId) throws InterruptedException {
        BlockingQueue<Message> queue = pendingResponses.get(requestId);
        if (queue == null) {
            throw new IllegalStateException("No response from this requestId: " + requestId);
        }
        Message response = queue.poll(20, TimeUnit.MINUTES);    // opzionale: timeout per evitare blocchi infiniti
        if (response == null) {
            throw new InterruptedException("Timeout expired: " + requestId);
        }

        pendingResponses.remove(requestId);
        return response;
    }
}