package tc.oc.pgm.match;

import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import java.time.Duration;
import java.time.Instant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.commons.core.inject.InjectionScopable;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.commons.core.util.PunchClock;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.countdowns.SingleCountdownContext;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.time.TickClock;
import tc.oc.pgm.time.TickTime;

public interface Match extends MultiAudience, IMatchQuery, Filterable<IMatchQuery>, MatchPlayerFinder, InjectionScopable<MatchScoped> {

    /**
     * Unique ID for this match
     */
    String getId();

    /**
     * Readable identifier for this match
     */
    default String getSlug() {
        return createSlug(serialNumber());
    }

    static String createSlug(int serialNumber) {
        return "match-" + serialNumber;
    }

    /**
     * A unique serial number assigned to this match.
     *
     * This will be roughly the number of matches that have loaded since server startup.
     */
    int serialNumber();

    /**
     * URL of the match info page
     */
    URL getUrl();

    @Deprecated // use your own logger
    Logger getLogger();

    @Deprecated // @Inject me
    PGMMap getMap();

    @Deprecated // @Inject me
    default MapInfo getMapInfo() {
        return getMap().getInfo();
    }

    @Deprecated // @Inject me
    Plugin getPlugin();

    @Deprecated // @Inject me
    World getWorld();

    @Deprecated // @Inject me
    Server getServer();

    @Deprecated // @Inject me
    PluginManager getPluginManager();

    @Override
    default Match getMatch() {
        return this;
    }

    boolean isMainThread();

    @Deprecated // @Inject me
    MatchFeatureContext features();

    @Override
    default <T extends Feature<?>> T feature(FeatureFactory<T> factory) {
        return features().get(factory);
    }

    @Override
    default Optional<? extends Filterable<? super IMatchQuery>> filterableParent() {
        return Optional.empty();
    }

    @Override
    default Stream<? extends Filterable<? extends IMatchQuery>> filterableChildren() {
        return parties();
    }

    @Override
    default <R extends Filterable<?>> Stream<? extends R> filterableDescendants(Class<R> type) {
        Stream<R> result = Stream.of();
        if(type.isAssignableFrom(Match.class)) {
            result = Stream.concat(result, Stream.of((R) this));
        }
        if(Party.class.isAssignableFrom(type)) {
            result = Stream.concat(result, Streams.instancesOf(parties(), type));
        }
        if(type.isAssignableFrom(MatchPlayer.class)) {
            result = Stream.concat(result, (Stream<? extends R>) players());
        }
        return result;
    }

    @Deprecated // @Inject me
    MatchScheduler getScheduler(MatchScope scope);

    @Deprecated // @Inject me
    TickClock getClock();

    @Deprecated // @Inject TickClock
    default Instant getInstantNow() {
        return getClock().now().instant;
    }

    /**
     * Return a {@link Entropy} that changes state between ticks,
     * and remains in a constant state for the duration of each tick.
     */
    Entropy entropyForTick();

    @Deprecated // use Entropy
    Random getRandom();

    @Deprecated // Does nothing special, just use EventBus
    void callEvent(Event event);

    /**
     * Register an event {@link Listener} scoped to this match.
     *
     * The listener will only receive events from this match,
     * and it will be automatically unregistered when the match unloads.
     *
     * The {@link MatchScope} associated with the listener determines when
     * event handlers are called. An exception will be thrown if no scope
     * can be derived.
     *
     * @see MatchBinders#matchListener the preferred way to do this
     */
    void registerEvents(Listener listener);

    /**
     * Unregister a {@link Listener} that was previously passed to {@link #registerEvents}.
     */
    void unregisterEvents(Listener listener);

    /**
     * Register any {@link Repeatable} methods found on the given object
     * to be called during this match.
     *
     * The {@link MatchScope} associated with the object/method determines when
     * it is called. An exception will be thrown if no scope can be derived.
     */
    void registerRepeatable(Object object);

    /**
     * Unregister an object that was previously passed to {@link #registerRepeatable}.
     */
    void unregisterRepeatable(Object object);

