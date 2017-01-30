package tc.oc.commons.bukkit.nick;

import java.lang.ref.WeakReference;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;
import com.google.common.eventbus.Subscribe;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;

@Singleton
public class PlayerOrderCache implements PlayerOrder.Factory, PluginFacet {

    private final LoadingCache<Player, Integer> prioritiesByPlayer;
    private final LoadingCache<CommandSender, PlayerOrder> comparatorsByViewer;

    @Inject PlayerOrderCache(IdentityProvider identityProvider, BukkitUserStore userStore, MinecraftService minecraftService) {

        prioritiesByPlayer = CacheUtils.newWeakKeyCache(player -> userStore
            .getUser(player)
            .minecraft_flair()
            .stream()
            .filter(flair -> minecraftService.getLocalServer().realms().contains(flair.realm))
            .map(flair -> flair.priority)
            .min(Integer::compare)
            .orElse(Integer.MAX_VALUE)
        );

        comparatorsByViewer = CacheUtils.newWeakKeyCache(strongViewer -> {
            final WeakReference<CommandSender> weakViewer = new WeakReference<>(strongViewer);
            return (a, b) -> {
                // Do not reference strongViewer in here
                final CommandSender viewer = weakViewer.get();
                if(viewer == null) return 0;

                final Identity aIdentity = identityProvider.currentIdentity(a);
                final Identity bIdentity = identityProvider.currentIdentity(b);
                return ComparisonChain.start()
                    .compareTrueFirst(a == viewer, b == viewer)
                    .compareTrueFirst(aIdentity.isFriend(viewer), bIdentity.isFriend(viewer))
                    .compare(priority(a, aIdentity, viewer), priority(b, bIdentity, viewer))
                    .compare(aIdentity.getName(viewer), bIdentity.getName(viewer), String::compareToIgnoreCase)
                    .result();
            };
        });
    }

    private int priority(Player player, Identity identity, CommandSender viewer) {
        if(identity.isDisguised(viewer)) return Integer.MAX_VALUE;

        final Integer priority = prioritiesByPlayer.getUnchecked(player);
        if(!player.willBeOnline()) {
            prioritiesByPlayer.invalidate(player);
        }
        return priority;
    }

    @Override
    public PlayerOrder apply(CommandSender viewer) {
        final PlayerOrder order = comparatorsByViewer.getUnchecked(viewer);
        if(viewer instanceof Player && !((Player) viewer).willBeOnline()) {
            comparatorsByViewer.invalidate(viewer);
        }
        return order;
    }

    @Subscribe
    public void onReconfigure(LocalServerReconfigureEvent event) {
        // Invalidate everything if local realms change
        if(event.getOldConfig() != null && !event.getOldConfig().realms().equals(event.getNewConfig().realms())) {
            prioritiesByPlayer.invalidateAll();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        prioritiesByPlayer.invalidate(event.getPlayer());
        comparatorsByViewer.invalidate(event.getPlayer());
    }
}
