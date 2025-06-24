package it.polimi.ingsw.galaxytrucker.Model;

import java.io.Serializable;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Hourglass implements Serializable {
    private final int TIMER = 60;
    private int flips = 0;
    private HourglassState state = HourglassState.EXPIRED;

    private final Consumer<Hourglass> controllerCallBack;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public Hourglass(Consumer<Hourglass> controllerCallBack) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.controllerCallBack = controllerCallBack;
    }

    public synchronized void flip() {
        flips++;
        state = HourglassState.ONGOING;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = scheduler.schedule(() -> {
            synchronized(this) {
                state = HourglassState.EXPIRED;
            }
            controllerCallBack.accept(this);
            scheduler.shutdown();
        }, TIMER, TimeUnit.SECONDS);
    }

    public synchronized void cancel() {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void shutdown() {scheduler.shutdownNow();}

    public synchronized int  getFlips() { return flips; }

    public synchronized HourglassState getState() { return state; }
}