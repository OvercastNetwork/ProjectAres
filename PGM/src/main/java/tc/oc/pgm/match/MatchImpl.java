package tc.oc.pgm.match;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.BiMap;
import com.google.common.collect.BoundType;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import java.time.Instant;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.model.IdFactory;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.inject.ChildInjectorFactory;
import tc.oc.commons.core.inject.FacetContext;
import tc.oc.commons.core.inject.InjectionScope;
import tc.oc.commons.core.inject.InjectionStore;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.util.Lazy;
import tc.oc.commons.core.util.LinkedHashMultimap;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.commons.core.util.PunchClock;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.countdowns.SingleCountdownContext;
import tc.oc.pgm.events.CompetitorAddEvent;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchLoadEvent;
import tc.oc.pgm.events.MatchPlayerAddEvent;
import tc.oc.pgm.events.MatchPostCommitEvent;
import tc.oc.pgm.events.MatchPreCommitEvent;
import tc.oc.pgm.events.MatchStateChangeEvent;
import tc.oc.pgm.events.MatchUnloadEvent;
import tc.oc.pgm.events.MatchUserAddEvent;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PartyRemoveEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.events.PlayerJoinMatchEvent;
import tc.oc.pgm.events.PlayerJoinPartyEvent;
import tc.oc.pgm.events.PlayerLeaveMatchEvent;
import tc.oc.pgm.events.PlayerLeavePartyEvent;
import tc.oc.pgm.events.PlayerParticipationStartEvent;
import tc.oc.pgm.events.PlayerParticipationStopEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.ffa.events.MatchResizeEvent;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.inject.ForMatch;
import tc.oc.pgm.match.inject.ForRunningMatch;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.time.TickClock;
import tc.oc.pgm.time.TickTime;
import tc.oc.pgm.time.WorldTickClock;
import tc.oc.pgm.utils.WorldTickRandom;

import static com.google.common.base.Preconditions.*;

public class MatchImpl implements Match {

    private Logger logger;

    @Inject private ChildInjectorFactory<MatchUserContext> userInjectorFactory;

    @Inject private ExceptionHandler exceptionHandler;
    @Inject private OnlinePlayers onlinePlayers;
    @Inject private BukkitUserStore userStore;

    // Random
    @Inject @ForMatch private Random random;
    @Inject private WorldTickRandom worldTickRandom;

    @Inject private Plugin plugin;
    @Inject private PluginManager pluginManager;
    @Inject private Server server;
    @Inject private PGMMap map;
    @Inject private MapModuleContext mapContext;
    @Inject private FeatureDefinitionContext featureDefinitions;
    @Inject private World world;

    @Inject private Audiences audiences;

    // State management
    private final AtomicBoolean unloaded = new AtomicBoolean(true);     // true before loading starts and after unloading finishes
    private final AtomicBoolean loaded = new AtomicBoolean();           // true after loading finishes and before unloading starts

    // Time
    @Inject private WorldTickClock clock;
    private TickTime loadTime;
    private @Nullable TickTime unloadTime;

    // State
    private MatchState state;
    private final Map<MatchState, TickTime> stateTimeChange = Maps.newHashMap();
    private @Nullable TickTime commitTime;

    // Contexts
    @Inject @ForMatch private SingleCountdownContext countdownContext;
    @Inject private MatchFeatureContext matchFeatureContext;
    @Inject @ForMatch Collection<Provider<Listener>> boundListeners;
    @Inject @ForMatch Collection<Provider<Optional<? extends Listener>>> boundOptionalListeners;

    // Player limit
    // TODO: could be provided by JoinHandlers
    private Range<Integer> playerLimits = Range.singleton(0);

    // Parties
    @Inject private Lazy<Observers> observers;
    private final Set<Party> parties = new HashSet<>();
    private final Set<Competitor> competitors = new HashSet<>();
    private final LinkedHashMultimap<PlayerId, Competitor> pastCompetitorsByPlayer = new LinkedHashMultimap<>();

    private final Map<MatchPlayer, Party> partyChanges = new HashMap<>(); // Used to detect re-entrancy of the party change method

