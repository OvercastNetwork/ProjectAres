package tc.oc.commons.bungee.sessions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import java.time.Duration;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.scheduler.Tickable;

@Singleton
public class MojangSessionServiceMonitor implements PluginFacet, Tickable {
    private static final String URL = "https://sessionserver.mojang.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final java.time.Duration POLLING_INTERVAL = java.time.Duration.ofSeconds(10);

    private final Logger logger;
    private SessionState forceState = SessionState.ABSENT;
    private SessionState state = SessionState.ONLINE; // assume online

    @Inject MojangSessionServiceMonitor(Loggers loggers) {
        this.logger = loggers.get(getClass());
    }

    @Override
    public java.time.Duration tickPeriod() {
        return POLLING_INTERVAL;
    }

    /**
     * Gets the session state. State can either be forced or discovered by
     * periodically pinging the Mojang session server.
     */
    public SessionState getState() {
        if (forceState != SessionState.ABSENT) {
            return forceState;
        } else {
            return state;
        }
    }

    /**
     * Get the forced session state.
     */
    public SessionState getForceState() {
        return forceState;
    }

    /**
     * Set the forced session state. This will override whatever state has been
     * discovered.
     */
    public void setForceState(SessionState state) {
        forceState = state;
    }

    /**
     * Gets the session state discovered by pinking Mojang's session server.
     */
    public SessionState getDiscoveredState() {
        return state;
    }

    @Override
    public void tick() {
        int response = getResponseCode(URL);
        SessionState newState = response == 200 ? SessionState.ONLINE : SessionState.OFFLINE;

        if(state != newState) {
            logger.warning("Status (" + response + ") changed from " + state + ChatColor.RESET + " to " + newState);
            state = newState;
        }
    }

    private int getResponseCode(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout((int) TIMEOUT.toMillis());
            connection.setReadTimeout((int) TIMEOUT.toMillis());
            connection.setRequestMethod("GET");

            connection.connect();
            return connection.getResponseCode();
        } catch (IOException e) {
            return -1;
        }
    }
}
