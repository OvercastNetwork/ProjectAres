package tc.oc.commons.core.restart;

import javax.annotation.Nullable;
import javax.inject.Inject;

import java.time.Duration;
import java.util.Set;

import com.google.common.collect.Sets;
import tc.oc.commons.core.configuration.ConfigUtils;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class RestartConfiguration {

    private final ConfigurationSection config;
    private final ExceptionHandler<Throwable> exceptionHandler;

    @Inject RestartConfiguration(Configuration config, ExceptionHandler<Throwable> exceptionHandler) {
        this.config = checkNotNull(config.getSection("restart"));
        this.exceptionHandler = exceptionHandler;
    }

    public java.time.Duration interval() {
        return exceptionHandler.flatGet(() -> config.duration("interval"))
                               .orElse(java.time.Duration.ofMinutes(1));
    }

    public @Nullable Duration uptimeLimit() {
        return ConfigUtils.getDuration(config, "uptime");
    }

    public long memoryLimit() {
        return config.getLong("memory", 0) * 1024 * 1024; // Megabytes
    }

    /**
     * Maximum time a restart can be deferred after it is requested
     */
    public @Nullable Duration deferTimeout() {
        return ConfigUtils.getDuration(config, "defer-timeout", null);
    }

    /**
     * Maximum time restart can be delayed after new player connections have been blocked (Bungee only)
     */
    public @Nullable Duration emptyTimeout() {
        return ConfigUtils.getDuration(config, "empty-timeout", null);
    }

    /**
     * Maximum number of players that can be disconnected in order to restart the server.
     * This takes priority over empty-timeout.
     */
    public int kickLimit() {
        return config.getInt("kick-limit", Integer.MAX_VALUE);
    }

    /**
     * Restart the server when any of the given stop signals are received from the system.
     */
    public Set<String> stopSignals() {
        return ConfigUtils.getStringSet(config, "stop-signal.triggers", Sets.newHashSet("INT", "TERM"));
    }

    /**
     * The priority that stop signals will restart the server with.
     */
    public Integer stopSignalPriority() {
        return config.getInt("stop-signal.priority", Integer.MAX_VALUE);
    }

    /**
     * Maximum time the server will wait for deferals to resume before forcing a restart.
     */
    public Duration stopSignalTimeout() {
        return ConfigUtils.getDuration(config, "stop-signal.timeout", Duration.ofHours(6));
    }
}