    // Players
    final BiMap<UUID, MatchUserContext> users = HashBiMap.create();

    private final BiMap<Player, MatchPlayer> players = HashBiMap.create();
    private final BiMap<Player, MatchPlayer> playersView = Maps.unmodifiableBiMap(players);

    private final SetMultimap<Party.Type, MatchPlayer> playersByType = HashMultimap.create();
    private final SetMultimap<Party.Type, MatchPlayer> playersByTypeView = Multimaps.unmodifiableSetMultimap(playersByType);

    private final Set<PlayerId> pastParticipants = new HashSet<>();
    private final Set<PlayerId> pastParticipantsView = Collections.unmodifiableSet(pastParticipants);
    private final PunchClock<PlayerId> participationClock = new PunchClock<>(this::runningTime);

    // Identity
    @Inject private MatchCounter matchCounter;
    private int serialNumber = -1; // Set after loading is complete

    private String id;
    private URL url;

    // Scoped task schedulers
    @Inject private MatchScheduler scheduler;
    @Inject private @ForRunningMatch MatchScheduler runningScheduler;

    // Scoped event bus
    @Inject private com.google.common.eventbus.EventBus guavaEventBus;
    @Inject private org.bukkit.event.EventBus bukkitEventBus;
    @Inject private MatchEventRegistry matchEventRegistry;

    private final Set<Listener> listeners = new HashSet<>();

    @Inject private MatchModuleContext matchModuleContext;

    @Inject void init(Loggers loggers, IdFactory idFactory, WorldTickClock clock) throws MalformedURLException {
        logger = loggers.get(getClass());
        id = idFactory.newId();
        url = new URL("http", "localhost:3000", "/matches/" + id);
        loadTime = clock.now();
        setState(MatchState.Idle);
    }


    // -------------------
    // ---- Accessors ----
    // -------------------

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{world=" + this.getWorld().getName() + "}";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int serialNumber() {
        checkState(serialNumber >= 0, "Serial number is not available before match has fully loaded");
        return serialNumber;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    @Deprecated // @Inject me
    public PGMMap getMap() {
        return map;
    }

    @Override
    @Deprecated // @Inject me
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    @Deprecated // @Inject me
    public World getWorld() {
        return world;
    }

    @Override
    @Deprecated // @Inject me
    public Server getServer() {
        return server;
    }

    @Override
    public boolean isMainThread() {
        return server.isPrimaryThread();
    }

    @Override
    @Deprecated // @Inject me
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public Stream<Audience> audiences() {
        return Stream.of(audiences.console(), audiences.filter(sender -> player(sender).isPresent()));
    }


    // -----------------------------
    // ---- Utility/Convenience ----
    // -----------------------------

    @Override
    public MatchScheduler getScheduler(MatchScope scope) {
        switch(scope) {
            case LOADED: return scheduler;
            case RUNNING: return runningScheduler;
        }
        throw new IllegalStateException();
    }


    // ---------------------
    // ---- World Clock ----
    // ---------------------

    @Override
    public TickClock getClock() {
        return this.clock;
    }


    // ----------------
    // ---- Random ----
    // ----------------

    @Override
    @Deprecated // use Entropy
    public Random getRandom() {
        return this.random;
    }

    @Override
    public Entropy entropyForTick() {
        return worldTickRandom.entropy();
    }


    // ----------------
    // ---- Events ----
    // ----------------

    @Override
    public void callEvent(Event event) {
        bukkitEventBus.callEvent(event);
    }

    @Override
    public void registerEvents(Listener listener) {
        if(listeners.add(listener)) {
            guavaEventBus.register(listener);
            matchEventRegistry.startListening(this, listener);
        }
    }

    @Override
    public void unregisterEvents(Listener listener) {
        if(listeners.remove(listener)) {
            matchEventRegistry.stopListening(this, listener);
            guavaEventBus.unregister(listener);
        }
    }


    // ---------------------
    // ---- Repeatables ----
    // ---------------------

    @Override
    public void registerRepeatable(Object object) {
        scheduler.registerRepeatables(object);
        runningScheduler.registerRepeatables(object);
    }

