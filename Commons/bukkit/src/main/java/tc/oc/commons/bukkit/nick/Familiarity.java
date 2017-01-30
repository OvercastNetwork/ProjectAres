package tc.oc.commons.bukkit.nick;

import tc.oc.commons.core.util.Orderable;

/**
 * Level of familiarity between two players. Comparisons are useful e.g.
 *
 *     identity.familiarity(viewer).noLessThan(Familiarity.FRIEND)
 */
public enum Familiarity implements Orderable<Familiarity> {
    ANONYMOUS,      // Don't know who they are
    PERSON,         // Know their name, never met them
    FRIEND,         // On friend list
    SELF            // Same person
}
