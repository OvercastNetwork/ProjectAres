package tc.oc.commons.bukkit.listeners;

import com.google.common.eventbus.Subscribe;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Server;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Create and update magic permission groups from the local server document
 */
public class PermissionGroupListener implements PluginFacet {

    private final PluginManager pluginManager;
    private final Server localServer;
    private final OnlinePlayers onlinePlayers;

    @Inject PermissionGroupListener(PluginManager pluginManager, Server localServer, OnlinePlayers onlinePlayers) {
        this.pluginManager = pluginManager;
        this.localServer = localServer;
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public void enable() {
        updateServer(null, localServer);
    }

    @Subscribe
    public void onReconfigure(LocalServerReconfigureEvent event) {
        updateServer(event.getOldConfig(), event.getNewConfig());
    }

    private void updateServer(@Nullable Server before, Server after) {
        boolean dirty = false;
        dirty |= updatePermission(Permissions.OBSERVER, before == null ? null : before.observer_permissions(), after.observer_permissions());
        dirty |= updatePermission(Permissions.PARTICIPANT, before == null ? null : before.participant_permissions(), after.participant_permissions());
        dirty |= updatePermission(Permissions.MAPMAKER, before == null ? null : before.mapmaker_permissions(), after.mapmaker_permissions());

        if(dirty) {
            onlinePlayers.all().forEach(Permissible::recalculatePermissions);
        }
    }

    private boolean updatePermission(String name, Map<String, Boolean> before, Map<String, Boolean> after) {
        if(Objects.equals(before, after)) return false;

        final Permission perm = new Permission(name, PermissionDefault.FALSE, after);
        pluginManager.removePermission(perm);
        pluginManager.addPermission(perm);
        return true;
    }
}
