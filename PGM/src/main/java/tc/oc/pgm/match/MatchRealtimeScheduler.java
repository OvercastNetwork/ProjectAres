package tc.oc.pgm.match;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchUnloadEvent;

/**
 * Can be used by other modules to schedule tasks in real time (not tick time).
 *
 * Tasks scheduled with zero delay are run synchronously.
 *
 * Pending tasks are cancelled when the match unloads, and tasks that are scoped
 * to the running match are cancelled when the match ends.
 */
@ListenerScope(MatchScope.LOADED)
public class MatchRealtimeScheduler implements Listener {

    private final Match match;
    private final MainThreadExecutor mainThreadExecutor;
    private final ListeningScheduledExecutorService schedulerLoaded;
    private final ListeningScheduledExecutorService schedulerRunning;
    private final List<Runnable> atMatchStart = new ArrayList<>();

    @Inject MatchRealtimeScheduler(Match match, MainThreadExecutor mainThreadExecutor) {
        this.match = match;
        this.mainThreadExecutor = mainThreadExecutor;

        this.schedulerLoaded = newScheduler();
        this.schedulerRunning = newScheduler();
    }

    private static ListeningScheduledExecutorService newScheduler() {
        final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setRemoveOnCancelPolicy(true);
        return MoreExecutors.listeningDecorator(scheduler);
    }

    public void schedule(Duration delay, Runnable task) {
        if(Comparables.greaterThan(delay, Duration.ZERO)) {
            schedulerLoaded.schedule(() -> schedule(Duration.ZERO, task),
                                     delay.toMillis(),
                                     TimeUnit.MILLISECONDS);
        } else {
            mainThreadExecutor.execute(() -> {
                if(match.isLoaded()) {
                    task.run();
                }
            });
        }
    }

    public void schedule(Instant time, Runnable task) {
        mainThreadExecutor.execute(() -> {
            if(match.isUnloaded()) return;
            final Instant now = match.getInstantNow();
            if(!match.isLoaded() || time.isAfter(now)) {
                schedulerLoaded.schedule(() -> schedule(time, task),
                                         Math.max(50, Duration.between(now, time).toMillis()),
                                         TimeUnit.MILLISECONDS);
            } else {
                task.run();
            }
        });
    }

    public void scheduleAtRunningTime(Duration time, Runnable task) {
        mainThreadExecutor.execute(() -> {
            if(!match.hasStarted()) {
                atMatchStart.add(() -> scheduleAtRunningTime(time, task));
            } else if(!match.isFinished()) {
                final Duration now = match.runningTime();
                if(Comparables.greaterThan(time, now)) {
                    schedulerRunning.schedule(() -> scheduleAtRunningTime(time, task),
                                              Math.max(50, time.toMillis() - now.toMillis()),
                                              TimeUnit.MILLISECONDS);
                } else {
                    task.run();
                }
            }
        });
    }

    @EventHandler
    void start(MatchBeginEvent event) {
        atMatchStart.forEach(mainThreadExecutor::execute);
        atMatchStart.clear();
    }

    @EventHandler
    void start(MatchEndEvent event) {
        schedulerRunning.shutdown();
    }

    @EventHandler
    void unload(MatchUnloadEvent event) {
        schedulerLoaded.shutdown();
    }
}
