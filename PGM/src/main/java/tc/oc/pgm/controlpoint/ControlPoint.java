package tc.oc.pgm.controlpoint;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.controlpoint.events.CapturingTeamChangeEvent;
import tc.oc.pgm.controlpoint.events.CapturingTimeChangeEvent;
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent;
import tc.oc.pgm.goals.IncrementalGoal;
import tc.oc.pgm.goals.SimpleGoal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.utils.Strings;

public class ControlPoint extends SimpleGoal<ControlPointDefinition> implements IncrementalGoal<ControlPointDefinition> {

    public static final ChatColor COLOR_NEUTRAL_TEAM = ChatColor.WHITE;

    public static final String SYMBOL_CP_INCOMPLETE = "\u29be";     // ⦾
    public static final String SYMBOL_CP_COMPLETE = "\u29bf";       // ⦿

    protected final ControlPointPlayerTracker playerTracker;
    protected final ControlPointBlockDisplay blockDisplay;

    protected final Vector centerPoint;

    // This is set false after the first state change if definition.permanent == true
    protected boolean capturable = true;

    // The team that currently owns the point. The goal is completed for this team.
    // If this is null then the point is unowned, either because it is in the
    // neutral state, or because it has no initial owner and has not yet been captured.
    protected Competitor owner = null;

    // The team that will own the CP if the current capture is successful.
    // If this is null then either the point is not being captured or it is
    // being "uncaptured" toward the neutral state.
    protected Competitor capturer = null;

    // Time accumulated towards the next owner change. When this passes timeToCapture,
    // it is reset to zero and the owner changes to the capturer (which may be null,
    // if changing to the neutral state). When this is zero, the capturer is null.
    protected Duration progress = Duration.ZERO;

    public ControlPoint(Match match, ControlPointDefinition definition) {
        super(definition, match);

        if(this.definition.getInitialOwner() != null) {
            this.owner = match.needMatchModule(TeamMatchModule.class).team(this.definition.getInitialOwner());
        }

        this.centerPoint = this.getCaptureRegion().getBounds().center();

        this.playerTracker = new ControlPointPlayerTracker(match, this.getCaptureRegion());

        this.blockDisplay = new ControlPointBlockDisplay(match, this);
    }