    @Override
    public void unregisterRepeatable(Object object) {
        scheduler.unregisterRepeatables(object);
        runningScheduler.unregisterRepeatables(object);
    }


    // -----------------------------------
    // ---- Modules/Features/Contexts ----
    // -----------------------------------

    @Override
    @Deprecated // @Inject me
    public MapModuleContext getModuleContext() {
        return mapContext;
    }

    @Override
    @Deprecated // @Inject me
    public FeatureDefinitionContext featureDefinitions() {
        return featureDefinitions;
    }

    @Override
    @Deprecated // @Inject me
    public @Nullable <T extends MatchModule> T getMatchModule(Class<T> matchModuleClass) {
        return matchModuleContext.getModule(matchModuleClass);
    }

    @Override
    @Deprecated // @Inject me
    public boolean hasMatchModule(Class<? extends MatchModule> matchModuleClass) {
        return getMatchModule(matchModuleClass) != null;
    }

    @Override
    @Deprecated // @Inject me
    public <T extends MatchModule> T needMatchModule(Class<T> matchModuleClass) {
        return matchModuleContext.needModule(matchModuleClass);
    }

    @Deprecated // @Inject me
    public <T extends MatchModule> Optional<T> module(Class<T> moduleType) {
        return Optional.ofNullable(getMatchModule(moduleType));
    }

    @Override
    @Deprecated // @Inject me
    public SingleCountdownContext countdowns() {
        return this.countdownContext;
    }

    @Override
    @Deprecated // @Inject me
    public MatchFeatureContext features() {
        return matchFeatureContext;
    }


    // ---------------------
    // ---- Load/Unload ----
    // ---------------------

    @Override
    public boolean isLoaded() {
        return this.loaded.get();
    }

    @Override
    public Instant getLoadTime() {
        return loadTime.instant;
    }

    @Override
    public boolean isUnloaded() {
        return this.unloaded.get();
    }

    @Override
    public @Nullable Instant getUnloadTime() {
        return unloadTime == null ? null : unloadTime.instant;
    }

    @Override
    public InjectionStore<MatchScoped> injectionStore() {
        return matchModuleContext.injectionStore();
    }

    @Override
    public InjectionScope<MatchScoped> injectionScope() {
        return matchModuleContext.injectionScope();
    }

    @Override
    public void load() throws ModuleLoadException {
        try {
            unloaded.set(false);

            matchModuleContext.load();
            if(!matchModuleContext.getErrors().isEmpty()) {
                // If loading fails, rethrow the first exception, which should be the only one
                throw matchModuleContext.getErrors().iterator().next();
            }

            featureDefinitions().all().forEach(definition -> definition.load(this));

            boundListeners.forEach(listener -> registerEventsAndRepeatables(listener.get()));
            boundOptionalListeners.forEach(listener -> listener.get().ifPresent(this::registerEventsAndRepeatables));

            scheduler.start();
            addParty(observers.get());
            callEvent(new MatchLoadEvent(this));

            loaded.set(true);
            serialNumber = matchCounter.getAndIncrement();

        } catch(Throwable e) {
            unload();
            throw e;
        }
    }

    @Override
    public void unload() {
        checkState(this.getPlayers().isEmpty(), "cannot unload a match with players");

        boolean wasLoaded = this.loaded.get();
        this.loaded.set(false);
        this.unloadTime = getClock().now();

        if(wasLoaded) {
            this.getPluginManager().callEvent(new MatchUnloadEvent(this));
        }

        users.values().forEach(FacetContext::disableAll);

        if(parties.contains(observers.get())) {
            removeParty(observers.get());
        }

        runningScheduler.cancel();
        scheduler.cancel();

        this.countdownContext.cancelAll();

        for(MatchModule matchModule : this.matchModuleContext.loadedModules()) {
            try {
                matchModule.unload();
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Exception unloading " + matchModule, e);
            }
        }

        Streams.copyOf(listeners)
               .forEach(this::unregisterEvents);

        unloaded.set(true);
    }

    // ----------------
    // ---- States ----
    // ----------------

    @Override
    public @Nullable TickTime getCommitTime() {
        return commitTime;
    }

