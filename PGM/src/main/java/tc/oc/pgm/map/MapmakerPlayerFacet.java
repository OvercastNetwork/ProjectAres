package tc.oc.pgm.map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.util.Permissions;
import tc.oc.pgm.match.MatchPlayerFacet;

/**
 * Grant the ocn.mapmaker permission to any authors of the current {@link PGMMap}.
 *
 * TODO: If we add an isActive mechanism to MatchPlayerFacet, this one should only
 * be active for authors.
 */
public class MapmakerPlayerFacet implements MatchPlayerFacet {

    private final Plugin plugin;
    private final MapInfo mapInfo;
    private final PlayerId playerId;
    private final Player player;

    private @Nullable PermissionAttachment permissionAttachment;

    @Inject MapmakerPlayerFacet(Plugin plugin, MapInfo mapInfo, PlayerId playerId, Player player) {
        this.plugin = plugin;
        this.mapInfo = mapInfo;
        this.playerId = playerId;
        this.player = player;
    }

    @Override
    public void enable() {
        if(mapInfo.isAuthor(playerId)) {
            permissionAttachment = player.addAttachment(plugin, Permissions.MAPMAKER, true);
        }
    }

    @Override
    public void disable() {
        if(permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
        }
    }
}
