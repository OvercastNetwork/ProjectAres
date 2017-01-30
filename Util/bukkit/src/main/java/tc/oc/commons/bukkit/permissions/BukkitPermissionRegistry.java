package tc.oc.commons.bukkit.permissions;

import javax.inject.Inject;

import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class BukkitPermissionRegistry implements PermissionRegistry {

    private final PluginManager pluginManager;

    @Inject BukkitPermissionRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void addPermission(Permission permission) {
        pluginManager.addPermission(permission);
    }

    @Override
    public void removePermission(Permission permission) {
        pluginManager.removePermission(permission);
    }
}
