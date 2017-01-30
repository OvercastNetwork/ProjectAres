package tc.oc.commons.bungee.restart;

import java.time.Instant;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.eventbus.Subscribe;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.minecraft.users.OnlinePlayers;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.restart.CancelRestartEvent;
import tc.oc.commons.core.restart.RequestRestartEvent;
import tc.oc.commons.core.restart.RestartConfiguration;
import tc.oc.commons.core.scheduler.ReusableTask;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.scheduler.Task;

public class RestartListener implements PluginFacet, Listener, Runnable {

    private final Logger logger;
    private final RestartConfiguration config;
    private final Server localServer;
    private final OnlinePlayers onlinePlayers;
    private final ReusableTask task;

    private @Nullable RequestRestartEvent.Deferral deferral;

    @Inject RestartListener(Loggers loggers, RestartConfiguration config, Server localServer, OnlinePlayers onlinePlayers, Scheduler scheduler) {
        this.logger = loggers.get(getClass());
        this.config = config;
        this.localServer = localServer;
        this.onlinePlayers = onlinePlayers;
        this.task = scheduler.createReusableTask(this);
    }

    private boolean canRestart(int playerCount) {
        // If server is on public DNS, it cannot restart
        if(localServer.dns_enabled()) {
            logger.info("Deferring restart because server is on public DNS");
            return false;
        }

        // If we are still waiting for the server to empty, it cannot restart
        final Instant deadline = localServer.dns_toggled_at().plus(config.emptyTimeout());
        if(playerCount > 0 && deadline.isAfter(Instant.now())) {
            logger.info("Deferring restart until the server empties or until " + deadline + ", whichever is first");
            task.schedule(Task.Parameters.fromInstant(deadline));
            return false;
        }

        // If we have given up waiting for the server to empty, it can restart.
        // However, the kick-limit enforced by RestartManager may still prevent it from restarting.
        return true;
    }

    private void update(int playerCount) {
        if(this.deferral != null && canRestart(playerCount)) {
            final RequestRestartEvent.Deferral deferral = this.deferral;
            this.deferral = null;
            deferral.resume();
        }
    }

    @Override
    public void run() {
        update(onlinePlayers.count());
    }

    @Subscribe
    public void onRequestRestart(RequestRestartEvent event) {
        if(event.priority() < ServerDoc.Restart.Priority.HIGH && !canRestart(onlinePlayers.count())) {
            deferral = event.defer(getClass().getName());
        }
    }

    @Subscribe
    public void onCancelRestart(CancelRestartEvent event) {
        deferral = null;
    }

    @Subscribe
    public void onReconfigure(LocalServerReconfigureEvent event) {
        if(event.getOldConfig() != null &&
           event.getOldConfig().dns_enabled() &&
           !event.getNewConfig().dns_enabled()) run();
    }

    @EventHandler
    synchronized public void onConnect(final LoginEvent event) {
        this.update(onlinePlayers.count() + 1);
    }

    @EventHandler
    synchronized public void onDisconnect(final PlayerDisconnectEvent event) {
        this.update(onlinePlayers.count() - 1);
    }

}
