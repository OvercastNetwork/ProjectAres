package tc.oc.commons.bukkit.util;

import java.util.Map;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import tc.oc.api.util.Permissions;

public abstract class PermissionUtils {
    public static boolean isStaff(Permissible permissible) {
        return permissible.hasPermission(Permissions.STAFF);
    }

    public static void setPermissions(PermissionAttachment attachment,  Map<String, Boolean> permissions) {
        for(Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            attachment.setPermission(entry.getKey(), entry.getValue());
        }
    }
}
