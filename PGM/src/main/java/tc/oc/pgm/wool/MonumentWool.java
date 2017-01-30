package tc.oc.pgm.wool;

import java.util.Collections;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.events.PlayerItemTransferEvent;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.goals.TouchableGoal;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.teams.Team;

public class MonumentWool extends TouchableGoal<MonumentWoolFactory> implements Goal<MonumentWoolFactory> {

    public static final String SYMBOL_WOOL_INCOMPLETE = "\u2b1c";   // ⬜
    public static final String SYMBOL_WOOL_TOUCHED = "\u2592";      // ▒
    public static final String SYMBOL_WOOL_COMPLETE = "\u2b1b";     // ⬛

    protected boolean placed = false;
    private final Location woolLocation;
    private final Location monumentLocation;

    public MonumentWool(MonumentWoolFactory definition, Match match) {
        super(definition, match);
        this.woolLocation = definition.getLocation().toLocation(match.getWorld());
        this.monumentLocation = definition.getPlacementRegion().getBounds().center().toLocation(match.getWorld());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MonumentWool{");
        sb.append("id=").append(this.slug());
        sb.append(",owner=").append(this.owner.getName());
        sb.append(",definition=").append(this.definition);
        sb.append('}');
        return sb.toString();
    }

    // Remove @Nullable
    @Override
    public Team getOwner() {
        return super.getOwner();
    }

    @Override
    public BaseComponent getTouchMessage(ParticipantState toucher, boolean self) {
        return new TranslatableComponent(self ? "match.touch.wool.you" : "match.touch.wool.teamSuffix",
                                         new PlayerComponent(toucher.getIdentity(), NameStyle.COLOR),
                                         getComponentName(),
                                         toucher.getParty().getComponentName());
    }

    @Override
    public Iterable<Location> getProximityLocations(ParticipantState player) {
        if(hasTouched(player.getParty())) {
            // After the wool has been touched, the goal is located at the monument
            return Collections.singleton(monumentLocation);
        } else {
            // Before the wool has been touched, the goal is located at the wool
            return Collections.singleton(woolLocation);
        }
    }

    @Override
    protected boolean canPlayerUpdateProximity(ParticipantState player) {
        // Wool proximity is affected by all players, while monument proximity only counts for wool runners
        if(!super.canPlayerUpdateProximity(player)) return false;
        if(!hasTouched(player.getParty())) return true;
        MatchPlayer onlinePlayer = player.getMatchPlayer();
        return onlinePlayer != null && this.getDefinition().isHolding(onlinePlayer);
    }

    @Override
    protected boolean canBlockUpdateProximity(BlockState oldState, BlockState newState) {
        // If monument proximity metric is closest block, make it only the wool
        return !hasTouched(getOwner()) || this.getDefinition().isObjectiveWool(newState.getData());
    }

    public void handleWoolAcquisition(Player player, ItemStack item) {
        if(!this.isPlaced() && this.getDefinition().isObjectiveWool(item)) {
            ParticipantState participant = this.getMatch().getParticipantState(player);
            if(participant != null && this.canComplete(participant.getParty())) {
                touch(participant);

                // Initialize monument proximity
                ProximityMetric metric = getProximityMetric(participant.getParty());
                if(metric != null) {
                    switch(metric.type) {
                        case CLOSEST_BLOCK:
                            updateProximity(participant, this.woolLocation);
                            break;
                        case CLOSEST_PLAYER:
                            updateProximity(participant, player.getLocation());
                            break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemTransfer(PlayerItemTransferEvent event) {
        if(event.isAcquiring()) handleWoolAcquisition(event.getPlayer(), event.getItemStack());
    }

    public DyeColor getDyeColor() {
        return this.definition.getColor();
    }

    public boolean isPlaced() {
        return this.placed;
    }

    public void markPlaced() {
        this.placed = true;
    }

    @Override
    public boolean canComplete(Competitor team) {
        return team == this.getOwner();
    }

    @Override
    public boolean isCompleted() {
        return this.placed;
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return this.placed && this.canComplete(team);
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        if(getDyeColor() == DyeColor.BLUE) {
            return ChatColor.DARK_BLUE; // DARK_BLUE looks ok on sidebar, but not in chat
        } else {
            return BukkitUtils.toChatColor(this.getDyeColor());
        }
    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        if(this.isCompleted(competitor)) {
            return SYMBOL_WOOL_COMPLETE;
        } else if(shouldShowTouched(competitor, viewer)) {
            return SYMBOL_WOOL_TOUCHED;
        } else {
            return SYMBOL_WOOL_INCOMPLETE;
        }
    }
}
