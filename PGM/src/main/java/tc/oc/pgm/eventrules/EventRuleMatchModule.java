package tc.oc.pgm.eventrules;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.event.GeneralizingEvent;
import tc.oc.commons.bukkit.util.BlockStateUtils;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.ParticipantBlockTransformEvent;
import tc.oc.pgm.filters.Filter.QueryResponse;
import tc.oc.pgm.filters.query.BlockEventQuery;
import tc.oc.pgm.filters.query.IBlockQuery;
import tc.oc.pgm.filters.query.IEventQuery;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.filters.query.PlayerBlockEventQuery;
import tc.oc.pgm.flag.event.FlagPickupEvent;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.utils.MatchPlayers;

import static tc.oc.pgm.map.ProtoVersions.REGION_PRIORITY_VERSION;

@ListenerScope(MatchScope.LOADED)
public class EventRuleMatchModule extends MatchModule implements Listener {

    protected final EventRuleContext ruleContext;
    protected final boolean useRegionPriority;

    public EventRuleMatchModule(Match match, EventRuleContext ruleContext) {
        super(match);
        this.ruleContext = ruleContext;
        this.useRegionPriority = this.getMatch().getMapInfo().proto.isNoOlderThan(REGION_PRIORITY_VERSION);
    }

    protected void checkEnterLeave(Event event, MatchPlayer player, Optional<BlockVector> from, BlockVector to) {
        if(player == null || !player.canInteract()) return;

        if(this.useRegionPriority) {
            // We need to handle both scopes in the same loop, because the priority order can interleave them
            for(EventRule rule : this.ruleContext.getAll()) {
                if((rule.scope() == EventRuleScope.PLAYER_ENTER && rule.region().enters(from, to)) ||
                   (rule.scope() == EventRuleScope.PLAYER_LEAVE && rule.region().exits(from, to))) {

                    if(processQuery(event, rule, player)) {
                        break; // Stop after the first non-abstaining filter
                    }
                }
            }
        } else {
            // To preserve legacy behavior exactly, these need to be in seperate loops
            for(EventRule rule : this.ruleContext.get(EventRuleScope.PLAYER_ENTER)) {
                if(rule.region().enters(from, to)) {
                    processQuery(event, rule, player);
                }
            }

            for(EventRule rule : this.ruleContext.get(EventRuleScope.PLAYER_LEAVE)) {
                if(rule.region().exits(from, to)) {
                    processQuery(event, rule, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkEnterLeave(final CoarsePlayerMoveEvent event) {
        this.checkEnterLeave(event, match.getPlayer(event.getPlayer()), Optional.of(event.getBlockFrom().toBlockVector()), event.getBlockTo().toBlockVector());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkFlagPickup(final FlagPickupEvent event) {
        this.checkEnterLeave(event, event.getCarrier(), Optional.empty(), event.getCarrier().getBukkit().getLocation().toBlockVector());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void applyEffects(final CoarsePlayerMoveEvent event) {
        MatchPlayer player = this.match.getPlayer(event.getPlayer());
        if(player == null) return;

        final BlockVector from = event.getBlockFrom().toBlockVector();
        final BlockVector to = event.getBlockTo().toBlockVector();

        for(EventRule rule : this.ruleContext.get(EventRuleScope.EFFECT)) {
            if(rule.velocity() == null && rule.kit() == null) continue;

            boolean enters = rule.region().enters(from, to);
            boolean exits = rule.region().exits(from, to);
            if(!enters && !exits) continue;

            if(!player.canInteract() || rule.filter() == null || rule.filter().query(player) != QueryResponse.DENY) {
                // Note: works on observers
                if(enters && rule.velocity() != null) {
                    event.getPlayer().setVelocity(rule.velocity());
                }

                if(rule.kit() != null && player.canInteract()) {
                    if(enters) {
                        player.facet(KitPlayerFacet.class).applyKit(rule.kit(), false);
                    }

                    if(exits && rule.lendKit()) {
                        rule.kit().remove(player);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkBlockTransform(final BlockTransformEvent event) {
        final BlockVector pos = BlockUtils.center(event.getNewState()).toBlockVector();
        final Optional<ParticipantState> actor = getActor(event);

        BlockState againstBlock = null;
        if(event.getCause() instanceof BlockPlaceEvent) {
            againstBlock = ((BlockPlaceEvent) event.getCause()).getBlockAgainst().getState();
        } else if(event.getCause() instanceof PlayerBucketEmptyEvent) {
            againstBlock = ((PlayerBucketEmptyEvent) event.getCause()).getBlockClicked().getState();
        }

        final IEventQuery breakQuery = PlayerBlockEventQuery.of(event.getOldState(), event, actor);
        final IEventQuery placeQuery = PlayerBlockEventQuery.of(event.getNewState(), event, actor);
        final IEventQuery againstQuery = againstBlock == null ? null : PlayerBlockEventQuery.of(againstBlock, event, actor);

        if(this.useRegionPriority) {
            // Note that the event may be in multiple scopes, which is why they must all be handled in the same pass
            ruleLoop: for(EventRule rule : this.ruleContext.getAll()) {
                switch(rule.scope()) {
                    case BLOCK_BREAK:
                        if(event.isBreak() && rule.region().contains(event.getOldState())) {
                            if(processQuery(rule, breakQuery)) {
                                break ruleLoop;
                            }
                        }
                        break;

                    case BLOCK_PLACE:
                        if(event.isPlace() && rule.region().contains(event.getNewState())) {
                            if(processQuery(rule, placeQuery)) {
                                break ruleLoop;
                            }
                        }
                        break;

                    case BLOCK_PLACE_AGAINST:
                        if(againstQuery != null) {
                            if(rule.region().contains(((IBlockQuery) againstQuery).getBlock())) {
                                if(processQuery(rule, againstQuery)) {
                                    break ruleLoop;
                                }
                            }
                        }
                        break;
                }
            }
        } else {
            // Legacy behavior
            if(event.isPlace()) {
                for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_PLACE)) {
                    if(rule.region().contains(pos)) {
                        processQuery(rule, placeQuery);
                    }
                }
            } else {
                for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_BREAK)) {
                    if(rule.region().contains(pos)) {
                        processQuery(rule, breakQuery);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkBlockPhysics(final BlockPhysicsEvent event) {
        BlockEventQuery query = new BlockEventQuery(event, event.getBlock().getState());
        for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_PHYSICS)) {
            if(rule.region().contains(event.getBlock()) && processQuery(rule, query)) break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkBlockDamage(final BlockDamageEvent event) {
        MatchPlayer player = this.match.getParticipant(event.getPlayer());
        if(player == null) return;

        PlayerBlockEventQuery query = new PlayerBlockEventQuery(player, event, event.getBlock().getState());

        for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_BREAK)) {
            if(rule.earlyWarning() && rule.region().contains(event.getBlock())) {
                if(processQuery(rule, query)) {
                    if(event.isCancelled() && rule.message() != null) {
                        player.sendWarning(rule.message(), true);
                    }
                    if(this.useRegionPriority) {
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkUse(final PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            MatchPlayer player = this.match.getParticipant(event.getPlayer());
            if(player == null) return;

            Block block = event.getClickedBlock();
            if(block == null) return;

            this.handleUse(event, block.getState(), player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkHangingPlace(final HangingPlaceEvent event) {
        this.handleHangingPlace(event, getHangingBlockState(event.getEntity()), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkHangingBreak(final HangingBreakByEntityEvent event) {
        this.handleHangingBreak(event, event.getEntity(), event.getRemover());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkItemFrameItemRemove(EntityDamageByEntityEvent event) {
        // This event is fired when popping an item out of an item frame, without breaking the frame itself
        if(event.getEntity() instanceof ItemFrame && ((ItemFrame) event.getEntity()).getItem() != null) {
            this.handleHangingBreak(event, (Hanging) event.getEntity(), event.getDamager());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkItemFrameRotate(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
            if(itemFrame.getItem() != null) {
                // If frame contains an item, right-click will rotate it, which is handled as a "use" event
                this.handleUse(event, getHangingBlockState(itemFrame), this.match.getParticipant(event.getPlayer()));
            } else if(event.getPlayer().getItemInHand() != null) {
                // If the frame is empty and it's right clicked with an item, this will place the item in the frame,
                // which is handled as a "place" event, with the placed item as the block material
                BlockState blockState = BlockStateUtils.cloneWithMaterial(itemFrame.getLocation().getBlock(),
                                                                          event.getPlayer().getItemInHand().getData());
                this.handleHangingPlace(event, blockState, event.getPlayer());
            }
        }
    }

    private void handleUse(Event event, BlockState blockState, MatchPlayer player) {
        if(!player.canInteract()) return;

        PlayerBlockEventQuery query = new PlayerBlockEventQuery(player, event, blockState);

        for(EventRule rule : this.ruleContext.get(EventRuleScope.USE)) {
            if(rule.region().contains(blockState)) {
                if(processQuery(rule, query)) {
                    if(query.getEvent() instanceof PlayerInteractEvent && ((PlayerInteractEvent) query.getEvent()).isCancelled()) {
                        PlayerInteractEvent pie = (PlayerInteractEvent) query.getEvent();
                        pie.setCancelled(false);
                        pie.setUseItemInHand(Event.Result.ALLOW);
                        pie.setUseInteractedBlock(Event.Result.DENY);

                        if(rule.message() != null) {
                            player.sendWarning(rule.message(), false);
                        }
                    }
                    if(this.useRegionPriority) {
                        break;
                    }
                }
            }
        }
    }

    private void handleHangingPlace(Event event, BlockState blockState, Entity placer) {
        IEventQuery query = makeBlockQuery(event, placer, blockState);

        for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_PLACE)) {
            if(rule.region().contains(blockState)) {
                if(processQuery(rule, query)) {
                    sendCancelMessage(rule, query);
                    if(this.useRegionPriority) break;
                }
            }
        }
    }

    private void handleHangingBreak(Event event, Hanging hanging, Entity breaker) {
        BlockState blockState = getHangingBlockState(hanging);
        if(blockState == null) return;

        IEventQuery query = makeBlockQuery(event, breaker, blockState);

        for(EventRule rule : this.ruleContext.get(EventRuleScope.BLOCK_BREAK)) {
            if(rule.region().contains(blockState)) {
                if(processQuery(rule, query)) {
                    sendCancelMessage(rule, query);
                    if(this.useRegionPriority) break;
                }
            }
        }
    }

    private void sendCancelMessage(EventRule rule, IEventQuery query) {
        if(rule.message() != null &&
           query.getEvent() instanceof Cancellable &&
           ((Cancellable) query.getEvent()).isCancelled() &&
           query instanceof IPlayerQuery) {

            MatchPlayer player = getMatch().getPlayer(((IPlayerQuery) query).getPlayerId());
            if(player != null) player.sendWarning(rule.message(), false);
        }
    }

    private IEventQuery makeBlockQuery(Event event, Entity entity, BlockState block) {
        if(entity instanceof Player) {
            MatchPlayer player = this.match.getPlayer((Player) entity);
            if(MatchPlayers.canInteract(player)) {
                return new PlayerBlockEventQuery(player, event, block);
            }
        }
        return new BlockEventQuery(event, block);
    }

    private Optional<ParticipantState> getActor(BlockTransformEvent event) {
        // Legacy maps assume that all TNT damage is done by "world"
        if(getMatch().getMapInfo().proto.isOlderThan(ProtoVersions.FILTER_OWNED_TNT) &&
           event.getCause() instanceof EntityExplodeEvent) {
            return Optional.empty();
        }

        return Optional.ofNullable(ParticipantBlockTransformEvent.getPlayerState(event));
    }

    private static BlockState getHangingBlockState(Hanging hanging) {
        Block block = hanging.getLocation().getBlock();
        Material type = getHangingType(hanging);
        return type == null ? null : BlockStateUtils.cloneWithMaterial(block, type);
    }

    private static Material getHangingType(Hanging hanging) {
        if(hanging instanceof Painting) {
            return Material.PAINTING;
        } else if(hanging instanceof ItemFrame) {
            return Material.ITEM_FRAME;
        } else if(hanging instanceof LeashHitch) {
            return Material.LEASH;
        } else {
            return null;
        }
    }

    protected static boolean processQuery(EventRule rule, IEventQuery query) {
        return processQuery(query.getEvent(), rule, query);
    }

    /**
     * Query the rule's filter with the given objects.
     * If the query is denied, cancel the event and set the deny message.
     * If the query is allowed, un-cancel the event.
     * If the query abstains, do nothing.
     * @return false if the query abstained, otherwise true
     */
    protected static boolean processQuery(Event event, EventRule rule, IQuery query) {
        if(rule.filter() == null) {
            return false;
        }

        switch(rule.filter().query(query)) {
            case ALLOW:
                if(event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(false);
                }
                return true;

            case DENY:
                if(event instanceof GeneralizingEvent) {
                    ((GeneralizingEvent) event).setCancelled(true, rule.message());
                } else if(event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                }
                return true;

            default:
                return false;
        }
    }
}
