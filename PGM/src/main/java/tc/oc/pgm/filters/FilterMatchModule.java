package tc.oc.pgm.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.inject.Inject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.MatchScoreChangeEvent;
import tc.oc.pgm.events.MatchStateChangeEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.victory.RankingsChangeEvent;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.flag.event.FlagStateChangeEvent;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

@ListenerScope(MatchScope.LOADED)
public class FilterMatchModule extends MatchModule implements Listener, FilterDispatcher {

    private static class ListenerSet {
        final Set<FilterListener<?>> rise = new HashSet<>();
        final Set<FilterListener<?>> fall = new HashSet<>();
    }

    private final Table<Filter, Class<? extends Filterable>, ListenerSet> listeners = HashBasedTable.create();

    // Most recent responses for each filter with listeners (used to detect changes)
    private final Table<Filter, Filterable<?>, Boolean> lastResponses = HashBasedTable.create();

    // Filterables that need a check in the next tick (cleared every tick)
    private final Set<Filterable<?>> dirtySet = new HashSet<>();

    private <F extends Filterable<?>> void register(Class<F> scope, Filter filter, boolean response, FilterListener<? super F> listener) {
        if(match.isLoaded()) {
            throw new IllegalStateException("Cannot register filter listener after match is loaded");
        }

        final ListenerSet listenerSet = listeners.row(filter)
                                                 .computeIfAbsent(scope, s -> new ListenerSet());

        (response ? listenerSet.rise
                  : listenerSet.fall).add(listener);

        match.filterableDescendants(scope)
             .forEach(filterable -> {
                 final boolean last = lastResponse(filter, filterable);
                 if(last == response) {
                     dispatch(listener, filter, filterable, last);
                 }
             });
    }

    @Override
    public <F extends Filterable<?>> void onChange(Class<F> scope, Filter filter, FilterListener<? super F> listener) {
        logger.fine("onChange scope=" + scope.getSimpleName() + " listener=" + listener + " filter=" + filter);
        register(scope, filter, true, listener);
        register(scope, filter, false, listener);
    }

    @Override
    public void onChange(Filter filter, FilterListener<? super Filterable<?>> listener) {
        onChange((Class) Filterable.class, filter, listener);
    }

    @Override
    public <F extends Filterable<?>> void onRise(Class<F> scope, Filter filter, Consumer<? super F> listener) {
        logger.fine("onRise scope=" + scope.getSimpleName() + " listener=" + listener + " filter=" + filter);
        register(scope, filter, true, (filterable, response) -> listener.accept(filterable));
    }

    @Override
    public void onRise(Filter filter, Consumer<? super Filterable<?>> listener) {
        onRise((Class) Filterable.class, filter, listener);
    }

    @Override
    public <F extends Filterable<?>> void onFall(Class<F> scope, Filter filter, Consumer<? super F> listener) {
        logger.fine("onFall scope=" + scope.getSimpleName() + " listener=" + listener + " filter=" + filter);
        register(scope, filter, false, (filterable, response) -> listener.accept(filterable));
    }

    @Override
    public void onFall(Filter filter, Consumer<? super Filterable<?>> listener) {
        onFall((Class) Filterable.class, filter, listener);
    }

    private boolean lastResponse(Filter filter, Filterable<?> filterable) {
        return MapUtils.computeIfAbsent(lastResponses.row(filter), filterable, filter::response);
    }

    private <F extends Filterable<?>> void dispatch(FilterListener<? super F> listener, Filter filter, F filterable, boolean response) {
        if(logger.isLoggable(Level.FINER)) {
            logger.finer("Dispatching response=" + response +
                         " listener=" + listener +
                         " filter=" + filter +
                         " filterable=" + filterable);
        }
        listener.filterQueryChanged(filterable, response);
    }

