package tc.oc.commons.core.restart;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import sun.misc.Signal;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.minecraft.users.OnlinePlayers;
import tc.oc.commons.core.exception.NamedThreadFactory;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.Comparables;
import tc.oc.minecraft.api.scheduler.Tickable;
import tc.oc.minecraft.api.server.LocalServer;

/**
 * Manages restarting logic for all plugins.
 *
 * Also monitors uptime and memory use and automatically requests a restart if needed.
 */
@Singleton
public class RestartManager implements PluginFacet, Tickable {

    private final Logger logger;
    private final LocalServer minecraftServer;
    private final MinecraftService minecraftService;
    private final Server localServer;
    private final RestartConfiguration config;
    private final EventBus eventBus;
    private final OnlinePlayers onlinePlayers;
    private final NamedThreadFactory threads;

    private final Instant startTime;
    private @Nullable RequestRestartEvent currentRequest;
    private @Nullable ScheduledExecutorService timer;

    @Inject RestartManager(Loggers loggers, LocalServer minecraftServer, MinecraftService minecraftService, Server localServer, RestartConfiguration config, EventBus eventBus, OnlinePlayers onlinePlayers, NamedThreadFactory threads) {
        this.localServer = localServer;
        this.onlinePlayers = onlinePlayers;
        this.logger = loggers.get(getClass());
        this.minecraftServer = minecraftServer;
        this.minecraftService = minecraftService;
        this.config = config;
        this.eventBus = eventBus;
        this.threads = threads;
        this.startTime = Instant.now();
        onSignal(this.config.stopSignals());
    }

    @Override
    public java.time.Duration tickPeriod() {
        return config.interval();
    }

