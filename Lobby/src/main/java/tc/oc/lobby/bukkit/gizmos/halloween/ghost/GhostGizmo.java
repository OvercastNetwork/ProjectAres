package tc.oc.lobby.bukkit.gizmos.halloween.ghost;

import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.util.OnlinePlayerMapAdapter;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.gizmos.Gizmos;
import tc.oc.lobby.bukkit.gizmos.halloween.HalloweenGizmo;

public class GhostGizmo extends HalloweenGizmo implements Listener {
    private final OnlinePlayerMapAdapter<Instant> coolDowns = new OnlinePlayerMapAdapter<>(Lobby.get());
    private static final Duration COOLDOWN = Duration.ofMinutes(1);

    @Inject private static Audiences audiences;

    public GhostGizmo(String name, String prefix, String description, Material icon) {
        super(name, prefix, description, icon);
        this.coolDowns.enable();
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!(Gizmos.gizmoMap.get(player) instanceof GhostGizmo)) return;
        if (player.getItemInHand().getType() != this.getIcon()) return;
        if (coolDowns.get(player) == null || coolDowns.get(player).isBefore(Instant.now().minus(COOLDOWN))) {
            coolDowns.put(player, Instant.now());
            new GhostTask(player).runTask(0, 1);
        } else {
            audiences.get(player).sendWarning(new TranslatableComponent("gizmo.use.cooldown"), true);
        }

    }
}
