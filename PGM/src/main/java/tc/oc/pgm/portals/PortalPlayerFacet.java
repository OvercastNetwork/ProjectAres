package tc.oc.pgm.portals;

import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

public class PortalPlayerFacet implements MatchPlayerFacet {

    private boolean teleported;

    /**
     * Mark this player as having used a portal in this tick, and return the previous value.
     *
     * Portals call this method before telporting a player, and skip the teleport if it returns true.
     * The flag is reset to false every tick, so this effectively limits players to one portal use
     * per tick, avoiding portal loops.
     *
     * If a teleport is skipped, the player will have to make the portal trigger go low and high
     * again in order to use that portal, i.e. leave and re-enter the portal. This is intended
     * behavior, and is essential for bidirectional portals.
     */
    public boolean teleport() {
        final boolean old = teleported;
        teleported = true;
        return old;
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        teleported = false;
    }
}
