package tc.oc.pgm.spawns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInitialSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jdom2.Element;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.commons.core.util.ThrowingConsumer;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.spawns.states.Joining;
import tc.oc.pgm.spawns.states.Observing;
import tc.oc.pgm.spawns.states.State;
import tc.oc.pgm.xml.InvalidXMLException;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;
import static tc.oc.commons.core.util.MapUtils.ifPresent;

@ListenerScope(MatchScope.LOADED)
public class SpawnMatchModule extends MatchModule implements Listener {

    private final SpawnModule module;
    private final Map<MatchPlayer, State> states = new HashMap<>();
    private final ListMultimap<MatchPlayer, State> transitions = ArrayListMultimap.create();
    private final Set<MatchPlayer> processing = new HashSet<>();

    private final Map<Competitor, Spawn> uniqueSpawns = new HashMap<>();
    private final Set<Spawn> failedSpawns = new HashSet<>();

    @Inject private ObserverToolFactory observerToolFactory;

    public SpawnMatchModule(Match match, SpawnModule module) {
        super(match);
        this.module = module;
    }

    public RespawnOptions getRespawnOptions(IQuery query) {
        return module.respawnOptions.stream().filter(respawnOption -> respawnOption.filter.query(query).equals(Filter.QueryResponse.ALLOW))
                .findFirst().orElseThrow(() -> new IllegalStateException("No respawn option could be used"));
    }

    public Spawn getDefaultSpawn() {
        return module.defaultSpawn;
    }

    public List<Spawn> getSpawns() {
        return module.spawns;
    }

    public List<Kit> getPlayerKits() {
        return module.playerKits;
    }

    public ObserverToolFactory getObserverToolFactory() {
        return observerToolFactory;
    }

    /**
     * Return all {@link Spawn}s that the given player is currently allowed to spawn at
     */
    public List<Spawn> getSpawns(MatchPlayer player) {
        List<Spawn> result = Lists.newArrayList();
        for(Spawn spawn : this.getSpawns()) {
            if(spawn.allows(player)) {
                result.add(spawn);
            }
        }
        return result;
    }

    /**
     * Return a randomly chosen {@link Spawn} that the given player is currently allowed
     * to spawn at, or null if none are available. If a team is given, assume the player
     * will have switched to that team by the time they spawn.
     */
    public @Nullable Spawn chooseSpawn(MatchPlayer player) {
        Competitor competitor = player.getCompetitor();
        if(player.isObserving()) {
            return getDefaultSpawn();
        } else if(competitor != null && uniqueSpawns.containsKey(competitor)) {
            return uniqueSpawns.get(competitor);
        } else {
            List<Spawn> potential = getSpawns(player);
            potential.removeAll(uniqueSpawns.values());
            if(!potential.isEmpty()) {
                Spawn spawn = RandomUtils.element(match.getRandom(), potential);
                if(spawn.attributes().exclusive) uniqueSpawns.put(competitor, spawn);
                return spawn;
            } else {
                return null;
            }
        }
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        // Copy states so they can transition without concurrent modification
        ImmutableMap.copyOf(states).forEach((player, state) -> {
            state.tick();
            processQueuedTransitions(player);
        });
    }

