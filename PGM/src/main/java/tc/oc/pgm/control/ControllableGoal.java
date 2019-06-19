package tc.oc.pgm.control;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.collection.WeakHashSet;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.control.events.ControllableOwnerChangeEvent;
import tc.oc.pgm.control.events.ControllableTeamChangeEvent;
import tc.oc.pgm.control.events.ControllableTimeChangeEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.goals.IncrementalGoal;
import tc.oc.pgm.goals.SimpleGoal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.utils.MatchPlayers;
import tc.oc.pgm.utils.Strings;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ListenerScope(MatchScope.LOADED)
public abstract class ControllableGoal<T extends ControllableGoalDefinition> extends SimpleGoal<T> implements IncrementalGoal<T>, Listener {

    /**
     * Set to false if the goal is defined as permanent.
     */
    protected boolean capturable;

    /**
     * The team that currently owns the goal. The goal is completed for this team.
     * If this is null then the goal is unowned, either because it is in the
     * neutral state, or because it has no initial owner and has not yet been captured.
     */
    protected Competitor owner;

    /**
     * The team that will own the goal if the current capture is successful.
     * If this is null then either the goal is not being captured or it is
     * being "uncaptured" toward the neutral state.
     */
    protected Competitor capturer;

    /**
     * Time accumulated towards the next owner change. When this passes the capture time,
     * it is reset to zero and the owner changes to the capturer (which may be null,
     * if changing to the neutral state). When this is zero, the capturer is null.
     */
    protected Duration progress;

    /**
     * Keeps track of players that are nearby the controllable goal.
     */
    protected Set<MatchPlayer> players;

    /**
     * String symbols to render the completion of the goal in the sidebar.
     * Incomplete symbol is at index 0, complete symbol is at index 1.
     */
    protected String[] symbols;

    public ControllableGoal(T definition, Match match, String... symbols) {
        super(definition, match);
        this.capturable = true;
        this.owner = definition.initialOwner().map(owner -> match.needMatchModule(TeamMatchModule.class).team(owner)).orElse(null);
        this.capturer = null;
        this.progress = Duration.ZERO;
        this.players = new WeakHashSet<>();
        this.symbols = new String[] {"\u29be", "\u29bf"};
        for(int i = 0; symbols.length == 2 && i < 2; i++) {
            if(symbols[i] != null) {
                this.symbols[i] = symbols[i];
            }
        }
    }

    /**
     * Get an approximate region of where the capture/control region of the goal is.
     */
    public abstract Region region();

    /**
     * Should the controllable goal track the given player at the given location.
     */
    protected abstract boolean tracking(MatchPlayer player, Location location);

    /**
     * Display progress of the controllable goal in the physical world.
     */
    protected abstract void displayProgress(Competitor controlling, Competitor capturing, double progress);

    /**
     * Reset progress of the controllable goal in the physical world to a new owning team.
     */
    protected abstract void displaySet(Competitor owner);

    /**
     * The team that owns (is receiving points from) this goal,
     * or null if the goal is unowned.
     */
    public Competitor owner() {
        return owner;
    }

    /**
     * The team that is "capturing" the goal. This is the team
     * that the current progress counts towards. The progress
     * goes up whenever this team has the most players on the goal,
     * and goes down when any other team has the most players on the goal.
     * If progress reaches captureTime, this team will take ownership of the goal,
     * if they don't own it already. When progress goes below zero, the capturing team
     * changes to the team with the most players on the goal, and the goal becomes unowned.
     */
    public Competitor capturer() {
        return capturer;
    }

    /**
     * The partial owner of the goal is defined in three scenarios:
     *  1. If the goal is owned and has a neutral state, the partial owner is the owner of the goal.
     *  2. If the goal is in contest, the partial owner is the team that is currently capturing the goal.
     *  3. If the goal is un-owned and not in contest, the progressingTeam is null.
     */
    public Competitor partialOwner() {
        return definition.neutralState() && owner != null ? owner() : capturer();
    }

    /**
     * Progress towards "capturing" the goal for the current capturing team.
     */
    public Duration getProgress() {
        return progress;
    }

    @Override
    public double getCompletion() {
        return (double) progress.toMillis() / (double) definition.captureTime().toMillis();
    }

    public double getCompletion(Competitor team) {
        if(Objects.equals(owner, team)) {
            return 1 - getCompletion();
        } else if(Objects.equals(capturer, team)) {
            return getCompletion();
        } else {
            return 0;
        }
    }

    @Override
    public String renderCompletion() {
        return Strings.progressPercentage(getCompletion());
    }

    @Override
    public @Nullable String renderPreciseCompletion() {
        return null;
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        return capturer != null ? capturer.getColor() : ChatColor.WHITE;
    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        if(Duration.ZERO.equals(progress)) {
            return symbols[owner != null ? 1 : 0];
        } else {
            return renderCompletion();
        }
    }

    @Override
    public ChatColor renderSidebarLabelColor(@Nullable Competitor competitor, Party viewer) {
        return owner != null ? owner.getColor() : ChatColor.WHITE;
    }

    @Override
    public boolean getShowProgress() {
        return definition.showProgress();
    }

