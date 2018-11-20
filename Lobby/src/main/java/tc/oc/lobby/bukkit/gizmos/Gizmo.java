package tc.oc.lobby.bukkit.gizmos;

import com.google.common.collect.Lists;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.halloween.HalloweenGizmo;
import tc.oc.lobby.bukkit.listeners.RaindropsListener;

public abstract class Gizmo implements Listener {
    private final String name;
    private final String prefix;
    private final String description;
    private final Material icon;
    private final int cost;

    public Gizmo(String name, String prefix, String description, Material icon, int cost) {
        this.name = name;
        this.prefix = prefix;
        this.description = description;
        this.icon = icon;
        this.cost = cost;

        this.initialize();
    }
 
    protected abstract void initialize();

    public String getName(Player viewer) {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getDescription(Player viewer) {
        return this.description;
    }

    public String getColoredName(Player viewer) {
        return this.getPrefix() + this.getName(viewer);
    }

    public Material getIcon() {
        return this.icon;
    }

    public int getCost() {
        return this.cost;
    }

    public boolean canPurchase(Player player) {
        return RaindropsListener.raindrops.get(player) - this.getCost() >= 0;
    }

    public String getCostText(Player player) {
        if (this instanceof HalloweenGizmo) {
            return ChatColor.YELLOW + LobbyTranslations.get().t("gizmo.specialEvent", player);
        } else if(this.getClass().isInstance(Gizmos.purchasingMap.get(player))) {
            return ChatColor.GOLD + LobbyTranslations.get().t("gizmo.purchasing", player);
        } else if(this.ownsGizmo(player)) {
            return ChatColor.GREEN + LobbyTranslations.get().t("gizmo.purchased", player);
        } else {
            return (this.canPurchase(player) ? ChatColor.GREEN : ChatColor.RED) + LobbyTranslations.get().t("gizmo.cost", player, String.format("%,d", this.getCost()) + ChatColor.AQUA);
        }
    }

    public String getNoPurchaseMessage(Player player) {
        return ChatColor.RED + LobbyTranslations.get().t("raindrops.purchase.fail", player);
    }

    public boolean ownsGizmo(Player player) {
        if(cost <= 0 && (!(this instanceof HalloweenGizmo))) {
            return true;
        } else {
            return player.hasPermission(this.getPermissionNode());
        }
    }

    public String getPermissionNode() {
        return "lobby.gizmo." + getIdentifier();
    }

    public String getIdentifier() {
        return this.name.toLowerCase().replaceAll(" ", "");
    }

    public ItemStack getItemStack(Player player) {
        ItemStack itemStack = new ItemStack(this.getIcon());
        ItemMeta meta = itemStack.getItemMeta();

        List<String> lore = Lists.newArrayList(this.getCostText(player));
        String piece = "";
        for(String word : this.getDescription(player).split(" ")) {
            piece += word + " ";
            if(piece.length() > 35) {
                lore.add(ChatColor.GOLD + piece);
                piece = "";
            }
        }
        if(piece.length() > 0) lore.add(ChatColor.GOLD + piece);

        meta.setLore(lore);
        meta.setDisplayName(this.getColoredName(player));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public ItemStack getHotbarItemStack(Player player) {
        ItemStack itemStack = this.getItemStack(player);
        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(LobbyTranslations.get().t("gizmo.current", player, meta.getDisplayName()));
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
