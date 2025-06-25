package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Model.Hourglass;
import it.polimi.ingsw.galaxytrucker.Model.HourglassState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class HourglassTest {

    private Hourglass hg;
    private AtomicBoolean callbackInvoked;

    @BeforeEach
    void setUp() {
        callbackInvoked = new AtomicBoolean(false);
        hg = new Hourglass(h -> callbackInvoked.set(true));
    }

    @Test
    void testFlipIncrementsFlipsAndSetsOngoing() {
        assertEquals(0, hg.getFlips());
        assertEquals(HourglassState.EXPIRED, hg.getState());

        hg.flip();

        assertEquals(1, hg.getFlips(), "getFlips() deve essere 1 dopo il primo flip");
        assertEquals(HourglassState.ONGOING, hg.getState(), "lo stato deve essere ONGOING subito dopo flip()");
    }

    private Runnable getScheduledTask() throws Exception {
        Field f = Hourglass.class.getDeclaredField("scheduledFuture");
        f.setAccessible(true);
        Object sf = f.get(hg);
        assertNotNull(sf, "scheduledFuture non deve essere null dopo flip()");
        assertTrue(sf instanceof Runnable, "scheduledFuture deve implementare Runnable");
        return (Runnable) sf;
    }

    @Test
    void testCallbackAndStateAfterRunningScheduledTask() throws Exception {
        hg.flip();

        Runnable task = getScheduledTask();
        task.run();

        assertTrue(callbackInvoked.get(), "dopo run() del scheduledFuture il callback deve essere chiamato");
        assertEquals(HourglassState.EXPIRED, hg.getState(), "dopo expiry lo stato deve essere EXPIRED");
    }

    @Test
    void testMultipleFlipsInvokeCallbackEachTime() throws Exception {
        hg.flip();
        Runnable t1 = getScheduledTask();
        t1.run();
        assertTrue(callbackInvoked.get(), "primo flip deve invocare callback");
        assertEquals(1, hg.getFlips());

        callbackInvoked.set(false);
        hg.flip();
        assertEquals(2, hg.getFlips(), "dopo due flip getFlips() deve restituire 2");
        Runnable t2 = getScheduledTask();
        t2.run();
        assertTrue(callbackInvoked.get(), "secondo flip deve invocare callback");
    }

    @Test
    void testCancelShutsDownSchedulerAndCancelsFuture() throws Exception {
        injectScheduler(neverScheduler());

        hg.flip();
        hg.cancel();
        Field sfField = Hourglass.class.getDeclaredField("scheduledFuture");
        sfField.setAccessible(true);
        ScheduledFuture<?> sf = (ScheduledFuture<?>) sfField.get(hg);

        assertNotNull(sf, "scheduledFuture non deve essere null dopo flip()");
        assertTrue(sf.isCancelled(), "cancel() deve aver chiamato scheduledFuture.cancel(false)");

        Field schedField = Hourglass.class.getDeclaredField("scheduler");
        schedField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedField.get(hg);

        assertTrue(scheduler.isShutdown(), "cancel() deve shutdownNow() il scheduler");
    }

    @Test
    void testShutdownAlwaysShutsDownScheduler() throws Exception {
        Field schedField = Hourglass.class.getDeclaredField("scheduler");
        schedField.setAccessible(true);
        ScheduledExecutorService before = (ScheduledExecutorService) schedField.get(hg);

        assertFalse(before.isShutdown(), "all’inizio lo scheduler NON deve essere shutdown");
        hg.shutdown();
        ScheduledExecutorService after = (ScheduledExecutorService) schedField.get(hg);
        assertTrue(after.isShutdown(), "shutdown() deve shutdownNow() il scheduler");
    }

    private void injectScheduler(ScheduledExecutorService sched) throws Exception {
        Field fld = Hourglass.class.getDeclaredField("scheduler");
        fld.setAccessible(true);
        fld.set(hg, sched);
    }

    @Test
    void testCancelPreventsCallback() throws Exception {
        injectScheduler(neverScheduler());

        hg.flip();
        hg.cancel();
        Thread.sleep(50);
        assertFalse(callbackInvoked.get(), "callback non deve essere invocato dopo cancel");
        assertEquals(HourglassState.ONGOING, hg.getState());
    }

    @Test
    void testShutdownNoThrow() {
        assertDoesNotThrow(hg::shutdown);
    }

    private ScheduledExecutorService neverScheduler() {
        return new ScheduledThreadPoolExecutor(1) {
            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                return new ScheduledFuture<>() {
                    @Override public long getDelay(TimeUnit u) { return 0; }
                    @Override public int compareTo(java.util.concurrent.Delayed o) { return 0; }
                    @Override public boolean cancel(boolean mayInterruptIfRunning) { return true; }
                    @Override public boolean isCancelled() { return true; }
                    @Override public boolean isDone() { return false; }
                    @Override public Object get() { throw new RuntimeException("Never"); }
                    @Override public Object get(long timeout, TimeUnit u) { throw new RuntimeException("Never"); }
                };
            }
        };
    }
}