    @Override
    public void enable() {
        final Duration uptimeLimit = config.uptimeLimit();
        if(uptimeLimit != null) {
            // Use a timer to schedule the uptime limit restart,
            // because the check in the tick method does not run
            // while the server is suspended.
            logger.info("Scheduling restart in " + uptimeLimit);
            timer = Executors.newSingleThreadScheduledExecutor(threads.newThreadFactory("Restart timer"));
            timer.schedule(
                () -> requestUptimeRestart(uptimeLimit),
                uptimeLimit.toMillis(),
                TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public void disable() {
        if(timer != null) {
            timer.shutdownNow();
        }
    }

    @Override
    public void tick() {
        if(!this.restartIfRequested()) {
            Duration uptime = Duration.between(this.startTime, Instant.now());
            Duration uptimeLimit = config.uptimeLimit();

            if(uptimeLimit != null && Comparables.greaterOrEqual(uptime, uptimeLimit)) {
                requestUptimeRestart(uptime);
            } else {
                long memory = Runtime.getRuntime().totalMemory();
                long memoryLimit = config.memoryLimit();

                if(memoryLimit > 0 && memory > memoryLimit) {
                    this.requestRestart("Exceeded memory limit (" + memory + " > " + memoryLimit + ")");
                }
            }
        }
    }

    public @Nullable Instant restartRequestedAt() {
        return localServer.restart_queued_at();
    }

    public boolean isRestartRequested() {
        return localServer.restart_queued_at() != null;
    }

    public boolean isRestartRequested(int priority) {
        return isRestartRequested() &&
               localServer.restart_priority() >= priority;
    }

    private Set<RequestRestartEvent.Deferral> deferrals() {
        return currentRequest != null ? currentRequest.deferrals()
                                      : Collections.emptySet();
    }

    private boolean isDeferralTimedOut() {
        return isRestartRequested() &&
               config.deferTimeout() != null &&
               !restartRequestedAt().plus(config.deferTimeout()).isAfter(Instant.now());
    }

    public boolean isRestartDeferred() {
        return !(deferrals().isEmpty() || isDeferralTimedOut());
    }

    public ListenableFuture<?> requestUptimeRestart(Duration uptime) {
        return requestRestart("Exceeded uptime limit (" + uptime + " >= " + config.uptimeLimit() + ")");
    }

    public ListenableFuture<?> requestRestart(String reason) {
        return requestRestart(reason, ServerDoc.Restart.Priority.NORMAL);
    }

    public ListenableFuture<?> requestRestart(String reason, int priority) {
        if(this.isRestartRequested(priority)) {
            return Futures.immediateCancelledFuture();
        } else {
            final Instant now = Instant.now();
            logger.info("Requesting restart at " + now + ", because " + reason);
            return minecraftService.updateLocalServer(new ServerDoc.Restart() {
                @Override public Instant restart_queued_at() { return now; }
                @Override public String restart_reason() { return reason; }
                @Override public int restart_priority() { return priority; }
            });
        }
    }

    private void requestRestartInternal(Instant time, String reason, int priority) {
        logger.info("Restart requested at " + time +
                    ", with " + priority +
                    " priority, because \"" + reason + '"');
        currentRequest = new RequestRestartEvent(logger, reason, priority, this::restartIfRequested);
        eventBus.post(currentRequest);
        restartIfRequested();
    }

    public ListenableFuture<?> cancelRestart() {
        if(this.isRestartRequested()) {
            return minecraftService.updateLocalServer(new ServerDoc.Restart() {
                @Override public Instant restart_queued_at() { return null; }
                @Override public String restart_reason() { return null; }
            });
        } else {
            return Futures.immediateCancelledFuture();
        }
    }

    @Subscribe
    public void onReconfigure(LocalServerReconfigureEvent event) {
        final Instant oldTime, newTime;
        final String oldReason, newReason;
        final int oldPriority, newPriority;

        if(event.getOldConfig() == null) {
            oldTime = null;
            oldReason = null;
            oldPriority = ServerDoc.Restart.Priority.NORMAL;
        } else {
            oldTime = event.getOldConfig().restart_queued_at();
            oldReason = event.getOldConfig().restart_reason();
            oldPriority = event.getOldConfig().restart_priority();
        }

        newTime = event.getNewConfig().restart_queued_at();
        newReason = event.getNewConfig().restart_reason();
        newPriority = event.getNewConfig().restart_priority();

        if(Objects.equals(oldTime, newTime) &&
           Objects.equals(oldReason, newReason) &&
           Objects.equals(oldPriority, newPriority)) return;

        if(oldTime != null) {
            logger.info("Restart cancelled");
            currentRequest = null;
            eventBus.post(new CancelRestartEvent());
        }

        if(newTime != null) {
            requestRestartInternal(newTime, newReason, newPriority);
        }
    }

    private boolean shouldRestartNow() {
        // If no restart requested, don't restart
        if(!isRestartRequested()) return false;

        // If there are deferrals, and they are not timed out, don't restart
        if(isRestartDeferred()) return false;

        // If there are more players online than we are allowed to kick, don't restart
        if(onlinePlayers.count() > config.kickLimit()) {
            logger.info("Deferring restart because more than " + config.kickLimit() + " players are online");
            return false;
        }

        return true;
    }

    private boolean restartIfRequested() {
        if(shouldRestartNow() && !minecraftServer.isStopping()) {
            logger.info("Restarting due to request at " + restartRequestedAt());
            minecraftServer.stop();
            return true;
        } else {
            return false;
        }
    }

    private void onSignal(Collection<String> signals) {
        signals.stream()
               .map(Signal::new)
               .forEach(signal -> Signal.handle(signal, s -> {
                   requestRestartInternal(Instant.now(), "Received signal " + s.getName() + " (" + s.getNumber() + ") from system", Integer.MAX_VALUE);
                   try {
                       Thread.sleep(currentRequest.deferrals()
                                                  .stream()
                                                  .filter(deferral -> deferral.predictedDelay() != null)
                                                  .map(RequestRestartEvent.Deferral::predictedDelay)
                                                  .findFirst()
                                                  .orElse(Duration.ZERO)
                                                  .toMillis() + 1L);
                   } catch(InterruptedException e) {
                       if(!minecraftServer.isStopping()) {
                           logger.severe(s.getName() + " signal is unable to wait for restart");
                       }
                   }
                   if(!restartIfRequested() && !minecraftServer.isStopping()) {
                       minecraftServer.stop();
                   }
               }));
    }
}
