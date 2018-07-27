package tc.oc.pgm.flag.state;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.pgm.events.PlayerLeavePartyEvent;
import tc.oc.pgm.filters.query.PlayerQueryWithLocation;
import tc.oc.pgm.flag.Flag;
import tc.oc.pgm.flag.FlagDefinition;
import tc.oc.pgm.flag.Net;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.flag.event.FlagCaptureEvent;
import tc.oc.pgm.flag.event.FlagStateChangeEvent;
import tc.oc.pgm.goals.events.GoalEvent;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.scoreboard.SidebarMatchModule;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.teams.TeamMatchModule;

/**
 * State of a flag when a player has picked it up and is wearing the banner on their head.
 */
public class Carried extends Spawned implements Missing {

    protected final MatchPlayer carrier;
    protected ItemStack helmetItem;
    protected @Nullable Net deniedByNet;
    protected @Nullable Flag deniedByFlag;
    protected @Nullable BaseComponent lastMessage;

    private static final int DROP_QUEUE_SIZE = 100;
    private Deque<Location> dropLocations = new ArrayDeque<>(DROP_QUEUE_SIZE);

    public Carried(Flag flag, Post post, MatchPlayer carrier, Location dropLocation) {
        super(flag, post);
        this.carrier = carrier;
        this.dropLocations.add(dropLocation); // Need an initial dropLocation in case the carrier never generates ones
    }

    public MatchPlayer getCarrier() {
        return carrier;
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }

    @Override
    public Location getLocation() {
        return this.carrier.getBukkit().getLocation();
    }

    @Override
    public Iterable<Location> getProximityLocations(ParticipantState player) {
        if(isCarrying(player)) {
            return IterableUtils.transfilter(flag.getNets(), (net) -> {
                if(net.getCaptureFilter().query(player).isAllowed()) {
                    return net.getProximityLocation().toLocation(flag.getMatch().getWorld());
                }
                else {
                    return null;
                }
            });
        } else {
            return super.getProximityLocations(player);
        }
    }

    @Override
    public void enterState() {
        super.enterState();

        Kit kit = this.flag.getDefinition().getPickupKit();
        if(kit != null) carrier.facet(KitPlayerFacet.class).applyKit(kit, false);
        kit = this.flag.getDefinition().getCarryKit();
        if(kit != null) carrier.facet(KitPlayerFacet.class).applyKit(kit, false);

        this.helmetItem = this.carrier.getBukkit().getInventory().getHelmet();

        this.carrier.getBukkit().getInventory().setHelmet(this.flag.getBannerItem().clone());

        SidebarMatchModule smm = this.flag.getMatch().getMatchModule(SidebarMatchModule.class);
        if(smm != null) smm.blinkGoal(this.flag, 2, null);
    }

    @Override
    public void leaveState() {
        SidebarMatchModule smm = this.flag.getMatch().getMatchModule(SidebarMatchModule.class);
        if(smm != null) smm.stopBlinkingGoal(this.flag);

        this.carrier.getBukkit().sendMessage(ChatMessageType.ACTION_BAR, Components.blank());

        this.carrier.getInventory().remove(this.flag.getBannerItem());
        this.carrier.getInventory().setHelmet(this.helmetItem);

        Kit kit = this.flag.getDefinition().getDropKit();
        if(kit != null) this.carrier.facet(KitPlayerFacet.class).applyKit(kit, false);
        kit = this.flag.getDefinition().getCarryKit();
        if(kit != null) kit.remove(this.carrier);

        super.leaveState();
    }

    protected Competitor getBeneficiary(TeamFactory owner) {
        if(owner != null) {
            return this.flag.getMatch().needMatchModule(TeamMatchModule.class).team(owner);
        } else {
            return this.carrier.getCompetitor();
        }
    }

