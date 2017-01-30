package tc.oc.pgm.tnt.license;

import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerBlockTransformEvent;
import tc.oc.pgm.events.PlayerItemTransferEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.tnt.TNTProperties;

import static org.bukkit.Material.FLINT_AND_STEEL;
import static tc.oc.pgm.events.ItemTransferEvent.Type.PICKUP;
import static tc.oc.pgm.events.ItemTransferEvent.Type.PLUGIN;

/**
 * Restricts access to TNT related items if the {@link MatchPlayer} has no license.
 */
@ListenerScope(MatchScope.LOADED)
public class LicenseAccessPlayerFacet implements MatchPlayerFacet, Listener {

    private final User user;
    private final MatchPlayer self;
    private final TNTProperties tntProperties;
    private final LicenseConfiguration licenseConfiguration;

    @Inject LicenseAccessPlayerFacet(User user, MatchPlayer self, TNTProperties tntProperties, LicenseConfiguration licenseConfiguration) {
        this.user = user;
        this.self = self;
        this.tntProperties = tntProperties;
        this.licenseConfiguration = licenseConfiguration;
    }

    private boolean restrictAccess() {
        return licenseConfiguration.controlAccess() &&
               tntProperties.licensing &&
               user.granted_tnt_license_at() == null;
    }

    @TargetedEventHandler(ignoreCancelled = true)
    public void onItemAcquire(PlayerItemTransferEvent event) {
        if(restrictAccess() && event.isAcquiring() && isRestricted(event.getItemStack().getType())) {
            event.setCancelled(true);
            if(event.getType() != PLUGIN && event.getType() != PICKUP) {
                sendWarning();
            }
        }
    }

    @TargetedEventHandler(ignoreCancelled = true)
    public void onBlockTransform(PlayerBlockTransformEvent event) {
        if(restrictAccess() && isRestricted(event.getBlock().getType())) {
            event.setCancelled(true);
            if(event.isManual()) {
                sendWarning();
            }
        }
    }

    @TargetedEventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if(!restrictAccess()) return;
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch(event.getClickedBlock().getType()) {
                case STONE_BUTTON:
                case WOOD_BUTTON:
                case LEVER:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                case REDSTONE_COMPARATOR_OFF:
                case REDSTONE_COMPARATOR_ON:
                    event.setCancelled(true);
                    sendWarning();
                    break;
                case TNT:
                    if(event.getItem() != null && event.getItem().getType() == FLINT_AND_STEEL) {
                        event.setCancelled(true);
                        sendWarning();
                    }
                    break;
            }
        } else if(event.getAction() == Action.PHYSICAL) {
            switch(event.getClickedBlock().getType()) {
                case STONE_PLATE:
                case WOOD_PLATE:
                case GOLD_PLATE:
                case IRON_PLATE:
                case TRIPWIRE:
                    event.setCancelled(true);
                    break;
            }
        }
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRevoke(LicenseRevokeEvent event) {
        if(event.hadLicense()) {
            final PlayerInventory inventory = self.getInventory();
            inventory.contents().stream()
                     .filter(item -> item != null && isRestricted(item.getType()))
                     .forEach(inventory::remove);
        }
    }

    private void sendWarning() {
        self.sendMessage(new WarningComponent("tnt.license.use.restricted", "/tnt request"));
    }

    private boolean isRestricted(Material material) {
        switch(material) {
            case REDSTONE:
            case REDSTONE_WIRE:
            case REDSTONE_BLOCK:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case TNT:
                return true;
            default:
                return false;
        }
    }

}
