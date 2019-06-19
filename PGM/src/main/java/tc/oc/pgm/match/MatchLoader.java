package tc.oc.pgm.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.events.CycleEvent;
import tc.oc.pgm.events.MapArchiveEvent;
import tc.oc.pgm.map.MapNotFoundException;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.terrain.WorldManager;

/**
 * Server-scoped object that creates/cycles/unloads matches
 * and tracks currently loaded matches by world.
 *
 * This class does NOT make any decisions about when to cycle
 * or what maps to use. Some other object does that and calls
 * {@link #cycleTo(Match, PGMMap)} on this class.
 */
@Singleton
public class MatchLoader implements MatchFinder {

    private final Logger log;
    private final Provider<WorldManager> worldManager;
    private final Provider<MatchInjectionScope> matchInjectionScope;
    private final Provider<Match> matchProvider;
    private final MatchCounter matchCounter;
    private final EventBus eventBus;

    /** Matches that are currently running. */
    private final Map<World, Match> matches = new HashMap<World, Match>();

    @Inject MatchLoader(Loggers loggers,
                        Provider<WorldManager> worldManager,
                        Provider<MatchInjectionScope> matchInjectionScope,
                        Provider<Match> matchProvider,
                        MatchCounter matchCounter,
                        EventBus eventBus) {

        this.log = loggers.get(getClass());
        this.worldManager = worldManager;
        this.matchInjectionScope = matchInjectionScope;
        this.matchProvider = matchProvider;
        this.matchCounter = matchCounter;
        this.eventBus = eventBus;
    }

    @Override
    public Map<World, Match> matchesByWorld() {
        return matches;
    }

    /**
     * Force all {@link Match}es to end and unload. This should only be
     * called immediately before the server shuts down as it will leave
     * PGM in a completely dysfunctional state.
     */
    public void unloadAllMatches() {
        for(Match match : ImmutableSet.copyOf(this.matches.values())) {
            unloadMatch(match);
        }
    }

    /**
     * Call {@link #cycleToUnsafe(Match, PGMMap)} and log any exceptions to the map logger.
     * @return the new match, or null if loading failed
     */
    public @Nullable Match cycleTo(@Nullable Match oldMatch, PGMMap newMap) throws MapNotFoundException {
        try {
            return this.cycleToUnsafe(oldMatch, newMap);
        } catch(MapNotFoundException e) {
            throw e;
        } catch(ModuleLoadException e) {
            if(e.module() != null) {
                newMap.getLogger().log(Level.SEVERE, "Exception loading module " + e.module().getSimpleName() + ": " + e.getMessage(), e);
            } else {
                newMap.getLogger().log(Level.SEVERE, "Exception loading map: " + e.getMessage(), e);
            }
        } catch(Throwable e) {
            log.log(Level.SEVERE, e.getClass().getSimpleName() + " cycling to " + newMap.getName(), e);
            newMap.getLogger().log(Level.SEVERE, "Internal error cycling to " + newMap.getName() + " (" + e + ")", e);
        }
        return null;
    }

    /**
     * Creates and loads a new {@link Match} on the given map, optionally unloading an old
     * match and transferring all players to the new one.
     *
     * @param oldMatch      if given, this match is unloaded and all players are transferred to the new match
     * @param newMap        the map to load for the new match
     * @return              the newly loaded {@link Match}
     * @throws Throwable    generally, any exceptions thrown during loading/unloading are propagated
     */
    private Match cycleToUnsafe(@Nullable Match oldMatch, PGMMap newMap) throws Throwable {
        this.log.info("Cycling to " + newMap.toString());

        // End the old match if it's still running
        if(oldMatch != null) oldMatch.ensureNotRunning();

        // Create and load the new match.
        // Starting here, there are two "current" matches. While that is true,
        // we have to explicitly specify a current match during any calls that
        // might try to acquire it, either by calling one of the getCurrentMatch
        // methods in this class, or injecting a @MatchScoped dependency.
        final Match newMatch = loadMatch(newMap);

        if(oldMatch != null) {
            // Build a list of players to move from the old match to the new one.
            // Ensure player is online before moving them. This should clean up
            // any mess caused by a previous failure to remove them.
            final List<Player> players = oldMatch.players()
                                                 .filter(MatchPlayer::isOnline)
                                                 .map(MatchPlayer::getBukkit)
                                                 .collect(Collectors.toCollection(ArrayList::new));

            // Add players to the new match in random order, to avoid any
            // repetition between matches when auto-join is enabled.
            Collections.shuffle(players);

            // Teleport players between worlds
            for(Player player : players) {
                player.teleport(newMatch.getWorld().getSpawnLocation());
                player.setArrowsStuck(0);
            }

            // Remove players from the old match
            oldMatch.asCurrentScope(oldMatch::removeAllPlayers);

            // Add them to the new one
            newMatch.asCurrentScope(() -> newMatch.addAllPlayers(players.stream()));

            // Unload the old match.
            // After this method returns, there is a single "current match",
            // and we don't need to specify it explicitly any more.
            unloadMatch(oldMatch);
        }

        eventBus.callEvent(new CycleEvent(newMatch, oldMatch));
        log.info("Loaded " + newMap.toString());
        return newMatch;
    }

    /**
     * Try to load a match.  This will take care of copying and loading the
     * new world into Bukkit.  May throw an assortment of exceptions if
     * something goes wrong.
     *
     * @param map Map to load.
     */
    private Match loadMatch(PGMMap map) throws Throwable {
        return map.getContext().orElseThrow(MapNotFoundException::new).asCurrentScope(() -> {
            final WorldManager worldManager = this.worldManager.get();
            final String worldName = Match.createSlug(matchCounter.get());
            final World world = worldManager.createWorld(worldName);

            return matchInjectionScope.get().withNewStore(world, () -> {
                final Match match = matchProvider.get();
                this.matches.put(world, match);

                try {
                    match.load();
                    return match;
                } catch(Throwable e) {
                    this.matches.remove(world);
                    worldManager.unloadWorld(world);
                    throw e;
                }
            });
        });
    }

    /**
     * Unload match modules, unload the world, and archive it
     */
    private void unloadMatch(Match match) {
        match.asCurrentScope(() -> {
            match.ensureNotRunning();
            match.removeAllPlayers();
            match.unload();

            final WorldManager worldManager = this.worldManager.get();
            worldManager.unloadWorld(match.getWorld());

            MapArchiveEvent archiveEvent = new MapArchiveEvent(match, null);
            eventBus.callEvent(archiveEvent);
            worldManager.destroyWorld(match.getWorld().getName(), archiveEvent.getOutputDirectory());

            this.matches.remove(match.getWorld());
        });
    }
}
