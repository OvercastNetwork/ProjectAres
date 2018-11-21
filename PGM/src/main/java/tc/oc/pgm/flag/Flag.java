package tc.oc.pgm.flag;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.util.BlockVector;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.item.BooleanItemTag;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.bukkit.util.Materials;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.bukkit.util.materials.Banners;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Lazy;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerLeavePartyEvent;
import tc.oc.pgm.filters.query.ILocationQuery;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.fireworks.FireworkUtil;
import tc.oc.pgm.flag.event.FlagCaptureEvent;
import tc.oc.pgm.flag.event.FlagStateChangeEvent;
import tc.oc.pgm.flag.state.BaseState;
import tc.oc.pgm.flag.state.Captured;
import tc.oc.pgm.flag.state.Completed;
import tc.oc.pgm.flag.state.Returned;
import tc.oc.pgm.flag.state.Spawned;
import tc.oc.pgm.flag.state.State;
import tc.oc.pgm.goals.TouchableGoal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.points.AngleProvider;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.StaticAngleProvider;
import tc.oc.pgm.regions.PointRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

@ListenerScope(MatchScope.LOADED)
public class Flag extends TouchableGoal<FlagDefinition> implements Listener {

    public static final String RESPAWNING_SYMBOL = "\u2690"; // ⚐
    public static final String RETURNED_SYMBOL = "\u2691";  // ⚑
    public static final String DROPPED_SYMBOL = "\u2691";  // ⚑
    public static final String CARRIED_SYMBOL = "\u2794";  // ➔