    @Override
    public void commit() {
        if(!isCommitted()) {
            callEvent(new MatchPreCommitEvent(this));

            commitTime = getClock().now();

            for(MatchPlayer player : getParticipatingPlayers()) {
                pastParticipants.add(player.getPlayerId());
                pastCompetitorsByPlayer.put(player.getPlayerId(), player.getCompetitor());
                player.commit();
            }

            for(Competitor competitor : getCompetitors()) {
                competitor.commit();
            }

            callEvent(new MatchPostCommitEvent(this));
        }
    }

    @Override
    public MatchState matchState() {
        return this.state;
    }

    private MatchState setState(MatchState newState) {
        final MatchState oldState = state;
        state = newState;
        logger.info("Transitioning " + oldState + " -> " + newState);
        stateTimeChange.put(state, clock.now());
        return oldState;
    }

    @Override
    public void transitionTo(MatchState newState) {
        if(!canTransitionTo(newState)) {
            throw new IllegalStateException("Cannot transition from " + state + " to " + newState);
        }

        if(newState == MatchState.Huddle || newState == MatchState.Running) {
            commit();
        }

        final MatchState oldState = setState(newState);

        switch(newState) {
            case Running:
                onStart(oldState);
                break;

            case Finished:
                onEnd();
                break;

            default:
                callEvent(new MatchStateChangeEvent(this, oldState, newState));
                break;
        }
    }

    protected void onStart(MatchState oldState) {
        for(MatchModule matchModule : this.matchModuleContext.loadedModules()) {
            matchModule.enable();
        }

        runningScheduler.start();

        callEvent(new MatchBeginEvent(this, oldState));
        refreshPlayers();
    }


    // ----------------
    // ---- Ending ----
    // ----------------

    protected void onEnd() {
        runningScheduler.cancel();
        this.countdowns().cancelAll();

        this.callEvent(new MatchEndEvent(this));

        for(MatchModule matchModule : this.matchModuleContext.loadedModules()) {
            matchModule.disable();
        }

        this.refreshPlayers();
    }


    // ---------------
    // ---- Times ----
    // ---------------

    @Override
    public @Nullable Instant getStateChangeTime(MatchState state) {
        TickTime time = stateTimeChange.get(state);
        return time == null ? null : time.instant;
    }

    @Override
    public PunchClock<PlayerId> getParticipationClock() {
        return participationClock;
    }


    // -----------------
    // ---- Players ----
    // -----------------

    @Override
    public Range<Integer> getPlayerLimits() {
        return playerLimits;
    }

    @Override
    public void setPlayerLimits(Range<Integer> limits) {
        if(!playerLimits.equals(limits)) {
            checkArgument(limits.lowerBoundType() == BoundType.CLOSED);
            checkArgument(limits.upperBoundType() == BoundType.CLOSED);
            playerLimits = limits;
            callEvent(new MatchResizeEvent(this));
        }
    }

