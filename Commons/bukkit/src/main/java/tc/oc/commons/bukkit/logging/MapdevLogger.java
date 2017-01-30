package tc.oc.commons.bukkit.logging;

import java.util.Optional;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.kencochrane.raven.dsn.Dsn;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.minecraft.logging.BetterRaven;

/**
 * A logger that shows messages to map developers in-game,
 * and forwards to the mapdev Sentry.
 *
 * This logger can be injected and used in any place where
 * it might be useful for mapdevs to know about an error.
 */
@Singleton
public class MapdevLogger extends Logger {
    @Inject MapdevLogger(Loggers loggers, MapdevSentryConfiguration sentryConfig, Optional<BetterRaven> raven) {
        super(loggers.defaultLogger().getName() + ".maps", null);

        setParent(loggers.defaultLogger());
        setUseParentHandlers(false);
        addHandler(new ChatLogHandler(Permissions.MAPERRORS));

        if(sentryConfig.enabled() && raven.isPresent()) {
            // If a map DSN is configured, create a seperate Raven for the map logger
            final Dsn dsn = sentryConfig.dsn();
            loggers.defaultLogger().info("Sending mapdev errors to Sentry at " + dsn);
            final BetterRaven mapRaven = raven.get().clone(dsn);
            mapRaven.listen(this);
        }
    }
}