    public static final BukkitSound PICKUP_SOUND_OWN = new BukkitSound(Sound.ENTITY_WITHER_AMBIENT, 0.7f, 1.2f);
    public static final BukkitSound DROP_SOUND_OWN = new BukkitSound(Sound.ENTITY_WITHER_HURT, 0.7f, 1);
    public static final BukkitSound RETURN_SOUND_OWN = new BukkitSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.1f, 1.2f);

    public static final BukkitSound PICKUP_SOUND = new BukkitSound(Sound.ENTITY_FIREWORK_LARGE_BLAST_FAR, 1f, 0.7f);
    public static final BukkitSound DROP_SOUND = new BukkitSound(Sound.ENTITY_FIREWORK_TWINKLE_FAR, 1f, 1f);
    public static final BukkitSound RETURN_SOUND = new BukkitSound(Sound.ENTITY_FIREWORK_TWINKLE_FAR, 1f, 1f);

    private static final BooleanItemTag FLAG_ITEM = new BooleanItemTag("flag", false);

    private final ImmutableSet<Net> nets;
    private final @Nullable Team owner;

    private final Lazy<Set<Team>> capturers;
    private final Lazy<Set<Team>> controllers;
    private final Lazy<Set<Team>> completers;

    private BaseState state;
    private boolean transitioning;

    private final BannerInfo bannerInfo;
    private static class BannerInfo {
        final Location location;
        final BannerMeta meta;
        final ItemStack item;
        final AngleProvider yawProvider;

        private BannerInfo(Location location, BannerMeta meta, ItemStack item, AngleProvider yawProvider) {
            this.location = location;
            this.meta = meta;
            FLAG_ITEM.set(this.meta, true);
            this.item = item;
            this.yawProvider = yawProvider;
        }
    }

    protected Flag(Match match, FlagDefinition definition, ImmutableSet<Net> nets) throws ModuleLoadException {
        super(definition, match);
        this.nets = nets;

        final TeamMatchModule tmm = match.getMatchModule(TeamMatchModule.class);

        this.owner = definition.owner()
                               .map(def -> tmm.team(def)) // Do not use a method ref here, it will NPE if tmm is null
                               .orElse(null);

        this.capturers = Lazy.from(
            () -> Optionals.stream(match.module(TeamMatchModule.class))
                           .flatMap(TeamMatchModule::teams)
                           .filter(team -> getDefinition().canPickup(team) && canCapture(team))
                           .collect(Collectors.toSet())
        );

        this.controllers = Lazy.from(
            () -> nets.stream()
                      .flatMap(net -> Optionals.stream(net.returnPost()
                                                          .flatMap(Post::owner)))
                      .map(def -> tmm.team(def))
                      .collect(Collectors.toSet())
        );

        this.completers = Lazy.from(
            () -> nets.stream()
                      .flatMap(net -> Optionals.stream(net.returnPost()))
                      .filter(Post::isPermanent)
                      .flatMap(post -> Optionals.stream(post.owner()))
                      .map(def -> tmm.team(def))
                      .collect(Collectors.toSet())
        );

        Banner banner = null;
        pointLoop: for(PointProvider returnPoint : definition.getDefaultPost().getReturnPoints()) {
            Region region = returnPoint.getRegion();
            if(region instanceof PointRegion) {
                // Do not require PointRegions to be at the exact center of the block.
                // It might make sense to just override PointRegion.getBlockVectors() to
                // always do this, but it does technically violate the contract of that method.
                banner = toBanner(((PointRegion) region).getPosition().toLocation(match.getWorld()).getBlock());
                if(banner != null) break pointLoop;
            } else {
                for(BlockVector pos : returnPoint.getRegion().getBlockVectors()) {
                    banner = toBanner(pos.toLocation(match.getWorld()).getBlock());
                    if(banner != null) break pointLoop;
                }
            }
        }

        if(banner == null) {
            throw new ModuleLoadException("Flag '" + getName() + "' must have a banner at its default post");
        }

        final Location location = Banners.getLocationWithYaw(banner);
        bannerInfo = new BannerInfo(location,
                                    Banners.getItemMeta(banner),
                                    new ItemStack(Material.BANNER),
                                    new StaticAngleProvider(location.getYaw()));
        bannerInfo.item.setItemMeta(bannerInfo.meta);

        match.registerEvents(this);

        this.state = new Returned(this, this.getDefinition().getDefaultPost(), bannerInfo.location);
        this.state.enterState();
    }

    private static Banner toBanner(Block block) {
        if(block == null) return null;
        BlockState state = block.getState();
        return state instanceof Banner ? (Banner) state : null;
    }

    @Override
    public String toString() {
        return "Flag{name=" + this.getName() + " state=" + this.state + "}";
    }

    public DyeColor getDyeColor() {
        DyeColor color = this.getDefinition().getColor();
        if(color == null) color = bannerInfo.meta.getBaseColor();
        return color;
    }

    public net.md_5.bungee.api.ChatColor getChatColor() {
        return BukkitUtils.toChatColor(this.getDyeColor());
    }

    public String getColoredName() {
        return this.getChatColor() + this.getName();
    }

    public Component getComponentName() {
        return new Component(getName()).color(getChatColor());
    }

    public ImmutableSet<Net> getNets() {
        return nets;
    }

    public BannerMeta getBannerMeta() {
        return bannerInfo.meta;
    }

    public ItemStack getBannerItem() {
        return bannerInfo.item;
    }

    public State state() {
        return state;
    }

    /**
     * Owner is defined in XML, and does not change during a match
     */
    public @Nullable Team getOwner() {
        return owner;
    }

    /**
     * Physical location of the flag, if any
     */
    public Optional<Location> getLocation() {
        return state instanceof Spawned ? Optional.of(((Spawned) state).getLocation())
                                        : Optional.empty();
    }

    /**
     * Controller is the owner of the {@link Post} the flag is at, which obviously can change
     */
    public @Nullable Team getController() {
        return this.state.getController();
    }

    public boolean hasMultipleControllers() {
        return !controllers.get().isEmpty();
    }

    public boolean canDropOn(BlockState base) {
        return Materials.isColliding(base.getType()) || (getDefinition().canDropOnWater() && Materials.isWater(base.getType()));
    }

    public boolean canDropAt(Location location) {
        if(!match.getWorld().equals(location.getWorld())) return false;

        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        if(!canDropOn(below.getState())) return false;
        if(block.getRelative(BlockFace.UP).getType() != Material.AIR) return false;

        switch(block.getType()) {
            case AIR:
            case LONG_GRASS:
                return true;
            default:
                return false;
        }
    }

    public boolean canDrop(ILocationQuery query) {
        return canDropAt(query.getLocation()) &&
               getDefinition().getDropFilter().query(query).isAllowed();
    }

    public Location getReturnPoint(Post post) {
        return post.getReturnPoint(this, bannerInfo.yawProvider).clone();
    }


    // Touchable

    @Override
    public boolean canTouch(ParticipantState player) {
        MatchPlayer matchPlayer = player.getMatchPlayer();
        return matchPlayer != null && canPickup(matchPlayer, state.getPost());
    }

    @Override
    public boolean showEnemyTouches() {
        return true;
    }

    @Override
    public BaseComponent getTouchMessage(ParticipantState toucher, boolean self) {
        if(self) {
            return new TranslatableComponent("match.flag.pickup.you", getComponentName());
        } else {
            return new TranslatableComponent("match.flag.pickup", getComponentName(), toucher.getStyledName(NameStyle.COLOR));
        }
    }


    // Proximity

    @Override
    public Iterable<Location> getProximityLocations(ParticipantState player) {
        return state.getProximityLocations(player);
    }

    @Override
    public boolean isProximityRelevant(Competitor team) {
        if(hasTouched(team)) {
            return canCapture(team);
        } else {
            return canPickup(team);
        }
    }


    // Misc

    /**
     * Transition to the given state. This happens immediately if not already transitioning.
     * If this is called from within a transition, the state is queued and the transition
     * happens after the current one completes. This allows {@link BaseState#enterState} to
     * immediately transition into another state without nesting the transitions, and keeps
     * the events in the correct order.
     */
    public void transition(BaseState newState) {
        if(this.transitioning) {
            throw new IllegalStateException("Nested flag state transition");
        }

        BaseState oldState = this.state;
        try {
            logger.fine("Transitioning " + getName() + " from " + oldState + " to " + newState);

            this.transitioning = true;
            this.state.leaveState();
            this.state = newState;
            this.state.enterState();
        } finally {
            this.transitioning = false;
        }

        getMatch().callEvent(new FlagStateChangeEvent(this, oldState, this.state));

        // If we are still in the state we just transitioned into, start the countdown, if any.
        // We check this because the FlagStateChangeEvent may have already transitioned into another state.
        if(this.state == newState) {
            this.state.startCountdown();
        }

        // Check again, in case startCountdown transitioned. In that case, the nested
        // transition will have already called these events if necessary.
        if(this.state == newState) {
            getMatch().callEvent(new GoalStatusChangeEvent(this));
            if(isCompleted()) {
                getMatch().callEvent(new GoalCompleteEvent(this,
                                                           true,
                                                           c -> false,
                                                           c -> c.equals(getController())));
            }
        }
    }

    public boolean canPickup(IQuery query, Post post) {
        return getDefinition().getPickupFilter().query(query).isAllowed() &&
               post.getPickupFilter().query(query).isAllowed();
    }

    public boolean canPickup(IQuery query) {
        return canPickup(query, state.getPost());
    }

    public boolean canCapture(IQuery query, Net net) {
        return getDefinition().getCaptureFilter().query(query).isAllowed() &&
               net.getCaptureFilter().query(query).isAllowed();
    }

    public boolean canCapture(IQuery query) {
        return getDefinition().canCapture(query, getNets());
    }

    public boolean isCurrent(Class<? extends State> state) {
        return state.isInstance(this.state);
    }

    public boolean isCurrent(State state) {
        return this.state == state;
    }

    public boolean isCarrying(ParticipantState player) {
        MatchPlayer matchPlayer = player.getMatchPlayer();
        return matchPlayer != null && isCarrying(matchPlayer);
    }

    public boolean isCarrying(MatchPlayer player) {
        return this.state.isCarrying(player);
    }

    public boolean isCarrying(Competitor party) {
        return this.state.isCarrying(party);
    }

    public boolean isAtPost(Post post) {
        return this.state.isAtPost(post);
    }

    public boolean isCompletable() {
        return !completers.get().isEmpty();
    }

    @Override
    public boolean canComplete(Competitor team) {
        return team instanceof Team && capturers.get().contains(team);
    }

    @Override
    public boolean isShared() {
        // Flag is shared if it has multiple capturers or no capturers
        return capturers.get().size() != 1;
    }

    @Override
    public boolean isCompleted() {
        return isCurrent(Completed.class);
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return isCompleted() && getController() == team;
    }

    public boolean isCaptured() {
        return isCompleted() || isCurrent(Captured.class);
    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        return this.state.getStatusText(viewer);
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        return this.state.getStatusColor(viewer);
    }

    @Override
    public ChatColor renderSidebarLabelColor(@Nullable Competitor competitor, Party viewer) {
        return this.state.getLabelColor(viewer);
    }

    public void playFlareEffect() {
        if(isCurrent(Spawned.class)) {
            Location location = ((Spawned) this.state).getLocation();
            if(location == null) return;
            FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(this.getDyeColor().getColor()).build();
            Firework firework = FireworkUtil.spawnFirework(location, effect, 0);
            NMSHacks.skipFireworksLaunch(firework);
        }
    }

    /**
     * Play one of two status sounds depending on the team of the listener.
     * Owning players hear the first sound, other players hear the second.
     */
    public void playStatusSound(BukkitSound ownerSound, BukkitSound otherSound) {
        for(MatchPlayer listener : getMatch().getPlayers()) {
            if(listener.getParty() != null && (listener.getParty() == this.getOwner() || listener.getParty() == this.getController())) {
                listener.playSound(ownerSound);
            } else {
                listener.playSound(otherSound);
            }
        }
    }

    private boolean isFlagItem(ItemStack item) {
        return FLAG_ITEM.get(item);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isFlagItem);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onGoalChange(GoalEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onFlagStateChange(FlagStateChangeEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onFlagCapture(FlagCaptureEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        if(event.getFrom().getWorld() == event.getTo().getWorld()) { // yes, this can be false
            this.state.onEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerMove(CoarsePlayerMoveEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBlockTransform(BlockTransformEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onItemDrop(PlayerDropItemEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerDespawn(ParticipantDespawnEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerDespawn(PlayerLeavePartyEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onInventoryClick(InventoryClickEvent event) {
        this.state.onEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onProjectileHit(EntityDamageEvent event) {
        this.state.onEvent(event);
    }
}
