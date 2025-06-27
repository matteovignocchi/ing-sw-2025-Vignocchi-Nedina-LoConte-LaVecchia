package it.polimi.ingsw.galaxytrucker.Model;

import java.io.Serializable;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Manages a 60-second countdown timer ("hourglass") that can be flipped to start or restart.
 * Tracks how many times it has been flipped and its current state, and invokes a callback
 * when the timer expires.
 *
 * @author Gabriele La Vecchia
 */

public class Hourglass implements Serializable {
    private final int TIMER = 60;
    private int flips = 0;
    private HourglassState state = HourglassState.EXPIRED;

    private final Consumer<Hourglass> controllerCallBack;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    /**
     * Creates an Hourglass with the specified callback to notify when the timer expires.
     *
     * @param controllerCallBack the callback to invoke upon expiration
     */
    public Hourglass(Consumer<Hourglass> controllerCallBack) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.controllerCallBack = controllerCallBack;
    }

    /**
     * Flips the hourglass to start or restart the countdown.
     * Increments the flip count, sets the state to ONGOING, and schedules a task
     * to mark the state as EXPIRED and invoke the callback after {@code TIMER} seconds.
     */
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

    /**
     * Cancels the current countdown if it is running, and shuts down the scheduler.
     * If a scheduled expiration task exists and is not yet done, it is cancelled.
     * Then the scheduler is shut down immediately.
     */
    public synchronized void cancel() {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Immediately shuts down the scheduler, cancelling any pending tasks.
     */
    public void shutdown() {scheduler.shutdownNow();}

    /**
     * Returns the number of times the hourglass has been flipped.
     *
     * @return the flip count
     */
    public synchronized int  getFlips() { return flips; }

    /**
     * Returns the current state of the hourglass.
     *
     * @return the current HourglassState (ONGOING or EXPIRED)
     */
    public synchronized HourglassState getState() { return state; }
}