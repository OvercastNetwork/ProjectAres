package tc.oc.lobby.bukkit.portals;

import org.bukkit.geometry.Cuboid;
import tc.oc.commons.bukkit.config.ExternalConfiguration;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.teleport.Navigator;
import tc.oc.commons.core.logging.Loggers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PortalsConfig extends ExternalConfiguration {

    private Logger logger;
    private Navigator navigator;

    @Inject
    PortalsConfig(Loggers loggers, Navigator navigator) {
        this.logger = loggers.get(getClass());
        this.navigator = navigator;
    }

    public Set<Portal> loadPortals() {
        if (enabled()) {
            Set<Portal> portals = new HashSet();
            logger.log(Level.INFO, "Portals are enabled, loading...");
            for (String name : getPortals()) {
                Portal portal = new Portal(name, navigator.parseConnector(getTo(name)), Cuboid.between(ConfigUtils.getVector(config(), "portals." + name + ".min", null), ConfigUtils.getVector(config(), "portals." + name + ".max", null)));
                portals.add(portal);
                logger.fine("Enabled portal " + name);
            }
            return portals;
        } else {
            logger.fine("Portals are not enabled, therefore they will not be in use.");
        }
        return Collections.emptySet();
    }

    public String configName() {
        return "portals";
    }

    protected String fileName() {
        return "lobby-portals";
    }

    private String getTo(String token) {
        return config().getString("portals." + token + ".to");
    }

    private Set<String> getPortals() {
        return config().getConfigurationSection("portals").getKeys(false);
    }

    private boolean enabled() {
        return config().getBoolean("enabled");
    }
}