    @Override
    public boolean canComplete(Competitor team) {
        return canCapture(team);
    }

    @Override
    public boolean isCompleted() {
        return owner != null;
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return isCompleted() && owner.equals(team);
    }

    private boolean canCapture(Competitor team) {
        Filter filter = definition.captureFilter();
        return filter == null || filter.query(team).isAllowed();
    }

    private boolean canDominate(MatchPlayer player) {
        Filter filter = definition.defendFilter();
        return filter == null || filter.query(player).isAllowed();
    }

    private Duration calculateDominateTime(int lead, Duration duration) {
        return TimeUtils.multiply(duration, 1 + (lead - 1) * definition.multiplierTime());
    }

    public void tick(Duration duration) {
        tickCapture(duration);
        tickScore(duration);
    }

    protected void tickScore(Duration duration) {
        if(definition.affectsScore() && isCompleted()) {
            match.module(ScoreMatchModule.class).ifPresent(scores -> {
                float seconds = match.getLength().getSeconds();
                float initial = definition.pointsPerSecond();
                float growth = definition.pointsGrowth();
                float rate = (float) (initial * Math.pow(2, seconds / growth));
                scores.incrementScore(owner, rate * duration.toMillis() / 1000d);
            });
        }
    }

    protected void tickCapture(Duration duration) {
        Map<Competitor, Integer> playerCounts = new DefaultMapAdapter<>(new HashMap<>(), 0);
        Competitor leader = null, runnerUp = null;
        int defenderCount = 0;
        for(MatchPlayer player : ImmutableSet.copyOf(players)) {
            Competitor team = player.getCompetitor();
            if(canDominate(player)) {
                defenderCount++;
                int playerCount = playerCounts.get(team) + 1;
                playerCounts.put(team, playerCount);
                if(team != leader) {
                    if(leader == null || playerCount > playerCounts.get(leader)) {
                        runnerUp = leader;
                        leader = team;
                    } else if(team != runnerUp && (runnerUp == null || playerCount > playerCounts.get(runnerUp))) {
                        runnerUp = team;
                    }
                }
            }
        }
        int lead = 0;
        if(leader != null) {
            lead = playerCounts.get(leader);
            defenderCount -= lead;
            switch(definition.captureCondition()) {
                case EXCLUSIVE:
                    if(defenderCount > 0) {
                        lead = 0;
                    }
                    break;
                case MAJORITY:
                    lead = Math.max(0, lead - defenderCount);
                    break;
                case LEAD:
                    if(runnerUp != null) {
                        lead -= playerCounts.get(runnerUp);
                    }
                    break;
            }
        }
        if(lead > 0) {
            dominateAndFireEvents(leader, calculateDominateTime(lead, duration));
        } else {
            dominateAndFireEvents(null, duration);
        }
    }

    /**
     * Cycle of domination on this goal for the given team over the duration.
     * The team can be null, which means no team is dominating the goal,
     * which can cause the state to change in some configurations.
     */
    private void dominateAndFireEvents(@Nullable Competitor dominator, Duration duration) {
        final Duration oldProgress = progress;
        final Competitor oldCapturer = capturer, oldOwner = owner;
        dominate(dominator, duration);
        if(!Objects.equals(oldCapturer, capturer) || !oldProgress.equals(progress)) {
            displayProgress(owner, capturer, getCompletion());
            match.callEvent(new ControllableTimeChangeEvent(match, this));
            match.callEvent(new GoalStatusChangeEvent(this));
        }
        if(!Objects.equals(oldCapturer, capturer)) {
            match.callEvent(new ControllableTeamChangeEvent(match, this, oldCapturer, capturer));
        }
        if(!Objects.equals(oldOwner, owner)) {
            displaySet(owner);
            match.callEvent(new ControllableOwnerChangeEvent(match, this, oldOwner, owner));
            match.callEvent(new GoalCompleteEvent(this, owner != null, c -> c.equals(oldOwner), c -> c.equals(owner)));
            match.module(ScoreMatchModule.class).ifPresent(scores -> {
                if(oldOwner != null) {
                    scores.incrementScore(oldOwner, definition.pointsOwned() * -1);
                }
                if(owner != null) {
                    scores.incrementScore(owner, definition.pointsOwned());
                }
            });
        }
    }

