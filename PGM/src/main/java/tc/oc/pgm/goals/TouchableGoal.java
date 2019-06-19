package tc.oc.pgm.goals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalTouchEvent;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;

/**
 * A {@link Goal} that may be 'touched' by players, meaning the player has
 * made some tangible progress in completing the goal.
 */
@ListenerScope(MatchScope.RUNNING)
public abstract class TouchableGoal<T extends ProximityGoalDefinition> extends ProximityGoal<T> implements Listener {

    public static final ChatColor COLOR_TOUCHED = ChatColor.YELLOW;
    public static final String SYMBOL_TOUCHED = "\u2733"; // ✳

    protected boolean touched;
    protected final Set<Competitor> touchingCompetitors = new HashSet<>();
    protected final Set<ParticipantState> touchingPlayers = new HashSet<>();
    protected final Set<ParticipantState> recentTouchingPlayers = new HashSet<>();

    public TouchableGoal(T definition, Match match) {
        super(definition, match);
        match.registerEvents(this);
    }

    /**
     * Should touches NOT be credited until the goal is completed?
     */
    public boolean getDeferTouches() {
        return false;
    }

    /**
     * Gets a formatted message designed to be broadcast when a player touches the goal.
     *
     * @param toucher The player
     * @param self is the message for the toucher?
     */
    public abstract BaseComponent getTouchMessage(@Nullable ParticipantState toucher, boolean self);

    @Override
    public net.md_5.bungee.api.ChatColor renderProximityColor(Competitor team, Party viewer) {
        return hasTouched(team) ? net.md_5.bungee.api.ChatColor.YELLOW : super.renderProximityColor(team, viewer);
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        return shouldShowTouched(competitor, viewer) ? COLOR_TOUCHED
                                                     : super.renderSidebarStatusColor(competitor, viewer);
    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        return shouldShowTouched(competitor, viewer) ? SYMBOL_TOUCHED
                                                     : super.renderSidebarStatusText(competitor, viewer);
    }

    public boolean isTouched() {
        return touched;
    }

    /** Gets whether or not the specified team has touched the goal since the last reset. */
    public boolean hasTouched(Competitor team) {
        return touchingCompetitors.contains(team);
    }

    public boolean hasTouched(ParticipantState player) {
        return touchingPlayers.contains(player);
    }

    public ImmutableSet<ParticipantState> getTouchingPlayers() {
        return ImmutableSet.copyOf(touchingPlayers);
    }

    /** Gets whether or not the specified player has recently (in their current lifetime) touched the goal. */
    public boolean hasTouchedRecently(final ParticipantState player) {
        return recentTouchingPlayers.contains(player);
    }

    /**
     * Gets whether or not the specified player touching the goal has any significance at this moment.
     */
    public boolean canTouch(final ParticipantState player) {
        return canComplete(player.getParty()) &&
               !isCompleted(player.getParty()) &&
               !hasTouchedRecently(player);
    }

    public void touch(final @Nullable ParticipantState toucher) {
        // TODO: support playerless touches (deduce which team to give the touch to based on objective owner etc)
        if(toucher == null) return;

        touched = true;

        GoalTouchEvent event;
        if(toucher == null) {
            event = new GoalTouchEvent(this, getMatch().getClock().now().instant);
        } else {
            if(!canTouch(toucher)) return;

            boolean firstForCompetitor = touchingCompetitors.add(toucher.getParty());
            boolean firstForPlayer = touchingPlayers.add(toucher);
            boolean firstForPlayerLife = recentTouchingPlayers.add(toucher);

            event = new GoalTouchEvent(this,
                                       toucher.getParty(), firstForCompetitor,
                                       toucher, firstForPlayer, firstForPlayerLife,
                                       getMatch().getClock().now().instant);
        }

        getMatch().callEvent(event);
        sendTouchMessage(toucher, !event.getCancelToucherMessage());
        playTouchEffects(toucher);
    }

