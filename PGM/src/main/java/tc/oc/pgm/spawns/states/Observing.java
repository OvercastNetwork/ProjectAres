package tc.oc.pgm.spawns.states;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.util.Materials;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.spawns.ObserverToolFactory;
import tc.oc.pgm.spawns.Spawn;

public class Observing extends State {

    private static final double VOID_HEIGHT = -64;

    private final boolean reset;
    private final boolean teleport;
    private PermissionAttachment permissionAttachment;

    public Observing(MatchPlayer player, boolean reset, boolean teleport) {
        super(player);
        this.reset = reset;
        this.teleport = teleport;
    }

    @Override
    public void enterState() {
        super.enterState();

        permissionAttachment = bukkit.addAttachment(this.smm.getMatch().getPlugin(), Permissions.OBSERVER, true);

        if(reset) player.reset();
        player.setDead(false);
        player.setSpawned(false);
        player.refreshInteraction();
        player.refreshVisibility();
        bukkit.setGameMode(GameMode.CREATIVE);
        bukkit.setAllowFlight(true);
        bukkit.setGravity(true);

        Spawn spawn = smm.getDefaultSpawn();

        if(teleport || player.getBukkit().getLocation().getY() < VOID_HEIGHT) {
            Location location = spawn.getSpawn(player);
            if(location != null) {
                PlayerRespawnEvent event = new PlayerRespawnEvent(player.getBukkit(), location, false);
                player.getMatch().callEvent(event);

                player.getBukkit().teleport(event.getRespawnLocation());
            }
        }

        if(reset) {
            // Give basic observer items
            ObserverToolFactory toolFactory = smm.getObserverToolFactory();
            player.getInventory().setItem(0, toolFactory.getTeleportTool(bukkit));

            ItemStack book = toolFactory.getHowToBook(bukkit);
            if(toolFactory.canUseEditWand(bukkit)) {
                player.getInventory().setItem(1, toolFactory.getEditWand(bukkit));
                if(book != null) player.getInventory().setItem(28, book);
            } else {
                if(book != null) player.getInventory().setItem(1, book);
            }

            // Let other modules give observer items
            player.getMatch().callEvent(new ObserverKitApplyEvent(player.getBukkit()));

            // Apply observer spawn kit, if there is one
            spawn.applyKit(player);
        }

        player.getBukkit().updateInventory();
        player.setVisible(true);
        player.refreshVisibility();

        // The player is not standing on anything, turn their flying on
        if(bukkit.getAllowFlight()) {
            Block block = bukkit.getLocation().subtract(0, 0.1, 0).getBlock();
            if(block == null || !Materials.isColliding(block.getType())) {
                bukkit.setFlying(true);
            }
        }
    }

    @Override
    public void leaveState() {
        super.leaveState();
        if(permissionAttachment != null) bukkit.removeAttachment(permissionAttachment);
    }

    private void tryJoin(Party party) {
        if(party.isParticipatingType() && canSpawn()) {
            transition(new Joining(player));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(player.hasParty()) {
            tryJoin(player.getParty());
        }
    }

    @Override
    public void onEvent(PlayerChangePartyEvent event) {
        super.onEvent(event);
        tryJoin(event.getNewParty());
    }

    @Override
    public void onEvent(CoarsePlayerMoveEvent event) {
        // Don't let observers fall into the void
        if(event.getFrom().getY() >= VOID_HEIGHT && event.getTo().getY() < VOID_HEIGHT) {
            event.setCancelled(true);
            if(event.getPlayer().getAllowFlight()) {
                event.getPlayer().setFlying(true);
            }
        }
    }
}
