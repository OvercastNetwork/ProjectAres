package tc.oc.pgm.filters.query;

import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.util.ImVector;

public class PlayerQueryWithLocation implements ForwardingPlayerQuery {

    private final IPlayerQuery player;
    private final Location location;
    private final ImVector blockCenter;

    public PlayerQueryWithLocation(IPlayerQuery player, Location location) {
        this.player = player;
        this.location = location;
        this.blockCenter = ImVector.copyOf(location.position().blockCenter());
    }

    @Override
    public IPlayerQuery playerQuery() {
        return player;
    }

    @Override
    public EntityLocation getEntityLocation() {
        return EntityLocation.coerce(location, playerQuery().getEntityLocation());
    }

    @Override
    public ImVector blockCenter() {
        return blockCenter;
    }
}
