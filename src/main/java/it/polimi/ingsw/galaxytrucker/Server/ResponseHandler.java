package it.polimi.ingsw.galaxytrucker.Server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseHandler {
    private final BlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();

    public void handleResponse(Message msg) {
        responseQueue.offer(msg.getPayload());
    }

    public Object waitForResponse() throws InterruptedException {
        return responseQueue.take();
    }
}