    /**
     * Register {@link Repeatable} methods on the given object, and also
     * register it for events if it is a {@link Listener}.
     *
     * @see #registerEvents
     * @see #registerRepeatable
     */
    default void registerEventsAndRepeatables(Object thing) {
        registerRepeatable(thing);
        if(thing instanceof Listener) registerEvents((Listener) thing);
    }

    /**
     * Unregister {@link Repeatable} methods on the given object, and also
     * unregister it for events if it is a {@link Listener}.
     *
     * @see #unregisterEvents
     * @see #unregisterRepeatable
     */
    default void unregisterEventsAndRepeatables(Object thing) {
        unregisterRepeatable(thing);
        if(thing instanceof Listener) unregisterEvents((Listener) thing);
    }

    /**
     * Return the {@link MapModuleContext} that was used to load this match.
     *
     * This may not be the current context in the {@link PGMMap} object, if
     * it has just reloaded, for example (that's why it needs to be cached).
     */
    @Deprecated // @Inject me
    MapModuleContext getModuleContext();

    @Deprecated // @Inject me
    FeatureDefinitionContext featureDefinitions();

    @Deprecated // @Inject me
    @Nullable <T extends MatchModule> T getMatchModule(Class<T> matchModuleClass);

    @Deprecated // @Inject me
    boolean hasMatchModule(Class<? extends MatchModule> matchModuleClass);

    @Deprecated // @Inject me
    <T extends MatchModule> T needMatchModule(Class<T> matchModuleClass);

    @Deprecated // @Inject me
    SingleCountdownContext countdowns();

    /**
     * True if this Match is loaded. This is only set true after the entire loading
     * process is complete i.e. all modules are loaded, events are called etc. Likewise,
     * it is set false before the unloading process starts. It is safe to call this method
     * from any thread.
     */
    boolean isLoaded();

    /**
     * The time that this match started loading (it may not have finished yet).
     * This is never null, as it is set immediately on match construction.
     */
    Instant getLoadTime();

    /**
     * True if this match has completely unloaded.
     */
    boolean isUnloaded();

    /**
     * The time that this match started unloading (it may not have finished yet),
     * or null if the match has not started unloading yet.
     */
    @Nullable Instant getUnloadTime();

    /**
     * Load the match
     */
    void load() throws ModuleLoadException;

    /**
     * Unload the match
     */
    void unload();

    /**
     * The time that this match last transitioned into the given state,
     * or null if the match has never been in that state.
     *
     * @see #matchState
     */
    @Nullable Instant getStateChangeTime(MatchState state);

    /**
     * Is this match currently in the given state?
     *
     * @see #matchState
     */
    default boolean inState(MatchState state) {
        return matchState() == state;
    }

    /**
     * Is this match currently in the given scope?
     */
    default boolean inScope(MatchScope scope) {
        switch(scope) {
            case LOADED: return !isUnloaded(); // This scope includes (un)loading
            case RUNNING: return isRunning();
            default: throw new IllegalStateException();
        }
    }

    default boolean isStarting() {
        return inState(MatchState.Starting);
    }

    default boolean isRunning() {
        return inState(MatchState.Running);
    }

    default boolean isFinished() {
        return inState(MatchState.Finished);
    }

    default boolean hasStarted() {
        return inState(MatchState.Running) || inState(MatchState.Finished);
    }

    default boolean canTransitionTo(MatchState state) {
        return matchState().canTransitionTo(state);
    }

    default boolean canBeIn(MatchState state) {
        return inState(state) || canTransitionTo(state);
    }

    /**
     * Is this match in a state where it can be unloaded, without interrupting anything important?
     */
    default boolean canAbort() {
        // Don't allow restart while match is running or starting, unless it's empty.
        switch(matchState()) {
            case Idle:
            case Finished:
                return true;
            default:
                return getParticipatingPlayers().isEmpty();
        }
    }

    /**
     * Transition into the given state
     *
     * @throws IllegalStateException if the transition is invalid
     */
    void transitionTo(MatchState newState);

    default void ensureState(MatchState state) {
        if(!inState(state)) {
            transitionTo(state);
        }
    }

