package tc.oc.api.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Permissions {

    private Permissions() {}

    public static final String CONSOLE = "ocn.console";
    public static final String LOGIN = "ocn.login";
    public static final String STAFF = "projectares.staff";
    public static final String OBSERVER = "ocn.observer";
    public static final String PARTICIPANT = "ocn.participant";
    public static final String MAPMAKER = "ocn.mapmaker";
    public static final String DEVELOPER = "ocn.developer";
    public static final String MAPDEV = "pgm.mapdev";
    public static final String MAPERRORS = "pgm.maperrors";

    /**
     * Merge the given by-realm permissions into a single set of permissions using the given (ordered) realms
     * @param realms          Effective realms, in application order (later realms will override earlier ones)
     * @param permsByRealm    Permissions, grouped by realm
     * @return                Effective permissions
     */
    public static Map<String, Boolean> mergePermissions(Collection<String> realms, Map<String, Map<String, Boolean>> permsByRealm) {
        Map<String, Boolean> effectivePerms = new HashMap<>();
        for(String realm : realms) {
            Map<String, Boolean> perms = permsByRealm.get(realm);
            if(perms != null) {
                effectivePerms.putAll(perms);
            }
        }
        return effectivePerms;
    }
}