    private <F extends Filterable<?>, Q extends IQuery> void check(F filterable, Q query, List<Runnable> dispatches) {
        final Map<Filter, Boolean> beforeCache = new HashMap<>();
        final Map<Filter, Boolean> afterCache = lastResponses.column(filterable);

        // For each scope that the given filterable applies to
        listeners.columnMap().forEach((scope, column) -> {
            if(scope.isInstance(filterable)) {
                // For each filter in this scope
                column.forEach((filter, filterListeners) -> {
                    final Boolean before;
                    final boolean after;
                    if(beforeCache.containsKey(filter)) {
                        // If the filter has already been checked, we have both responses saved.
                        before = beforeCache.get(filter);
                        after = afterCache.get(filter);
                    } else {
                        // The first time a particular filter is checked, move the old response to
                        // a local temporary cache and save the new response to the permanent cache.
                        before = afterCache.get(filter);
                        beforeCache.put(filter, before);
                        after = filter.response(query);
                        afterCache.put(filter, after);
                    }

                    if(before == null || before != after) {
                        dispatches.add(() -> {
                            (after ? filterListeners.rise
                                   : filterListeners.fall).forEach(listener -> dispatch((FilterListener<? super F>) listener, filter, filterable, after));
                        });
                    }
                });
            }
        });
    }

    private <F extends Filterable<?>, Q extends IQuery> void check(F filterable, Q query) {
        final List<Runnable> dispatches = new ArrayList<>();
        check(filterable, query, dispatches);
        dispatches.forEach(Runnable::run);
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        final Set<Filterable<?>> checked = new HashSet<>();
        for(;;) {
            // Collect Filterables that are dirty, and have not already been checked in this tick
            final Set<Filterable<?>> checking = ImmutableSet.copyOf(Sets.difference(dirtySet, checked));
            if(checking.isEmpty()) break;

            // Remove what we are about to check from the dirty set, and add them to the checked set
            dirtySet.removeAll(checking);
            checked.addAll(checking);

            // Do all the filter checks and collect the notifications in a list to dispatch afterward.
            // This prevents listeners from altering the results of filters for other listeners that
            // were invalidated at the same time.
            final List<Runnable> dispatches = new ArrayList<>();
            checking.forEach(f -> check(f, f, dispatches));

            // The Listeners might invalidate more Filterables, which is why we have to loop around
            // and empty the dirtySet again after this. We keep looping until there is nothing more
            // we can check in this tick. If they invalidate something that has already been checked
            // in this tick, it will remain in the dirtySet until the next tick.
            dispatches.forEach(Runnable::run);
        }
    }

    public void invalidate(Filterable<?> filterable) {
        if(dirtySet.add(filterable)) {
            filterable.filterableChildren().forEach(this::invalidate);
        }
    }

    /**
     * TODO: optimize using the filter parameter
     */
    public void invalidate(Filter filter, Filterable<?> filterable) {
        invalidate(filterable);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(CoarsePlayerMoveEvent event) {
        // On movement events, check the player immediately instead of invalidating them.
        // We can't wait until the end of the tick because the player could move several
        // more times by then (i.e. if we received multiple packets from them in the same
        // tick) which would make region checks highly unreliable.
        match.player(event.getPlayer()).ifPresent(player -> {
            invalidate(player);
            match.getServer().postToMainThread(match.getPlugin(), true, this::tick);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(MatchPlayerDeathEvent event) {
        invalidate(event.getVictim());
        event.onlineKiller().ifPresent(this::invalidate);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(PlayerChangePartyEvent event) throws EventException {
        if(event.newParty().isPresent()) {
            invalidate(event.getPlayer());
        } else {
            // Before a player leaves, force all filters false that are not already false.
            // So, all dynamic player filters are effectively wrapped in "___ and online",
            // and listeners don't need to do any cleanup as long as they don't hold on to
            // players that don't match the filter.
            listeners.columnMap().forEach((scope, column) -> {
                if(scope.isInstance(event.getPlayer())) {
                    // For each filter in this scope
                    column.forEach((filter, filterListeners) -> {
                        // If player joined very recently, they may not have a cached response yet
                        final Boolean response = lastResponses.get(filter, event.getPlayer());
                        if(response != null && response) {
                            filterListeners.fall.forEach(listener -> dispatch((FilterListener<? super MatchPlayer>) listener, filter, event.getPlayer(), false));
                        }
                    });
                }
            });

            event.yield();

            // Wait until after the event to remove them, in case they get invalidated during the event.
            dirtySet.remove(event.getPlayer());
            lastResponses.columnKeySet().remove(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchStateChange(MatchStateChangeEvent event) {
        invalidate(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGoalComplete(GoalCompleteEvent event) {
        invalidate(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFlagChange(FlagStateChangeEvent event) {
        invalidate(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScoreChange(MatchScoreChangeEvent event) {
        invalidate(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRankingsChange(RankingsChangeEvent event) {
        invalidate(match);
    }
}