    protected BaseComponent getMessage() {
        BaseComponent message;
        if(this.deniedByNet == null) {
            if(this.flag.getDefinition().getCarryMessage() != null) {
                message = this.flag.getDefinition().getCarryMessage();
            } else {
                message = new TranslatableComponent("match.flag.carrying.you", this.flag.getComponentName());
            }

            message.setColor(ChatColor.AQUA);
            message.setBold(true);
            return message;
        } else {
            if(this.deniedByNet.getDenyMessage() != null) {
                message = this.deniedByNet.getDenyMessage();
            } else if(this.deniedByFlag != null){
                message = new TranslatableComponent("match.flag.captureDenied.byFlag",
                                                    this.flag.getComponentName(),
                                                    this.deniedByFlag.getComponentName());
            } else {
                message = new TranslatableComponent("match.flag.captureDenied",
                                                    this.flag.getComponentName());
            }

            message.setColor(ChatColor.RED);
            message.setBold(true);
            return message;
        }
    }

    @Override
    public void tickRunning() {
        super.tickRunning();
        BaseComponent message = this.getMessage();

        if (MinecraftVersion.atLeast(MinecraftVersion.MINECRAFT_1_8, this.carrier.getBukkit().getProtocolVersion())) {
            this.carrier.sendHotbarMessage(message);
        }

        if(!Components.equals(message, this.lastMessage)) {
            this.lastMessage = message;
            this.carrier.showTitle(new Component(), message, 0, 5, 35);
        }

        ScoreMatchModule smm = this.flag.getMatch().getMatchModule(ScoreMatchModule.class);
        if(smm != null && this.flag.getDefinition().getPointsPerSecond() > 0) {
            smm.incrementScore(this.getBeneficiary(this.flag.getDefinition().getOwner()),
                               this.flag.getDefinition().getPointsPerSecond() / 20D);
        }
    }

    @Override
    public boolean isCarrying(MatchPlayer player) {
        return this.carrier == player;
    }

    @Override
    public boolean isCarrying(Party party) {
        return this.carrier.getParty() == party;
    }

    @Override
    protected boolean canSeeParticles(Player player) {
        return super.canSeeParticles(player) && player != this.carrier.getBukkit();
    }

    protected void dropFlag() {
        for(Location dropLocation : this.dropLocations) {
            if(this.flag.canDrop(new PlayerQueryWithLocation(carrier, dropLocation))) {
                this.flag.transition(new Dropped(this.flag, this.post, dropLocation, this.carrier));
                return;
            }
        }

        // Could not find a usable drop location, just recover the flag
        forceRecover();
    }

    protected void captureFlag(Net net) {
        this.carrier.sendMessage(new TranslatableComponent("match.flag.capture.you",
                                                           this.flag.getComponentName()));

        this.flag.getMatch().sendMessageExcept(new TranslatableComponent("match.flag.capture",
                                                                         this.flag.getComponentName(),
                                                                         this.carrier.getComponentName()),
                                               this.carrier);

        this.flag.resetTouches(this.carrier.getCompetitor());
        this.flag.resetProximity(this.carrier.getCompetitor());

        ScoreMatchModule smm = this.flag.getMatch().getMatchModule(ScoreMatchModule.class);
        if(smm != null) {
            if(net.getPointsPerCapture() != 0) {
                smm.incrementScore(this.getBeneficiary(net.getOwner()),
                                   net.getPointsPerCapture());
            }

            if(this.flag.getDefinition().getPointsPerCapture() != 0) {
                smm.incrementScore(this.getBeneficiary(this.flag.getDefinition().getOwner()),
                                   this.flag.getDefinition().getPointsPerCapture());
            }
        }

        Post post = net.getReturnPost() != null ? net.getReturnPost() : this.post;
        if(post.isPermanent()) {
            this.flag.transition(new Completed(this.flag, post));
        } else {
            this.flag.transition(new Captured(this.flag, post, net, this.getLocation()));
        }

        FlagCaptureEvent event = new FlagCaptureEvent(this.flag, this.carrier, net);
        this.flag.getMatch().callEvent(event);
    }