    default void end() {
        transitionTo(MatchState.Finished);
    }

    default void ensureNotRunning() {
        if(isRunning()) {
            transitionTo(MatchState.Finished);
        }
    }

    /**
     * If the match has not started yet, returns null.
     * If the match is running, return the current time.
     * If the match is finished, return the time that it finished.
     */
    default @Nullable Instant getEndTime() {
        if(isFinished()) {
            return getStateChangeTime(MatchState.Finished);
        } else if(this.hasStarted()) {
            return getClock().now().instant;
        } else {
            return null;
        }
    }

    /**
     * If the match has not started, throws {@link IllegalStateException}
     * If the match is running, return the time since it started.
     * If the match is finished, return the total time it ran for.
     */
    default Duration getLength() {
        Instant startTime = getStateChangeTime(MatchState.Running);
        if(startTime == null) {
            throw new IllegalStateException("match has not started yet");
        }
        return Duration.between(startTime, getEndTime());
    }

    /**
     * Get the duration of the match, or zero if the match has not started
     */
    @Override
    default Duration runningTime() {
        Instant startTime = getStateChangeTime(MatchState.Running);
        if(startTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startTime, getEndTime());
    }

    /**
     * The range of player counts this match can support.
     *
     * This is initially zero, and is updated by some modules that load.
     *
     * @see #setPlayerLimits
     */
    Range<Integer> getPlayerLimits();

    default int getMaxPlayers() {
        return getPlayerLimits().upperEndpoint();
    }

    void setPlayerLimits(Range<Integer> limits);

    /**
     * All players currently in this match
     */
    @Override
    default Stream<MatchPlayer> players() {
        return getPlayers().stream();
    }

    /**
     * All players currently in this match
     */
    default Set<MatchPlayer> getPlayers() {
        return playersByEntity().values();
    }

    /**
     * All players currently in this match, by their Bukkit entity
     */
    BiMap<Player, MatchPlayer> playersByEntity();

    /**
     * All players currently in this match, by party type
     */
    SetMultimap<Party.Type, MatchPlayer> playersByType();

    default Set<MatchPlayer> getPlayers(Party.Type type) {
        return playersByType().get(type);
    }

    default Set<MatchPlayer> getObservingPlayers() {
        return playersByType().get(Party.Type.Observing);
    }

    default Set<MatchPlayer> getParticipatingPlayers() {
        return playersByType().get(Party.Type.Participating);
    }

    /**
     * Players who have been in a participating {@link Party} after match commitment.
     *
     * @see #commit
     */
    Set<PlayerId> getPastParticipants();

    default boolean hasEverParticipated(PlayerId playerId) {
        return getPastParticipants().contains(playerId);
    }

    /**
     * The {@link PunchClock} that tracks the cumulative participation times
     * of all players who have ever participated in this match.
     */
    PunchClock<PlayerId> getParticipationClock();

    /**
     * Find a {@link MatchUserContext} for the player with the given {@link UUID}.
     *
     * This can be used to retrieve {@link MatchUserFacet}s.
     */
    Optional<MatchUserContext> userContext(UUID uuid);

    @Override
    default Optional<MatchPlayer> player(UserId userId) {
        return MatchPlayerFinder.super.player(userId);
    }

    @Override
    default Optional<MatchPlayer> participant(UserId userId) {
        return MatchPlayerFinder.super.participant(userId);
    }

    @Override
    default Stream<MatchPlayer> participants() {
        return getParticipatingPlayers().stream();
    }

    @Override
    default Stream<MatchPlayer> observers() {
        return getObservingPlayers().stream();
    }

    @Override
    default @Nullable MatchPlayer getPlayer(@Nullable Player bukkit) {
        return bukkit == null ? null : playersByEntity().get(bukkit);
    }

    /**
     * Add the given {@link Player} to this match, if they are not already in it.
     *
     * The player will be added to the default {@link Party}
     * and teleported to the match {@link World}.
     */
    void addPlayer(Player bukkit);

    default void addAllPlayers(Stream<Player> bukkits) {
        bukkits.forEach(this::addPlayer);
    }

