package tc.oc.commons.bukkit.respack;

import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.util.OnlinePlayerMapAdapter;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;

@Singleton
public class ResourcePackListener implements ResourcePackManager, Listener, PluginFacet {

    // Minimum time a player must be connected before sending them a res pack.
    // If we send it too soon, the client behaves badly.
    private static final Duration JOIN_DELAY = Duration.ofSeconds(1);

    private final Logger logger;
    private final Plugin plugin;
    private final MinecraftService minecraftService;
    private final UserService userService;

    private boolean enabled = true;
    private final OnlinePlayerMapAdapter<String> lastSentSha1;
    private final OnlinePlayerMapAdapter<Instant> joinTime;

    @Inject ResourcePackListener(Loggers loggers, Plugin plugin, MinecraftService minecraftService, UserService userService, OnlinePlayerMapAdapter<String> lastSentSha1, OnlinePlayerMapAdapter<Instant> joinTime) {
        this.logger = loggers.get(getClass());
        this.userService = userService;
        this.lastSentSha1 = lastSentSha1;
        this.joinTime = joinTime;
        this.minecraftService = minecraftService;
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        lastSentSha1.enable();
        joinTime.enable();
    }

    @Override
    public void disable() {
        joinTime.disable();
        lastSentSha1.disable();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(this.enabled != enabled) {
            this.enabled = enabled;
            if(enabled && isFastUpdate()) {
                refreshAll();
            }
        }
    }

    @Override
    public boolean isFastUpdate() {
        return minecraftService.getLocalServer().resource_pack_fast_update();
    }

    @Override
    public @Nullable String getUrl() {
        return minecraftService.getLocalServer().resource_pack_url();
    }

    @Override
    public @Nullable String getSha1() {
        return minecraftService.getLocalServer().resource_pack_sha1();
    }

    @Override
    public void refreshPlayer(final Player player) {
        if(!enabled) return;
        if(!player.isOnline()) return;

        String url = getUrl();
        String sha1 = getSha1();
        if(url == null || sha1 == null) return;

        if(!Objects.equals(lastSentSha1.get(player), sha1)) {
            Instant joined = joinTime.get(player);
            if(joined == null) return;
            long delayMillis = Duration.between(Instant.now(), joined.plus(JOIN_DELAY)).toMillis();

            if(delayMillis <= 0) {
                logger.fine("Sending resource pack " + url + " with SHA1 " + sha1 + " to player " + player.getName());
                lastSentSha1.put(player, sha1);
                player.setResourcePack(url, sha1);
            } else {
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        refreshPlayer(player);
                    }
                }, delayMillis / 50 + 1);
            }
        }
    }

    @Override
    public void refreshAll() {
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        this.joinTime.put(event.getPlayer(), Instant.now());
        refreshPlayer(event.getPlayer());
    }

    @Subscribe
    public void reconfigure(LocalServerReconfigureEvent event) {
        if(event.getNewConfig().resource_pack_fast_update()) {
            String oldSha1 = event.getOldConfig() == null ? null : event.getOldConfig().resource_pack_sha1();
            String newSha1 = event.getNewConfig().resource_pack_sha1();
            if(!Objects.equals(oldSha1, newSha1)) refreshAll();
        }
    }

    @EventHandler
    public void confirm(final PlayerResourcePackStatusEvent event) {
        logger.fine("Player " + event.getPlayer().getName() + " sent res pack status " + event.getStatus());
        final UserDoc.ResourcePackStatus status;
        switch(event.getStatus()) {
            case ACCEPTED: status = UserDoc.ResourcePackStatus.ACCEPTED; break;
            case DECLINED: status = UserDoc.ResourcePackStatus.DECLINED; break;
            case SUCCESSFULLY_LOADED: status = UserDoc.ResourcePackStatus.LOADED; break;
            case FAILED_DOWNLOAD: status = UserDoc.ResourcePackStatus.FAILED; break;
            default: throw new IllegalStateException("Unknown status " + event.getStatus());
        }
        userService.update(
            Users.playerId(event.getPlayer()),
            (UserDoc.ResourcePackResponse) () -> status
        );
    }
}
