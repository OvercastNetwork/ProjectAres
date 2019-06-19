package tc.oc.commons.bukkit.permissions;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * Abstraction layer for Bukkit permission API
 */
public interface PermissionRegistry {

    void register(Permission perm);

    void unregister(Permission perm);

    default Permission create(String name, PermissionDefault def) {
        Permission permission = new Permission(name, def);
        register(permission);
        return permission;
    }

    default Permission create(String name) {
        return create(name, Permission.DEFAULT_PERMISSION);
    }

    default Permission createWithDefaultOp(String name) {
        return create(name, PermissionDefault.OP);
    }

    default Permission createWithDefaultFalse(String name) {
        return create(name, PermissionDefault.FALSE);
    }

    default Permission createWithDefaultTrue(String name) {
        return create(name, PermissionDefault.TRUE);
    }
}