    /**
     * If there is a neutral state, then the point cannot be owned and captured
     * at the same time. This means that at least one of controllingTeam or capturingTeam
     * must be null at any particular time.
     *
     * If controllingTeam is non-null, the point is owned, and it must be "uncaptured"
     * before any other team can capture it. In this state, capturingTeam is null,
     * the controlling team will decrease capturingTimeMillis, and all other teams will
     * increase it.
     *
     * If controllingTeam is null, then the point is in the neutral state. If capturingTeam
     * is also null, then the point is not being captured, and capturingTimeMillis is
     * zero. If capturingTeam is non-null, then that is the only team that will increase
     * capturingTimeMillis. All other teams will decrease it.
     *
     * If there is no neutral state, then the point is always either being captured
     * by a specific team, or not being captured at all.
     *
     * If incremental capturing is disabled, then capturingTimeMillis is reset to
     * zero whenever it stops increasing.
     */
    private void dominate(@Nullable Competitor dominator, Duration duration) {
        if(!capturable || Comparables.lessOrEqual(duration, Duration.ZERO)) {
            return;
        }
        if(owner != null && definition.neutralState()) {
            // Point is owned and has a neutral state
            if(Objects.equals(owner, dominator)) {
                // Owner is recovering the point
                recover(duration, dominator);
            } else if(dominator != null) {
                // Non-owner is uncapturing the point
                uncapture(duration, dominator);
            } else {
                // Point is decaying towards the owner
                decay(duration);
            }
        } else if(capturer != null) {
            // Point is partly captured by someone
            if(Objects.equals(capturer, dominator)) {
                // Capturer is making progress
                capture(duration);
            } else if(dominator != null) {
                // Non-capturer is reversing progress
                recover(duration, dominator);
            } else {
                // Point is decaying towards owner or neutral
                decay(duration);
            }
        } else if(dominator != null && !Objects.equals(owner, dominator) && canCapture(dominator)) {
            // Point is not being captured and there is a dominant team that is not the owner, so they start capturing
            capturer = dominator;
            dominate(dominator, duration);
        } else if(owner != null && definition.neutralRate() > 0) {
            // Point has an owner and there are no players nearby, so it rolls back to neutral,
            // even if the goal explicitly states it has no neutral state
            if(players.stream().noneMatch(player -> canCapture(player.getCompetitor()))) {
                rollback(duration);
            } else {
                recover(duration, dominator);
            }
        }
    }

    /**
     * The goal is owned, and a non-owner is pushing it towards neutral.
     */
    private void uncapture(Duration duration, Competitor dominator) {
        duration = addCaptureTime(duration);
        if(duration != null) {
            // If uncapture is complete, recurse with the dominant team's remaining time
            owner = null;
            dominate(dominator, duration);
        }
    }

    /**
     * The goal is either owned or neutral, and someone is pushing it towards themselves.
     */
    private void capture(Duration duration) {
        duration = addCaptureTime(duration);
        if(duration != null) {
            owner = capturer;
            capturer = null;
            if(definition.permanent()) {
                // The objective is permanent, so the first capture disables it
                capturable = false;
            }
        }
    }

    /**
     * The goal is being pulled back towards its current state.
     */
    private void recover(Duration duration, Competitor dominator) {
        duration = TimeUtils.multiply(duration, definition.recoveryRate());
        duration = subtractCaptureTime(duration);
        if(duration != null) {
            capturer = null;
            if(!Objects.equals(dominator, owner)) {
                // If the dominant team is not the controller, recurse with the remaining time
                dominate(dominator, TimeUtils.multiply(duration, 1D / definition.recoveryRate()));
            }
        }
    }

    /**
     * The goal is decaying back towards its current state.
     */
    private void decay(Duration duration) {
        duration = TimeUtils.multiply(duration, definition.decayRate());
        duration = subtractCaptureTime(duration);
        if(duration != null) {
            capturer = null;
        }
    }

    /**
     * The goal has no player controlling it and will revert to a neutral state.
     */
    private void rollback(Duration duration) {
        duration = TimeUtils.multiply(duration, 1.0 / definition.neutralRate());
        duration = addCaptureTime(duration);
        if(duration != null) {
            // If uncapture is complete, recurse with the dominant team's remaining time
            owner = null;
        }
    }

    /**
     * Increase the base amount of capture time by a certain amount.
     */
    private @Nullable Duration addCaptureTime(final Duration duration) {
        progress = progress.plus(duration);
        if(Comparables.lessThan(progress, definition.captureTime())) {
            return null;
        } else {
            final Duration remainder = progress.minus(definition.captureTime());
            progress = Duration.ZERO;
            return remainder;
        }
    }

    /**
     * Decrease the base amount of capture time by a certain amount.
     */
    private @Nullable Duration subtractCaptureTime(final Duration duration) {
        if(Comparables.greaterThan(progress, duration)) {
            progress = progress.minus(duration);
            return null;
        } else {
            final Duration remainder = duration.minus(progress);
            progress = Duration.ZERO;
            return remainder;
        }
    }

    /**
     * Reset and show progress of the controllable goal in the physical world for the first time.
     */
    public void display() {
        displaySet(owner);
        displayProgress(owner, capturer, getCompletion());
    }

    /**
     * Update the tracked players that are on the controllable goal
     */
    protected void updatePlayers(Player bukkit, Location to) {
        MatchPlayer player = match.getPlayer(bukkit);
        if(!MatchPlayers.canInteract(player)) return;
        if(!player.getBukkit().isDead() && tracking(player, to)) {
            players.add(player);
        } else {
            players.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(CoarsePlayerMoveEvent event) {
        updatePlayers(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        updatePlayers(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(ParticipantDespawnEvent event) {
        players.remove(event.getPlayer());
    }

    @Override
    public MatchDoc.IncrementalGoal getDocument() {
        return new Document();
    }

    class Document extends SimpleGoal.Document implements MatchDoc.IncrementalGoal {
        @Override
        public double completion() {
            return getCompletion();
        }
    }

}
