package it.polimi.ingsw.galaxytrucker.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handles asynchronous responses from the server mapped by unique request IDs.
 *
 * Maintains a thread-safe map of blocking queues where each queue corresponds
 * to a pending request. Supports registering expected responses, handling incoming
 * messages, and waiting for responses with a timeout.
 * @author Matteo Vignocchi
 */

public class ResponseHandler {

    // Mappa delle risposte attese: requestId -> coda bloccante
    private final ConcurrentHashMap<String, BlockingQueue<Message>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * Registers that a response is expected for the given request ID.
     * Initializes a blocking queue to hold the response messages for that request.
     * @param requestId the unique ID of the request
     */
    public void expect(String requestId) {
        pendingResponses.putIfAbsent(requestId, new LinkedBlockingQueue<>());
    }

    /**
     * Checks whether there is a pending response for the specified request ID.
     * @param requestId the request ID to check
     * @return true if a response is expected, false otherwise
     */
    public boolean hasPending(String requestId) {
        return pendingResponses.containsKey(requestId);
    }

    /**
     * Adds a received response message to the queue associated with the request ID.
     * If no queue exists for the request ID, the response is silently ignored.
     * @param requestId the ID of the request being responded to
     * @param msg the response message to enqueue
     */
    public void handleResponse(String requestId, Message msg) {
        BlockingQueue<Message> queue = pendingResponses.get(requestId);
        if (queue != null) {
            queue.offer(msg);
        }
    }

    /**
     * Blocks and waits for a response message associated with the given request ID.
     * Waits up to 20 minutes before timing out. Once the response is received, the
     * pending response entry is removed from the map.
     * @param requestId the request ID to wait for
     * @return the response message
     * @throws InterruptedException if the wait times out or the thread is interrupted
     * @throws IllegalStateException if no response was expected for the request ID
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
