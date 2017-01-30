package tc.oc.commons.bukkit.permissions;

import org.bukkit.permissions.Permission;

/**
 * Abstraction layer for Bukkit permission API
 */
public interface PermissionRegistry {
    void addPermission(Permission perm);
    void removePermission(Permission perm);
}
