package tc.oc.pgm.kits;

import javax.inject.Inject;

import org.bukkit.event.Listener;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;

/**
 * This used to do more, but currently just calls Kit.apply.
 *
 * It may have more uses in the future, so we keep it around.
 */
public class KitPlayerFacet implements MatchPlayerFacet, Listener {

    @Inject private MatchPlayer player;

    public void applyKit(Kit kit, boolean force) {
        kit.apply(player, force);
    }
}
