package tc.oc.pgm.start;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.time.Duration;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.countdowns.SingleCountdownContext;
import tc.oc.pgm.cycle.CycleMatchModule;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPreCommitEvent;
import tc.oc.pgm.events.PlayerJoinMatchEvent;
import tc.oc.pgm.events.PlayerLeaveMatchEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.restart.RestartListener;

import static com.google.common.base.Preconditions.checkState;

@ListenerScope(MatchScope.LOADED)
public class StartMatchModule extends MatchModule implements Listener {

    class UnreadyTimeout extends MatchCountdown {
        public UnreadyTimeout(Match match) {
            super(match);
        }

        @Override
        public BaseComponent barText(Player viewer) {
            checkState(!unreadyReasons.isEmpty());
            return new Component(unreadyReasons.iterator().next().getReason(), ChatColor.RED);
        }

        @Override
        public BarColor barColor(Player viewer) {
            return BarColor.RED;
        }

        @Override
        public void onEnd(Duration total) {
            super.onEnd(total);
            getMatch().needMatchModule(CycleMatchModule.class).cycleNow();
        }
    }

    protected final Set<UnreadyReason> unreadyReasons = new HashSet<>();
    protected final BossBarMatchModule bbmm;
    protected final StartConfig config;
    protected boolean autoStart; // Initialized from config, but is mutable

    @Inject StartMatchModule(StartConfig config, BossBarMatchModule bbmm) {
        this.config = config;
        this.bbmm = bbmm;
        this.autoStart = this.config.autoStart();
    }

    @Override
    public void load() {
        update();
    }

    @EventHandler
    public void onCommit(MatchPreCommitEvent event) {
        unreadyReasons.clear();
    }

    private SingleCountdownContext cc() {
        return getMatch().countdowns();
    }

