package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;
import tc.oc.pgm.teams.Team;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class TeamChestMutation extends KitMutation {

    private final static FreeItemKit TEAM_CHEST = new FreeItemKit(new ItemBuilder()
            .material(Material.CHEST)
            .name(Constants.PREFIX + "Team Chest")
            .lore(Constants.SUBTEXT + "Place down to open!")
            .get());

    private Map<Team, Inventory> TEAM_INVENTORY = new WeakHashMap<>();

    public TeamChestMutation(Match match) {
        super(match, false);
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        PlayerInventory playerInventory = player.getInventory();
        if (!(playerInventory.contains(chestAsItem()))) {
            kits.add(TEAM_CHEST);
        }
    }

    @EventHandler
    public void blockPlace(PlayerInteractEvent e) {
        if (e.getItem().isSimilar(chestAsItem())) {
            Player player = e.getPlayer();
            MatchPlayer matchPlayer = match().getPlayer(player);
            handleInventoryOpen(matchPlayer);
            e.setCancelled(true);
        }
    }

    private ItemStack chestAsItem() {
        return TEAM_CHEST.item();
    }

    private void handleInventoryOpen(MatchPlayer matchPlayer) {
        if (matchPlayer.isObserving()) {
            return;
        }
        if (!(TEAM_INVENTORY.containsKey((Team) matchPlayer.getParty()))) {
            Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Team Chest");
            TEAM_INVENTORY.put((Team) matchPlayer.getParty(), inventory);
            matchPlayer.getBukkit().openInventory(inventory);
        } else {
            matchPlayer.getBukkit().openInventory(TEAM_INVENTORY.get((Team) matchPlayer.getParty()));
        }
    }
}