    public void registerEvents() {
        this.match.registerEvents(this.playerTracker);
        this.match.registerEvents(this.blockDisplay);

        this.blockDisplay.render();
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this.blockDisplay);
        HandlerList.unregisterAll(this.playerTracker);
    }

    public ControlPointBlockDisplay getBlockDisplay() {
        return blockDisplay;
    }

    public ControlPointPlayerTracker getPlayerTracker() {
        return playerTracker;
    }

    public Region getCaptureRegion() {
        return definition.getCaptureRegion();
    }

    public Duration getTimeToCapture() {
        return definition.getTimeToCapture();
    }

    /**
     * Point that can be used as the location of the ControlPoint
     */
    public Vector getCenterPoint() {
        return centerPoint.clone();
    }

    /**
     * The team that owns (is receiving points from) this ControlPoint,
     * or null if the ControlPoint is unowned.
     */
    public Competitor getOwner() {
        return this.owner;
    }

    /**
     * The team that is "capturing" the ControlPoint. This is the team
     * that the current capturingTime counts towards. The capturingTime
     * goes up whenever this team has the most players on the point,
     * and goes down when any other team has the most players on the point.
     * If capturingTime reaches timeToCapture, this team will take
     * ownership of the point, if they don't own it already. When capturingTime
     * goes below zero, the capturingTeam changes to the team with the most
     * players on the point, and the point becomes unowned.
     */
    public Competitor getCapturer() {
        return this.capturer;
    }

    /**
     * The partial owner of the ControlPoint. The "partial owner" is defined in
     * three scenarios. If the ControlPoint is owned and has a neutral state, the
     * partial owner is the owner of the ControlPoint. If the ControlPoint is in
     * contest, the partial owner is the team that is currently capturing the
     * ControlPoint. Lastly, if the ControlPoint is un-owned and not in contest,
     * the progressingTeam is null.
     *
     * @return The team that should be displayed as having partial ownership of
     *         the point, if any.
     */
    public Competitor getPartialOwner() {
        return this.definition.hasNeutralState() && this.getOwner() != null ? this.getOwner() : this.getCapturer();
    }

    /**
     * Progress towards "capturing" the ControlPoint for the current capturingTeam
     */
    public Duration getProgress() {
        return this.progress;
    }

    /**
     * Progress toward "capturing" the ControlPoint for the current capturingTeam,
     * as a real number from 0 to 1.
     */
    @Override
    public double getCompletion() {
        return (double) this.progress.toMillis() / (double) this.definition.getTimeToCapture().toMillis();
    }

    @Override
    public String renderCompletion() {
        return Strings.progressPercentage(this.getCompletion());
    }

    @Override
    public @Nullable String renderPreciseCompletion() {
        return null;
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        return this.capturer == null ? COLOR_NEUTRAL_TEAM : this.capturer.getColor();
    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        if(Duration.ZERO.equals(this.progress)) {
            return this.owner == null ? SYMBOL_CP_INCOMPLETE : SYMBOL_CP_COMPLETE;
        } else {
            return this.renderCompletion();
        }
    }

    @Override
    public ChatColor renderSidebarLabelColor(@Nullable Competitor competitor, Party viewer) {
        return this.owner == null ? COLOR_NEUTRAL_TEAM : this.owner.getColor();
    }

    /**
     * Ownership of the ControlPoint for a specific team given as a real number from
     * 0 to 1.
     */
    public double getCompletion(Competitor team) {
        if (this.getOwner() == team) {
            return 1 - this.getCompletion();
        } else if (this.getCapturer() == team) {
            return this.getCompletion();
        } else {
            return 0;
        }
    }

    @Override
    public boolean getShowProgress() {
        return this.definition.getShowProgress();
    }

    @Override
    public boolean canComplete(Competitor team) {
        return this.canCapture(team);
    }

    @Override
    public boolean isCompleted() {
        return this.owner != null;
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return this.owner != null && this.owner == team;
    }

    private boolean canCapture(Competitor team) {
        return this.definition.getCaptureFilter() == null ||
               this.definition.getCaptureFilter().query(team).isAllowed();
    }

    private boolean canDominate(MatchPlayer player) {
        return this.definition.getPlayerFilter() == null ||
               this.definition.getPlayerFilter().query(player).isAllowed();
    }

    private Duration calculateDominateTime(int lead, Duration duration) {
        // Don't scale time if only one player is present, don't zero duration if multiplier is zero
        return TimeUtils.multiply(duration, 1 + (lead - 1) * definition.getTimeMultiplier());
    }

    public void tick(Duration duration) {
        this.tickCapture(duration);
        this.tickScore(duration);
    }

    /**
     * Do a scoring cycle on this ControlPoint over the given duration.
     */
    protected void tickScore(Duration duration) {
        if(this.getOwner() != null && this.getDefinition().affectsScore()) {
            ScoreMatchModule scoreMatchModule = this.getMatch().getMatchModule(ScoreMatchModule.class);
            if(scoreMatchModule != null) {
                float seconds = this.getMatch().getLength().getSeconds();
                float initial = this.getDefinition().getPointsPerSecond();
                float growth = this.getDefinition().getPointsGrowth();
                float rate = (float) (initial * Math.pow(2, seconds / growth));
                scoreMatchModule.incrementScore(this.getOwner(), rate * duration.toMillis() / 1000d);
            }
        }
    }

    /**
     * Do a capturing cycle on this ControlPoint over the given duration.
     */
    protected void tickCapture(Duration duration) {
        Map<Competitor, Integer> playerCounts = new DefaultMapAdapter<>(new HashMap<>(), 0);

        // The teams with the most and second-most capturing players on the point, respectively
        Competitor leader = null, runnerUp = null;

        // The total number of players on the point who are allowed to dominate and not on the leading team
        int defenderCount = 0;

        for(MatchPlayer player : this.playerTracker.getPlayersOnPoint()) {
            Competitor team = player.getCompetitor();
            if(this.canDominate(player)) {
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

            switch(this.definition.getCaptureCondition()) {
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
            this.dominateAndFireEvents(leader, calculateDominateTime(lead, duration));
        } else {
            this.dominateAndFireEvents(null, duration);
        }

    }

    /**
     * Do a cycle of domination on this ControlPoint for the given team over the given duration. The team can be null,
     * which means no team is dominating the point, which can cause the state to change in some configurations.
     */
    private void dominateAndFireEvents(@Nullable Competitor dominator, Duration duration) {
        final Duration oldProgress = progress;
        final Competitor oldCapturer = capturer;
        final Competitor oldOwner = owner;

        dominate(dominator, duration);

        if(!Objects.equals(oldCapturer, capturer) || !oldProgress.equals(progress)) {
            match.callEvent(new CapturingTimeChangeEvent(match, this));
            match.callEvent(new GoalStatusChangeEvent(this));
        }

        if(!Objects.equals(oldCapturer, capturer)) {
            match.callEvent(new CapturingTeamChangeEvent(match, this, oldCapturer, capturer));
        }

        if(!Objects.equals(oldOwner, owner)) {
            match.callEvent(new ControllerChangeEvent(match, this, oldOwner, owner));
            match.callEvent(new GoalCompleteEvent(this, owner != null, c -> c.equals(oldOwner), c -> c.equals(owner)));

            ScoreMatchModule smm = this.getMatch().getMatchModule(ScoreMatchModule.class);
            if (smm != null) {
                if (oldOwner != null) smm.incrementScore(oldOwner, getDefinition().getPointsOwned() * -1);
                if (owner != null) smm.incrementScore(owner, getDefinition().getPointsOwned());
            }
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

        if(owner != null && definition.hasNeutralState()) {
            // Point is owned and has a neutral state
            if(Objects.equals(dominator,  owner)) {
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
            if(Objects.equals(dominator, capturer)) {
                // Capturer is making progress
                capture(duration);
            } else if(dominator != null) {
                // Non-capturer is reversing progress
                recover(duration, dominator);
            } else {
                // Point is decaying towards owner or neutral
                decay(duration);
            }
        } else if(dominator != null && !Objects.equals(dominator, owner) && canCapture(dominator)) {
            // Point is not being captured and there is a dominant team that is not the owner, so they start capturing
            capturer = dominator;
            dominate(dominator, duration);
        }
    }

    private @Nullable Duration addCaptureTime(final Duration duration) {
        progress = progress.plus(duration);
        if(Comparables.lessThan(progress, definition.getTimeToCapture())) {
            return null;
        } else {
            final Duration remainder = progress.minus(definition.getTimeToCapture());
            progress = Duration.ZERO;
            return remainder;
        }
    }

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
     * Point is owned, and a non-owner is pushing it towards neutral
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
     * Point is either owned or neutral, and someone is pushing it towards themselves
     */
    private void capture(Duration duration) {
        duration = addCaptureTime(duration);
        if(duration != null) {
            owner = capturer;
            capturer = null;
            if(definition.isPermanent()) {
                // The objective is permanent, so the first capture disables it
                capturable = false;
            }
        }
    }

    /**
     * Point is being pulled back towards its current state
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
     * Point is decaying back towards its current state
     */
    private void decay(Duration duration) {
        duration = TimeUtils.multiply(duration, definition.decayRate());
        duration = subtractCaptureTime(duration);
        if(duration != null) {
            capturer = null;
        }
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