    protected boolean isCarrier(MatchPlayer player) {
        return carrier.equals(player);
    }

    protected boolean isCarrier(Entity player) {
        return carrier.getBukkit().equals(player);
    }

    protected boolean isFlag(ItemStack stack) {
        return stack.isSimilar(this.flag.getBannerItem());
    }

    @Override
    public void onEvent(PlayerDropItemEvent event) {
        super.onEvent(event);
        if(this.isCarrier(event.getPlayer()) && this.isFlag(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
            dropFlag();
        }
    }

    @Override
    public void onEvent(ParticipantDespawnEvent event) {
        super.onEvent(event);
        // Don't drop the flag when the match ends and everyone despawns
        if(isCarrier(event.getPlayer()) && flag.getMatch().isRunning()) {
            dropFlag();
        }
    }

    @Override
    public void onEvent(PlayerLeavePartyEvent event) {
        super.onEvent(event);
        // Handle flag carrier disconnecting after match ends.
        // Disconnect during the match is handled by the despawn event instead.
        if(isCarrier(event.getPlayer()) && flag.getMatch().isFinished()) {
            dropFlag();
        }
    }

    @Override
    public void onEvent(InventoryClickEvent event) {
        super.onEvent(event);
        if(this.isCarrier(event.getWhoClicked()) && event.getSlot() == ArmorType.HELMET.inventorySlot()) {
            event.setCancelled(true);
            event.getView().setCursor(null);
            event.setCurrentItem(null);
            this.flag.getMatch().getScheduler(MatchScope.RUNNING).createTask(() -> {
                if(isCurrent()) {
                    dropFlag();
                }
            });
        }
    }

    @Override
    public void onEvent(CoarsePlayerMoveEvent event) {
        super.onEvent(event);

        if(this.isCarrier(event.getPlayer())) {
            final Location playerLoc = event.getBlockTo();
            if((dropLocations.isEmpty() || !dropLocations.peekLast().equals(playerLoc)) &&
               flag.canDrop(new PlayerQueryWithLocation(carrier, playerLoc))) {

                if(this.dropLocations.size() >= DROP_QUEUE_SIZE) this.dropLocations.removeLast();
                this.dropLocations.addFirst(playerLoc);
            }

            this.checkCapture(playerLoc);
        }
    }

    @Override
    public void onEvent(GoalEvent event) {
        super.onEvent(event);
        this.checkCapture(null);
    }

    @Override
    public void onEvent(FlagStateChangeEvent event) {
        super.onEvent(event);
        this.checkCapture(null);
    }

    protected void checkCapture(Location to) {
        if(to == null) to = this.carrier.getBukkit().getLocation();

        this.deniedByFlag = null;
        if(this.deniedByNet != null && !this.deniedByNet.isSticky()) {
            this.deniedByNet = null;
        }

        for(Net net : this.flag.getNets()) {
            if(net.getRegion().contains(to)) {
                if(tryCapture(net)) {
                    return;
                } else {
                    this.deniedByNet = net;
                }
            }
        }

        if(this.deniedByNet != null) {
            tryCapture(this.deniedByNet);
        }
    }

    protected boolean tryCapture(Net net) {
        for(FlagDefinition returnableDef : net.getRecoverableFlags()) {
            Flag returnable = returnableDef.getGoal(this.flag.getMatch());
            if(returnable.isCurrent(Carried.class)) {
                this.deniedByFlag = returnable;
                return false;
            }
        }

        if(this.flag.canCapture(this.carrier, net)) {
            this.captureFlag(net);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ChatColor getStatusColor(Party viewer) {
        if(this.flag.getDefinition().hasMultipleCarriers()) {
            return this.carrier.getParty().getColor();
        } else {
            return super.getStatusColor(viewer);
        }
    }

    @Override
    public String getStatusSymbol(Party viewer) {
        return Flag.CARRIED_SYMBOL;
    }
}
