package tc.oc.pgm.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.menu.gui.MainMenuButton;

public class MenuListener implements Listener {

    @EventHandler
    public void onObserverInteract(ObserverInteractEvent event) {
        if (event.getClickType() == ClickType.RIGHT) {
            MainMenuButton button = new MainMenuButton();
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
        ItemStack main = new MainMenuButton().getIcon().create();
        player.getInventory().setItem(5, main);
    }
}
