package tc.oc.pgm.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.raindrops.RaindropConstants;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.menu.gui.MainMenuButton;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.tokens.gui.MainTokenButton;

import java.time.Duration;

public class MenuListener implements Listener {

    @EventHandler
    public void onObserverInteract(ObserverInteractEvent event) {
        if (event.getClickType() == ClickType.RIGHT) {
            MainMenuButton button = new MainMenuButton(event.getPlayer().getBukkit());
            ItemStack main = button.getIcon().create();
            //isSimilar so that stacks of the item will still open the menu
            if (event.getPlayer().getBukkit().getItemInHand().isSimilar(main)) {
                button.function(event.getPlayer().getBukkit());
            }
        }
    }

    @EventHandler
    public void giveKitToObservers(ObserverKitApplyEvent event) {
        Player player = event.getPlayer();
        ItemStack main = new MainMenuButton(player).getIcon().create();
        player.getInventory().setItem(5, main);
    }
}