    @Override
    public Optional<MatchUserContext> userContext(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    @Override
    public Set<PlayerId> getPastParticipants() {
        return pastParticipantsView;
    }

    @Override
    public BiMap<Player, MatchPlayer> playersByEntity() {
        return playersView;
    }

    @Override
    public SetMultimap<Party.Type, MatchPlayer> playersByType() {
        return playersByTypeView;
    }

    @Override
    public @Nullable MatchPlayer getPlayer(@Nullable UUID uuid) {
        return uuid == null ? null : getPlayer(onlinePlayers.find(uuid));
    }

    @Override
    public @Nullable MatchPlayer getPlayer(@Nullable UserId userId) {
        return userId == null ? null : getPlayer(onlinePlayers.find(userId));
    }

    @Override
    public void addPlayer(Player bukkit) {
        try {
            MapUtils.computeIfAbsent(players, bukkit, () -> {
                final MatchUserContext userContext = MapUtils.computeIfAbsent(users, bukkit.getUniqueId(), () -> {
                    final User user = userStore.getUser(bukkit);
                    logger.fine("Adding user " + user.username());

                    // Create the user's Injector
                    // Get a new context and enable it
                    final MatchUserContext newUserContext = userInjectorFactory.createChildInjector(new MatchUserManifest(user))
                                                                               .getInstance(MatchUserContext.class);
                    newUserContext.enableAll();

                    callEvent(new MatchUserAddEvent(this, user));

                    return newUserContext;
                });

                logger.fine("Adding player " + bukkit);

                // Create the player and initialize facets. At this point, the
                // player has no party and is not in any collections. Facets should
                // be careful not to assume otherwise in their enable/disable
                // methods. If they want the player in a more complete state, they
                // can listen for events that fire later in the join process.
                final MatchPlayer player = userContext.playerInjectorFactory.createChildInjector(new MatchPlayerManifest(bukkit))
                                                                            .getInstance(MatchPlayer.class);
                player.enableAll();

                callEvent(new MatchPlayerAddEvent(this, player));

                // If the player hasn't joined a party by this point, join the default party
                if(!player.hasParty()) {
                    setPlayerParty(player, getDefaultParty(), false);
                }

                return player;
            });
        } catch(Exception e) {
            exceptionHandler.handleException(e);
            bukkit.kickPlayer("Internal error");
        }
    }

    public void removePlayer(MatchPlayer player) {
        checkArgument(playersByEntity().containsValue(player));

        try {
            logger.fine("Removing player " + player);
            setOrClearPlayerParty(player, null, false);

            // As with enable, facets are disabled after the player is removed
            // from their party and all collections.
            player.disableAll();
        } catch(Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    @Override
    public void addAllPlayers(Stream<Player> bukkits) {
        Match.super.addAllPlayers(bukkits);
        refreshPlayers();
    }

    private void refreshPlayers() {
        for(MatchPlayer player : this.players.values()) {
            player.refreshInteraction();
            player.refreshVisibility();
        }
    }


    // -----------------
    // ---- Parties ----
    // -----------------

    @Override
    public Set<Party> getParties() {
        return parties;
    }

    @Override
    public Set<Competitor> getCompetitors() {
        return competitors;
    }

    @Override
    public Set<Competitor> getPastCompetitors(PlayerId playerId) {
        return pastCompetitorsByPlayer.get(playerId);
    }

    @Override
    public boolean hasParty(Party party) {
        return parties.contains(party);
    }

    @Override
    public void addParty(Party party) {
        logger.fine("Adding party " + party);
        checkArgument(equals(party.getMatch()), "Party belongs to a different match");
        checkState(party.getPlayers().isEmpty(), "Party already contains players");
        checkState(!hasParty(party), "Party is already in this match");

        parties.add(party);
        if(party instanceof Competitor) {
            competitors.add((Competitor) party);
        }

        callEvent(party instanceof Competitor ? new CompetitorAddEvent((Competitor) party) : new PartyAddEvent(party));
    }

    @Override
    public void removeParty(Party party) {
        logger.fine("Removing party " + party);

        checkNotNull(party);
        checkState(parties.contains(party), "Party is not in this match");
        checkState(party.getPlayers().isEmpty(), "Party still has players in it");

        callEvent(party instanceof Competitor ? new CompetitorRemoveEvent((Competitor) party) : new PartyRemoveEvent(party));

        if(party instanceof Competitor) competitors.remove(party);
        parties.remove(party);
    }

    @Override
    public Party getDefaultParty() {
        return observers.get();
    }

    @Override
    public boolean setPlayerParty(MatchPlayer player, Party newParty, boolean force) {
        return setOrClearPlayerParty(player, checkNotNull(newParty), force);
    }

    /**
     * Attempt to add the given player to the given party, and return true if successful. This also handles
     * most of the logic for joining and leaving the match. Doing these things simultaneously is what allows
     * their events to be combined, and ensures that everything is in a consistent state at any point where
     * an event is fired.
     *
     *  - If the player is not in the match, they will be added.
     *  - If newParty is not in the match, and it is automatic, it will be added.
     *  - If newParty is null, the player will be removed from the match, and so will their old party if it is automatic and empty.
     *  - If the player is already in newParty, or if the party change is cancelled by {@link PlayerParticipationStopEvent},
     *    none of the above changes will happen, and the method will return false.
     */
    private boolean setOrClearPlayerParty(MatchPlayer player, @Nullable Party newParty, boolean force) {
        final Party oldParty = player.party;

        checkArgument(equals(player.getMatch()), "Player belongs to a different match");
        checkArgument(newParty == null || equals(newParty.getMatch()), "Party belongs to a different match");
        checkState(oldParty == null || players.containsValue(player), "Joining player is already in the match");
        checkState(newParty == null || newParty.isAutomatic() || parties.contains(newParty), "Party is not in this match and cannot be automatically added");

        if(Objects.equals(oldParty, newParty)) return false;

        logger.fine("Moving player from " + oldParty + " to " + newParty);

        try {
            // This method is fairly complex and generates a lot of events, so it's worthwhile
            // to detect nested calls for the same player, which we definitely do not want.
            final Party nested = partyChanges.put(player, newParty);
            if(nested != null) {
                throw new IllegalStateException("Nested party change: " + player + " tried to join " + newParty + " in the middle of joining " + nested);
            }

            if(oldParty instanceof Competitor && !force) {
                final PlayerParticipationStopEvent request = new PlayerParticipationStopEvent(player, (Competitor) oldParty);
                bukkitEventBus.callEvent(request);
                if(request.isCancelled() && newParty != null) { // Can't cancel this if the player is leaving the match
                    return false;
                }
            }

            if(newParty instanceof Competitor) {
                bukkitEventBus.callEvent(new PlayerParticipationStartEvent(player, (Competitor) newParty));
            }

            // Adding the party will fire an event, so do it before any other state changes
            if(newParty != null && newParty.isAutomatic() && !parties.contains(newParty)) {
                addParty(newParty);
            }

            // Fire pre-change events
            if(newParty == null) {
                bukkitEventBus.callEvent(new PlayerLeaveMatchEvent(player, oldParty));
            } else if(oldParty != null) {
                bukkitEventBus.callEvent(new PlayerLeavePartyEvent(player, oldParty, newParty));
            }

            // Fire around-change event
            bukkitEventBus.callEvent(new PlayerChangePartyEvent(player, oldParty, newParty), event -> {
                if(oldParty == null) {
                    // Player is joining the match
                    players.put(player.getBukkit(), player);
                } else {
                    // Player is leaving a party, update the old party's state
                    oldParty.removePlayerInternal(player);
                    playersByType.remove(player.party.getType(), player);

                    if(player.party instanceof Competitor) {
                        getParticipationClock().punchOut(player.getPlayerId());
                    }
                }

                // Update the player's state
                player.setPartyInternal(newParty);

                if(newParty == null) {
                    // Player is leaving the match, remove them before calling events.
                    //
                    // In this case, handlers of the events fired below will get the MatchPlayer
                    // object after it has been removed from the match and invalidated, so they
                    // need to check for this case and be careful. If you need to do access a
                    // MatchPlayer when it leaves the match, listen for PlayerChangePartyEvent
                    // and do your thing before yielding.
                    players.remove(player.getBukkit());
                } else {
                    // Player is joining a party, update the new party's state
                    if(newParty instanceof Competitor) {
                        getParticipationClock().punchIn(player.getPlayerId());

                        if(isCommitted()) {
                            pastCompetitorsByPlayer.force(player.getPlayerId(), (Competitor) newParty);
                            if(pastParticipants.add(player.getPlayerId())) {
                                player.commit();
                            }
                        }
                    }
                    playersByType.put(player.party.getType(), player);
                    newParty.addPlayerInternal(player);
                }
            });

            // Fire post-change events
            if(newParty == null) {
                bukkitEventBus.callEvent(new PlayerPartyChangeEvent(player, oldParty, null));
            } else if(oldParty == null) {
                bukkitEventBus.callEvent(new PlayerJoinMatchEvent(player, newParty));
            } else {
                bukkitEventBus.callEvent(new PlayerJoinPartyEvent(player, oldParty, newParty));
            }

            // Removing the party will fire an event, so do it after all other state changes
            if(oldParty != null && oldParty.isAutomatic() && oldParty.getPlayers().isEmpty()) {
                removeParty(oldParty);
            }

            return true;

        } finally {
            partyChanges.remove(player);
        }
    }
}