    /**
     * If true, the match start countdown will automatically start when conditions allow it
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Enable/disable auto-start and return true if the setting was changed
     */
    public boolean setAutoStart(boolean autoStart) {
        if(this.autoStart != autoStart) {
            this.autoStart = autoStart;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get all {@link UnreadyReason}s currently preventing the match from starting.
     * If forceStart is true, only return the reasons that prevent the match from
     * being force started.
     */
    public Collection<UnreadyReason> getUnreadyReasons(boolean forceStart) {
        if(forceStart) {
            List<UnreadyReason> reasons = new ArrayList<>(unreadyReasons.size());
            for(UnreadyReason reason : unreadyReasons) {
                if(!reason.canForceStart()) reasons.add(reason);
            }
            return reasons;
        } else {
            return unreadyReasons;
        }
    }

    /**
     * Add the given unready reason, replacing any existing reasons of the same type
     */
    public void addUnreadyReason(UnreadyReason newReason) {
        addUnreadyReason(newReason.getClass(), newReason);
    }

    /**
     * Atomically replace all unready reasons of the given type with one other reason
     */
    public void addUnreadyReason(Class<? extends UnreadyReason> oldReasonType, UnreadyReason newReason) {
        if(getMatch().isCommitted()) return;

        boolean removed = removeUnreadyReasonsNoUpdate(oldReasonType);
        boolean added = addUnreadyReasonNoUpdate(newReason);
        if(removed || added) update();
    }

    /**
     * Withdraw all unready reasons of the given type
     */
    public void removeUnreadyReason(Class<? extends UnreadyReason> reasonType) {
        if(getMatch().isCommitted()) return;

        if(removeUnreadyReasonsNoUpdate(reasonType)) {
            update();
        }
    }

    private boolean addUnreadyReasonNoUpdate(UnreadyReason reason) {
        if(unreadyReasons.add(reason)) {
            logger.fine("Added " + reason);
            return true;
        }
        return false;
    }

    private boolean removeUnreadyReasonsNoUpdate(Class<? extends UnreadyReason> reasonType) {
        boolean removed = false;
        for(Iterator<UnreadyReason> iterator = unreadyReasons.iterator(); iterator.hasNext(); ) {
            UnreadyReason reason = iterator.next();
            if(reasonType.isInstance(reason)) {
                iterator.remove();
                removed = true;
                logger.fine("Removed " + reason);
            }
        }
        return removed;
    }

    public boolean canStart(boolean force) {
        if(!getMatch().canTransitionTo(MatchState.Running)) return false;
        if(!force && (cc().getCountdown() instanceof RestartListener.RestartCountdown ||
                      cc().getCountdown() instanceof CycleMatchModule.CycleCountdown)) return false;
        for(UnreadyReason reason : unreadyReasons) {
            if(!force || !reason.canForceStart()) return false;
        }
        return true;
    }

    public boolean canHuddle() {
        return getMatch().canBeIn(MatchState.Huddle);
    }

    /**
     * Force the countdown to start with default duration if at all possible
     */
    public boolean forceStartCountdown() {
        return forceStartCountdown(null, null);
    }

    /**
     * Force the countdown to start with the given duration if at all possible
     */
    public boolean forceStartCountdown(@Nullable Duration duration, @Nullable Duration huddle) {
        return canStart(true) && startCountdown(duration, huddle, true);
    }

    public boolean forceHuddleCountdown(@Nullable Duration duration) {
        if(canHuddle()) {
            if(duration == null) duration = config.huddle();
            cc().start(new HuddleCountdown(getMatch()), duration);
            return true;
        }
        return false;
    }

    /**
     * Start the countdown with default duration if auto-start is enabled,
     * and there are no soft {@link UnreadyReason}s preventing it.
     */
    public boolean autoStartCountdown() {
        return isAutoStart() && canStart(false) && startCountdown(null, null, false);
    }

    public boolean restartUnreadyTimeout() {
        if(cc().getCountdown() instanceof UnreadyTimeout) {
            startUnreadyTimeout();
            return true;
        }
        return false;
    }

    private boolean startCountdown(@Nullable Duration duration, @Nullable Duration huddle, boolean force) {
        if(duration == null) duration = config.countdown();
        if(huddle == null) huddle = config.huddle();

        logger.fine("Starting countdown");
        cc().start(new StartCountdown(getMatch(), force, huddle), duration);

        return true;
    }

    private boolean cancelCountdown() {
        if(cc().cancelAll(StartCountdown.class)) {
            logger.fine("Cancelled countdown");
            return true;
        }
        return false;
    }

    private void startUnreadyTimeout() {
        if(cc().getCountdown() == null || cc().getCountdown() instanceof UnreadyTimeout) {
            final Duration duration = config.timeout();
            logger.fine("Starting unready timeout with duration " + duration);
            cc().start(new UnreadyTimeout(getMatch()), duration);
        }
    }

    private void cancelUnreadyTimeout() {
        if(cc().cancelAll(UnreadyTimeout.class)) {
            logger.fine("Cancelled unready timeout");
        }
    }

    private void update() {
        if(getMatch().isCommitted()) return;

        final StartCountdown countdown = cc().getCountdown(StartCountdown.class);
        final boolean ready = canStart(countdown != null && countdown.isForced());
        final boolean empty = getMatch().getPlayers().isEmpty();
        if(countdown == null && ready && isAutoStart()) {
            startCountdown(null, null, false);
        } else if(countdown != null && !ready) {
            cancelCountdown();
        }

        final UnreadyTimeout timeout = cc().getCountdown(UnreadyTimeout.class);
        if(timeout == null) {
            if(!ready && !empty) {
                startUnreadyTimeout();
            }
        } else if(ready || empty) {
            cancelUnreadyTimeout();
        } else {
            bbmm.render(timeout);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinMatchEvent event) {
        if(getMatch().getPlayers().size() == 1) {
            update();
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveMatchEvent event) {
        if(getMatch().getPlayers().isEmpty()) {
            update();
        }
    }
}
