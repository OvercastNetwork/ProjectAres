package tc.oc.lobby.bukkit.gizmos;

import com.google.common.collect.Lists;
import javax.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.users.PurchaseGizmoRequest;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.raindrops.PlayerRecieveRaindropsEvent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.Utils;
import tc.oc.lobby.bukkit.listeners.RaindropsListener;
import tc.oc.minecraft.scheduler.SyncExecutor;

public class GizmoUtils {

    @Inject private static SyncExecutor syncExecutor;
    @Inject private static UserService userService;

    public static void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 18, ChatColor.RED + LobbyTranslations.get().t("gizmos", player));
        inventory.setItem(4, Utils.getGhastTear(player, RaindropsListener.raindrops.get(player)));

        int i = 9;
        for(Gizmo gizmo : Gizmos.gizmos) {
            inventory.setItem(i++, gizmo.getItemStack(player));
        }

        player.openInventory(inventory);
    }

    public static void openShop(Player player, Gizmo gizmo) {
        Inventory inventory = Bukkit.createInventory(player, 54, StringUtils.truncate(ChatColor.GOLD + LobbyTranslations.get().t("gizmos.shopFor", player, gizmo.getColoredName(player)), 32));

        inventory.setItem(4, Utils.getGhastTear(player, RaindropsListener.raindrops.get(player)));
        inventory.setItem(22, gizmo.getItemStack(player));

        ItemStack accept = new ItemStack(Material.DIAMOND);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + LobbyTranslations.get().t("purchase.purchase", player, gizmo.getColoredName(player)));
        acceptMeta.setLore(Lists.newArrayList(gizmo.getCostText(player)));
        accept.setItemMeta(acceptMeta);

        ItemStack exit = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName(ChatColor.RED + LobbyTranslations.get().t("purchase.cancel", player));
        exit.setItemMeta(exitMeta);

        for(int i = 3; i < 6; i++) {
            for(int x = 0; x < 3; x++) {
                inventory.setItem(i * 9 + x, accept);
            }

            for(int y = 6; y < 9; y++) {
                inventory.setItem(i * 9 + y, exit);
            }
        }

        player.openInventory(inventory);
    }

    public static void setGizmo(Player player, Gizmo gizmo, boolean initial) {
        Gizmos.gizmoMap.put(player, gizmo);
        player.getInventory().setItem(6, gizmo.getHotbarItemStack(player));
        if(!initial) player.sendMessage(ChatColor.GOLD + LobbyTranslations.get().t("gizmo.equip", player, gizmo.getColoredName(player)));
    }

    public static void cancelPurchase(final Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + LobbyTranslations.get().t("gizmo.purchasing.cancel", player));
        Gizmos.purchasingMap.remove(player);
    }

    public static void purchaseGizmo(final Player player, final Gizmo gizmo) {
        player.closeInventory();

        final PlayerId playerId = Users.playerId(player);

        syncExecutor.callback(
            userService.purchaseGizmo(playerId, new PurchaseGizmoRequest(gizmo.getIdentifier(), gizmo.getCost())),
            CommandFutureCallback.onSuccess(player, obj -> {
                if(!player.isOnline()) return;

                Bukkit.getPluginManager().callEvent(new PlayerRecieveRaindropsEvent(player, -gizmo.getCost(), 100, new Component("Purchased gizmo")));
                player.addAttachment(Lobby.get(), gizmo.getPermissionNode(), true);
                Gizmos.purchasingMap.remove(player);
                setGizmo(player, gizmo, false);

                player.sendMessage(ChatColor.GREEN + LobbyTranslations.get().t(
                    "gizmo.purchased.success",
                    player,
                    gizmo.getColoredName(player) + ChatColor.GREEN,
                    String.valueOf(gizmo.getCost()) + ChatColor.AQUA
                ));
            })
        );
    }
}
