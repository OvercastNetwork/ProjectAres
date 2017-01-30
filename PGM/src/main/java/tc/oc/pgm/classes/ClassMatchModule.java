package tc.oc.pgm.classes;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.users.ChangeClassRequest;
import tc.oc.api.users.UserService;
import tc.oc.commons.core.stream.BiStream;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@ListenerScope(MatchScope.LOADED)
public class ClassMatchModule extends MatchModule implements Listener {

    @Inject private BukkitUserStore userStore;
    @Inject private UserService userService;

    private final String category;
    private final Map<String, PlayerClass> classes;
    private final Set<PlayerClass> classesByName;
    private final PlayerClass defaultClass;

    private final Map<UserId, PlayerClass> selectedClasses = Maps.newHashMap();
    private final Map<UserId, PlayerClass> lastPlayedClass = Maps.newHashMap();

    public ClassMatchModule(Match match, String category, Map<String, PlayerClass> classes, PlayerClass defaultClass) {
        super(match);
        this.category = checkNotNull(category, "category");
        this.classes = checkNotNull(classes, "classes");
        this.defaultClass = checkNotNull(defaultClass, "default class");

        this.classesByName = new TreeSet<>(Comparator.comparing(PlayerClass::getName));
        this.classesByName.addAll(this.classes.values());
    }

    /**
     * Gets the set of classes that are present in alphabetical order by name.
     *
     * @return set of classes present
     */
    public Set<PlayerClass> getClasses() {
        return classesByName;
    }

    /**
     * Gets the player class by the given search term.
     *
     * @param search search term
     * @return class where the name exactly matches the given search term
     */
    public @Nullable PlayerClass getPlayerClass(String search) {
        return this.classes.get(search);
    }

    public Optional<PlayerClass> findClass(String search) {
        return MapUtils.value(classes, search);
    }

    /**
     * Gets the class that the given player has chosen to be on next respawn,
     * which is not necessarily the class that they are currently playing as.
     * @param user player to look up
     * @return player's class or the default class if none selected
     */
    public PlayerClass selectedClass(User user) {
        return selectedClasses.computeIfAbsent(user, x ->
            MapUtils.value(user.classes(), category)
                    .flatMap(this::findClass)
                    .orElse(defaultClass)
        );
    }

    /**
     * Get the last class that the given player spawned as.
     */
    public Optional<PlayerClass> lastPlayedClass(UserId userId) {
        return MapUtils.value(lastPlayedClass, userId);
    }

    /**
     * Get the class that given player is currently playing as
     */
    public Optional<PlayerClass> playingClass(MatchPlayer player) {
        return Optional.of(player)
                       .filter(MatchPlayer::isSpawned)
                       .flatMap(mp -> lastPlayedClass(mp.getPlayerId()));
    }

    /**
     * Gets all players who currently have the given class.
     *
     * @param cls class of which to fetch members
     * @return set of players (some of which may be offline) who have the given
     *         class
     */
    public Set<UserId> getClassMembers(PlayerClass cls) {
        return BiStream.from(selectedClasses)
                       .filterValues(cls::equals)
                       .keys()
                       .collect(Collectors.toImmutableSet());
    }

    public Set<UserId> getClassMembers(Optional<PlayerClass> cls) {
        return cls.map(this::getClassMembers)
                  .orElseGet(ImmutableSet::of);
    }

    /**
     * Get whether the given player can change classes.
     *
     * @param userId player to check
     * @return true if the player can change classes, false otherwise
     */
    public boolean getCanChangeClass(UserId userId) {
        PlayerClass cls = this.lastPlayedClass.get(userId);
        return cls == null || !cls.isSticky();
    }

    /**
     * Sets the given player's class to the one indicated.
     *
     * @param userId player to set the class
     * @param newClass class to set
     * @return old class or default if none selected
     *
     * @throws IllegalStateException if the player may not change classes
     */
    public PlayerClass setPlayerClass(UserId userId, PlayerClass newClass) {
        checkNotNull(userId, "player id");
        checkNotNull(newClass, "player class");
        checkArgument(this.classes.containsValue(newClass), "class is not valid for this match");

        if(!this.getCanChangeClass(userId)) {
            throw new IllegalStateException("cannot change sticky class");
        }

        PlayerClass oldClass = this.selectedClasses.put(userId, newClass);
        if(oldClass == null) oldClass = this.defaultClass;

        userService.changeClass(userId, new ChangeClassRequest() {
            @Override public String category() {
                return category;
            }

            @Override public String name() {
                return newClass.getName();
            }
        });

        MatchPlayer matchPlayer = this.match.getPlayer(userId);
        if(matchPlayer != null) {
            this.match.getPluginManager().callEvent(new PlayerClassChangeEvent(this.match, matchPlayer, this.category, oldClass, newClass));
        }

        return oldClass;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(ParticipantSpawnEvent event) {
        this.lastPlayedClass.put(event.getPlayer().getPlayerId(),
                                 selectedClass(event.getPlayer().getDocument()));
    }

    public void giveClassKits(MatchPlayer player) {
        for(Kit kit : selectedClass(player.getDocument()).getKits()) {
            player.facet(KitPlayerFacet.class).applyKit(kit, true);
        }
    }
}
