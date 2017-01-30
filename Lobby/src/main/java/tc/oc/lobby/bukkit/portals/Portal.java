package tc.oc.lobby.bukkit.portals;

import com.google.common.base.Preconditions;
import org.bukkit.geometry.Cuboid;
import tc.oc.commons.bukkit.teleport.Navigator;

public class Portal {

    private final String name;
    private final Navigator.Connector connector;
    private final Cuboid cuboid;

    public Portal(String name, Navigator.Connector connector, Cuboid cuboid) {
        this.name = name;
        this.connector = Preconditions.checkNotNull(connector);
        this.cuboid = cuboid;
    }

    public String getName() {
        return name;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public Navigator.Connector getConnector() {
        return connector;
    }
}
