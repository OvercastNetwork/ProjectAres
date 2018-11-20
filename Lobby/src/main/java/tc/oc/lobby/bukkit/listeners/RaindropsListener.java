package tc.oc.lobby.bukkit.listeners;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.raindrops.PlayerRecieveRaindropsEvent;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.core.format.GeneralFormatter;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.lobby.bukkit.Utils;
import tc.oc.lobby.bukkit.gizmos.GizmoUtils;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

@Singleton
public class RaindropsListener implements PluginFacet, Listener {
    public static final Map<Player, Integer> raindrops = Maps.newHashMap();

    private static Objective getOrCreateObjective(Scoreboard scoreboard, String name, String criteria) {
        Objective obj = scoreboard.getObjective(name);
        if(obj == null) {
            obj = scoreboard.registerNewObjective(name, criteria);
        }
        return obj;
    }

    private final GeneralFormatter generalFormatter;
    private final ComponentRenderContext renderer;
    private final BukkitUserStore userStore;

    @Inject private RaindropsListener(GeneralFormatter generalFormatter, ComponentRenderContext renderer, BukkitUserStore userStore) {
        this.generalFormatter = generalFormatter;
        this.renderer = renderer;
        this.userStore = userStore;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void displayScoreboard(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final User user = userStore.getUser(player);
        final PlayerId playerId = tc.oc.api.bukkit.users.Users.playerId(player);
        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        final Objective objective = getOrCreateObjective(scoreboard, player.getName(), "dummy");
        final String raindropsName = ChatColor.AQUA + "Droplets";

        objective.setDisplayName(renderer.renderLegacy(generalFormatter.brandName(), event.getPlayer()));

        objective.getScore(raindropsName).setScore(2);
        Utils.displayScoreboard(player, objective);
        setRaindropsCount(player, user.raindrops());
        GizmoUtils.setGizmo(player, Gizmos.emptyGizmo, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void giveGhastTear(final PlayerRespawnEvent event) {
        Utils.giveGhastTear(event.getPlayer(), raindrops.get(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void recieveRaindrops(final PlayerRecieveRaindropsEvent event) {
        Integer drops = raindrops.get(event.getPlayer());
        if(drops != null) {
            setRaindropsCount(event.getPlayer(), drops + RaindropUtil.useMultiplier(event.getRaindrops(), event.getMultiplier()));
        }
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(event.getPlayer().getName());
        if(objective != null) objective.unregister();
        raindrops.remove(event.getPlayer());
    }

    private void setRaindropsCount(Player player, int count) {
        if(player == null) return;

        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(player.getName());

        Integer oldCount = raindrops.get(player);
        if(oldCount != null) {
            Utils.removeScore(player, String.format("%,d", oldCount));
        }
        Utils.addScore(player, objective, String.format("%,d", count), 1);

        raindrops.put(player, count);

        Utils.giveGhastTear(player, count);
    }
}
