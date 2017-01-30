package tc.oc.pgm.itemkeep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;

public class ItemKeepPlayerFacet implements MatchPlayerFacet, Listener {

    private final MatchPlayer player;
    private final Player bukkit;
    private final ItemKeepRules rules;
    private final Map<Slot, ItemStack> kept = new HashMap<>();

    @Inject ItemKeepPlayerFacet(MatchPlayer player, Player bukkit, ItemKeepRules rules) {
        this.player = player;
        this.bukkit = bukkit;
        this.rules = rules;
    }

    /**
     * NOTE: Must be called before {@link tc.oc.pgm.tracker.trackers.DeathTracker#onPlayerDeath(PlayerDeathEvent)}
     */
    @SuppressWarnings("deprecation")
    @TargetedEventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void processPlayerDeath(PlayerDeathEvent event) {
        if(!player.isParticipating()) return;
        if(!rules.canKeepAny()) return;

        final Map<Slot, ItemStack> carrying = new HashMap<>();
        Slot.Player.player().forEach(slot -> slot.item(event.getEntity())
                                                 .ifPresent(item -> carrying.put(slot, item)));

        kept.clear();

        carrying.forEach((slot, item) -> {
            if(rules.canKeep(slot, item)) {
                event.getDrops().remove(item);
                kept.put(slot, item);
            }
        });
    }

    public void restoreKeptInventory() {
        final List<ItemStack> displaced = new ArrayList<>();
        final PlayerInventory inv = bukkit.getInventory();

        kept.forEach((slot, keptStack) -> {
            final ItemStack invStack = slot.getItem(bukkit);

            if(invStack == null || slot instanceof Slot.Armor) {
                slot.putItem(inv, keptStack);
            } else {
                if(invStack.isSimilar(keptStack)) {
                    int n = Math.min(keptStack.getAmount(), invStack.getMaxStackSize() - invStack.getAmount());
                    invStack.setAmount(invStack.getAmount() + n);
                    keptStack.setAmount(keptStack.getAmount() - n);
                }
                if(keptStack.getAmount() > 0) {
                    displaced.add(keptStack);
                }
            }

            for(ItemStack stack : displaced) {
                inv.addItem(stack);
            }
        });
        kept.clear();
    }

    @TargetedEventHandler(priority =  EventPriority.MONITOR, ignoreCancelled = true)
    public void partyChange(PlayerPartyChangeEvent event) {
        kept.clear();
    }
}
