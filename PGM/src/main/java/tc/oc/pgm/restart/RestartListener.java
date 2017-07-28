package tc.oc.pgm.restart;

import java.time.Duration;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.eventbus.Subscribe;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerSuspendEvent;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.restart.CancelRestartEvent;
import tc.oc.commons.core.restart.RequestRestartEvent;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.events.ConfigLoadEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchLoadEvent;
import tc.oc.pgm.events.MatchUnloadEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;

/**
 * Listens for {@link RequestRestartEvent} and defers it until after the current match
 * and the restart countdown. Also listens for {@link CancelRestartEvent} and handles
 * it appropriately.
 *
 * Also keeps count of matches and requests a restart from {@link RestartManager} after
 * the configured limit.
 */
@Singleton
public class RestartListener implements PluginFacet, Listener {

    private final Logger logger;
    private final RestartManager restartManager;
    private final MatchManager mm;
    private final Server server;
    private AutoRestartConfiguration config;

    private @Nullable Integer matchLimit;
    private @Nullable RequestRestartEvent.Deferral deferral;
    private boolean startingCountdown;

    @Inject RestartListener(Loggers loggers, RestartManager restartManager, MatchManager mm, Server server, AutoRestartConfiguration config) {
        this.restartManager = restartManager;
        this.logger = loggers.get(getClass());
        this.mm = mm;
        this.server = server;
        this.config = config;
        this.matchLimit = config.matchLimit() > 0 ? config.matchLimit() : null;
    }

    @EventHandler
    public void configure(ConfigLoadEvent event) {
         this.config = new AutoRestartConfiguration(event.getConfig());
    }

    private boolean isCountdownRunning(Match match) {
        return match.countdowns().getCountdown() instanceof RestartCountdown;
    }

    private void startCountdown(Match match, Duration duration) {
        try {
            startingCountdown = true;
            match.ensureNotRunning();
            match.countdowns().start(new RestartCountdown(match), duration);
        } finally {
            startingCountdown = false;
        }
    }

    /**
     * Start a countdown of the given duration after cancelling any existing one
     */
    private void forceCountdown(Match match, Duration duration) {
        match.countdowns().cancelAll();
        logger.info("Starting countdown from " + duration);
        startCountdown(match, duration);
    }

    private void ensureCountdown(Match match) {
        if(!isCountdownRunning(match) && !startingCountdown) {
            logger.info("Starting countdown from " + config.time());
            startCountdown(match, config.time());
        }
    }

    private void cancelCountdown(Match match) {
        if(isCountdownRunning(match)) {
            logger.info("Cancelling countdown");
            match.countdowns().cancelAll();
        }
    }

    public boolean willRestart(Match match) {
        return startingCountdown || canResumeRestart(match);
    }

    private boolean canResumeRestart(Match match) {
        if(deferral == null) return false;
        final int priority = deferral.request().priority();
        if(priority >= ServerDoc.Restart.Priority.HIGH) {
            // Restart immediately
            return true;
        } else if(priority >= ServerDoc.Restart.Priority.NORMAL) {
            // Restart after current match ends
            return match.canAbort();
        } else {
            // Restart when server is empty
            return match.getPlayers().isEmpty();
        }
    }

    private void checkCountdown(Match match) {
        if(deferral != null) {
            if(canResumeRestart(match)) {
                ensureCountdown(match);
            } else {
                cancelCountdown(match);
            }
        }
    }

    /**
     * When a restart is requested, let it restart immediately if the server if empty,
     * otherwise defer the restart and ensure that a countdown is running.
     */
    @Subscribe
    public void onRequestRestart(RequestRestartEvent event) {
        if(!server.isSuspended()) {
            logger.info("Deferring restart");
            deferral = event.defer(getClass().getName());
            checkCountdown(mm.needCurrentMatch());
        }
    }

    /**
     * When restart is cancelled, cancel any countdown and discard our deferral
     */
    @Subscribe
    public void onCancelRestart(CancelRestartEvent event) {
        cancelCountdown(mm.needCurrentMatch());
        deferral = null;
    }

    @EventHandler
    public void onSuspend(ServerSuspendEvent event) {
        if(deferral != null) {
            deferral.resume();
            deferral = null;
        }
    }

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        checkCountdown(event.getMatch());
    }

    /**
     * When match ends, start a countdown if a restart is already requested,
     * otherwise check for the match limit and request a restart if needed.
     * This listens on LOW priority so that it takes priority over map cycling.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMatchEnd(MatchEndEvent event) {
        if(startingCountdown) return;

        if(deferral != null) {
            checkCountdown(event.getMatch());
        } else if(matchLimit != null && event.getMatch().serialNumber() >= matchLimit) {
            restartManager.requestRestart("Reached match limit (" + event.getMatch().serialNumber() + " >= " + matchLimit + ")");
        }
    }

    @EventHandler
    public void onMatchUnload(MatchUnloadEvent event) {
        if(deferral != null && deferral.request().priority() >= ServerDoc.Restart.Priority.NORMAL) {
            logger.info("Resuming restart because the match unloaded");
            deferral.resume();
            deferral = null;
        }
    }

    /**
     * If the match empties out while a restart is queued, end the match
     */
    @EventHandler
    public void onPartyChange(PlayerPartyChangeEvent event) {
        checkCountdown(event.getMatch());
    }

    /**
     * Request a restart from {@link RestartManager} and defer it with
     * a countdown of the given duration.
     */
    public void queueRestart(Match match, Duration duration, String reason) {
        restartManager.requestRestart(reason);
        forceCountdown(match, duration);
    }

    /**
     * Set the match limit restart to the given number of matches from now,
     * or disable the limit if given null;
     * @return The number of matches from now when the server will restart
     */
    public @Nullable Integer restartAfterMatches(Match match, @Nullable Integer matches) {
        matchLimit = matches == null ? null : match.serialNumber() + matches;
        return matchLimit == null ? null : matchLimit - match.serialNumber();
    }

    public class RestartCountdown extends MatchCountdown {
        public RestartCountdown(Match match) {
            super(match);
        }

        @Override
        public BaseComponent barText(Player viewer) {
            if(Comparables.greaterThan(remaining, Duration.ZERO)) {
                return new Component(new TranslatableComponent("broadcast.serverRestart.message",
                                                               secondsRemaining(ChatColor.DARK_RED)),
                                     ChatColor.AQUA);
            } else {
                return new Component(new TranslatableComponent("broadcast.serverRestart.kickMsg"),
                                     ChatColor.RED);
            }
        }

        @Override
        public void onEnd(Duration total) {
            super.onEnd(total);
            if(deferral != null) {
                logger.info("Resuming restart after countdown");
                deferral.resume();
                deferral = null;
            }
        }
    }
}
