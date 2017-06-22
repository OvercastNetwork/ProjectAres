package tc.oc.pgm.match;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.bukkit.PlayerSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import java.time.Duration;
import java.time.Instant;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.attribute.AttributeUtils;
import tc.oc.commons.bukkit.chat.BukkitAudiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.Named;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.bukkit.util.PlayerStates;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.ForwardingAudience;
import tc.oc.commons.core.chat.Sound;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.events.PlayerResetEvent;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.kits.WalkSpeedKit;
import tc.oc.pgm.settings.ObserverSetting;
import tc.oc.pgm.settings.Settings;

/**
 * MatchPlayer represents a player who is part of a match.  Note that the
 * MatchPlayer object should only exist as long as the corresponding Match
 * instance exists.
 *
 * MatchPlayer stores all information that is necessary for the core plugin.
 */
public class MatchPlayer extends MatchFacetContext<MatchPlayerFacet> implements InventoryHolder,
                                                                                ForwardingAudience,
                                                                                Named,
                                                                                IPlayerQuery,
                                                                                Listener,
                                                                                Filterable<IPlayerQuery> {

    private static final Sound ERROR_SOUND = new BukkitSound(org.bukkit.Sound.BLOCK_NOTE_BASS, 1f, 0.75f);

    private static final Duration ERROR_SOUND_COOLDOWN = Duration.ofSeconds(10);
    private static final Duration SPARK_SOUND_COOLDOWN = Duration.ofSeconds(1);

    @Inject private BukkitUserStore userStore;
    @Inject private IdentityProvider identityProvider;
    @Inject private Server localServer;
    @Inject private OnlineFriends friendMap;
    @Inject private PlayerStates playerStates;

    @Inject MatchUserContext userContext;

    // per-player stuff
    @Inject private Player bukkit;
    @Inject private PlayerId playerId;

    // per-match stuff
    @Inject private Match match;

    private Logger logger;
    private SettingManager settings;
    private Audience audience;
    @Inject private void init(Loggers loggers, SettingManagerProvider settingManagerProvider, BukkitAudiences audiences, Player player) {
        this.logger = loggers.get(match.getLogger(), getClass(), getName());
        this.settings = settingManagerProvider.getManager(player);
        this.audience = audiences.get(player);
    }

    protected @Nullable Party party;

    protected Instant lastErrorSoundTime;
    protected Instant lastSparkSoundTime;
    protected boolean spawned;
    protected boolean dead;
    protected boolean visible;

    // Save these so toString works after bukkit is nulled
    private String name;
    private UUID uuid;

    @Inject void saveNameAndId(Player bukkit) {
        name = bukkit.getName();
        uuid = bukkit.getUniqueId();
    }

    @Override
    public String toString() {
        return MatchPlayer.class.getSimpleName() +
               "{name=" + getName() +
               " uuid=" + getUniqueId() + "}";
    }

    @Override
    public boolean equals(Object that) {
        return this == that ||
               (that instanceof MatchPlayer &&
                this.match.equals(((MatchPlayer) that).match) &&
                this.getUniqueId().equals(((MatchPlayer) that).getUniqueId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(match, getUniqueId());
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void enableAllThrows() throws Exception {
        super.enableAllThrows();

        match.registerRepeatable(this);
    }

    @Override
    public void disableAllThrows() throws Exception {
        match.unregisterRepeatable(this);

        // Clear these so anything that tries to use this object
        // out of scope will cause a noisy error.
        match = null;
        bukkit = null;

        super.disableAllThrows();
    }

    /**
     * Call this to check if a player is still online.
     *
     * You can't call getBukkit().isOnline(), it will throw an IllegalStateException
     */
    public boolean isOnline() {
        return bukkit != null && bukkit.isOnline();
    }

    public String getName(CommandSender viewer) {
        return getBukkit().getName(viewer);
    }

    public String getName(MatchPlayer viewer) {
        return getName(viewer.getBukkit());
    }

    public String getDisplayName() {
        return getBukkit().getDisplayName();
    }

    public String getDisplayName(CommandSender viewer) {
        return getBukkit().getDisplayName(viewer);
    }

    public String getDisplayName(MatchPlayer viewer) {
        return getDisplayName(viewer.getBukkit());
    }

    public PlayerInventory getInventory() {
        return getBukkit().getInventory();
    }

    @Override
    public World getWorld() {
        return match.getWorld();
    }

    public Match getMatch() {
        return match;
    }

    public Player getBukkit() {
        if(bukkit == null) {
            throw new IllegalStateException("Tried to access unloaded " + this);
        }
        return bukkit;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public User getDocument() {
        return userStore.getUser(getBukkit()); // Must use the real Player, not this
    }

    public MatchUserContext getUserContext() {
        return userContext;
    }

    /**
     * Reverse-chronological list of match commitments within the last 24 hours
     */
    public List<Instant> recentMatchCommitments() {
        final List<Instant> instants = getDocument().recent_match_joins_by_family_id().get(localServer.family());
        return instants == null ? Collections.emptyList() : instants;
    }

    public boolean hasParty() {
        return party != null;
    }

    /**
     * The player's current party.
     */
    public Party getParty() {
        if(!hasParty()) {
            throw new IllegalStateException(getName() + " has no party");
        }
        return party;
    }

    public boolean inParty(Party party) {
        return party.equals(this.party);
    }

    public Optional<Party> partyMaybe() {
        return Optional.ofNullable(party);
    }

    /**
     * Called ONLY by {@link Match}. The match is not necessarily
     * in a consistent state when this method is called, so it should do nothing
     * except update internal data structures. Any other reactions to changing
     * parties should be implemented with an event handler.
     */
    protected @Nullable Party setPartyInternal(@Nullable Party newParty) {
        final Party oldParty = party;
        if(!Objects.equals(oldParty, newParty)) {
            party = newParty;
            playerStates.setParticipating(getBukkit(), competitor().isPresent());
        }
        return oldParty;
    }

    public @Nullable Competitor getCompetitor() {
        return party instanceof Competitor ? (Competitor) party : null;
    }

    public Optional<Competitor> competitor() {
        return party instanceof Competitor ? Optional.of((Competitor) party) : Optional.empty();
    }

    @Override
    public MatchPlayerState playerState() {
        final Identity identity = identityProvider.currentIdentity(getBukkit());
        final Party party = getParty();
        if(party instanceof Competitor) {
            return new ParticipantState(match,
                                        identity,
                                        getUniqueId(),
                                        (Competitor) party,
                                        getEntityLocation());
        } else {
            return new MatchPlayerState(match,
                                        identity,
                                        getUniqueId(),
                                        party,
                                        getEntityLocation());
        }
    }

    @Override
    public Optional<ParticipantState> participantState() {
        return Optionals.cast(playerState(), ParticipantState.class);
    }

    public @Nullable ParticipantState getParticipantState() {
        return participantState().orElse(null);
    }

    @Override
    public Optional<? extends Filterable<? super IPlayerQuery>> filterableParent() {
        return Optional.of(getParty());
    }

    @Override
    public Stream<? extends Filterable<? extends IPlayerQuery>> filterableChildren() {
        return Stream.of();
    }

    @Override
    public Optional<MatchPlayer> onlinePlayer() {
        return Optional.of(this);
    }

    @Override
    public EntityLocation getEntityLocation() {
        return getBukkit().getEntityLocation();
    }

    /**
     * Called when the player is committed to the match.
     * See {@link Match#commit()} for details.
     */
    protected void commit() {
    }

    /**
     * Get the cumulative time the player has participated in this match
     */
    public Duration getCumulativeParticipationTime() {
        return match.getParticipationClock().getCumulativePresence(getPlayerId());
    }

    /**
     * Get the cumulative percentage of the match running time in which the player has participated
     */
    public double getCumulativeParticipationPercent() {
        return match.getParticipationClock().getCumulativePresencePercent(getPlayerId());
    }

    public boolean isParticipatingType() {
        return party != null && party.isParticipatingType();
    }

    public boolean isParticipating() {
        return party != null && party.isParticipating();
    }

    public boolean isObservingType() {
        return party != null && party.isObservingType();
    }

    public boolean isObserving() {
        return isObservingType() && !isSpawned();
    }

    public boolean isCommitted() {
        return isParticipatingType() && match.isCommitted();
    }

    public boolean canInteract() {
        return this.isParticipating() && !this.isDead();
    }

    public void setSpawned(boolean spawned) {
        if(this.spawned != spawned) {
            this.spawned = spawned;
            if(spawned) setDead(false);
        }
    }

    public boolean isSpawned() {
        return spawned;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        if(this.dead != dead) {
            this.dead = dead;
            playerStates.setDead(getBukkit(), dead);
            if(dead) setSpawned(false);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void refreshInteraction() {
        final Player bukkit = getBukkit();
        boolean interact = canInteract();
        if(!interact) bukkit.leaveVehicle();
        bukkit.setAffectsSpawning(interact);
        bukkit.setCollidesWithEntities(interact);
    }

    public void clearInventory() {
        final Player bukkit = getBukkit();
        bukkit.getInventory().clear();
    }

    public void reset() {
        final Player bukkit = getBukkit();
        bukkit.closeInventory();
        clearInventory();
        bukkit.setExhaustion(0);
        bukkit.setFallDistance(0);
        bukkit.setFireTicks(0);
        bukkit.setFoodLevel(20); // full
        bukkit.setMaxHealth(20);
        bukkit.setHealth(bukkit.getMaxHealth());
        bukkit.setAbsorption(0);
        bukkit.setLevel(0);
        bukkit.setExp(0); // clear xp
        bukkit.setSaturation(5); // default
        bukkit.setFastNaturalRegeneration(false);
        bukkit.setSlowNaturalRegeneration(true);
        bukkit.setAllowFlight(false);
        bukkit.setFlying(false);
        bukkit.setSneaking(false);
        bukkit.setSprinting(false);
        bukkit.setFlySpeed(0.1f);
        bukkit.setKnockbackReduction(0);
        bukkit.setWalkSpeed(WalkSpeedKit.BUKKIT_DEFAULT);
        AttributeUtils.removeAllModifiers(bukkit);
        resetPotions();

        // we only reset bed spawn here so people don't have to see annoying messages when they respawn
        bukkit.setBedSpawnLocation(null);

        match.callEvent(new PlayerResetEvent(this));
    }

    public void resetPotions() {
        final Player bukkit = getBukkit();
        for(PotionEffect effect : bukkit.getActivePotionEffects()) {
            if(effect.getType() != null) {
                bukkit.removePotionEffect(effect.getType());
            }
        }
    }

    public boolean canSee(MatchPlayer that) {
        // Dead players are never visible
        if(!that.isVisible()) return false;

        // Spawned players are always visible
        if(that.isSpawned()) return true;

        // Spawned/dead players can only see other spawned players
        if(isSpawned() || isDead()) return false;

        // If both players are observing, decide based on the viewer's setting
        switch(settings.getValue(ObserverSetting.get(), ObserverSetting.Options.class)) {
            case NONE:
                return false;
            case FRIENDS:
                return friendMap.areFriends(getBukkit(), that.getBukkit());
            default:
                return true;
        }
    }

    public void refreshVisibility() {
        final Player bukkit = getBukkit();
        bukkit.showInvisibles(isObserving());

        for(MatchPlayer other : match.getPlayers()) {
            if(canSee(other)) {
                bukkit.showPlayer(other.getBukkit());
            } else {
                bukkit.hidePlayer(other.getBukkit());
            }

            if(other.canSee(this)) {
                other.getBukkit().showPlayer(bukkit);
            } else {
                other.getBukkit().hidePlayer(bukkit);
            }
        }
    }

    public ChatColor getColor() {
        return party == null ? ChatColor.AQUA : party.getColor();
    }

    public String getColoredName() {
        return getColor() + getName();
    }

    public String getColoredName(CommandSender viewer) {
        return getColor() + getName(viewer);
    }

    public String getColoredName(MatchPlayer viewer) {
        return getColor() + getName(viewer.getBukkit());
    }

    public BaseComponent getComponentName() {
        return getStyledName(NameStyle.COLOR);
    }

    @Override
    public PlayerComponent getStyledName(NameStyle style) {
        return new PlayerComponent(identityProvider.currentIdentity(getBukkit()), style);
    }

    @Override
    public Audience audience() {
        return audience;
    }

    @Override
    public void sendWarning(String message, boolean audible) {
        audience().sendWarning(message, audible);
        if(audible) playWarningSound();
    }

    @Override
    public void sendWarning(BaseComponent message, boolean audible) {
        audience().sendWarning(message, audible);
        if(audible) playWarningSound();
    }

    @Override
    public void playSound(Sound sound) {
        this.playSound(sound, getBukkit().getLocation());
    }

    public void sendWarning(BaseComponent message) {
        this.sendWarning(message, false);
    }

    public boolean playWarningSound() {
        Instant now = Instant.now();
        if(this.lastErrorSoundTime == null || this.lastErrorSoundTime.isBefore(now.minus(ERROR_SOUND_COOLDOWN))) {
            this.lastErrorSoundTime = now;
            this.playSound(ERROR_SOUND);
            return true;
        }
        return false;
    }

    public void playSound(String sound, Location location, float volume, float pitch) {
        if(this.getToggle(Settings.SOUNDS)) {
            getBukkit().playSound(location, sound, volume, pitch);
        }
    }

    public void playSound(Sound sound, Location location) {
        if(this.getToggle(Settings.SOUNDS)) {
            this.playSound(sound.name(), location, sound.volume(), sound.pitch());
        }
    }

    public void playSound(org.bukkit.Sound sound, Location location, float volume, float pitch) {
        if(this.getToggle(Settings.SOUNDS)) {
            getBukkit().playSound(location, sound, volume, pitch);
        }
    }

    public void playSound(org.bukkit.Sound sound, float volume, float pitch) {
        this.playSound(sound, getBukkit().getLocation(), volume, pitch);
    }

    public void playSound(org.bukkit.Sound sound, Location location) {
        this.playSound(sound, location, 1, 1);
    }

    public void playSound(org.bukkit.Sound sound) {
        this.playSound(sound, getBukkit().getLocation(), Float.POSITIVE_INFINITY, 1); // +Inf volume means non-local
    }

    public void playSparks() {
        if(this.lastSparkSoundTime == null || this.lastSparkSoundTime.isBefore(Instant.now().minus(SPARK_SOUND_COOLDOWN))) {
            this.playSound(org.bukkit.Sound.ENTITY_FIREWORK_BLAST, 1, 1);
            this.playSound(org.bukkit.Sound.ENTITY_FIREWORK_TWINKLE, 1, 1);
        }
    }

    public boolean getToggle(Setting setting) {
        return PlayerSettings.getManager(getBukkit()).getValue(setting, Boolean.class);
    }

    public void nextTick(MatchScope scope, Runnable task) {
        match.getScheduler(scope).createTask(() -> {
            if(isOnline()) {
                task.run();
            }
        });
    }

    public void nextTick(Runnable task) {
        nextTick(MatchScope.LOADED, task);
    }
}
