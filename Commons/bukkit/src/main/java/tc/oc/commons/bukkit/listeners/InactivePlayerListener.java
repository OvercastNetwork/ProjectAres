package tc.oc.commons.bukkit.listeners;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.localization.CommonsTranslations;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.bukkit.util.OnlinePlayerMapAdapter;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.scheduler.Tickable;

@Singleton
public class InactivePlayerListener implements Listener, PluginFacet, Tickable {

    public static class Config {
        private final Configuration config;
        private final ExceptionHandler<Throwable> exceptionHandler;

        @Inject Config(Configuration config, ExceptionHandler<Throwable> exceptionHandler) {
            this.config = config;
            this.exceptionHandler = exceptionHandler;
        }

        public boolean enabled() {
            return timeout() != null;
        }

        public Duration timeout() {
            return ConfigUtils.getDuration(config, "afk.timeout");
        }

        public Duration warning() {
            return ConfigUtils.getDuration(config, "afk.warning");
        }

        public java.time.Duration interval() {
            return exceptionHandler.flatGet(() -> config.duration("afk.interval"))
                                   .orElse(java.time.Duration.ofSeconds(10));
        }
    }

    private static final String AFK_FOREVER_PERM = "afk.forever";
    private final Config config;
    private final PlayerServerChanger playerServerChanger;

    private final OnlinePlayerMapAdapter<Instant> lastActivity;
    private @Nullable Instant lastCheck;

    @Inject InactivePlayerListener(Config config, PlayerServerChanger playerServerChanger, OnlinePlayerMapAdapter<Instant> lastActivity) {
        this.config = config;
        this.playerServerChanger = playerServerChanger;
        this.lastActivity = lastActivity;
    }

    @Override
    public boolean isActive() {
        return config.enabled();
    }

    @Override
    public java.time.Duration tickPeriod() {
        return config.interval();
    }

    @Override
    public void enable() {
        lastActivity.enable();
    }

    @Override
    public void disable() {
        lastActivity.disable();
    }

    private void activity(Player player) {
        if(player.hasPermission(AFK_FOREVER_PERM)) {
            this.lastActivity.remove(player);
        } else {
            this.lastActivity.put(player, Instant.now());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) {
        this.activity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void move(PlayerMoveEvent event) {
        if(!Objects.equals(event.getFrom(), event.getTo())) {
            this.activity(event.getPlayer());
        }
    }

    @Override
    public void tick() {
        final Duration timeout = config.timeout();
        final Duration warning = config.warning();

        Instant now = Instant.now();
        Instant kickTime = now.minus(timeout);
        Instant warnTime = warning == null ? null : now.minus(warning);
        Instant lastWarnTime = warning == null || lastCheck == null ? null : lastCheck.minus(warning);

        // Iterate over a copy, because kicking players while iterating the original
        // OnlinePlayerMapAdapter throws a ConcurrentModificationException
        for(Map.Entry<Player, Instant> entry : lastActivity.entrySetCopy()) {
            Player player = entry.getKey();
            Instant time = entry.getValue();

            if(time.isBefore(kickTime)) {
                playerServerChanger.kickPlayer(player, CommonsTranslations.get().t("afk.kick", player));
            } else if(warnTime != null && time.isAfter(lastWarnTime) && !time.isAfter(warnTime)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + CommonsTranslations.get().t(
                    "afk.warn", player,
                    ChatColor.AQUA.toString() + ChatColor.BOLD +
                        timeout.minus(warning).getSeconds() +
                        ChatColor.RED + ChatColor.BOLD
                ));
            }
        }

        lastCheck = now;
    }
}
