package tc.oc.pgm.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.pgm.events.CompetitorAddEvent;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalProximityChangeEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.goals.events.GoalTouchEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.victory.VictoryMatchModule;

@ListenerScope(MatchScope.LOADED)
public class GoalMatchModule extends MatchModule implements Listener {

    protected static final BukkitSound GOOD_SOUND = new BukkitSound(Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
    protected static final BukkitSound BAD_SOUND = new BukkitSound(Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);

    protected final List<Goal> goals = new ArrayList<>();
    protected final Multimap<Competitor, Goal> goalsByCompetitor = ArrayListMultimap.create();
    protected final Multimap<Goal, Competitor> competitorsByGoal = HashMultimap.create();
    protected final LoadingCache<Competitor, GoalProgress> progressCache = CacheUtils.newCache(GoalProgress::new);

    @Override
    public void load() {
        super.load();
        match.featureDefinitions()
             .all(new TypeToken<GoalDefinition<?>>(){})
             .map(match.features()::get)
             .forEach(this::addGoal);
    }

    private void addGoal(Goal<?> goal) {
        logger.fine("Adding goal " + goal);

        if(!goal.isVisible()) return;

        if(goals.isEmpty()) {
            logger.fine("First goal added, appending " + GoalsVictoryCondition.class.getSimpleName());
            match.needMatchModule(VictoryMatchModule.class).setVictoryCondition(new GoalsVictoryCondition(goalsByCompetitor));
        }

        goals.add(goal);

        for(Competitor competitor : match.getCompetitors()) {
            addCompetitorGoal(competitor, goal);
        }
    }

    public Collection<Goal> getGoals() {
        return goals;
    }

    public Collection<Goal> getGoals(Competitor competitor) {
        return goalsByCompetitor.get(competitor);
    }

    public Collection<Competitor> getCompetitors(Goal goal) {
        return competitorsByGoal.get(goal);
    }

    public Multimap<Competitor, Goal> getGoalsByCompetitor() {
        return goalsByCompetitor;
    }

    public Multimap<Goal, Competitor> getCompetitorsByGoal() {
        return competitorsByGoal;
    }

    private void addCompetitorGoal(Competitor competitor, Goal<?> goal) {
        if(goal.canComplete(competitor)) {
            logger.fine("Competitor " + competitor + " can complete goal " + goal);

            goalsByCompetitor.put(competitor, goal);
            competitorsByGoal.put(goal, competitor);
        }
    }

    @EventHandler
    public void onCompetitorAdd(CompetitorAddEvent event) {
        logger.fine("Competitor added " + event.getCompetitor());

        for(Goal goal : goals) {
            addCompetitorGoal(event.getCompetitor(), goal);
        }
    }

    @EventHandler
    public void onCompetitorRemove(CompetitorRemoveEvent event) {
        progressCache.invalidate(event.getCompetitor());
        goalsByCompetitor.removeAll(event.getCompetitor());
        competitorsByGoal.entries().removeIf(entry -> entry.getValue().equals(event.getCompetitor()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Goal> Multimap<Competitor, T> getGoals(Class<T> filterClass) {
        Multimap<Competitor, T> filteredGoals = ArrayListMultimap.create();
        for(Entry<Competitor, Goal> entry : this.goalsByCompetitor.entries()) {
            if(filterClass.isInstance(entry.getValue())) {
                filteredGoals.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return filteredGoals;
    }

    public int compareProgress(Competitor a, Competitor b) {
        return progressCache.getUnchecked(a).compareTo(progressCache.getUnchecked(b));
    }

    protected void updateProgress(Goal goal) {
        competitorsByGoal.get(goal).forEach(progressCache::invalidate);
        match.needMatchModule(VictoryMatchModule.class).invalidateCompetitorRanking();
    }

    // TODO: These events will often be fired together.. debounce them somehow?

    @EventHandler
    public void onComplete(GoalCompleteEvent event) {
        updateProgress(event.getGoal());

        // Don't play the objective sound if the match is over, because the win/lose sound will play instead
        if(!match.needMatchModule(VictoryMatchModule.class).checkMatchEnd() && event.getGoal().isVisible()) {
            for(MatchPlayer player : event.getMatch().getPlayers()) {
                if(!(player.getParty() instanceof Competitor)) {
                    player.playSound(GOOD_SOUND);
                } else {
                    final Competitor competitor = (Competitor) player.getParty();
                    player.playSound(event.wasCompletedFor(competitor) && !event.isCompletedFor(competitor) ? BAD_SOUND : GOOD_SOUND);
                }
            }
        }
    }

    @EventHandler
    public void onStatusChange(GoalStatusChangeEvent event) {
        updateProgress(event.getGoal());
    }

    @EventHandler
    public void onProximityChange(GoalProximityChangeEvent event) {
        updateProgress(event.getGoal());
    }

    @EventHandler
    public void onTouch(GoalTouchEvent event) {
        updateProgress(event.getGoal());
    }
}
