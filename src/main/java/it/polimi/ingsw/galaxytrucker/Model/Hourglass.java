package it.polimi.ingsw.galaxytrucker.Model;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class Hourglass {
    private final int TIMER = 60;
    private int flips = 0;
    private HourglassState state = HourglassState.EXPIRED;

    private final Consumer<Hourglass> controllerCallBack;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public Hourglass(Consumer<Hourglass> controllerCallBack) {
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

    public synchronized int  getFlips() { return flips; }

    public synchronized HourglassState getState() { return state; }
}