    public void resetTouches() {
        touched = false;
        touchingCompetitors.clear();
        touchingPlayers.clear();
        recentTouchingPlayers.clear();
    }

    public void resetTouches(Competitor team) {
        if(touchingCompetitors.remove(team)) {
            for(Iterator<ParticipantState> iterator = touchingPlayers.iterator(); iterator.hasNext(); ) {
                if(iterator.next().getParty() == team) iterator.remove();;
            }
            for(Iterator<ParticipantState> iterator = recentTouchingPlayers.iterator(); iterator.hasNext(); ) {
                if(iterator.next().getParty() == team) iterator.remove();;
            }
        }
    }

    @Override
    public @Nullable ProximityMetric getProximityMetric(Competitor team) {
        if(hasTouched(team)) {
            return getDefinition().getPostTouchMetric();
        } else {
            return super.getProximityMetric(team);
        }
    }

    public boolean showEnemyTouches() {
        return false;
    }

    public boolean shouldShowTouched(@Nullable Competitor team, Party viewer) {
        return team != null &&
               !isCompleted(team) &&
               hasTouched(team) &&
               (team == viewer || showEnemyTouches() || viewer.isObservingType() || getMatch().isFinished());
    }

    protected void sendTouchMessage(@Nullable ParticipantState toucher, boolean includeToucher) {
        if(!isVisible()) return;

        BaseComponent message = getTouchMessage(toucher, false);
        ComponentRenderers.send(Bukkit.getConsoleSender(), message);

        if(!showEnemyTouches()) {
            message = new Component(toucher.getParty().getChatPrefix(), message);
        }

        for(MatchPlayer viewer : getMatch().getPlayers()) {
            if(shouldShowTouched(toucher.getParty(), viewer.getParty()) && (toucher == null || !toucher.isPlayer(viewer))) {
                viewer.sendMessage(message);
            }
        }

        if(toucher != null) {
            if(includeToucher) {
                toucher.sendMessage(getTouchMessage(toucher, true));
            }

            if(getDeferTouches()) {
                toucher.sendMessage(new TranslatableComponent("match.touch.destroyable.deferredNotice"));
            }
        }
    }

    protected void playTouchEffects(@Nullable ParticipantState toucher) {
        if(toucher == null || !isVisible()) return;

        MatchPlayer onlineToucher = toucher.getMatchPlayer();
        if(onlineToucher == null) return;

        onlineToucher.playSparks();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(ParticipantDespawnEvent event) {
        ParticipantState victim = event.getPlayer().getParticipantState();
        if(victim != null) recentTouchingPlayers.remove(victim);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCompetitorRemove(CompetitorRemoveEvent event) {
        resetTouches(event.getCompetitor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onComplete(GoalCompleteEvent event) {
        if(this == event.getGoal()) {
            resetTouches();
        }
    }

    @Override
    public MatchDoc.TouchableGoal getDocument() {
        return new Document();
    }

    public class Document extends OwnedGoal.Document implements MatchDoc.TouchableGoal {
        @Override
        public Collection<? extends MatchDoc.TouchableGoal.Proximity> proximities() {
            return Collections2.transform(
                getMatch().getCompetitors(),
                new Function<Competitor, MatchDoc.TouchableGoal.Proximity>() {
                    @Override
                    public MatchDoc.TouchableGoal.Proximity apply(Competitor competitor) {
                        return new Proximity(competitor);
                    }
                }
            );
        }

        public class Proximity implements MatchDoc.TouchableGoal.Proximity {
            private final Competitor competitor;

            @Override
            public String _id() {
                return competitor.getId();
            }

            public Proximity(Competitor competitor) {
                this.competitor = competitor;
            }

            @Override
            public boolean touched() {
                return hasTouched(competitor);
            }

            @Override
            public Metric metric() {
                final ProximityMetric metric = getProximityMetric(competitor);
                return metric == null ? null : metric.apiValue();
            }

            @Override
            public double distance() {
                return getMinimumDistance(competitor);
            }
        }
    }
}