    /**
     * Remove the given player from this match.
     *
     * All match-related state will be torn down, but the player
     * will not be removed from the {@link World}.
     *
     * @throws IllegalArgumentException if the given player is not in this match
     */
    void removePlayer(MatchPlayer player);

    /**
     * Remove the given {@link Player} from this match, if they are currently in it.
     *
     * If the player is the only member of an automatic {@link Party}, then that
     * party is also removed from the match (see {@link Party#isAutomatic()}).
     *
     * @see #removePlayer
     */
    default void removePlayer(Player bukkit) {
        final MatchPlayer player = getPlayer(bukkit);
        if(player != null) {
            removePlayer(player);
        }
    }

    default void removeAllPlayers() {
        while(getPlayers().size() > 0) {
            removePlayer(getPlayers().iterator().next());
        }
    }

    /**
     * Return all {@link Party}s currently in the match
     */
    Set<Party> getParties();

    /**
     * Return all {@link Competitor}s currently in the match
     */
    Set<Competitor> getCompetitors();

    default Stream<Party> parties() {
        return getParties().stream();
    }

    @Override
    default Stream<Competitor> competitors() {
        return getCompetitors().stream();
    }

    /**
     * Return all {@link Competitor}s that the given player has ever been
     * a member of, after the match was committed. This will always be empty
     * before the match is committed.
     *
     * @see #commit
     */
    Set<Competitor> getPastCompetitors(PlayerId playerId);

    /**
     * Return the most recent {@link Competitor} that the given player
     * has been a member of, since the match was committed. Returns null
     * if the player has never competed in this match, or the match has
     * not been committed yet.
     *
     * @see #commit
     */
    default @Nullable Competitor getLastCompetitor(PlayerId playerId) {
        return Iterables.getLast(getPastCompetitors(playerId), null);
    }

    /**
     * Is the given {@link Party} currently in this match?
     */
    boolean hasParty(Party party);

    /**
     * Add the given {@link Party} to this match.
     *
     * The party must be empty of players, and not already in this match.
     */
    void addParty(Party party);

    /**
     * Remove the given {@link Party} from this match.
     *
     * The party must be empty of players, and currently in this match.
     */
    void removeParty(Party party);

    /**
     * Return the default {@link Party} for this match.
     *
     * This is the party that new players are added to automatically.
     */
    Party getDefaultParty();

    /**
     * Add the given {@link MatchPlayer} to the given {@link Party}, after removing
     * them from any current party they are in. If the party is not currently in
     * this match, and it is an automatic party, then the party is also added to the
     * match (see {@link Party#isAutomatic}).
     *
     * This is the ONLY way that external code can change a player's party.
     * Any other methods that appear to do so are meant for internal use only.
     */
    boolean setPlayerParty(MatchPlayer player, Party newParty, boolean force);

    /**
     * Commit the match, if it is not already committed. Commitment is a boolean
     * state that starts false and becomes true at some point before or at match start.
     * The transition only happens once per match, and is irreversible, even if the start
     * countdown is cancelled.
     *
     * The commitment event is when teams are chosen/balanced (depending on settings),
     * and also when players become committed to playing the match, if that is enabled.
     * If mid-match join is disallowed, this is also when that restriction becomes effective.
     *
     * Commitment happens automatically at match start, if this method has not been
     * called before then.
     */
    void commit();

    /**
     * Has this match been committed yet?
     *
     * @see #commit
     */
    default boolean isCommitted() {
        return getCommitTime() != null;
    }

    /**
     * The time that this match was committed, or null if it has not been committed yet.
     *
     * @see #commit
     */
    @Nullable TickTime getCommitTime();

    default void sendMessageExcept(BaseComponent message, MatchPlayer... except) {
        players().filter(player -> !ArrayUtils.contains(except, player))
                 .forEach(player -> player.sendMessage(message));
    }

    default void sendMessageExcept(BaseComponent message, MatchPlayerState... except) {
        players().filter(player -> Stream.of(except).noneMatch(ex -> ex.isPlayer(player)))
                 .forEach(player -> player.sendMessage(message));
    }
}
