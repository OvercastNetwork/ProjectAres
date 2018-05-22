package tc.oc.api.util;

import com.google.common.collect.Lists;
import tc.oc.minecraft.api.command.CommandSender;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Permissions {

    String CONSOLE = "ocn.console";
    String LOGIN = "ocn.login";
    String STAFF = "projectares.staff";
    String OBSERVER = "ocn.observer";
    String PARTICIPANT = "ocn.participant";
    String MAPMAKER = "ocn.mapmaker";
    String DEVELOPER = "ocn.developer";
    String MAPDEV = "pgm.mapdev";
    String MAPERRORS = "pgm.maperrors";

    /**
     * Merge the given by-realm permissions into a single set of permissions using the given (ordered) realms
     * @param realms          Effective realms, in application order (later realms will override earlier ones)
     * @param permsByRealm    Permissions, grouped by realm
     * @return                Effective permissions
     */
    static Map<String, Boolean> mergePermissions(Collection<String> realms, Map<String, Map<String, Boolean>> permsByRealm) {
        Map<String, Boolean> effectivePerms = new HashMap<>();
        for(String realm : realms) {
            Map<String, Boolean> perms = permsByRealm.get(realm);
            if(perms != null) {
                effectivePerms.putAll(perms);
            }
        }
        return effectivePerms;
    }

    /**
     * Get a list of enums a {@link CommandSender} has permission to use.
     *
     * This is useful for enums that correspond to an action. Instead of granting permission
     * to a user for each node, they have access to any enum below the highest ordinal node.
     * <code>
     * enum Trig {
     *     SOH, CAH, TOA
     * }
     * </code>
     * So if a sender has explicit permission to ocn.foo.cah, the sender has implicit
     * permission to use Trig.SOH and Trig.CAH.
     *
     * @param sender    The command sender.
     * @param enumClass The class of the enum to get the values from.
     * @return          List of {@link E}s that the {@param sender} is allowed to use, ascending order based on {@link E#ordinal()}.
     */
     static <E extends Enum> List<E> enumPermissions(CommandSender sender, String base, Class<E> enumClass) {
           final List<E> enums = Lists.newArrayList(enumClass.getEnumConstants());
           final Function<E, String> normalizer = value -> base + "." + value.name().toLowerCase().replaceAll("_", "-");
           final int max = enums.stream()
                                        .filter(value -> sender.hasPermission(normalizer.apply(value)))
                                        .map(E::ordinal)
                                        .max(Integer::compare)
                                        .orElse(-1);
           return enums.subList(0, max + 1);
     }

     /**
     * Get whether a {@link CommandSender} has permission to use a selected {@link E}.
     *
     * @see #enumPermissions(CommandSender, String, Class)
     * @return Whether the {@param sender} has permission.
     */
     static <E extends Enum> boolean hasPermissionForEnum(CommandSender sender, String base, E selected) {
           return enumPermissions(sender, base, (Class<E>) selected.getClass()).contains(selected);
     }
}