    public void reportFailedSpawn(Spawn spawn, MatchPlayer player) {
        if(failedSpawns.add(spawn)) {
            Element elSpawn = getMatch().getModuleContext().features().definitionNode(spawn);
            InvalidXMLException ex = new InvalidXMLException("Failed to generate spawn location for " + player.getName(), elSpawn);
            getMatch().getMap().getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void leaveState(MatchPlayer player) {
        final State state = states.get(player);
        if(state != null) {
            logger.fine(player.getName() + " leave " + state);
            state.leaveState();
            states.remove(player);
        }
    }

    private void enterState(MatchPlayer player, State state) {
        logger.fine(player.getName() + " enter " + state);
        states.put(player, state);
        state.enterState();
    }

    private void changeState(MatchPlayer player, @Nullable State state) {
        leaveState(player);
        if(state != null) {
            enterState(player, state);
        }
    }

    private boolean hasQueuedTransitions(MatchPlayer player) {
        return transitions.containsKey(player);
    }

    private void processQueuedTransitions(MatchPlayer player) {
        // Prevent nested processing of the same player
        if(processing.add(player)) {
            try {
                final List<State> queue = transitions.get(player);
                while(!queue.isEmpty()) {
                    changeState(player, queue.remove(0));
                }
            } finally {
                processing.remove(player);
            }
        }
    }

    public void transition(MatchPlayer player, @Nullable State newState) {
        logger.fine(player.getName() + " queue " + newState);
        transitions.put(player, newState);
    }

    private <X extends Throwable> void withState(@Nullable MatchPlayer player, ThrowingConsumer<State, X> consumer) throws X {
        if(player == null) return;
        ifPresent(states, player, rethrowConsumer(consumer::acceptThrows));
    }

    private void withState(@Nullable Entity bukkit, BiConsumer<MatchPlayer, State> consumer) {
        final MatchPlayer player = match.getPlayer(bukkit);
        withState(player, state -> consumer.accept(player, state));
    }

    private void dispatchEvent(@Nullable MatchPlayer player, Consumer<State> consumer) {
        withState(player, state -> {
            consumer.accept(state);
            processQueuedTransitions(player);
        });
    }

    private void dispatchEvent(@Nullable Entity bukkit, BiConsumer<MatchPlayer, State> consumer) {
        withState(bukkit, (player, state) -> {
            consumer.accept(player, state);
            processQueuedTransitions(player);
        });
    }


    // Events delegated to States

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(final PlayerChangePartyEvent event) throws EventException {
        final MatchPlayer player = event.getPlayer();
        if(event.getOldParty() == null) {
            // Join match
            event.yield();
            if(event.getNewParty().isParticipating()) {
                enterState(player, new Joining(player));
            } else {
                enterState(player, new Observing(player, true, true));
            }
        } else if(event.getNewParty() == null) {
            // Leave match
            leaveState(player);
        } else {
            // Party change during match
            withState(player, state -> {
                state.onEvent(event);
                if(hasQueuedTransitions(player)) {
                    // If the party change caused a state transition, leave the old
                    // state before the change, and enter the new state afterward.

                    // The potential danger here is that the player has no spawn state
                    // during the party change, while other events are firing. The
                    // danger is minimized by listening at MONITOR priority.

                    leaveState(player);
                    event.yield();
                    processQueuedTransitions(player);
                }
            });
        }
    }

    /** Must run before {@link tc.oc.pgm.tracker.trackers.DeathTracker#onPlayerDeath} */
    @EventHandler(priority = EventPriority.LOW)
    public void onVanillaDeath(final PlayerDeathEvent event) {
        dispatchEvent(event.getEntity(), (player, state) -> state.onEvent(event));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(final MatchPlayerDeathEvent event) {
        dispatchEvent(event.getVictim(), state -> state.onEvent(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        dispatchEvent(event.getWhoClicked(), (player, state) -> state.onEvent(event));
    }

    // Listen on HIGH so the picker can handle this first
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onObserverInteract(final ObserverInteractEvent event) {
        dispatchEvent(event.getPlayer(), state -> state.onEvent(event));
    }

    @EventHandler
    public void matchBegin(final MatchBeginEvent event) {
        // Copy states so they can transition without concurrent modification
        ImmutableMap.copyOf(states).forEach((player, state) -> {
            state.onEvent(event);
            processQueuedTransitions(player);
        });
    }

    @EventHandler
    public void matchEnd(final MatchEndEvent event) {
        // Copy states so they can transition without concurrent modification
        ImmutableMap.copyOf(states).forEach((player, state) -> {
            // This event can be fired from inside a party change, so some players may have no party
            if(player.hasParty()) {
                state.onEvent(event);
                processQueuedTransitions(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void playerMove(final CoarsePlayerMoveEvent event) {
        dispatchEvent(event.getPlayer(), (player, state) -> state.onEvent(event));
    }


    // Events handled for other reasons

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInitialSpawn(final PlayerInitialSpawnEvent event) {
        // Make all joining players spawn in this match's world
        event.setSpawnLocation(match.getWorld().getSpawnLocation());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerJoin(final PlayerJoinEvent event) {
        // Add the player to the match if they spawn in this world
        if(match.getWorld().equals(event.getPlayer().getLocation().getWorld())) {
            event.getPlayer().setGliding(true); // Fixes client desync if player joins server while gliding
            match.addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(final PlayerQuitEvent event) {
        match.removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCompetitorRemove(CompetitorRemoveEvent event) {
        // If a competitor is no longer valid, free up its provider
        Competitor competitor = event.getCompetitor();
        if(uniqueSpawns.containsKey(competitor)) {
            Spawn spawn = uniqueSpawns.get(competitor);
            // Do not change if persistence is enabled
            if(!spawn.attributes().persistent) {
                uniqueSpawns.remove(competitor);
            }
        }
    }
}
